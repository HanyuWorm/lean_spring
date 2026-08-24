# Chặng 6 - Capstone Order & Payment Platform

Thời lượng: tuần 16 cho bản demo; nên tiếp tục hoàn thiện sau lộ trình.

## Mục tiêu

Xây một hệ thống đủ nhỏ để chạy local nhưng đủ failure mode để chứng minh năng lực senior.

```text
Next.js client
    -> BFF/Gateway
       -> Order modular service -> PostgreSQL/outbox
       -> Payment service       -> PostgreSQL/outbox
                    \             /
                     Kafka/CDC
                         -> Order read model
                         -> Notification mock
```

Có thể bắt đầu bằng modular monolith, sau đó extract Payment để chứng minh migration path. Không cần tạo microservices từ ngày đầu.

## Milestone

### M1 - Synchronous core

- Java 21, Spring Boot, Virtual Threads;
- order aggregate dùng record/sealed type hợp lý;
- H2 cho fast test, PostgreSQL/Testcontainers cho integration test;
- idempotent create-order API;
- HTTP payment adapter có timeout/bulkhead/observation.

### M2 - Reliable events

- outbox cùng transaction với order;
- Debezium/Kafka hoặc outbox relay thay thế được;
- consumer inbox/deduplication;
- read model eventual consistency;
- trace correlation từ HTTP đến message.

### M3 - Saga

- explicit states và transition tests;
- payment/inventory failure và compensation;
- deadline, retry budget, poison message handling;
- operator endpoint hoặc UI xem workflow stuck.

### M4 - RAG support assistant

- pgvector lưu knowledge về order policy;
- tenant/metadata filtering;
- AI adapter không nằm trong core transaction;
- citation/source contract, evaluation set và cost/latency metrics;
- degrade gracefully khi AI unavailable.

## Failure injection bắt buộc

- DB pool chỉ có 5 connection với 200 concurrent requests;
- payment timeout và HTTP 429/500;
- app crash sau DB commit;
- duplicate và out-of-order message;
- Kafka unavailable;
- projection cần rebuild;
- AI/vector store unavailable;
- expired/invalid JWT và rate-limit breach.

## Artifact để phỏng vấn

- Context/container/component diagram;
- 6 ADR: module boundary, Virtual Threads, pool sizing, outbox, Saga, CQRS/RAG;
- OpenAPI và event schema;
- benchmark report có p50/p95/p99;
- threat model;
- SLO/dashboard/runbook;
- postmortem giả lập một incident duplicate payment hoặc DB saturation.

## Tiêu chí bảo vệ thiết kế

Bạn phải trả lời được:

1. Bottleneck nằm đâu khi tăng concurrency gấp 10?
2. Điều gì được đảm bảo atomic, điều gì chỉ eventual?
3. Duplicate được chặn ở technical layer và business layer thế nào?
4. Khi nào modular monolith không còn đáp ứng?
5. Recovery có cần thao tác operator không và audit ở đâu?
6. Chi phí phức tạp nào có thể bỏ nếu traffic nhỏ hơn dự kiến?

