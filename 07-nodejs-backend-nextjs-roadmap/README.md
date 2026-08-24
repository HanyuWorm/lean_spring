# Node.js Backend & Next.js — Senior Learning Track

Track này dành cho senior Java/Spring Boot muốn học Node.js backend và Next.js theo thứ tự từ runtime đến architecture, thay vì chỉ học syntax/framework API.

## Baseline tại 2026-08-24

- Node.js 24 LTS cho production; Node 26 vẫn là Current tại thời điểm biên soạn.
- TypeScript strict; ESM là mặc định cho code mới trong workspace này.
- Fastify 5 cho API lab nhẹ, schema-first và dễ nhìn lifecycle.
- NestJS 11 được giải thích như lựa chọn enterprise gần Spring, nhưng không dùng làm abstraction đầu tiên.
- Next.js 16.3, React 19.2 và App Router.

Version web thay đổi nhanh. Luôn xem [SOURCES.md](SOURCES.md), Node release status và Next.js security advisory trước khi nâng production.

## Mental model

```text
JavaScript/TypeScript language
        ↓
V8: heap, JIT, garbage collector
        ↓
Node.js runtime: event loop + libuv + native APIs
        ↓
Backend framework: Fastify / NestJS / Express
        ↓
Data, messaging, security, observability
        ↓
Next.js: React Server Components + routing + cache + mutations
```

Node.js không phải “JavaScript single-thread nên không có concurrency”. Một JavaScript isolate thường chạy callback trên một main thread, trong khi kernel/libuv xử lý asynchronous I/O, một worker pool xử lý một số API, và Worker Threads có thể chạy JavaScript CPU-bound song song. Kiến trúc tốt phải biết đoạn nào đang block event loop.

## Nội dung học

| Chương | Chủ đề | Kết quả |
|---|---|---|
| [01](notes/01-javascript-typescript.md) | JavaScript và TypeScript | Dùng type system mà không mang nguyên xi tư duy Java |
| [02](notes/02-node-runtime.md) | Event loop, async, streams, workers | Giải thích được concurrency và backpressure |
| [03](notes/03-backend-api.md) | HTTP API, Fastify, NestJS | Thiết kế API có validation và lifecycle rõ |
| [04](notes/04-data-distributed-systems.md) | Database, cache, queue, idempotency | Xây backend reliable thay vì CRUD demo |
| [05](notes/05-security.md) | Auth, dependency, input, secret | Threat-model Node/Next production |
| [06](notes/06-performance-observability.md) | Memory, profiling, telemetry | Chẩn đoán event-loop lag, heap và latency |
| [07](notes/07-testing-delivery.md) | Test pyramid, CI/CD, container | Ship artifact reproducible và graceful |
| [08](notes/08-nextjs-foundations.md) | App Router, RSC, routing | Chọn đúng Server/Client boundary |
| [09](notes/09-nextjs-data-cache-security.md) | Fetch, cache, action, auth | Không sai caching hoặc làm lộ dữ liệu |
| [10](notes/10-nextjs-production.md) | SEO, performance, deploy | Vận hành Next.js có SLO |
| [11](notes/11-spring-to-node-map.md) | So sánh Spring ↔ Node | Tận dụng kiến thức Java, tránh false equivalence |
| [12](notes/12-interview-questions.md) | 60 câu hỏi và đáp án | Luyện từ foundation đến architect |

Lộ trình theo tuần: [ROADMAP.md](ROADMAP.md).

## Ba lab chạy được

| Lab | Mục tiêu | Lệnh |
|---|---|---|
| [`apps/node-runtime-lab`](apps/node-runtime-lab/README.md) | Event loop, abort, stream, worker, built-in test | `npm run test:runtime` |
| [`apps/fastify-api`](apps/fastify-api/README.md) | TypeScript API, JSON Schema, idempotency, graceful shutdown | `npm run test:api` |
| [`apps/next-learning`](apps/next-learning/README.md) | Next 16 App Router, RSC, Cache Components, Route Handler | `npm run build:next` |

### Chạy toàn bộ

```powershell
cd 07-nodejs-backend-nextjs-roadmap
npm install
npm test
npm run build
```

Máy hiện tại có Node.js 24 LTS phù hợp với track. Không commit `.env`, `.next`, coverage hoặc `node_modules`.

## Framework decision

| Lựa chọn | Phù hợp | Không nên chọn chỉ vì |
|---|---|---|
| Node core | Học runtime, service rất nhỏ/specialized | “Không dependency luôn nhanh hơn” |
| Fastify | API hiệu năng tốt, schema-first, plugin encapsulation | Benchmark hello-world |
| NestJS | Team enterprise cần conventions, module/DI/guard/interceptor | Nó trông giống Spring |
| Express | Ecosystem/legacy, service đơn giản | Thói quen tutorial cũ |
| Next.js Route Handlers/BFF | UI-owned BFF, webhook, server-side composition | Muốn nhét mọi domain backend vào frontend deployment |

Default architecture cho hệ thống lớn: Next.js làm web/BFF boundary, domain backend riêng khi cần independent scaling, messaging, long-running job hoặc nhiều client cùng dùng API.

## Definition of done

Sau track này, người học phải:

- giải thích event loop phases, microtask starvation và libuv pool;
- biết khi nào dùng stream, Worker Thread, process/replica;
- thiết kế validation, error contract, timeout, cancellation, idempotency;
- profile heap/event-loop lag và đóng service graceful;
- phân biệt Server Component, Client Component, Route Handler, Server Action;
- mô tả chính xác cache/freshness/authorization boundary trong Next.js;
- bảo vệ được quyết định Fastify, NestJS, Next BFF hay Spring service bằng SLO và team context.
