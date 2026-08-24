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

1. Chọn choreography hay orchestration theo tiêu chí nào?
2. Compensation thất bại thì workflow đi đâu?
3. Circuit breaker khác rate limiter và bulkhead thế nào?
4. CQRS có cần hai database không?
5. Làm sao replay event mà không gửi lại email/thanh toán?
