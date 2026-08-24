# 01 — JVM Memory Model và Garbage Collection

## 1. Không đồng nhất heap với toàn bộ memory

Các vùng quan trọng của một Spring Boot process:

| Vùng | Chứa gì | Triệu chứng khi có vấn đề |
|---|---|---|
| Heap | object Java, bean, DTO, collection, entity | `Java heap space`, GC dày, latency tăng |
| Metaspace | metadata của class, proxy, generated class | `Metaspace`, class count/classloader tăng |
| Code cache | machine code do JIT sinh | compilation bị hạn chế, performance giảm |
| Thread stack | frame, local variable, native call | nhiều platform thread làm RSS tăng; `unable to create native thread` |
| Direct memory | NIO/Netty buffer, driver buffer | `Direct buffer memory`, RSS tăng trong khi heap bình thường |
| Native/JVM | GC structure, libc, TLS, agent, library | container bị kill nhưng heap còn dư |

Ba khái niệm không được trộn lẫn:

- **Reserved**: address space JVM đã dành, chưa chắc đã dùng RAM vật lý.
- **Committed**: memory JVM có thể dùng mà không xin thêm từ OS.
- **Used**: phần đang chứa dữ liệu tại thời điểm đo.

RSS của process gần với memory vật lý đang resident, nhưng cũng không hoàn toàn bằng tổng `used` của các JVM pool. Khi chạy trong container, cgroup accounting mới là ranh giới sống còn.

## 2. Allocation rate, live set và retention

Ví dụ mỗi request tạo 5 MB object tạm, phục vụ 100 request/s:

```text
allocation rate ≈ 500 MB/s
```

Nếu object chết nhanh, heap vẫn có thể không leak nhưng young GC sẽ chạy nhiều. Ngược lại, một cache chỉ tăng 2 MB/phút có allocation rate thấp nhưng sau vài giờ làm live set tăng tới OOM.

- **Allocation rate**: số byte được cấp phát mỗi đơn vị thời gian.
- **Live set**: object còn sống sau một full/concurrent collection đáng tin cậy.
- **Retention**: vì sao object vẫn reachable, thường được phân tích qua path to GC root.
- **Churn**: cấp phát rồi bỏ rất nhanh; gây CPU/GC pressure nhưng không nhất thiết leak.

Biểu đồ heap bình thường có dạng răng cưa. Dấu hiệu đáng ngờ là đáy sau GC tăng dần dưới workload tương đương.

## 3. Reachability và GC root

GC đi từ các root như static field, live thread, JNI reference và các JVM internal root. Object reachable qua chuỗi strong reference sẽ không bị thu hồi.

```text
GC Root: application thread
  -> ThreadLocalMap
    -> MDC/request context
      -> request DTO
        -> byte[50 MB]
```

GC hoạt động đúng nhưng object vẫn sống: đây là logical leak. Circular reference không tự tạo leak; một cycle không còn reachable từ root vẫn được GC thu hồi.

### Shallow size và retained size

- **Shallow size**: kích thước chính object đó.
- **Retained size**: tổng memory có thể được giải phóng nếu object này biến mất và không còn đường giữ khác.

Một `HashMap` shallow size nhỏ nhưng retained size vài GB mới là suspect thực sự. Trong heap analyzer, ưu tiên dominator tree và retained size, sau đó xem path to GC roots.

## 4. Generational hypothesis

Phần lớn object sống rất ngắn, nên collector tối ưu bằng cách thu gom young generation thường xuyên và xử lý object sống lâu ở old generation. Spring request DTO, JSON token và temporary collection thường chết trẻ; singleton/cache/session/entity bị giữ lâu sẽ tiến vào vùng old.

Tối ưu quan trọng hơn micro-tuning GC:

- không tạo graph dữ liệu lớn không cần thiết;
- stream/page thay vì materialize toàn bộ;
- bound queue và cache;
- giảm thời gian giữ reference;
- chọn đúng scope và transaction boundary.

## 5. Chọn GC như một trade-off

| Collector | Mục tiêu chính | Khi cân nhắc |
|---|---|---|
| G1 | Cân bằng throughput và pause; mặc định phổ biến trên server | Điểm bắt đầu tốt cho đa số Spring service |
| Parallel GC | Throughput cao, stop-the-world dài hơn | Batch job chấp nhận pause |
| ZGC | Pause rất thấp với phần lớn công việc concurrent | Heap lớn/latency-sensitive, chấp nhận trade-off throughput và test kỹ |

Không chọn collector chỉ vì “mới nhất”. Bắt đầu từ default, xác định SLO pause/throughput, đo workload thật rồi mới đổi. G1 đặt pause goal nhưng không phải real-time guarantee.

### Heap lớn không luôn tốt hơn

Heap lớn có thể giảm tần suất collection, nhưng:

- tăng memory cost và thời gian dump/analyze;
- để ít headroom cho direct/native/thread memory;
- có thể làm thời gian recovery hoặc một số phase dài hơn;
- che giấu leak lâu hơn thay vì sửa nguyên nhân.

Sizing phải dựa trên live set ở peak, allocation rate, pause SLO và native headroom.

## 6. Các loại lỗi thường gặp

### `OutOfMemoryError: Java heap space`

Heap không còn chỗ cho allocation. Có thể do leak, peak hợp lệ vượt sizing, payload/batch quá lớn hoặc allocation pressure khiến collector không kịp.

### `GC overhead limit exceeded`

JVM dành gần hết thời gian GC nhưng thu hồi quá ít. Thường là heap gần đầy bởi live object; tăng heap chỉ là containment nếu retention sai.

### `OutOfMemoryError: Metaspace`

Kiểm tra số class/classloader, dynamic code generation, repeated `ApplicationContext`, hot reload/agent. Metaspace leak thường là classloader còn reachable.

### `OutOfMemoryError: Direct buffer memory`

Kiểm tra NIO/Netty buffer, pooling, reference-counted buffer, request aggregation và native memory. Heap dump không thể hiện toàn bộ payload off-heap.

### `unable to create native thread`

Có thể hết native memory, chạm OS/process thread limit hoặc tạo thread vô hạn. Kiểm tra thread count và dump trước khi chỉ giảm `-Xss`.

### Container `OOMKilled`

Kernel/cgroup kill process khi tổng memory vượt limit. JVM có thể không kịp tạo heap dump và log không có `OutOfMemoryError`.

## 7. Nguyên tắc tuning

1. Định nghĩa SLO: throughput, p95/p99 latency, maximum pause.
2. Dùng production-like workload và data shape.
3. Đo live set, allocation rate, GC CPU, pause và RSS.
4. Sửa retention/backlog/payload trước khi thay collector.
5. Thay một biến mỗi lần và giữ kết quả benchmark/JFR.
6. Chừa native headroom thay vì dùng toàn bộ container limit cho `-Xmx`.

Một cấu hình log GC ban đầu trên JDK hiện đại:

```text
-Xlog:gc*:file=/logs/gc.log:time,uptime,level,tags:filecount=10,filesize=20M
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/dumps
```

`HeapDumpPath` cần đủ disk, writable và được bảo vệ vì dump có thể chứa secret/PII.
