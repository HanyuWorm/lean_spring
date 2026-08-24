# Architecture Review Checklist

Dùng checklist này trước architecture review board. Một mục không áp dụng phải ghi `N/A` kèm lý do.

## 1. Business và scope

- [ ] Business outcome, actors và critical journeys đã rõ.
- [ ] In-scope/out-of-scope và assumptions được ghi.
- [ ] Constraints về timeline, budget, team, vendor, region và compliance đã xác nhận.
- [ ] Success metric và guardrail metric có owner.
- [ ] Current-state pain có số liệu, không chỉ nhận xét.

## 2. Quality Attribute Scenario

Viết theo format:

```text
Source -> Stimulus -> Environment -> Artifact -> Response -> Measurable response
```

Ví dụ: `Peak traffic -> 2,000 checkout/s -> payment latency p99 2s -> checkout -> reject overload early -> p99 < 1.5s, error < 1%, không duplicate charge`.

- [ ] Availability SLO và error budget.
- [ ] Latency p95/p99 và throughput peak.
- [ ] Consistency/freshness requirement theo journey.
- [ ] RTO/RPO theo data/service tier.
- [ ] Security/privacy/data-residency requirements.
- [ ] Modifiability: thay đổi nào phải rẻ/nhanh trong 12-24 tháng.
- [ ] Cost ceiling hoặc unit economics.

## 3. API và integration

- [ ] Sync/async style có lý do.
- [ ] Contract, ownership và source of truth rõ.
- [ ] Timeout/deadline/retry/idempotency được định nghĩa.
- [ ] Error/partial success/cancellation semantics rõ.
- [ ] Versioning/deprecation/compatibility policy có telemetry.
- [ ] Event ordering, delivery, duplicate, schema evolution và replay rõ.
- [ ] Không có synchronous cycle giữa services.

## 4. Data

- [ ] Data owner, classification và lifecycle/retention.
- [ ] Transaction/invariant boundary và isolation level.
- [ ] Consistency model/read-after-write behavior.
- [ ] Partition key/index/growth/archive plan.
- [ ] Cache source of truth, TTL, invalidation và failure behavior.
- [ ] Backup/restore đã có test evidence.
- [ ] Migration/reconciliation/data-quality plan.

## 5. Capacity worksheet

```text
peak concurrency ≈ peak arrival rate × p95 service time
DB concurrent work ≈ arrival rate × connection hold time
daily storage ≈ events/second × average event bytes × 86,400
network egress ≈ responses/second × average response bytes
```

- [ ] Average, peak và burst assumptions.
- [ ] CPU, memory, connection, queue, partition và storage limits.
- [ ] Bottleneck đầu tiên và saturation signal.
- [ ] Headroom, growth forecast và scale trigger.
- [ ] Load-test model giống traffic/data distribution thực tế.

## 6. Failure mode matrix

| Failure | Detection | Automatic response | Data impact | Operator action | Recovery test |
|---|---|---|---|---|---|
| Downstream timeout |  |  |  |  |  |
| DB pool exhausted |  |  |  |  |  |
| Broker unavailable |  |  |  |  |  |
| Duplicate/out-of-order event |  |  |  |  |  |
| Cache unavailable/stale |  |  |  |  |  |
| AZ/region loss |  |  |  |  |  |
| Bad deployment/schema |  |  |  |  |  |
| Credential/key compromise |  |  |  |  |  |

## 7. Security và compliance

- [ ] Data-flow/trust-boundary diagram.
- [ ] Authentication, service identity và object-level authorization.
- [ ] Least privilege cho runtime, CI/CD, operator và database.
- [ ] Encryption in transit/at rest và key rotation.
- [ ] Secret storage/rotation/revocation.
- [ ] Input/output validation và abuse/rate-limit model.
- [ ] Audit evidence và sensitive-data redaction.
- [ ] Threat model có mitigation, residual risk và owner.

## 8. Operability

- [ ] SLI/SLO/dashboard/alert map tới critical journeys.
- [ ] Distributed trace/correlation qua message boundary.
- [ ] Health probes và graceful shutdown/drain.
- [ ] Runbook cho degradation, replay, restore và failover.
- [ ] Deployment/canary/rollback và schema compatibility.
- [ ] On-call ownership, support/escalation và game day.
- [ ] Capacity/cost/security fitness functions chạy định kỳ.

## 9. Cost và sustainability

- [ ] Monthly run cost theo environment và unit cost theo transaction/tenant.
- [ ] Cost ở normal, peak và DR mode.
- [ ] Data transfer, log/trace, backup và license không bị bỏ quên.
- [ ] Autoscaling có upper bound và abuse protection.
- [ ] Build/buy và lock-in/reversibility được đánh giá.
- [ ] Có phương án đơn giản hơn nếu traffic thấp hơn forecast.

## 10. ADR template

```markdown
# ADR-NNN: Decision title

## Status
Proposed | Accepted | Superseded | Deprecated

## Context and drivers
Business, constraints, quality attributes, evidence.

## Options considered
At least two viable options.

## Decision
What is selected and scope of the decision.

## Consequences
Positive, negative, risks, cost and organizational impact.

## Fitness functions
How the decision is continuously verified.

## Migration and rollback
Transition steps, compatibility, rollback trigger.

## Review date
When and what condition causes re-evaluation.
```

## 11. Review outcome

Chỉ dùng bốn trạng thái:

- `Approved`: evidence đủ và risk được chấp nhận.
- `Approved with actions`: action có owner/deadline, không phải ghi chú mơ hồ.
- `Revise`: thiếu decision/evidence quan trọng; nêu rõ exit criteria.
- `Rejected`: option không đáp ứng driver/constraint; ghi lý do và alternative.

