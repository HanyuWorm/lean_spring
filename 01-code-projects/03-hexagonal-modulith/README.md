# 03 — Hexagonal Architecture + Spring Modulith

Hai application module `order` và `inventory` nằm trong cùng process nhưng không gọi trực tiếp bean nội bộ của nhau. `order` phát public domain event; `inventory` lắng nghe event đó.

## Boundary

```text
PlaceOrderCommand -> OrderManagement -> Order (domain)
                           |
                           +-> OrderStore (outbound port)
                           +-> OrderPlaced (public event)
                                      |
                                      v
                              InventoryManagement
```

`order.domain.Order` không import Spring. `InMemoryOrderStore` là driven adapter. Spring Modulith kiểm tra module dependency bằng test.

## Chạy

```powershell
mvn -pl 03-hexagonal-modulith test
```

## Bài tập

1. Thêm inbound REST adapter nhưng không trả domain object trực tiếp.
2. Thay `InMemoryOrderStore` bằng JPA adapter mà không sửa domain.
3. Thử import class trong `order.internal` từ `inventory`; xác nhận verification thất bại.
4. Thêm payment module chỉ phụ thuộc public event/API của order.
5. Dùng `@ApplicationModuleTest` để test riêng inventory.

