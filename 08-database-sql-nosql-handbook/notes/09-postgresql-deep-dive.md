# 09 — PostgreSQL deep dive

## Baseline

**PostgreSQL 18.6** là current stable tại ngày tài liệu. PostgreSQL 19 Beta 3 chỉ dùng compatibility/lab, không production. PostgreSQL major upgrade cần `pg_upgrade`, dump/restore hoặc logical replication; minor release update in-place theo hướng dẫn package/provider.

## Heap và MVCC

PostgreSQL table heap chứa tuple versions; B-tree index trỏ tới tuple location. Update thường tạo version mới. Dead tuples cần VACUUM; autovacuum không phải optional housekeeping.

- **VACUUM:** reclaim space để reuse, cập nhật visibility map và chống transaction ID wraparound.
- **ANALYZE:** cập nhật planner statistics.
- **HOT update:** có thể tránh thêm index tuple khi indexed columns không đổi và page còn chỗ.
- **Long transaction/replication slot:** giữ old versions/WAL, gây bloat hoặc disk exhaustion.
- **VACUUM FULL:** rewrite/lock nặng, không phải default cure cho bloat.

Theo dõi dead tuples, autovacuum progress, oldest transaction, replication slot lag và table/index growth.

## Isolation

Default là `READ COMMITTED`: mỗi statement thấy snapshot tại đầu statement. `REPEATABLE READ` dùng snapshot transaction và ngăn nhiều anomaly nhưng concurrent conflict có thể abort. `SERIALIZABLE` dùng Serializable Snapshot Isolation, có thể phát hiện dangerous structure và trả serialization failure; application phải retry toàn transaction.

Row locks không chặn plain MVCC reads, nhưng writer/DDL/advisory/application locks vẫn tạo blocking. `SKIP LOCKED` hữu ích cho queue workers nhưng không nên dùng như general consistency shortcut.

## Index toolbox

- B-tree: equality/range/order.
- GIN: JSONB, array, full-text theo operator class.
- GiST/SP-GiST: range, geometric, nearest-neighbor và cấu trúc chuyên biệt.
- BRIN: bảng cực lớn khi physical order tương quan với value.
- Partial/expression indexes: tối ưu subset/expression.
- `INCLUDE`: covering payload, nhưng index-only scan còn phụ thuộc visibility map.

Extension/operator class quyết định khả năng exact. Không nói “GIN index JSON” mà phải nêu operator/query.

## Planner và observability

Sử dụng `EXPLAIN (ANALYZE, BUFFERS, WAL, SETTINGS)` cẩn thận. `pg_stat_statements` cho query fingerprints; `pg_stat_io` (PG16+) tách I/O context; `auto_explain` có overhead và cần cấu hình có chọn lọc.

Estimate sai thường do stale stats, skew, correlation, expression hoặc cross-column dependency. Tăng statistics target/extended statistics có chọn lọc; không tắt join method toàn cục như first response.

## WAL, checkpoint và replication

WAL hỗ trợ crash recovery, physical streaming replication, archiving/PITR và logical decoding. Checkpoint quá gắt tạo write spike; quá thưa tăng recovery/WAL volume. Replica sync/async là durability/latency trade-off.

Logical replication phù hợp selective replication/migration/version transition nhưng DDL/sequence/large-object semantics và slot retention cần kế hoạch. Slot bị bỏ quên có thể giữ WAL đến đầy disk.

## Connection model

PostgreSQL có backend process theo connection; hàng nghìn idle/active sessions vẫn có cost và active query cạnh tranh work memory/CPU. Dùng Hikari pool bounded; PgBouncer khi topology/workload cần. Transaction pooling có giới hạn với session state, temp table, prepared statement/advisory lock; phải test driver/framework behavior.

`work_mem` có thể cấp theo sort/hash node, không phải tổng per connection đơn giản. Nhân worst case với concurrent operations trước khi tăng.

## JSONB và relational

JSONB tốt cho attributes thay đổi và query có index phù hợp. Không dùng JSONB để né mọi schema/constraint/join. Field có invariant, join, high-selectivity filter hoặc update độc lập thường nên là typed column/table. Có thể kết hợp relational core + JSONB extension attributes.

## Metrics ưu tiên

- p95/p99 query, calls/total_exec_time từ query stats;
- active/idle-in-transaction/wait events;
- locks/deadlocks/serialization abort;
- buffer/I/O/temp spill/checkpoint/WAL rate;
- dead tuples, vacuum/analyze age, bloat signal;
- replica replay lag và slot retained WAL;
- connection saturation;
- disk capacity/latency.
