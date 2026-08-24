# 03 — SOLID, GRASP và thiết kế cho thay đổi

## SRP — một lý do thay đổi

Không phải “mỗi class một method”. `Invoice` có thể có nhiều behavior cùng phục vụ invariant hóa đơn. Tách persistence/email/rendering vì chúng đổi theo actor/reason khác.

Dấu hiệu vi phạm: class import web + JPA + pricing + email; constructor có 12 dependency; một change policy chạm nhiều nhánh không liên quan.

## OCP — mở rộng tại trục biến đổi đã biết

Strategy cho pricing/channel khi có nhiều policy và tần suất đổi. Không tạo plugin framework cho rule chưa tồn tại. OCP luôn trả cost: indirection, registration, ordering và debugging.

## LSP — subtype giữ contract

Nếu `Square extends Rectangle` làm `setWidth` phá expectation, hierarchy sai. Kiểm tra precondition không mạnh hơn, postcondition không yếu hơn, invariant giữ nguyên và error semantics tương thích. Composition/capability interface thường an toàn hơn deep inheritance.

## ISP — contract theo client

`ReadableOrderStore` và `OrderWriter` có thể tốt hơn `OrderRepository` 20 method nếu client chỉ cần một capability. Nhưng interface quá nhỏ cho từng method làm mất cohesion và tăng ceremony.

## DIP — policy không phụ thuộc mechanism

Domain/application phụ thuộc `PaymentGateway`, adapter phụ thuộc SDK vendor. Abstraction nên do use case sở hữu, diễn đạt nhu cầu domain, không mirror toàn API vendor.

## GRASP hữu ích

- **Information Expert:** object đủ data giữ decision.
- **Creator:** object aggregate/factory tạo part nó sở hữu.
- **Controller:** application use case nhận system event, không phải web controller làm mọi việc.
- **Low Coupling/High Cohesion:** tối ưu khả năng hiểu/thay đổi cùng nhau.
- **Polymorphism:** variation bằng implementation thay conditional theo type.
- **Pure Fabrication:** repository/mapper khi không có domain object tự nhiên nhưng giúp cohesion.
- **Indirection/Protected Variations:** port/facade chống biến động vendor/protocol.

## Dependency direction

```text
delivery adapter ─┐
persistence adapter ──> application ports/use cases ──> domain
vendor adapter ───┘
```

Domain không import Spring/JPA/HTTP. Đây là direction, không yêu cầu mọi project có bốn module vật lý.

## Design for change worksheet

| Change dự kiến | Tần suất | Impact nếu không cô lập | Boundary/pattern |
|---|---:|---:|---|
| pricing policy | cao | nhiều conditional | Strategy |
| payment vendor | vừa | SDK/DTO/error leak | Port + Adapter |
| domain invariant | thấp nhưng critical | data corruption | Aggregate method + tests |
| output format | cao | domain polluted | Presenter/Mapper |

Chỉ thêm abstraction khi variation có bằng chứng hoặc boundary có risk rõ.
