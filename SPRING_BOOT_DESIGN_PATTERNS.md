# Design Patterns hiện đại trong Spring Boot

> Mục tiêu: học pattern theo vấn đề production, không học thuộc tên của 23 GoF patterns. Baseline của workspace là Java 21, Spring Boot 4.1.1, Spring Framework 7 và Spring Modulith 2.1.

> Trạng thái kiểm chứng ngày 2026-08-24: Spring Boot 4.1.1 đang nằm trong dòng stable của tài liệu chính thức; Spring Framework 7 có native `@Retryable`, `@ConcurrencyLimit`, `RetryTemplate`; `RestTemplate` đã deprecated; null-safety chuyển sang JSpecify. Với Virtual Threads, Spring Boot yêu cầu Java 21+ và khuyến nghị Java 24+ để có trải nghiệm tốt hơn.

> Nếu chưa chắc về bean, `@Bean`, `@Primary`, `@Qualifier`, MVC, transaction, JPA/Hibernate hoặc cache, đọc [Spring Fundamentals Handbook](10-spring-fundamentals-handbook/README.md) trước.

## 1. Bản đồ học tập

```text
Java composition
    -> Spring IoC và proxy
        -> Ports & Adapters
            -> Modular Monolith + DDD
                -> Reliable events
                    -> HTTP resilience
                        -> Observability + concurrency
```

Mỗi chặng phải trả lời được bốn câu hỏi:

1. Pattern giải quyết force/conflict nào?
2. Boundary và dependency chạy theo hướng nào?
3. Failure mode mới do pattern tạo ra là gì?
4. Test nào chứng minh implementation đúng?

Khung trả lời dùng cho mọi pattern:

1. **Force/conflict:** nêu hai yêu cầu đang kéo hệ thống theo hai hướng, ví dụ cần thay đổi implementation mà không làm domain phụ thuộc framework. Nếu không chỉ ra được conflict, rất có thể pattern đang bị áp dụng vì thói quen.
2. **Boundary/dependency:** xác định component nào sở hữu rule, component nào chỉ là adapter và chiều dependency compile-time. Với Ports & Adapters, domain định nghĩa port; adapter phụ thuộc port, không đảo ngược.
3. **Failure mode:** liệt kê chi phí mới như thêm indirection, cấu hình sai bean, retry khuếch đại tải, event trùng hoặc eventual consistency. Pattern không xóa độ phức tạp mà chuyển nó đến nơi kiểm soát được hơn.
4. **Bằng chứng bằng test:** ưu tiên test đúng loại rủi ro: unit test cho policy, contract test cho boundary, integration test cho transaction/configuration, concurrency test cho race và architecture test cho chiều dependency.

## 2. Spring-native patterns phải nắm sâu

### Dependency Injection / Inversion of Control

Deep-dive project: [`09-spring-native-patterns-deep-dive/01-dependency-injection`](09-spring-native-patterns-deep-dive/01-dependency-injection/README.md).

Spring container tạo object graph; application code chỉ khai báo dependency. Ưu tiên constructor injection và dependency vào abstraction có ý nghĩa nghiệp vụ.

Nên hiểu:

- Bean lifecycle, scope và lazy initialization.
- `@Primary`, qualifier và collection injection.
- Circular dependency thường là tín hiệu boundary sai.
- Domain object không cần trở thành Spring bean.

### Proxy

Deep-dive project: [`09-spring-native-patterns-deep-dive/02-proxy-aop`](09-spring-native-patterns-deep-dive/02-proxy-aop/README.md).

`@Transactional`, `@Async`, caching, method security, retry và nhiều AOP feature chạy qua proxy.

Hệ quả quan trọng:

- Gọi method từ bên ngoài bean đi qua proxy.
- Self-invocation thường không đi qua proxy.
- Private/final method không phải join point phù hợp trong proxy-based AOP thông thường.
- Transaction boundary nên đặt ở public application use case.
- Đừng dùng AOP để che business flow quan trọng.

### Strategy

Deep-dive project: [`09-spring-native-patterns-deep-dive/03-strategy`](09-spring-native-patterns-deep-dive/03-strategy/README.md).

Dùng khi thuật toán hoặc policy thay đổi theo loại payment, tenant, country, fulfillment mode hoặc feature flag.

Spring có thể inject `List<Strategy>` hoặc `Map<String, Strategy>`. Registry nên fail fast khi thiếu strategy, thay vì âm thầm fallback.

### Factory

Deep-dive project: [`09-spring-native-patterns-deep-dive/04-factory`](09-spring-native-patterns-deep-dive/04-factory/README.md).

