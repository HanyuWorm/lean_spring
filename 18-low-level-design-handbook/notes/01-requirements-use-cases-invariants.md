# 01 — Requirement, use case và invariant

## Tách bốn loại yêu cầu

- **Capability:** người dùng cần làm gì, ví dụ “giữ ghế trong 5 phút”.
- **Policy:** quyết định có thể đổi, ví dụ giá theo ngày/thành viên.
- **Invariant:** điều luôn đúng, ví dụ một ghế không có hai hold còn hiệu lực.
- **Quality constraint:** latency, concurrency, audit, security, precision.

Noun extraction chỉ giúp tìm candidate. Behavior và lifecycle mới quyết định object. “User, Car, Spot” chưa nói ai cấp spot, khi nào ticket hợp lệ hoặc race được xử lý ra sao.

## Use-case contract

```text
Use case: HoldSeat
Actor: authenticated customer
Input: showId, seatIds, idempotencyKey
Preconditions: show open; seats sellable
Success: one active hold owns all requested seats until expiresAt
Invariant: no overlapping active hold for one show/seat
Failure: unavailable, invalid selection, duplicate conflict, capacity/timeout
Side effects: persist hold; emit SeatHeld after commit
Idempotency: same actor+key+payload returns same result
Authorization: actor can only read/release own hold
```

## Tìm invariant

Hỏi:

- Điều gì nếu sai sẽ làm mất tiền, vượt tồn kho hoặc lộ dữ liệu?
- Rule nào phải đúng ngay trong transaction? Rule nào eventual được?
- Hai request đồng thời có thể cùng pass validation không?
- Retry cùng command có tạo effect lần hai không?
- State transition nào bị cấm?

Viết invariant dưới dạng predicate:

```text
activeReservations(show, seat).count <= 1
ticket.exitAt >= ticket.entryAt
money.currency == price.currency
transition(current, command) ∈ allowedTransitions
```

## Decision table

Ví dụ phát ticket parking:

| Vehicle fits? | Spot available? | Gate open? | Kết quả |
|---|---|---|---|
| No | * | * | Reject `UNSUPPORTED_VEHICLE` |
| Yes | No | * | Reject `LOT_FULL` |
| Yes | Yes | No | Không reserve; `GATE_UNAVAILABLE` |
| Yes | Yes | Yes | Allocate + issue ticket |

Decision table bắt missing combination tốt hơn nested `if` dài.

## Functional và non-functional trong LLD

NFR ảnh hưởng trực tiếp design: thread-safety quyết định mutation model; audit quyết định immutable event; latency quyết định algorithm/cache; precision quyết định `BigDecimal`/rounding; security quyết định actor context và object-level authorization.

## Acceptance-to-test trace

Mỗi invariant có ít nhất:

- One happy scenario.
- Boundary values.
- Illegal transition/negative case.
- Duplicate/retry case nếu side effect.
- Concurrent case nếu shared resource.
- Persistence failure/rollback case nếu transaction.

Template đầy đủ: [lld-spec-template.md](../templates/lld-spec-template.md).
