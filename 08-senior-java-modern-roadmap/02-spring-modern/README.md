# Chặng 2 - Modern Spring

Thời lượng: tuần 5-8. Đọc song song [`../../SPRING_BOOT_3_1_TO_4_1_CHANGES.md`](../../SPRING_BOOT_3_1_TO_4_1_CHANGES.md).

## 1. Migration và platform hygiene

Checklist cần làm được trên một service thật:

- chuyển `javax.*` sang `jakarta.*` và kiểm tra cả dependency transitively;
- dùng migration guide/release notes theo từng bước, không nhảy version thiếu kiểm soát;
- audit custom starter, actuator endpoint, serialization, validation và security config;
- chạy integration test với database thật tương thích production;
- tạo rollback condition và compatibility matrix.

Kết quả: một migration ADR có scope, breaking changes, test evidence, rollout/canary và rollback.

## 2. HTTP client hiện đại

So sánh theo use case, không theo độ mới:

| Lựa chọn | Phù hợp | Cần kiểm soát |
|---|---|---|
| `RestClient` | imperative synchronous flow | timeout, connection pool, observation |
| HTTP Interface | declarative typed client | error mapping, hidden network boundary |
| `WebClient` | streaming/reactive chain | context, backpressure, operator complexity |

Thực hành ở `05-http-resilience`: thêm deadline budget, error taxonomy và anti-corruption layer. Retry chỉ dành cho operation idempotent và phải nằm trong tổng latency budget.

## 3. AOT và GraalVM Native Image

Không mặc định “native nhanh hơn”. Đo riêng:

- startup time và memory footprint;
- steady-state throughput/p99;
- build time, artifact size, CI complexity;
- reflection/resource/proxy hints;
- khả năng debug và observability.

Đưa ra quyết định theo workload: serverless/scale-to-zero/CLI có thể hưởng lợi khác long-running service.

## 4. Observability-first

Micrometer Observation là boundary chung cho metrics/traces/log correlation. Với OpenTelemetry, thiết kế semantic convention và cardinality trước khi thêm tag.

Lab ở `06-observability-concurrency`:

- trace một request qua HTTP -> service -> repository;
- giữ correlation khi dùng virtual thread;
- chứng minh metric không tạo cardinality vô hạn từ user/order ID;
- dashboard có latency, traffic, errors, saturation;
- alert gắn với SLO, không chỉ CPU threshold.

## 5. Spring Modulith

Dùng `03-hexagonal-modulith` và `04-reliable-events` để phân biệt:

- module boundary với package convention;
- domain event nội bộ với integration event;
- transaction-bound listener với reliable publication;
- modular monolith với “microservices nằm chung process”.

Chỉ tách service khi có boundary về ownership, scaling, availability hoặc release cadence đủ mạnh để trả chi phí distributed system.

## 6. Spring AI

Học ở mức architecture trước API:

- `ChatModel`/embedding/vector store nằm sau application port;
- prompt template là versioned artifact;
- RAG pipeline có chunking, metadata filter, retrieval và reranking;
- bảo vệ PII, prompt injection, tool authorization và cost budget;
- evaluation dataset, quality metric, latency/cost/error telemetry;
- fallback khi model/vector store không khả dụng.

Không đặt model call trực tiếp trong JPA transaction. AI output là untrusted input và cần schema validation.

## Câu hỏi phỏng vấn

1. Khi nào `WebClient` vẫn hợp lý hơn Virtual Threads + `RestClient`?
2. AOT/native image thay đổi cách dùng reflection/proxy ra sao?
3. Bạn đặt retry ở client, gateway hay message consumer dựa trên tiêu chí nào?
4. Domain event khác integration event về contract và transaction thế nào?
5. Làm sao đo chất lượng RAG ngoài việc demo một prompt đẹp?