Factory gom logic khởi tạo và lựa chọn implementation. Spring dùng pattern này ở `BeanFactory`, auto-configuration và builder APIs. Ở domain, chỉ tạo factory khi invariant khởi tạo đủ phức tạp; constructor đơn giản vẫn tốt hơn.

### Template Method / Callback

Deep-dive project: [`09-spring-native-patterns-deep-dive/05-template-callback`](09-spring-native-patterns-deep-dive/05-template-callback/README.md).

`JdbcTemplate` và `TransactionTemplate` giữ resource lifecycle, còn callback cung cấp phần logic thay đổi. Pattern này hữu ích khi framework phải đảm bảo cleanup, exception translation và transaction semantics.

### Chain of Responsibility

Deep-dive project: [`09-spring-native-patterns-deep-dive/06-chain-of-responsibility`](09-spring-native-patterns-deep-dive/06-chain-of-responsibility/README.md).

Xuất hiện ở servlet filters, Spring Security filter chain, MVC interceptors và validation/rule pipelines. Luôn xác định:

- Thứ tự chain.
- Điều kiện short-circuit.
- Error propagation.
- Rule nào có side effect.

### Observer và Domain Event

Deep-dive project: [`09-spring-native-patterns-deep-dive/07-observer-domain-events`](09-spring-native-patterns-deep-dive/07-observer-domain-events/README.md).

Spring application event mặc định là synchronous và in-process. Nó không tự nhiên trở thành durable message.

Phân biệt:

- Domain event: điều đã xảy ra trong domain.
- Integration event: contract gửi ra ngoài bounded context.
- Transactional event listener: gắn listener với transaction phase.
- Broker event: có delivery semantics, retry, ordering và retention riêng.

### Adapter và Decorator

Deep-dive project: [`09-spring-native-patterns-deep-dive/08-adapter-decorator`](09-spring-native-patterns-deep-dive/08-adapter-decorator/README.md).

Controller, repository implementation, HTTP client và Kafka consumer là adapter. Logging, metrics, tracing, retry và caching thường phù hợp với decorator/interceptor hơn là trộn vào domain.

## 3. Kiến trúc ưu tiên cho hệ Spring lớn

### Package by Feature

Ưu tiên:

```text
commerce
├── order
│   ├── domain
│   ├── application
│   └── adapter
├── inventory
└── payment
```

Tránh một hệ thống lớn chỉ có các package toàn cục `controller`, `service`, `repository`, vì chúng che mất business boundary.

### Hexagonal Architecture / Ports and Adapters

Dependency direction:

```text
Driving adapter -> application use case -> domain
                                          ^
Driven adapter  -> outbound port ----------|
```

Quy tắc:

- Domain không import Spring, JPA, HTTP client hoặc broker SDK.
- Inbound port mô tả use case mà hệ thống cung cấp.
- Outbound port mô tả capability hệ thống cần.
- Adapter mapping external DTO sang domain type.
- Không cần tạo interface cho mọi class; interface nên biểu diễn boundary hoặc variation point.

### Modular Monolith

Đây là default tốt cho nhiều hệ thống trước microservices:

- Một deployment và transaction model đơn giản.
- Boundary module rõ ràng và kiểm tra tự động.
- Giao tiếp trực tiếp qua exposed API hoặc event.
- Có thể tách module ra service khi đã có bằng chứng vận hành.

Spring Modulith hỗ trợ phát hiện module, verify dependency, module test, sinh tài liệu, observability và persistent event publication.

### DDD tactical patterns

- Aggregate bảo vệ invariant và transaction boundary.
- Value Object immutable, so sánh theo value.
- Domain Service dùng khi logic không tự nhiên thuộc một entity/value object.
- Repository biểu diễn collection của aggregate, không phải generic CRUD gateway cho mọi table.
- Anti-Corruption Layer ngăn model bên ngoài xâm nhập domain.

Đừng biến mọi table thành aggregate và mọi service thành domain service.

## 4. Reliable data và messaging patterns

### Transactional Outbox

Business state và event record được commit cùng transaction. Publisher gửi event record ra broker sau đó. Pattern xử lý dual-write problem nhưng thường vẫn có at-least-once delivery, nên consumer phải idempotent.

### Inbox / Idempotent Consumer

Consumer lưu message ID hoặc business idempotency key. Check và business update phải nằm trong cùng transaction khi có thể.

### Saga

