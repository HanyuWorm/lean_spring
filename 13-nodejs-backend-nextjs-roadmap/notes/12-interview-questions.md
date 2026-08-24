# 12 — 60 câu hỏi Node.js Backend và Next.js

Trả lời theo khung: **mechanism → impact → trade-off → evidence/mitigation**. Senior không chỉ đọc API name.

## A. JavaScript/TypeScript cơ bản

### 1. `var`, `let`, `const` khác nhau thế nào?

`let`/`const` block-scoped và có temporal dead zone; `var` function-scoped, hoisted và cho redeclare. `const` cấm gán lại binding, không làm object deep immutable.

### 2. Closure là gì và có rủi ro gì ở backend?

Function giữ lexical environment nơi nó được tạo. Nó hữu ích cho factory/middleware nhưng callback/queue sống lâu có thể giữ request, Buffer hoặc graph lớn ngoài ý muốn.

### 3. `==` và `===`?

`==` thực hiện coercion với edge cases; `===` so sánh không coercion và là default. Có intentional case hiếm như `x == null`, nhưng codebase cần convention rõ.

### 4. `null` và `undefined` khác gì?

`undefined` thường là thiếu/uninitialized; `null` thường biểu đạt intentionally empty. Với TypeScript strict, model cả hai explicit và normalize boundary để tránh ba trạng thái không cần thiết.

### 5. Vì sao ưu tiên `unknown` hơn `any`?

`any` tắt type checking và lan truyền. `unknown` buộc code narrow/validate trước khi dùng, phù hợp error và external input.

### 6. TypeScript interface có validate request JSON không?

Không; type bị erase runtime. Request phải qua JSON Schema/Zod/Ajv/class-validator hoặc parser tương đương, rồi mới coi là typed DTO.

## B. Async và event loop

### 7. `async/await` có tạo thread không?

Không. Nó là syntax quanh Promise/continuation; asynchronous I/O do runtime/OS/libuv hỗ trợ. CPU JavaScript vẫn chạy trên isolate thread trừ khi chuyển Worker.

### 8. Microtask là gì?

Promise continuation/`queueMicrotask` vào microtask queue và được drain tại các checkpoint trước khi event loop tiếp tục phase khác. Recursive microtask có thể starve timers/I/O.

### 9. `process.nextTick` khác `setImmediate`?

`nextTick` chạy trước khi event loop tiếp tục và lạm dụng có thể starvation; `setImmediate` chạy ở check phase sau poll. Thứ tự với timer phụ thuộc context, không nên xây correctness trên race timing.

### 10. Vì sao `forEach(async ...)` thường sai?

`forEach` không await Promise từ callback; outer flow kết thúc sớm và error dễ unhandled. Dùng `for...of` tuần tự hoặc `Promise.all(items.map(...))` với concurrency limit.

### 11. `Promise.all` có cancel Promise khác khi một cái reject?

Không; returned Promise reject sớm nhưng operations còn chạy. Dùng `AbortController`/signal được các operation hỗ trợ và cleanup rõ.

### 12. Timeout bằng `Promise.race` đã đủ chưa?

Chưa. Nó chỉ bỏ chờ result; loser có thể tiếp tục query/fetch và giữ resource. Cần cancellation/deadline truyền xuống driver/client.

## C. Runtime, stream và scale

### 13. Khi nào Node block event loop?

Synchronous I/O, CPU loop, JSON/regex/compression lớn hoặc callback/microtask dài. Hậu quả là tất cả request trên isolate tăng latency; đo event-loop delay/utilization và CPU profile.

### 14. Worker Threads dùng khi nào?

JavaScript CPU-bound có thể partition/parallelize. I/O thường không lợi vì Node async I/O hiệu quả hơn; dùng worker pool bounded thay worker-per-request.

### 15. libuv worker pool làm gì?

Thực thi một số filesystem, DNS, crypto/zlib operations. Pool saturation tạo head-of-line blocking giữa các API dùng chung; tăng size chỉ sau profile/capacity test.

### 16. Backpressure trong stream là gì?

Consumer báo không theo kịp để producer dừng/giảm. Tôn trọng `write()` false/`drain` hoặc dùng `pipeline`; vẫn phải bound số stream concurrent và expansion.

### 17. Buffer và V8 heap có quan hệ gì?

Buffer wrapper là object JavaScript, backing memory thường được tính external/off-heap theo V8/Node accounting. RSS có thể tăng dù heap chart ổn; slice có thể giữ backing allocation lớn.

### 18. Một process hay nhiều process/container?

Nhiều replica cho CPU parallelism, failure/resource isolation và rollout; Worker cho CPU task trong process. Trên orchestrator thường một main process/container dễ budget, rồi scale ngang.

## D. Backend/API

### 19. Fastify khác Express ở điểm cốt lõi nào?

Fastify có plugin encapsulation, lifecycle hooks và schema-compiled validation/serialization là first-class. Express tối giản/middleware ecosystem, application phải tự chuẩn hóa nhiều concern.

### 20. NestJS giống Spring ở đâu và khác gì?

