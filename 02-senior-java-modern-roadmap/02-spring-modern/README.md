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

Thực hành ở `01-code-projects/05-http-resilience`: thêm deadline budget, error taxonomy và anti-corruption layer. Retry chỉ dành cho operation idempotent và phải nằm trong tổng latency budget.

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

Lab ở `01-code-projects/06-observability-concurrency`:

- trace một request qua HTTP -> service -> repository;
- giữ correlation khi dùng virtual thread;
- chứng minh metric không tạo cardinality vô hạn từ user/order ID;
- dashboard có latency, traffic, errors, saturation;
- alert gắn với SLO, không chỉ CPU threshold.

## 5. Spring Modulith

Dùng `01-code-projects/03-hexagonal-modulith` và `01-code-projects/04-reliable-events` để phân biệt:

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

### 1. Khi nào `WebClient` vẫn hợp lý hơn Virtual Threads + `RestClient`?

**Trả lời:** Khi pipeline end-to-end đã reactive; cần streaming/SSE, backpressure, fan-out lớn với operator composition; hoặc phải tích hợp API/library chỉ cung cấp non-blocking publisher. `WebClient` cũng phù hợp khi team có năng lực debug reactive context và lợi ích đo được.

Với request/response blocking thông thường, JDBC và codebase imperative, Virtual Threads + `RestClient` thường đơn giản hơn. Không trộn `.block()` tùy tiện trong event loop; chọn theo toàn data path, failure/cancellation và benchmark cùng downstream limit.

### 2. AOT/native image thay đổi cách dùng reflection/proxy ra sao?

**Trả lời:** Native image dùng closed-world analysis nên reflection, resource, serialization, JNI và dynamic proxy không được tự động khám phá như JVM runtime. Framework phải sinh code hoặc cung cấp runtime hints; class/proxy được tạo hoàn toàn động, scan classpath và plugin load runtime bị hạn chế.

Giữ reflection ở adapter, dùng Spring AOT-compatible APIs, chạy native integration test và kiểm startup/RSS/build-time thay vì giả định native luôn tốt hơn. JDK dynamic proxy cho interface và class proxy có constraint khác; self-invocation vẫn không qua proxy.

### 3. Bạn đặt retry ở client, gateway hay message consumer dựa trên tiêu chí nào?

**Trả lời:** Đặt ở layer gần operation nhất nhưng biết được transient classification, idempotency và deadline; chỉ một layer sở hữu retry budget. Client library hợp với network transient cho một dependency. Gateway chỉ retry request an toàn/idempotent và không che business semantics. Consumer retry theo delivery contract, có backoff/DLQ và idempotent handler.

Không retry ở cả ba layer. Tính tổng attempts toàn chain, tránh retry 429 không theo `Retry-After`, và với write cần idempotency key/outcome query vì timeout không chứng minh operation chưa chạy.

### 4. Domain event khác integration event về contract và transaction thế nào?

**Trả lời:** Domain event diễn đạt fact bên trong bounded context, có thể dùng type/domain detail nội bộ và thường được tạo cùng aggregate transition. Integration event là public contract cho context khác, cần schema/version, backward compatibility, privacy và lifecycle dài hơn.

Có thể map domain event sang integration event sau commit. Nếu cần reliable publish, ghi outbox trong cùng transaction rồi relay. Không gửi object JPA/domain trực tiếp ra broker và không remote publish từ entity trong transaction.

### 5. Làm sao đo chất lượng RAG ngoài việc demo một prompt đẹp?

**Trả lời:** Xây golden dataset có answerable/no-answer/adversarial/tenant slices. Đo retrieval riêng bằng Recall@k, MRR/nDCG và ACL leakage; đo answer bằng correctness, groundedness, citation precision/recall, abstention và task success. Grader code/rule ưu tiên, model judge phải calibrate với human labels.

Release so candidate với baseline và hard-gate security/critical facts; theo dõi p95 latency, tokens/cost, empty retrieval, user correction và drift online. Trace query → candidates → selected chunks → answer/citations để biết lỗi ở ingestion, retrieval hay generation.
