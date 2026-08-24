# 11 — Ánh xạ Spring Boot sang Node.js/Next.js

Ánh xạ giúp học nhanh, nhưng không coi hai runtime giống nhau.

| Spring/Java | Node/Fastify/Nest | Khác biệt quan trọng |
|---|---|---|
| JVM + threads/Virtual Threads | V8 isolate + event loop/libuv | CPU callback block toàn isolate |
| `CompletableFuture` | Promise | Promise không là thread/task cancellation tự động |
| `ThreadLocal`/MDC | AsyncLocalStorage | propagation theo async resource, không theo thread-per-request |
| Spring bean singleton | module/object/provider singleton | closure/module cache cũng tạo long-lived state |
| `@Bean`/DI | factory/composition root/Nest provider | structural typing/token/reflection khác Java |
| MVC controller | Fastify route/Nest controller | runtime schema cần explicit |
| Filter/interceptor/advice | hook/plugin/middleware/interceptor | lifecycle/order khác framework |
| Bean Validation | JSON Schema/Zod/class-validator | TS type bị erase, validation không tự có |
| Jackson DTO | serializer schema/plain DTO | object shape dễ lộ extra field nếu không lọc |
| HikariCP | DB driver/ORM pool | event loop concurrency vượt pool rất dễ |
| `@Transactional` | transaction callback/unit of work | async scope/lifetime cần cẩn thận |
| JPA persistence context | ORM client/session tùy library | không giả định identity map/dirty checking giống Hibernate |
| `@Cacheable` | cache wrapper/plugin/Next cache | Next có nhiều cache layers/render semantics |
| Actuator/Micrometer | health plugins + OpenTelemetry/metrics libs | ít convention thống nhất hơn |
| Maven/Gradle BOM | package.json + lockfile/workspaces | npm dependency graph/supply-chain khác |
| JUnit/Testcontainers | node:test/Vitest/Jest + Testcontainers | runtime transform/mock model khác |
| Spring Cloud Gateway | Fastify gateway/Next BFF/reverse proxy | Next BFF gắn UI/render lifecycle |
| Thymeleaf/MVC SSR | Next RSC/SSR/streaming | component server/client split và hydration |

## Tư duy Java nên giữ

- explicit transaction/invariant;
- hexagonal/modular boundary;
- idempotency, outbox, saga và message ordering;
- schema/API compatibility;
- resource budget, SLO và observability;
- test failure modes và graceful shutdown.

## Tư duy cần điều chỉnh

### Đừng class hóa mọi thứ

Function/module/factory và discriminated union thường đơn giản hơn interface + abstract class. Dùng class khi identity/state/invariant hoặc framework convention thực sự cần.

### Compile-time không phải runtime

Java DTO deserialization thường gắn validation/type conversion trong framework. TypeScript interface biến mất; external data phải parse explicit.

### Không block event loop

Java có thể dành thread cho CPU/blocking request (vẫn có limits). Node main isolate khiến một synchronous loop/JSON/regex lớn làm chậm mọi request trên process.

### Request scope không miễn phí

Nest request-scoped provider tạo subgraph theo request; tương tự Spring request scope nhưng trong Node high-concurrency có allocation/DI cost. Stateless singleton + explicit request data thường tốt hơn.

### Framework ecosystem ít “một chuẩn” hơn Spring

Logging, metrics, validation, ORM, migration, config và DI có nhiều lựa chọn. Team cần platform conventions/ADR/template để tránh mỗi service một stack.

## Chọn Node hay Spring

Node phù hợp:

- I/O-heavy API/BFF/realtime;
- full-stack TypeScript/team product nhỏ;
- server-side rendering và UI composition;
- service cần iteration nhanh, ecosystem JS.

Spring phù hợp:

- domain backend enterprise với mature Java platform/team;
- ecosystem JVM/data/integration cần thiết;
- CPU/multithread workload phù hợp;
- governance/conventions và long-lived systems.

Không quyết định bằng throughput benchmark đơn giản. Xem team skill, domain, dependency ecosystem, SLO, operability, deployment và ownership. Polyglot chỉ đáng khi lợi ích lớn hơn platform/cognitive cost.

## Architecture target thực dụng

```text
Next.js
  - UI, RSC, SEO, session-aware BFF
  - không chứa mọi core domain

Node/Fastify/Nest service
  - realtime/I/O-heavy/domain phù hợp JS team

Spring Boot service
  - domain/integration có lợi thế JVM/team hiện hữu

Shared contracts
  - OpenAPI/AsyncAPI/event schema, không share internal model
```

Senior architect tối ưu toàn socio-technical system, không biến chọn ngôn ngữ thành identity.
