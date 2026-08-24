# Spring Boot: thay đổi quan trọng từ 3.1 đến 4.1.x

> Cập nhật ngày 24/08/2026. Phạm vi của tài liệu là **Spring Boot 3.1 → 4.1.x**, không phải Spring Framework 3.1 cũ. Workspace hiện resolve Spring Boot 4.1.1, Spring Framework 7.0.9 và Java 21.

## 1. Tóm tắt cho senior engineer

Các thay đổi có ảnh hưởng lớn nhất đến cách thiết kế và migrate hệ thống:

1. Spring Boot 3.2 đưa virtual threads vào auto-configuration và đưa `RestClient`/`JdbcClient` thành lựa chọn chính thức.
2. Spring Boot 3.3 cải thiện production packaging với CDS, SBOM, observability và service connections.
3. Spring Boot 3.4 bật graceful shutdown mặc định, thêm structured logging và cải thiện bean selection qua `@Fallback`.
4. Spring Boot 3.5 là bridge release nên đi qua trước khi lên 4.x; nhiều API deprecated từ 3.3 bị xóa.
5. Spring Boot 4.0/Spring Framework 7 là major migration: modular auto-configuration, Jackson 3 mặc định, JSpecify null-safety, native resilience, API versioning và HTTP client mới.
6. Spring Boot 4.1 bổ sung first-class gRPC support và nâng toàn bộ Spring portfolio lên thế hệ 2026.

Không nên nâng trực tiếp một codebase lớn từ 3.1 lên 4.1. Lộ trình an toàn:

```text
3.1 -> 3.2 -> 3.3 -> 3.4 -> 3.5 -> 4.0 -> 4.1
```

Ở mỗi bước: bật compiler warnings, loại bỏ deprecation, chạy integration/contract/performance tests và so sánh configuration properties.

## 2. Ma trận phiên bản

| Spring Boot | Spring generation | Điểm cần nhớ |
|---|---|---|
| 3.1 | Framework 6.0 | Java 17 baseline, Jakarta EE 10; mốc bắt đầu |
| 3.2 | Framework 6.1 | Java 21 virtual threads, `RestClient`, `JdbcClient` |
| 3.3 | Framework 6.1 | CDS, SBOM, Prometheus 1.x, observability/service connections |
| 3.4 | Framework 6.2 | Structured logging, graceful shutdown mặc định, `@Fallback` |
| 3.5 | Framework 6.2 | Bridge release trước Boot 4, tightening và removal deprecations |
| 4.0 | Framework 7.0 | Modular Boot, Jackson 3, JSpecify, resilience, API versioning |
| 4.1.x | Framework 7.0.x | gRPC và dependency generation 2026 |

Patch releases chủ yếu gồm bug/security/dependency fixes. Feature migration nên dựa trên release notes của minor line như 4.1.0, sau đó dùng patch mới nhất của line đó.

## 3. Spring Boot 3.2 — bước ngoặt Java 21

### Virtual threads

Điều kiện:

```properties
spring.threads.virtual.enabled=true
spring.main.keep-alive=true
```

Khi bật:

- Tomcat và Jetty xử lý servlet requests bằng virtual threads.
- Auto-configured `applicationTaskExecutor` dùng `SimpleAsyncTaskExecutor` với virtual threads.
- `@Async`, Spring MVC async processing và WebFlux blocking execution có thể dùng executor này.
- Scheduler, Kafka, RabbitMQ, Pulsar và một số integration có hỗ trợ virtual threads.
- Các thuộc tính chỉnh thread pool truyền thống có thể không còn tác dụng vì executor không phải fixed pool.

Thiết kế phải thay đổi từ “thread pool là bulkhead” sang explicit concurrency control theo capacity của downstream.

### `RestClient`

`RestClient` được đưa vào Spring Framework 6.1 và được Boot auto-configure. Đây là synchronous fluent client thay thế dần `RestTemplate`.

```java
@Service
class CatalogClient {
    private final RestClient client;

    CatalogClient(RestClient.Builder builder) {
        this.client = builder.baseUrl("https://catalog.example").build();
    }
}
```

Luôn inject auto-configured builder để giữ HTTP settings, message converters, tracing và observation customizers.

### `JdbcClient`

`JdbcClient` cung cấp fluent facade cho JDBC, phù hợp query nhỏ hoặc use case không cần ORM. Đừng xem nó là lý do để bỏ repository/domain boundary; đây chỉ là adapter API tốt hơn.

### Các điểm migration khác

