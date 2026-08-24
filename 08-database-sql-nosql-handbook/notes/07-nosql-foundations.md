# 07 — NoSQL foundations

NoSQL là họ database với data model và consistency khác nhau, không phải một sản phẩm hay một consistency level.

## Chọn theo access pattern

| Loại | Điểm mạnh | Ví dụ use case | Cảnh báo |
|---|---|---|---|
| Key-value | O(1)-like lookup theo key, TTL | session, cache, rate-limit state | Query ngoài key hạn chế |
| Document | Aggregate JSON/BSON linh hoạt | catalog, content, profile | Document growth, duplicate/reference |
| Wide-column | Write scale và partition-key query | telemetry, event/time series | Query-first schema, hot partition |
| Graph | Traversal nhiều hop | fraud, network, recommendation | Scale/distribution và query cost |
| Search | inverted index, relevance | full-text/log search | Không nên là source of truth transaction |
| Vector | nearest-neighbor similarity | semantic retrieval | recall/latency/index rebuild/filtering |

Một hệ thống có thể dùng nhiều store, nhưng mỗi store thêm backup, security, upgrade, on-call và consistency cost. Polyglot persistence phải mua được giá trị đủ lớn.

## CAP và PACELC

CAP nói khi **network partition** xảy ra, hệ thống phân tán không thể đồng thời đảm bảo availability cho mọi request và linearizable consistency. Nó không nói “chọn hai trong ba” cho mọi thời điểm.

PACELC bổ sung: nếu có Partition chọn Availability hay Consistency; Else, bình thường chọn Latency hay Consistency. Product/database còn cho tuning theo operation, nên cần đọc exact read/write concern chứ không gắn nhãn CP/AP đơn giản.

## BASE không phải “không transaction”

Basically Available, Soft state, Eventual consistency mô tả một cách chấp nhận hội tụ. Nhiều NoSQL hỗ trợ transaction; câu hỏi là transaction scope/cost có phù hợp data model và throughput không.

## Document modeling

**Embed khi:** dữ liệu được đọc/ghi cùng nhau, lifecycle phụ thuộc, cardinality bounded, cần atomic single-document update.

**Reference khi:** entity có lifecycle độc lập, many-to-many, cardinality/unbounded growth, được chia sẻ/cập nhật riêng.

Đừng nhúng event/history vô hạn vào một document. Dùng bucket/time-series/separate collection. MongoDB có giới hạn BSON document 16 MiB.

## Denormalization discipline

Duplicate field cần:

- canonical owner;
- expected staleness;
- propagation mechanism;
- version/timestamp;
- reconciliation/backfill;
- behavior khi projection unavailable.

Ví dụ order item giữ `productNameAtPurchase` là snapshot lịch sử; catalog projection giữ `currentPrice` duplicated cần CDC/reconciliation.

## Consistency per use case

| Use case | Requirement điển hình |
|---|---|
| Payment ledger | strong invariant, durable idempotency, audit |
| Shopping cart | session read-your-write; merge conflict policy |
| Product search | eventual projection chấp nhận được |
| Inventory reservation | atomic conditional change, bounded oversell |
| Analytics | snapshot/batch freshness theo SLA |
| Notification | at-least-once + deduplication |

Không chọn consistency chung cho cả hệ thống; chọn theo command/query/invariant.

## Secondary index vẫn có giá

NoSQL không loại bỏ index. Secondary index có thể fan-out, tốn storage/write, yêu cầu global coordination hoặc chỉ local theo partition. Query thiếu partition/shard key dễ scatter-gather.

## Schema-flexible không phải schema-less

Schema vẫn tồn tại trong producer, consumer và dữ liệu cũ. Cần validation, versioning, backward/forward compatibility và backfill. Thay đổi field type nguy hiểm hơn thêm optional field. Contract test và migration telemetry vẫn cần.

## Anti-patterns

- chọn MongoDB chỉ vì payload là JSON;
- dùng Redis làm durable system of record mà không hiểu persistence/failover;
- dùng search index làm ledger;
- tạo distributed transaction để bù data model sai;
- shard trước khi tối ưu query/index/vertical capacity;
- xem eventual consistency như excuse cho dữ liệu sai không giới hạn;
- không có repair/reconciliation.
