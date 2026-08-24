# 03 - Strategy

## Vấn đề

Giá phụ thuộc customer tier. Nếu mỗi policy có dependency, release cadence hoặc test matrix riêng, một chuỗi `if/switch` trong use case sẽ tăng coupling. Strategy đóng gói từng policy sau cùng contract.

```text
PricingService
    -> PricingStrategyRegistry
       -> STANDARD strategy
       -> VIP strategy
```

Spring chỉ làm composition: inject `List<PricingStrategy>`. Registry xây immutable map và bảo đảm key duy nhất. `PricingService` không biết concrete class.

## Strategy khác DI thế nào?

DI là mechanism cung cấp dependencies. Strategy là behavioral pattern biểu diễn thuật toán/policy thay thế được. Spring collection injection giúp compose strategies nhưng không tự thiết kế selection contract, duplicate handling hoặc business key.

## Selection policy

Các lựa chọn thường gặp:

- exact key như demo: đơn giản, deterministic;
- `supports(context)`: linh hoạt nhưng có thể nhiều strategy cùng match hoặc không match;
- ordered first-match: cần priority contract và test order;
- composite strategy: nhiều policy cùng đóng góp kết quả, cần rule merge/conflict.

Không dùng bean name làm business key nếu đổi tên class/bean có thể phá contract âm thầm.

## Force, boundary, failure mode

- Force: thay đổi/extension độc lập đối nghịch với selection/registration complexity.
- Boundary: use case phụ thuộc `PricingStrategy`; implementation chứa policy.
- Failure mới: duplicate key, missing strategy, ambiguous `supports`, wrong priority và silent fallback.
- Test: contract test cho mọi implementation, registry duplicate/missing test và business invariant test.

## Strategy hay sealed switch?

Giữ sealed type + exhaustive switch khi tập biến thể nhỏ, ổn định, không có dependencies riêng và logic thuộc một module. Chọn Strategy khi variants là plugin/policy thật, cần inject collaborator hoặc deploy/change độc lập. Nhiều class không tự động tốt hơn một switch rõ ràng.

## Production considerations

- Tiền dùng `BigDecimal`, scale/rounding là policy phải explicit.
- Feature flag không nên chọn strategy làm thay đổi giá giữa hai retry của cùng order; snapshot decision vào order.
- Strategy có remote call cần timeout/idempotency/observation ở adapter, không giấu trong domain pricing formula.

## Bài mở rộng

1. Thêm employee policy và dùng parameterized contract test cho tất cả strategies.
2. Đổi sang `supports(PricingRequest)` rồi xử lý zero/multiple matches.
3. Thêm effective date/version để tái dựng giá lịch sử.

