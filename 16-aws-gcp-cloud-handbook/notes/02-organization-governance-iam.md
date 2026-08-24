# 02 — Organization, governance và IAM

## AWS hierarchy

```text
AWS Organization
└─ Organizational Units (OUs)
   └─ AWS Accounts
      └─ Resources
```

- **AWS Organizations:** quản lý nhiều account, consolidated billing và organization policies.
- **AWS Account:** isolation, quota, billing và security boundary quan trọng; không phải user account.
- **OU:** nhóm account theo control/workload purpose.
- **SCP:** permission guardrail đặt maximum permissions; không tự grant permission.
- **Control Tower:** orchestration landing zone, account vending và preventive/detective/proactive controls.
- **IAM Identity Center:** workforce SSO/federation và permission sets qua accounts.
- **IAM role/STS:** temporary credentials cho human/workload/cross-account.

Tách management, log archive, security/audit, shared network, production và non-production accounts. Root không dùng hàng ngày, không có access key; break-glass và alert.

## Google Cloud hierarchy

```text
Organization
└─ Folders
   └─ Projects
      └─ Resources
```

- **Organization:** root governance gắn Cloud Identity/Workspace domain.
- **Folder:** nhóm projects theo environment/business/control.
- **Project:** resource, API enablement, IAM, quota và billing association boundary.
- **Organization Policy:** constraint guardrail kế thừa xuống hierarchy.
- **IAM policy/role:** principal được permissions trên resource; inheritance theo hierarchy.
- **Cloud Identity/Workforce Federation:** human identities.
- **Service account:** non-human principal đồng thời là resource phải bảo vệ.
- **Workload Identity Federation:** external/on-prem/CI workload dùng short-lived identity thay service-account key.

Tách bootstrap/seed, security/logging, shared networking, production và non-production projects/folders.

## Policy evaluation key points

AWS có identity policy, resource policy, permission boundary, session policy, SCP và explicit deny. GCP IAM chủ yếu allow policy + deny policy/organization constraints/conditions. Không map syntax trực tiếp; test effective permission và escalation path (`PassRole`/service-account impersonation) bằng policy analyzer.

## Landing-zone minimum

Hierarchy, billing, federation, break-glass, policy guardrails, centralized audit, asset inventory, security findings, network/DNS, KMS/secrets, account/project vending, tag/label standard, budget/quota và incident access.
