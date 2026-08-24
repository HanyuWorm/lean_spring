# Chặng 3 - Database, Persistence và Data Architecture

Thời lượng: tuần 9-11. Mục tiêu là nhìn database như một tài nguyên hữu hạn và một hệ thống đồng thời, không phải implementation detail của repository.

## 1. Hibernate 6 và SQL thực tế

Phải làm được:

- đọc SQL sinh ra và execution plan;
- phát hiện N+1 bằng integration test/statistics;
- phân biệt fetch join, entity graph, batch fetch và projection;
- hiểu dirty checking, flush mode, write-behind và transaction scope;
- map JSON khi cấu trúc thực sự là document; không dùng JSON để né relational design;
- cân nhắc optimistic/pessimistic lock từ contention và business invariant.

Lab: cùng một API danh sách order, triển khai ba query strategy và so sánh query count, allocation, latency theo data volume.

## 2. Virtual Threads và connection pool

`maxPoolSize` không nên tăng theo số virtual thread. Bắt đầu từ capacity database và thời gian giữ connection:

```text
concurrent DB work ≈ arrival rate × average connection hold time
```

Sau đó kiểm chứng bằng load test. Theo dõi active/idle/pending connections, transaction duration, lock wait, DB CPU/IO và p99. Queue dài trước pool chỉ che saturation và kéo tail latency.

Lab đã có: `07-virtual-threads-system-design` dùng H2/Hikari để thấy cơ chế. Bước production-like tiếp theo là chạy cùng test với PostgreSQL/Testcontainers vì H2 không đại diện hoàn toàn cho locking, planner và kiểu dữ liệu của PostgreSQL.

## 3. Transactional Outbox và CDC

Luồng chuẩn:

```text
business transaction
    -> update aggregate
    -> insert outbox row (same DB transaction)
    -> Debezium reads log
    -> Kafka delivers at least once
    -> consumer deduplicates and updates read model
```

Test bắt buộc:

- crash sau commit nhưng trước publish;
- cùng event được giao nhiều lần;
- consumer xử lý xong nhưng crash trước commit offset;
- event đến sai thứ tự;
- schema evolution;
- poison message và replay.

Outbox giải quyết atomicity giữa DB write và event intent; nó không tự tạo exactly-once business semantics.

## 4. Idempotency và coordination

Ưu tiên invariant bền vững trong database:

- idempotency key + unique constraint;
- inbox/processed-event table;
- compare-and-set/version column;
- upsert có semantics rõ ràng.

Redis lock chỉ dùng khi cần coordination phân tán thực sự. Phải xác định owner token, TTL, renewal, fencing token và điều gì xảy ra khi process pause lâu hơn TTL. Lock không thay thế unique constraint cho invariant dữ liệu.

## 5. pgvector và RAG data path

Lab đề xuất `09-pgvector-rag`:

- PostgreSQL + pgvector bằng Testcontainers;
- ingestion idempotent, chunk versioning và metadata filter;
- so sánh exact search với approximate index trên recall/latency;
- tenant isolation;
- re-embedding/reindex migration;
- evaluation dataset và trace retrieval-to-answer.

## Câu hỏi phỏng vấn

1. Vì sao tăng Hikari pool có thể giảm throughput?
2. Outbox có còn duplicate không? Consumer xử lý thế nào?
3. Khi nào JSON column hợp lý hơn bảng quan hệ?
4. Optimistic lock thất bại nên retry ở đâu và bao nhiêu lần?
5. Redis distributed lock thiếu fencing token có failure mode gì?

