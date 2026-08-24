# 08 — Câu hỏi Memory từ Basic đến Architect

Cách luyện: trả lời theo cấu trúc **definition → mechanism → production impact → evidence/metric → mitigation**. Câu trả lời senior không dừng ở định nghĩa; phải nói được trade-off và cách chứng minh.

## Level 1 — Foundation

### 1. Heap và stack khác nhau thế nào?

**Trả lời:** Heap chứa phần lớn object Java và được GC quản lý. Mỗi platform thread có stack chứa frame của method, local variable/reference và trạng thái call. Object được reference bởi local variable vẫn nằm trên heap theo mô hình quan sát thông thường; JIT có thể scalar-replace/escape-optimize nhưng đó là implementation optimization. Nhiều thread có thể gây native-memory pressure dù heap còn dư.

**Senior signal:** nhắc thêm RSS/native stack và không nói đơn giản “primitive ở stack, object ở heap” như một luật tuyệt đối.

### 2. Young generation và old generation để làm gì?

**Trả lời:** Chúng khai thác giả thuyết phần lớn object chết trẻ. Object mới thường được allocate ở young; object sống qua collection có thể được promote. Long-lived singleton/cache/session/entity state làm old/live set tăng. Chi tiết vùng phụ thuộc collector, nên không áp mô tả của collector cũ cho mọi JVM.

### 3. GC xác định object chết bằng cách nào?

**Trả lời:** GC tìm object reachable từ GC roots như live threads, static/JNI references và JVM roots. Object không reachable mới có thể thu hồi. Reference counting không phải nền tảng chung của HotSpot GC nên cycle không tự gây leak.

### 4. Memory leak trong Java nghĩa là gì khi đã có GC?

**Trả lời:** Object không còn cần cho nghiệp vụ nhưng vẫn reachable, ví dụ singleton map không eviction hoặc ThreadLocal trên pool thread. GC không thể biết semantic lifetime. Dấu hiệu là post-GC live set tăng và path-to-root chỉ ra owner giữ object.

### 5. `used`, `committed`, `max` khác gì?

**Trả lời:** `used` đang chứa data; `committed` JVM đã bảo đảm có thể dùng; `max` là trần của pool nếu có. RSS là góc nhìn process/OS và còn gồm non-heap/native. Không kết luận leak chỉ từ committed cao vì JVM không nhất thiết trả memory ngay cho OS.

### 6. `-Xms` và `-Xmx` là gì?

**Trả lời:** Chúng đặt initial/minimum-style heap target và maximum heap. Đặt bằng nhau có thể làm behavior dễ dự đoán nhưng reserve/commit và container cost phải đo. `Xmx` không giới hạn tổng RSS.

### 7. Metaspace chứa gì?

**Trả lời:** Metadata của class nằm ở native memory. Metaspace growth có thể do load nhiều class hợp lệ hoặc classloader leak từ redeploy, dynamic context/proxy/code generation. Cần xem loaded/unloaded class và classloader reachability.

### 8. Strong, soft, weak và phantom reference khác nhau ra sao?

**Trả lời:** Strong giữ object sống. Weak cho phép thu hồi khi không còn strong reachability. Soft có semantics phụ thuộc memory pressure và không nên là capacity policy chính cho cache. Phantom dùng với reference queue cho post-mortem cleanup patterns, không truy cập referent. Resource như file/socket vẫn nên đóng deterministic.

### 9. Spring singleton có phải singleton toàn JVM không?

**Trả lời:** Không. Mặc định là một instance cho mỗi bean definition trong mỗi `ApplicationContext`. Nhiều context có thể có nhiều instance. Tuy vậy vòng đời thường bằng application nên field của nó là retention root quan trọng.

### 10. Tại sao gọi `System.gc()` không sửa leak?

**Trả lời:** Reachable object vẫn không được thu hồi. Explicit GC còn có thể bị JVM bỏ qua hoặc tạo pause/CPU cost tùy cấu hình. Phải sửa owner/lifetime/capacity gây retention.

