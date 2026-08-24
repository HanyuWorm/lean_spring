# 08 — Security services

## Capability map

| Capability | AWS | GCP | Làm gì |
|---|---|---|---|
| IAM/federation | IAM, Identity Center, STS | IAM, Cloud Identity, Workforce/Workload Identity Federation | human/workload access |
| Key management | KMS, CloudHSM | Cloud KMS, Cloud HSM | cryptographic keys, envelope encryption |
| Secrets | Secrets Manager, Parameter Store | Secret Manager | store/version/rotate/audit secrets |
| Edge protection | Shield, WAF | Cloud Armor | DDoS và L7 filtering |
| Threat detection | GuardDuty | Security Command Center threat capabilities | analyze telemetry/findings |
| Posture/findings | Security Hub CSPM, Config | Security Command Center, Organization Policy | aggregate posture/misconfiguration |
| Vulnerability | Inspector, ECR scanning | Artifact Analysis/VM Manager capabilities | image/package/VM findings |
| Audit | CloudTrail | Cloud Audit Logs | control/data access trail theo config |
| Data discovery | Macie | Sensitive Data Protection | classify/discover sensitive data |
| Service perimeter | account/VPC endpoint/SCP patterns | VPC Service Controls | reduce data exfiltration boundary |

Tên service không phải control design. Ví dụ KMS key tồn tại nhưng application role có `Decrypt *` thì blast radius vẫn lớn.

## Practices

- Central delegated security administration và log archive account/project.
- Organization-wide audit/findings/asset inventory; alert khi logging/control bị disable.
- Temporary federated credentials; no routine root/owner.
- Customer-managed key chỉ khi requirement/ownership/rotation justify operation.
- Private endpoints + egress control cho sensitive services.
- Findings có severity + asset/business context + owner/SLA; không chỉ dashboard.
- Incident automation có human guardrail cho destructive containment.
