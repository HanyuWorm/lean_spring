# Chặng 1 - Java Core Evolution

Thời lượng: tuần 1-4. Production baseline là Java 21; dùng Java 25 riêng cho phần Scoped Values và Structured Concurrency preview.

## 1. Domain modeling bằng type system

Học và thực hành:

- records cho immutable value object/DTO, kèm validation ở compact constructor;
- sealed interface để đóng tập trạng thái hoặc command hợp lệ;
- pattern matching cho `switch` và exhaustive handling;
- Sequenced Collections khi nghiệp vụ phụ thuộc phần tử đầu/cuối và thứ tự encounter.

Lab: refactor order/payment state từ `String`/enum rời rạc thành sealed hierarchy. Test phải chứng minh thêm subtype mới sẽ buộc compiler hoặc test chỉ ra nhánh còn thiếu.

Điểm senior cần nói được: type system giúp loại bỏ invalid states, nhưng entity JPA/proxy/lazy loading có những ràng buộc khiến record không phải lựa chọn mặc định cho mọi model.

## 2. Virtual Threads

Mental model:

```text
request -> virtual thread -> blocking I/O -> unmount carrier
                           -> CPU work     -> vẫn chiếm carrier/core
                           -> DB call      -> vẫn cần connection hữu hạn
```

Virtual Threads giảm chi phí giữ thread khi chờ I/O; chúng không tạo thêm CPU, database connection hoặc downstream capacity. Vì vậy thiết kế mới phải chuyển từ “thread pool là backpressure” sang giới hạn rõ ràng bằng connection pool, semaphore, rate limiter, bulkhead và queue hữu hạn.

Lab bắt buộc với `07-virtual-threads-system-design`:

1. chạy cùng workload với platform threads và virtual threads;
2. giữ Hikari pool nhỏ hơn concurrency;
3. đo p95/p99, thời gian chờ connection và số request in-flight;
4. thêm admission control rồi so sánh tail latency;
5. chèn một đoạn CPU-bound để chứng minh Virtual Threads không cải thiện CPU saturation.

### Pinning theo phiên bản

- Java 21: block khi giữ monitor `synchronized` có thể pin carrier; dùng JFR/diagnostic để tìm hot path thực tế.
- Java 24+: JEP 491 cho phép unmount trong hầu hết trường hợp monitor, vì vậy chọn `synchronized` hay `java.util.concurrent.locks` theo semantics và contention, không theo slogan.
- Native/JNI/FFM và thư viện cũ vẫn cần profiling; không suy đoán từ source code đơn lẻ.

## 3. Scoped Values và Structured Concurrency

Scoped Values phù hợp với context immutable, có lifetime giới hạn theo call tree; không coi đây là nơi chứa mọi request state. Trên Java 25, Scoped Values đã trở thành permanent feature.

Structured Concurrency tổ chức các subtask theo cùng lifetime của request, hỗ trợ reasoning về cancellation, failure và observability. Tuy nhiên `StructuredTaskScope` vẫn là preview; API đã đổi qua nhiều preview nên production code cần adapter boundary.

PoC: API tổng hợp song song `customer`, `inventory`, `pricing` với:

- một deadline chung;
- cancel siblings khi nhánh bắt buộc thất bại;
- partial result chỉ cho nhánh được business cho phép;
- correlation/tenant context qua Scoped Value;
- test không để orphan task sau khi request kết thúc.

## 4. Profiling thay vì phỏng đoán

Thu thập ít nhất:

- JFR recording trong workload I/O-bound và CPU-bound;
- thread dump có virtual threads;
- allocation rate, GC pause và CPU flame graph;
- latency histogram, không chỉ average.

## Câu hỏi phỏng vấn

1. Vì sao chuyển sang Virtual Threads có thể làm database sập nhanh hơn?
2. Khi nào cần semaphore nếu đã có Hikari connection pool?
3. `ThreadLocal` có vấn đề gì khi số virtual thread rất lớn?
4. Deadline propagation khác từng HTTP client timeout độc lập thế nào?
5. Vì sao không nên dùng preview API xuyên suốt domain/application layer?
6. Sau JEP 491, bạn còn lý do gì để thay `synchronized` bằng `ReentrantLock`?

Nguồn: [Oracle - Virtual Threads](https://docs.oracle.com/en/java/javase/25/core/virtual-threads.html), [Oracle - Scoped Values](https://docs.oracle.com/en/java/javase/25/core/scoped-values.html), [Oracle - Structured Concurrency](https://docs.oracle.com/en/java/javase/25/core/structured-concurrency.html).