Cùng module/DI/decorator/controller/guard-interceptor concepts. Nhưng Nest chạy trên Node event loop, TS structural/erased types, provider scope/lifecycle và underlying HTTP adapter khác; không copy tuning/thread mental model.

### 21. Tại sao response schema quan trọng?

Nó lọc field chống accidental leak, giữ contract và có thể giúp serializer nhanh. Không trả raw ORM row/entity chứa internal/secret fields.

### 22. Thiết kế error response thế nào?

Stable machine code, safe message, correlation ID và bounded field violations; HTTP status đúng semantics. Stack/SQL/internal detail chỉ ở redacted server logs/traces.

### 23. Offset hay cursor pagination?

Offset đơn giản nhưng large offset chậm và data thay đổi gây duplicate/missing. Cursor/keyset dựa stable unique ordering tốt hơn scale, nhưng contract/key encode phức tạp hơn.

### 24. Idempotency key cần lưu gì?

Key scoped theo caller/operation, request fingerprint, processing state và durable response/outcome. Unique/atomic claim xử lý concurrent duplicate; cùng key khác payload trả conflict.

## E. Data/distributed systems

### 25. Tại sao DB pool không bằng số request concurrent?

Pool là DB concurrency budget. Event loop giữ nhiều request chờ rẻ hơn thread nhưng Promise/context vẫn tốn memory/latency; pool toàn replicas phải phù hợp database capacity.

### 26. Transaction nên bắt đầu ở đâu?

Application use-case boundary nơi invariant cần atomic, không trong từng repository method độc lập. Tránh giữ transaction qua remote network call.

### 27. N+1 trong Node ORM là gì?

Một query lấy roots rồi mỗi root tạo thêm query association. Detect bằng query count/trace; sửa join/batch/data loader/projection theo cardinality, không blindly eager mọi graph.

### 28. Transactional Outbox giải quyết gì?

Atomic local write domain state + outbox row, tránh DB/broker dual-write window. Relay vẫn có thể duplicate nên consumer cần idempotent; ordering/schema/cleanup vẫn phải thiết kế.

### 29. Kafka ordering bảo đảm tới đâu?

Trong một partition. Dùng message key theo entity cần order, nhưng tránh hot key/partition và xử lý retry không làm reorder ngoài contract.

### 30. Saga compensation có phải rollback không?

Không; là transaction nghiệp vụ mới để bù hiệu ứng đã commit và có thể fail. Saga state/timeout/idempotency/compensation policy phải durable.

## F. Security

### 31. CORS có bảo vệ API khỏi attacker không?

Chỉ hạn chế browser đọc/gửi một số cross-origin requests; curl/server không bị chặn. Nó không thay authentication/authorization/CSRF protection.

### 32. Khi nào cần CSRF?

Khi browser tự gắn credential như cookie cho cross-site request. Dùng SameSite, CSRF token/origin checks theo flow; bearer token trong explicit header có threat model khác nhưng vẫn có XSS risk.

### 33. JWT được ký có giữ bí mật payload không?

Không. Signature bảo vệ integrity/authenticity, payload thường chỉ base64url. Validate algorithm/issuer/audience/time/key và không nhét secret/PII không cần thiết.

### 34. SSRF trong Node/Next xảy ra thế nào?

Server fetch URL do user kiểm soát rồi truy cập internal/metadata service. Cần host/scheme/port allowlist, DNS/IP/network controls, redirect/timeout/response limits.

### 35. Node Permission Model có phải sandbox không?

Không; official model là defense-in-depth/seat belt cho trusted code và có constraints/bypass đối với malicious code. Kết hợp OS/container least privilege.

### 36. `NEXT_PUBLIC_*` có ý nghĩa gì?

Biến được expose/inline cho client bundle, nên tuyệt đối không chứa secret. Server-only env vẫn có thể leak nếu truyền value vào Client Component/response/log.

## G. Observability và production

### 37. Metric quan trọng riêng của Node là gì?

Event-loop delay/utilization cùng CPU, heap/RSS/external memory, active handles, request latency và downstream pool. CPU tổng thấp không loại trừ main isolate nghẽn một core.

### 38. Heap ổn nhưng RSS tăng, nghĩ gì?

Buffer/external memory, native addon, threads, allocator fragmentation hoặc mapped memory. Đo `process.memoryUsage`, profile/native metrics và owner của buffers.

### 39. AsyncLocalStorage dùng làm gì?

Propagate request context như trace/correlation/tenant qua async chain. Store minimal immutable identifiers; không thay explicit auth/domain parameters và phải hiểu lifecycle.

### 40. Metric cardinality explosion là gì?

Label user/order/raw URL tạo vô số time series, tăng memory/cost và phá monitoring. Dùng route template, bounded error code/status; ID nằm ở trace/log có sampling.

### 41. Graceful shutdown đúng flow?

Mark unready/stop accept, drain in-flight với deadline, close consumers/server/pools, flush telemetry tối thiểu rồi exit. Test với SIGTERM và idempotent retry.

