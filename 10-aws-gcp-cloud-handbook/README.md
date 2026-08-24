# AWS & Google Cloud Handbook

Track cloud cho senior Java/Spring Boot và solution architect. Mục tiêu là hiểu component làm gì, boundary/availability/cost/security của nó, rồi mới học tên dịch vụ.

> Snapshot **24/08/2026**. AWS: Well-Architected 6 pillars. Google Cloud: Well-Architected 6 pillars, Cloud Functions 2nd gen mang tên **Cloud Run functions**, Deployment Manager đã hết support từ 01/04/2026 nên IaC mới dùng Terraform/Infrastructure Manager.

## Kết quả cần đạt

- Hiểu region, zone, control/data plane, shared responsibility và cloud economics.
- Thiết kế organization/landing zone, identity và network trước workload.
- Chọn VM, container, Kubernetes hay serverless dựa trên workload.
- Chọn storage/database/messaging theo consistency, access pattern và SLO.
- Thiết kế security, observability, HA/DR, cost guardrail và IaC.
- Ánh xạ AWS ↔ GCP mà không coi hai dịch vụ là giống hệt.
- Viết ADR và defend cloud architecture bằng trade-off/evidence.

## Nội dung

| Chương | Nội dung |
|---|---|
| [01](notes/01-cloud-foundations.md) | Cloud fundamentals và Well-Architected |
| [02](notes/02-organization-governance-iam.md) | Landing zone, hierarchy, IAM |
| [03](notes/03-networking.md) | VPC, subnet, route, LB, DNS, CDN, hybrid |
| [04](notes/04-compute-containers-serverless.md) | VM, ECS/GKE/EKS, Cloud Run/Lambda |
| [05](notes/05-storage.md) | Object, block, file, lifecycle và backup |
| [06](notes/06-databases-analytics.md) | RDS/Aurora/DynamoDB và Cloud SQL/AlloyDB/Spanner |
| [07](notes/07-messaging-integration.md) | SQS/SNS/EventBridge/Kinesis và Pub/Sub/Eventarc |
| [08](notes/08-security-services.md) | KMS, secrets, WAF, threat posture/audit |
| [09](notes/09-observability-operations.md) | Metrics/logs/traces/audit/config/SSM |
| [10](notes/10-reliability-dr.md) | Multi-AZ/zone, region, RPO/RTO, chaos |
| [11](notes/11-cost-finops.md) | Pricing dimensions, budgets, commitment, unit cost |
| [12](notes/12-iac-devsecops.md) | Terraform, CloudFormation/CDK, Infra Manager, CI OIDC |
| [13](notes/13-reference-architectures.md) | Java/Spring architectures AWS và GCP |
| [14](notes/14-aws-gcp-service-map.md) | Bảng ánh xạ dịch vụ chi tiết |
| [15](notes/15-interview-questions.md) | 50 câu hỏi có đáp án |

Thực hành theo [labs](labs/README.md), dùng template [workload assessment](templates/workload-assessment.md) và [ADR](templates/cloud-adr.md). Không có lab nào tự động `apply` hoặc tạo resource có phí.

## Quy tắc học

- Không học tên service mà bỏ qua scope: global/regional/zonal và data/control plane.
- “Managed” không có nghĩa customer hết trách nhiệm.
- Multi-AZ/zone không tự là multi-region DR.
- Serverless không tự rẻ; tính duration, request, concurrency, network và downstream.
- Không dùng một cloud account/project cho cả công ty và mọi environment.
- Tránh long-lived access key/service-account key; dùng federation/attached workload identity.
- Mọi kiến trúc có SLO, RPO/RTO, quota, cost model và exit strategy.
