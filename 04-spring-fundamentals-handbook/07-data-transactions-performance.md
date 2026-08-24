# 07 - Transaction, Locking và Database Performance

## Transaction boundary

Transaction nên bao một business consistency boundary ngắn:

```java
@Transactional
public void reserveInventory(ReserveInventory command) {
    var stock = stocks.findBySku(command.sku()).orElseThrow();
    stock.reserve(command.quantity());
}
```

Không giữ DB transaction trong lúc gọi payment HTTP nhiều giây. Điều đó giữ connection/lock, tăng contention và làm rollback không thu hồi remote side effect.

## ACID ngắn gọn

- Atomicity: local transaction commit tất cả hoặc không.
- Consistency: invariant được application + DB constraints bảo vệ.
- Isolation: concurrent transactions quan sát/ảnh hưởng nhau theo level.
- Durability: commit sống qua failure theo guarantees của DB/config.

ACID của một database không tự mở rộng qua HTTP/broker. Dùng Outbox/Saga/idempotency cho distributed workflow.

## Optimistic locking

```java
@Version
private long version;
```

Update SQL chứa version cũ; nếu row count = 0, provider ném optimistic lock exception. Phù hợp conflict hiếm.

Retry chỉ khi operation có thể re-run an toàn trên state mới và có giới hạn. Với user edit, trả conflict để user merge có thể đúng hơn retry mù.

## Pessimistic locking

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select s from StockEntity s where s.sku = :sku")
Optional<StockEntity> findForUpdate(String sku);
```

Phù hợp critical section ngắn và contention đã đo. Failure modes: lock wait, timeout, deadlock và throughput collapse. Luôn có database constraint cho invariant cuối.

## Unique constraint và idempotency

```sql
alter table payment add constraint uk_payment_request unique (merchant_id, idempotency_key);
```

Check-then-insert trong application vẫn race. Unique constraint quyết định winner atomically; application map violation thành replay/conflict đúng contract.

## Connection pool

Hikari pool giới hạn concurrent DB work. Pool quá lớn có thể làm DB context switching/lock/IO tệ hơn. Theo dõi:

- active/idle/pending connections;
- connection acquisition time;
- transaction/connection hold time;
- DB CPU, IO, locks, slow queries;
- p95/p99 latency.

Virtual Threads không thay connection pool; nhiều request có thể chờ một pool nhỏ và làm tail latency tăng. Dùng admission control khi cần từ chối sớm.

## Query performance workflow

1. Đo endpoint/query chậm bằng trace/metric.
2. Capture SQL thật và parameters shape.
3. Dùng `EXPLAIN (ANALYZE, BUFFERS)` trên database phù hợp.
4. Kiểm tra cardinality, scan, join, sort, rows estimate.
5. Thay query/index/data model.
6. Đo lại với data volume/concurrency thực tế.

Index tăng read speed nhưng tăng write/storage/maintenance. Composite index column order theo predicates/order, không thêm index cho mọi field.

## Batch operations

- JDBC batch giảm network round-trips.
- Hibernate batching cần ID strategy/config phù hợp.
- Flush/clear theo chunk tránh persistence context giữ quá nhiều entities.
- Bulk JPQL/native update bypass managed entity state; clear/refresh context để tránh stale objects.

## Pagination

Offset `LIMIT/OFFSET` đơn giản nhưng offset lớn vẫn phải scan/skip và dữ liệu thay đổi gây duplicate/missing giữa pages. Keyset pagination dùng stable sort + last seen key:

```sql
where (created_at, id) < (:lastCreatedAt, :lastId)
order by created_at desc, id desc
limit 50
```

## Transaction testing caveat

`@DataJpaTest` thường chạy mỗi test trong transaction rồi rollback. Điều này có thể che:

- lazy loading ngoài transaction;
- SQL chỉ phát lúc commit;
- after-commit listener;
- connection lifecycle.

Dùng `flush()`, clear persistence context hoặc integration test không bọc transaction khi cần chứng minh production boundary.

## Checklist

- Transaction nằm ở application use case chưa?
- Có remote call khi đang giữ connection/lock không?
- Invariant cuối có DB constraint/version/lock không?
- Query count và execution plan đã đo chưa?
- Pool sizing dựa trên DB capacity hay số application threads?
- Retry conflict có bounded/idempotent không?

Nguồn: [Spring Transaction Management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html), [Spring Data JPA Locking](https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html).

