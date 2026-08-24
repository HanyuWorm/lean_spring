# 13 — Testing như feedback cho design

Test không chỉ xác nhận code; friction trong test thường báo dependency/state boundary kém.

## Test layers

- Unit: value object, aggregate, policy, state transition.
- Component/application: use case với fake ports và transaction semantics.
- Contract: adapter ↔ vendor/DB/message schema.
- Integration: mapping, constraint, isolation, transaction/rollback.
- End-to-end: critical user journey ít nhưng có giá trị.

## Test invariant, không test implementation

```text
Given any balance >= amount >= 0
When debit(amount)
Then new balance = old - amount and never negative
```

Property-based testing tìm boundary combination tốt hơn vài example cho Money/range/parser/state machine.

## State-machine test

- Mọi allowed transition có happy test.
- Mọi forbidden transition bị reject và state không đổi.
- Idempotent transition replay.
- Expiry boundary `now == expiresAt`.
- Persistence/reload giữ state/version.

## Fake, stub, mock

- Stub trả dữ liệu định trước.
- Fake có implementation nhẹ như in-memory repository.
- Mock verify interaction khi interaction là contract thật.

Đừng mock mọi internal collaborator; test sẽ khóa implementation. Fake repository phải mô phỏng unique/version behavior quan trọng, nếu không concurrency tests cho cảm giác an toàn giả.

## Contract tests

Port owner publish suite mà mọi adapter chạy: success, validation mapping, timeout, unknown outcome, idempotency. Test HTTP status thôi chưa đủ nếu domain semantics mapping sai.

## Concurrency tests

Cho nhiều task tranh cùng resource, synchronize start bằng barrier/latch, assert invariant cuối và số winner. Lặp nhiều lần nhưng không dựa timing `sleep`. Test integration với DB constraint/locking thật cho correctness cuối.

## Time tests

Dùng fixed/mutable clock. Test trước/đúng/sau boundary, timezone và DST nếu local time. Không tăng timeout để chữa flaky test do real clock.

## Mutation testing

Hữu ích kiểm test có bắt rule bị đảo/xóa không. Coverage cao không chứng minh assertion tốt. Ưu tiên mutation ở domain invariant/policy, không cần mọi getter/config.

## Testability checklist

- Required dependency explicit qua constructor.
- IO/time/random/id boundary thay được.
- Domain không cần Spring context để test.
- Result/error typed và deterministic.
- Không global mutable singleton.
- Transaction/concurrency behavior có integration evidence.
