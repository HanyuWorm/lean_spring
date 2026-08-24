# 02 — Spring Bean Lifecycle, Scope và Memory Leak

## 1. Scope là quyết định về ownership và retention

Spring singleton là một instance cho mỗi bean definition trong một `ApplicationContext`, không phải singleton toàn JVM. Scope mặc định này phù hợp với service stateless, nhưng mọi object được field của bean tham chiếu có thể sống gần bằng ứng dụng.

| Scope | Vòng đời điển hình | Rủi ro memory |
|---|---|---|
| `singleton` | bằng `ApplicationContext` | field tích lũy state, unbounded map/list |
| `prototype` | Spring tạo mỗi lần request bean | Spring không tự gọi đầy đủ destruction callback sau khi giao object |
| `request` | một HTTP request | giữ payload lớn đến cuối request |
| `session` | một HTTP session | số session × graph object có thể rất lớn |
| `application` | toàn servlet application | tương tự global cache |
| `websocket` | một WebSocket session | connection lâu làm state sống lâu |

Đừng dùng scope ngắn hơn như một cách “tối ưu GC” tùy tiện. Nó có thể tăng allocation rate. Hãy chọn scope theo ownership và tính đúng đắn trước.

## 2. Stateful singleton — lỗi phổ biến nhất

```java
@Service
class ReportService {
    private final Map<String, byte[]> generatedReports = new ConcurrentHashMap<>();

    byte[] generate(String userId) {
        return generatedReports.computeIfAbsent(userId, this::render);
    }
}
```

`ConcurrentHashMap` giải quyết race condition, không giải quyết capacity. Nếu user ID có cardinality không giới hạn, đây là leak theo nghiệp vụ.

Thiết kế tốt hơn:

- nếu cần cache local: maximum size/weight, expiration, eviction metric;
- nếu không cần reuse: trả kết quả và bỏ reference;
- nếu cần lưu lâu: external object storage/database;
- tránh để request, `SecurityContext`, entity hoặc large buffer trong singleton field.

Một singleton service nên mặc định stateless. Field hợp lý thường là immutable dependency/configuration, không phải dữ liệu theo request.

## 3. Inject bean có scope ngắn vào singleton

Nếu inject trực tiếp một request/prototype bean sai cách, singleton có thể giữ instance được resolve tại thời điểm tạo. Dùng scoped proxy hoặc `ObjectProvider<T>` khi thực sự cần resolve instance hiện hành:

```java
@Component
class RequestAwareService {
    private final ObjectProvider<RequestMetadata> metadataProvider;

    RequestAwareService(ObjectProvider<RequestMetadata> metadataProvider) {
        this.metadataProvider = metadataProvider;
    }

    void handle() {
        RequestMetadata current = metadataProvider.getObject();
    }
}
```

Không lưu `current` vào field. Proxy/provider giải quyết lookup theo scope, không cấp phép kéo object request sang background job.

## 4. Prototype và destruction

Spring cấu hình, khởi tạo và giao prototype bean cho caller, sau đó không quản lý trọn vòng đời hủy như singleton. Nếu prototype giữ file handle, socket hoặc native resource, caller phải có ownership rõ ràng và đóng resource; thường nên dùng `AutoCloseable`/try-with-resources thay vì trông chờ `@PreDestroy`.

Đây là khác biệt giữa:

- memory do GC quản lý;
- external/native resource cần deterministic cleanup.

## 5. Lifecycle callback đúng cách

Dùng `@PreDestroy`, `DisposableBean` hoặc destroy method để:

- shutdown executor do bean sở hữu;
- unregister listener;
- close client/pool/resource;
- cancel scheduled task;
- dừng producer/consumer thread.

Không cần gán mọi field bằng `null` trong `@PreDestroy`; khi context và bean graph không còn reachable, GC xử lý. Việc quan trọng là cắt reference từ root sống lâu và đóng resource ngoài heap.

## 6. Các retention pattern cần audit

### Static collection

Static field thường là GC root thông qua classloader. Map/list không bounded giữ data đến khi classloader được thu hồi.

### Listener/subscriber không unregister

Publisher sống lâu giữ listener; listener lại capture service/context cũ. Đặc biệt nguy hiểm với custom child contexts, plugin system hoặc redeploy trong cùng JVM.

### Scheduler và executor

Scheduled future, task queue hoặc unfinished `CompletableFuture` có thể giữ closure chứa request/entity/byte array. Bean nào tạo executor thì bean đó phải định nghĩa capacity, rejection policy và shutdown.

### Lambda capture

```java
byte[] payload = loadLargePayload();
executor.execute(() -> audit(payload));
```

Payload sống ít nhất tới khi task chạy xong. Nếu queue backlog 10.000 task, retention là tổng payload của toàn queue.

### HTTP session

Không đặt entity graph, upload bytes hay cache kết quả lớn trong session. Session replication còn nhân memory/network cost theo node. Chỉ giữ identifier/nhỏ gọn, data thật nằm ở store phù hợp.

### Dynamic context/class generation

Tạo lặp `ApplicationContext`, classloader, proxy hoặc expression-generated class có thể gây metaspace growth. Sau khi đóng context, nếu thread/listener/static registry vẫn tham chiếu classloader, toàn bộ class metadata có thể bị giữ.

## 7. Những thứ thường bị hiểu nhầm

- Circular dependency không đồng nghĩa memory leak; reachability từ root mới quyết định.
- `@Lazy` trì hoãn khởi tạo, không tự giảm live set sau khi bean đã được tạo.
- Spring proxy có overhead, nhưng leak thường nằm ở target/state mà proxy giữ, không phải bản thân proxy.
- `WeakReference` không phải cách sửa ownership mơ hồ; cache cần policy rõ ràng và đo được.
- Gọi `System.gc()` không sửa được reachable-object leak và có thể gây pause.

## 8. Checklist code review cho bean

- Bean này sống bao lâu và ai sở hữu nó?
- Mỗi field mutable có cardinality tối đa bao nhiêu?
- Có field nào chứa request/user/entity/buffer/future không?
- Cache/queue có hard limit, eviction/rejection và metric không?
- Listener/task/executor/client được đóng ở đâu?
- Short-lived scope có bị singleton/background task giữ lại không?
- Cleanup có chạy cả success, exception, timeout và cancellation không?
- Có tạo context/classloader/proxy động theo request hoặc tenant không?
