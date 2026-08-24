# Labs — không tự tạo resource có phí

## Nguyên tắc an toàn

Các bài chỉ tạo design/plan. Không chạy `terraform apply`, không bật service hay tạo cloud account/project tự động. Trước mọi lab thật: budget alert, MFA/federation, sandbox account/project riêng, region và teardown owner.

## Lab 1 — Landing zone

Vẽ:

- AWS Organization với Security/Infrastructure/Workloads/Sandbox OUs; management, log archive, audit, network, prod/non-prod accounts; SCP và Identity Center.
- GCP Organization với Bootstrap/Common/Production/Non-production/Sandbox folders; logging/security/network/workload projects; Organization Policies và federation.

Ghi rõ inheritance, break-glass, billing, account/project vending và central logs.

## Lab 2 — Network

Thiết kế 3 zones, public edge và private application/data. Tính CIDR, route, NAT/egress, private service endpoint, DNS, hybrid dual links và failure. So sánh AWS subnet AZ-scoped với GCP subnet regional.

## Lab 3 — Spring Boot deployment

Cùng service REST + PostgreSQL + event:

- AWS: ALB/API Gateway, ECS Fargate hoặc EKS, Aurora/RDS, SQS/EventBridge, roles/KMS/secrets, CloudWatch.
- GCP: Load Balancing/API Gateway, Cloud Run hoặc GKE, AlloyDB/Cloud SQL, Pub/Sub/Eventarc, workload identity/KMS/secrets, Cloud Operations.

Tính max instances × Hikari pool, downstream connection budget, autoscaling metric, timeout/retry và cost/order.

## Lab 4 — DR game day

Giả lập zone rồi region failure. Viết detection, traffic failover, data promotion/fencing, capacity/quota, DNS/cache, credential/key và validation. Đo RPO/RTO; thêm logical corruption để chứng minh replica không thay backup.

## Lab 5 — CI không static key

Viết trust policy pseudocode cho GitHub Actions OIDC:

- exact issuer/audience;
- exact organization/repository;
- production chỉ protected environment/branch;
- separate deploy identity per environment;
- short session, least privilege;
- audit subject/commit/workflow.

## Lab 6 — Cost review

Ước lượng compute, DB, storage/operations, messages, logs, NAT/LB/IP và egress cho average/peak. Tạo unit cost và ba phương án optimization không phá SLO/security.

## Definition of done

Mỗi lab có diagram, assumptions, quotas, RACI, SLO/RPO/RTO, security controls, monthly cost range, failure modes và ADR. Tên dịch vụ không được thay lời giải thích component làm gì.
