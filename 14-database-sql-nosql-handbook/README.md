# Database Engineering Handbook

Track database dành cho senior backend engineer và solution architect. Mục tiêu không phải học thuộc cú pháp, mà là biết dữ liệu được lưu, khóa, sao chép, phục hồi và mở rộng như thế nào; từ đó chọn đúng database và bảo vệ quyết định kiến trúc.

> Baseline tài liệu ngày **24/08/2026**: MySQL 9.7 LTS, PostgreSQL 18.6 và MongoDB 8.3.x. MySQL 8.4 vẫn là nhánh LTS hợp lệ. PostgreSQL 19 đang beta nên chỉ theo dõi, không dùng làm production baseline.

## Kết quả cần đạt

Sau track này, bạn phải có thể:

- thiết kế schema với constraint thể hiện đúng invariant;
- đọc execution plan, chọn index và chứng minh bằng số liệu;
- giải thích transaction, MVCC, lock, deadlock và isolation anomaly;
- thiết kế replication, backup/PITR, partitioning và sharding theo RPO/RTO;
- chọn SQL hay NoSQL dựa trên access pattern và consistency requirement;
- vận hành MySQL, PostgreSQL, MongoDB an toàn ở mức production;
- lập kế hoạch upgrade dựa trên release train, compatibility và rollback;
- review một database architecture bằng trade-off thay vì khẩu hiệu.

## Bản đồ nội dung

| Chương | Nội dung | Artifact cần làm |
|---|---|---|
| [01](notes/01-database-foundations.md) | Storage, WAL, cache, ACID, OLTP/OLAP | Vẽ write/read path |
| [02](notes/02-sql-basic-to-advanced.md) | SQL từ query cơ bản tới window/CTE/upsert | Hoàn thành query lab |
| [03](notes/03-relational-data-modeling.md) | Key, constraint, normalization, tenancy | Review commerce schema |
| [04](notes/04-indexes-and-query-plans.md) | Index, statistics, EXPLAIN, pagination | So sánh plan trước/sau index |
| [05](notes/05-transactions-locking-mvcc.md) | Isolation, lock, MVCC, deadlock | Chạy inventory race lab |
| [06](notes/06-replication-partitioning-sharding.md) | HA, replication, partition, shard, CDC | Viết RPO/RTO và failover plan |
| [07](notes/07-nosql-foundations.md) | KV/document/wide-column/graph/search/vector | Chọn model theo use case |
| [08](notes/08-mysql-deep-dive.md) | InnoDB, redo/undo, locking, GTID | Tune và explain MySQL |
| [09](notes/09-postgresql-deep-dive.md) | Heap/MVCC/VACUUM/WAL/index/JSONB | Tune và explain PostgreSQL |
| [10](notes/10-mongodb-deep-dive.md) | BSON, replica set, shard, aggregation | Model aggregate MongoDB |
| [11](notes/11-database-selection-and-architecture.md) | Decision framework và system design | Viết ADR chọn database |
| [12](notes/12-production-operations-security.md) | SLO, capacity, backup, migration, security | Diễn tập restore/migration |
| [13](notes/13-changelog-2023-2026.md) | Thay đổi chính ba năm gần đây | Lập upgrade checklist |
| [14](notes/14-interview-questions.md) | Câu hỏi từ cơ bản tới architect | Trả lời bằng diagram/trade-off |

Nguồn chính thức và ngày kiểm chứng nằm tại [SOURCES.md](SOURCES.md). Lab nằm trong [labs](labs/README.md).

## Cách học đề xuất

1. Tuần 1-2: chương 01-03 và SQL lab.
2. Tuần 3: chương 04-05; bắt buộc đọc plan và tái hiện race condition.
3. Tuần 4: chương 06-07; viết consistency matrix cho một hệ thống thật.
4. Tuần 5: chọn MySQL hoặc PostgreSQL làm primary deep dive; MongoDB làm comparative deep dive.
5. Tuần 6: chương 11-14, viết ADR và tổ chức mock architecture review.

Mỗi kết luận performance phải kèm workload, dataset, execution plan và percentile latency. Không chuyển benchmark của người khác thành con số cam kết cho hệ thống của mình.

## Quy tắc kiến trúc ngắn gọn

- Mặc định bắt đầu bằng relational database nếu invariant và transaction là trung tâm.
- NoSQL là lựa chọn theo data/access pattern, không phải từ đồng nghĩa với “scale”.
- Constraint trong database là lớp bảo vệ cuối; validation ở API không thay thế được.
- Index tăng tốc read nhưng làm tốn RAM/disk và tăng write amplification.
- Replica không tự động cho strong read-after-write; phải định nghĩa consistency của từng API.
- Backup chưa từng restore thành công chưa được xem là backup.
- Pool connection là admission control; không tăng vô hạn để “hợp” với Virtual Threads.
- Sharding là quyết định tổ chức và vận hành, không chỉ là kỹ thuật chia bảng.
