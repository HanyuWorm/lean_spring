# Lộ trình DevOps & AWS DevOps trong 16 tuần

Nhịp đề xuất: 8–10 giờ/tuần. Mỗi tuần phải tạo một artifact có thể review; xem video hoặc đọc tài liệu chưa được tính là hoàn thành.

## Giai đoạn 1 — Nền tảng hệ điều hành và mạng (tuần 1–3)

### Tuần 1 — DevOps, Git và delivery flow

- Đọc: [DevOps foundations](notes/01-devops-foundations.md).
- Thực hành: trunk-based flow nhỏ, protected branch, pull request checklist.
- Artifact: value-stream map từ commit đến production, ghi thời gian chờ và handoff.
- Done khi: phân biệt CI, continuous delivery, continuous deployment và release.

### Tuần 2 — Linux và shell

- Process/thread, signal, file descriptor, permission, systemd, log và disk.
- Thực hành: tìm process chiếm CPU/RAM, port listener, deleted-open file và disk inode đầy.
- Artifact: runbook “service không trả lời” với lệnh, expected output và stop condition.

### Tuần 3 — Network end-to-end

- DNS, TCP, TLS, HTTP, proxy/LB/NAT, timeout và connection pool.
- Thực hành: dùng `dig/nslookup`, `curl -v`, `ss/netstat`, `traceroute` phù hợp hệ điều hành.
- Artifact: sơ đồ request từ client đến Spring Boot và database, ghi timeout từng hop.

## Giai đoạn 2 — CI/CD và container (tuần 4–6)

### Tuần 4 — Continuous Integration

- Pipeline: lint → unit → integration → security → package.
- Cache chỉ tăng tốc; artifact phải tái tạo được và không phụ thuộc cache.
- Artifact: GitHub Actions workflow có least-privilege `permissions`, concurrency và timeout.

### Tuần 5 — Artifact và deployment safety

- Version/digest, registry, immutable artifact, environment promotion.
- Rolling, recreate, blue/green, canary, feature flag; rollback khác roll-forward.
- Artifact: deployment decision table và automated rollback condition.

### Tuần 6 — Docker production

- Multi-stage build, non-root, read-only filesystem, signal/PID 1, health và resource limit.
- Artifact: Dockerfile cho một Spring Boot project; scan image và ghi lại CVE triage.
- Done khi: cùng một image digest được promote qua dev/staging/prod.

## Giai đoạn 3 — Platform, Kubernetes và IaC (tuần 7–10)

### Tuần 7 — Kubernetes workload

- Pod, Deployment, Service, ConfigMap/Secret, probes, requests/limits và rollout.
- Artifact: manifest hoặc Helm chart có startup/readiness/liveness đúng vai trò.

### Tuần 8 — Kubernetes production

- HPA, PodDisruptionBudget, topology spread, NetworkPolicy, RBAC, ingress và storage.
- Artifact: game day mất một node/AZ giả lập; chứng minh service vẫn đạt SLO.

### Tuần 9 — Terraform workflow

- `init`, `validate`, `plan`, saved plan, `apply`, state, backend và locking.
- Artifact: module nhỏ, CI speculative plan, approval và apply đúng saved plan.
- Done khi: giải thích được partial apply và quy trình phục hồi mà không sửa state bằng tay.

### Tuần 10 — GitOps và policy as code

- Pull-based reconciliation, drift, desired state, secret delivery và promotion.
- Artifact: policy chặn public storage/wildcard IAM/untagged resource; có test pass/fail.

## Giai đoạn 4 — Observability, SRE và security (tuần 11–13)

### Tuần 11 — Metrics, logs và traces

- RED/USE, correlation ID, OpenTelemetry, cardinality và retention.
- Artifact: dashboard từ user journey xuống dependency; mỗi panel có câu hỏi vận hành rõ.

### Tuần 12 — SLI/SLO và incident response

- Availability/latency SLI, error budget, burn-rate alert, on-call, severity và postmortem.
- Artifact: SLO + hai alert windows + runbook + postmortem từ một failure injection.

### Tuần 13 — DevSecOps và software supply chain

- Threat model pipeline, OIDC, dependency/image/IaC scan, SBOM, signing, provenance.
- Artifact: threat model pipeline và policy chỉ cho deploy artifact đã verify.

## Giai đoạn 5 — AWS DevOps (tuần 14–16)

### Tuần 14 — AWS foundation và identity

- Organizations/account boundary, IAM role, STS, SCP, CloudTrail, Config, KMS và Secrets Manager.
- Artifact: GitHub OIDC trust policy giới hạn repository/ref hoặc environment; không có static key.

### Tuần 15 — AWS delivery platform

- CodePipeline/CodeBuild/CodeDeploy hoặc GitHub Actions; ECR; ECS/EKS/Lambda deployment.
- Artifact: thiết kế pipeline cross-account dev → staging → prod với artifact provenance.

### Tuần 16 — AWS operations capstone

- CloudWatch, X-Ray/OpenTelemetry, EventBridge, Systems Manager, Auto Scaling, Backup và DR.
- Artifact: bảo vệ kiến trúc trước review board theo SLO, blast radius, security, rollback và cost.
- Ôn [câu DevOps nền tảng](notes/12-devops-basic-interview-questions.md) và [câu AWS DevOps](notes/13-aws-devops-interview-questions.md).

## Capstone đề xuất

Triển khai một Spring Boot order service theo hai target:

1. **Local:** Docker Compose hoặc Kubernetes local, metrics/logs/traces và load test.
2. **AWS design:** ECR + ECS Fargate hoặc EKS, RDS, ALB, CloudWatch, GitHub OIDC và Terraform.

Không cần tạo resource AWS thật để hoàn thành design. Bản bảo vệ phải có:

- architecture diagram và trust boundaries;
- pipeline diagram, artifact/digest flow và database migration order;
- Terraform state boundary và account/environment strategy;
- SLI/SLO, dashboard, alert và runbook;
- deployment strategy cùng rollback/roll-forward triggers;
- RPO/RTO, backup restore test và game-day plan;
- cost drivers, quotas và scaling limits;
- ít nhất ba failure scenarios có bằng chứng phục hồi.