- Compiler cần giữ parameter names bằng `-parameters`; Framework 6.1 không còn suy đoán bằng cách parse bytecode.
- `spring.application.name` xuất hiện trong log format mặc định.
- H2 được nâng lên 2.2; database file cũ có thể cần export/import migration.
- Logging correlation ID và OpenTelemetry integration được cải thiện.
- Testcontainers có thể startup song song.

Nguồn: [Spring Boot 3.2 release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.2-Release-Notes).

## 4. Spring Boot 3.3 — production packaging và operations

### CDS support

Boot cung cấp layout thuận lợi cho JVM Class Data Sharing:

```bash
java -Djarmode=tools -jar application.jar extract
```

CDS có thể giảm startup time và memory footprint. Nó bổ sung, không thay thế AOT/native image; hãy benchmark đúng deployment model.

### Observability và actuator

- Cải thiện observation cho nhiều integration.
- Prometheus Java Client 1.x được hỗ trợ.
- Có SBOM Actuator endpoint.
- Brave/Zipkin và OTLP integrations được cập nhật.

### Service connections

Docker Compose/Testcontainers service connections được mở rộng. Integration test có thể lấy connection details từ container thay vì tự truyền property thủ công.

### Virtual-thread refinements

Nhiều integration bắt đầu dùng virtual threads khi feature được bật. Điều này tăng khả năng tạo concurrency, nên database/broker/API quotas càng cần bulkhead rõ ràng.

### Migration watchlist

- Flyway 10 modular hóa database support; có thể cần thêm database-specific artifact.
- Prometheus dependency và client generation thay đổi.
- Native Build Tools cần version mới.

Nguồn: [Spring Boot 3.3 release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.3-Release-Notes).

## 5. Spring Boot 3.4 — operability mặc định tốt hơn

### Graceful shutdown bật mặc định

Web server ngừng nhận request mới và cho request đang chạy thời gian hoàn tất. Cần đồng bộ:

- Kubernetes termination grace period.
- Load balancer deregistration.
- Application shutdown timeout.
- Consumer listener shutdown và transaction duration.

Đừng giả định graceful shutdown xử lý được job không có deadline.

### Structured logging

Boot hỗ trợ sẵn ECS, GELF và Logstash format:

```properties
logging.structured.format.console=ecs
```

Thiết kế log contract cần xác định field names, PII policy, correlation/trace IDs và cardinality.

### `@Fallback`

`@Fallback` đánh dấu implementation có độ ưu tiên thấp hơn default candidate. Nó phù hợp default implementation trong starter/library; `@Primary` vẫn phù hợp override có chủ đích.

### HTTP request factory builders

API builder thống nhất việc cấu hình JDK, Apache, Jetty, Reactor Netty hoặc simple client. Điều này giảm code vendor-specific nhưng timeout và connection-pool policy vẫn phải được khai báo.

### Testing migration

`@MockBean` và `@SpyBean` bắt đầu deprecated để chuyển sang Spring Framework `@MockitoBean` và `@MockitoSpyBean`. Nên migrate test trước khi lên Boot 4.

### Các thay đổi khác

- Actuator endpoint access control rõ hơn.
- OCI image builder mặc định nhỏ hơn và có thể không chứa shell.
- OtlpMeterRegistry và Undertow có thêm virtual-thread support ở line này.
- Gradle minimum version tăng.

Nguồn: [Spring Boot 3.4 release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes).

## 6. Spring Boot 3.5 — bridge release trước Boot 4

Đây là version nên đạt tới trước khi migrate 4.0.

### Breaking/tightening đáng chú ý

- API/property deprecated từ Boot 3.3 và đánh dấu remove-at-3.5 đã bị xóa.
- Giá trị boolean `.enabled` được kiểm tra chặt thành `true` hoặc `false`.
- Profile names được validate chặt hơn.
- Bean conditions có xét generic return type chính xác hơn.
- Internal `spring-boot-parent` không còn được publish; application bình thường nên dùng `spring-boot-starter-parent` hoặc import BOM.

### Security và operations

- `heapdump` actuator endpoint mặc định không có quyền truy cập; muốn dùng phải vừa expose vừa cấp access.
- Có SSL bundle certificate-chain/expiry metrics.
- Quartz actuator có thể trigger job bằng POST.

### Checklist trước Boot 4

- Build không còn deprecation warning thuộc Spring Boot/Spring Framework.
- Đã chuyển `@MockBean`/`@SpyBean`.
- JSON contract tests bao phủ dates, property order, null và polymorphism.
- Không import sâu từ package auto-configuration nội bộ.
- Đã kiểm kê starter và transitive dependencies.

Nguồn: [Spring Boot 3.5 release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.5-Release-Notes).

