# 06 — Databases và analytics

## Relational

| AWS | GCP | Chức năng |
|---|---|---|
| RDS | Cloud SQL | managed MySQL/PostgreSQL/SQL Server families; patch/backup/HA options |
| Aurora | AlloyDB for PostgreSQL | cloud-optimized compatible relational; exact compatibility/features khác |
| Aurora DSQL | Spanner | distributed relational category nhưng architecture/SQL/consistency/limits khác |

Multi-AZ/HA replica phục vụ failover không mặc định là read replica. Cross-region replica không tự zero RPO. Kiểm tra engine version, extension, connection limit/pool, storage autoscale, maintenance, backup/PITR, encryption và failover behavior.

## NoSQL

- **DynamoDB:** key-value/document, partition key + optional sort key, on-demand/provisioned capacity, global tables, streams. Thiết kế access pattern/partition/hot key trước.
- **Firestore:** document database, mobile/web/server patterns, collection/document indexes, real-time/offline client capabilities tùy mode. Security Rules không thay IAM/server authorization design.
- **Bigtable:** wide-column, massive low-latency key-range/time-series; row key quyết định distribution.
- **ElastiCache / Memorystore:** managed Redis/Valkey/Memcached offerings; cache/session/rate state, không mặc định source of truth.

## Analytics/search

- **Redshift / BigQuery:** analytical warehouse; BigQuery serverless consumption/capacity model, Redshift cluster/serverless options. Tách OLTP.
- **Athena / BigQuery external/federated capabilities:** SQL trên object/external data; partition/pruning và scanned bytes ảnh hưởng cost.
- **EMR/Glue / Dataproc/Dataflow:** big-data processing và ETL; Dataflow là managed Beam stream/batch, Glue có catalog/ETL ecosystem.
- **OpenSearch Service / managed search options on GCP:** full-text/log/search; product mapping không 1:1.

## Selection questions

Invariant/transaction, query/access pattern, consistency, data/QPS/growth, hot keys, latency, region/DR, operations, portability, driver/ecosystem và cost. Dùng CDC/outbox cho projection; không dual-write primary + warehouse/search.
