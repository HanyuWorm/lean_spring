# Low-Level Design Handbook — Java/Spring

Track tiếng Việt dành cho senior Java developer/solution architect muốn thiết kế **bên trong một module/service**: object, interface, state, invariant, algorithm, transaction, error và test. System Design trả lời “các khối lớn giao tiếp thế nào”; LLD trả lời “code bên trong khối đó giữ đúng nghiệp vụ và thay đổi an toàn thế nào”.

## Kết quả cần đạt

- Chuyển requirement mơ hồ thành use case, invariant, state machine và contract kiểm thử được.
- Phân rã object theo responsibility/behavior thay vì map một class cho mỗi bảng.
- Dùng SOLID, composition, immutability, cohesion/coupling đúng ngữ cảnh.
- Chọn pattern theo problem/forces/consequence, không theo checklist thuộc lòng.
- Thiết kế transaction, concurrency, idempotency, error và time-dependent behavior.
- Tạo code dễ test, dễ mở rộng nhưng không over-engineer.
- Trình bày LLD trong phỏng vấn bằng assumption, diagram, API, data model và trade-off.
- Review LLD bằng evidence: invariant, scenario, test và failure mode.

## Lộ trình nội dung

| Chương | Nội dung |
|---|---|
| [00](notes/00-lld-foundations.md) | LLD là gì, deliverables và workflow |
| [01](notes/01-requirements-use-cases-invariants.md) | Requirement, use case, rule và invariant |
| [02](notes/02-object-modeling-and-uml.md) | Object modeling, CRC, UML vừa đủ |
| [03](notes/03-solid-grasp-and-design-for-change.md) | SOLID, GRASP và trục thay đổi |
| [04](notes/04-cohesion-coupling-encapsulation.md) | Cohesion, coupling, encapsulation, immutability |
| [05](notes/05-creational-patterns.md) | Factory, Builder, Prototype, Singleton/DI |
| [06](notes/06-structural-patterns.md) | Adapter, Facade, Decorator, Composite, Proxy |
| [07](notes/07-behavioral-patterns.md) | Strategy, State, Command, Observer, Chain, Template |
| [08](notes/08-domain-modeling-and-ddd-tactical.md) | Entity, Value Object, Aggregate, domain service/event |
| [09](notes/09-application-boundaries-and-contracts.md) | Controller/use case/port/repository, DTO và mapping |
| [10](notes/10-state-concurrency-transactions.md) | State, locking, optimistic concurrency, idempotency |
| [11](notes/11-errors-resilience-and-time.md) | Error taxonomy, retry, timeout, clock và scheduler |
| [12](notes/12-extensibility-configuration-versioning.md) | Extension point, configuration và compatibility |
| [13](notes/13-testing-for-design.md) | Test pyramid, contract/property/concurrency tests |
| [14](notes/14-case-studies.md) | Parking lot, elevator, booking, vending, notification, rate limiter |
| [15](notes/15-anti-patterns-and-review-checklist.md) | Anti-pattern và checklist review |
| [16](notes/16-interview-questions.md) | 60 câu hỏi có đáp án |

## Project thực hành

[lld-java-lab](labs/lld-java-lab/README.md) là Maven project Java 21 không framework, tập trung vào domain design:

- Parking lot: aggregate, value object, strategy, repository port và clock.
- Vending machine: explicit state machine và transition validation.
- Seat reservation: optimistic version, idempotency và concurrency test.
- Notification: channel strategy, decorator và retry policy.

## Cách học

1. Đọc `00`–`04`, dùng [LLD spec template](templates/lld-spec-template.md) cho một bài toán thật.
2. Đọc patterns `05`–`07`; với mỗi pattern, ghi một case **không nên dùng**.
3. Đọc `08`–`13`, chạy lab và sửa TODO trong README của lab.
4. Tự thiết kế sáu case study ở `14` trước khi đọc design gợi ý.
5. Dùng [review checklist](templates/lld-review-template.md) và trả lời 60 câu hỏi.

## Nguyên tắc cốt lõi

- Bắt đầu từ behavior/invariant, không bắt đầu từ class diagram.
- Encapsulation nghĩa là object ngăn state sai, không chỉ `private` field.
- Interface được tạo quanh variability/boundary thật, không tạo `IThing` cho mọi class.
- Composition là mặc định; inheritance chỉ khi subtype giữ được contract của base type.
- Pattern là vocabulary của trade-off, không phải mục tiêu số lượng.
- Domain model không phụ thuộc HTTP/JPA/message DTO.
- Concurrency correctness là một phần contract, không phải tối ưu sau cùng.
- Code dễ test thường là kết quả của dependency/time/state boundary rõ.
