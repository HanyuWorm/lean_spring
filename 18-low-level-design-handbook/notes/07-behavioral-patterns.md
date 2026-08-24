# 07 — Behavioral patterns

## Strategy

Đóng gói policy/algorithm thay đổi: pricing, spot selection, routing. Context truyền input đủ và strategy trả result; tránh strategy tự query nửa hệ thống.

Chọn khi có nhiều policy thực hoặc cần test/A-B/config. Một `if` ổn định hai nhánh đơn giản có thể rõ hơn registry/plugin.

## State

Behavior phụ thuộc state và transition phức tạp. Có hai cách:

- Enum + transition table: state ít, action đơn giản, persistence dễ.
- State objects: behavior mỗi state lớn, transition thường đổi.

State pattern không tự giải quyết concurrent transition; cần version/lock ở storage boundary.

## Command

Biến request thành object: queue, audit, retry, undo hoặc dispatcher. Command chứa intent/validated input, không nên chứa service locator. Handler orchestration; domain object giữ invariant.

Phân biệt Command (yêu cầu có thể reject) và Event (fact đã xảy ra, tên quá khứ).

## Chain of Responsibility

Pipeline validation/filter/handler theo thứ tự. Contract cần rõ:

- first-match hay tất cả?
- short-circuit khi nào?
- error/partial result aggregate thế nào?
- ordering do ai sở hữu?

Spring Security filter chain là ví dụ; chain động khó debug nếu bean order ngầm.

## Template Method vs Callback

Template Method dùng inheritance định skeleton, subclass override hook. Callback/composition truyền function/strategy thường linh hoạt và test dễ hơn trong Java hiện đại.

`TransactionTemplate.execute(callback)` giữ skeleton transaction và cho caller phần biến đổi.

## Observer

Publisher thông báo subscribers. In-process observer synchronous khác broker event durable. Cần xác định ordering, transaction, error isolation và subscriber lifecycle. Domain event thường thu trong aggregate rồi publish after commit; không remote call trực tiếp từ entity.

## Mediator

Giảm many-to-many giữa components bằng coordinator. Hợp UI/workflow; mediator dễ thành God Object nếu chứa business rules của mọi component.

## Iterator

Ẩn representation khi traverse collection/stream. Với remote/paged data, contract cần cursor lifecycle, consistency, backpressure và resource close; đừng giả nó giống in-memory list.

## Memento

Snapshot state để undo/audit. Với aggregate lớn, snapshot cost cao; command/event log có thể phù hợp nhưng phức tạp schema/version. Không expose mutable internals trong memento.

## Visitor

Thêm operation mới trên hierarchy type ổn định. Đổi lại thêm subtype mới rất đắt. Sealed classes + pattern matching giảm ceremony nhưng trade-off type-vs-operation vẫn còn.

## Pattern combinations

- State + Strategy: lifecycle cố định, policy trong một transition thay đổi.
- Command + Handler + Repository: application use case.
- Composite + Specification: rule tree.
- Observer + Outbox: durable integration event sau commit.
- Decorator + Strategy: cross-cutting quanh policy implementation.
