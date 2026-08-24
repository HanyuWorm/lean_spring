# 04 — Reliable events

Project ghi business state và event publication trong cùng transaction qua Spring Modulith Event Publication Registry. Listener notification lưu `eventId` làm idempotency key.

## Flow

```text
@Transactional place order
    -> INSERT orders
    -> publish OrderPlaced
    -> INSERT event_publication (cùng transaction)
    -> @ApplicationModuleListener
    -> INSERT processed_event (idempotency/inbox tối giản)
```

H2 chỉ phục vụ lab. Production cần migration rõ ràng và recovery policy cho publication ở trạng thái failed/stale.

## Chạy

```powershell
mvn -pl 04-reliable-events test
```

## Bài tập

1. Làm listener fail hai lần, quan sát event publication table.
2. Viết endpoint/admin job resubmit failed publications có giới hạn attempts.
3. Thêm unique business key vào order để request tạo order có tính idempotent.
4. Externalize `OrderPlaced` sang Kafka và version integration event riêng.
5. Dùng Testcontainers thay H2 để kiểm tra transaction behavior đúng database production.