## Level 2 — Spring và Data

### 11. Một singleton service có `ConcurrentHashMap` có an toàn memory không?

**Trả lời:** Thread-safe không đồng nghĩa bounded. Nếu key space vô hạn thì map sống bằng context và tăng tới OOM. Cần maximum size/weight, expiration/eviction, metric và quyết định cache/local store rõ ràng.

**Senior signal:** hỏi cardinality, value retained size, peak arrival trong TTL và behavior khi đầy.

### 12. `prototype` bean có tự chạy `@PreDestroy` không?

**Trả lời:** Spring tạo/configure rồi giao prototype cho caller và không quản lý đầy đủ destruction lifecycle sau đó. Resource cần owner đóng rõ ràng, ưu tiên `AutoCloseable`/try-with-resources hoặc custom lifecycle management.

### 13. Inject request-scoped bean vào singleton thế nào?

**Trả lời:** Dùng scoped proxy hoặc `ObjectProvider` để resolve instance hiện tại. Không lấy instance request rồi lưu vào singleton field/background closure vì sẽ kéo dài lifetime và sai context.

### 14. `@Lazy` có giải quyết memory leak không?

**Trả lời:** Không. Nó trì hoãn creation. Khi bean đã tạo, lifetime/references không tự ngắn hơn. Nó có thể giảm startup baseline nếu bean không bao giờ dùng, nhưng cần đo và không thay thế ownership đúng.

### 15. Persistence context giữ gì trong memory?

**Trả lời:** Managed entity identity map, dirty-check state/snapshot, pending actions và association/collection đã load. Unit of work lớn làm heap và flush CPU tăng.

### 16. `flush()` và `clear()` khác nhau thế nào?

**Trả lời:** `flush()` đồng bộ pending change xuống DB nhưng entity vẫn managed. `clear()` detach entity và giải phóng persistence-context references khi không còn owner khác. Batch lớn thường cần flush + clear theo chunk.

### 17. Vì sao `saveAll()` một triệu entity có thể OOM dù bật JDBC batching?

**Trả lời:** Caller đã materialize list lớn; transaction/persistence context và action queue tiếp tục giữ state. JDBC batching giảm round-trip chứ không bound input/live set. Đọc, persist, flush, clear theo chunk và cân nhắc transaction per chunk.

### 18. OSIV tác động memory thế nào?

**Trả lời:** Persistence context sống qua web request, nên managed graph có thể được giữ lâu và lazy serialization có thể load thêm. Nó không mặc định là leak, nhưng che query boundary/N+1 và làm retention theo response khó kiểm soát. API thường rõ ràng hơn khi fetch DTO trong transaction.

### 19. Tại sao trả JPA entity trực tiếp từ REST API nguy hiểm?

**Trả lời:** Serializer có thể traverse lazy relationships, gây N+1, cycle hoặc materialize graph lớn. Entity còn trộn persistence model với contract. Dùng response DTO/projection và fetch plan vừa đủ.

### 20. Cache local nên giới hạn theo entry hay weight?

**Trả lời:** Nếu value tương đối đồng đều, entry count có thể đủ. Nếu kích thước biến thiên lớn, weight gần memory/business cost đáng tin hơn. Cả hai vẫn cần TTL/freshness, key cardinality và metrics; weight estimator phải được kiểm thử.

### 21. TTL có đủ ngăn cache OOM không?

**Trả lời:** Không. TTL chỉ giới hạn lifetime, không giới hạn số key xuất hiện trong một cửa sổ TTL. Burst cardinality vẫn có thể OOM trước expiration; cần capacity và admission/eviction.

### 22. `@Transactional(readOnly = true)` có bảo đảm ít memory không?

**Trả lời:** Không. Nó thể hiện intent và provider có thể tối ưu, nhưng query vẫn có thể trả hàng triệu row và application vẫn có thể collect chúng. Projection, page/chunk, fetch plan và lifecycle của stream/context mới giới hạn volume.

