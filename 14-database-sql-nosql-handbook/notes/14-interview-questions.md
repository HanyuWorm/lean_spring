# 14 — 60 câu hỏi database từ cơ bản tới architect

Mỗi câu trả lời nên có: định nghĩa, cơ chế, trade-off, ví dụ và cách đo/quan sát. Với câu system design, nói rõ assumption trước.

## A. Fundamentals và SQL

### 1. Database index là gì?

Cấu trúc dữ liệu phụ trợ ánh xạ key tới row/document/location, đổi storage và write amplification để giảm read. Nêu loại index, query operator, selectivity và maintenance; “index làm query nhanh” là chưa đủ.

### 2. Primary key khác unique key thế nào?

PK là identity chính, non-null và mỗi table có một PK; có thể có nhiều unique constraints. Cả hai enforce uniqueness nhưng storage/clustered behavior khác engine. InnoDB cluster table theo PK; PostgreSQL heap không cluster bền theo PK mặc định.

### 3. `WHERE` khác `HAVING`?

`WHERE` lọc row trước aggregation; `HAVING` lọc group sau `GROUP BY`. Đặt predicate không phụ thuộc aggregate vào `WHERE` thường giảm input sớm.

### 4. `COUNT(*)`, `COUNT(1)`, `COUNT(column)`?

`COUNT(*)` đếm row; `COUNT(column)` bỏ null; `COUNT(1)` về semantics đếm row và optimizer hiện đại thường tương đương `COUNT(*)`. Chọn biểu đạt rõ, không dựa vào folklore performance.

### 5. `INNER JOIN` và `LEFT JOIN`?

Inner chỉ giữ match; left giữ mọi row bên trái và null-extend bên phải. Filter cột phải trong `WHERE` có thể loại null và biến semantics gần inner join.

### 6. Vì sao `NOT IN` có thể sai khi có NULL?

SQL dùng three-valued logic; một null trong subquery khiến so sánh có thể unknown. `NOT EXISTS` thường diễn đạt anti-join an toàn hơn với null semantics rõ.

### 7. `UNION` khác `UNION ALL`?

`UNION` loại duplicate, thường cần sort/hash; `UNION ALL` giữ tất cả và rẻ hơn. Chỉ trả cost dedup khi business semantics cần.

### 8. CTE luôn nhanh hơn subquery?

Không. CTE chủ yếu cho readability/recursion; optimizer có thể inline hoặc materialize tùy engine/version/hint. Đọc plan.

### 9. Window function khác `GROUP BY`?

Window tính trên partition nhưng giữ mỗi row; group by co row theo group. Dùng cho rank, running total, lag/lead, top-N per group.

### 10. Vì sao keyset pagination tốt hơn offset sâu?

Nó seek từ last stable key nên tránh scan/skip lớn và ít drift dưới concurrent write. Cần deterministic order, unique tie-breaker và index phù hợp; không nhảy arbitrary page dễ như offset.

### 11. Sargable là gì?

Predicate cho phép engine dùng index boundary/lookup, ví dụ range trực tiếp trên column. Function/cast implicit/leading wildcard có thể làm mất khả năng đó trừ khi có functional/special index.

### 12. Vì sao không dùng floating point cho tiền?

Binary floating point không biểu diễn chính xác nhiều decimal fraction. Dùng integer minor units hoặc fixed precision decimal và định nghĩa rounding/currency scale.

## B. Modeling và index

### 13. Khi nào dùng natural key?

Khi thật sự unique, nhỏ, stable và được mọi producer thống nhất. Nếu business key có thể đổi/dài/PII, dùng surrogate PK và unique business constraint.

### 14. Foreign key có làm chậm hệ thống?

Có validation/locking/write cost nhưng bảo vệ referential integrity cho mọi writer. Index referencing side, thiết kế cascade và transaction đúng. Chỉ bỏ FK khi distributed ownership/scale có lý do và có cơ chế integrity/reconciliation thay thế.

### 15. Normalization và denormalization?

