# Lộ trình 20 tuần

Mỗi tuần: 5–7 giờ đọc, 5–7 giờ code, 1 giờ viết architecture decision record (ADR). Senior nên dành nhiều thời gian cho failure mode và profiling hơn syntax.

## Phase 1 — Language và runtime (tuần 1–4)

### Tuần 1: Modern JavaScript

- lexical scope, closure, prototype, class syntax;
- value/reference semantics, mutation, equality;
- iterator/generator, Promise, async/await;
- ESM và CommonJS interop.

**Bài tập:** giải thích vì sao `await` không tạo thread; viết retry có timeout, jitter và `AbortSignal`.

### Tuần 2: TypeScript strict

- union/intersection, narrowing, discriminated union;
- generics, conditional/mapped types vừa đủ;
- `unknown` thay `any`, `never` cho exhaustiveness;
- runtime validation khác compile-time types.

**Bài tập:** model domain result bằng discriminated union, không dùng exception cho expected outcome.

### Tuần 3: Node runtime

- V8, event loop, microtask/macrotask;
- libuv worker pool; blocking APIs;
- timers, cancellation, AsyncLocalStorage.

**Bài tập:** chạy `apps/node-runtime-lab`, đo event-loop delay khi CPU loop chạy trên main thread và Worker Thread.

### Tuần 4: Streams và process lifecycle

- readable/writable/transform, pipeline/backpressure;
- signal, graceful shutdown, unhandled rejection;
- ESM package/export boundaries.

**Acceptance:** stream file lớn với heap gần như ổn định; shutdown không nhận request mới và chờ in-flight có deadline.

## Phase 2 — Backend engineering (tuần 5–10)

### Tuần 5: HTTP/API

- method semantics, status code, headers, content negotiation;
- pagination, filtering, versioning, error contract;
- schema validation và OpenAPI.

### Tuần 6: Fastify

- plugin encapsulation, decorators, hook lifecycle;
- JSON Schema validation/serialization;
- inject-based tests và structured logging.

**Bài tập:** mở rộng `fastify-api` thêm update endpoint và optimistic concurrency.

### Tuần 7: NestJS và architecture

- module/provider/controller, guard/pipe/interceptor/filter;
- request scope cost, DI tokens, dynamic module;
- modular monolith, hexagonal architecture.

**ADR:** khi nào chọn NestJS thay Fastify thuần.

### Tuần 8: Database

- driver pool, transaction boundary, isolation/locking;
- ORM/Query Builder trade-off; N+1 và batching;
- migration, outbox và connection budget.

### Tuần 9: Cache và messaging

- cache-aside, TTL/jitter, stampede, invalidation;
- Kafka partition/order, idempotent consumer, retry/DLQ;
- bounded concurrency và backpressure.

### Tuần 10: Security

- authentication/session/token và authorization gần data;
- validation, injection, SSRF, prototype pollution;
- CSRF/CORS/CSP/cookie, dependency/supply-chain;
- secret handling và Node permission model.

**Acceptance:** threat model, security tests và dependency audit cho API lab.

## Phase 3 — Reliability và scale (tuần 11–13)

### Tuần 11: Testing

- unit, component/integration, contract, end-to-end;
- deterministic clock/random/ID;
- database/container test và failure injection.

### Tuần 12: Performance/observability

- throughput vs latency, event-loop utilization/delay;
- heap snapshot, allocation profile, CPU flame graph;
- logs/metrics/traces, AsyncLocalStorage context.

### Tuần 13: Production delivery

- multi-stage image, non-root, health/readiness;
- graceful shutdown, zero-downtime rollout;
- resource limit, autoscaling, runtime upgrade policy.

**Acceptance:** load test normal + slow dependency + SIGTERM; có dashboard/runbook.

## Phase 4 — Next.js App Router (tuần 14–18)

### Tuần 14: React mental model

- render, state, effect, controlled form;
- composition, Suspense, error boundary;
- server/client execution environments.

### Tuần 15: App Router

- layout/page/template, dynamic segments, route groups;
- loading/error/not-found, metadata;
- Server Component mặc định, tối thiểu hóa client boundary.

### Tuần 16: Data và cache

- server-side data access, parallel fetch, streaming;
- Cache Components, `use cache`, cache lifetime/tag;
- freshness, invalidation và personalized data.

### Tuần 17: Mutation và security

- Server Function/Action, Route Handler;
- validate input và authorize lại mỗi mutation;
- DAL, DTO, `server-only`, secret/env boundary.

### Tuần 18: Production

- Core Web Vitals, bundle, image/font, SEO;
- self-hosting, reverse proxy, multi-instance cache;
- OpenTelemetry, logs, error monitoring.

**Acceptance:** build `next-learning`; giải thích từng component chạy ở đâu và dữ liệu nào được ship tới browser.

## Phase 5 — Architecture capstone (tuần 19–20)

Thiết kế một commerce platform:

```text
Browser
  -> CDN / reverse proxy
  -> Next.js web + BFF
  -> Identity / Catalog / Order APIs
  -> PostgreSQL + Redis + Kafka
```

Deliverables:

- C4 context/container/component;
- API và error/idempotency contract;
- cache/freshness matrix;
- authn/authz and data exposure model;
- connection/concurrency/memory budget;
- observability/SLO/runbook;
- ADR: phần nào ở Next.js, Node service hay Spring service.