## Level 3 — Concurrency, HTTP và Reactive

### 23. Thread pool fixed có ngăn memory overload không?

**Trả lời:** Chỉ giới hạn task chạy đồng thời; queue unbounded vẫn giữ task/payload vô hạn. Cần bounded queue, rejection/backpressure, timeout, cancellation và queue-depth metric.

### 24. `ThreadLocal` leak xảy ra thế nào trong server?

**Trả lời:** Worker thread sống lâu và được tái sử dụng. Value không `remove()` có thể giữ request/security/MDC graph và rò context sang request sau. Cleanup trong `finally`, hoặc dùng context-propagation abstraction phù hợp.

### 25. Vì sao `CompletableFuture` có thể giữ memory lớn?

**Trả lời:** Future chưa complete giữ dependent stages, captured variables, result/exception và callbacks. Timeout phía caller không nhất thiết cancel work. Cần bound executor/in-flight, propagate cancellation và capture immutable/minimal input.

### 26. Bật Virtual Threads có làm giảm mọi loại memory không?

**Trả lời:** Không. Nó giảm cost/scheduling của thread-per-task so với nhiều platform threads cho blocking I/O, nhưng mỗi task vẫn giữ request graph, stack chunk, buffers và ThreadLocal. Concurrency tăng mạnh có thể làm tổng live set/backlog tăng.

### 27. Tại sao vẫn cần HikariCP nhỏ hơn số Virtual Thread?

**Trả lời:** Connection pool phản ánh DB concurrency mà database chịu được. Virtual Threads có thể chờ connection rẻ hơn về thread, nhưng waiter vẫn giữ memory và latency budget. Pool quá lớn có thể phá DB; dùng semaphore/admission limit và acquire timeout.

### 28. Pinning của Virtual Thread có phải memory leak không?

**Trả lời:** Không trực tiếp. Nó giữ carrier bận trong một số blocking/monitor/native scenario, gây throughput giảm; backlog sau đó có thể làm retention tăng. Xác nhận bằng JFR/thread dump và tránh giữ lock qua I/O.

### 29. WebFlux có backpressure sao vẫn OOM?

**Trả lời:** Backpressure chỉ hiệu quả khi cả pipeline tôn trọng demand và buffer bounded. `collectList`, `join`, `cache`, `replay`, unbounded `onBackpressureBuffer`, high-cardinality `groupBy` hoặc external request arrival có thể giữ data không giới hạn.

### 30. Direct buffer leak khác heap leak thế nào?

**Trả lời:** Payload nằm off-heap/native nên heap graph không phản ánh đầy đủ. RSS/direct buffer tăng trong khi post-GC heap ổn. Kiểm tra buffer-pool metrics, NMT và Netty ownership/release ở success, error, discard, cancel.

### 31. Large upload nên thiết kế thế nào?

**Trả lời:** Giới hạn body/multipart ở gateway và app, stream theo chunk tới storage, giới hạn concurrent upload, tránh `getBytes()`/aggregate, kiểm soát decompressed size, xử lý cancellation và disk quota. Memory budget phải tính chunk × in-flight.

### 32. Kafka consumer memory upper bound phụ thuộc gì?

**Trả lời:** Concurrency × records per poll/batch × expanded payload size × số stage/in-flight, cộng deserialize/processing state. Consumer lag không tự gây app heap nếu broker giữ backlog, nhưng prefetch/retry/future queue có thể. Bound batch, concurrency và retry.

## Level 4 — Diagnostics và Tuning

### 33. Làm sao phân biệt leak với allocation rate cao?

**Trả lời:** Leak làm post-GC live set tăng dưới workload tương đương. Allocation pressure tạo nhiều young collection/GC CPU nhưng baseline có thể ổn. Dùng GC metrics/log và JFR allocation profiling; heap dump/path-to-root cho retention.

