# 07 — Virtual Threads và tác động lên System Design

Project dùng Spring Boot 4.1, Java 21, Spring Framework 7 native `@ConcurrencyLimit`, Spring Data JPA, HikariCP và H2.

## Vấn đề được mô phỏng

Virtual thread giúp mô hình blocking/thread-per-request scale tốt hơn về số thread. Nó không tăng số connection database và không làm transaction rẻ hơn.

```text
N virtual threads
       |
       v
@ConcurrencyLimit(4)       application bulkhead/backpressure
       |
       v
Hikari maximum-pool-size=4 finite database resource
       |
       v
H2 database
```

`WorkloadService.process()` ghi database bằng `saveAndFlush()` trước khi sleep. Connection được giữ trong transaction trong thời gian chờ, mô phỏng một transaction chậm. `@ConcurrencyLimit(4)` chặn số transaction đồng thời trước khi chúng tranh chấp Hikari pool.

## Chạy

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

mvn -pl 07-virtual-threads-system-design test
mvn -pl 07-virtual-threads-system-design spring-boot:run
```

## API

Tạo một work item:

```powershell
Invoke-RestMethod -Method Post `
  -Uri http://localhost:8080/api/work-items `
  -ContentType application/json `
  -Body '{"customerId":"C-1","processingMillis":200}'
```

Chạy 20 blocking transactions đồng thời:

```powershell
Invoke-RestMethod -Method Post `
  -Uri 'http://localhost:8080/api/load?requests=20&processingMillis=200'
```

Kiểm tra:

- `GET http://localhost:8080/api/work-items/stats`
- `GET http://localhost:8080/actuator/metrics/workload.inflight`
- `GET http://localhost:8080/actuator/prometheus`
- `GET http://localhost:8080/h2-console`

H2 console: JDBC URL `jdbc:h2:mem:virtual-threads`, user `sa`, password trống.

## Thí nghiệm bắt buộc

1. Chạy load với `requests=100`, quan sát `virtualThreadExecutions=100` nhưng `maxObservedConcurrency<=4`.
2. Xóa `@ConcurrencyLimit(4)`, chạy lại và quan sát task chờ ở Hikari pool thay vì ở application bulkhead.
3. Tăng Hikari pool lên 20 nhưng giữ database workload chậm; so sánh throughput và latency.
4. Di chuyển `Thread.sleep` ra ngoài transaction và giải thích vì sao connection được giải phóng sớm hơn.
5. Tắt `spring.threads.virtual.enabled`; so sánh thread name, throughput và memory/JFR.
6. Đổi workload thành CPU-bound hashing; xác nhận virtual threads không cải thiện CPU capacity.
7. Thêm timeout/deadline thay vì cho task chờ bulkhead vô hạn.

## Kết luận kiến trúc

- Không dùng thread-pool size như concurrency control ngầm nữa; đặt bulkhead/semaphore rõ ràng theo downstream capacity.
- Transaction phải ngắn. Virtual thread không hợp thức hóa việc giữ connection trong lúc gọi network.
- Pool database, HTTP connection pool, broker partition và rate limit vẫn là finite resources.
- `ThreadLocal` hoạt động nhưng tạo rất nhiều dữ liệu per-thread có thể tốn bộ nhớ; ưu tiên context nhỏ và lifecycle rõ ràng.
- Benchmark phải đo p95/p99, pool wait, timeout, pinned threads và downstream saturation, không chỉ requests/second.

