# 02 - Proxy / AOP

## Mental model

```text
caller -> Spring proxy -> transaction interceptor -> target method
target -> this.otherMethod() ----------------------> target method
```

Spring declarative transaction, method security, caching, async và resilience annotations thường được thực thi bởi proxy/advice. Annotation là metadata; proxy mới là runtime mechanism.

`TransactionProbeService` chứng minh hai call khác nhau:

- `probe.transactionalProbe()` đi từ test qua proxy nên transaction active.
- `probe.callTransactionalMethodThroughSelf()` vào target rồi gọi `this.transactionalProbe()`, không quay lại proxy nên annotation ở method thứ hai không được áp.

## JDK proxy và class-based proxy

- JDK dynamic proxy proxy interface.
- Class-based proxy tạo subclass, hữu ích khi bean không có interface.
- `final` class/method, private method và construction lifecycle giới hạn join point.

Đừng thiết kế domain dựa vào loại proxy. Đặt transaction ở public application use case là boundary dễ hiểu nhất.

## Transaction semantics quan trọng

- Runtime exception mặc định làm rollback; checked exception cần policy rõ.
- Transaction chỉ bao phủ resource tham gia transaction manager; không làm HTTP call/broker publish atomic.
- Giữ transaction trong remote call dài làm connection/lock bị giữ lâu.
- Advice order giữa transaction, retry, cache và observation thay đổi behavior.

`LedgerService.recordThenFail()` insert vào H2 rồi throw. Integration test chứng minh transaction advice rollback thật; mock repository không thể chứng minh điều này.

## Force, boundary, failure mode

- Force: cross-cutting consistency đối nghịch với business code clarity.
- Boundary: external invocation qua proxy; advice bọc target invocation.
- Failure mới: self-invocation bypass, wrong method visibility, propagation/rollback sai, advice order và hidden control flow.
- Test: phải lấy bean từ Spring context, assert proxy và quan sát transaction/database behavior.

## Cách sửa self-invocation

1. Đặt transaction tại public use case bao trọn business operation.
2. Tách collaborator bean nếu method kia thật sự là boundary riêng.
3. Dùng `TransactionTemplate` khi cần transaction scope explicit trong cùng class.

Không khuyến khích self-inject proxy hoặc gọi `AopContext.currentProxy()` vì coupling implementation detail của framework và làm flow khó đọc.

## Khi không nên dùng AOP

Không dùng advice để ẩn workflow nghiệp vụ như “sau payment thì reserve inventory”. Flow đó cần hiển thị trong application service/process manager. AOP phù hợp với concern có semantics đồng nhất: transaction, authorization, telemetry hoặc technical retry policy.

## Bài mở rộng

1. Thêm checked exception và kiểm tra rollback mặc định, sau đó cấu hình `rollbackFor`.
2. Thêm `REQUIRES_NEW`, giải thích connection usage và partial commit.
3. Bọc method bằng retry và transaction theo hai thứ tự, đo số transaction được tạo.

