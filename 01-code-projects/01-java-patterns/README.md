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

- **Khi nào registry tốt hơn `switch`?** Khi implementations được thêm độc lập, cần lookup theo key/capability, plugin/DI registration và duplicate/missing validation. `switch` rõ hơn với tập case nhỏ, đóng và ổn định; registry không nên che ordering/selection mơ hồ.
- **Vì sao decorator giữ Open/Closed Principle nhưng có thể làm flow khó debug?** Behavior mới được thêm bằng wrapper không sửa core, nhưng kết quả phụ thuộc order, stack trace/bean proxy có nhiều lớp và một call có thể retry/cache/metric ngầm. Dùng tên rõ, composition root, trace và test ordering.
- **Rule trong chain có nên side effect không?** Mặc định không: validation/decision chain pure giúp reorder/retry/test an toàn. Nếu chain là processing pipeline có side effect, contract phải định nghĩa order, short-circuit, idempotency, rollback/partial failure và không gọi nó là validation chain.
