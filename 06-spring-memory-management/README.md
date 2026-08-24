# Spring Memory Management Handbook

Handbook này dành cho senior Java/Spring Boot cần thiết kế, review và điều tra sự cố memory trong production. Mục tiêu không phải học thuộc tên GC, mà là trả lời được bốn câu hỏi:

1. Byte đang nằm ở đâu: heap, metaspace, thread stack, direct buffer hay native memory?
2. Byte được giữ bởi GC root nào và vòng đời của nó có đúng với nghiệp vụ không?
3. Hệ thống đang leak, allocation quá nhanh, giữ backlog quá lớn hay đơn giản là sizing sai?
4. Giới hạn nào bảo vệ JVM, database và container khi tải tăng đột biến?

## Mental model cốt lõi

```text
Container / process RSS
├── Java heap
│   ├── object sống ngắn: request DTO, JSON, temporary collection
│   └── live set: singleton, cache, session, managed entity, backlog
├── Metaspace: class metadata, proxy/generated class
├── Code cache: JIT-compiled code
├── Thread stacks: platform threads và carrier threads
├── Direct/off-heap buffers: Netty, NIO, compression, driver
└── JVM/GC/native libraries và memory-mapped regions
```

Vì vậy:

- Heap ổn định không chứng minh process memory ổn định.
- GC chỉ thu hồi object không còn reachable; GC không biết object nào “hết giá trị nghiệp vụ”.
- Memory leak trong Java thường là object vẫn reachable ngoài ý muốn, không phải object bị “quên free”.
- Tăng `-Xmx` chỉ giúp khi workload thực sự cần heap lớn hơn. Nó không sửa cache vô hạn, direct-buffer leak hay số thread quá lớn.
- `-Xmx` không được đặt bằng container limit. JVM còn cần native headroom.

## Bản đồ học tập

| Chương | Nội dung | Kết quả cần đạt |
|---|---|---|
| [01](01-jvm-memory-model-and-gc.md) | JVM memory, allocation, live set, GC | Phân biệt heap pressure, native pressure và leak |
| [02](02-spring-beans-lifecycle-and-leaks.md) | Bean scope, lifecycle, proxy, singleton leak | Review được vòng đời object trong Spring |
| [03](03-jpa-hibernate-memory.md) | Persistence context, dirty checking, batch, OSIV | Không OOM khi xử lý data lớn |
| [04](04-cache-http-reactive-memory.md) | Cache, HTTP payload, WebFlux/DataBuffer | Thiết kế bounded memory cho data path |
| [05](05-threads-async-virtual-threads.md) | ThreadLocal, async, queue, Virtual Threads | Giữ concurrency và backlog trong budget |
| [06](06-containers-sizing-observability.md) | Container sizing, metrics, alert | Lập memory budget và phát hiện sớm |
| [07](07-troubleshooting-playbook.md) | OOM taxonomy, JFR, heap dump, NMT | Điều tra sự cố có bằng chứng |
| [08](08-interview-questions.md) | Câu hỏi basic đến architect/incident | Tự kiểm tra và luyện phỏng vấn |

## 12 điểm phải nhớ

1. Đo `used`, `committed`, `max` và RSS; đừng chỉ nhìn một biểu đồ heap percent.
2. Leak được nhận biết rõ hơn qua **post-GC live set tăng dần**, không phải saw-tooth bình thường.
3. Allocation rate cao gây GC pressure dù live set không tăng.
4. Singleton Spring có vòng đời gần bằng `ApplicationContext`; mọi field của singleton đều có khả năng trở thành long-lived state.
5. Cache local phải có capacity, expiration và metric. TTL một mình không chặn được peak cardinality.
6. Hibernate persistence context là first-level cache và dirty-checking workspace; `flush()` không phải `clear()`.
7. Một transaction khổng lồ giữ entity, snapshot, connection và undo/lock lâu hơn.
8. Executor queue vô hạn là một memory leak được thiết kế sẵn khi producer nhanh hơn consumer.
9. Virtual Thread rẻ hơn platform thread, không miễn phí; nó làm concurrency dễ tăng tới mức DB pool, downstream và heap bị tràn.
10. WebFlux có backpressure nhưng các operator như `collectList`, `cache`, `replay` hoặc buffer vô hạn vẫn có thể OOM.
11. Pod có thể bị `OOMKilled` mà JVM không ném `OutOfMemoryError` nếu RSS chạm cgroup limit.
12. Heap dump chứa token, PII và dữ liệu nghiệp vụ; phải lưu, truyền và xóa như dữ liệu nhạy cảm.

## Lộ trình thực hành 7 ngày

- Ngày 1: đọc chương 01, quan sát heap saw-tooth và post-GC baseline bằng JFR.
- Ngày 2: đọc chương 02, audit singleton fields, static collections, listeners và `ThreadLocal`.
- Ngày 3: đọc chương 03, viết batch import có `flush/clear`, so sánh với `saveAll` một triệu record.
- Ngày 4: đọc chương 04, đặt limit cho cache, upload, codec buffer và message batch.
- Ngày 5: đọc chương 05, chuyển một blocking workload sang Virtual Threads nhưng giữ semaphore bảo vệ DB.
- Ngày 6: đọc chương 06-07, tạo memory budget, JFR và heap dump trên môi trường test.
- Ngày 7: trả lời chương 08 không nhìn đáp án; với mỗi câu phải nêu metric và bằng chứng cần thu thập.

## Cheat sheet khi có incident

```text
Heap sau GC tăng?       -> heap dump, histogram, GC roots, retained size
Heap saw-tooth ổn định? -> xem allocation rate / pause; có thể chỉ là churn
Heap ổn nhưng RSS tăng? -> NMT, direct buffer, thread count, native library
Pod OOMKilled?          -> cgroup/container limit và RSS; JVM có thể chưa kịp OOME
Thread count tăng?      -> thread dump, executor lifecycle, stack size, blocked tasks
Batch job OOM?          -> persistence context, collection aggregation, page/batch size
WebFlux OOM?            -> pooled buffer, collect/join/cache/replay, demand và queue
Sau deploy mới tăng?    -> compare class histogram/JFR trước-sau, classloader/proxy/cache
```

Nguồn chính thức và version baseline được ghi tại [SOURCES.md](SOURCES.md).
