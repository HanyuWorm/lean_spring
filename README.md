# Spring Boot Design Patterns Learning Workspace

Workspace thực hành dành cho senior Java/Spring Boot. Mỗi thư mục là một project Maven chạy độc lập; root project là Maven reactor để build tất cả cùng lúc.

## Yêu cầu

- JDK 21
- Maven 3.9+
- Spring Boot 4.1.1
- Spring Modulith 2.1.0

Trên máy hiện tại, `mvn` đang mặc định dùng JDK 17. Trước khi build, chuyển `JAVA_HOME` sang JDK 21:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
mvn test
```

## Thứ tự học

| Thứ tự | Project | Trọng tâm |
|---:|---|---|
| 1 | `01-java-patterns` | Strategy, Factory, Chain of Responsibility, Decorator bằng Java thuần |
| 2 | `02-spring-core-patterns` | DI, strategy registry, proxy/AOP và giới hạn của proxy |
| 3 | `03-hexagonal-modulith` | Ports & Adapters, package-by-feature, domain event, module verification |
| 4 | `04-reliable-events` | Transaction boundary, persistent event publication, idempotent listener |
| 5 | `05-http-resilience` | HTTP Service Client, anti-corruption layer, native retry/concurrency limit |
| 6 | `06-observability-concurrency` | Micrometer Observation, virtual threads, context propagation |
| 7 | `07-virtual-threads-system-design` | Virtual threads + H2/Hikari, backpressure và tác động lên system design |
| 9 | `09-spring-native-patterns-deep-dive` | 8 project chuyên sâu, mỗi Spring-native pattern một project độc lập |
| 11 | `11-architecture-distributed-case-studies` | 6 upstream case study: Saga/Outbox/flash sale/Kafka/Virtual Threads/Spring Cloud |
| 12 | `12-spring-memory-management` | JVM/Spring memory, Hibernate, cache, Virtual Threads, container sizing và OOM playbook |
| 13 | `13-nodejs-backend-nextjs-roadmap` | Node.js/TypeScript backend và Next.js App Router từ nền tảng đến production |
| 14 | `14-database-sql-nosql-handbook` | SQL/NoSQL, MySQL/PostgreSQL/MongoDB, transaction/index/scale và changelog 2023-2026 |
| 15 | `15-system-design-security-handbook` | Security architecture cho cloud/no-cloud: identity, API, data, network, supply chain và incident |
| 16 | `16-aws-gcp-cloud-handbook` | AWS/GCP từ foundations tới landing zone, workload, data, security, DR, FinOps và IaC |
| 17 | `17-ai-engineering-agentic-development` | AI Engineering, RAG, agent harness, MCP/A2A, eval, security, Spring AI 2 và AI-assisted development |

Đọc [SPRING_BOOT_DESIGN_PATTERNS.md](SPRING_BOOT_DESIGN_PATTERNS.md) trước, sau đó làm `README.md` và các bài `TODO` trong từng project. Khi migrate hệ thống hiện có, đọc thêm [SPRING_BOOT_3_1_TO_4_1_CHANGES.md](SPRING_BOOT_3_1_TO_4_1_CHANGES.md).

## Lộ trình Senior Java hiện đại

Folder [`08-senior-java-modern-roadmap`](08-senior-java-modern-roadmap/README.md) cung cấp lộ trình 20 tuần từ Java 21/25, Virtual Threads, Spring hiện đại, Hibernate/data architecture đến Outbox/CDC, Saga, CQRS, Spring AI và so sánh Java với Node.js. Bốn tuần cuối là Solution Architect track về API/system design patterns, architecture review và bảo vệ target architecture. Roadmap liên kết lại các project 01-07 làm lab và định nghĩa acceptance criteria cho capstone.

Để học sâu các pattern native của Spring, dùng reactor [`09-spring-native-patterns-deep-dive`](09-spring-native-patterns-deep-dive/README.md). Mỗi pattern có project, test và note riêng.

Nếu cần học lại nền tảng trước khi vào patterns, đọc [`10-spring-fundamentals-handbook`](10-spring-fundamentals-handbook/README.md): bean/IoC/DI, `@Bean`, `@Primary`, `@Qualifier`, configuration, MVC, transaction, JPA/Hibernate, cache, async, testing, security và observability.

Để luyện Architecture/Distributed Systems từ source thực tế, dùng [`11-architecture-distributed-case-studies`](11-architecture-distributed-case-studies/README.md). Folder này clone FTGO, Debezium Outbox, Piomin, Miaosha, benchmark Virtual Threads/WebFlux và Petclinic; kèm repository guide, build report và note chuyên sâu về inventory race, idempotency, Kafka ordering và backpressure.

Để học và điều tra memory ở mức senior/architect, dùng [`12-spring-memory-management`](12-spring-memory-management/README.md). Handbook phân biệt heap với native/RSS, giải thích bean scope, JPA persistence context, cache, WebFlux buffer, Virtual Threads, container sizing, quy trình phân tích OOM và 50 câu hỏi có đáp án từ nền tảng đến production incident.

Để mở rộng sang hệ sinh thái JavaScript, dùng [`13-nodejs-backend-nextjs-roadmap`](13-nodejs-backend-nextjs-roadmap/README.md). Track này đi từ JavaScript/TypeScript, Node runtime, backend API và distributed systems đến Next.js 16 App Router; có lab Node core, Fastify API và Next.js, đồng thời ánh xạ khái niệm tương đương trong Spring Boot.

Để học database engineering từ nền tảng tới solution architecture, dùng [`14-database-sql-nosql-handbook`](14-database-sql-nosql-handbook/README.md). Track bao gồm SQL nâng cao, modeling, index/execution plan, transaction/MVCC, replication/sharding/CDC, NoSQL, deep dive MySQL/PostgreSQL/MongoDB, 60 câu hỏi có đáp án và Docker lab so sánh ba engine trên cùng commerce domain.

Để thiết kế security xuyên suốt hệ thống, dùng [`15-system-design-security-handbook`](15-system-design-security-handbook/README.md). Track áp dụng NIST CSF 2.0, Zero Trust, NIST Digital Identity, OWASP ASVS 5.0 và OAuth Security BCP cho cả cloud, hybrid lẫn on-prem/no-cloud; kèm threat model, security review, incident tabletop và commerce case study.

Để học cloud theo góc nhìn solution architect, dùng [`16-aws-gcp-cloud-handbook`](16-aws-gcp-cloud-handbook/README.md). Track định nghĩa tiếng Việt các thành phần AWS/GCP, giải thích chức năng và trade-off, ánh xạ dịch vụ, đồng thời bao quát landing zone/IAM/network/compute/data/messaging/security/operations/DR/FinOps/IaC với lab không tự phát sinh chi phí.

Để học AI theo hướng software engineer/solution architect, dùng [`17-ai-engineering-agentic-development`](17-ai-engineering-agentic-development/README.md). Track phản biện note Gemini và cập nhật tới 24/08/2026: model/API, prompt và context engineering, structured output/tool calling, RAG, agent harness, MCP/A2A, memory, eval, observability, security, production, spec-driven coding agent và Spring AI 2; kèm ba lab chạy offline và bộ template áp dụng ngay vào dự án.

## Lệnh thường dùng

```powershell
# Chạy toàn bộ test
mvn test

# Chạy riêng một project và các dependency cần thiết
mvn -pl 03-hexagonal-modulith -am test

# Khởi động một Spring Boot project
mvn -pl 05-http-resilience spring-boot:run
```

Không cần khởi động project trước để chạy test. Các project có web server đều dùng cổng ngẫu nhiên trong test.