## 7. Spring Boot 4.0 / Spring Framework 7 — major migration

### Modular auto-configuration

Boot 4 tách monolithic auto-configuration thành các module theo technology. Hệ quả:

- Có thêm focused starters, ví dụ `spring-boot-starter-restclient`.
- Package của auto-configuration/support classes có thể thay đổi.
- Một starter cũ có thể không kéo toàn bộ behavior từng được lấy transitively.
- Có `spring-boot-starter-classic` làm cầu nối migration, nhưng code mới nên dùng focused starters.

Đây là lý do project `01-code-projects/05-http-resilience` khai báo riêng `spring-boot-starter-restclient`.

### Jackson 3 mặc định

Jackson 3 đổi package từ `com.fasterxml.jackson...` sang `tools.jackson...` và có default behavior mới.

Các vùng cần contract test:

- Property ordering.
- ISO-8601 date/time serialization.
- Custom modules/deserializers.
- Polymorphic type handling.
- Dữ liệu JSON đã persist trong database/cache/event log.

Boot hỗ trợ Jackson 2 tạm thời để migration từng bước, nhưng target nên là Jackson 3.

### JSpecify null-safety

Spring Framework 7 dùng JSpecify `@NullMarked`/`@Nullable`. Các annotation nullability cũ trong `org.springframework.lang` bị deprecated.

Đây không chỉ là đổi import: JSpecify hỗ trợ type-use nullability, ví dụ phân biệt collection nullable với phần tử nullable.

### Native resilience

Framework 7 cung cấp:

```java
@EnableResilientMethods

@Retryable(
    includes = TransientException.class,
    maxRetries = 3,
    delay = 100,
    multiplier = 2,
    jitter = 20)
@ConcurrencyLimit(10)
public Result call() { ... }
```

Lưu ý:

- Đây là proxy-based behavior; self-invocation/final class có thể làm thiết kế sai hoặc proxy creation lỗi.
- `@ConcurrencyLimit` rất quan trọng với virtual threads vì không còn fixed thread pool làm giới hạn ngầm.
- Resilience4j vẫn cần thiết nếu cần circuit breaker/rate limiter/bulkhead/reactive feature phức tạp.

### HTTP stack

- `RestTemplate` deprecated trong Spring Framework 7; migrate sang `RestClient`.
- HTTP Service Client (`@HttpExchange`) được cải thiện.
- Có API versioning support ở MVC/WebFlux.
- `RestTestClient` giúp test server REST API.

### Observability

- Focused OpenTelemetry starter và OTLP integration.
- Tracing modules được tách theo Brave/OpenTelemetry.
- Ưu tiên Micrometer Observation API trong application code.

### Platform changes

- Servlet 6.1 baseline.
- Undertow bị loại khỏi Boot 4.0 do chưa tương thích baseline tại thời điểm release.
- Gradle requirements tăng.
- Test/tooling generation mới có thể làm test assumptions cũ hỏng.

Nguồn:

