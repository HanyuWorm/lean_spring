# 08 — Next.js App Router Foundations

## Next.js là gì?

Next.js là React framework hỗ trợ routing, server rendering, static/prerendering, Server Components, data/cache integration, Route Handlers, build và deployment conventions. Nó không tự động thay domain backend cho mọi hệ thống.

Next.js 16 dùng App Router làm hướng chính, Turbopack mặc định và đổi `middleware.ts` sang `proxy.ts` cho network boundary Node runtime. Đọc migration guide khi nâng major.

## File conventions

```text
app/
  layout.tsx       # shared UI, giữ state qua navigation
  page.tsx         # route UI
  loading.tsx      # Suspense fallback
  error.tsx        # client error boundary
  not-found.tsx
  route.ts         # HTTP Route Handler, không đặt cùng page cùng segment
  products/[id]/page.tsx
```

Route groups `(marketing)` tổ chức mà không đổi URL; private folders `_components` không thành route. Parallel/intercepting routes dùng cho advanced UI, không nên là điểm học đầu.

## Server Component mặc định

Page/layout mặc định là React Server Component:

- chạy/render trên server;
- có thể đọc DB/API/server secret qua DAL;
- không ship component JavaScript đó cho browser;
- không dùng browser API, event handler, state/effect.

Server Component có thể truyền serializable props tối thiểu xuống Client Component. Dữ liệu đã truyền có thể tới browser; không truyền entity/user object rộng rồi hy vọng secret được ẩn.

## Client Component

File có `'use client'` tạo client boundary. Dùng khi cần state, effect, event handler, browser API hoặc client-only library. Import descendants có thể kéo thêm code vào client graph, nên đặt boundary sâu và nhỏ.

Anti-pattern: đặt `'use client'` ở root layout vì một button, khiến quá nhiều component/data/dependency thành client bundle.

## Composition

Server Component có thể render Client Component và truyền Server-rendered children. Client file không được import arbitrary server-only module. Dùng composition để interactive shell nhỏ bao quanh server content.

## Dynamic segments và params

`[id]`, `[...slug]`, `[[...slug]]` lấy từ URL và luôn là untrusted input. Next 16 sử dụng async request APIs/params theo conventions hiện hành; `await params`/`searchParams` trong page tương ứng.

Validate format, authorization và existence. `notFound()` cho missing resource; đừng phân biệt unauthorized resource nếu threat model cần che tồn tại.

## Loading, Suspense và streaming

`loading.tsx` tạo loading UI theo segment. Suspense boundary cho phép shell/fast section stream trước slow section. Tránh sequential waterfall bằng đưa fetch gần component và bắt đầu independent work song song.

Streaming cải thiện perceived latency nhưng không sửa backend chậm. Boundary quá nhiều gây UX nhấp nháy và complexity.

## Error handling

`error.tsx` là Client Component bắt render error trong segment con và có retry/reset. `global-error.tsx` xử lý root. Expected domain error nên model/return rõ thay vì throw mọi thứ; unexpected error log server-side với digest/correlation và response không lộ detail.

## Metadata và assets

Dùng Metadata API/static/dynamic metadata, `next/image`, font optimization và semantic HTML. Dynamic metadata fetch cần tránh waterfall/duplicate data; React/Next memoization/cache semantics phải hiểu theo version.

## Navigation

`<Link>` hỗ trợ client navigation/prefetch. App Router giữ layout và có thể dùng cached/prefetched route data. Không đặt side effect vào render vì component có thể render lại/prerender. Mutation thuộc action/handler.

## Checklist

- Component này thực sự cần `'use client'`?
- Props sang client có minimal và không chứa secret?
- Params/search params được validate/authorize?
- Loading/error/not-found boundary đúng segment?
- Fetch độc lập có chạy song song, slow work có Suspense?
- Client bundle và third-party dependency được đo?
