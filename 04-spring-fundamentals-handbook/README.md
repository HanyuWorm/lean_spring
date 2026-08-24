# Spring Fundamentals Handbook

Handbook này dành cho người đã biết Java nhưng cần xây nền Spring chắc từ đầu. Baseline: Java 21, Spring Boot 4.1.1, Spring Framework 7, Jakarta APIs.

Mục tiêu: hiểu container/proxy/transaction/persistence/cache thật sự làm gì, không chỉ nhớ annotation.

## Mục lục

| Chương | Nội dung |
|---:|---|
| 1 | [Spring Framework, Spring Boot và request lifecycle](01-spring-and-spring-boot.md) |
| 2 | [Bean, IoC, DI, `@Bean`, `@Component`, `@Primary`, `@Qualifier`](02-beans-ioc-di.md) |
| 3 | [Configuration, properties, profiles và environment](03-configuration-properties.md) |
| 4 | [AOP, proxy và `@Transactional`](04-aop-proxy-transaction.md) |
| 5 | [Spring MVC, REST, validation và error handling](05-web-mvc-rest.md) |
| 6 | [JDBC, JPA, Hibernate và Spring Data JPA](06-jpa-hibernate-spring-data.md) |
| 7 | [Transaction, locking và database performance](07-data-transactions-performance.md) |
| 8 | [Spring Cache và Hibernate cache](08-cache.md) |
| 9 | [Application events, async và scheduling](09-events-async-scheduling.md) |
| 10 | [Testing trong Spring Boot](10-testing.md) |
| 11 | [Security, Actuator và observability cơ bản](11-security-observability.md) |
| 12 | [Annotation cheat sheet](12-annotation-cheat-sheet.md) |

## Lộ trình sáu tuần

| Tuần | Đọc | Thực hành |
|---:|---|---|
| 1 | Chương 1-3 | Tạo application, ba beans, config properties và hai profiles |
| 2 | Chương 4-5 | REST CRUD nhỏ, validation, Problem Details, transaction proxy test |
| 3 | Chương 6 | Entity/repository/query, tái hiện N+1 và LazyInitializationException |
| 4 | Chương 7-8 | Optimistic locking, transaction rollback, cache hit/miss/evict |
| 5 | Chương 9-10 | Event commit/rollback, async context, unit/slice/integration tests |
| 6 | Chương 11-12 | Security filter chain, Actuator, metrics và tổng ôn annotation |

## Cách học

Với mỗi annotation, luôn trả lời:

1. Ai đọc annotation: compiler, Spring container, proxy, MVC, JPA provider hay test framework?
2. Nó có hiệu lực ở startup, bean creation, proxy invocation, HTTP binding hay transaction flush?
3. Trường hợp nào annotation không có tác dụng: object tự `new`, self-invocation, private method, không có starter/processor?
4. Test nào chứng minh behavior thay vì chỉ chứng minh context khởi động?

## Project tham chiếu trong workspace

- [`../03-spring-native-patterns-deep-dive`](../03-spring-native-patterns-deep-dive/README.md): DI, proxy, strategy, factory, callback, chain, event, adapter/decorator.
- [`../01-code-projects/07-virtual-threads-system-design`](../01-code-projects/07-virtual-threads-system-design/README.md): H2, Hikari và Virtual Threads.
- [`../01-code-projects/04-reliable-events`](../01-code-projects/04-reliable-events/README.md): transaction-bound event và idempotency.

## Quy ước quan trọng

- Ví dụ ngắn nhằm giải thích semantics; không phải toàn bộ production architecture.
- Entity JPA không được trả thẳng qua REST trong ví dụ production.
- Secret không commit vào `application.yml`.
- H2 hữu ích cho học nhanh nhưng PostgreSQL/Testcontainers cần cho behavior gần production.