- Choreography: service phản ứng với event; coupling cú pháp thấp nhưng flow khó nhìn khi dài.
- Orchestration: orchestrator giữ state machine và ra command; flow rõ hơn nhưng orchestrator là thành phần quan trọng.

Không dùng distributed transaction 2PC như lựa chọn mặc định. Phải định nghĩa compensation theo business, không chỉ gọi API `undo` kỹ thuật.

### Locking

- Optimistic locking: phù hợp khi conflict hiếm, retry có giới hạn.
- Pessimistic locking: phù hợp critical section ngắn với contention đã đo được.
- Database constraint vẫn là lớp bảo vệ invariant cuối cùng.

### Cache Aside

Application đọc cache, miss thì đọc source of truth rồi populate cache. Cần quyết định TTL, invalidation, stale-data tolerance và cache stampede protection.

## 5. Resilience patterns

Khi phân tích resilience, có thể dùng thứ tự suy luận sau:

```text
Timeout -> Retry -> Circuit Breaker -> Bulkhead -> Rate Limiter -> Fallback
```

Đây không phải pipeline bắt buộc và thứ tự decorator thực tế ảnh hưởng semantics. Ví dụ rate limiter/admission control thường cần từ chối trước khi tiêu tốn downstream resource; retry phải nằm trong deadline và không được vô tình đi vòng ngoài làm sai thống kê circuit breaker. Thiết kế dựa trên failure model, không ghép đủ annotation cho có.

### Timeout

Mọi network call cần connect timeout và response/read timeout. Timeout budget phải nhỏ hơn request deadline tổng.

### Retry

- Chỉ retry transient failure.
- Dùng exponential backoff và jitter.
- Không retry validation/authentication lỗi.
- Write operation cần idempotency key.
- Tránh retry ở nhiều layer gây retry amplification.

Spring Framework 7 có native `@Retryable`, programmatic `RetryTemplate` và `@ConcurrencyLimit`; bật bằng `@EnableResilientMethods`.

### Circuit Breaker

Ngăn tiếp tục gọi dependency đang lỗi. Circuit breaker không thay thế timeout và không đảm bảo fallback đúng nghiệp vụ.

### Bulkhead / Concurrency Limit

Cô lập tài nguyên để một dependency không chiếm hết thread/connection. Với virtual threads, vẫn cần giới hạn concurrency tới database hoặc remote service.

### Rate Limiter

Bảo vệ quota hoặc capacity. Cần xác định key: API key, tenant, customer hay endpoint.

## 6. Integration patterns mới

### HTTP Service Client

Khai báo remote contract bằng interface `@HttpExchange`; proxy được tạo trên `RestClient` hoặc `WebClient`.

- Dùng `RestClient` cho synchronous/blocking flow.
- Dùng `WebClient` khi thực sự cần non-blocking, streaming hoặc backpressure.
- `RestTemplate` đã deprecated trong Spring Framework 7.
- Remote DTO phải dừng ở adapter boundary.

### API Composition

Chỉ aggregate synchronous calls khi latency budget cho phép. Fan-out lớn làm tail latency và failure probability tăng mạnh. Cân nhắc read model/materialized view cho query quan trọng.

## 7. Concurrency hiện đại

### Virtual Threads

Virtual threads phù hợp blocking I/O và mô hình thread-per-request. Không làm CPU-bound task nhanh hơn, không thay connection pool, rate limit hay concurrency control.

Spring Boot:

```properties
spring.threads.virtual.enabled=true
spring.main.keep-alive=true
```

Cần theo dõi pinned threads và dùng JFR/jcmd khi benchmark. Trên Java 21, blocking trong `synchronized` có thể pin carrier; từ Java 24, JEP 491 đã loại bỏ gần như toàn bộ pinning do monitor. Spring Boot hiện khuyến nghị Java 24+ để có trải nghiệm Virtual Threads tốt hơn. Native/JNI/FFM hoặc thư viện cũ vẫn cần profiling. Không kết luận dựa trên microbenchmark không có database/network contention.

### Async boundary

`@Async` thay execution context. Transaction, MDC, security context và observation context không nên được giả định là tự truyền. Boundary async phải được thiết kế và test rõ ràng.

## 8. Observability như một design concern

Ba tín hiệu: logs, metrics, traces. Dùng Micrometer Observation làm abstraction chính trong Spring application.

- Low-cardinality tags dùng được cho metrics và traces.
- High-cardinality values như user/order ID chỉ nên vào trace/log phù hợp.
- Correlation ID cần propagation qua HTTP/message boundaries.
- SLI xuất phát từ user journey: latency, errors, availability, freshness.
- Đo retry count, circuit state, queue lag, outbox backlog và idempotency conflicts.

