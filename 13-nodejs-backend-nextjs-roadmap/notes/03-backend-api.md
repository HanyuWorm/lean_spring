# 03 — Node.js Backend và API Design

## Framework landscape

### Fastify

Fastify dùng plugin/encapsulation, hook lifecycle và schema-based validation/serialization. Phù hợp service cần overhead thấp, contract rõ và team hiểu composition.

### NestJS

NestJS có module/provider/controller, DI, guard, pipe, interceptor và exception filter, gần mental model Spring. Nó giúp team lớn thống nhất convention nhưng abstraction/decorator không thay hiểu biết Node runtime. Scope request và reflection/dependency graph có cost.

### Express

Express tối giản, ecosystem lớn và nhiều legacy code. Application phải tự chọn conventions cho validation, async error, logging, DI và structure. Chọn vì context/ecosystem, không vì tutorial phổ biến.

## HTTP semantics

- GET safe/idempotent theo semantics; không mutation ẩn.
- PUT replace/idempotent; PATCH partial update theo contract.
- POST có thể được làm idempotent bằng idempotency key.
- 400 cho malformed/validation; 401 unauthenticated; 403 authenticated nhưng forbidden; 404 có thể che resource existence; 409 conflict; 422 valid syntax nhưng domain rejection; 429 rate limited.

Status code không đủ. Error contract ổn định nên có machine code, safe message, correlation ID và field violations; không trả stack/SQL/internal object.

## Boundary pipeline

```text
reverse proxy
 -> request size/rate limit
 -> authentication
 -> parse + schema validation
 -> authorization
 -> application use case
 -> repository/external ports
 -> response DTO + serialization schema
```

Thứ tự cụ thể phụ thuộc framework, nhưng expensive work không chạy trước cheap rejection nếu tránh được.

## Validation và serialization

JSON Schema có thể là runtime contract cho body/query/params/response. Fastify compile schema bằng Ajv/serializer. Schema là code tin cậy; không compile schema do user gửi. DB checks không nên nằm trong initial synchronous validation; đặt ở application/pre-handler phase với timeout.

Response schema vừa tránh accidental data leak vừa tối ưu serialization. Không trả raw ORM record/entity.

## API evolution

- additive fields thường dễ tương thích hơn rename/remove;
- consumer phải bỏ qua unknown fields;
- enum mở có rủi ro với generated exhaustive client;
- version khi semantic breaking change thực sự, không version mỗi sprint;
- contract test và deprecation telemetry trước khi remove.

## Pagination

Offset dễ dùng nhưng chậm/không ổn định ở offset lớn hoặc data thay đổi. Cursor/keyset cần stable unique ordering:

```text
ORDER BY created_at DESC, id DESC
cursor = encoded(created_at, id)
```

Cursor phải opaque, signed nếu chứa thông tin không muốn client sửa và bound page size.

## Idempotency

Cho payment/order POST:

1. client gửi key unique trong scope tenant/user + operation;
2. server lưu key, request fingerprint, state và response trong durable store;
3. cùng key/cùng payload trả kết quả cũ;
4. cùng key/khác payload trả conflict;
5. concurrent request phải được atomic qua unique constraint/transaction.

Redis TTL key một mình có thể mất sau restart/eviction; với money/order cần database truth hoặc design durable tương ứng.

## Modular architecture

```text
feature/order/
  domain/          # entity/value/policy, không biết Fastify
  application/     # use case + ports
  infrastructure/  # DB, Kafka, HTTP adapters
  transport/       # routes/schema/DTO
```

Không tạo `controllers/`, `services/`, `repositories/` toàn hệ thống nếu feature coupling cao. Package-by-feature giúp ownership và modular monolith.

## Dependency injection

JavaScript có thể dùng constructor/factory parameter mà không cần container:

```ts
export function createOrderService(repo: OrderRepository, clock: Clock) {
  return { place: async (command: PlaceOrder) => { /* ... */ } };
}
```

DI container hữu ích khi graph lớn/conventions cần thiết, nhưng string token, decorator metadata và circular dependency có trade-off. Ưu tiên explicit dependency và composition root.

## BFF và Next.js

Next Route Handlers/Server Functions phù hợp BFF gần UI: composition, session-aware call, form mutation, webhook nhỏ. Tách dedicated backend khi domain có nhiều client, independent deployment/scale, long-running job, heavy messaging hoặc team ownership riêng.

## Checklist API production

- Runtime validate mọi untrusted boundary và response lọc field?
- Có request/decompressed size, rate và concurrency limits?
- Timeout/cancellation truyền tới DB/downstream?
- Mutation quan trọng có idempotency và concurrency control?
- Pagination stable/bounded?
- Log structured có redaction và correlation?
- Health, readiness và graceful shutdown đúng semantics?
