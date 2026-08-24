# 06 — Observability + Virtual Threads

Project xem observability như decorator quanh use case và sử dụng executor do Spring Boot auto-configure. Khi bật virtual threads, blocking task chạy trên virtual thread nhưng downstream capacity vẫn phải được giới hạn riêng.

## Chạy

```powershell
mvn -pl 06-observability-concurrency test
mvn -pl 06-observability-concurrency spring-boot:run
```

Sau khi chạy:

- `GET /lab/thread` — xem thread thực thi có phải virtual thread.
- `GET /lab/process?orderId=O-1` — tạo custom observation.
- `GET /actuator/prometheus` — xem metrics.

## Bài tập

1. Thêm OTLP tracing và kiểm tra trace propagation qua một HTTP client.
2. So sánh 500 blocking requests khi bật/tắt virtual threads.
3. Thêm semaphore/concurrency limit = 10 quanh downstream chỉ có 10 DB connections.
4. Dùng JFR tìm pinned virtual thread.
5. Thử đưa `orderId` vào metric tag, quan sát cardinality rồi sửa thành trace-only field.
6. Thêm `ContextPropagatingTaskDecorator` và kiểm tra observation context ở async task.

