# 04 — Cohesion, coupling, encapsulation và immutability

## Cohesion

Các phần đổi cùng lý do nên ở cùng nơi. Functional cohesion tốt nhất: mọi method phục vụ một capability. Coincidental cohesion là `CommonUtils` gom method không liên quan.

Đo bằng câu hỏi:

- Có thể đặt tên class không dùng `And`, `Manager`, `Helper`?
- Field nào chỉ được một nhóm method dùng?
- Một change request thường chạm bao nhiêu responsibility trong class?

## Coupling

Không phải mọi coupling đều xấu. Domain objects cần coupling với domain concepts. Coupling nguy hiểm là:

- Concrete/vendor coupling lan qua nhiều layer.
- Temporal coupling: phải gọi A rồi B rồi C nhưng API không enforce.
- Content coupling: sửa internal field của object khác.
- Shared mutable/global state.
- Control coupling: flag parameter thay đổi toàn algorithm.
- Stamp coupling: truyền object lớn chỉ dùng một field.

## Encapsulation đúng nghĩa

```java
public final class Wallet {
    private Money balance;

    public void debit(Money amount) {
        requireSameCurrency(amount);
        if (balance.isLessThan(amount)) throw new InsufficientFunds();
        balance = balance.subtract(amount);
    }
}
```

`setBalance` public phá invariant dù field private. Expose intent method và giữ transition atomic trong object/aggregate.

Không trả mutable collection nội bộ; dùng `List.copyOf`. Constructor/factory validate object ngay khi tạo. Tránh partially initialized object và setter injection cho required dependency.

## Immutability

Tốt cho value object, command, event và snapshot vì thread-safe by construction, equality rõ và dễ cache. Cost: allocation/copy và update graph. Entity vẫn có thể mutable có kiểm soát qua behavior; không nhất thiết mọi thứ persistent immutable.

## Composition vs inheritance

Composition cho phép thay policy runtime/test, không expose protected internals và tránh fragile base class. Inheritance hợp lý khi quan hệ “is-a” ổn định, base contract rõ, subtype thay thế được và lifecycle chung.

## Tell, don't ask

Thay vì lấy state rồi quyết định bên ngoài:

```java
if (order.getStatus() == PAID) order.setStatus(SHIPPED);
```

Dùng:

```java
order.ship(shipment);
```

Nhưng không biến thành chuỗi delegate vô nghĩa. Application orchestration vẫn cần query result từ collaborator; mục tiêu là giữ rule nơi sở hữu state.

## Law of Demeter

`order.getCustomer().getAddress().getCountry().getTaxRate()` tạo knowledge chain. Hỏi object gần nhất bằng method có nghĩa hoặc đưa policy đủ input. Không áp dụng máy móc cho immutable DTO/value object.
