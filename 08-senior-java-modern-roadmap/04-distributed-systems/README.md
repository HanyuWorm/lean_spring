# Chặng 4 - Modern Distributed Architecture

Thời lượng: tuần 12-14. Trọng tâm là failure semantics và ownership boundary.

Khi đã hoàn thành phần implementation, học tiếp catalog [System Design Patterns](../07-solution-architect/SYSTEM_DESIGN_PATTERNS.md) để nâng quyết định lên mức Solution Architect.

## 1. Target architecture để phân tích

```text
Web/Mobile
   -> BFF / API Gateway
      -> Order Service ---- PostgreSQL + Outbox
      -> Payment Service -- PostgreSQL + Outbox
                |                |
                +---- Kafka/CDC -+
                           |
                      Read Model / Search
```

Tại gateway/BFF: authentication, coarse authorization, routing, rate limiting và response composition. Business authorization và invariant vẫn phải được service sở hữu kiểm tra; gateway không phải security boundary duy nhất.

## 2. Saga

### Choreography

Phù hợp khi flow ngắn, ownership event rõ và coupling thấp. Rủi ro là control flow ẩn trong nhiều consumer, khó quan sát và khó thay đổi.

### Orchestration

Phù hợp khi workflow dài, cần explicit state, timeout, compensation và operator visibility. Rủi ro là orchestrator trở thành nơi chứa business logic của mọi service.

Lab: `Order -> Reserve inventory -> Authorize payment -> Confirm order`. Viết state machine, compensation matrix và test mọi điểm crash. Không gọi compensation là rollback: nó là business action mới và cũng có thể thất bại.

## 3. Resilience budget

Thiết kế theo thứ tự:

1. end-to-end deadline;
2. per-hop timeout nhỏ hơn budget còn lại;
3. retry chỉ với lỗi transient và operation idempotent;
4. exponential backoff + jitter;
5. circuit breaker để ngừng gọi dependency đang hỏng;
6. bulkhead/concurrency limit để cô lập tài nguyên;
7. fallback chỉ khi business chấp nhận dữ liệu giảm chất lượng.

Retry ở nhiều layer có thể nhân tải theo cấp số nhân. Ghi rõ layer nào sở hữu retry và maximum attempts toàn call chain.

## 4. CQRS và Event Sourcing

CQRS không bắt buộc Event Sourcing. Dùng decision matrix:

| Nhu cầu | CRUD | CQRS | Event Sourcing |
|---|---:|---:|---:|
| Domain đơn giản | Tốt | Thường quá mức | Quá mức |
| Read/write model khác mạnh | Có thể | Tốt | Có thể |
| Audit lịch sử đầy đủ | Bổ sung audit | Bổ sung audit | Native benefit |
| Rebuild state tại thời điểm bất kỳ | Khó | Tùy nguồn sự kiện | Tốt |
| Operational complexity thấp | Tốt | Trung bình | Kém |

Nếu chọn Event Sourcing, phải trả lời snapshot, event schema evolution, projection rebuild, GDPR/delete semantics và xử lý bug trong historical event.

## 5. Security và observability

- OAuth2/OIDC flow phải phù hợp client type; JWT không mặc định tốt hơn opaque token.
- Key rotation, audience/issuer validation, clock skew và token revocation phải có thiết kế.
- Trace/span phải thể hiện Saga ID, message ID và causation/correlation ID với cardinality an toàn.
- Có runbook cho lag, poison message, partial outage và replay.

## Câu hỏi phỏng vấn

### 1. Chọn choreography hay orchestration theo tiêu chí nào?

**Trả lời:** Choreography hợp flow ngắn, phản ứng độc lập và ownership phân tán; giảm coordinator nhưng global flow khó thấy, cycle/version/debug dễ phức tạp. Orchestration hợp workflow dài, state/timeout/compensation/audit rõ và cần operator visibility; đổi lại orchestrator là component phải scale/recover và không được nuốt domain logic của service.

Chọn theo số bước, branching, timeout, compliance, change ownership và nhu cầu quan sát—not theo số service. Dù chọn gì, có correlation, idempotency và explicit state/failure matrix.

### 2. Compensation thất bại thì workflow đi đâu?

**Trả lời:** Vào trạng thái explicit như `COMPENSATION_PENDING/FAILED`, retry idempotent theo budget, sau đó DLQ/operator queue và reconciler. Compensation là business action mới, không phải rollback ACID; nó cũng có authorization, timeout và có thể không hoàn tác hoàn toàn.

Persist saga state/attempt/error, alert theo age/SLO và cung cấp safe replay/manual resolution với audit. Định nghĩa forward recovery nếu hoàn tác không khả thi.

### 3. Circuit breaker khác rate limiter và bulkhead thế nào?

**Trả lời:** Circuit breaker ngừng gọi dependency có tỷ lệ lỗi/slow cao để fail fast và cho hồi phục. Rate limiter giới hạn tốc độ theo thời gian/identity để bảo vệ quota/capacity/abuse. Bulkhead giới hạn concurrency/resource theo workload để lỗi/saturation một nhóm không chiếm hết hệ thống.

Chúng giải quyết failure khác nhau và thường phối hợp: rate limit trước admission, bulkhead quanh dependency, circuit dựa trên outcome. Cấu hình sai có thể reject khỏe, oscillate hoặc che root cause.

### 4. CQRS có cần hai database không?

**Trả lời:** Không. CQRS là tách command model khỏi query model/contract; có thể cùng database/schema, view/materialized view hoặc storage riêng. Hai DB chỉ đáng dùng khi scale, shape, availability hoặc lifecycle read/write khác đủ mạnh.

Storage riêng tạo projection lag, replay, dual operations, schema evolution và reconciliation. Bắt đầu logical separation rồi tách vật lý khi metric/requirement chứng minh.

### 5. Làm sao replay event mà không gửi lại email/thanh toán?

**Trả lời:** Tách deterministic projection handlers khỏi irreversible side-effect handlers. Mỗi effect có event/effect ID và inbox/idempotency record hoặc provider idempotency key; replay mode chỉ rebuild projection, hoặc effect gateway kiểm ledger và skip effect đã hoàn tất.

Không reset offset rồi hy vọng consumer tự biết. Version replay job, dùng isolated consumer group/output store, disable/route external effect rõ và reconcile count/hash trước cutover.
