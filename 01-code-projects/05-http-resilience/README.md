# 05 — HTTP Service Client + resilience

Project dùng declarative HTTP client `@HttpExchange`, bọc provider DTO sau outbound port, và bật native resilience của Spring Framework 7.

## Boundary

```text
PaymentController
    -> PaymentPort                         domain-facing contract
        -> PaymentProviderAdapter          anti-corruption adapter
            -> PaymentProviderApi          @HttpExchange remote contract
                -> RestClient
```

`@Retryable` và `@ConcurrencyLimit` chạy qua Spring proxy. Retry chỉ áp dụng exception được xác định là transient.

## Chạy

```powershell
mvn -pl 05-http-resilience test
mvn -pl 05-http-resilience spring-boot:run
```

`payment.provider.base-url` mặc định là địa chỉ giả, vì vậy chỉ gọi endpoint khi đã cấu hình mock/provider thật.

## Bài tập

1. Dùng WireMock hoặc MockWebServer tạo chuỗi response `500, 500, 200`.
2. Cấu hình connect/read timeout; chứng minh tổng retry nằm trong request deadline.
3. Truyền `Idempotency-Key` cho payment write request.
4. Không retry HTTP 400/401/403; retry có chọn lọc 429/502/503/504.
5. Thêm circuit breaker bằng Resilience4j và metric cho state transition.
6. Chứng minh self-invocation không kích hoạt retry proxy.

