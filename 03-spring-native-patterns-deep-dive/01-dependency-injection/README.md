# 01 - Dependency Injection / Inversion of Control

## Vấn đề

`OrderNotificationService` cần gửi qua email hoặc SMS nhưng không nên tự `new` adapter, đọc environment hoặc biết cách các implementation được khởi tạo. Spring container sở hữu object graph; application service chỉ nhận dependency qua constructor.

## Luồng code

```text
Spring component scan
    -> List<NotificationChannel>
       -> NotificationChannelRegistry
          -> OrderNotificationService
```

`EmailNotificationChannel` và `SmsNotificationChannel` là plugin beans. Spring inject toàn bộ implementation vào registry. Registry đổi collection kỹ thuật thành business lookup có validation và fail-fast semantics.

## Vì sao constructor injection

- Dependency bắt buộc được biểu diễn ngay trong constructor.
- Object có thể unit test mà không cần container.
- Field có thể `final`; không tồn tại trạng thái “bean đã tạo nhưng chưa inject”.
- Circular dependency lộ sớm thay vì bị che bởi field/setter injection.

Setter injection chỉ hợp lý với dependency thật sự optional hoặc reconfiguration có chủ ý. Không dùng `ApplicationContext#getBean()` trong business service vì nó biến container thành Service Locator và che dependency.

## `@Primary`, `@Qualifier` và collection injection

- `@Primary`: một default mang tính composition; nguy hiểm nếu business cần lựa chọn rõ theo request.
- `@Qualifier`: tốt khi dependency có role kỹ thuật ổn định, ví dụ `fraudClock`/`systemClock`.
- `List<T>`: tốt cho plugin/pipeline. Order phải explicit nếu có nghĩa.
- `Map<String,T>` mặc định dùng bean name, dễ coupling business key vào tên bean. Demo dùng `key()` và registry riêng để key là contract rõ ràng.

## Force, boundary, failure mode

- Force: extensibility/testability đối nghịch với construction/configuration complexity.
- Boundary: consumer phụ thuộc `NotificationChannel`; Spring composition chọn concrete beans.
- Failure mới: duplicate/missing bean, key collision, ambiguous candidate, lifecycle/scope sai và circular graph.
- Mitigation: immutable registry, fail-fast lúc startup, constructor injection và context test nhỏ.

## Test đang chứng minh gì?

- Spring tìm và inject đủ hai channels.
- Business key chọn đúng implementation.
- Unknown key bị reject thay vì fallback âm thầm.

Context-load test đơn thuần chưa đủ; test phải gọi behavior của object graph.

## Không nên dùng pattern quá mức

Không cần interface nếu chỉ có một implementation, không có boundary/variation point và test có thể dùng concrete class. DI không đồng nghĩa “interface cho mọi service”. Domain entity/value object cũng không cần là Spring bean.

## Bài mở rộng

1. Thêm push channel nhưng không sửa `OrderNotificationService`.
2. Tạo duplicate key và viết `ApplicationContextRunner` test rằng startup thất bại.
3. Thêm prototype-scoped bean rồi giải thích vì sao inject nó vào singleton không tự tạo instance mới mỗi call.

