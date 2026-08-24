# 11 — Chọn database và review architecture

## Default có chủ đích

Với hệ thống nghiệp vụ mới, relational database thường là default tốt khi:

- invariant/transaction quan trọng;
- data relationship chưa ổn định;
- cần ad-hoc query/report;
- team có năng lực vận hành SQL;
- scale hiện tại chưa chứng minh cần specialized store.

Chọn specialized database khi access pattern và SLO tạo lợi ích đo được, không phải vì resume-driven architecture.

## Decision matrix

Chấm từng candidate theo 1-5 và ghi bằng chứng:

| Dimension | Câu hỏi |
|---|---|
| Correctness | Atomicity scope, constraint, isolation có bảo vệ invariant? |
| Access pattern | Point/range/join/traversal/search/vector/aggregate nào là critical? |
| Scale | Data/QPS/growth/hot key/working set và peak? |
| Consistency | Read-your-write, bounded staleness, ordering? |
| Availability | Region/node failure; quorum; degraded mode? |
| Operability | Backup/restore, upgrade, monitoring, tooling, on-call skill? |
| Evolution | Schema migration, new query, exit/migration strategy? |
| Security | Encryption, audit, tenant isolation, ecosystem compliance? |
| Cost | License/service, compute/storage/network, people/complexity? |

Weight theo business. Payment ưu tiên correctness/audit; search ưu tiên relevance/read scale/freshness.

## Reference choices

### Payment ledger

Relational DB với immutable double-entry entries, unique idempotency, transaction và reconciliation. Cache/search có thể phục vụ query nhưng không là source of truth. Không “update balance rồi hy vọng event tới”.

### Product catalog

Relational + JSONB hoặc document DB đều có thể đúng. Chọn theo variability, aggregate size, relationship, authoring transaction và query. Search engine là projection cho full text/facet; catalog source vẫn phải rõ.

### Shopping cart/session

Key-value/document store phù hợp nếu lookup theo user/session, TTL và availability cao. Phải định nghĩa merge khi multi-device, durability và checkout snapshot.

### Event/telemetry

Append/time-partitioned/columnar hoặc wide-column tùy ingest và query. Không ép raw event retention dài vào primary OLTP. Tách immutable raw data và derived aggregates.

### Fraud graph

Graph database hữu ích khi traversal nhiều hop là core và khó biểu diễn hiệu quả. Nếu chỉ lookup 1-2 hop cố định, relational indexes có thể đơn giản hơn.

## System of record và projection

```text
command -> SQL system of record -> outbox/CDC -> broker
                                              |-> Mongo read model
                                              |-> search index
                                              |-> cache invalidation
                                              `-> analytics
```

Projection có thể rebuild. Cần event schema/version, offset/checkpoint, idempotency, lag SLO và reconciliation. Khi projection down, command path có tiếp tục không? Query trả stale, fallback primary hay fail? Quyết định này thuộc product contract.

## CAPACITY review

Ước lượng:

```text
logical_data = writes_per_day * avg_record_size * retention_days
physical_data ≈ logical_data * indexes * replication * storage_overhead
peak_qps = average_qps * peak_factor
```

Đây chỉ là starting model. Bổ sung compression, update amplification, WAL/oplog/binlog, temporary space, backup, compaction/bloat và 30-50% headroom theo operational policy.

Connection budget:

```text
total_application_connections
  = instances * max_pool_per_instance
  + migrations/admin/monitoring/failover headroom
```

Scale-out app không được âm thầm nhân pool đến vượt DB capacity.

## ADR template

```markdown
# ADR: Database cho <capability>
Status/Date/Owners

## Context
Invariant, access patterns, volume, SLO, RPO/RTO, team constraints.

## Options
Candidate + evidence/benchmark/operational fit.

## Decision
Chosen store, topology, consistency and data ownership.

## Consequences
Benefits, new failure modes, skills/cost, migration/exit strategy.

## Validation
Load test, failure/restore drill, security review, review date.
```

## Architecture review questions

- Vì sao một database không đủ? Giá trị của mỗi store có lớn hơn operational cost?
- Ai là source of truth cho từng field?
- Invariant sống ở đâu và được test dưới concurrency thế nào?
- Khi partition/replica lag, endpoint chọn consistency hay availability?
- Có hot tenant/key/partition không?
- Backup/restore, failover, schema migration và rollback đã diễn tập?
- Nếu vendor/product không còn phù hợp, export/migration path là gì?
