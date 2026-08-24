# 07 — Production Memory Troubleshooting Playbook

## Nguyên tắc

Ưu tiên SLO và an toàn dữ liệu, nhưng thu bằng chứng trước restart khi điều kiện cho phép. Restart là containment hợp lệ; nó không phải root-cause analysis. Đừng trigger full GC hoặc heap dump trên instance critical mà chưa đánh giá pause, disk và I/O impact.

## Bước 1 — Phân loại sự cố

Hỏi ngay:

1. JVM log có `OutOfMemoryError` không?
2. Container/pod có `OOMKilled`, exit code 137 hoặc cgroup event không?
3. Heap sau GC tăng, hay heap ổn nhưng RSS tăng?
4. Thread/class/direct buffer/queue/cache nào tăng cùng thời điểm?
5. Có deploy, traffic spike, batch job hoặc downstream slowdown nào trùng thời gian?

| Triệu chứng | Hướng đầu tiên |
|---|---|
| `Java heap space` | heap histogram/dump, live set, payload/cache/batch |
| `GC overhead limit` | live set gần max, allocation rate, GC log |
| `Metaspace` | class count, classloader, dynamic proxy/context |
| `Direct buffer memory` | NMT, buffer pool, Netty/NIO ownership |
| `unable to create native thread` | thread count/dump, OS limits, native headroom |
| `OOMKilled` không có Java OOME | RSS/cgroup, native/direct/thread, container sizing |

## Bước 2 — Ổn định hệ thống

Tùy incident:

- load-shed/rate-limit endpoint nặng;
- pause batch/consumer hoặc giảm concurrency;
- disable feature/cache population mới bằng flag;
- scale out chỉ khi mỗi replica có memory ổn định và downstream chịu được;
- rolling restart instance xấu để tránh toàn cụm restart đồng thời.

Scale out không sửa leak per-instance; nó chỉ kéo dài thời gian tới failure và có thể nhân connection/cache tổng.

## Bước 3 — Ghi lại timeline và evidence

- phiên bản app/JDK/config/flags;
- deploy/config/traffic event;
- heap, RSS, GC pause/CPU, allocation, threads, direct buffer;
- cache size, queue depth, Hikari pending, consumer lag;
- error/timeout/retry rate;
- pod event và node memory pressure.

So sánh với một instance khỏe cùng version nếu có.

## Bước 4 — Lấy diagnostic artifact

Các lệnh HotSpot phổ biến:

```text
jcmd <pid> VM.flags
jcmd <pid> GC.heap_info
jcmd <pid> GC.class_histogram
jcmd <pid> Thread.print
jcmd <pid> GC.heap_dump /dumps/app.hprof

jcmd <pid> JFR.start name=memory settings=profile duration=10m filename=/dumps/memory.jfr
jcmd <pid> JFR.check
```

Heap dump tạo file xấp xỉ lớn theo heap live/used và có thể gây pause/I/O. Xác minh disk và security trước.

Nếu nghi native memory và NMT đã bật từ startup:

```text
jcmd <pid> VM.native_memory summary
jcmd <pid> VM.native_memory baseline
# chờ trong khi tái hiện workload
jcmd <pid> VM.native_memory summary.diff
```

NMT cho biết JVM internal native categories, không bảo đảm theo dõi allocation native bên ngoài JVM.

## Bước 5 — Đọc heap dump đúng cách

Thứ tự gợi ý trong Eclipse MAT/YourKit/JProfiler:

1. Leak suspects chỉ là gợi ý, không phải kết luận.
2. Xem class histogram: class nào tăng về count/bytes?
3. Xem dominator tree theo retained size.
4. Với suspect, xem path to GC roots và bỏ/giữ weak references theo mục đích phân tích.
5. Tìm owner thuộc application: cache, singleton, session, queue, thread local, persistence context.
6. So sánh hai dump cùng workload ở hai thời điểm để tìm growth.

Ví dụ diễn giải:

```text
ConcurrentHashMap retained 3.2 GB
  <- ProductCache singleton
  <- ApplicationContext
```

Kết luận không phải “HashMap leak”; phải hỏi policy nào cho phép cache key tăng, entry size bao nhiêu, vì sao eviction không chạy.

## Bước 6 — Phân biệt bốn nguyên nhân

### Leak/retention sai

Post-GC live set tăng theo thời gian, dominator/path-to-root chỉ ra collection/context giữ object ngoài ý muốn.

### Peak hợp lệ nhưng sizing sai

Live set tăng khi batch/traffic peak rồi giảm sau khi workload kết thúc. Cần chunk/concurrency limit hoặc thêm memory có căn cứ.

### Allocation pressure

Heap đáy ổn nhưng allocation rate/young GC/CPU cao. Tìm allocation hot spot bằng JFR; giảm intermediate object, serialization và copy.

### Native/off-heap pressure

Heap ổn nhưng RSS/direct/metaspace/thread tăng. Dùng NMT, buffer metric, thread/class count và native profiler nếu NMT không đủ.

## Bước 7 — Các playbook cụ thể

### Nightly Hibernate batch OOM

1. Đo số managed entity và heap theo record count.
2. Tìm `findAll`, input list lớn, `saveAll`, transaction toàn job.
3. Chuyển sang chunk/page; flush + clear; cân nhắc transaction per chunk.
4. Đo DB batching, lock/rollback semantics và idempotency.

### Heap ổn, pod vẫn OOMKilled

1. Xác nhận pod reason/cgroup memory peak.
2. So RSS với heap committed/used.
3. Kiểm tra direct buffer, thread count/stacks, metaspace và NMT.
4. Chỉnh leak/capacity hoặc giảm Xmx để tạo headroom; không kết luận chỉ từ heap dashboard.

### Sau khi bật Virtual Threads, latency rồi memory tăng

1. Xem request in-flight và Hikari pending.
2. Xác nhận limiter vô tình bị loại bỏ khi bỏ fixed pool.
3. Đặt semaphore/timeout theo DB/downstream capacity.
4. Audit ThreadLocal và task payload; test downstream chậm.

### WebFlux direct memory tăng

1. Xem direct buffer pool/RSS và Netty leak diagnostics phù hợp môi trường test.
2. Tìm manual `DataBuffer` retain/slice/store, error/cancel path.
3. Tìm body aggregation/operator buffering không bounded.
4. Sửa ownership/streaming/limit rồi soak test cả cancellation.

### Metaspace tăng sau mỗi reload/deploy

1. Theo dõi loaded/unloaded class count.
2. Heap dump để xem classloader bị giữ bởi thread, ThreadLocal, driver, listener hoặc static registry.
3. Đóng context/executor và unregister resource đúng lifecycle.
4. Soak qua nhiều reload cycle.

## Bước 8 — Xác minh fix

Fix chỉ được coi là hoàn tất khi:

- test tái hiện trước fix và không tái hiện sau fix;
- post-GC live set/RSS đạt plateau qua soak test đủ dài;
- overload có bounded behavior thay vì queue tăng vô hạn;
- latency/throughput và DB/downstream vẫn đạt SLO;
- alert/runbook được bổ sung;
- dump/recording nhạy cảm đã được xóa theo policy.

## Mẫu incident conclusion

```text
Symptom: pod OOMKilled sau 6 giờ, heap dùng 55% limit.
Evidence: RSS tăng 90 MB/giờ; direct buffer pool tăng cùng tốc độ;
          heap post-GC ổn định; NMT/Netty trace chỉ ra buffer path X.
Root cause: cancellation path không release pooled DataBuffer.
Containment: giới hạn upload concurrency và rolling restart.
Fix: release buffer trên discard/error/cancel; thêm request size limit.
Proof: soak test 12 giờ, RSS plateau, zero leak report, SLO giữ nguyên.
Prevention: direct-memory/RSS slope alert và cancellation regression test.
```