Normalization giảm anomaly và làm source of truth rõ. Denormalization tối ưu access pattern bằng duplicate/precompute; cần staleness SLA, sync và repair. Không phải SQL luôn normalized còn NoSQL luôn denormalized.

### 16. Composite index chọn thứ tự cột thế nào?

Dựa trên actual predicate, equality/range/order, cardinality/skew và covering need. Equality thường trước range/order nhưng không phải luật tuyệt đối; kiểm tra leftmost prefix và execution plan.

### 17. Covering index là gì?

Index chứa đủ dữ liệu trả query để giảm base-table access. PostgreSQL index-only scan còn phụ thuộc visibility map; MySQL secondary lookup behavior khác. Covering quá rộng làm write/storage tăng.

### 18. Vì sao index boolean thường kém?

Selectivity thấp nên đọc phần lớn table, random lookup có thể đắt hơn scan. Partial index trên minority state hoặc composite theo tenant/status/time có thể hữu ích.

### 19. Over-indexing gây gì?

Insert/update/delete chậm, WAL/redo/oplog và replication tăng, cache bị chia, storage/backup/DDL tốn hơn, optimizer có nhiều candidate. Review unused/duplicate index có observation window.

### 20. Index có đảm bảo query luôn dùng nó?

Không. Optimizer chọn cost estimate; scan có thể rẻ hơn nếu trả nhiều row, stats/skew/parameter/correlation ảnh hưởng plan. Hint cũng không thay data/query design.

### 21. Estimate lệch actual trong plan nghĩa là gì?

Planner hiểu sai cardinality, dẫn tới join/access choice kém. Kiểm tra stats age/sample, skew, correlated columns, expression và parameter; dùng histogram/extended statistics khi phù hợp.

### 22. N+1 query là gì?

Một query lấy N parent rồi N query riêng lấy child. Tail latency/DB round trip tăng. Sửa bằng join/fetch plan/batch/projection tùy cardinality; tránh join fetch nhiều collection gây Cartesian explosion.

### 23. Soft delete có chi phí gì?

Filter toàn hệ thống, unique/FK/lifecycle phức tạp, index/bloat, privacy deletion không giải quyết. Chỉ dùng với explicit restore/audit requirement và archive/purge strategy.

### 24. UUIDv4, UUIDv7 và bigint?

Bigint nhỏ/locality tốt nhưng centralized allocation/leak sequence. v4 phân tán nhưng random/lớn; v7 time-ordered cải thiện locality. Cân nhắc security, merging, index width, clock/library và engine.

## C. Transaction, locking, MVCC

### 25. MVCC có loại bỏ lock không?

Không. Nó giảm read-write blocking bằng versions/snapshots; writer-writer, metadata/DDL và explicit locks còn. Old versions cần vacuum/purge và long transaction gây retention.

### 26. Lost update và cách tránh?

Hai transaction đọc cùng value rồi ghi đè. Dùng atomic update, optimistic version, pessimistic lock hoặc isolation/serialization phù hợp. Test affected rows và retry idempotently.

### 27. Write skew là gì?

Hai transaction đọc cùng snapshot, update row khác và phá invariant đa row, dù không lost update. Cần serializable, materialize invariant/lock common row, constraint/trigger phù hợp hoặc redesign boundary.

### 28. Deadlock khác blocking?

Blocking là chờ; deadlock là cycle không thể tự tiến nên DB abort victim. Consistent lock order giảm deadlock; timeout và observability xử lý blocking. Application retry bounded transaction.

### 29. Vì sao transaction phải ngắn?

Giữ lock/snapshot/connection/versions, tăng contention, bloat/purge lag và failure cost. Không gọi remote service hoặc chờ user trong DB transaction.

### 30. `SELECT FOR UPDATE` vì sao nguy hiểm ở flash sale?

Mọi request cùng hot row xếp hàng nhưng vẫn giữ connection/thread/request budget, làm pool exhaustion và timeout cascade. Atomic conditional update, admission control, queue hoặc reservation model tốt hơn tùy invariant.

### 31. Optimistic hay pessimistic locking?

