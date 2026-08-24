# 08 - Adapter / Decorator

Hai pattern phối hợp tốt nhưng giải quyết hai vấn đề khác nhau.

## Adapter

`WarehouseInventoryAdapter` chuyển external contract sang application port:

```text
WarehouseResponse(productCode, onHand, reserved)
    -> WarehouseInventoryAdapter
       -> Stock(sku, available)
```

Adapter chịu trách nhiệm protocol/model/error mapping. `InventoryService` chỉ phụ thuộc `InventoryPort`; vendor DTO và exception dừng ở adapter boundary. Đây là Anti-Corruption Layer nhỏ.

Nếu thay fake client bằng HTTP/JPA/Kafka, domain/application contract không cần đổi. Port nên mô tả capability nghiệp vụ (`getStock`) chứ không phản chiếu vendor SDK (`executeWarehouseQueryV2`).

## Decorator

Decorator implement cùng interface và delegate:

```text
InventoryService
  -> CountingInventoryDecorator
     -> CachingInventoryDecorator
        -> WarehouseInventoryAdapter
           -> FakeWarehouseClient
```

Thứ tự có nghĩa:

- Counting ngoài cache đo logical calls: 2.
- Warehouse adapter chỉ chạy cache miss: 1.
- Nếu metrics decorator đặt trong cache, nó đo source loads thay vì user calls.

Retry, circuit breaker, tracing, caching và authorization đều có thể dùng decorator/interceptor, nhưng order/semantics phải explicit.

## Force, boundary, failure mode

- Adapter force: core model stability đối nghịch external protocol/vendor change.
- Decorator force: thêm orthogonal behavior nhưng không sửa core implementation.
- Boundary: core sở hữu `InventoryPort`; adapter/decorators phụ thuộc port; Spring config compose chain.
- Failure mới: mapping drift, exception leak, decorator order sai, double instrumentation, cache stale/stampede và recursive bean composition.
- Test: external-to-domain mapping, vendor error translation, delegate call count, cache hit/miss và decorator order.

## Vì sao compose trong `@Configuration`

Các decorator không tự inject `InventoryPort` bằng component scan vì sẽ dễ tạo ambiguous/recursive graph. Composition root tạo raw adapter rồi bọc theo order đọc được. Business code không biết decorators.

Trong production có thể dùng Spring Cache/Micrometer/Resilience framework, nhưng vẫn phải hiểu chain tương đương để giải thích metric, retry và cache order.

## Cache caveat

Demo cố ý tối giản, chưa có TTL/invalidation/stampede policy. Không copy cache này vào production. Cache design phải nêu freshness tolerance, eviction, negative caching, hot key và behavior khi source/cache down.

## Bài mở rộng

1. Thêm TTL và fake clock; test stale/fresh boundary.
2. Thêm retry decorator và chứng minh đặt ngoài cache không retry cache hit.
3. Thay fake client bằng `RestClient` mock server adapter mà giữ nguyên `InventoryService` test.

