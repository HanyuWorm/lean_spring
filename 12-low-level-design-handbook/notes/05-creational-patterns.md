# 05 — Creational patterns

Creational pattern kiểm soát **cách object hợp lệ được tạo**, đặc biệt khi constructor phức tạp, subtype phụ thuộc context hoặc lifecycle do container quản lý.

## Static factory / Factory Method

```java
Money usd(BigDecimal value) { return Money.of(value, Currency.getInstance("USD")); }
Payment create(PaymentCommand command) { ... }
```

Ưu điểm: tên thể hiện intent, có thể validate/cache/chọn subtype, không bắt caller biết constructor. Nhược: thêm indirection; factory lớn với switch mọi domain type trở thành God Factory.

Dùng khi creation có rule hoặc implementation selection. Không dùng factory chỉ để gọi `new` một constructor đơn giản.

## Abstract Factory

Tạo **họ object tương thích**: `CloudClientFactory` tạo storage/queue/secret clients cùng provider/config; theme factory tạo button/input cùng style.

Risk: interface phình khi thêm product type; factory theo vendor dễ leak vendor concepts. Trong Spring, configuration + bean graph có thể đóng vai trò factory; không cần viết pattern class hình thức.

## Builder

Hợp với object nhiều optional parameters, test fixture hoặc construction theo bước nhưng result phải immutable.

```java
var quote = Quote.builder(requiredCustomer, requiredCurrency)
    .line(product, quantity)
    .discount(policy)
    .build(); // validate cross-field invariant here
```

Builder không thay domain methods nếu object có lifecycle. Đừng tạo entity “rỗng” rồi chain setter làm state trung gian sai.

## Prototype

Copy template phức tạp khi creation cost cao hoặc config runtime. Phải định nghĩa shallow/deep copy, identity mới, mutable collection và resource handle. Java `clone()` thường khó dùng an toàn; copy constructor/factory rõ hơn.

## Singleton

Một instance process-wide phù hợp stateless immutable service hoặc expensive shared registry, nhưng global access che dependency, gây test isolation và concurrency problems. Với Spring, singleton là bean scope mặc định **trong một ApplicationContext**, không phải toàn JVM/cluster.

Inject dependency thay `Singleton.getInstance()`. State business không đặt trong singleton bean nếu cần durability/multi-instance consistency.

## Object Pool

Chỉ dùng cho resource đắt và bounded như DB connection. Pool application object thường là premature optimization và gây stale state. Pool cần borrow timeout, validation, max size, leak detection và reset contract.

## Selection guide

| Vấn đề | Pattern |
|---|---|
| Constructor cần tên/validation | Static factory |
| Chọn subtype theo context | Factory Method |
| Tạo family tương thích | Abstract Factory |
| Nhiều optional fields, immutable result | Builder |
| Copy template runtime | Prototype/copy factory |
| Shared managed resource | DI singleton/pool có boundary |
