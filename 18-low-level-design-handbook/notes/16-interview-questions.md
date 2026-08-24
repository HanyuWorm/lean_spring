# 16 — 60 câu hỏi LLD có đáp án

## Fundamentals

1. **LLD khác HLD?** HLD chọn service/data/deployment boundaries; LLD thiết kế use case, object, contract, state, algorithm, transaction và test bên trong boundary.
2. **Bắt đầu bài LLD từ đâu?** Clarify scope, actors, use cases và invariant; không bắt đầu bằng class.
3. **Deliverable LLD gồm gì?** Contracts, object responsibilities, state/sequence, persistence/concurrency/error decisions và test matrix.
4. **Invariant là gì?** Predicate phải luôn đúng tại consistency boundary, kể cả concurrent request/retry/failure.
5. **Business rule khác invariant?** Policy có thể chọn/thay; invariant là điều không được vi phạm trong model/scope đã định.
6. **CRC dùng làm gì?** Phân công Class–Responsibility–Collaborator và walkthrough use case trước diagram/code.
7. **Class diagram đủ chưa?** Không; cần behavior, state, sequence, concurrency, errors và tests.
8. **Entity khác Value Object?** Entity theo identity/lifecycle; value object immutable và equality theo value.
9. **Aggregate là gì?** Consistency/transaction boundary với root kiểm soát mutation và invariants.
10. **Khi nào transaction script hợp lý?** CRUD/rule đơn giản; rich domain model không phải mục tiêu mọi hệ thống.

## Principles

11. **SRP nghĩa là một method/class?** Không; một reason/actor of change, các behavior cohesive có thể cùng class.
12. **OCP có phải không sửa code cũ?** Không tuyệt đối; cô lập trục variation thật để thêm implementation ít tác động core.
13. **LSP kiểm tra gì?** Subtype giữ pre/postconditions, invariants và behavior/error expectations của base contract.
14. **ISP có dẫn tới interface một method?** Chỉ khi đó là capability cohesive theo client; tránh fragmentation máy móc.
15. **DIP là dependency injection?** DI là mechanism; DIP là policy phụ thuộc abstraction do phía cần capability sở hữu.
16. **Cohesion cao là gì?** Data/behavior trong module cùng phục vụ một purpose và đổi cùng lý do.
17. **Coupling nào nguy hiểm?** Vendor/concrete leak, shared mutable state, temporal/control/content coupling.
18. **Encapsulation hơn `private` thế nào?** Public API chỉ cho transition hợp lệ và không expose mutable internals.
19. **Composition hơn inheritance khi nào?** Khi cần thay policy/behavior độc lập hoặc quan hệ không có substitutability ổn định.
20. **YAGNI và extensibility cân bằng sao?** Chỉ abstraction cho boundary/risk/variation có evidence; ghi trigger để refactor sau.

## Patterns

21. **Factory khác Builder?** Factory chọn/tạo object hợp lệ; Builder thu thập nhiều construction options rồi build.
22. **Singleton trong Spring nghĩa gì?** Một bean mỗi ApplicationContext, không phải một instance toàn JVM/cluster.
23. **Adapter khác Facade?** Adapter đổi contract; Facade đơn giản hóa subsystem.
24. **Decorator khác Proxy?** Decorator thêm behavior composable; proxy kiểm soát access/lifecycle tới target, dù cấu trúc giống.
25. **Strategy khác State?** Strategy là policy được chọn; State thay behavior theo lifecycle và transition.
26. **Command khác Event?** Command là request có thể reject; event là fact đã xảy ra.
27. **Observer in-process có durable?** Không mặc định; broker/outbox cần nếu yêu cầu delivery sau crash/commit.
28. **Chain cần contract gì?** Order, short-circuit, first/all handler, error aggregation và registration.
29. **Template Method hay callback?** Callback/composition linh hoạt hơn; Template Method khi skeleton/hierarchy thật sự ổn định.
30. **Khi nào pattern là over-engineering?** Không giải quyết variation/boundary/risk hiện hữu nhưng thêm class, config và debug cost.

## Domain và boundaries

