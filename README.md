# Spring Boot Design Patterns Learning Workspace

Workspace thực hành dành cho senior Java/Spring Boot. Folder `01-code-projects` chứa các project Maven chạy độc lập; các folder `02–13` là roadmap/handbook theo chủ đề. Root project là Maven reactor để build toàn bộ Java labs.

## Website học trên điện thoại

Toàn bộ handbook và source demo được xuất bản thành website responsive tại [hanyuworm.github.io/lean_spring](https://hanyuworm.github.io/lean_spring/). Site có tìm kiếm, dark mode, mục lục, syntax highlighting và nút copy code; mỗi push vào `main` tự deploy bằng GitHub Pages. Xem [hướng dẫn preview và publish](website/README.md).

> **Mở nội dung chi tiết:** chọn trực tiếp tên folder trong bảng bên dưới. Trên điện thoại, nhấn biểu tượng menu ở góc trên bên trái để mở toàn bộ cây chương và các trang con.

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
| 1 | [01 — Code Projects](01-code-projects/README.md) | 7 project Java/Spring Boot thực hành, được gom riêng khỏi handbook |
| 2 | [02 — Senior Java Modern Roadmap](02-senior-java-modern-roadmap/README.md) | Lộ trình Java/Spring hiện đại và Solution Architect với câu hỏi phỏng vấn có đáp án |
| 3 | [03 — Spring-native Patterns Deep Dive](03-spring-native-patterns-deep-dive/README.md) | 8 project chuyên sâu, mỗi Spring-native pattern một project độc lập |
| 4 | [04 — Spring Fundamentals Handbook](04-spring-fundamentals-handbook/README.md) | Spring core, annotations, MVC, JPA/Hibernate, cache, testing, security và observability |
| 5 | [05 — Architecture & Distributed Case Studies](05-architecture-distributed-case-studies/README.md) | 6 upstream case study: Saga/Outbox/flash sale/Kafka/Virtual Threads/Spring Cloud |
| 6 | [06 — Spring Memory Management](06-spring-memory-management/README.md) | JVM/Spring memory, Hibernate, cache, Virtual Threads, container sizing và OOM playbook |
| 7 | [07 — Node.js Backend & Next.js Roadmap](07-nodejs-backend-nextjs-roadmap/README.md) | Node.js/TypeScript backend và Next.js App Router từ nền tảng đến production |
| 8 | [08 — Database SQL/NoSQL Handbook](08-database-sql-nosql-handbook/README.md) | SQL/NoSQL, MySQL/PostgreSQL/MongoDB, transaction/index/scale và changelog 2023-2026 |
| 9 | [09 — System Design Security Handbook](09-system-design-security-handbook/README.md) | Security architecture cho cloud/no-cloud: identity, API, data, network, supply chain và incident |
| 10 | [10 — AWS/GCP Cloud Handbook](10-aws-gcp-cloud-handbook/README.md) | AWS/GCP từ foundations tới landing zone, workload, data, security, DR, FinOps và IaC |
| 11 | [11 — AI Engineering & Agentic Development](11-ai-engineering-agentic-development/README.md) | AI Engineering, RAG, agent harness, MCP/A2A, eval, security, Spring AI 2 và AI-assisted development |
| 12 | [12 — Low-Level Design Handbook](12-low-level-design-handbook/README.md) | LLD tiếng Việt: object model, SOLID/GRASP, GoF, DDD tactical, state, concurrency, test và Java lab |
| 13 | [13 — DevOps & AWS DevOps Handbook](13-devops-aws-handbook/README.md) | Linux/network, CI/CD, Docker, Kubernetes, Terraform, SRE, DevSecOps và AWS DevOps từ cơ bản đến nâng cao |

Đọc [SPRING_BOOT_DESIGN_PATTERNS.md](SPRING_BOOT_DESIGN_PATTERNS.md) trước, sau đó làm `README.md` và các bài `TODO` trong từng project. Khi migrate hệ thống hiện có, đọc thêm [SPRING_BOOT_3_1_TO_4_1_CHANGES.md](SPRING_BOOT_3_1_TO_4_1_CHANGES.md).

## Lộ trình Senior Java hiện đại

Folder [`02-senior-java-modern-roadmap`](02-senior-java-modern-roadmap/README.md) cung cấp lộ trình 20 tuần từ Java 21/25, Virtual Threads, Spring hiện đại, Hibernate/data architecture đến Outbox/CDC, Saga, CQRS, Spring AI và so sánh Java với Node.js. Bốn tuần cuối là Solution Architect track về API/system design patterns, architecture review và bảo vệ target architecture. Roadmap liên kết lại các project trong [`01-code-projects`](01-code-projects/README.md) làm lab và định nghĩa acceptance criteria cho capstone.

Để học sâu các pattern native của Spring, dùng reactor [`03-spring-native-patterns-deep-dive`](03-spring-native-patterns-deep-dive/README.md). Mỗi pattern có project, test và note riêng.

Nếu cần học lại nền tảng trước khi vào patterns, đọc [`04-spring-fundamentals-handbook`](04-spring-fundamentals-handbook/README.md): bean/IoC/DI, `@Bean`, `@Primary`, `@Qualifier`, configuration, MVC, transaction, JPA/Hibernate, cache, async, testing, security và observability.

Để luyện Architecture/Distributed Systems từ source thực tế, dùng [`05-architecture-distributed-case-studies`](05-architecture-distributed-case-studies/README.md). Folder này clone FTGO, Debezium Outbox, Piomin, Miaosha, benchmark Virtual Threads/WebFlux và Petclinic; kèm repository guide, build report và note chuyên sâu về inventory race, idempotency, Kafka ordering và backpressure.

Để học và điều tra memory ở mức senior/architect, dùng [`06-spring-memory-management`](06-spring-memory-management/README.md). Handbook phân biệt heap với native/RSS, giải thích bean scope, JPA persistence context, cache, WebFlux buffer, Virtual Threads, container sizing, quy trình phân tích OOM và 50 câu hỏi có đáp án từ nền tảng đến production incident.

Để mở rộng sang hệ sinh thái JavaScript, dùng [`07-nodejs-backend-nextjs-roadmap`](07-nodejs-backend-nextjs-roadmap/README.md). Track này đi từ JavaScript/TypeScript, Node runtime, backend API và distributed systems đến Next.js 16 App Router; có lab Node core, Fastify API và Next.js, đồng thời ánh xạ khái niệm tương đương trong Spring Boot.

Để học database engineering từ nền tảng tới solution architecture, dùng [`08-database-sql-nosql-handbook`](08-database-sql-nosql-handbook/README.md). Track bao gồm SQL nâng cao, modeling, index/execution plan, transaction/MVCC, replication/sharding/CDC, NoSQL, deep dive MySQL/PostgreSQL/MongoDB, 60 câu hỏi có đáp án và Docker lab so sánh ba engine trên cùng commerce domain.

Để thiết kế security xuyên suốt hệ thống, dùng [`09-system-design-security-handbook`](09-system-design-security-handbook/README.md). Track áp dụng NIST CSF 2.0, Zero Trust, NIST Digital Identity, OWASP ASVS 5.0 và OAuth Security BCP cho cả cloud, hybrid lẫn on-prem/no-cloud; kèm threat model, security review, incident tabletop và commerce case study.

Để học cloud theo góc nhìn solution architect, dùng [`10-aws-gcp-cloud-handbook`](10-aws-gcp-cloud-handbook/README.md). Track định nghĩa tiếng Việt các thành phần AWS/GCP, giải thích chức năng và trade-off, ánh xạ dịch vụ, đồng thời bao quát landing zone/IAM/network/compute/data/messaging/security/operations/DR/FinOps/IaC với lab không tự phát sinh chi phí.

Để học AI theo hướng software engineer/solution architect, dùng [`11-ai-engineering-agentic-development`](11-ai-engineering-agentic-development/README.md). Track phản biện note Gemini và cập nhật tới 24/08/2026: model/API, prompt và context engineering, structured output/tool calling, RAG, agent harness, MCP/A2A, memory, eval, observability, security, production, spec-driven coding agent và Spring AI 2; kèm ba lab chạy offline và bộ template áp dụng ngay vào dự án.

Để luyện thiết kế chi tiết bên trong module/service, dùng [`12-low-level-design-handbook`](12-low-level-design-handbook/README.md). Track tiếng Việt đi từ requirement/use case/invariant, object modeling/UML, SOLID/GRASP, GoF và DDD tactical đến application boundary, state machine, transaction/concurrency/idempotency, error/time, testing, sáu case study, 60 câu hỏi và Java 21 lab có concurrency test.

Để học DevOps theo hướng developer rồi tiến tới vận hành AWS production, dùng [`13-devops-aws-handbook`](13-devops-aws-handbook/README.md). Track bao phủ Linux/network/Git, CI/CD và artifact, Docker, Kubernetes, Terraform/IaC, observability/SRE, DevSecOps, incident/DR/FinOps, lộ trình 16 tuần, lab an toàn và 70 câu hỏi có đáp án chia thành DevOps nền tảng và AWS DevOps.

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
