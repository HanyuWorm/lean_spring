# 09 — Bản đồ dịch vụ AWS dành cho DevOps

## 1. Account và identity trước pipeline

### Organizations và account boundary

- Tách production, non-production, security/log archive và shared services theo blast radius.
- Organizational Unit nhóm account để áp guardrail; SCP đặt permission ceiling, không tự cấp quyền.
- Identity Center/federation cho human; IAM role/workload identity cho automation.
- Không dùng root user cho vận hành thường ngày; bảo vệ root credentials và recovery path.

### IAM và STS

- Role policy trả lời role được làm gì.
- Trust policy trả lời principal nào được assume role và với condition nào.
- STS credential ngắn hạn giảm lifetime nhưng role quá rộng vẫn nguy hiểm.
- Permission boundary giới hạn maximum permission của identity; resource policy/SCP/session policy còn tham gia evaluation.

Pipeline cross-account nên assume deployment role riêng ở từng environment. Tooling account không giữ permanent admin key của production.

## 2. Source, build, artifact và orchestration

| Capability | AWS service | Điểm cần nhớ |
|---|---|---|
| Pipeline orchestration | CodePipeline | stage/action/artifact/conditions; không phải build engine |
| Build/test | CodeBuild | ephemeral build environment, buildspec, cache/artifact |
| Deployment | CodeDeploy | EC2/on-prem, ECS, Lambda; in-place/blue-green/traffic shifting tùy target |
| Package/image | CodeArtifact, ECR | version/digest, scanning, lifecycle, cross-account access |
| IaC | CloudFormation, CDK | stack/change set, drift; CDK synthesize CloudFormation |
| External CI | GitHub Actions/GitLab/Jenkins | dùng OIDC/role, không static access key |

CodePipeline stage chứa serial/parallel actions và có entry/on-failure/on-success conditions. CloudWatch alarm có thể làm gate/rollback signal; vẫn cần hiểu metric delay và false positive.

## 3. Compute/deployment targets

### EC2 và Auto Scaling

- Golden AMI/immutable replacement tốt hơn SSH patch drift.
- Launch Template version, health check, lifecycle hook và instance refresh.
- Systems Manager thay bastion/SSH trực tiếp khi phù hợp.
- CodeDeploy minimum healthy hosts và zonal behavior phải khớp capacity.

### ECS

- Task definition là versioned workload contract; service giữ desired tasks.
- Fargate giảm node operations; EC2 capacity provider cho kiểm soát/cost khác.
- Rolling hoặc CodeDeploy blue/green; ALB target group và health check là deployment gate.
- Task role khác execution role.

### EKS

- AWS quản control plane; customer vẫn sở hữu workload, IAM/RBAC, data plane choices, add-ons, upgrade và nhiều security controls.
- Managed node group vẫn cần customer initiate update/roll patched AMI.
- EKS Auto Mode tăng phần managed nhưng không xóa trách nhiệm application/SLO/data/security.
- IAM Roles for Service Accounts hoặc EKS Pod Identity cấp workload permission, tránh node role rộng.

### Lambda

- Version + alias tạo immutable deploy target.
- CodeDeploy canary/linear/all-at-once traffic shifting.
- Monitor error, throttle, duration, concurrency và downstream saturation.
- Reserved concurrency vừa bảo vệ function capacity vừa là bulkhead cho downstream.

## 4. Observability và audit

| Dịch vụ | Vai trò |
|---|---|
| CloudWatch Metrics/Logs/Alarms/Dashboards | workload/resource telemetry và alert |
| CloudWatch Synthetics/RUM | synthetic journey và user experience |
| X-Ray / OpenTelemetry integration | distributed tracing |
| CloudTrail | AWS API audit; management/data events theo cấu hình |
| AWS Config | resource configuration history/rules/conformance |
| EventBridge | route events tới automation/notification |
| Systems Manager OpsCenter/Automation | operational work item và runbook automation |

CloudTrail không thay application audit. CloudWatch alarm không thay SLO. Config rule không chặn mọi thay đổi tức thì trừ khi kết hợp preventive control khác.

## 5. Security và compliance

- KMS quản key/control/audit boundary; hiểu key policy + IAM policy.
- Secrets Manager/Parameter Store theo sensitivity/rotation/use case.
- Inspector scan workload/package exposure; ECR scanning cho image.
- Security Hub tổng hợp/chuẩn hóa findings; GuardDuty phát hiện threat signals.
- WAF/Shield bảo vệ edge theo lớp; không sửa authorization bug trong application.
- Control Tower giúp thiết lập/govern landing zone, không thay design account/network/IAM.

## 6. Event và remediation

```text
CloudWatch alarm / Config noncompliance / CloudTrail event
        -> EventBridge rule
        -> SNS notification + Systems Manager Automation/Lambda
        -> verify result
        -> audit + ticket/incident
```

Automation cần:

- exact scope và idempotency;
- rate/concurrency limit;
- dry-run hoặc approval cho high blast radius;
- rollback/stop condition;
- separate execution role least privilege;
- metrics và audit cho chính automation.

## 7. DOP-C02 capability map

Sáu domain hiện hành:

1. SDLC Automation — 22%.
2. Configuration Management and IaC — 17%.
3. Resilient Cloud Solutions — 15%.
4. Monitoring and Logging — 15%.
5. Incident and Event Response — 14%.
6. Security and Compliance — 17%.

Dùng tỷ trọng để phát hiện lỗ hổng học tập, không học mẹo exam thay cho lab/game day.

Nguồn: [DOP-C02 exam guide](https://docs.aws.amazon.com/aws-certification/latest/devops-engineer-professional-02/devops-engineer-professional-02.html), [CodePipeline concepts](https://docs.aws.amazon.com/codepipeline/latest/userguide/concepts.html), [EKS Best Practices](https://docs.aws.amazon.com/eks/latest/best-practices/introduction.html).

