# 09 — Next.js Data, Cache, Mutation và Security

## Chọn data access approach

Tài liệu Next.js nêu ba hướng chính:

- gọi external HTTP API: hợp hệ thống lớn/đã có backend;
- Data Access Layer (DAL): phù hợp app mới cần authz/DTO tập trung;
- component-level access: nhanh cho prototype/learning nhưng governance khó hơn.

Với tổ chức có Spring/Node backend riêng, Server Component gọi API theo Zero Trust: backend vẫn xác thực/authorize, Next không phải trusted bypass mặc định.

## Fetching trong Server Component

Server Component có thể `await` DB/API trực tiếp qua DAL. Fetch data ở component cần nó, nhưng tránh waterfall:

```ts
const userPromise = getUser(id);
const ordersPromise = getOrders(id);
const [user, orders] = await Promise.all([userPromise, ordersPromise]);
```

Không fetch Route Handler của chính Next app từ Server Component chỉ để reuse; gọi shared DAL/function trực tiếp, tránh HTTP hop và build/self-origin vấn đề.

## Cache không phải một khái niệm duy nhất

Phân biệt:

- request/render memoization/deduplication;
- data/function cache qua `use cache`;
- prerendered route/static shell;
- browser/router cache và prefetch;
- CDN/reverse-proxy HTTP cache;
- application Redis/database cache.

Mỗi layer có key, lifetime, invalidation, scope và multi-instance semantics khác. Luôn viết cache matrix thay vì nói “Next cache rồi”.

## Cache Components và `use cache`

Next 16 cho phép opt-in Cache Components. `'use cache'` đánh dấu async function/component/file cacheable; input và closed-over values tham gia key theo rules. Dữ liệu runtime như cookies/headers không đọc trực tiếp trong cached scope; đọc ngoài và truyền giá trị đã chuẩn hóa nếu việc cache personalized data thực sự an toàn.

`cacheLife` mô tả freshness/lifetime profile; tag hỗ trợ revalidation. `revalidateTag`/`updateTag` có semantics khác nhau theo version và read-your-writes need—đọc docs đúng patch.

### Cache personalized data

Không cache output theo URL chung nếu chứa user/tenant data. Key phải bao gồm identity/authorization dimension cần thiết, nhưng cache dữ liệu nhạy cảm per-user có cardinality/privacy cost. Nhiều trường hợp không cache tốt hơn.

## Dynamic request data

Cookies, headers và request-dependent values làm work động theo rendering model. Đừng vô tình biến toàn route dynamic khi chỉ một leaf cần data; cô lập leaf trong Suspense/dynamic boundary theo Cache Components model.

## Server Functions/Actions

Server Function là async function chạy server; khi dùng làm mutation/action, browser gọi bằng POST. Nó là public mutation surface về mặt security:

```text
receive FormData/arguments
 -> authenticate
 -> validate
 -> authorize resource/action
 -> execute transaction/idempotency
 -> invalidate/update cache
 -> return minimal result/redirect
```

Không tin hidden input, client state hoặc việc button chỉ hiện cho admin. Argument có thể được crafted.

## Route Handlers

`route.ts` dùng Web `Request`/`Response` và hỗ trợ HTTP methods. Route Handler phù hợp webhook, public API/BFF endpoints, health hoặc non-React client. GET không mặc định cache theo cách cũ; caching phải opt-in/thiết kế rõ theo Next 16 model.

Server Action phù hợp UI mutation và progressive enhancement; Route Handler phù hợp explicit HTTP contract/third-party/client khác. Domain service lớn có thể vẫn nằm ngoài Next deployment.

## DAL và DTO

DAL nên:

- chỉ chạy server (`server-only`);
- verify session/authorization gần query;
- nhận primitive/validated identifier;
- trả DTO minimal, không raw DB row/entity;
- cache only after authorization/key analysis;
- log audit cho action nhạy cảm.

Taint APIs là defense-in-depth theo trạng thái/version, không thay DTO filtering.

## Mutation UX

Form + action hỗ trợ progressive enhancement. Client Component có thể dùng pending state và optimistic update. Optimistic UI phải reconcile server rejection/conflict, không tự coi mutation thành công. Double-submit vẫn cần idempotency/constraint server-side.

## Checklist cache/security

- Đã vẽ tất cả cache layers và source of truth?
- Freshness/SWR/read-your-writes requirement rõ?
- Cache key có tenant/user/locale/permission dimension đúng?
- Mỗi Action/Handler validate và authorize lại?
- DAL/secret module có `server-only`, DTO tối thiểu?
- Mutation thành công mới revalidate; failure không làm UI nói dối?
- Multi-instance self-hosting có shared cache/tag invalidation strategy?
