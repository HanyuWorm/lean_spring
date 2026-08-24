# 15 — Anti-pattern và review checklist

## Anti-patterns

### God Service / Manager

`OrderManager` validate, price, persist, call payment, email và map DTO. Tách theo use case/boundary; đưa invariant về aggregate/policy.

### Anemic model trong domain phức tạp

Entity chỉ setter; service đọc state rồi quyết định. Race và rule duplication tăng. Expose behavior atomic.

### Interface explosion

Mỗi class có interface cùng tên, một implementation, không boundary/variation. Tăng navigation/mock mà không giảm coupling.

### Pattern soup

Factory tạo Strategy được Decorator bởi Proxy qua AbstractFactory dù requirement đơn giản. Pattern count không phải quality.

### Primitive obsession

String cho money/currency/email/order ID/status. Validation và mix-up lan rộng. Dùng value object khi semantics/rule đáng giá.

### Boolean blindness

`process(true, false, true)` hoặc trả boolean. Dùng enum/options/typed result.

### Temporal coupling

Caller phải `initialize→validate→save→publish` đúng order. Encapsulate một operation/use case hoặc state type.

### Leaky abstraction

Port trả vendor/JPA type/exception. Đổi vendor vẫn sửa core; abstraction không bảo vệ variation.

### Shared mutable singleton

Bean singleton giữ cache/session/counter không synchronization/durability. Multi-instance làm state khác nhau.

### Catch-and-ignore / retry-all

Che corruption và duplicate side effect. Phân loại error và định nghĩa recovery.

## Review checklist

### Requirement và model

- [ ] Scope/non-goal/assumption rõ.
- [ ] Use cases và alternate/failure flows.
- [ ] Invariants có owner và tests.
- [ ] Entity/value object/aggregate boundary theo lifecycle/consistency.
- [ ] Naming dùng ubiquitous language.

### Contracts và dependencies

- [ ] Commands/queries/results/errors typed rõ.
- [ ] Domain không phụ thuộc transport/persistence/vendor.
- [ ] Interfaces quanh boundary/variation thật.
- [ ] Authorization, tenant và sensitive output rõ.
- [ ] Error mapping không leak internals.

### State/data/concurrency

- [ ] State transition/illegal transition explicit.
- [ ] Transaction boundary và rollback behavior.
- [ ] Concurrent check-then-act được bảo vệ cuối cùng.
- [ ] Idempotency scope/fingerprint/outcome.
- [ ] Time, precision, ordering và version semantics.

### Changeability và operations

- [ ] Expected variation cô lập vừa đủ.
- [ ] Không speculative plugin/layer/interface.
- [ ] IO bounded timeout/retry safe.
- [ ] Audit/metric/log có redact.
- [ ] Resource lifecycle/cancellation rõ.

### Tests

- [ ] Happy/boundary/negative/illegal transition.
- [ ] Contract/integration cho adapters.
- [ ] Concurrency/idempotency/rollback.
- [ ] Fixed clock/no timing sleep.
- [ ] Test behavior, không khóa internal implementation.

Dùng [template review](../templates/lld-review-template.md) để ghi finding có scenario và recommendation thay vì nhận xét chung chung.