- [Spring Boot 4.0 release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)
- [Spring Boot 4 modularization](https://spring.io/blog/2025/10/28/modularizing-spring-boot)
- [Jackson 3 support](https://spring.io/blog/2025/10/07/introducing-jackson-3-support-in-spring/)
- [Spring Framework resilience](https://docs.spring.io/spring-framework/reference/core/resilience.html)

## 8. Spring Boot 4.1.x — current line của workspace

### Spring gRPC

Boot 4.1 đưa gRPC server/client/testing support vào portfolio chính, gồm standalone Netty server hoặc servlet integration qua HTTP/2.

Trước khi dùng cần quyết định:

- gRPC là internal synchronous RPC hay public API.
- Deadline propagation.
- Retry policy và idempotency.
- Protobuf schema compatibility.
- Load balancing và observability.

### Ecosystem generation 2026

Boot 4.1.0 nâng Spring Data BOM 2026.0, Spring Security 7.1, Spring Integration 7.1, Spring Kafka 4.1, Micrometer 1.17 và nhiều dependency khác. Vì vậy migration test phải bao phủ data access, security rules, messaging serialization và metrics names.

### Khác

- Spock support trở lại với Spock 2.4/Groovy 5.
- JPA repository bootstrap modes và build plugin behavior có các cập nhật.
- Patch 4.1.1 trong workspace bao gồm maintenance fixes; không nên xem patch release là một architecture migration riêng.

Nguồn: [Spring Boot 4.1 release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.1-Release-Notes).

## 9. Virtual threads thay đổi system design như thế nào?

### Điều không còn đúng

Với platform-thread pool:

```text
200 request threads ~= tối đa 200 blocking operations đồng thời
```

Thread pool vô tình đóng vai trò admission control. Với virtual threads, application có thể tạo hàng chục nghìn blocking executions. Nếu không có explicit limit, concurrency dồn xuống database, HTTP pool, broker hoặc filesystem.

### Mô hình mới

```text
request deadline
    -> rate limit
        -> use-case bulkhead / @ConcurrencyLimit
            -> short transaction
                -> finite DB connection pool
```

Mỗi downstream phải có concurrency budget dựa trên capacity và latency target, không dựa vào số virtual threads có thể tạo.

### Database design

- Hikari pool vẫn hữu hạn.
- Transaction giữ connection càng lâu thì pool saturation càng nhanh.
- Không gọi remote API khi đang giữ DB transaction nếu không có lý do consistency rõ ràng.
- Dùng optimistic locking/constraints cho correctness; virtual threads không giải quyết contention.
- Theo dõi connection acquire time, active/pending connections và transaction duration.

### HTTP clients

- Virtual threads làm synchronous `RestClient` trở thành lựa chọn dễ scale hơn cho nhiều blocking workloads.
- HTTP connection pool, remote quota và latency vẫn hữu hạn.
- Luôn có connect/read/overall deadline.
- Retry phải có budget, backoff, jitter và idempotency.

### Reactive hay virtual threads?

| Chọn | Phù hợp khi |
|---|---|
| Spring MVC + virtual threads | Blocking libraries/JDBC, imperative codebase, team muốn stack trace và flow tuyến tính |
| WebFlux/Reactor | Streaming, backpressure end-to-end, reactive driver, fan-out async lớn đã được kiểm soát |

Không rewrite một hệ reactive ổn định chỉ vì có virtual threads. Không chọn WebFlux chỉ vì nghĩ nó luôn nhanh hơn.

### ThreadLocal và context

Virtual thread hỗ trợ `ThreadLocal`, nhưng hàng chục nghìn threads nhân với context lớn có thể tiêu tốn bộ nhớ. Review:

- MDC/security/tracing context size.
- Cleanup lifecycle.
- Cached object per thread.
- Context propagation qua explicit async boundaries.

### Pinning và locking

Blocking trong một số `synchronized`/native regions có thể pin carrier thread. Dùng JFR và `jcmd` để tìm pinning; không refactor lock dựa trên phỏng đoán.

### Observability cần thêm

- Virtual thread count/start/failure/pinned events.
- Hikari active/idle/pending/acquire time.
- Bulkhead queue/rejections/wait time.
- Request p50/p95/p99 và deadline exceeded.
- CPU saturation, GC và carrier-thread utilization.

## 10. Migration checklist 3.1 → 4.1

### Code

- [ ] Constructor parameters được compile với `-parameters`.
- [ ] Không còn `RestTemplate` cho code mới; đã có plan `RestClient`.
- [ ] `@MockBean`/`@SpyBean` được migrate.
- [ ] Null annotations chuyển sang JSpecify.
- [ ] Final/proxy/self-invocation cases của transaction/retry/concurrency được test.
- [ ] Jackson customizations đã chuyển hoặc có compatibility strategy.

### Dependencies

- [ ] Nâng từng minor Boot tuần tự.
- [ ] Dùng focused starters của Boot 4.
- [ ] Kiểm tra Flyway database module.
- [ ] Kiểm tra removed/deprecated artifacts và package imports.
- [ ] Chạy dependency convergence/security scan.

### Data/contracts

- [ ] Snapshot JSON contract trước/sau Jackson 3.
- [ ] Test database migration trên bản copy production data.
- [ ] Test event serialization/version compatibility.
- [ ] Test security filter chain và authorization decisions.

### Runtime

- [ ] Graceful shutdown khớp orchestrator timeout.
- [ ] Virtual threads được load test với workload production-like.
- [ ] Có explicit concurrency limit theo downstream.
- [ ] Transaction không giữ connection trong network wait ngoài ý muốn.
- [ ] Metrics/traces/log correlation vẫn hoạt động.

### Rollout

- [ ] Canary hoặc blue/green.
- [ ] Dashboard so sánh latency/error/pool saturation.
- [ ] Rollback bao gồm schema và serialized-data compatibility.
- [ ] Không trộn framework upgrade với business refactor lớn trong cùng release nếu tránh được.

## 11. Project demo liên quan

- `01-code-projects/05-http-resilience`: HTTP Service Client và native resilience.
- `01-code-projects/06-observability-concurrency`: Observation và virtual executor cơ bản.
- `01-code-projects/07-virtual-threads-system-design`: H2/Hikari, transaction contention và explicit concurrency limit.
