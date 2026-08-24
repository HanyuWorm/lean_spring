# 05 — Transaction, locking và MVCC

## Isolation anomaly phải gọi đúng tên

- **Dirty read:** đọc write chưa commit.
- **Non-repeatable read:** cùng row được đọc lại ra giá trị khác.
- **Phantom:** chạy lại predicate thấy tập row khác.
- **Lost update:** hai writer đọc cùng trạng thái rồi write đè nhau.
- **Write skew:** hai transaction đọc cùng snapshot, update các row khác nhau và cùng làm hỏng invariant đa row.

Tên isolation level giống nhau không bảo đảm implementation giống nhau. PostgreSQL `READ UNCOMMITTED` thực tế như `READ COMMITTED`; MySQL InnoDB mặc định `REPEATABLE READ` với next-key/gap locking ở locking reads; MongoDB diễn đạt bằng read concern/write concern và transaction options.

## MVCC

MVCC cho reader nhìn snapshot mà thường không chặn writer. Nó không có nghĩa “không có lock”:

- writer vẫn xung đột với writer;
- DDL và metadata lock vẫn block;
- old versions phải được dọn;
- transaction dài giữ snapshot làm undo/dead tuples/version history phình;
- isolation cao hơn có thể abort và yêu cầu retry.

PostgreSQL tạo tuple version trong heap rồi VACUUM reclaim. InnoDB dùng undo records và purge. MongoDB/WiredTiger dùng snapshot/transaction machinery riêng. Không copy tuning giữa các engine.

## Ba cách bảo vệ inventory

### 1. Atomic conditional update — ưu tiên khi invariant cục bộ

```sql
UPDATE products
SET stock = stock - :quantity
WHERE id = :product_id
  AND stock >= :quantity;
```

Thành công khi affected rows = 1. Đây là một statement atomic, tránh read-then-write race.

### 2. Optimistic locking

```sql
UPDATE products
SET stock = :new_stock, version = version + 1
WHERE id = :id AND version = :expected_version;
```

Nếu zero rows, retry có giới hạn/jitter hoặc báo conflict. Phù hợp conflict thấp; không phù hợp hot key cực cao nếu mọi request retry.

### 3. Pessimistic locking

`SELECT ... FOR UPDATE` rồi update trong transaction. Hợp khi cần đọc nhiều field để quyết định và contention có kiểm soát. Transaction phải ngắn. Ở flash sale, hàng nghìn request chờ lock vẫn giữ connection, làm cạn pool và tạo timeout cascade; queue/atomic admission trước DB thường phù hợp hơn.

## Deadlock

Deadlock là cycle wait. Database phát hiện và abort một transaction; application phải coi deadlock/serialization failure là lỗi retryable có giới hạn.

Giảm deadlock bằng:

- update resource theo thứ tự ổn định;
- transaction ngắn, không gọi network khi giữ lock;
- index đúng để statement không lock/scan nhiều hơn dự kiến;
- batch nhỏ;
- timeout hợp lý và log transaction/query context;
- idempotency để retry an toàn.

Blocking không phải deadlock: một transaction có thể chờ rất lâu mà không tạo cycle. Cần lock timeout và observability.

## Transaction boundary trong Spring

- `@Transactional` thường qua proxy; self-invocation có thể bỏ qua advice.
- Không kéo HTTP call/Kafka publish chậm vào DB transaction nếu không có protocol rõ.
- `readOnly=true` là hint/optimization, không phải security boundary.
- Checked exception rollback semantics phải được hiểu/cấu hình.
- Open Session in View che giấu lazy query và kéo persistence context dài; API nên có transaction boundary rõ ở service/query layer.
- Retry phải bọc **toàn bộ transaction mới**, không retry bên trong transaction đã rollback-only.

## Idempotency

Idempotency key phải có scope nghiệp vụ và unique constraint:

```text
UNIQUE(tenant_id, operation_type, idempotency_key)
```

Trong cùng transaction: insert key/command record, thực hiện state change, lưu response/result. Duplicate đọc lại kết quả hoặc trả conflict nếu request payload hash khác. Redis TTL có thể giảm tải nhưng không thay thế unique constraint ở system of record. TTL phải dài hơn retry/redelivery horizon.

## Connection pool là bulkhead

Số request/thread có thể rất lớn; số transaction đồng thời phải bị giới hạn theo capacity DB. Với Virtual Threads, vẫn dùng bounded Hikari pool, acquisition timeout và upstream semaphore/rate limit. Tăng pool quá lớn làm context switching, memory và lock contention trong DB tệ hơn.

Ước lượng ban đầu bằng đo đạc, không có magic formula. Tổng connection của mọi instance + admin/migration/replication phải nhỏ hơn server budget và giữ headroom failover.

## Review checklist

- Invariant cần isolation/constraint nào?
- Hot row/key và contention expected?
- Lock được giữ bao lâu, có network call không?
- Transaction retry có idempotent và bounded không?
- Timeout chain: request > transaction > lock/query > connection acquisition có hợp lý không?
- Long-running transaction/snapshot được alert chưa?