## 9. Null-safety và API design

Spring Framework 7 chuyển sang JSpecify. Với Java codebase lớn:

- Package mặc định `@NullMarked`.
- Dùng `@Nullable` tại type-use nơi absence hợp lệ.
- Không dùng `Optional` cho entity field/JPA mapping một cách máy móc.
- Phân biệt missing, empty và invalid trong API contract.

## 10. CQRS và Event Sourcing

CQRS không bắt buộc hai database và không đồng nghĩa Event Sourcing. Chỉ dùng khi read/write model khác nhau đủ lớn hoặc cần scaling/security/workflow khác nhau.

Event Sourcing phù hợp khi event history là source of truth thực sự và business cần temporal reconstruction/audit mạnh. Chi phí gồm schema evolution, replay, projection consistency và operational tooling.

## 11. Anti-pattern checklist

- `@Transactional` bị vô hiệu vì self-invocation.
- Entity JPA trả thẳng ra REST API.
- Module truy cập repository nội bộ của module khác.
- God service vừa validate, persist, gọi remote, publish event và format response.
- Dùng Spring event như durable broker.
- Retry mọi exception hoặc retry non-idempotent operation.
- WebFlux được chọn chỉ vì được cho là luôn nhanh hơn.
- Microservice được tách theo entity/table.
- Generic `BaseService<T>` che mất use case và invariant.
- Exception bị nuốt để fallback thành response thành công giả.
- High-cardinality business ID được đưa vào metric label.

## 12. Lộ trình 8 tuần

### Tuần 1 — Java composition

Làm project `01-java-patterns`. Refactor `if/switch` thành Strategy, thiết kế validation chain, thêm decorator. Viết unit test trước khi thêm implementation.

### Tuần 2 — Spring internals

Làm `02-spring-core-patterns`. Quan sát injected strategy order và AOP proxy. Tạo một self-invocation case rồi sửa bằng cách tách bean/boundary.

### Tuần 3–4 — Hexagonal modular monolith

Làm `03-hexagonal-modulith`. Thêm payment module, giữ domain độc lập framework, viết architecture verification và module test.

### Tuần 5 — Reliable event

Làm `04-reliable-events`. Mô phỏng listener fail, kiểm tra publication record, thiết kế idempotency và recovery policy.

### Tuần 6 — Remote integration

Làm `05-http-resilience`. Thêm timeout, idempotency key, retry filter, test 500 -> 200 và chứng minh số attempts.

### Tuần 7 — Concurrency

Làm `06-observability-concurrency`. So sánh platform/virtual thread dưới blocking workload; thêm concurrency limit cho downstream có pool nhỏ.

### Tuần 8 — Capstone

Mở rộng project 03/04 thành commerce system gồm order, inventory, payment và notification. Sau đó thử tách payment thành service riêng mà không đổi order domain port.

## 13. Definition of Done cho mỗi project

- Có ít nhất một test cho happy path và một test cho failure path.
- Giải thích được dependency direction.
- Không để framework DTO/type lọt vào domain nếu không có chủ ý.
- Có test cho boundary quan trọng, không chỉ context-load test.
- Ghi lại trade-off và trường hợp không nên dùng pattern.
- Với concurrency/network/event: xác định timeout, retry, idempotency và observability.

## 14. Đáp án cho bốn câu hỏi xuyên suốt

### Cách hiểu bốn câu hỏi

#### 1. Pattern giải quyết force/conflict nào?

`Force` là hai hoặc nhiều nhu cầu kéo thiết kế theo các hướng khác nhau. Ví dụ hệ thống cần gọi payment nhanh nhưng payment provider có thể chậm; cần publish event nhưng business state và broker không có transaction chung. Pattern là một cách cân bằng các forces trong một context, không phải giải pháp luôn đúng.

Một câu trả lời senior phải nêu:

- context và invariant/SLO;
- forces đang xung đột;
- vì sao cách đơn giản hơn chưa đủ;
- consequence và trường hợp không nên dùng pattern.

#### 2. Boundary và dependency chạy theo hướng nào?

Phân biệt hai hướng:

- **Compile-time dependency:** code nào import/biết abstraction nào. Trong Hexagonal, adapter phụ thuộc port/application/domain; domain không phụ thuộc framework.
- **Runtime call/data flow:** request có thể đi `controller -> use case -> port -> adapter -> database`, nhưng compile-time dependency của adapter vẫn hướng vào port do core định nghĩa.

