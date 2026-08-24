# 08 — MySQL / InnoDB deep dive

## Baseline

Production conservative: **MySQL 9.7 LTS** cho greenfield sau compatibility test; **8.4 LTS** vẫn phù hợp với fleet hiện hữu. Innovation **26.7** dùng để đánh giá sớm, không tự động là lựa chọn tốt hơn LTS.

## InnoDB storage mental model

- Table được tổ chức theo clustered primary key.
- Secondary-index leaf chứa primary-key value để tìm clustered row.
- Primary key ngẫu nhiên/dài làm split, locality kém và nhân kích thước secondary index.
- Nếu thiếu PK phù hợp, InnoDB phải chọn/tạo clustered key nội bộ; luôn thiết kế PK rõ.
- Buffer pool cache data/index pages; redo log bảo vệ crash recovery; undo hỗ trợ rollback/MVCC; purge dọn history.

## Isolation và lock

Default InnoDB là `REPEATABLE READ`. Locking read và update range có thể dùng record/gap/next-key locks để ngăn phantom theo semantics. Query thiếu index có thể scan/lock phạm vi lớn hơn dự kiến.

Theo dõi:

- long transaction và history list/purge lag;
- lock wait/deadlock;
- metadata locks làm DDL chờ;
- transaction mở nhưng idle;
- affected rows cho atomic conditional update.

Đọc deadlock report, không chỉ tăng timeout. Application phải retry transaction idempotently.

## Query và index

Composite B-tree theo leftmost prefix. `EXPLAIN ANALYZE`, Performance Schema và `sys` schema giúp tìm query tốn time/rows/temp. Histogram hỗ trợ column không index hoặc distribution skew; MySQL 8.4 có automatic histogram update, nhưng phải đo plan sau thay đổi.

JSON column không tự có index cho mọi path. Dùng generated/functional/multi-valued index theo exact query và version capability; relational columns vẫn tốt cho field trọng yếu, constraint và join.

## Replication và HA

- Binary log là nền replication/CDC/PITR workflow.
- GTID đơn giản hóa xác định transaction và failover.
- Async replica có lag/data-loss window; semi-sync không đồng nghĩa mọi replica đã apply.
- Group Replication/InnoDB Cluster hỗ trợ HA nhưng vẫn cần quorum, fencing, router/client retry và failure drill.
- Read replica cần consistency strategy, không chỉ connection routing.

## Connection và memory

MySQL dùng thread/session resources và per-connection buffers có thể làm memory tăng theo concurrency. Global buffer lớn cộng per-session worst case phải nằm trong container/host limit. Connection pool ở service cần giới hạn tổng fleet; query timeout/lock timeout và admission control chặn overload.

## DDL/migration

Online DDL support phụ thuộc exact operation/version/table. `ALGORITHM`/`LOCK` mong muốn phải được kiểm tra; “online” vẫn có metadata lock ở các phase và có thể tăng I/O/replication lag. Với bảng lớn:

- đo disk headroom và replica lag;
- canary trên production-like copy;
- dùng expand-contract;
- đặt timeout và kill/rollback strategy;
- xác minh trigger/online schema tool nếu dùng.

## Metrics ưu tiên

- query latency/digest, rows examined vs returned;
- buffer pool hit/dirty pages, redo pressure/checkpoint;
- connections running/waiting, thread concurrency;
- row lock wait/deadlocks, metadata locks;
- temp table/disk spill;
- replica lag, binlog disk/retention;
- disk latency/IOPS/capacity;
- long transactions/purge lag.

## Những điểm upgrade dễ vấp

- default và removed variables/options thay đổi giữa 8.0/8.4/9.x;
- authentication policy/plugin changes;
- terminology/API replication cũ bị loại bỏ;
- optimizer plan thay đổi sau upgrade/stats refresh;
- collation và reserved words;
- connector/driver/ORM/tool compatibility;
- chỉ upgrade theo supported path, dùng MySQL Shell upgrade checker và rehearsal.

Chi tiết timeline nằm trong [13-changelog-2023-2026.md](13-changelog-2023-2026.md).
