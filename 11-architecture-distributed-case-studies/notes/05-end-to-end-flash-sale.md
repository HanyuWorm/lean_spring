# Case Study: End-to-end Flash Sale

## 1. Invariant và mục tiêu

Invariant quan trọng hơn sơ đồ:

- không bán vượt quá tồn kho có thể cam kết;
- một user chỉ nhận một kết quả cho cùng idempotency key;
- một reservation chỉ chuyển trạng thái theo state machine hợp lệ;
- mọi reservation hết hạn phải được release hoặc reconciliation phát hiện;
- hệ thống overload phải từ chối sớm, không để DB tự sập.

SLO ví dụ: API tiếp nhận p99 dưới 200 ms; trả `ACCEPTED` sau khi reservation và enqueue thành công; kết quả cuối hội tụ dưới 30 giây; oversell bằng 0; tỷ lệ reservation treo nhỏ hơn ngưỡng cảnh báo.

## 2. Luồng đề xuất

```text
Client
  -> CDN/WAF/Gateway: auth, token bucket, request-size limit
  -> proof/captcha: làm phẳng burst của bot
  -> Application admission limit
  -> Redis Cluster Lua: validate user + atomic reserve stock
  -> Kafka: OrderRequested(key = campaignId/skuId hoặc orderId tùy invariant)
  -> Order worker: inbox + order + outbox trong một DB transaction
  -> Outbox relay/Debezium -> downstream events
  -> payment/fulfilment Saga
  -> polling/SSE trả state, không giữ request tạo đơn mở quá lâu
```

## 3. Vai trò từng tầng

### Gateway

Rate limit theo nhiều dimension: IP để chống flood, principal/device để chống abuse, campaign/SKU để bảo vệ hot resource. Token bucket cho phép burst nhỏ; concurrency limit bảo vệ tài nguyên đang chạy. Trả `429` kèm `Retry-After`, không retry đồng loạt.

Captcha/proof token phải được ký, TTL ngắn, dùng một lần hoặc gắn với user + campaign. Nó là traffic shaping, không phải authorization và không bảo đảm tồn kho.

### Cache

Caffeine phù hợp với product description, campaign window, feature flags và negative caching ngắn. Dữ liệu correctness-sensitive phải có version/TTL và invalidation. Không trừ stock độc lập ở từng JVM vì các node sẽ có các bản sao khác nhau.

### Redis reservation

Lua thực hiện trong một lần chạy: xác nhận campaign đang mở, user chưa mua, stock > 0, giảm stock, ghi reservation/idempotency marker và trả kết quả. Trên Redis Cluster, mọi key mà script truy cập phải cùng hash slot, ví dụ:

```text
flash:{campaign-42}:stock:sku-7
flash:{campaign-42}:buyer:user-9
flash:{campaign-42}:reservation:request-123
```

Không xóa reservation chỉ vì client timeout: request có thể đã thành công. Client gọi lại cùng idempotency key để nhận cùng kết quả.

### Kafka và worker

Kafka hấp thụ burst và tách latency tiếp nhận khỏi DB write. Nó không tạo thêm capacity cho DB; tốc độ consumer phải được giới hạn theo write capacity. Lag là một business signal: lag quá cao thì đóng nhận reservation mới trước khi vượt SLA/TTL.

Worker dùng `processed_message(event_id UNIQUE)` hoặc inbox table trong cùng transaction với `orders`. Sau khi commit, ghi event nghiệp vụ vào outbox trong chính transaction đó. Debezium/relay có thể publish trùng, nên consumer kế tiếp vẫn phải idempotent.

### Kết quả bất đồng bộ

Polling đơn giản, dễ scale và recover. SSE tốt cho cập nhật một chiều nhưng mỗi connection vẫn tiêu tốn socket/buffer và cần proxy timeout/heartbeat. WebSocket chỉ đáng dùng khi cần hai chiều. Kênh realtime là optimization; trạng thái bền trong DB vẫn là nguồn để client reconnect.

## 4. State machine

```text
RECEIVED -> RESERVED -> ORDER_CREATED -> PAYMENT_PENDING -> CONFIRMED
              |               |                 |
              +-----------> REJECTED/EXPIRED/COMPENSATING -> CANCELLED
```

Mỗi transition dùng optimistic version hoặc conditional update:

```sql
update reservation
set status = 'CONFIRMED', version = version + 1
where id = :id and status = 'PAYMENT_PENDING' and version = :expected_version;
```

`0 rows updated` nghĩa là stale/duplicate/transition không hợp lệ, không phải cứ retry vô hạn.

## 5. Compensation và reconciliation

Nếu DB create order thất bại sau Redis reserve, consumer retry trước. Khi quá retry budget, một compensating command release reservation bằng operation idempotent. Nếu compensation message cũng mất/chậm, scheduled reconciler so sánh reservation quá hạn với order DB và sửa lệch.

Phải quyết định rõ nguồn sự thật theo phase:

- Redis là admission/reservation ledger trong cửa sổ flash sale;
- DB là order/payment ledger bền;
- reconciliation nối hai ledger và báo chênh lệch.

Không có distributed transaction nên luôn tồn tại khoảng inconsistency tạm thời. Thiết kế tốt định nghĩa khoảng đó và cách hội tụ.

## 6. Failure matrix tối thiểu

| Điểm lỗi | Quan sát | Xử lý |
|---|---|---|
| Client timeout sau reserve | Client không biết kết quả | retry cùng key, replay kết quả |
| Redis reserve xong, Kafka publish lỗi | Reservation không có order event | transactional handoff phù hợp hoặc reservation log + relay/reconciler |
| Consumer commit DB, chưa commit offset | Kafka redelivery | inbox unique + replay kết quả |
| Outbox publish, relay crash trước mark | event có thể trùng | downstream idempotent |
| Payment thành công, response timeout | caller tưởng thất bại | provider idempotency key + query status |
| Hot partition | lag chỉ ở một partition | chọn key/shard campaign có chủ đích, giữ ordering scope |

## 7. Câu hỏi review kiến trúc

- Giữa Redis và Kafka đang có dual-write nào? Nếu process chết đúng giữa hai bước thì ai sửa?
- Reservation TTL dài hơn worst-case Kafka lag bao nhiêu?
- Rebalance consumer có làm vượt processing timeout và redelivery không?
- Có giới hạn số request đang chờ Hikari/Redis/Kafka hay chỉ giới hạn thread?
- Dashboard có stock reserved, confirmed, released, DB total và reconciliation delta không?
- Load test có failure injection, retry storm và hot-key distribution không?
