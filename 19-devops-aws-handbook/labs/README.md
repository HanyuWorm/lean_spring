# DevOps & AWS DevOps labs

Các lab mặc định không tạo resource cloud có phí. Chỉ chạy `apply` AWS khi bạn đã tự chọn account/scope, kiểm tra plan và có budget guardrail.

## Lab 1 — Linux/network incident

1. Chạy một Spring Boot project trong workspace.
2. Ghi PID, port listener, RSS, FD count và process tree.
3. Tạo ba lỗi: port sai, DNS sai và dependency timeout.
4. Viết runbook phân biệt symptom bằng lệnh/evidence.
5. Acceptance: người khác làm runbook có thể xác định đúng layer trong 10 phút.

## Lab 2 — CI quality gates

Dùng [`github-actions/ci-aws-oidc.example.yml`](github-actions/ci-aws-oidc.example.yml) làm skeleton, không copy nguyên production.

- Tách PR checks và publish/deploy.
- `permissions` read-only mặc định.
- Thêm timeout, concurrency và artifact retention.
- Chứng minh untrusted PR không lấy AWS token.
- Acceptance: mỗi artifact truy ngược được commit SHA và workflow run.

## Lab 3 — Docker

- Chọn project `07-virtual-threads-system-design` hoặc một Spring project độc lập.
- Viết multi-stage Dockerfile, non-root và graceful shutdown.
- Build hai lần để quan sát cache; thay source và dependency để xem layer invalidation.
- Chạy với memory/CPU limit, tạo tải và quan sát throttle/OOM behavior.
- Scan image; phân loại CVE theo severity, fix availability và reachability.

## Lab 4 — Kubernetes

Dùng [`kubernetes/deployment.yaml`](kubernetes/deployment.yaml) làm review exercise:

```bash
kubectl apply --dry-run=server -f kubernetes/deployment.yaml
kubectl diff -f kubernetes/deployment.yaml
```

Sửa placeholder image digest trước khi deploy. Tái hiện:

- readiness fail nhưng container không restart;
- liveness fail dẫn restart;
- rollout với một pod không ready;
- drain node khi PDB giới hạn disruption;
- HPA scale nhưng downstream bị giới hạn.

## Lab 5 — Terraform local, không tốn phí

Folder [`terraform-local`](terraform-local/) chỉ dùng built-in `terraform_data`:

```bash
terraform -chdir=terraform-local init
terraform -chdir=terraform-local fmt -check
terraform -chdir=terraform-local validate
terraform -chdir=terraform-local plan -out=tfplan
terraform -chdir=terraform-local apply tfplan
```

Sau đó chạy hai apply đồng thời để quan sát locking của backend hiện dùng. Local state không phù hợp teamwork; bài tập thiết kế remote backend gồm encryption, versioning, access, audit, locking và recovery.

## Lab 6 — AWS pipeline design, không apply

Thiết kế trên giấy/IaC pseudocode:

- GitHub OIDC → tooling role.
- Build image → ECR immutable digest + SBOM/signature.
- Assume staging/prod deployment roles.
- ECS blue/green hoặc EKS GitOps.
- CloudWatch technical + business gates.
- CloudTrail/Config/log archive và break-glass.

Review bằng [pipeline checklist](../templates/pipeline-review.md). Acceptance: có failure matrix cho OIDC claim sai, artifact/KMS deny, health false positive, DB migration partial và automatic rollback failure.

## Lab 7 — SLO/game day

1. Chọn user journey và định nghĩa good/valid events.
2. Đặt SLO, window và error-budget policy.
3. Tạo dashboard RED + downstream saturation.
4. Inject latency/error; đo detection/acknowledge/mitigation/recovery.
5. Viết [postmortem](../templates/postmortem.md) và ít nhất một regression guardrail.

