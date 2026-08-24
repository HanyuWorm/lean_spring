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

### 1. Vì sao chuyển sang Virtual Threads có thể làm database sập nhanh hơn?

**Trả lời:** Virtual Threads bỏ giới hạn ngầm do platform-thread pool tạo ra, nên hàng nghìn request có thể đồng thời đi tới đoạn JDBC. Database không trở nên nhanh hơn: CPU, IOPS, locks và `max_connections` vẫn hữu hạn. Hikari chỉ giới hạn số connection đang được mượn; hàng nghìn virtual thread còn lại vẫn có thể xếp hàng chờ connection, giữ request payload/context, hết deadline rồi retry, tạo retry amplification và tail latency rất lớn.

Phải giới hạn concurrency theo capacity đã đo, đặt `connectionTimeout` nhỏ hơn deadline còn lại, dùng admission control/load shedding, theo dõi Hikari pending/acquire time, DB saturation và retry rate. Virtual Threads tăng khả năng tạo concurrency, không tăng capacity downstream.

### 2. Khi nào cần semaphore nếu đã có Hikari connection pool?

**Trả lời:** Hikari là last-line bound cho **connection**, còn semaphore là admission/bulkhead cho **đơn vị công việc**. Dùng semaphore khi muốn từ chối/chờ có giới hạn trước khi request thực hiện parse, gọi service khác rồi mới đứng ở pool; khi cần chia quota giữa endpoint/tenant; hoặc một use case có nhiều DB round-trip nhưng chỉ nên có N workflow đồng thời.

Không thêm semaphore mù quáng trùng đúng kích thước pool. Permits phải dựa trên DB budget và connection hold time; acquire có deadline, release trong `finally`, metric queue/deny rõ. Thứ tự acquire thống nhất để tránh giữ connection trong khi chờ permit khác.

### 3. `ThreadLocal` có vấn đề gì khi số virtual thread rất lớn?

**Trả lời:** `ThreadLocal` vẫn hoạt động, nhưng mỗi virtual thread thường phục vụ một task và số lượng có thể rất lớn. Giá trị per-thread, đặc biệt buffer/client/connection đắt, bị nhân theo số virtual thread và không còn được amortize như fixed thread pool; `InheritableThreadLocal` còn có thể copy context ngoài ý muốn. Nó làm memory/allocation tăng và che dependency/lifecycle.

Dùng `ThreadLocal` nhỏ, thật sự thread-confined và luôn cleanup nếu framework tái sử dụng context. Không dùng để pool resource. Với context immutable theo call tree, ưu tiên `ScopedValue`; với business dependency, truyền tham số hoặc inject rõ. Có thể dùng `-Djdk.traceVirtualThreadLocals=true` khi điều tra migration.

### 4. Deadline propagation khác từng HTTP client timeout độc lập thế nào?

**Trả lời:** Deadline là thời điểm tuyệt đối chung cho toàn request. Mỗi hop/subtask tính `remaining = deadline - now`, đặt timeout không vượt remaining, cancel work không còn giá trị và chừa budget để compose/serialize response. Nếu mỗi client tự có timeout 2 giây, ba call tuần tự và retry có thể dùng 6–12 giây dù upstream chỉ cho 3 giây; work tiếp tục sau khi caller đã bỏ cuộc.

Per-hop timeout vẫn cần cho connect/read/write, nhưng phải được cắt theo deadline chung và retry budget. Propagate deadline qua request context/header có kiểm soát, không tin deadline tùy ý từ client bên ngoài mà không clamp theo server policy.

### 5. Vì sao không nên dùng preview API xuyên suốt domain/application layer?

**Trả lời:** Preview API yêu cầu `--enable-preview` khi compile và run, có thể đổi signature/semantics hoặc bị rút ở release sau. Nếu type preview xuất hiện trong domain/public contracts, toàn bộ module, consumer, test, build image và upgrade cadence bị khóa theo JDK đó; migration chạm diện rộng.

Nếu lợi ích đáng giá, cô lập preview API sau port/adapter nhỏ và giữ domain contract bằng type ổn định. Có fallback, compatibility test và ADR ghi JDK/version/removal plan. Production vẫn có thể dùng preview, nhưng blast radius phải chủ động và có bằng chứng.

### 6. Sau JEP 491, bạn còn lý do gì để thay `synchronized` bằng `ReentrantLock`?

**Trả lời:** Trên Java 24+, không còn nên đổi chỉ để tránh monitor pinning: JEP 491 cho virtual thread unmount trong gần như mọi trường hợp `synchronized`. `ReentrantLock` vẫn cần khi yêu cầu fairness, `tryLock`, timed/interruptible acquisition, nhiều `Condition`, lock vượt lexical scope hoặc instrumentation/algorithm cần API lock tường minh. Read/write hoặc optimistic semantics dùng lock type khác phù hợp.

Nếu chỉ cần mutual exclusion lexical đơn giản, `synchronized` ngắn gọn và ít lỗi quên unlock hơn. Dù chọn loại nào, thu hẹp critical section và tránh IO khi giữ lock. Trên Java 21–23, pinning do monitor vẫn là lý do runtime-specific để cân nhắc `ReentrantLock`; JEP 491 không sửa pinning hiếm do native/foreign callback.

Nguồn: [JEP 444 - Virtual Threads](https://openjdk.org/jeps/444), [JEP 491 - Synchronize Virtual Threads without Pinning](https://openjdk.org/jeps/491), [Oracle - Virtual Threads](https://docs.oracle.com/en/java/javase/25/core/virtual-threads.html), [Oracle - Scoped Values](https://docs.oracle.com/en/java/javase/25/core/scoped-values.html), [Oracle - Structured Concurrency](https://docs.oracle.com/en/java/javase/25/core/structured-concurrency.html).
