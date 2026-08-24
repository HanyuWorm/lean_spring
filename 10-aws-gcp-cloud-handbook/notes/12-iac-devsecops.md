# 12 — IaC và DevSecOps

## Tooling

- **AWS CloudFormation:** native declarative stacks; drift/change sets.
- **AWS CDK:** define infrastructure bằng programming language rồi synthesize CloudFormation.
- **Terraform/OpenTofu:** multi-provider declarative state; provider/version/state lifecycle.
- **GCP Infrastructure Manager:** managed Terraform deployment/preview/state workflow.
- **Google Cloud Deployment Manager:** đã hết support từ 01/04/2026; không chọn cho greenfield.

## IaC lifecycle

Module/version → lint/validate → policy/security scan → plan/change set → peer/approval → apply bằng workload identity → verify/drift → evidence. Không apply từ laptop admin cho production.

State chứa topology và có thể có sensitive values: encrypted remote backend, strict IAM, locking/versioning/backup, separate per environment. Secret reference chứ không plaintext trong code/state/plan output khi provider/resource cho phép.

## CI identity

GitHub/GitLab/CI phát OIDC token, cloud trust policy check exact issuer/audience/org/repo/branch/environment, rồi cấp temporary role/service-account impersonation. Một deployment identity mỗi environment/pipeline, least privilege; không lưu AWS access key/GCP JSON key.

## Guardrails

Organization policy/SCP đặt maximum boundary; policy-as-code tại PR/plan; admission/runtime config detection. Pin provider/module/action/image version/digest, SBOM/provenance/sign artifact. Separate bootstrap pipeline vì nó có quyền tạo trust/organization controls.

## Deployment

Immutable artifact promote qua environments; blue-green/canary/rolling theo service. Database migration expand-contract và rollback/roll-forward. Validate health bằng user SLI, không chỉ process alive.
