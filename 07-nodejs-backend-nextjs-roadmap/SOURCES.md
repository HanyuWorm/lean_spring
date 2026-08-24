# Official Sources

Baseline được kiểm tra ngày 2026-08-24. Hệ sinh thái JavaScript thay đổi nhanh; production phải pin version bằng lockfile và theo dõi security release.

## Node.js và TypeScript

- [Node.js release schedule](https://nodejs.org/en/about/previous-releases): chọn Active/Maintenance LTS cho production.
- [Node.js API documentation](https://nodejs.org/docs/latest-v24.x/api/): ESM, streams, workers, test runner, diagnostics và runtime APIs.
- [Don't block the event loop](https://nodejs.org/en/learn/asynchronous-work/dont-block-the-event-loop): event loop/worker pool và denial-of-service risk.
- [Node.js diagnostics](https://nodejs.org/en/learn/diagnostics): memory, flame graphs, profiling và reports.
- [Node.js Permission Model](https://nodejs.org/api/permissions.html): defense-in-depth cho filesystem/network/process/worker; không phải sandbox chống malicious code.
- [TypeScript Handbook](https://www.typescriptlang.org/docs/handbook/intro.html) và [strict option](https://www.typescriptlang.org/tsconfig/strict.html).

## Backend frameworks

- [Fastify Reference](https://fastify.dev/docs/latest/Reference/): lifecycle, plugins, hooks, validation, serialization và testing.
- [Fastify Validation and Serialization](https://fastify.dev/docs/latest/Reference/Validation-and-Serialization/): JSON Schema, Ajv và response serializer.
- [NestJS Documentation](https://docs.nestjs.com/): modules, providers, controller, guard, pipe, interceptor và microservices.

## Next.js

- [Next.js App Router](https://nextjs.org/docs/app): routing, Server/Client Components và project conventions.
- [Next.js 16 release](https://nextjs.org/blog/next-16): Cache Components, Turbopack, React 19.2 và migration notes.
- [Server and Client Components](https://nextjs.org/docs/app/getting-started/server-and-client-components).
- [Fetching Data](https://nextjs.org/docs/app/getting-started/fetching-data) và [Mutating Data](https://nextjs.org/docs/app/getting-started/mutating-data).
- [Cache Components](https://nextjs.org/docs/app/getting-started/cache-components) và [`use cache`](https://nextjs.org/docs/app/api-reference/directives/use-cache).
- [Route Handlers](https://nextjs.org/docs/app/getting-started/route-handlers).
- [Data Security](https://nextjs.org/docs/app/guides/data-security) và [Authentication](https://nextjs.org/docs/app/guides/authentication).
- [Self-hosting](https://nextjs.org/docs/app/guides/self-hosting).

### Cảnh báo bảo mật theo thời điểm

Ngày 2026-08-20, [Next.js Blog](https://nextjs.org/blog) thông báo sẽ phát hành bản vá ngày 2026-08-26 cho nhánh 16.3 và 15.5, gồm một lỗ hổng mức critical. Lab pin `16.3.2` vì đây là bản npm mới nhất ngày 2026-08-24, nhưng **không triển khai production sau ngày phát hành advisory nếu chưa nâng lên bản đã vá và chạy lại build/test**.

## Version policy

- Node Current không mặc định là production target; theo Node release table.
- Chạy `npm audit`, nhưng không dùng auto-fix major update mà không review/build/test.
- Pin lockfile, dùng automated dependency PR và đọc framework security advisory.
- Next.js có lịch security release riêng; kiểm tra advisory mới hơn ngày baseline trước deployment.
