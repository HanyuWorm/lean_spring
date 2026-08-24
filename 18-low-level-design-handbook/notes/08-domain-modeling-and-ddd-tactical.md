# 08 — Domain modeling và DDD tactical

DDD tactical là công cụ LLD cho domain có rule/language phức tạp, không bắt buộc cho CRUD đơn giản.

## Entity

Có identity ổn định và lifecycle. Entity method enforce transition; constructor/factory tạo state hợp lệ. Equality/hashCode với JPA cần cẩn thận ID sinh sau persist và proxy; có thể dùng business ID được cấp trước.

## Value Object

Immutable, equality theo value, không identity độc lập:

```java
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount == null || currency == null) throw new IllegalArgumentException();
        amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.UNNECESSARY);
    }
}
```

Value object giảm primitive obsession và đặt validation/operation gần data.

## Aggregate

Consistency boundary cho invariant cần atomic. Rule:

- Chỉ mutate qua aggregate root.
- Reference aggregate khác bằng ID khi có thể.
- Aggregate nhỏ để tránh contention/load graph.
- Một transaction thường đổi một aggregate; cross-aggregate dùng process/event nếu business cho phép.

Không chọn aggregate theo quan hệ database `@OneToMany`; chọn theo invariant và concurrency boundary.

## Repository

Collection-like port cho aggregate, không phải DAO cho mỗi table. API theo use case: `findActiveHold(showId, seatId)` có thể thuộc query port; `save(Reservation)` thuộc repository. Đừng expose `JpaRepository` vào domain/application nếu muốn giữ boundary.

## Domain service

Dùng khi rule nghiệp vụ cần nhiều concepts nhưng không thuộc tự nhiên một entity. Không dùng `XService` làm nơi chứa mọi logic bị rút khỏi entity.

## Domain event

Fact trong bounded context: `SeatHeld`, `PaymentAuthorized`. Event immutable, past tense, có identity/time/causation. Phân biệt domain event nội bộ và integration event versioned. Publish remote sau commit bằng outbox nếu cần reliability.

## Specification

Đóng gói predicate domain composable. Hợp validation/query rule phức tạp; specification vừa chạy in-memory vừa translate SQL thường leak limitation của ORM. Tách domain spec và query criteria khi semantics khác.

## Anemic vs rich model

Anemic model không luôn xấu: CRUD/reporting đơn giản dùng transaction script rõ hơn. Rich model đáng giá khi có state transitions, invariants, policy và behavior tái sử dụng. Tránh “rich” giả: entity gọi network, repository và clock toàn cục.

## Mapping persistence

- Domain-first: persistence adapter map domain ↔ record/entity; isolation tốt, thêm mapping cost.
- JPA entity làm domain: ít mapping, nhưng proxy/constructor/collection/transaction constraints đi vào model.

Chọn theo domain complexity và team, ghi rõ compromise. Không để lazy-loading quyết định application behavior ngầm.