31. **Ai giữ business invariant?** Object/aggregate đủ state; application service orchestration, authorization và transaction.
32. **Repository là gì?** Port collection-like cho aggregate/persistence need, không nhất thiết DAO mỗi table.
33. **Domain service dùng khi nào?** Rule domain không thuộc tự nhiên một entity/value object và cần nhiều concepts.
34. **Domain event publish khi nào?** Thu khi transition xảy ra; integration publish sau commit, thường qua outbox nếu cần reliability.
35. **Có nên dùng JPA entity làm API DTO?** Thường không khi lifecycle/version/security khác; tránh lazy graph và mass assignment leak.
36. **Port thuộc ai?** Layer/use case cần capability; adapter implement và map external semantics.
37. **Authorization đặt đâu?** Authn ở boundary, object-level authz ở application/use case; domain invariant vẫn riêng.
38. **Aggregate lớn có vấn đề gì?** Load graph, lock contention, transaction dài và change coupling.
39. **Cross-aggregate invariant xử lý sao?** Thiết kế lại boundary nếu cần atomic; hoặc process/reservation/eventual consistency + reconciliation.
40. **Rich model luôn tốt?** Không; chọn theo domain complexity và cost mapping/tooling.

## State, concurrency và failure

41. **Concurrent collection đủ thread-safe?** Chỉ operation riêng; compound check-then-act vẫn cần atomic method/compute/lock.
42. **Optimistic lock hoạt động thế nào?** Update kèm expected version; zero rows nghĩa conflict, caller retry/reject.
43. **Pessimistic lock khi nào?** Conflict cao, critical section ngắn và serialize acceptable; cần timeout/order/deadlock plan.
44. **Unique index có vai trò gì?** Last-line invariant chống concurrent duplicate xuyên nhiều app instances.
45. **Idempotency key scope thế nào?** Tenant/actor + operation + key, kèm payload fingerprint và stored outcome.
46. **Timeout có nghĩa operation chưa chạy?** Không; downstream có thể hoàn tất sau client timeout, nên cần idempotency/reconciliation.
47. **Retry lỗi nào?** Chỉ transient và safe/idempotent trong attempt/deadline budget; không retry business reject.
48. **Tại sao inject Clock?** Deterministic test và một định nghĩa thống nhất cho expiry/boundary/timezone logic.
49. **Illegal state xử lý sao?** Không tạo được qua constructor/factory; transition bị cấm trả typed error/exception và state không đổi.
50. **DB transaction qua remote call?** Tránh vì lock/connection dài và outcome mơ hồ; dùng workflow/reservation/outbox tùy rule.

## Testing và interview cases

51. **Test aggregate gì?** Invariants, allowed/forbidden transitions, boundary, events và state unchanged on reject.
52. **Mock mọi dependency có tốt?** Không; khóa implementation. Mock interaction chỉ khi interaction là contract; fake/stub phù hợp hơn nhiều case.
53. **Contract test dùng cho gì?** Đảm bảo mọi adapter giữ semantics port/schema, gồm timeout/error/idempotency mapping.
54. **Test race condition thế nào?** Barrier đồng bộ start, nhiều contenders, assert winner/invariant; integration với DB constraint thật.
55. **Coverage 100% đủ?** Không; thiếu assertions/boundaries/concurrency vẫn sai. Mutation/property tests đánh chất lượng tốt hơn ở core.
56. **Thiết kế parking lot cần hỏi gì?** Floors/gates, spot/vehicle types, allocation, pricing, payment, concurrency và out-of-scope.
57. **Elevator nên tách gì?** Car safety state machine khỏi group dispatch strategy và hardware ports.
58. **Seat booking invariant chính?** Một seat/show tối đa một hold active/sale; expiry/owner/confirm atomic và idempotent.
59. **Rate limiter distributed khác local?** Cần shared atomic state/script, clock/partition/failure semantics; local lock không đủ.
60. **Kết thúc bài LLD thế nào?** Walkthrough critical/failure/concurrent flows, nêu trade-offs, tests và điều cố ý chưa thiết kế.