### 34. Heap dump cần xem gì trước?

**Trả lời:** Class histogram để thấy volume, dominator tree theo retained size, rồi path to GC roots để tìm owner. Shallow size đơn thuần dễ đánh lừa. So sánh hai dump theo thời gian thường mạnh hơn một snapshot.

### 35. Khi nào dùng JFR, heap dump và NMT?

**Trả lời:** JFR cho timeline, allocation hot spots, GC/thread/I/O với overhead phù hợp profiling. Heap dump cho object graph/reachability tại snapshot. NMT cho JVM internal native categories khi đã bật từ startup. Kết hợp chúng theo triệu chứng; NMT không thấy mọi native allocation bên thứ ba.

### 36. G1 hay ZGC cho Spring service?

**Trả lời:** Không có đáp án theo framework. Bắt đầu default G1 cho workload tổng quát; cân nhắc ZGC khi latency pause rất chặt/heap lớn và có benchmark chứng minh trade-off throughput/cost phù hợp. Dựa vào SLO, heap size, CPU budget, JDK và production-like load.

### 37. Tăng heap có thể làm tình hình tệ hơn không?

**Trả lời:** Có. Nó giảm native headroom trong container, tăng dump/recovery cost, có thể che leak lâu hơn và thay đổi pause behavior. Nếu live set hợp lệ thì tăng heap có thể đúng; phải chứng minh bằng live set/allocation/RSS.

### 38. Heap ổn nhưng RSS tăng liên tục, điều tra gì?

**Trả lời:** Direct buffers, metaspace/classloaders, thread stacks/count, native library/agent, mmap và JVM internal native memory. Dùng NMT baseline/diff, buffer/thread/class metrics và OS/container tools. Heap dump vẫn có thể tìm Java owner của native wrapper nhưng không đo toàn bộ native bytes.

### 39. Vì sao pod bị OOMKilled mà không có heap dump?

**Trả lời:** Cgroup/kernel kill process khi tổng memory chạm limit; đây không phải Java OOME nên `HeapDumpOnOutOfMemoryError` không được kích hoạt. Xem pod event, cgroup/RSS history và native headroom.

### 40. Heap dump production có rủi ro gì?

**Trả lời:** Stop/pause tùy runtime/state, I/O và disk lớn, instance thêm pressure, file chứa secret/PII. Cần capacity, restricted path/access, encryption/transfer policy và deletion. Đôi khi histogram/JFR hoặc dump một replica đã drain traffic an toàn hơn.

## Level 5 — Architect / Incident Scenarios

### 41. Sau deploy, old-gen đáy tăng 100 MB mỗi giờ. Bạn làm gì?

**Trả lời kỳ vọng:**

1. Correlate deploy marker, traffic và post-GC live-set slope.
2. So instance/version khỏe–xấu; lấy histogram/JFR, rồi heap dump nếu an toàn.
3. Dominator/path-to-root để tìm owner mới: cache, listener, queue, session, classloader.
4. Contain bằng feature flag/load shedding/rollback nếu SLO rủi ro.
5. Fix ownership/capacity và soak test lâu hơn thời gian leak biểu hiện.

**Senior signal:** không bắt đầu bằng tăng Xmx; có containment, evidence và proof-of-fix.

### 42. Một nightly import 5 triệu row OOM ở 70%. Thiết kế lại ra sao?

**Trả lời kỳ vọng:** Đọc input theo chunk, không giữ full list; transaction boundary theo chunk nếu nghiệp vụ cho phép; JDBC batch + periodic `flush/clear`; projection/cursor cho source; bounded validation/error collection; checkpoint/idempotency để resume. Đo heap plateau, DB locks, rollback và throughput.

### 43. Pod limit 2 GiB, heap chỉ 800 MiB nhưng pod chết. Giả thuyết nào?