Optimistic hợp conflict thấp/transaction ngắn, abort/retry khi version mismatch. Pessimistic hợp conflict dự kiến cao hoặc decision cần stable state, nhưng tăng wait/deadlock. Đo conflict rate và retry cost.

### 32. Isolation level cao nhất luôn tốt nhất?

Không; correctness cao hơn thường thêm abort, coordination hoặc giảm concurrency. Chọn mức thấp nhất vẫn bảo vệ invariant và thêm atomic/constraint. `SERIALIZABLE` cần retry strategy.

### 33. Idempotency key thiết kế thế nào?

Scope theo tenant/operation, unique DB constraint, lưu request hash + status/result trong cùng transaction. Duplicate cùng payload trả result; payload khác conflict. Cache TTL chỉ giảm tải, không thay durable uniqueness.

### 34. Tại sao retry transaction phải bắt đầu transaction mới?

Transaction đã deadlock/serialization failure thường bị abort/rollback-only; retry bên trong cùng context không hợp lệ. Retry outer operation, có backoff/jitter/budget và idempotency.

### 35. Virtual Threads có cần connection pool?

Có. Virtual Threads làm blocking thread rẻ hơn nhưng DB connection/CPU/I/O/locks không rẻ hơn. Pool bounded là bulkhead; có acquisition timeout và concurrency admission theo DB capacity.

### 36. Spring `@Transactional` self-invocation?

Proxy-based advice thường chỉ chạy khi call đi qua proxy; method trong cùng object gọi nhau có thể không mở transaction mong muốn. Tách bean/use TransactionTemplate/AspectJ phù hợp và test boundary.

## D. MySQL, PostgreSQL, MongoDB

### 37. InnoDB clustered PK ảnh hưởng ra sao?

Data row tổ chức theo PK và secondary leaf chứa PK. PK dài/random làm secondary indexes lớn, page split/locality kém. Chọn PK nhỏ và locality phù hợp.

### 38. InnoDB gap/next-key lock là gì?

Lock record cùng khoảng key để ngăn insert phantom trong range ở locking operations/isolation phù hợp. Query/index kém có thể lock range rộng. Exact behavior phụ thuộc statement/isolation/index uniqueness.

### 39. Redo và undo trong InnoDB?

Redo ghi thay đổi để crash recovery/durability; undo hỗ trợ rollback và consistent reads/old versions. Long transaction làm undo history/purge lag tăng.

### 40. PostgreSQL VACUUM để làm gì?

Reclaim dead tuple space để reuse, cập nhật visibility map, freeze transaction IDs chống wraparound. Nó không thường trả file space về OS; `VACUUM FULL` rewrite và lock nặng.

### 41. Vì sao PostgreSQL bloat?

MVCC update/delete tạo dead versions; long snapshot, autovacuum không theo kịp, fillfactor/index update/workload làm space không reuse hiệu quả. Diagnose trước khi rewrite; tune per table.

### 42. PostgreSQL index-only scan có luôn chỉ đọc index?

Không. Nó cần visibility map chứng minh tuple visible; nếu page chưa all-visible vẫn phải heap fetch. VACUUM và update pattern ảnh hưởng.

### 43. PostgreSQL `work_mem` đặt càng lớn càng tốt?

Không. Có thể được cấp cho nhiều sort/hash node ở nhiều concurrent query/process; global memory có thể bùng nổ. Tune theo workload/role/query và quan sát spill.

### 44. MongoDB embed hay reference?

Embed khi cùng lifecycle/read/write, bounded cardinality và cần single-document atomicity. Reference khi independent/shared/many-to-many/unbounded. Định lượng document growth và query fan-out.

### 45. MongoDB transaction có nghĩa nên model như relational?

Không. Transaction hữu ích nhưng multi-document/distributed transaction có overhead/conflict/limits. Aggregate design vẫn ưu tiên invariant trong document khi tự nhiên; không ép embed sai chỉ để né transaction.

### 46. Read concern, write concern, read preference?

Lần lượt điều khiển consistency view, acknowledgement/durability và node đọc. Phối hợp với session/causal consistency; secondary read có thể stale. Nêu contract theo endpoint.

