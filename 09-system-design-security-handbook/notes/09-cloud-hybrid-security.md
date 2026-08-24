# 09 — Cloud và hybrid security

## Shared responsibility

Provider bảo vệ **of the cloud**; customer bảo vệ cấu hình, identity, data và workload **in the cloud**. Tỷ lệ thay đổi theo IaaS/PaaS/SaaS/serverless. Managed service giảm patch/OS scope nhưng không chuyển data classification, IAM, network exposure, application authorization hay backup/retention decision.

Tạo RACI per service/control, không dùng một sơ đồ chung cho toàn cloud.

## Landing zone

- Organization/account/project hierarchy và production isolation.
- Central workforce federation, no routine root/owner, break-glass.
- Policy guardrail, allowed regions/services, mandatory tags/labels.
- Central immutable audit/security accounts/projects.
- Network topology, DNS, ingress/egress/private service access.
- KMS/secret, vulnerability, asset inventory và security findings.
- Account/project vending bằng IaC và approved baseline.
- Cost/quota/anomaly controls.

## Cloud-native identity

Human dùng federation/SSO + MFA, permission set/JIT. Workload dùng attached role/service account hoặc workload identity federation; tránh long-lived cloud access key. Cross-account/project access có exact trust condition, audience và external/subject identity.

## Hybrid

- Federate identity thay đồng bộ static credentials.
- Dual connectivity, route/DNS ownership, encryption và failover.
- Central log cần buffer khi WAN mất.
- Data residency/egress và key ownership.
- Không tạo transitive trust từ corporate network tới mọi cloud workload.
- Test compromised on-prem identity không chiếm cloud organization và ngược lại.

## Multi-cloud

Chỉ dùng khi business/regulatory/capability/exit requirement trả được chi phí. “Portable” lowest-common-denominator có thể mất managed security benefits. Giữ control objectives chung nhưng implementation native; central inventory/identity/telemetry/schema và provider-specific expertise.
