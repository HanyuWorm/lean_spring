# 04 — Cache, HTTP và Reactive Memory

## 1. Cache local là long-lived heap

`@Cacheable` không tự quy định capacity. Policy nằm ở cache provider. Với Caffeine/local cache, cần xem đồng thời:

- `maximumSize` hoặc `maximumWeight`;
- expire-after-write/access phù hợp freshness;
- key cardinality;
- kích thước value và graph mà value giữ;
- hit rate, eviction count/weight, load latency và load failure.

TTL một mình không chặn peak. Nếu trong 5 phút có 10 triệu key mới, tất cả có thể cùng tồn tại trước khi hết hạn.

### Size theo entry có thể đánh lừa

Một avatar 2 KB và report 50 MB đều tính là một entry với `maximumSize`. Dùng weight khi value size biến thiên mạnh, hoặc không giữ large object trong process.

### Cache key explosion

Key chứa timestamp, random ID, full query hoặc tổ hợp filter không chuẩn hóa có thể làm hit rate gần 0 nhưng memory tăng liên tục. Review key space như review database index.

### Cache stampede

Khi key nóng hết hạn, nhiều request cùng load value lớn. Single-flight/synchronized loading giảm duplicate load, nhưng cần timeout, failure policy và không được giữ request context trong loader. TTL nên jitter nếu nhiều key hết hạn cùng lúc.

### Negative cache

Cache “not found” trong TTL ngắn có thể bảo vệ database, nhưng key từ input tùy ý vẫn cần cardinality/rate limit.

## 2. Redis không làm application memory biến mất

Remote cache chuyển phần resident data ra process khác, nhưng application vẫn cấp phát cho:

- serialized request/response buffer;
- deserialized object graph;
- client command queue/pipeline;
- retry/backlog;
- local near-cache nếu có.

Large value gây network, latency và temporary allocation. Đừng lấy một blob 100 MB từ Redis rồi deserialize thành graph lớn và gọi đó là “off-heap”.

## 3. HTTP request/response buffering

Spring MVC với `byte[]`, `String`, `MultipartFile#getBytes()` hoặc object JSON lớn có thể materialize toàn body trên heap. Một request 100 MB × 50 concurrent requests đã vượt xa logic nhìn từ một request đơn.

Thiết kế:

- giới hạn request/body/multipart ở gateway và application;
- stream upload thẳng tới storage khi có thể;
- dùng pagination/chunked response cho tập dữ liệu lớn;
- không log toàn body;
- chống decompression bomb và đặt limit sau giải nén;
- giới hạn concurrency của endpoint nặng, không chỉ rate/second.

Khi export file, stream từng chunk tới output thay vì tạo `ByteArrayOutputStream` chứa toàn file. Cần xử lý client chậm/cancel để không tiếp tục sản xuất và giữ buffer.

## 4. WebFlux không đồng nghĩa constant memory

Reactive Streams cung cấp cơ chế demand/backpressure, nhưng application có thể vô hiệu hóa lợi ích bằng cách aggregate hoặc buffer:

- `collectList()` giữ toàn bộ sequence;
- `DataBufferUtils.join(...)` ghép body;
- `bodyToMono(byte[].class)` materialize body;
- `cache()`/`replay()` giữ signal theo policy;
- `onBackpressureBuffer()` không giới hạn tạo backlog;
- `groupBy()` với cardinality lớn giữ nhiều group;
- concurrency cao ở `flatMap` giữ nhiều in-flight item.

Phải đặt giới hạn ở operator, codec, request size và business concurrency. Backpressure trong một pipeline không tự giới hạn upstream bên ngoài như HTTP requests mới hoặc Kafka partitions nếu admission control không tồn tại.

## 5. Netty `DataBuffer` và direct memory

Trong WebFlux/Netty, buffer có thể pooled, reference-counted và nằm ngoài Java heap. Nếu code làm việc ở abstraction cao (`bodyToFlux`, codecs chuẩn), framework thường quản lý lifecycle. Khi tự retain/slice/store/release buffer, ownership phải rõ; thiếu release có thể gây direct-memory leak.

Nguyên tắc:

- không giữ `DataBuffer` sau callback nếu chưa chuyển ownership đúng cách;
- release khi discard/error/cancel nếu code trực tiếp sở hữu buffer;
- đặt codec max-in-memory cho trường hợp aggregate;
- quan sát direct buffer pool/RSS cùng heap;
- chỉ thay `MaxDirectMemorySize` sau khi xác định usage hợp lệ, không dùng để che leak.

## 6. Messaging và backlog

Kafka/RabbitMQ consumer có thể gây heap pressure khi:

- poll batch quá lớn;
- concurrency × batch size × message size quá cao;
- deserialize toàn payload trước khi xử lý;
- pause downstream nhưng vẫn nhận/buffer message;
- retry in-memory vô hạn;
- giữ failed payload/exception graph trong future/list.

Memory upper bound xấp xỉ:

```text
in-flight memory ≈ concurrency × batch size × expanded message size × pipeline stages
```

“Expanded” quan trọng: JSON nén 1 MB có thể thành object graph nhiều MB. DLQ/durable broker tốt hơn retry queue vô hạn trong heap.

## 7. Checklist bounded data path

- Cache có hard capacity và key cardinality đã biết?
- Payload có giới hạn trước và sau decompression?
- Có đoạn nào biến stream thành collection/byte array không?
- Concurrency endpoint nặng có semaphore/bulkhead?
- Queue/buffer/retry có giới hạn và behavior khi đầy?
- Direct buffer ownership có rõ ở success/error/cancel?
- Metric có bao gồm cache size/eviction, queue depth, in-flight và RSS?
- Load test có slow client, timeout, cancellation và downstream outage?
