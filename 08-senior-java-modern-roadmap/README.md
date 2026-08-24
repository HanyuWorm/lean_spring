# Senior Java & Spring Modernization Roadmap

Lộ trình này dành cho senior Java có nền tảng chính là Spring Boot. Mục tiêu không phải học thuộc API, mà là đủ khả năng:

- giải thích trade-off ở mức JVM, framework, database và distributed system;
- thiết kế hệ thống có failure model, backpressure, observability và migration plan;
- chứng minh quyết định bằng benchmark, test và tài liệu kiến trúc;
- tự tin phỏng vấn System Design cho vị trí Senior/Lead Java và tiến tới Solution Architect.

Thời lượng gợi ý: **20 tuần, 8-10 giờ/tuần**. Tuần 1-16 xây nền tảng implementation; tuần 17-20 là Solution Architect track. Có thể học track architect song song sau khi hoàn thành tuần 7.

## 1. Baseline công nghệ

| Lane | Dùng cho | Quy ước |
|---|---|---|
| Production | Java 21 LTS, Spring Boot 4.1.x | Viết code có thể chạy ổn định và đo tải |
| Modernization | Java 25 LTS | Đánh giá nâng cấp, Scoped Values đã permanent |
| Exploration | Java 25/26 với `--enable-preview` | Structured Concurrency; không đưa thẳng vào production |

### Ba điểm cần hiệu chỉnh từ recommendation gốc

1. **Java 21 vẫn là baseline tốt, nhưng Java 25 là LTS mới hơn.** Học Java 21 để hiểu hệ thống đang chạy, đồng thời phải có migration assessment lên Java 25.
2. **Pinning do `synchronized` phụ thuộc phiên bản JDK.** Trên Java 21, virtual thread có thể bị pin khi block trong `synchronized`; JEP 491 đã thay đổi cơ chế này từ Java 24 và loại bỏ gần như toàn bộ trường hợp đó. Không nên áp dụng máy móc quy tắc “luôn đổi sang `ReentrantLock`”.
3. **Structured Concurrency chưa phải API production ổn định.** Nó vẫn là preview ở Java 25 và tiếp tục preview ở Java 26. Học mô hình lifetime, cancellation và deadline; cô lập API preview sau một adapter nếu thử nghiệm.

