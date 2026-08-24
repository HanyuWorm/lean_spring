# 04 - Factory

Project minh họa hai khái niệm thường bị trộn: domain factory và Spring `FactoryBean`.

## Domain Factory

`OrderFactory` gom creation policy:

- customer bắt buộc;
- total phải dương;
- order number đến từ injected generator;
- thời gian đến từ `Clock` để test được;
- trạng thái khởi tạo luôn `PENDING`.

Factory hợp lý khi object creation có invariant, nhiều collaborator hoặc cần chọn subtype. Nếu chỉ là `new Address(street, city)`, constructor/named static factory đơn giản hơn.

```text
application -> OrderFactory -> Order
                    |-> OrderNumberGenerator
                    |-> Clock
```

Factory không nên persist, publish event và gọi remote trong cùng `create`; đó là orchestration của application service.

## Spring `FactoryBean<T>`

`ShippingClientFactoryBean` là extension point của container cho object có construction phức tạp hoặc do framework/proxy sinh ra.

- Bean name `shippingClient` trả product `ShippingClient`.
- Bean name `&shippingClient` trả chính factory.
- `getObjectType()` giúp container type matching.
- `isSingleton()` mô tả scope của product do factory quản lý.

Đừng nhầm `FactoryBean` với `BeanFactory`: `BeanFactory` là Spring container contract; `FactoryBean<T>` là một bean đặc biệt tạo product bean.

## Force, boundary, failure mode

- Force: construction/invariant phức tạp đối nghịch với caller simplicity.
- Boundary: caller phụ thuộc factory/product contract, không biết construction details.
- Failure mới: god factory, Service Locator, hidden singleton/state, create có side effect hoặc default sai.
- Test: invariant của product, invalid input, deterministic collaborator và container lookup product versus factory.

## Factory khác Strategy

Factory quyết định object nào được tạo; Strategy quyết định behavior nào được thực thi. Factory có thể tạo một Strategy, nhưng đừng tạo một mega-factory chứa mọi `switch` của hệ thống.

## Bài mở rộng

1. Thay `Clock` bằng fixed clock trong `@TestConfiguration` và assert exact timestamp.
2. Tạo prototype product từ `FactoryBean`, phân tích lifecycle/thread safety.
3. Thêm `OrderDraft` riêng và chỉ factory được chuyển draft hợp lệ thành aggregate.

