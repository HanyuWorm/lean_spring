# 10 — Next.js Production Engineering

## Rendering decision

| Need | Hướng |
|---|---|
| Nội dung ổn định/public | prerender/cache, CDN |
| Fresh theo interval/event | cache lifetime/tag revalidation |
| Personalized/request-time | dynamic render, không shared-cache sai scope |
| Interactive browser state | Client Component nhỏ |
| Mutation gắn UI | Server Action hoặc Route Handler theo contract |

Không chọn CSR toàn bộ để tránh hiểu server rendering. Nó tăng bundle, waterfall và mất lợi thế server data access. Cũng không server-render mọi interaction; browser state thuộc client.

## Performance budget

- JavaScript shipped/hydrated theo route;
- image/font size và LCP resource priority;
- server render/TTFB và downstream latency;
- Suspense/streaming timing;
- cache hit/freshness;
- Core Web Vitals: LCP, INP, CLS;
- navigation/prefetch behavior.

Đo field data/RUM cùng lab data. Lighthouse một lần không đại diện user/device/network production.

## Bundle discipline

Đặt client boundary sâu; dynamic import heavy client widget; tránh barrel import kéo cả library; dùng bundle analyzer. Server-only dependency không nên vào client graph. Kiểm tra locale/icon/chart/editor packages.

React Compiler/memoization không sửa architecture hoặc network waterfall. Không thêm `useMemo`/`memo` theo thói quen trước profile.

## Image, font và metadata

`next/image` cần remote pattern allowlist chặt và image optimization capacity. Self-host nhiều replica cần cache/storage strategy. Dùng optimized font loading và Metadata API/robots/sitemap/canonical/structured data theo SEO use case.

Image optimizer có thể thành SSRF/resource abuse surface nếu remote policy rộng; reverse proxy/rate/resource limits vẫn cần.

## Self-hosting

Đặt reverse proxy trước Next server để xử lý malformed/slow request, body limit, rate limit, TLS và routing. Không expose trực tiếp nếu production architecture cần các controls này.

Multi-instance cần xem:

- cache/tag invalidation có shared/coherent không;
- build ID/deployment version có đồng nhất;
- session/secret/action encryption key rotation;
- static asset/CDN routing;
- graceful shutdown và streaming connection;
- image optimization cache.

Standalone output giúp image nhỏ hơn nhưng cần copy đúng `.next/static` và `public` theo deployment model.

## Environment variables

Phân biệt build-time và runtime env. `NEXT_PUBLIC_*` được inline/expose client. Image được build một lần rồi promote môi trường cần tránh bake environment-specific secret/public URL sai. Validate config startup/build theo nơi nó được dùng.

## Security headers

CSP, HSTS, frame ancestors, referrer policy, permissions policy và secure cookie theo threat model. CSP với nonce/hash có tương tác dynamic rendering/caching; test thực tế, không copy header cứng làm app hỏng hoặc `unsafe-inline` vô nghĩa.

## Observability

- server request/render/action/route latency và error;
- downstream fetch/DB spans;
- cache hit/miss/revalidation;
- route navigation/Core Web Vitals client-side;
- build version và deployment marker;
- memory/RSS/event-loop của Node process.

Error digest cần map được tới server log/trace nhưng không lộ stack cho user. Client monitoring phải scrub PII.

## Failure modes

- backend slow → render waterfall/timeout;
- cache stampede sau deploy/invalidation;
- one slow component giữ navigation nếu thiếu boundary;
- action double-submit/timeout nhưng commit thành công;
- old/new build assets mismatch;
- memory tăng do server cache/cardinality hoặc Client Component bundle dev tooling;
- rolling deployment cắt stream/action.

Test chaos và retry/idempotency thay vì chỉ happy-path `next dev`.

## Production checklist

- `next build` và `next start` production-like đã chạy?
- Node/Next patch hiện hành, security advisory đã review?
- Reverse proxy, body/rate/concurrency/timeouts?
- Cache matrix và multi-instance invalidation?
- Client JS/Core Web Vitals budgets?
- Authz tại DAL/action/handler và secrets không vào client?
- Graceful shutdown, canary/rollback, build compatibility?
