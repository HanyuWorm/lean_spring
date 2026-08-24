# 11 — Incident, change, DR, FinOps và platform engineering

## 1. Incident lifecycle

```text
detect -> acknowledge -> classify -> stabilize/mitigate
       -> diagnose -> resolve -> recover -> learn -> prevent recurrence
```

Trong sự cố lớn tách vai trò:

- Incident Commander: mục tiêu, ưu tiên, quyết định và coordination.
- Operations lead: thực hiện mitigation/technical work.
- Communications lead: stakeholder/customer updates.
- Scribe: timeline, hypothesis, action, evidence.
- Subject matter experts tham gia theo yêu cầu, không tạo chat song song vô chủ.

Ưu tiên giảm user impact trước root cause hoàn hảo. Mỗi hành động có hypothesis, expected signal, owner và rollback/stop condition.

## 2. Severity

Severity dựa business/customer impact, scope, duration, data/security và workaround; không dựa độ “thú vị” kỹ thuật. Ví dụ:

- SEV1: critical journey diện rộng, data/security nghiêm trọng hoặc không workaround.
- SEV2: impact đáng kể nhưng scope/workaround hạn chế.
- SEV3: degraded/non-critical, xử lý trong giờ làm theo policy.

Định nghĩa phải phù hợp tổ chức và có SLA acknowledge/escalation.

## 3. Runbook và playbook

- Runbook: procedure đã biết cho thao tác cụ thể, ví dụ rotate certificate.
- Playbook: hướng điều tra/ra quyết định cho scenario rộng, ví dụ latency tăng.

Một runbook tốt có precondition, quyền cần, exact scope, safe commands, expected output, stop/escalate condition, rollback và validation.

## 4. Postmortem không đổ lỗi

Postmortem gồm:

- impact và detection gap;
- timeline dựa evidence;
- contributing technical/organizational factors;
- vì sao guardrail/test/alert không bắt được;
- điều gì làm mitigation chậm/nhanh;
- action có owner, due date, priority và verification.

Không dừng ở “human error”. Hỏi vì sao hành động đó hợp lý với context/tooling lúc ấy và guardrail nào đáng lẽ hạn chế blast radius.

## 5. Change management hiện đại

- Standard low-risk change được pre-authorize và tự động hóa.
- Normal change dùng review/gate theo risk.
- Emergency change dùng break-glass, audit và retrospective bắt buộc.
- Approval thủ công không có evidence không tạo safety; có thể chỉ tăng batch size/lead time.
- Small, frequent, reversible changes giảm blast radius.

Operational Readiness Review kiểm tra owner, SLO, telemetry, capacity, security, backup/restore, runbook, dependency và support trước production/peak event.

## 6. Backup và DR

- HA xử lý component failure; DR xử lý mất site/region/dữ liệu ở scope lớn.
- RPO: mất tối đa bao nhiêu dữ liệu theo thời gian.
- RTO: khôi phục service trong bao lâu.
- Backup tồn tại không chứng minh restore được.
- Replication có thể sao chép corruption/deletion; cần point-in-time/version/immutable backup phù hợp.

Game day phải đo actual RPO/RTO, DNS/credential/dependency/quota/capacity và quyết định failback.

## 7. FinOps trong DevOps

- Tag/account/namespace allocation giúp ownership nhưng không hoàn hảo.
- Đo unit cost như cost/order, cost/tenant, cost/build minute.
- CI runners, log retention, high-cardinality metrics, NAT/data transfer và idle non-prod là cost drivers phổ biến.
- Budget/forecast/anomaly alert có owner và action.
- Spot/commitment/rightsizing là trade-off reliability/flexibility, không áp một cách cơ học.

## 8. Platform engineering

Platform là product nội bộ cung cấp self-service capability qua golden paths:

- service template, pipeline, observability, identity, environment;
- guardrails/policy/secure defaults;
- documentation, support, version/migration;
- API/portal chỉ là interface, không phải toàn platform.

Đo adoption, time-to-first-deploy, lead time, failure/support rate và developer satisfaction. Không ép golden path khi nó không đáp ứng use case; có paved road và exception/extension model.

## 9. Automation safety

Automation sửa production cần:

- idempotency và bounded scope;
- concurrency/rate limit;
- circuit breaker/kill switch;
- dry-run/canary;
- approval theo blast radius;
- audit và telemetry;
- test failure path;
- fallback manual đã diễn tập.

Self-healing không đồng nghĩa restart vô hạn. Restart storm có thể che root cause và làm dependency sập nhanh hơn.

Nguồn: [AWS Operational Excellence — Operate](https://docs.aws.amazon.com/wellarchitected/latest/operational-excellence-pillar/operate.html), [AWS Implement change](https://docs.aws.amazon.com/wellarchitected/latest/reliability-pillar/implement-change.html).

