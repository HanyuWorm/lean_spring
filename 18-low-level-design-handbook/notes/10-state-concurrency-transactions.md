# 10 — State, concurrency và transaction

## State machine trước `if`

Mô hình hóa `State × Command → NewState + Effects | Error`. Liệt kê transition table:

| Current | Command | Guard | Next | Effect |
|---|---|---|---|---|
| AVAILABLE | Reserve | seat sellable | HELD | set expiry |
| HELD | Confirm | same owner, not expired | SOLD | attach payment |
| HELD | Expire | now ≥ expiry | AVAILABLE | emit expired |
| SOLD | Reserve | — | — | reject |

Illegal transition phải explicit; không silently ignore trừ khi idempotent semantics định nghĩa vậy.

## Race condition kinh điển

Hai request cùng đọc `AVAILABLE`, cùng tạo reservation. In-memory check đúng nhưng không atomic ở database.

Các lựa chọn:

- Unique constraint `(show_id, seat_id)` cho active representation.
- Optimistic version: update `where id=? and version=?`, loser retry/reject.
- Pessimistic lock: serialize critical row, có timeout/deadlock cost.
- Atomic compare-and-set/update condition.
- Single-writer/partitioned command processing.

Luôn có database invariant cuối cùng; lock ứng dụng một process không bảo vệ multi-instance.

## Optimistic vs pessimistic

Optimistic phù hợp conflict hiếm, transaction ngắn; cần conflict UX/retry budget. Pessimistic phù hợp conflict cao và critical section nhỏ; cần lock ordering, timeout và đo contention.

Không retry toàn transaction vô hạn. Retry chỉ khi operation safe/idempotent và deadline còn.

## Idempotency

Key scope = actor/tenant + operation + key. Lưu request fingerprint và final/in-progress outcome. Cùng key khác payload phải conflict, không trả kết quả cũ.

State cơ bản:

```text
ABSENT → IN_PROGRESS → SUCCEEDED/FAILED_FINAL
             └── lease timeout/recovery
```

Unique index giải quyết concurrent insert; transaction gắn idempotency record với business write. Với external effect, cần provider idempotency key hoặc outbox/reconciliation.

## Transaction

ACID không có nghĩa mọi rule toàn hệ thống atomic. Xác định:

- Data nào cùng consistency boundary?
- Isolation anomaly nào chấp nhận?
- Remote call trước/ngoài/sau transaction?
- Event publish atomic bằng gì?
- Failure sau commit nhưng trước response xử lý ra sao?

## Thread safety trong object

Ưu tiên confinement/immutability. Nếu shared mutable state:

- Define atomic operations, không expose check-then-act.
- Lock minimal consistent order.
- Không call unknown/remote code khi giữ lock.
- Concurrent collection không làm compound operation atomic.
- Test bằng repeated concurrency scenario, nhưng database constraint vẫn là protection.

## Version trong domain

Version là concurrency token, không business state. Repository compare version và trả `ConcurrentModification`; application quyết định reload/retry/conflict. Không để domain silently overwrite newer state.