Boundary phải nói rõ owner của contract, transaction, dữ liệu, failure handling và mapping. Chỉ tạo interface không tự sinh ra architecture boundary.

#### 3. Failure mode mới do pattern tạo ra là gì?

Mọi pattern đổi một loại complexity lấy loại khác. Ví dụ retry che transient failure nhưng tạo retry storm/duplicate; cache giảm latency nhưng tạo stale data/stampede; event giảm temporal coupling nhưng tạo duplicate/order/lag/replay problem.

Câu trả lời chưa đủ nếu chỉ nói lợi ích. Phải nêu detection, containment, recovery và operator action.

#### 4. Test nào chứng minh implementation đúng?

Test pattern phải chứng minh semantics tại boundary, không chỉ kiểm tra bean tồn tại:

- happy path;
- failure/timeout/conflict/duplicate;
- invariant và side effect count;
- recovery/retry/replay;
- architecture dependency;
- observability signal;
- load/concurrency nếu correctness phụ thuộc timing hoặc capacity.

### 14.1 Spring-native và composition patterns

| Pattern | Force/conflict được giải quyết | Boundary/dependency | Failure mode mới | Test có sức chứng minh |
|---|---|---|---|---|
| Dependency Injection / IoC | Cần thay implementation/testability nhưng không muốn caller tự dựng graph | Consumer phụ thuộc abstraction; composition root/Spring config chọn implementation | Ambiguous/missing bean, hidden optional dependency, circular graph, scope/lifecycle sai | Context slice cho wiring + unit test khởi tạo use case bằng fake; test fail-fast khi thiếu strategy |
| Proxy/AOP | Cần transaction/security/retry/observation nhất quán mà không trộn boilerplate vào use case | External call đi qua proxy rồi target; advice là infrastructure boundary | Self-invocation bypass, advice order sai, transaction rộng, exception bị đổi làm sai rollback | Integration test gọi qua bean proxy; test self-invocation case; assert transaction rollback/commit và attempt count |
| Strategy | Policy/algorithm thay đổi theo payment/country/tenant mà không tăng `if/switch` | Application/domain gọi strategy contract; implementations nằm cùng core hoặc adapter tùy dependency | Duplicate/missing key, silent fallback, strategy order không deterministic | Parameterized contract test cho mọi strategy; registry uniqueness/missing-key test; business invariant test |
| Factory | Khởi tạo/lựa chọn object cần invariant hoặc nhiều biến thể | Caller phụ thuộc factory/domain creation API, không biết concrete construction | Factory thành service locator hoặc god factory; default sai bị che | Test từng input tạo đúng type/state; invalid combination bị reject; không cần Spring context nếu factory thuần |
| Template Method / Callback | Framework phải giữ resource lifecycle nhưng logic thao tác thay đổi | Template sở hữu open/close/transaction/translation; callback chỉ cung cấp phần biến đổi | Callback leak resource, giữ transaction quá lâu, swallow/wrap exception sai | Test cleanup cả success/failure; exception translation; transaction rollback; callback không dùng resource sau scope |
| Chain of Responsibility | Nhiều rule/filter cần compose, reorder và short-circuit | Request đi lần lượt qua handler contract; composition root sở hữu order | Sai order, chạy side effect hai lần, chain không dừng, error bị nuốt | Test order, short-circuit, error propagation, side-effect count; integration test security/filter chain quan trọng |
| Adapter | Core cần giao tiếp HTTP/DB/broker nhưng không phụ thuộc protocol/vendor | Core định nghĩa port; adapter phụ thuộc port và map external model | Mapping mất semantic, exception vendor rò vào core, chatty calls | Port contract test; adapter integration test với real protocol/Testcontainers; mapping/error taxonomy test |
| Decorator | Cần thêm metrics/cache/retry/logging quanh capability mà giữ core đơn giản | Decorator implement cùng port và delegate; composition root xác định order | Double instrumentation, cache/retry order sai, decorator quên delegate/propagate | Assert delegate calls/order; cache hit/miss; metric attempt/outcome; exception không bị đổi ngoài contract |

### 14.2 Architecture và DDD patterns

