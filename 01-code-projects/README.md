# Code Projects

Nhóm này chỉ chứa các project Java/Spring Boot chạy độc lập để thực hành. Tài liệu roadmap và handbook được tách thành các folder top-level `02–13`, giúp menu website phân biệt rõ **code lab** với **learning track**.

| Project | Trọng tâm | Lệnh nhanh |
|---|---|---|
| [01 — Java Patterns](01-java-patterns/README.md) | Strategy, Factory, Chain, Decorator bằng Java thuần | `mvn -pl 01-java-patterns test` |
| [02 — Spring Core Patterns](02-spring-core-patterns/README.md) | DI, strategy registry, proxy/AOP | `mvn -pl 02-spring-core-patterns test` |
| [03 — Hexagonal Modulith](03-hexagonal-modulith/README.md) | Ports & Adapters, Modulith, module verification | `mvn -pl 03-hexagonal-modulith test` |
| [04 — Reliable Events](04-reliable-events/README.md) | Transaction-bound event và idempotency | `mvn -pl 04-reliable-events test` |
| [05 — HTTP Resilience](05-http-resilience/README.md) | HTTP Service Client, retry và concurrency limit | `mvn -pl 05-http-resilience test` |
| [06 — Observability & Concurrency](06-observability-concurrency/README.md) | Micrometer Observation và Virtual Threads | `mvn -pl 06-observability-concurrency test` |
| [07 — Virtual Threads & System Design](07-virtual-threads-system-design/README.md) | H2/Hikari, backpressure và database contention | `mvn -pl 07-virtual-threads-system-design test` |

## Build

Từ repository root:

```powershell
mvn test
mvn -pl 03-hexagonal-modulith -am test
mvn -pl 05-http-resilience spring-boot:run
```

Các artifact ID được giữ ổn định, vì vậy lệnh `mvn -pl <artifactId>` không đổi dù project đã được gom dưới `01-code-projects`.
