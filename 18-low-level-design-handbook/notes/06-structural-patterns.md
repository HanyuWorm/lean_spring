# 06 — Structural patterns

## Adapter

Chuyển interface vendor/legacy sang port do application sở hữu.

```java
interface PaymentGateway { Authorization authorize(PaymentRequest request); }
final class StripePaymentAdapter implements PaymentGateway { /* map DTO/error */ }
```

Adapter là anti-corruption boundary: mapping type, error, retry/idempotency semantics. Đừng chỉ rename method rồi để vendor exception/DTO leak.

## Facade

Một interface đơn giản cho subsystem phức tạp. `CheckoutFacade` có thể expose use cases cho UI, nhưng không nên trở thành transaction script khổng lồ. Facade giảm knowledge/coupling; không thay ownership boundary.

## Decorator

Bọc cùng contract để thêm behavior composable:

```text
CoreNotifier ← RetryingNotifier ← MetricsNotifier ← AuditingNotifier
```

Order decorator quan trọng. Retry bên ngoài metrics cho metric khác retry bên trong. Side effect phải idempotent. Spring AOP/proxy là infrastructure decorator nhưng self-invocation và final/private method có giới hạn.

## Proxy

Đại diện cho object khác để lazy load, remote call, access control, cache hoặc instrumentation. Proxy giữ cùng contract nhưng remote proxy không thể che latency/failure; API phải thể hiện async/error/deadline khi cần.

## Composite

Cho client xử lý leaf và group đồng nhất: rule tree, file hierarchy, UI component. Cần quyết định short-circuit, ordering, cycle và error aggregation.

```java
sealed interface Rule permits AtomicRule, AllOf, AnyOf {
    Decision evaluate(Context context);
}
```

## Bridge

Tách hai dimension thay đổi độc lập, ví dụ `Report` abstraction và `Renderer` implementation. Tránh Cartesian subclass `PdfSalesReport`, `HtmlSalesReport`, `PdfAuditReport`.

## Flyweight

Chia sẻ intrinsic immutable state cho số lượng object rất lớn; extrinsic state truyền vào operation. Có lợi khi profiling chứng minh memory pressure. Cache key, lifecycle và thread safety là cost.

## Khi các pattern dễ nhầm

| Pattern | Mục đích chính |
|---|---|
| Adapter | đổi contract không tương thích |
| Facade | đơn giản hóa subsystem |
| Decorator | thêm behavior theo composition |
| Proxy | kiểm soát truy cập tới target |
| Bridge | tách hai hierarchy biến đổi |
| Composite | đồng nhất leaf/group |

Không đặt tên theo pattern nếu intent domain rõ hơn: `AuthorizedPaymentGateway` tốt hơn `PaymentGatewayProxy`.
