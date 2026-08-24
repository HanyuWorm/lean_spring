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

### 1. Giữa Redis và Kafka đang có dual-write nào? Nếu process chết đúng giữa hai bước thì ai sửa?

**Trả lời:** Có dual-write nếu request giảm stock trong Redis rồi publish Kafka bằng hai thao tác độc lập. Process chết sau bước một tạo reservation mồ côi; chết trước bước một nhưng client nhận trạng thái không rõ có thể gây retry. Không thể giải quyết bằng `try/catch` đơn thuần. Cần lưu một reservation record có trạng thái và idempotency key, sau đó relay/reconciler định kỳ publish lại hoặc release reservation quá hạn. Consumer phải idempotent vì relay có thể publish trùng. Nếu dùng Redis Streams/Lua để tạo reservation và command atomically trong cùng Redis thì vẫn cần chiến lược đưa dữ liệu bền vững sang Kafka/DB và phục hồi khi Redis gặp sự cố.

### 2. Reservation TTL dài hơn worst-case Kafka lag bao nhiêu?

**Trả lời:** TTL không nên là một con số đoán. Ngân sách tối thiểu phải lớn hơn `queue lag cực đại + processing timeout + retry/backoff + thời gian phục hồi sự cố + safety margin`. Cần cảnh báo khi tuổi reservation tiến gần TTL và reconciler phải phân biệt reservation đang xử lý với reservation thật sự mồ côi. Nếu TTL hết trước khi consumer commit, hệ thống có thể vừa trả stock vừa tạo order, gây oversell.

### 3. Rebalance consumer có làm vượt processing timeout và redelivery không?

**Trả lời:** Có. Rebalance, xử lý lâu hơn `max.poll.interval.ms`, crash sau DB commit nhưng trước offset commit đều có thể làm message được giao lại. Thiết kế phải giả định at-least-once: dùng inbox/unique constraint theo `event_id`, transaction hóa inbox với business write, chỉ commit offset sau commit DB và điều chỉnh batch, poll interval, pause/resume theo thời gian xử lý thực tế. Không lấy việc “Kafka chỉ giao một lần trong test” làm invariant.

### 4. Có giới hạn số request đang chờ Hikari/Redis/Kafka hay chỉ giới hạn thread?

**Trả lời:** Phải giới hạn work-in-flight theo từng downstream. Thread pool hay Virtual Thread không đại diện cho sức chứa của DB, Redis hoặc Kafka. Dùng semaphore/bulkhead, bounded queue và deadline trước khi gọi dependency; khi hết capacity thì fail fast hoặc degrade. Hikari là hàng rào cuối cho connection DB, không thay thế admission control ở endpoint và không bảo vệ phần Redis/Kafka hay memory dành cho request đang chờ.

### 5. Dashboard có stock reserved, confirmed, released, DB total và reconciliation delta không?

**Trả lời:** Cần có. Dashboard tối thiểu theo dõi số lượng `available`, `reserved`, `confirmed`, `released`, stock bền vững trong DB, reconciliation delta, tuổi reservation lâu nhất và số lần sửa lệch. Một invariant mẫu là `initial stock = available + active reservations + confirmed sales` sau khi tính các release hợp lệ. Chỉ theo dõi HTTP success rate sẽ không phát hiện silent oversell hoặc stock bị kẹt.

### 6. Load test có failure injection, retry storm và hot-key distribution không?

**Trả lời:** Load test chỉ bắn happy-path request chưa đủ. Cần mô phỏng một campaign nóng, phân bố key lệch, client retry cùng và khác idempotency key, Kafka rebalance/redelivery, DB chậm, Redis timeout, relay crash giữa hai bước và dependency hồi phục. Tiêu chí đạt không chỉ là throughput/p99 mà còn gồm không oversell, không double charge, backlog thoát hết sau sự cố, reconciliation delta về 0 và hệ thống từ chối tải có kiểm soát.