| Pattern | Force/conflict được giải quyết | Boundary/dependency | Failure mode mới | Test có sức chứng minh |
|---|---|---|---|---|
| Package by Feature | Code cần phản ánh business ownership nhưng technical-layer packages làm boundary biến mất | Dependency chủ yếu ở trong feature; cross-feature qua API/event công khai | Package chỉ đổi tên nhưng repository/entity vẫn bị dùng chéo | ArchUnit/Modulith verification cấm internal package access; module-level tests |
| Hexagonal Architecture | Domain cần ổn định trong khi UI/database/vendor thay đổi | Driving adapter -> inbound port/use case -> domain; driven adapter implements outbound port do core sở hữu | Quá nhiều interface/mapping; anemic core; abstraction phản chiếu vendor | Domain unit test không Spring; port contract test; adapter integration test; dependency rule test |
| Modular Monolith | Cần module autonomy nhưng microservices operational cost chưa đáng | Module giao tiếp qua exposed API/event; internal types/repositories không lộ | Distributed-monolith habits trong một process, synchronous cycles, shared table ownership | Modulith verify; module integration test; event test; cấm cycles/internal access |
| Aggregate | Cần bảo vệ invariant và transaction consistency boundary | Command vào aggregate root; repository load/save root; entity con không bị sửa tùy ý | Aggregate quá lớn gây contention hoặc quá nhỏ làm invariant rò ra ngoài | Unit test invariant/transitions; optimistic conflict test; transaction integration test |
| Value Object | Giá trị có identity theo value và cần bất biến/validation | Domain sở hữu type; external primitives được map tại adapter | Mapping/serialization/JPA friction; validation duplicate ở nhiều nơi | Constructor/property tests; equality/hashCode; serialization/persistence integration nếu cần |
| Domain Service | Domain logic cần nhiều object nhưng không thuộc tự nhiên vào một entity | Domain service chỉ phụ thuộc domain abstractions; application service điều phối I/O | Mọi logic bị đẩy vào stateless “service”, entity trở nên anemic | Unit test business rule không framework; review dependency để không có repository/HTTP ngoài chủ ý |
| Repository | Domain cần persistence abstraction theo aggregate | Core định nghĩa collection-like port; adapter hiện thực bằng JPA/JDBC | Generic CRUD để lộ persistence model; query explosion; transaction assumptions ẩn | Contract test repository; integration test query/locking; test không save aggregate invalid |
| Anti-Corruption Layer | Model/semantics legacy/vendor xung đột ubiquitous language nội bộ | ACL adapter map DTO/error/state ngoài sang port/domain model | Mapping drift, information loss, latency, ACL thành passthrough | Golden mapping tests; consumer/provider contract; unknown enum/error compatibility test |

### 14.3 Data và messaging patterns

| Pattern | Force/conflict được giải quyết | Boundary/dependency | Failure mode mới | Test có sức chứng minh |
|---|---|---|---|---|
| Domain Event | Module cần thông báo điều đã xảy ra mà không gọi trực tiếp mọi observer | Aggregate/application phát domain fact; handler cùng bounded context | Handler synchronous làm transaction chậm; side effect ẩn; listener order assumptions | Assert event từ state transition; handler failure semantics; transaction phase test |
| Transactional Outbox | DB state và event intent cần atomic nhưng DB/broker không có transaction chung | Application transaction ghi aggregate + outbox; relay/CDC adapter publish | Duplicate, backlog, out-of-order, cleanup/storage growth, schema evolution | Crash-after-commit test; relay retry; duplicate delivery; backlog/recovery; event contract test |
| Inbox / Idempotent Consumer | At-least-once delivery có duplicate nhưng business effect chỉ được xảy ra một lần | Consumer adapter nhận message; inbox key và business update cùng transaction | Key scope/TTL sai, concurrent duplicate, poison message | Gửi cùng message đồng thời/nhiều lần; crash before/after commit; assert one business effect |
| Saga | Business transaction qua nhiều data owner không thể atomic bằng local DB transaction | Orchestrator/process manager hoặc event contracts điều phối local transactions | Stuck state, compensation fail, duplicate/out-of-order event, semantic rollback không hoàn hảo | State-transition table test; inject fail tại mọi step; duplicate/timeout/compensation/recovery test |
| Optimistic Lock | Concurrent update hiếm nhưng cần phát hiện lost update | Version check ở persistence; application quyết định retry/conflict response | Retry starvation, user overwrite sau retry mù, conflict storm | Hai transaction cùng version; một commit, một conflict; retry bounded và invariant giữ nguyên |
| Pessimistic Lock | Critical section cần serialize vì contention/oversell đã chứng minh | Database transaction giữ lock; application giới hạn scope/duration | Deadlock, lock wait, throughput collapse, lock leak do transaction dài | Concurrent test, lock timeout/deadlock handling; đo hold/wait time; invariant/constraint test |
| Cache Aside | Read latency/load cần giảm nhưng source of truth vẫn ở DB/service | Application/adapter đọc cache rồi source; domain không biết cache | Stale value, stampede, hot key, cache outage, invalidation race | Hit/miss/expiry; concurrent miss; update/invalidation race; cache-down fallback; freshness metric |

