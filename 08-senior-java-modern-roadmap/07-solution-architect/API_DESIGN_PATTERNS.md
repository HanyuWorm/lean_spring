# API Design Patterns

Mục tiêu của API architecture là tạo contract dễ hiểu, tiến hóa được, an toàn và có failure semantics rõ ràng. “RESTful” không tự bảo đảm các thuộc tính này.

## 1. Chọn interaction style

| Style | Dùng khi | Tránh hoặc cân nhắc khi |
|---|---|---|
| REST/HTTP resource API | Public/domain API, CRUD và workflow có resource rõ | RPC command phức tạp bị ép thành CRUD giả |
| RPC/gRPC | Internal low-latency, typed contract, streaming | Browser/public ecosystem, proxy/tooling không hỗ trợ tốt |
| GraphQL | Client cần projection linh hoạt, nhiều view khác nhau | Write workflow phức tạp, cache/authorization/query cost chưa kiểm soát |
| Async event/message API | Loose coupling, buffering, eventual workflow | Caller cần kết quả tức thời hoặc business không chấp nhận eventual consistency |
| Webhook | Provider cần callback sang consumer qua HTTP | Consumer không có endpoint ổn định hoặc thiếu retry/signature/dedup |

Một hệ thống có thể dùng nhiều style, nhưng mỗi boundary phải có lý do và ownership rõ ràng.

## 2. Resource-oriented API

### Collection/Item Resource

```http
POST   /orders
GET    /orders/{orderId}
GET    /orders?customerId=...&status=...
PATCH  /orders/{orderId}
DELETE /orders/{orderId}
```

- URI mô tả resource; method/status/header mô tả semantics.
- `POST` create trả `201 Created` và `Location` khi resource đã được tạo.
- `202 Accepted` chỉ dùng khi xử lý chưa hoàn thành và phải cung cấp cách theo dõi trạng thái.
- Không trả `200` cho mọi tình huống rồi nhét lỗi vào body.

### Sub-resource

`/orders/{id}/items` phù hợp khi lifecycle/authorization gắn mạnh với order. Tránh nesting sâu vì nó làm URI phụ thuộc cấu trúc nội bộ và khó định danh resource độc lập.

### Action/Command Resource

Với transition nghiệp vụ không phải CRUD, command endpoint rõ ràng tốt hơn cập nhật status tùy ý:

```http
POST /orders/{id}/cancellations
POST /payments/{id}/refunds
```

`cancellation`/`refund` trở thành resource có ID, state, audit và idempotency. Dạng `/orders/{id}:cancel` có thể phù hợp với API convention nhất quán nhưng cần document rõ RPC semantics.

## 3. Idempotent Receiver

Dùng cho create/payment/command có thể bị retry:

```text
client sends Idempotency-Key
    -> server atomically reserves key + request fingerprint
       -> same key + same request: replay previous outcome
       -> same key + different request: reject conflict
```

Quyết định cần ghi rõ:

- scope theo tenant/client/operation;
- TTL và hành vi sau expiry;
- lưu cả success hay deterministic failure;
- xử lý concurrent request cùng key;
- transaction giữa idempotency record và business mutation;
- response replay có giữ nguyên status/header/body hay không.

Không nhầm idempotent HTTP method với exactly-once business execution.

## 4. Optimistic Concurrency bằng Conditional Request

Server trả `ETag`; client cập nhật với `If-Match`. Nếu representation đã đổi, trả `412 Precondition Failed` thay vì silently lost update.

Pattern này tốt cho API editing/concurrent updates. Với invariant nhiều aggregate hoặc contention cao, vẫn cần transaction/locking/domain rule ở database.

## 5. Pagination, filtering và projection

### Offset pagination

Dễ dùng, hỗ trợ nhảy trang; kém ổn định và đắt khi offset lớn hoặc dataset thay đổi liên tục.

### Cursor/keyset pagination

Tốt cho feed lớn và dữ liệu thay đổi. Cursor phải opaque, có stable deterministic sort và tie-breaker. Contract cần nói cursor expiry và consistency expectation.

### Filter/sort/field selection

- whitelist field/operator để tránh biến API thành query engine không kiểm soát;
- giới hạn page size và query complexity;
- sparse fieldsets/projection giúp giảm payload nhưng tăng cache/authorization/test matrix;
- không lộ trực tiếp tên column hoặc JPA property thành public contract.

## 6. Long-running Operation

```http
POST /exports
202 Accepted
Location: /operations/op-123

GET /operations/op-123
200 { "status": "RUNNING", "progress": 60 }
```

Operation resource cần state machine, cancellation policy, result expiry, retry semantics và authorization. Với callback/webhook, vẫn nên cho phép polling/reconciliation.

## 7. Batch/Bulk API

Batch giảm round-trip nhưng tạo vấn đề partial success, giới hạn payload và transaction scope. Contract phải chọn một trong:

- atomic all-or-nothing;
- per-item result có correlation ID;
- accepted async job;
- fail-fast hoặc collect-all-errors.

Không giữ một database transaction dài chỉ để đáp ứng batch rất lớn.

## 8. Error Contract bằng Problem Details

Dùng `application/problem+json` theo RFC 9457 và mở rộng có kiểm soát:

