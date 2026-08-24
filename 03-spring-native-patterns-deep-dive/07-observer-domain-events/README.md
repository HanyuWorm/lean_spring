# 07 - Observer / Domain Event

Spring application events là Observer in-process. Publisher không biết concrete listeners, nhưng mặc định event được multicast đồng bộ trên thread của publisher.

```text
transaction starts
  -> insert order
  -> publish OrderPlaced
       -> @EventListener runs immediately
       -> @TransactionalEventListener registered for AFTER_COMMIT
  -> commit: AFTER_COMMIT listener runs
  OR rollback: AFTER_COMMIT listener is skipped
```

Test rollback cho thấy listener thường đã quan sát event dù database không commit. Đây là lý do transaction phase là business semantics, không chỉ annotation detail.

## Domain event và integration event

- Domain event: fact trong bounded context, dùng ubiquitous language.
- Application event: transport in-process của Spring.
- Integration event: versioned contract vượt bounded context/process.
- Broker record: có delivery, retry, ordering, retention và security riêng.

Một domain event có thể được map thành integration event sau commit; không nên serialize thẳng entity/JPA object ra broker.

## `@TransactionalEventListener`

- `BEFORE_COMMIT`: vẫn có thể làm transaction fail; kéo dài transaction.
- `AFTER_COMMIT`: chỉ chạy khi commit thành công, nhưng transaction đã kết thúc; write mới cần transaction riêng.
- `AFTER_ROLLBACK`: cleanup/audit rollback có chủ ý.
- `AFTER_COMPLETION`: chạy sau cả commit hoặc rollback.

Nếu publish ngoài transaction, listener transactional mặc định không chạy trừ khi cấu hình fallback execution; cần quyết định rõ thay vì bật máy móc.

## Force, boundary, failure mode

- Force: publisher cần decouple nhiều reactions nhưng vẫn cần semantics về thời điểm.
- Boundary: publisher phát fact; listener đăng ký reaction. Application event vẫn cùng process/deployment.
- Failure mới: listener order, synchronous latency, listener exception làm publisher fail, event trước rollback và side effect không durable.
- Test: commit/rollback phase, listener failure, thread/context và duplicate/idempotency nếu bridge ra broker.

## Vì sao không durable

Process có thể crash sau DB commit nhưng trước/đang chạy listener. Spring event không có broker log, retry durable hoặc replay. Với integration quan trọng, dùng Transactional Outbox hoặc Spring Modulith persistent event publication.

## Bài mở rộng

1. Cho immediate listener throw và quan sát transaction rollback.
2. Đổi multicaster sang async; test thread/context và giải thích mất durability.
3. Map `OrderPlaced` sang outbox record rồi inject crash giữa commit và publish.

