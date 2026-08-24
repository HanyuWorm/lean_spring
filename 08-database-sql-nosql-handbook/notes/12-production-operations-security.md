# 12 — Production operations và security

## SLO trước dashboard

Định nghĩa service-level indicators theo workload:

- availability của read/write;
- p50/p95/p99 latency theo query class;
- successful transaction rate;
- freshness/replication lag;
- backup success **và restore success**;
- RPO/RTO drill;
- data correctness/reconciliation discrepancy.

CPU 80% không tự là incident; p99 tăng do lock với CPU thấp vẫn là incident.

## Golden diagnostic path

1. Xác nhận impact, thời gian và query/tenant nào.
2. So request rate, connection/pool wait và DB sessions.
3. Kiểm tra blocking/deadlock/long transaction.
4. Kiểm tra query digest/plan regression/rows examined.
5. Kiểm tra CPU, memory/cache eviction, disk latency/capacity, network.
6. Kiểm tra checkpoint/vacuum/purge/compaction, replication lag.
7. Mitigate có giới hạn: admission control, kill exact offender, rollback deploy, route workload.
8. Bảo toàn evidence trước restart nếu an toàn.

Restart có thể xóa symptom và evidence, cache lạnh sau restart còn làm tail latency tệ hơn.

## Backup strategy

Áp dụng 3-2-1 khi phù hợp: nhiều bản sao, nhiều media/failure domain, một bản offsite/immutable. Bao gồm:

- base/full backup;
- transaction-log archive cho PITR;
- encryption và key recovery;
- retention/legal hold;
- checksum/integrity;
- catalog/version/tooling;
- automated restore test và quarterly disaster drill.

Replica không thay thế backup. Logical dump có lợi cho object-level restore/portability nhưng có thể chậm và không tự cho PITR.

## Schema migration an toàn

- Forward/backward-compatible theo expand-contract.
- Tách DDL khỏi backfill; batch nhỏ, checkpoint, throttle.
- Đặt lock/statement timeout để migration fail nhanh thay vì chặn production.
- Đo WAL/binlog/oplog, replica lag, disk/temp headroom.
- Canary và kill switch.
- Data validation bằng count/checksum/invariant.
- Roll-forward thường thực tế hơn rollback destructive DDL.

ORM auto-DDL (`ddl-auto=update`) không phải production migration governance. Dùng Flyway/Liquibase hoặc workflow tương đương có review.

## Least privilege

Tách role:

- application runtime: DML cần thiết, không DDL/admin;
- read-only/reporting: schema/table giới hạn;
- migration: DDL theo pipeline có audit;
- replication/backup/monitoring: quyền chuyên biệt;
- break-glass admin: MFA, time-bound, audit.

Không dùng root/superuser trong application. Rotate secret/certificate; dùng short-lived identity nếu platform hỗ trợ. TLS in transit, encryption at rest và key ownership phải rõ.

## SQL/NoSQL injection

Parameterized query không chỉ cho SQL; Mongo query object tạo từ untrusted operators cũng có NoSQL injection. Validate field/operator allowlist, không merge trực tiếp request JSON vào filter/update, và giới hạn query complexity/page size.

## PII và audit

- Data classification và minimization.
- Column/field encryption/tokenization cho dữ liệu nhạy cảm.
- Không log bind values chứa secret/PII tùy tiện.
- Audit tamper-resistant cho privileged/data-sensitive operation.
- Retention/delete propagation qua primary, replica, backup và projection có policy rõ.
- Tenant boundary test và authorization ở mọi access path.

## Capacity và overload

- Admission control trước khi DB queue vô hạn.
- Bounded connection pool, query/lock/acquisition timeout.
- Rate limit theo tenant/workload, không chỉ global.
- Tách batch/reporting khỏi interactive OLTP.
- Circuit breaker không chữa DB slow query nhưng ngăn retry storm.
- Retry exponential backoff + jitter + budget; chỉ retry transient/idempotent operation.
- Load shedding giữ critical transaction sống.

## Patch và upgrade

1. Theo dõi security/advisory/release notes chính thức.
2. Inventory server, driver, connector, extension/plugin và tooling.
3. Compatibility/upgrade checker.
4. Restore production-like snapshot đã sanitize.
5. Test correctness, plan/performance, failover và downgrade boundary.
6. Canary, observe, rồi rollout.
7. Chỉ bật compatibility/FCV/new feature sau observation window nếu product hỗ trợ.

“Latest” không đồng nghĩa “bật ngay”; nhưng ở lại patch cũ có security/correctness risk. Cần patch SLA theo severity.

## Runbook tối thiểu

- connection exhaustion;
- slow query/plan regression;
- lock storm/deadlock;
- disk nearly full/WAL-oplog-binlog growth;
- replica lag/failure/failover;
- backup failure/restore;
- corrupt data/reconciliation;
- credential compromise;
- online migration abort;
- region failure.

Mỗi runbook có symptom, safe diagnostic commands, decision owner, mitigation, rollback, validation và escalation.
