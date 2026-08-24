# Kafka Message Ordering & Partitioning

## 1. Guarantee thực tế

Kafka duy trì thứ tự record trong một partition theo offset. Không có total order giữa các partition. Trong một consumer group, một partition tại một thời điểm được gán cho tối đa một consumer; vì vậy tăng consumer vượt số partition không tăng parallelism.

Ordering cần được định nghĩa theo aggregate/invariant, không chọn key theo thói quen:

| Invariant cần giữ | Key thường phù hợp |
|---|---|
| lifecycle một order | `order_id` |
| số dư/ledger tuần tự của user/account | `account_id` hoặc `user_id` |
| biến động tồn kho một SKU | `campaign_id + sku_id` |
| trạng thái shipment | `shipment_id` |

Không thể vừa phân tán một hot SKU ra nhiều partition vừa có một serial order miễn phí cho chính SKU đó. Muốn shard, phải tách invariant thành quota/bucket độc lập rồi tổng hợp/reconcile.

## 2. Key trade-off

`order_id` cho parallelism tốt và đúng order lifecycle, nhưng hai order của cùng user có thể chạy song song. `user_id` serialize mọi order/payment của user nhưng user lớn có thể thành hot key. `sku_id` phù hợp inventory stream nhưng tất cả event hot SKU dồn một partition.

Một event có nhiều downstream ordering need có thể được project thành các topic theo aggregate, thay vì cố tìm một universal key.

## 3. Partition count và repartition

Kafka mặc định hash key theo số partition. Tăng partition có thể đổi mapping của key cho các record mới; cùng key cũ/mới có thể tồn tại ở hai partition trong giai đoạn chuyển đổi và phá giả định ordering end-to-end. Vì vậy:

- chọn partition count với growth headroom;
- nếu repartition, version topic (`orders.v2`) và migrate có kiểm soát;
- consumer dựa thêm `aggregate_version` để phát hiện stale/gap;
- theo dõi skew: bytes/records/lag theo partition, không chỉ tổng lag.

## 4. Retry có thể phá ordering

Nếu record offset 10 lỗi nhưng offset 11 được xử lý rồi mới đưa 10 sang retry topic, business order đã đảo dù source partition đúng thứ tự. Các chiến lược:

- strict order: pause partition, retry 10 có backoff/budget, sau đó DLQ và dừng/alert;
- key-scoped order: parking-lot theo key phức tạp hơn nhưng không chặn toàn partition;
- commutative/idempotent handler: cho phép out-of-order bằng version/conditional update;
- reorder buffer theo aggregate version, có timeout và gap recovery.

`max.poll.interval.ms`, batch size và processing time phải khớp; nếu handler quá lâu, rebalance tạo redelivery. Không giữ DB transaction mở trong lúc backoff/network retry.

## 5. Producer settings

Idempotent producer tránh duplicate do producer retry trong một producer session. Giữ `acks=all`, replication phù hợp và không vô hiệu các giới hạn bảo đảm ordering. Transactional producer chỉ atomically group Kafka writes/offsets trong Kafka; không bao phủ DB ngoài Kafka.

Event envelope nên có:

```json
{
  "eventId": "uuid",
  "eventType": "OrderConfirmed",
  "aggregateType": "Order",
  "aggregateId": "order-42",
  "aggregateVersion": 7,
  "occurredAt": "...",
  "schemaVersion": 2,
  "correlationId": "...",
  "causationId": "..."
}
```

Consumer kiểm tra `eventId` để dedupe và `aggregateVersion` để bỏ stale/phát hiện gap. Không dùng timestamp làm ordering authority vì clock skew và precision.

## 6. Câu trả lời phỏng vấn

“Tôi chọn key theo aggregate sở hữu invariant. Order lifecycle dùng `order_id`; account ledger dùng `account_id`; stock dùng `campaign+sku`. Kafka chỉ order trong partition, nên retry/DLQ và tăng partition đều phải được thiết kế để không âm thầm phá order. Consumer vẫn idempotent và version-aware vì ordering không loại bỏ duplicate.”

## 7. Checklist vận hành

- key null có bị round-robin/sticky partition ngoài ý muốn không?
- một key chiếm bao nhiêu phần trăm traffic?
- partition lag max là bao nhiêu, không chỉ average?
- schema evolution có backward/forward compatibility không?
- handler có inbox unique và conditional aggregate version không?
- DLQ có replay tool giữ nguyên event ID/key và audit không?
- khi replay, side effect external có idempotent không?
