# Fastify TypeScript API

API task nhỏ nhưng có các concern production quan trọng:

- composition root và dependency injection không cần container;
- Fastify JSON Schema validate request + serialize response;
- domain/application logic không phụ thuộc HTTP;
- idempotency key với fingerprint/conflict semantics;
- `app.inject()` component test;
- graceful SIGTERM/SIGINT.

```powershell
npm test --workspace @learning/fastify-api
npm run dev --workspace @learning/fastify-api
```

Endpoints:

```text
GET  /health
GET  /tasks
POST /tasks   Header: idempotency-key
```

Repository và idempotency store đang in-memory để lab chạy không cần hạ tầng. Production phải dùng database/durable store, unique constraint và transaction để nhiều replica xử lý duplicate đúng.