### 14.4 Resilience và integration patterns

| Pattern | Force/conflict được giải quyết | Boundary/dependency | Failure mode mới | Test có sức chứng minh |
|---|---|---|---|---|
| Timeout/Deadline | Remote dependency có thể chờ vô hạn nhưng request có latency SLO | Client adapter áp connect/read timeout; application truyền deadline budget | Timeout quá ngắn tạo false failure; quá dài gây resource exhaustion; work tiếp tục sau caller timeout | Delayed stub; assert bounded duration/cancellation/error mapping; deadline giảm qua từng hop |
| Retry | Transient failure có thể hồi phục nhưng caller cần reliability | Đặt tại một owner/layer quanh idempotent operation | Duplicate side effect, retry storm/amplification, vượt deadline | Stub `500 -> 200`; assert exact attempts/backoff; non-retryable không retry; idempotency prevents duplicate |
| Circuit Breaker | Dependency lỗi kéo dài làm lãng phí capacity và lan failure | Decorator quanh remote port; state/metrics ở infrastructure | Threshold sai, half-open herd, fallback che lỗi, stale open state | Đủ failures mở circuit; open fail-fast; half-open recovery; metrics/state transition test |
| Bulkhead / Concurrency Limit | Một dependency/workload không được chiếm hết tài nguyên chung | Semaphore/pool/cell tại outbound boundary hoặc admission point | Queue starvation, reject sai priority, permit leak, limit thấp thành bottleneck | Concurrent test assert max in-flight; timeout/rejection; permit released on exception/cancel |
| Rate Limiter | Cần bảo vệ quota/fairness/capacity trước traffic burst | Gateway và/hoặc service theo tenant/principal/operation cost | Noisy neighbor do key sai, clock/distributed counter drift, bypass qua endpoint khác | Boundary/window/burst test; per-tenant isolation; `429`/retry metadata; distributed integration test |
| Fallback | Dependency unavailable nhưng business cho phép degraded result | Application policy quyết định; adapter cung cấp cache/alternative data | Success giả, stale/unsafe data, lỗi authorization bị che | Chỉ fallback lỗi cho phép; response có degradation/freshness; không fallback auth/data corruption |
| HTTP Service Client | Muốn typed remote contract và giảm boilerplate | Interface/DTO thuộc adapter/integration boundary; domain chỉ biết outbound port | Proxy làm network call trông như local, default timeout/retry mơ hồ, DTO leak | Mock server/contract test cho status/body/header/error; timeout/observation; mapping sang domain |
| API Composition | Client cần một view từ nhiều owner nhưng nhiều round-trip không phù hợp | BFF/aggregator gọi nhiều outbound ports hoặc đọc materialized view | Tail-latency amplification, partial failure, N+1, inconsistent snapshot | Parallel fan-out deadline; one-branch failure; partial-result policy; query/call count; load test p99 |

### 14.5 Concurrency, observability và read/write patterns

| Pattern | Force/conflict được giải quyết | Boundary/dependency | Failure mode mới | Test có sức chứng minh |
|---|---|---|---|---|
| Virtual Threads | Nhiều blocking I/O cần thread-per-task dễ đọc nhưng platform thread đắt | Servlet/task execution dùng virtual thread; database/downstream vẫn qua bounded ports | Unbounded in-flight work làm cạn DB/downstream, CPU-bound không nhanh hơn, Java 21 pinning | So sánh platform/virtual cùng workload; DB pool nhỏ; assert admission limit; JFR/thread dump; CPU-bound control test |
| `@Async` | Caller không cần chờ hoặc work cần execution context khác | Public proxy boundary chuyển task sang executor; transaction/message durability phải thiết kế riêng | Context mất, exception không được observe, task mất khi crash, queue/executor quá tải | Gọi qua proxy; context propagation; exception handler/future; executor saturation; shutdown/crash semantics |
| Observability/Decorator | Cần hiểu hành vi production nhưng domain không nên phụ thuộc telemetry SDK | Instrument adapter/use-case boundary; propagate context qua HTTP/message | Cardinality explosion, sensitive data leak, missing async context, double spans | Assert low-cardinality tags; trace propagation; retry/queue/outbox metrics; redaction test |
| CQRS | Read/write model có forces khác nhau nhưng một model gây coupling/performance/security kém | Command owner ghi source; projection consumer xây read model; query API đọc projection | Stale projection, rebuild/replay, dual model drift, read-your-write gap | Projection idempotency; out-of-order/replay; rebuild equality; freshness/SLO; query contract test |
| Event Sourcing | Event history cần là source of truth và temporal reconstruction là business capability | Command -> aggregate -> append event stream; projections phụ thuộc event contract | Event schema vĩnh viễn, replay side effects, projection/version tooling phức tạp | Given-events/when-command/then-events; optimistic stream version; upcaster; replay deterministic và không external side effect |
| Null-safety/API absence | Cần phân biệt required/optional/missing/empty và bắt lỗi sớm | JSpecify ở Java type boundary; API DTO/domain mapping quyết định semantics | Annotation sai/inconsistent override, ORM/serialization mismatch, `null` lẩn qua unmarked code | Static NullAway/IDE checks; JSON missing/null/empty contract test; repository mapping integration |

