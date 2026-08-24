# Chặng 7 - Solution Architect Track

Thời lượng: tuần 17-20. Track này dùng capstone Order & Payment làm case study xuyên suốt.

Mục tiêu là chuyển từ tư duy “service này nên code thế nào?” sang “business cần năng lực gì, quality attribute nào quyết định kiến trúc, pattern nào phù hợp và tổ chức sẽ vận hành nó ra sao?”.

## Lộ trình bốn tuần

| Tuần | Trọng tâm | Sản phẩm bắt buộc |
|---:|---|---|
| 17 | [API design patterns](API_DESIGN_PATTERNS.md) | API style decision, OpenAPI/AsyncAPI, error/idempotency/versioning policy |
| 18 | [System design patterns](SYSTEM_DESIGN_PATTERNS.md) | Context/container/component diagrams, pattern decision matrix |
| 19 | [Architecture review](ARCHITECTURE_REVIEW_CHECKLIST.md) | NFR scenarios, capacity model, failure/threat/cost review, ADR pack |
| 20 | [Architecture board case study](CASE_STUDY.md) | 45-minute presentation, objections, phased migration roadmap |

## Tư duy của Solution Architect

```text
Business driver
    -> constraints và quality attributes
       -> architecture options
          -> trade-off + risk + cost
             -> decision/ADR
                -> measurable fitness functions
                   -> roadmap và governance
```

Pattern không phải điểm bắt đầu. Nếu chưa biết traffic, consistency, RTO/RPO, security classification, team ownership và budget thì chưa đủ dữ liệu để chọn microservices, Kafka, Event Sourcing hoặc multi-region.

## Bốn cấp độ tài liệu

1. **Executive:** business outcome, risk, cost range và roadmap; không chứa class diagram.
2. **Architecture:** context/container, trust boundary, data ownership, integration và quality attributes.
3. **Engineering:** component, sequence, API/event contract, failure semantics và deployment.
4. **Operations:** SLO, capacity, observability, runbook, DR và cost controls.

Một kiến trúc tốt phải truy vết được từ business driver xuống fitness function. Ví dụ: “checkout không mất đơn” phải dẫn đến idempotency, durable intent/outbox, duplicate handling, monitoring và recovery procedure cụ thể.

## Artifact portfolio cần có

- System context và container diagram;
- component diagram cho một bounded context;
- hai sequence diagram: happy path và partial failure;
- OpenAPI cho synchronous boundary và AsyncAPI/event catalog cho asynchronous boundary;
- quality attribute scenarios có số đo;
- capacity model và bottleneck assumptions;
- data classification/data-flow diagram và threat model;
- failure mode matrix, RTO/RPO, backup/restore evidence;
- 10 ADR quan trọng;
- roadmap `current -> transition -> target` kèm dependency, risk và rollback.

## Definition of Done

- Mỗi pattern đều có context, forces, decision, consequence và anti-pattern.
- Không dùng “best practice” làm lý do nếu không gắn với constraint của case study.
- Có ít nhất hai option thật sự khả thi trước khi chốt kiến trúc.
- Mọi cơ chế retry/cache/replication đều nói rõ consistency và failure behavior.
- Có cost/operability/team-skill trong decision matrix, không chỉ performance.
- Bảo vệ thiết kế trong 45 phút và trả lời phản biện trong 15 phút.

