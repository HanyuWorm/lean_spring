# 04 — Data và Distributed Systems trong Node.js

## Connection pool là concurrency budget

Event loop có thể nhận hàng nghìn request, nhưng database chỉ chịu số query hữu hạn. Pool không chỉ reuse connection; nó giới hạn DB concurrency. Pool per instance × replicas phải nằm trong DB budget.

Queue chờ pool vẫn giữ Promise, request context và timeout. Đặt acquire timeout, query/statement timeout, transaction timeout và admission control. Không tăng pool theo số concurrent socket.

## Transaction boundary

Transaction thuộc application use case, không thuộc repository method rời rạc. Truyền transaction/session context rõ ràng hoặc unit-of-work abstraction. Không giữ transaction qua remote HTTP call nếu tránh được.

Node async context có thể giúp propagation nhưng không biến distributed call thành atomic. Tránh callback/future chạy sau khi transaction đã commit nhưng vẫn dùng transaction handle.

## ORM/query choices

ORM cho mapping/unit-of-work/productivity; query builder/SQL cho visibility và control. Dù dùng Prisma, TypeORM, MikroORM, Drizzle, Knex hay driver:

- xem SQL và query plan;
- tránh N+1;
- select cột cần thiết;
- batch/page/stream data lớn;
- migration là artifact reviewable;
- không tin type generated thay runtime data constraints.

Library popularity thay đổi; chọn theo transaction semantics, migration, driver, observability và team—not social trend.

## Optimistic concurrency

Version column hoặc conditional update:

```sql
UPDATE inventory
SET quantity = quantity - :amount, version = version + 1
WHERE id = :id AND version = :expected AND quantity >= :amount;
```

Affected rows = 0 nghĩa conflict/out-of-stock, không được silently overwrite. Retry chỉ khi operation safe và có budget.

## Cache-aside

```text
read cache -> miss -> read DB -> populate cache -> return
write DB -> invalidate/update cache
```

Các vấn đề: stampede, stale data, key cardinality, serialization, multi-tenant isolation. Dùng TTL+jitter, single-flight, bounded local cache và metric hit/miss/eviction. Cache không là source of truth trừ khi thiết kế durability tương ứng.

## Messaging

Broker delivery thường at-least-once thực tế, nên consumer idempotent. Kafka chỉ giữ ordering trong partition; chọn key theo entity cần ordering (`orderId`, `accountId`), đồng thời cân nhắc hot partition.

Consumer flow:

```text
deserialize + validate
 -> deduplicate/inbox
 -> transactional business write
 -> record outcome
 -> acknowledge/commit offset
```

Retry phải phân biệt transient/permanent, có exponential backoff+jitter và DLQ/quarantine. In-memory retry vô hạn là outage amplifier.

## Transactional Outbox

Không dual-write database rồi publish broker trực tiếp. Cùng local transaction ghi domain state + outbox row. Relay/CDC publish outbox; consumer idempotent vì relay vẫn có thể publish duplicate.

Outbox không tự giải quyết ordering, schema evolution, poison event hoặc retention. Định nghĩa aggregate key, event ID/version và cleanup.

## Saga

Saga là chuỗi local transaction + events/commands và compensating action. Compensation là nghiệp vụ mới, không phải rollback thời gian. Orchestration dễ thấy state/timeout hơn; choreography giảm central coordinator nhưng flow khó quan sát khi lớn.

Persist saga state, timeout/deadline, correlation/causation ID và idempotency. Không giữ Promise/process memory chờ saga nhiều giờ.

## Distributed lock

Lock không thay database constraint/idempotency. Lease có expiry và process pause/network partition có thể làm holder cũ tiếp tục sau khi lease hết; fencing token giúp downstream reject stale owner. Chỉ dùng khi problem thực sự cần mutual exclusion cross-process.

## Backpressure toàn hệ thống

- HTTP: rate + concurrency limit, 429/503 và retry-after.
- DB: pool/semaphore và timeout.
- Broker: partition/concurrency/batch/prefetch.
- Worker: bounded queue và rejection.
- Client: retry budget/circuit breaker.

Node xử lý socket rẻ không có nghĩa dependency capacity vô hạn.

## Checklist

- Pool budget đã nhân replicas và workload khác?
- Transaction boundary và isolation phù hợp invariant?
- N+1/batch/large result đã test bằng production-like cardinality?
- Dual-write đã dùng outbox/CDC hay chấp nhận failure window rõ?
- Consumer idempotent và message key/order đúng aggregate?
- Retry/DLQ/backlog có hard bounds và operational workflow?
