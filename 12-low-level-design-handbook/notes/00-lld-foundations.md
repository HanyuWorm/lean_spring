# 00 — Nền tảng Low-Level Design

## LLD nằm ở đâu?

```text
Business/System requirements
        ↓
High-Level Design: service/module/data ownership/deployment
        ↓
Low-Level Design: use case/object/interface/state/algorithm/transaction/error
        ↓
Implementation + tests
```

LLD không đồng nghĩa class diagram. Diagram chỉ là một view. Một LLD đủ dùng thường gồm:

- Scope, actors, happy/alternate/failure flows.
- Business rules và invariants.
- Public contracts: command/query/result/error/event.
- Object responsibilities, ownership và lifecycle.
- State machine/decision table cho behavior có trạng thái.
- Persistence/transaction/concurrency/idempotency decisions.
- Extension points và các dependency boundaries.
- Sequence cho critical flow.
- Test scenarios và unresolved trade-offs.

## Workflow 10 bước

1. Clarify scope và non-goals.
2. Viết actors/use cases bằng động từ.
3. Liệt kê invariant và policy có thể đổi.
4. Xác định commands, queries, events và errors.
5. Tìm object có identity/lifecycle và value object.
6. Gán responsibility bằng “ai có đủ dữ liệu để quyết định?”.
7. Vẽ state/sequence cho critical flow.
8. Chọn transaction/concurrency boundary.
9. Thêm ports tại IO/variability boundary; chọn pattern tối thiểu.
10. Walkthrough scenarios, viết test matrix rồi mới code.

## LLD và HLD khác nhau

| Câu hỏi | HLD | LLD |
|---|---|---|
| Boundary | service/module/region | aggregate/class/interface/function |
| Data | ownership, DB, replication | fields, invariants, mapping, transaction |
| Interaction | API/event/topology | method/command/sequence/callback |
| Failure | dependency/zone/queue | exception/result/retry/state transition |
| Scale | capacity/partition/cache | algorithm, batching, lock/contention |
| Evidence | SLO/load/failure test | unit/contract/property/concurrency test |

## Quality criteria

- **Correct:** không thể dễ dàng tạo state vi phạm invariant.
- **Comprehensible:** tên và dependency direction kể được câu chuyện nghiệp vụ.
- **Changeable:** policy thường đổi được cô lập; không speculative abstraction.
- **Testable:** time/random/IO/concurrency boundary kiểm soát được.
- **Operable:** error/audit/metric có ý nghĩa ở boundary quan trọng.
- **Secure:** authorization và data exposure nằm trong contract, không là decoration.

## Trình tự phỏng vấn 45–60 phút

1. 5–8 phút: clarify requirements, scale vừa đủ và assumptions.
2. 5 phút: use cases/invariants.
3. 10 phút: domain objects và APIs.
4. 10 phút: critical sequence/state.
5. 10 phút: patterns, persistence/concurrency/error.
6. 5 phút: extensibility, tests, trade-offs.

Đừng code class trước khi interviewer đồng ý scope và invariant; bạn sẽ tối ưu nhầm bài toán.