```json
{
  "type": "https://api.example.com/problems/order-state-conflict",
  "title": "Order state conflict",
  "status": 409,
  "detail": "Only CONFIRMED orders can be shipped",
  "instance": "/orders/o-123",
  "errorCode": "ORDER_STATE_CONFLICT",
  "traceId": "..."
}
```

- `type` và `errorCode` là machine-readable/stable;
- `detail` không dùng để client parse logic;
- validation errors có field/path/code nhưng không lộ implementation hoặc PII;
- `traceId` hỗ trợ support, không thay thế domain error code;
- phân biệt `400`, `401`, `403`, `404`, `409`, `412`, `422`, `429`, `500`, `503` theo semantics.

## 9. Compatibility và versioning

Ưu tiên compatible evolution:

- thêm optional field, không đổi meaning của field cũ;
- consumer phải tolerant với field mới nhưng producer không được tùy tiện đổi enum semantics;
- schema diff và contract test trong CI;
- deprecation có telemetry usage, communication, sunset date và migration guide;
- expand-and-contract cho database/event/API xuyên nhiều deployment.

Version trong path/header/media type đều có trade-off. Chỉ tạo major version khi có breaking semantic change thật sự; version không thay thế lifecycle governance.

## 10. API Composition Patterns

### API Gateway

Cross-cutting policy: routing, TLS, authentication, coarse rate limiting, request size và observability. Không đặt toàn bộ business orchestration vào gateway.

### Backend for Frontend

Mỗi experience/client family có API composition phù hợp. BFF tránh one-size-fits-all nhưng tăng số contract và nguy cơ duplicate logic.

### Aggregator

Fan-out nhiều service rồi kết hợp response. Bắt buộc có deadline budget, parallelism limit, partial-result policy và tránh N+1 network call.

### Anti-Corruption Layer

Chuyển model/protocol của legacy/external system sang model nội bộ. Đây là nơi mapping semantic và error taxonomy, không chỉ copy DTO.

## 11. Event API Patterns

| Pattern | Payload | Coupling |
|---|---|---|
| Event notification | ID + sự kiện tối thiểu; consumer gọi lại | Payload nhỏ, temporal coupling cao hơn |
| Event-carried state transfer | Dữ liệu consumer cần để cập nhật local view | Duplicate data, giảm synchronous dependency |
| Command message | Yêu cầu một owner thực hiện action | Một logical handler, cần reply/failure model |
| Competing consumers | Nhiều instance chia workload | Cần partition/order/idempotency rõ |

Event contract phải có event ID, type, occurred time, producer, schema version, correlation/causation, partition key và data classification. Không publish entity JPA hoặc internal DTO trực tiếp.

## 12. Security and tenancy patterns

- OAuth2/OIDC scopes không thay thế domain authorization.
- Resource server kiểm tra issuer, audience, expiry và key rotation.
- Object-level authorization chạy trên mọi item/sub-resource; không tin ID do client gửi.
- Tenant context phải được derive từ trusted identity và enforce ở application/data layer.
- Rate limit theo principal/tenant/cost; GraphQL/search/batch cần cost-based quota.
- Webhook có signature, timestamp/replay window, retry/backoff và receiver idempotency.
- API log/traces không chứa token, secret hoặc sensitive payload.

## 13. Spring Boot mapping

| Pattern | Spring building block |
|---|---|
| Problem Details | `ProblemDetail`, `ErrorResponse`, `@ControllerAdvice` |
| Conditional update | `ETag`/`If-Match` + entity version/domain check |
| Idempotent receiver | filter/interceptor + application service + unique constraint |
| Typed contract | OpenAPI-generated types hoặc HTTP Interface tại client boundary |
| Long-running operation | persistent operation state + worker/event; không dùng request thread giữ chờ |
| API observation | Micrometer Observation, trace propagation, low-cardinality tags |

## 14. Anti-patterns

- CRUD API phản chiếu database schema.
- Chatty API/N+1 over network.
- `POST /doEverything` với payload và semantics thay đổi theo flag.
- Version API nhưng không có deprecation/consumer telemetry.
- Retry non-idempotent request mà không có idempotency contract.
- Trả stack trace/internal exception cho consumer.
- Dùng `200 OK` cho error hoặc `500` cho validation/business conflict.
- BFF trở thành monolith orchestration không có ownership.
- Event schema là serialized Java class.

## 15. API review checklist

- Consumer/persona/use case và SLO là gì?
- Vì sao chọn sync, async, REST, RPC hoặc GraphQL?
- Resource/command và ownership boundary có rõ không?
- Happy path, validation, conflict, rate limit, timeout và dependency failure trả gì?
- Retry có an toàn? Idempotency scope/TTL/storage là gì?
- Pagination/order/filter có deterministic và bounded không?
- Concurrent update/lost update được xử lý thế nào?
- Authorization có object/tenant level không?
- Compatibility, deprecation và breaking-change detection ra sao?
- Contract có OpenAPI/AsyncAPI, example và consumer test không?
- Metrics/traces/log có hỗ trợ SLO và support nhưng không lộ PII không?

Nguồn chuẩn: [RFC 9110 - HTTP Semantics](https://www.rfc-editor.org/rfc/rfc9110.html), [RFC 9457 - Problem Details](https://www.rfc-editor.org/rfc/rfc9457.html), [OpenAPI Specification](https://spec.openapis.org/oas/latest.html), [AsyncAPI Specification](https://www.asyncapi.com/docs/reference/specification/latest).

