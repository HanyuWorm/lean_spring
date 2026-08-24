# 14 — Sáu case study LLD

Mỗi case bắt đầu bằng clarification/invariant rồi mới class. Đây là outline để tự luyện, không phải một “đáp án chuẩn”.

## 1. Parking Lot

### Scope

Nhiều tầng/cổng; vehicle motorcycle/car/truck; issue ticket, allocate spot, pay, exit. Bỏ reservation tháng và license recognition.

### Invariants

- Một spot tối đa một active vehicle.
- Một ticket active gắn đúng vehicle/spot.
- Vehicle chỉ vào spot phù hợp.
- Exit chỉ sau payment đủ; duration không âm.

### Objects

`ParkingLot` aggregate, `ParkingSpot`, `ParkingTicket`, `Vehicle`, `Money`, `SpotSelectionPolicy`, `PricingPolicy`, repositories, `Clock`.

### Critical decisions

- Strategy cho allocation/pricing.
- Atomic allocate bằng aggregate lock một process hoặc DB conditional update/version multi-instance.
- Ticket lifecycle `ACTIVE→PAID→CLOSED`; lost-ticket là policy riêng.

## 2. Elevator

### Scope/invariants

Multiple cars, hall/car calls, direction, door/safety. Car không di chuyển khi door mở; không vượt floors; emergency ưu tiên.

Model `ElevatorCar` state machine (`IDLE/MOVING/DOOR_OPEN/OUT_OF_SERVICE`), `DispatchStrategy`, `Request`, `Door`, `Motor` ports. Scheduler quyết định assignment; car giữ safety transitions. Hỏi fairness, starvation, capacity và duplicate calls.

## 3. Seat Booking

### Invariants

Một seat/show chỉ có một hold active hoặc sale; hold có expiry; confirm đúng owner; payment retry không bán hai lần.

Model `ShowSeatInventory`, `Hold`, `Reservation`, `TimeRange`, optimistic version/unique constraint và idempotency. Tránh scheduler xóa hold là protection duy nhất—availability query phải coi expired hold không active hoặc cleanup atomic.

## 4. Vending Machine

State: `IDLE`, `HAS_CREDIT`, `DISPENSING`, `OUT_OF_SERVICE`. Commands: insert, select, cancel, refill. Invariants: credit không âm, dispense chỉ khi đủ stock/credit, refund/dispense failure có recovery. State transition rõ hơn boolean `hasMoney/isSelected/isDispensing`.

Model `VendingMachine`, `Inventory`, `CashBalance`, `ProductSlot`, `PricingPolicy`, hardware ports. IO hardware có failure; state persisted/reconciled nếu power loss là requirement.

## 5. Notification Platform

Scope: email/SMS/push, template, preference, retry. `NotificationService` orchestration, `Channel` Strategy, `TemplateRenderer`, `PreferencePolicy`, decorators metrics/retry/rate-limit. Idempotency theo notification ID + channel; provider accepted nhưng response timeout là unknown outcome. PII redaction và unsubscribe là invariant/policy.

## 6. Rate Limiter

Algorithms: fixed/sliding window, token bucket. LLD gồm `RateLimitKey`, `Policy`, `Clock`, `TokenBucket`, store. In-memory atomic state dùng lock/CAS; distributed cần atomic Redis script và server time/clock semantics. Contract trả allowed + remaining + retryAfter, không chỉ boolean.

### Token bucket core

```text
refill = elapsed * rate
tokens = min(capacity, tokens + refill)
if tokens >= cost: tokens -= cost; allow
else deny with wait = deficit/rate
```

Test fractional refill, backward clock, concurrent acquire, overflow và policy change.

## Cách tự chấm

Cho mỗi case, kiểm:

1. Scope/non-goal rõ?
2. Ít nhất ba invariant?
3. Public API/typed errors?
4. Object owner của từng rule?
5. State/concurrent race?
6. Extension point dựa trên requirement thật?
7. Critical sequence và failure?
8. Test matrix?