### 47. MongoDB shard key tốt?

Cardinality cao, distribution đều, xuất hiện trong targeted queries, immutable, tránh hotspot và hỗ trợ locality cần thiết. Query thiếu shard key scatter-gather; hashed key đánh đổi range locality.

### 48. Oplog window quan trọng vì sao?

Secondary/change stream consumer chỉ catch up/resume trong retained history phù hợp. Lag vượt window có thể cần resync hoặc mất resume history; monitor time window, rate và disk.

## E. Distributed data và operations

### 49. Replica có phải backup?

Không. Error/delete/corruption có thể replicate. Backup có retention, isolation/immutability và PITR; chỉ có giá trị sau restore test.

### 50. RPO và RTO?

RPO là data-loss window tối đa; RTO là recovery-time tối đa. Phải nêu failure scope. Chúng drive replication, backup, topology, automation và cost.

### 51. Async replica có rủi ro gì?

Lag/stale read và data loss khi promote replica chưa nhận/apply write. Cần lag metric, routing/read-after-write, promotion eligibility/fencing và client retry/idempotency.

### 52. Partitioning khác sharding?

Partitioning chia logical table/collection thành phần; có thể trên một database. Sharding phân ownership qua node/shard, thêm routing/rebalancing/cross-shard coordination. Partitioning không tự cho horizontal HA.

### 53. Transactional Outbox giải quyết gì?

Tránh dual-write DB + broker bằng cách ghi state và outbox cùng local transaction, rồi relay/CDC publish. Delivery vẫn có duplicate; consumer idempotent, ordering và outbox cleanup/lag required.

### 54. Exactly-once có thật không?

Có thể có trong boundary/protocol cụ thể, nhưng end-to-end qua DB, broker, HTTP side effect cần transaction/idempotency/reconciliation. Đòi hỏi định nghĩa scope; slogan không bảo vệ charge/email hai lần.

### 55. Online schema migration an toàn?

Expand-contract, backward-compatible app, DDL/backfill tách, batch/throttle/checkpoint, timeout, disk/log/lag monitor, validation và canary. Destructive contract ở release sau; roll-forward plan.

### 56. Khi database chậm, tăng pool connection có đúng?

Thường làm queue chuyển vào DB, tăng contention/context/memory và tail latency. Tìm bottleneck; giữ bounded pool, admission/load shedding, fix query/index/lock/capacity. Pool tối ưu bằng load test và total fleet budget.

### 57. Chọn SQL hay MongoDB cho catalog?

Không đủ dữ kiện. Hỏi attribute variability, relationship, aggregate size/growth, write transaction, query/filter/search, consistency, team/ops và migration. SQL+JSONB, MongoDB hoặc relational source + search projection đều có thể đúng.

### 58. Thiết kế database cho multi-region payment?

Nêu invariant/ledger, home region/ownership, latency vs consistency, idempotency, fencing, quorum/failover, currency/account partition, RPO/RTO và reconciliation. Không hứa active-active write nếu chưa giải quyết concurrent ownership/conflict.

### 59. Làm sao review upgrade database?

Inventory versions/plugins/drivers, đọc mọi release/removal/security note, supported path, production-like restore, compatibility checker, plan/performance/correctness/failover/backup test, canary và rollback/FCV window.

### 60. Database architecture tốt được chứng minh bằng gì?

Invariant tests dưới concurrency; representative load/plan; SLO/lag/repair telemetry; restore/failover/migration drills; capacity/headroom model; ownership/runbook; ADR ghi trade-off. Diagram đẹp không phải bằng chứng.

## Cách tự luyện

- Câu 1-24: trả lời trong 2 phút và viết một query/schema minh họa.
- Câu 25-48: vẽ read/write path hoặc timeline transaction.
- Câu 49-60: trả lời 10-15 phút, nêu assumption, failure modes, metrics và alternatives.
- Nếu đưa con số performance, nói nguồn/workload và cách benchmark lại; không dùng con số tuyệt đối không có context.
