# Virtual Threads, HikariCP & Backpressure

## 1. Virtual Threads thay đổi gì

Virtual Thread làm thread-per-request khả thi cho workload chờ I/O: code imperative, stack trace và structured flow dễ đọc trong khi JVM park virtual thread thay vì giữ một OS thread. Nó không làm CPU, DB connection, socket, downstream quota hay memory trở nên vô hạn.

Khi bật virtual threads, giới hạn tự nhiên “ít platform threads” biến mất. Nếu không có admission control, hàng chục nghìn request có thể đồng thời đến `dataSource.getConnection()`. Hikari vẫn chỉ có N connection; phần còn lại chờ, giữ heap/context và đến hạn cùng lúc tạo timeout/retry storm.

## 2. Capacity model

DB concurrency không chọn theo số virtual thread. Bắt đầu từ load test của database và Little's Law:

```text
required_connections ~= target_db_throughput * average_connection_hold_time
```

Ví dụ muốn 800 DB operations/s, trung bình giữ connection 20 ms, concurrency lý thuyết khoảng 16. Thêm headroom có thể chọn pool 20–30 rồi kiểm chứng CPU, IOPS, locks và p99. Nếu hold time tăng 10 lần do slow query/lock, throughput an toàn giảm; tăng pool thường làm DB tệ hơn.

Tổng connection budget phải tính trên toàn fleet:

```text
instances * maximumPoolSize + admin/migration/reserve <= database max_connections budget
```

Autoscaling application mà không điều chỉnh budget có thể làm tổng connection tăng đột ngột.

## 3. Admission control trước pool

Semaphore/concurrency limiter đặt trước transaction ngăn một endpoint nuốt hết pool:

```java
final class DbBulkhead {
    private final Semaphore permits = new Semaphore(24, true);

    <T> T call(Callable<T> work) throws Exception {
        if (!permits.tryAcquire(75, TimeUnit.MILLISECONDS)) {
            throw new ServiceOverloadedException("db admission limit reached");
        }
        try {
            return work.call();
        } finally {
            permits.release();
        }
    }
}
```

Permit phải được acquire trước khi borrow connection và release sau transaction. Có bulkhead riêng cho workload nặng/nhẹ hoặc reserve capacity cho health/admin path. Semaphore trong từng instance chỉ bảo vệ cục bộ; limit tổng cần connection budget, autoscaling policy hoặc distributed admission ở upstream.

Không dùng semaphore để bao quanh remote call không cần DB trong khi đang giữ transaction. Trình tự tốt là validate/call độc lập trước, transaction DB ngắn sau; hoặc dùng Saga khi không thể atomically bao phủ remote call.

## 4. Timeout/deadline hierarchy

Outer deadline phải lớn hơn các inner timeout có budget rõ ràng, nhưng không quá lớn để giữ backlog:

```text
client deadline
  > gateway timeout
    > app operation budget
      > connection acquisition timeout
      > statement/lock timeout
      > downstream connect/read timeout
```

Không cộng retry khiến tổng vượt deadline. Propagate deadline, dừng work vô ích khi caller đã hủy. Cấu hình ví dụ phải được load test, không copy nguyên số:

- Hikari `maximumPoolSize`: theo DB budget;
- `connectionTimeout`: fail fast, thường ngắn hơn request budget;
- query/transaction/lock timeout: giới hạn thời gian giữ connection;
- `maxLifetime` nhỏ hơn infrastructure/NAT/database connection lifetime;
- leak detection chỉ dùng chẩn đoán, không phải correctness.

## 5. Pinning và phiên bản Java

Trên Java 21, virtual thread có thể bị pin vào carrier trong một số blocking operation khi nằm trong `synchronized`/native/foreign call; pinning dài làm giảm scalability. Dùng JFR và `-Djdk.tracePinnedThreads=full` trong chẩn đoán, giữ critical section ngắn, tránh blocking I/O bên trong monitor.

Java 24 đưa thay đổi “Synchronize Virtual Threads without Pinning” (JEP 491), loại bỏ phần lớn pinning do `synchronized`. Native/foreign code và resource bottleneck vẫn phải đo. Vì workspace chủ yếu JDK 21, checklist pinning vẫn quan trọng; đừng dùng kết quả Java mới để kết luận cho runtime cũ.

## 6. Spring Boot design

Với Spring MVC/imperative JDBC, virtual threads cho phép giữ programming model blocking dễ hiểu. Với WebFlux, lợi thế nằm ở end-to-end non-blocking, streaming và explicit backpressure nhưng có complexity của reactive chain/context. Không có lựa chọn thắng tuyệt đối; benchmark theo workload, team skill và dependency stack.

Những điểm cần audit khi bật virtual threads:

- `ThreadLocal` nặng hoặc cache theo thread: số thread tăng rất lớn;
- executor/bulkhead cũ dựa vào fixed thread pool như một concurrency limiter;
- transaction boundary vô tình bao gồm REST call;
- thư viện native/blocking có pinning;
- log/trace context propagation;
- scheduled task semantics và graceful shutdown;
- CPU-bound work vẫn giới hạn theo core, nên dùng bounded executor/permit.

## 7. Backpressure end-to-end

Backpressure không chỉ là Reactor operator. Với imperative service, nó gồm:

- gateway rate/concurrency limit;
- bounded request admission;
- semaphore/bulkhead theo downstream;
- bounded Kafka consumer concurrency;
- bounded queue và reject policy rõ;
- timeout, circuit breaker và retry budget;
- load shedding ưu tiên request quan trọng.

Queue không giới hạn chỉ đổi `503` sớm thành OOM/timeout muộn. Khi saturation, trả `429` cho quota và `503` cho capacity, kèm jittered backoff. Circuit breaker không thay concurrency limiter: breaker phản ứng failure, limiter ngăn saturation.

## 8. Metrics và thí nghiệm

Đo đồng thời:

- request active, queue/admission reject, p95/p99 và timeout;
- virtual thread count, carrier CPU, pinning/JFR events;
- Hikari active/idle/pending/acquisition time;
- DB sessions, CPU, IOPS, lock wait, slow query;
- downstream active calls, timeout, retry amplification;
- heap/GC và socket/file descriptors.

Chạy step load đến saturation, spike, slow-DB injection và downstream timeout. Success criterion không phải chỉ RPS cao: error rate bị chặn, p99 ổn định, DB không vượt budget và recovery nhanh sau khi bỏ fault.

## 9. Câu trả lời phỏng vấn

“Virtual Threads làm waiting Java thread rẻ, nên tôi không dùng thread count để bảo vệ DB nữa. Tôi giữ Hikari theo capacity thật của DB, đặt semaphore/admission trước pool, transaction và timeout ngắn, đo pending/acquisition/lock wait. Khi overload tôi reject sớm thay vì mở pool vô hạn. Trên Java 21 tôi còn audit pinning; Java 24 giảm synchronized pinning nhưng không xóa bottleneck tài nguyên.”