Nguồn chính thức: [JEP 444 - Virtual Threads](https://openjdk.org/jeps/444), [JEP 491 - Synchronize Virtual Threads without Pinning](https://openjdk.org/jeps/491), [JEP 505 - Structured Concurrency, Java 25 preview](https://openjdk.org/jeps/505), [JDK 26](https://openjdk.org/projects/jdk/26/).

## 2. Lộ trình 20 tuần

| Tuần | Chủ đề | Sản phẩm bắt buộc |
|---:|---|---|
| 0 | Self-assessment và baseline | Skill matrix, mục tiêu, số đo ban đầu |
| 1 | Records, sealed types, pattern matching | Refactor một domain model và ADR |
| 2 | Virtual Threads và Little's Law | Benchmark I/O-bound, thread dump, capacity sheet |
| 3 | Scoped Values và Structured Concurrency | Fan-out PoC, deadline/cancellation test |
| 4 | Sequenced Collections, GC/JFR/JMC | Profiling report, không chỉ benchmark throughput |
| 5 | Spring Boot 3.1 đến 4.1, Jakarta, migration | Migration checklist và dependency audit |
| 6 | REST clients, AOT/GraalVM, observability | Client comparison, native feasibility report |
| 7 | Spring Modulith và modular monolith | Module boundaries, verification test, event flow |
| 8 | Spring AI, RAG và guardrails | AI port/adapter design, prompt/evaluation contract |
| 9 | Hibernate 6, SQL plan, JSON mapping | Query plan, N+1 test, mapping decision record |
| 10 | Pool sizing, transactions, idempotency | Load test: virtual threads versus DB pool |
| 11 | Outbox, CDC, Kafka, Redis coordination | Failure matrix và duplicate-delivery tests |
| 12 | API Gateway/BFF, OAuth2/JWT, rate limit | Threat model và policy placement |
| 13 | Saga, Resilience4j, failure containment | Saga state machine, retry/bulkhead budget |
| 14 | CQRS/Event Sourcing và read model | Decision matrix và rebuild/projection plan |
| 15 | Java versus Node.js/full-stack delivery | Cùng một workload, hai implementation notes |
| 16 | Capstone và mock interview | Demo, ADR pack, runbook, postmortem giả lập |
| 17 | API design patterns | Style/contract/idempotency/error/versioning review |
| 18 | System design patterns | Pattern decision matrix và architecture diagrams |
| 19 | Architecture review | NFR, capacity, failure, threat, DR và cost review |
| 20 | Solution Architecture Board | 45-minute defense và phased migration roadmap |

## 3. Cách học mỗi tuần

Mỗi tuần chỉ được coi là hoàn thành khi có đủ năm bằng chứng:

1. **Concept note:** giải thích bằng ngôn ngữ của mình, tối đa hai trang.
2. **Runnable lab:** test phải tái hiện được cả happy path và failure path.
3. **Measurement:** latency p50/p95/p99, throughput, saturation và resource usage phù hợp với chủ đề.
4. **Decision record:** chọn giải pháp nào, từ chối giải pháp nào, điều kiện để đảo quyết định.
5. **Interview rehearsal:** trả lời thành tiếng trong 10-15 phút và tự phản biện.

Không dùng “throughput cao hơn” làm kết luận nếu chưa chỉ ra bottleneck đã chuyển sang đâu.

## 4. Bản đồ học liệu trong folder

| Chặng | Note |
|---|---|
| Đánh giá đầu vào | [00-assessment/README.md](00-assessment/README.md) |
| Java Core Evolution | [01-java-core/README.md](01-java-core/README.md) |
| Modern Spring | [02-spring-modern/README.md](02-spring-modern/README.md) |
| Data Architecture | [03-data-architecture/README.md](03-data-architecture/README.md) |
| Distributed Architecture | [04-distributed-systems/README.md](04-distributed-systems/README.md) |
| Java versus Node/full-stack | [05-java-node-fullstack/README.md](05-java-node-fullstack/README.md) |
| Capstone | [06-capstone/README.md](06-capstone/README.md) |
| Solution Architect Track | [07-solution-architect/README.md](07-solution-architect/README.md) |
| Theo dõi tiến độ | [PROGRESS.md](PROGRESS.md) |

## 5. Tận dụng các project đã có

| Năng lực | Project thực hành |
|---|---|
| Pattern bằng Java thuần | [`../01-java-patterns`](../01-java-patterns) |
| Spring DI, registry, proxy/AOP | [`../02-spring-core-patterns`](../02-spring-core-patterns) |
| Hexagonal + Spring Modulith | [`../03-hexagonal-modulith`](../03-hexagonal-modulith) |
| Reliable event + idempotent listener | [`../04-reliable-events`](../04-reliable-events) |
| HTTP client + resilience | [`../05-http-resilience`](../05-http-resilience) |
| Observation + concurrency | [`../06-observability-concurrency`](../06-observability-concurrency) |
| Virtual Threads + H2/Hikari/backpressure | [`../07-virtual-threads-system-design`](../07-virtual-threads-system-design) |

Các lab lớn chưa có code được định nghĩa trong roadmap để tạo ở vòng tiếp theo: `08-outbox-cdc-kafka`, `09-pgvector-rag`, `10-saga-cqrs` và capstone full-stack. Không đưa chúng vào Maven reactor cho tới khi có implementation chạy được.

## 6. Definition of Done toàn lộ trình

- Có ít nhất 10 ADR và 4 diagram kiến trúc do chính bạn bảo vệ được.
- Có benchmark Virtual Threads chứng minh database pool vẫn là admission control.
- Có test duplicate, retry, timeout, partial failure và recovery.
- Có một migration plan từ Java 21 lên 25 và Spring Boot 3.x lên 4.x.
- Có capstone demo được từ API đến database/event/read model.
- Có OpenAPI/AsyncAPI, quality attribute scenarios, capacity/failure/threat/cost review và transition architecture.
- Bảo vệ được target architecture trước một architecture board giả lập trong 45 phút.
- Trả lời được các câu hỏi trong từng note mà không chỉ đọc định nghĩa.
