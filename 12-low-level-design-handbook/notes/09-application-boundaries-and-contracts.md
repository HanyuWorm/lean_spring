# 09 — Application boundary và contracts

## Một request đi qua đâu?

```text
HTTP/message adapter
  → parse + transport validation
  → application use case (authz, transaction, orchestration)
  → domain objects/services (business invariant)
  → ports
  → persistence/vendor adapters
```

Web controller không gọi repository rồi mutate entity tùy ý. Domain không trả HTTP status. Adapter map boundary-specific concerns.

## Command/query/result

```java
record ReserveSeatCommand(UserId actor, ShowId show, Set<SeatId> seats, IdempotencyKey key) {}
sealed interface ReserveSeatResult permits Reserved, Unavailable, InvalidSelection {}
```

Typed result tốt khi failure là expected business outcome; exception cho programming/infrastructure/unexpected failure. Đừng trả `boolean` làm mất reason/context.

## DTO và mapping

- Transport DTO version theo API và validation syntax.
- Application command diễn đạt use case.
- Domain types giữ invariant.
- Persistence record tối ưu storage.

Không cần bốn object cho CRUD trivial, nhưng không dùng một JPA entity làm JSON input/output/event nếu lifecycle/version/security khác nhau.

## Port design

Port thuộc layer **cần** capability:

```java
interface PaymentGateway {
    AuthorizationResult authorize(PaymentAttempt attempt, IdempotencyKey key);
}
```

Nó nói ngôn ngữ application/domain, nhỏ theo use case và không leak SDK exception. Adapter map timeout/decline/unknown outcome khác nhau; `unknown` rất quan trọng với payment.

## Repository contract

Ghi semantics:

- Not found: `Optional`, typed result hay exception?
- Save: insert/update/upsert? version check?
- Transaction do caller hay repository sở hữu?
- Query consistency và ordering/pagination?
- Returned object attached/detached/mutable?

## Authorization

Authentication ở delivery boundary, nhưng object-level authorization thường ở application use case vì nó biết resource/action. Domain invariant không thay authorization. Truyền `Actor`/capability rõ, không đọc static security context trong domain.

## Transaction boundary

Application use case thường mở transaction quanh load → decide → save. Không giữ DB transaction qua remote call nếu tránh được; dùng reserve/process/outbox tùy workflow. Sau rollback, object in-memory có thể đã mutate—đừng reuse ngoài unit of work.

## Spring mapping

- `@RestController`: transport.
- `@Service`/use-case class: application orchestration.
- Plain Java: domain.
- `@Repository`, HTTP client, Kafka producer: adapters.
- `@Transactional`: application boundary hoặc explicit transaction component.

Annotation là mechanism; package/dependency tests mới giữ architecture boundary.
