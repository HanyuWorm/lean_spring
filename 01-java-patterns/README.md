# 01 — Java patterns trước Spring

Mục tiêu là nhìn thấy composition và dependency inversion trước khi Spring tự động wire object graph.

## Có sẵn

- Strategy: `DiscountPolicy`.
- Factory/Registry: `DiscountPolicyRegistry`.
- Chain of Responsibility: `OrderValidator` và danh sách `OrderRule`.
- Decorator: `TaxedPriceCalculator` bọc `BasePriceCalculator`.

## Chạy

```powershell
mvn -pl 01-java-patterns test
```

## Bài tập

1. Thêm segment `PARTNER` mà không sửa `DiscountPolicyRegistry`.
2. Làm registry fail fast khi có hai policy cùng hỗ trợ một segment.
3. Thêm rule giới hạn số lượng nhưng giữ thứ tự rule có chủ đích.
4. Thêm `DiscountedPriceCalculator` decorator và thử đổi thứ tự discount/tax.
5. Viết test chứng minh thứ tự decorator làm thay đổi kết quả.

## Câu hỏi review

- Khi nào registry tốt hơn `switch`?
- Vì sao decorator giữ Open/Closed Principle nhưng có thể làm flow khó debug?
- Rule trong chain có nên side effect không?