### 42. `uncaughtException` rồi tiếp tục chạy có an toàn?

Thường không; state/invariant có thể hỏng. Log/record tối thiểu, shutdown/restart theo policy; fix root cause và đừng dùng handler để nuốt lỗi.

## H. Next.js foundations

### 43. Server Component và Client Component khác gì?

Server Component mặc định chạy/render server, không có state/effect/browser API và không ship component JS. Client Component có `'use client'`, cần cho interaction/browser và làm client boundary tăng bundle.

### 44. Có thể truyền gì từ Server sang Client Component?

Serializable minimal props theo React/Next constraints. Mọi data truyền có thể tới browser, nên dùng DTO và không truyền secret/entity rộng.

### 45. `loading.tsx` và Suspense giúp gì?

Hiển thị fallback và cho route/component stream phần nhanh trước phần chậm. Nó cải thiện perceived latency, không sửa slow query; boundary phải có UX hợp lý.

### 46. Route Handler dùng khi nào?

Explicit HTTP endpoint/webhook/BFF/public API/non-React client. Dùng Web Request/Response và method semantics; validate/auth/rate/timeout như backend bình thường.

### 47. Server Action dùng khi nào?

Mutation gắn UI/form, tích hợp pending/revalidation/updated UI. Nó vẫn là POST-callable server endpoint nên validate/authenticate/authorize và idempotency.

### 48. Có nên gọi internal Route Handler từ Server Component?

Thường không; gọi shared DAL/function trực tiếp để tránh HTTP hop, self-origin/build issue và duplicate auth. Gọi external backend API khi đó là real boundary.

## I. Next data/cache/security

### 49. `use cache` làm gì?

Trong Cache Components, đánh dấu async function/component/file cacheable; input/closed values tham gia key theo rules. Phải cấu hình lifetime/tag và không đọc runtime secret/cookie sai scope.

### 50. Tại sao nói “Next cache data” chưa đủ?

Có function/data cache, prerendered output, client router/prefetch, CDN và application cache. Mỗi layer có key/lifetime/invalidation/multi-instance semantics riêng.

### 51. Cache personalized page nguy hiểm gì?

Sai key/scope có thể trả user A data cho B. Key theo identity/tenant/permission hoặc không shared-cache; cân nhắc cardinality/privacy và authorize trước data access.

### 52. `proxy.ts` có thay authorization trong DAL không?

Không. Nó tốt cho optimistic redirect/network routing; request có thể đi đường khác và session có thể thay đổi. Secure check gần data/action/handler vẫn bắt buộc.

### 53. `revalidateTag` và `updateTag` chọn thế nào?

Theo semantics version hiện hành: SWR/stale acceptance so với read-your-writes expiration. Không học signature từ blog cũ; mutation thành công mới invalidate đúng tag/scope.

### 54. Vì sao đặt `'use client'` ở root là anti-pattern?

Nó kéo component/dependency descendants vào client graph, tăng JS/hydration và dễ đưa data rộng tới browser. Đặt client islands sâu nhất có thể.

## J. Architect scenarios

### 55. Node service latency tăng nhưng CPU pod chỉ 30%. Điều tra gì?

Một core event loop có thể nghẽn, hoặc requests chờ DB/network/pool. Xem event-loop lag/utilization, per-core CPU, profiles, pool pending, downstream spans và queue—not scale CPU mù.

### 56. API OOM khi downstream chậm dù không leak bình thường?

In-flight Promise/request bodies/retry/queue tăng vì thiếu backpressure/concurrency limit. Đặt deadline/cancel, bulkhead/semaphore, bounded queue/load shedding và test slow dependency.

### 57. Khi nào tách Next.js BFF thành backend riêng?

Khi domain phục vụ nhiều clients, cần independent scaling/deployment, messaging/long jobs, complex transaction/compliance hoặc ownership riêng. BFF giữ UI composition/session-specific concerns.

### 58. Chọn NestJS hay Fastify cho team Spring?

Nest giúp convention/DI/onboarding, Fastify cho composition/schema/lifecycle nhẹ và visibility. Quyết định theo team size, platform template, complexity, performance profile và operability—not mức giống Spring.

### 59. Node hay Spring cho order/payment core?

Cả hai làm được. Đánh giá team/domain ecosystem, transaction/integration, SLO, operational maturity và reuse; reliability đến từ invariant/idempotency/outbox/observability, không từ ngôn ngữ tự động.

### 60. Production readiness review gồm gì?

Runtime/framework patch policy; contract/validation/security; DB/concurrency/memory budgets; timeout/cancel/retry/idempotency; tests/build/SBOM; logs/metrics/traces/SLO; graceful rollout/rollback; runbook/ownership và proven load/failure tests.

## Rubric

- **Foundation:** hiểu syntax, Promise, event loop, RSC.
- **Mid:** build API/Next route có validation/test/cache cơ bản.
- **Senior:** phân tích bounds, failure modes, profiling và security boundary.
- **Architect:** bảo vệ platform split, SLO/cost/governance và migration bằng evidence.
