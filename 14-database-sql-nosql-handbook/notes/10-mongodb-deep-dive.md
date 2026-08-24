# 10 — MongoDB deep dive

## Baseline

MongoDB **8.3.x** là latest stable series tại ngày tài liệu; dùng latest patch đã kiểm thử. MongoDB 8.2/8.3 có một số preview feature (Search/Vector Search self-managed, Queryable Encryption range/text capability tùy release); preview không nên đặt vào critical production path nếu chưa được product support matrix cho phép.

## Document và aggregate

BSON document tối đa 16 MiB. Single-document write là atomic, vì vậy embed các thành phần cùng invariant/lifecycle có lợi. Nhưng array unbounded tạo document growth, contention và giới hạn size.

Ví dụ order embed line item snapshot; customer và product reference bằng id. Không embed toàn bộ order history vào customer.

Schema validation (`$jsonSchema`) bảo vệ shape/type ở DB. Application versioning vẫn cần để đọc document cũ.

## WiredTiger và cache

WiredTiger quản lý cache, compression, checkpoints và MVCC-like snapshots. OS page cache vẫn tồn tại; đừng đặt cache database bằng toàn RAM container mà bỏ headroom. Working set/index lớn hơn memory làm eviction/I/O và tail latency tăng.

Transaction dài/lớn tăng cache pressure và conflict. MongoDB hỗ trợ multi-document transaction, nhưng nếu phần lớn command cần cross-document transaction thì review aggregate model trước.

## Replica set

Primary nhận write; secondaries replicate oplog. Election/failover không loại bỏ transient errors. Driver retryable read/write và application idempotency phải đúng.

- **Write concern:** mức acknowledgement/durability yêu cầu.
- **Read concern:** isolation/consistency view của read.
- **Read preference:** node nào được đọc; secondary read có thể stale.
- **Causal consistency/session:** hỗ trợ ordering/read-your-write theo điều kiện.

`w: "majority"` không thay thế journaling/storage/region architecture discussion. MongoDB 8.0 thay đổi chi tiết majority acknowledgement; đọc release note trước khi dựa vào timing “applied on secondary”.

## Sharding

`mongos` route query; config server giữ metadata; shard chứa chunks/ranges. Shard key quyết định distribution, targeted query, migration và hotspot.

- Hashed key phân phối tốt nhưng range query scatter.
- Monotonic key có thể hotspot vào chunk mới nhất.
- Low-cardinality key tạo jumbo/hot distribution.
- Compound key có thể cân bằng locality và distribution.

Resharding có cost I/O/network/storage; 8.x cải thiện workflow nhưng không biến shard-key choice thành miễn phí. Theo dõi chunk distribution, migration, orphan cleanup và balancer.

## Index và query

Compound index có prefix/order rules. Multikey index áp dụng khi array; có constraint khi nhiều array path. TTL cleanup không xảy ra chính xác tại expiry millisecond và không phải compliance deletion SLA. Wildcard index tiện cho dynamic attributes nhưng rộng/tốn chi phí; explicit index tốt hơn cho hot path.

```javascript
db.orders.find({ customerId: 42, status: "PAID" })
  .sort({ createdAt: -1 })
  .explain("executionStats")
```

So sánh `nReturned`, `totalKeysExamined`, `totalDocsExamined`, winning plan và execution time. Aggregation pipeline nên filter/project sớm khi semantics cho phép; tránh chuyển document khổng lồ qua mọi stage.

## Change streams

Change streams dựa trên oplog cho CDC/event reaction. Consumer phải resume token, xử lý invalidate/history loss, duplicate/retry và event ordering theo scope. Không để change stream là bản sao duy nhất của business event nếu cần audit lâu dài; thiết kế retention/replay/reconciliation.

## Operations

- Backup nhất quán với topology và test restore.
- Upgrade theo supported path và đặt Feature Compatibility Version (FCV) có chủ đích.
- Sau binary upgrade, đừng nâng FCV ngay nếu muốn observation/rollback window.
- Theo dõi replication lag, oplog window, cache eviction, page faults/I/O, connections, ticket/queue, query targeting, chunk balance và disk.
- Dùng latest patch vì maintenance releases thường gồm correctness/security fixes.

## Anti-patterns

- một giant collection không shard key strategy;
- document vô hạn;
- mọi quan hệ đều `$lookup` như relational join;
- `secondaryPreferred` cho endpoint cần read-your-write mà không có session contract;
- transaction lớn thay cho aggregate design;
- index wildcard mọi thứ;
- nâng FCV mà chưa kiểm tra downgrade/rollback.
