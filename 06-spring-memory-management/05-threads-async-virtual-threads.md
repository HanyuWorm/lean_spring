# 05 — Threads, Async và Virtual Threads

## 1. Platform thread có memory cost đáng kể

Mỗi platform thread cần native stack và JVM/OS bookkeeping. Stack size là reservation/configuration, mức resident thực tế có thể khác, nhưng hàng nghìn thread vẫn tạo áp lực native memory và scheduler.

Một fixed thread pool thường vô tình đóng vai trò concurrency limiter. Nhưng nếu queue là unbounded, overload chuyển từ “quá nhiều thread” thành “quá nhiều task đang giữ object”.

```text
producer rate > consumer capacity
        -> queue depth tăng
        -> request/payload/closure sống lâu
        -> old-gen/live set tăng
        -> latency và timeout tạo thêm retry
```

Queue cần capacity, rejection/backpressure policy và metric.

## 2. `ThreadLocal` với thread pool

Thread pool tái sử dụng worker, nên value có thể sống qua nhiều request và rò sang task sau nếu không xóa:

```java
context.set(requestContext);
try {
    process();
} finally {
    context.remove();
}
```

Audit MDC, tenant context, locale, security/custom tracing context. Framework có cơ chế propagation riêng; tự copy tất cả ThreadLocal sang async task có thể vừa sai ngữ nghĩa vừa nhân memory.

Key của `ThreadLocalMap` là weak reference nhưng value không tự biến mất ngay theo cách application mong muốn. “Mất key” vẫn có thể để value bị giữ bởi live thread tới khi map dọn entry.

## 3. `@Async` và `CompletableFuture`

Các vấn đề thường gặp:

- executor mặc định/cấu hình không rõ capacity;
- task closure capture entity/request/large payload;
- future chain chưa complete giữ cả result và callback graph;
- timeout ở caller nhưng task vẫn chạy;
- exception path không giải phóng resource;
- submit nhanh hơn xử lý.

`@Transactional` kiểu imperative thường gắn transaction với current thread. Tạo thread mới không tự truyền transaction sang đó. Đừng giữ managed entity rồi sửa trong async task với giả định transaction cũ còn tồn tại; truyền ID/immutable command và mở transaction mới ở boundary rõ ràng.

## 4. Virtual Thread thay đổi bottleneck, không xóa bottleneck

Virtual Thread giúp mô hình thread-per-request scale tốt hơn cho blocking I/O. Khi blocking, virtual thread thường được park và carrier thread chạy task khác. Nhưng mỗi task vẫn có:

- virtual-thread object và stack chunk;
- request/context/locals;
- buffers, futures và response state;
- một slot downstream khi thực sự gọi DB/API.

Nếu trước đây pool 200 platform thread vô tình giới hạn concurrency, chuyển sang hàng chục nghìn Virtual Thread có thể làm:

- hàng nghìn request chờ Hikari connection;
- timeout/retry storm;
- heap giữ hàng nghìn request graph;
- database bị overload;
- ThreadLocal value được nhân theo số virtual thread.

Do đó Virtual Thread loại bỏ “thread scarcity” chứ không tạo thêm capacity cho database.

## 5. Bounded concurrency với Virtual Threads

Giữ Virtual Thread cho code blocking dễ đọc, nhưng đặt admission control gần scarce resource:

```java
final class BoundedOrderRepository {
    private final Semaphore permits = new Semaphore(100);

    Order load(long id) throws InterruptedException {
        if (!permits.tryAcquire(200, TimeUnit.MILLISECONDS)) {
            throw new ServiceUnavailableException("database admission limit reached");
        }
        try {
            return blockingJdbcLoad(id);
        } finally {
            permits.release();
        }
    }
}
```

Con số permit phải khớp database capacity, Hikari pool, số replica service và các workload khác. Semaphore đặt cao hơn pool rất nhiều chỉ biến phần dư thành waiter trên heap. Nên fail fast/budget timeout thay vì chờ vô hạn.

## 6. HikariCP và memory

Connection pool nhỏ hơn số Virtual Thread là bình thường. Pool đại diện cho DB concurrency hữu ích, không phải số request đồng thời.

Phải quan sát:

- active/idle/pending connection;
- acquire timeout và query timeout;
- transaction duration;
- số waiter/in-flight request;
- heap retained bởi request đang chờ;
- DB CPU/IO/lock.

Không mở pool theo số request hoặc số Virtual Thread. Tổng connection = pool mỗi instance × số instance × các workload khác; database mới là constraint cuối.

## 7. Pinning và carrier starvation

Một Virtual Thread có thể không unmount được khỏi carrier trong một số blocking/native/monitor scenario tùy JDK và code. Pinning gây throughput/latency issue nhiều hơn là leak trực tiếp, nhưng backlog phát sinh sẽ giữ memory. Dùng JFR/thread dump để tìm tình huống pinning hoặc blocking lâu; không đoán từ CPU đơn thuần.

Không giữ lock qua network/database call. Nguyên tắc này tốt cho cả platform và virtual threads.

## 8. Context propagation

Context theo request gồm tracing, security, tenant và logging. Các lựa chọn:

- truyền explicit immutable context;
- framework-supported context propagation;
- Reactor Context cho reactive transaction/context;
- scoped value ở code Java phù hợp thay cho một số ThreadLocal use case.

Không capture toàn request object chỉ để lấy một correlation ID. Trích đúng dữ liệu nhỏ cần thiết.

## 9. Checklist concurrency-memory

- Tổng concurrency tối đa ở từng scarce resource là bao nhiêu?
- Queue/semaphore có timeout và metric không?
- Task capture object graph lớn nào?
- Cancellation có dừng downstream work không?
- Executor có shutdown và bounded queue/rejection policy?
- ThreadLocal/MDC được cleanup trên mọi path?
- Khi bật Virtual Threads, limiter cũ có biến mất không?
- Pool connection được tính theo DB capacity toàn cluster?
- Load test có downstream chậm/mất kết nối/retry storm?