**Trả lời kỳ vọng:** Heap chart có thể chỉ `used`, trong khi committed heap + direct + metaspace + stacks + native/GC vượt limit. Kiểm tra exit reason, RSS/cgroup, NMT, direct pool, threads/classes. Lập lại budget; không tùy tiện tăng pod trước khi tìm slope/root cause.

### 44. Chuyển MVC sang Virtual Threads, throughput không tăng và Hikari pending tăng mạnh. Vì sao?

**Trả lời kỳ vọng:** Bottleneck là DB, không phải thread. Fixed pool trước đó là accidental limiter; Virtual Threads tăng arrivals tới pool. Đặt admission control/semaphore, timeout và retry budget; tối ưu query/transaction; size pool theo DB total capacity và số replicas.

### 45. Cache hit rate 95% nhưng heap vẫn OOM. Có mâu thuẫn không?

**Trả lời:** Không. Hit rate không nói capacity hay retained bytes. 5% miss có thể tạo key cardinality vô hạn; 95% hit trên vài key nóng vẫn song song với hàng triệu key lạnh. Xem entry/weight distribution, eviction, key design và load amplification.

### 46. Chỉ một pod trong 20 pod leak. Bạn so sánh gì?

**Trả lời kỳ vọng:** Traffic affinity/session, partition assignment, tenant/cardinality, job leader role, connection/websocket lifetime, version/config/JDK/node, feature flags và error/retry paths. Compare histogram/JFR/config với pod khỏe. Tránh coi random rolling restart là bằng chứng fix.

### 47. WebFlux gateway OOM khi downstream chậm nhưng CPU thấp. Root cause khả dĩ?

**Trả lời kỳ vọng:** In-flight/buffer backlog, body aggregation, unbounded operator/retry queue, direct buffers hoặc client cancellation không propagate. CPU thấp phù hợp với tasks đang chờ. Xem in-flight, pending acquisition, direct memory/RSS, operator code và timeout/bulkhead.

### 48. `OutOfMemoryError: unable to create native thread`, bạn có giảm `-Xss` ngay không?

**Trả lời:** Không ngay. Xác định thread leak/pool growth, OS PID/thread limit, native headroom và blocked state. Giảm stack có thể tăng số thread nhưng che design lỗi và rủi ro stack overflow. Bound thread creation/queue và sửa lifecycle trước.

### 49. Service có GC pause cao nhưng không leak. Các hướng xử lý?

**Trả lời kỳ vọng:** Đo allocation rate/hotspots, live set và object size; giảm copy/serialization/intermediate collection/payload; tune heap young-generation/collector chỉ sau profile; cân nhắc G1/ZGC theo pause SLO; xác minh CPU/throughput trade-off bằng load test.

### 50. Bạn đặt memory SLO/guardrail cho một service mới thế nào?

**Trả lời kỳ vọng:**

- budget heap + native + safety headroom từ load test;
- hard limits cho payload, cache, queue, concurrency, batch;
- dashboard post-GC live set, RSS/limit, GC CPU/pause, direct/thread/class và business backlog;
- alert theo slope/SLO, không chỉ threshold tức thời;
- diagnostic flags/storage/runbook có security;
- overload/cancellation/downstream-failure soak tests;
- capacity review theo mỗi replica và toàn dependency cluster.

## Rubric tự chấm

| Mức | Đặc điểm câu trả lời |
|---|---|
| Junior | Nêu được heap/stack/GC và vài flag |
| Mid-level | Liên hệ Spring scope, cache, JPA, thread pool; biết heap dump |
| Senior | Phân biệt retention/allocation/native; nêu metric, evidence và bounded design |
| Staff/Architect | Lập budget toàn hệ thống, trade-off SLO/cost, failure containment và proof-of-fix |

Một câu trả lời tốt không cố đoán ngay root cause. Nó thu hẹp giả thuyết bằng dữ liệu, bảo vệ hệ thống trong lúc điều tra và biến fix thành guardrail có thể đo được.
