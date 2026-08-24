# 02 — Spring Core patterns

Project cho thấy Spring tự động compose strategy và dùng proxy để áp dụng cross-cutting concern.

## Quan sát

- `PaymentService` chỉ phụ thuộc `List<PaymentHandler>`.
- Registry được dựng một lần trong constructor và fail fast nếu trùng key.
- `@Audited` không chứa logic; `AuditAspect` là decorator/proxy concern.
- Test kiểm tra bean thực tế là AOP proxy.

## Chạy

```powershell
mvn -pl 02-spring-core-patterns test
```

## Bài tập

1. Thêm `WalletPaymentHandler` mà không sửa `PaymentService`.
2. Thử gọi một method `@Audited` bằng self-invocation và giải thích vì sao aspect không chạy.
3. Sửa self-invocation bằng cách tách application boundary sang bean khác.
4. Thêm `@Order` cho handler rồi so sánh registry theo list với map injection.
5. Không audit số thẻ hoặc dữ liệu nhạy cảm.