### 14.6 Năm câu trả lời mẫu ở mức senior

#### Khi nào dùng Strategy thay `if/switch`?

Dùng khi variation là policy có lifecycle, dependency hoặc test matrix riêng và còn tiếp tục tăng. Một `switch` exhaustive trên sealed type vẫn tốt nếu tập biến thể nhỏ, ổn định và logic nằm đúng trong một nơi. Strategy đổi conditional complexity thành registration/selection complexity, nên registry phải phát hiện duplicate/missing key ngay startup và mọi implementation phải chạy chung contract tests.

#### Vì sao `@Transactional` self-invocation là vấn đề?

Declarative transaction thường được áp bởi proxy. Một method trong target gọi `this.otherMethod()` không quay lại proxy, nên metadata ở method kia có thể không được áp. Sửa bằng cách đặt transaction ở public application use case, tách collaborator bean khi đó là boundary thật, hoặc dùng programmatic transaction khi flow cần explicit control. Test phải gọi bean từ Spring context và kiểm tra commit/rollback, không chỉ mock method call.

#### Outbox có bảo đảm exactly-once không?

Không. Outbox bảo đảm business state và ý định publish được commit atomically trong cùng database transaction. Relay hoặc broker vẫn có thể gửi trùng, đặc biệt khi crash sau publish nhưng trước khi đánh dấu. Consumer cần inbox/idempotency theo business effect. “Exactly-once” chỉ có nghĩa khi nêu rõ scope: transport, processing transaction hay business outcome.

#### Virtual Threads thay đổi system design thế nào?

Chúng làm blocking code scale tới concurrency cao với ít platform threads hơn, nhưng loại bỏ thread-pool bottleneck vốn vô tình giới hạn tải. Vì vậy phải thiết kế admission control rõ cho DB connection, downstream API và CPU section; theo dõi in-flight/queue wait/p99 thay vì chỉ thread count. Với Java 21 cần chú ý monitor pinning; Java 24+ giảm mạnh vấn đề này nhưng không xóa các bottleneck tài nguyên khác.

#### Khi nào tách Modular Monolith thành microservice?

Khi một module có boundary domain/ownership đã ổn định và có nhu cầu độc lập đủ lớn về deploy cadence, scaling, availability, data governance hoặc team autonomy. Không tách chỉ vì codebase lớn. Trước khi tách phải có explicit API/event contract, data ownership, observability, failure/retry/idempotency và migration/reconciliation plan; nếu không sẽ tạo distributed monolith.

### 14.7 Template trả lời phỏng vấn trong hai phút

```text
Context: hệ thống/use case/SLO/invariant nào?
Forces: những nhu cầu nào đang xung đột?
Decision: chọn pattern gì và boundary ở đâu?
Trade-off: complexity/failure mode/cost mới là gì?
Evidence: test, metric hoặc ADR nào chứng minh?
Alternative: khi nào giữ cách đơn giản hơn hoặc đổi pattern?
```

## 15. Tài liệu chính thức

- [Spring Boot](https://spring.io/projects/spring-boot/)
- [Spring Framework resilience](https://docs.spring.io/spring-framework/reference/core/resilience.html)
- [Spring REST clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [Spring Modulith events](https://docs.spring.io/spring-modulith/reference/events.html)
- [Spring Boot observability](https://docs.spring.io/spring-boot/reference/actuator/observability.html)
- [Spring Boot virtual threads](https://docs.spring.io/spring-boot/reference/features/spring-application.html#features.spring-application.virtual-threads)
- [Spring null-safety](https://docs.spring.io/spring-framework/reference/core/null-safety.html)
- [JEP 491 - Synchronize Virtual Threads without Pinning](https://openjdk.org/jeps/491)
