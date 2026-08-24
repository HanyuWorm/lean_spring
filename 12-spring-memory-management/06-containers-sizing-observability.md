# 06 — Container Sizing và Observability

## 1. Lập memory budget thay vì chỉ đặt `-Xmx`

Một budget ban đầu:

```text
container limit
= Java heap
+ metaspace
+ code cache
+ thread stacks
+ direct/NIO/Netty/driver buffers
+ GC/JIT/JVM/native libraries
+ safety headroom cho peak và sai số đo
```

Không có tỷ lệ heap/container đúng cho mọi service. WebFlux gateway nhiều direct buffer, MVC service nhiều platform thread và batch JPA có profile khác nhau. Hãy đo representative load bằng RSS, Native Memory Tracking (NMT), thread count và buffer metrics rồi mới chốt.

### Vì sao `-Xmx = pod memory limit` nguy hiểm?

Heap có thể hợp lệ dưới `Xmx`, nhưng native parts làm tổng RSS vượt cgroup limit. Kernel kill process; JVM không có cơ hội ném OOME hoặc dump heap.

### `MaxRAMPercentage`

Container-aware JVM có thể tính heap theo available container memory. Đây là điểm bắt đầu, không thay thế budget. Cùng percentage nhưng một service 256 MB và 16 GB có headroom requirement tuyệt đối rất khác.

## 2. Right-sizing theo workload

Thu thập dưới load ổn định và peak:

- post-GC heap/live set;
- allocation rate và GC CPU/pause;
- metaspace/class count;
- direct buffer usage;
- live/peak thread count;
- process RSS/cgroup current;
- Hikari pending/active;
- queue depth/in-flight request;
- cache estimated size/weight và eviction.

Heap budget phải đủ cho live set peak + allocation slack để GC làm việc hiệu quả. Nếu đặt quá sát live set, collector chạy liên tục dù về lý thuyết object cuối cùng có thể chết.

## 3. Metric cần có

Tên metric cụ thể phụ thuộc JDK, Micrometer version và registry, nhưng dashboard cần các nhóm:

| Nhóm | Câu hỏi trả lời |
|---|---|
| Heap used/committed/max theo pool | old/live set có tăng? headroom còn bao nhiêu? |
| GC pause/count/time | latency do GC hay không? GC chiếm bao nhiêu CPU? |
| Allocation/promotion | object churn hay long-lived promotion tăng? |
| Non-heap/metaspace/classes | classloader/generated class có tăng? |
| Buffer pool/direct memory | heap ổn nhưng off-heap tăng? |
| Thread live/peak/states | thread leak, waiter hay blocking? |
| Process RSS/cgroup usage | còn bao xa tới container kill? |
| Cache/queue/in-flight | nguyên nhân ứng dụng nào đang giữ data? |
| Hikari/downstream latency | backlog có đến từ scarce dependency? |

Metric JVM chỉ cho biết triệu chứng. Metric business/cardinality như số active session, cache entry, upload in-flight, consumer lag và job batch size thường chỉ thẳng nguyên nhân.

## 4. Alert có tín hiệu tốt

Tránh alert chỉ vì heap used tức thời đạt 85%; saw-tooth có thể chạm cao rồi GC bình thường. Kết hợp:

- post-GC old/live-set slope tăng liên tục;
- RSS/cgroup limit ratio cao kéo dài;
- GC CPU hoặc pause p99 vượt SLO;
- allocation/promotion thay đổi đột ngột sau deploy;
- direct buffer/metaspace/thread count tăng đơn điệu;
- queue depth/consumer lag/in-flight tăng;
- Hikari pending cùng request latency tăng.

Alert phải có deploy marker và workload context để phân biệt leak với traffic growth.

## 5. Spring Boot Actuator

Các endpoint/metric hữu ích:

- `metrics` và Prometheus scrape cho JVM/process/application metrics;
- `threaddump` để xem thread state;
- `heapdump` trên HotSpot để lấy HPROF.

Không expose rộng các endpoint nhạy cảm. Heap dump có thể rất lớn, làm pause/I/O pressure và chứa password, token, request body, PII. Chỉ bật qua management network, authentication/authorization chặt và quy trình lưu trữ/xóa rõ ràng.

## 6. Startup diagnostics

Cấu hình production-like tham khảo:

```text
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/dumps
-Xlog:gc*:file=/logs/gc.log:time,uptime,level,tags:filecount=10,filesize=20M
```

Tùy chiến lược recovery có thể cân nhắc exit sau OOME để orchestrator thay instance. Đây là operational decision: dump cần hoàn tất, readiness phải hạ đúng lúc và workload phải idempotent để retry an toàn.

NMT cần bật từ startup:

```text
-XX:NativeMemoryTracking=summary
```

NMT có overhead và không thấy mọi allocation của third-party native code, nên bật theo policy/diagnostic environment và đo overhead.

## 7. Kubernetes/container checklist

- Limit có chừa native headroom không?
- Request có phản ánh working set để scheduler đặt pod hợp lý?
- Dump/log volume có đủ chỗ và quyền ghi?
- Khi pod bị kill, có lưu event/reason/exit code và cgroup metric?
- Rolling restart có tránh dồn tải sang pod còn lại rồi cascade OOM?
- Autoscaling dùng CPU đơn thuần có che memory per-instance tăng?
- Readiness có loại instance đang thrash GC khỏi traffic?
- Capacity plan đã nhân pool/cache/memory theo số replica?

## 8. Ví dụ budget — chỉ để minh họa

Với pod limit 2 GiB, không mặc định đặt heap 2 GiB. Có thể bắt đầu test với:

```text
heap                         1.2 GiB
metaspace + code cache       0.2 GiB
threads + direct + native    0.35 GiB
safety headroom              0.25 GiB
```

Đây không phải công thức chuẩn. Nếu đo thấy direct buffer 500 MiB hoặc service cần live set 1.4 GiB, phải đổi architecture/configuration hoặc pod size. Sizing đáng tin cậy luôn đến từ load profile thật.
