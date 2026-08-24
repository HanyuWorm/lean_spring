# Next.js 16 Learning App

Catalog nhỏ minh họa:

- App Router và file conventions;
- Server Components mặc định;
- Client island nhỏ cho interaction;
- async `searchParams`/`params`;
- Cache Components + `'use cache'` + `cacheLife`;
- dynamic route, `generateStaticParams`, `notFound`;
- Route Handler dùng chung DAL thay vì Server Component tự gọi HTTP nội bộ.

```powershell
npm run build --workspace @learning/next-learning
npm run dev --workspace @learning/next-learning
```

Mở `http://localhost:3000` và `http://localhost:3000/api/products?q=node`.

Data đang là immutable in-process fixture để build không cần database. Bước tiếp theo: thay DAL bằng API backend, thêm auth, Server Action mutation, cache invalidation và Playwright E2E.
