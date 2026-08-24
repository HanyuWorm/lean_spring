# Architecture Board Case Study

## Bối cảnh

Công ty thương mại điện tử đang có modular monolith Spring Boot, PostgreSQL và 3 triệu order/tháng. Trong 18 tháng tới cần:

- mở rộng sang ba quốc gia và nhiều payment provider;
- peak sale dự kiến tăng 8 lần;
- mobile/web cần API khác nhau;
- không duplicate charge, order không được mất;
- catalog có thể stale 5 phút, payment/order thì không;
- hỗ trợ customer-service search/RAG nhưng không lộ PII;
- RTO 30 phút, RPO 5 phút cho order; payment reconciliation phải đầy đủ;
- team hiện có 5 squad, kinh nghiệm Kafka/Kubernetes ở mức trung bình;
- migration không được dừng bán hàng và phải cho rollback theo phase.

## Nhiệm vụ

Thiết kế `current -> transition -> target architecture` cho 18 tháng. Không bắt buộc microservices; phải chứng minh vì sao tách hoặc giữ từng boundary.

## Artifact phải nộp

1. Business capability map và bounded contexts.
2. Context/container diagrams với protocol, data và trust/failure boundary.
3. API style matrix cho web/mobile/internal/event/webhook.
4. OpenAPI cho create/query/cancel order, gồm idempotency, pagination, error và concurrency.
5. Event catalog/AsyncAPI cho `OrderPlaced`, `PaymentAuthorized`, `OrderConfirmed`.
6. Sequence diagram happy path và payment timeout/duplicate event.
7. Capacity model tại current, peak x8 và recovery replay.
8. Consistency matrix cho order/payment/inventory/catalog/read model.
9. Failure mode, security/threat, DR và cost review.
10. Ít nhất 8 ADR và phased migration/rollback plan.

## Các quyết định phải bảo vệ

- Modular monolith, modulith hay service extraction?
- REST, gRPC, GraphQL, async event và BFF dùng ở boundary nào?
- Transactional Outbox/Debezium hay application publisher?
- Saga choreography hay orchestration?
- Cache/read replica/CQRS cho journey nào và freshness bao nhiêu?
- Virtual Threads thay đổi capacity/backpressure thế nào?
- Single-region multi-AZ hay multi-region? Khi nào nâng cấp?
- Spring AI/pgvector nằm ở boundary nào và degrade ra sao?

## Phản biện giả lập

Chuẩn bị trả lời ngắn, có evidence:

1. CFO yêu cầu giảm 30% cost: bỏ thành phần nào trước?
2. CTO muốn microservices toàn bộ trong sáu tháng: risk và alternative?
3. Payment provider p99 tăng từ 500 ms lên 8 giây trong sale.
4. Kafka unavailable 45 phút, backlog tăng mạnh sau recovery.
5. Một event schema sai đã cập nhật read model trong hai giờ.
6. Region chính mất hoàn toàn; backup gần nhất cách 10 phút.
7. Một tenant lớn chiếm 60% DB connections.
8. Mobile client cũ không nâng cấp trong sáu tháng.

## Format buổi review

- 5 phút: business drivers và constraints;
- 10 phút: current/target/transition architecture;
- 10 phút: API/data/consistency decisions;
- 10 phút: scale, resilience, security, DR và cost;
- 10 phút: migration roadmap và risks;
- 15 phút: phản biện.

## Rubric

| Năng lực | Trọng số |
|---|---:|
| Traceability từ business/NFR tới decision | 20% |
| API/data/integration correctness | 20% |
| Scale/resilience/failure reasoning | 20% |
| Security/operability/DR/cost | 20% |
| Communication, option analysis và roadmap | 20% |

Không cộng điểm chỉ vì số lượng công nghệ. Một target đơn giản, tiến hóa được và có evidence tốt hơn một diagram nhiều boxes nhưng thiếu ownership/failure model.

