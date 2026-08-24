# 07 — Testing và Production Delivery

## Test strategy

| Lớp | Mục đích | Ví dụ |
|---|---|---|
| Unit | policy/function thuần | pricing, state transition |
| Component | module/service với adapter fake hoặc real lightweight | Fastify `inject`, use case + repo |
| Integration | database/broker/cache thật | transaction, migration, serialization |
| Contract | producer/consumer compatibility | OpenAPI/event schema |
| E2E | critical user journey | browser → Next → API → DB |

Không mock framework/ORM internals quá mức. Test invariant và boundary. Database behavior như isolation, unique constraint và SQL phải test trên engine production, không chỉ in-memory substitute.

## Node built-in test runner

`node:test` phù hợp unit/component cơ bản và giảm dependency. Vitest/Jest cung cấp ecosystem/UI/mock phù hợp frontend hoặc team needs. Chọn một convention, tránh pha runner khiến config/transform khác nhau.

Fastify `app.inject()` test route không cần bind socket và vẫn đi lifecycle. Next.js cần unit/component cộng browser E2E (Playwright) cho navigation, hydration, action và accessibility critical.

## Determinism

Inject clock, ID generator, random và external port. Không sleep trong test; dùng controllable clock/event. Reset database per test transaction/schema/container theo isolation cần thiết.

Test timeout/cancellation, duplicate message, concurrent update, retry và shutdown—not chỉ happy path.

## Typecheck, lint và build

Pipeline tối thiểu:

```text
npm ci
 -> format/lint
 -> TypeScript typecheck
 -> unit/component tests
 -> integration/contract tests
 -> production build
 -> dependency/container scan
 -> deploy + smoke/canary
```

Next 16 không dùng `next lint` như trước; chạy ESLint/Biome riêng. `next build` vẫn bắt server/client boundary và route type errors quan trọng.

## Package/lockfile

- commit `package-lock.json`;
- CI dùng `npm ci` để install đúng graph;
- không chạy production với devDependencies không cần thiết;
- review major update/codemod và build artifact;
- khai báo Node `engines` và dùng version manager/container digest.

Monorepo workspace giảm duplicate config nhưng coupling CI/release cần chủ đích. Package boundary/exports phải rõ, không deep import internal.

## Container

Multi-stage build, copy production artifact/deps tối thiểu, chạy non-root, read-only filesystem nếu có thể, init/signal handling đúng. Không bake secret vào image/build arg/client bundle.

Next standalone output có thể giảm deployment files; xác minh static/public assets và runtime env semantics. Backend image không chứa compiler/source map public nếu policy cấm.

## Health và graceful shutdown

- liveness: process có sống/không deadlock nghiêm trọng;
- readiness: instance có sẵn sàng nhận traffic/dependency critical hợp lệ;
- startup: app có đủ thời gian warm/migrate theo deployment design.

SIGTERM flow:

```text
mark unready -> stop accepting -> drain in-flight with deadline
-> close consumer/server/pools -> flush essential telemetry -> exit
```

Migration không nên chạy đồng thời tùy tiện ở mọi replica. Dùng migration job/leader strategy và backward-compatible expand/contract rollout.

## Deployment

Canary/blue-green với SLO guard, version marker và rollback. Schema/event/API phải tương thích trong thời gian old/new versions cùng chạy. Retryable work cần idempotent để pod termination không nhân side effect.

## Checklist

- Test có cover race, retry, timeout, cancel và duplicate?
- Database/messaging integration dùng semantics thật?
- Lockfile/Node version/build reproducible?
- `npm ci`, typecheck, tests và production build chạy CI?
- Container non-root/minimal và secret không nằm trong layer?
- SIGTERM/drain/rollout/migration đã test?
