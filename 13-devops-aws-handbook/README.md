# DevOps & AWS DevOps Handbook

Track tiếng Việt dành cho developer muốn đi từ nền tảng vận hành đến DevOps/SRE và triển khai workload production trên AWS. Nội dung không biến DevOps thành danh sách tool; mỗi chương luôn nối **flow delivery**, **failure mode**, **security boundary**, **SLO** và **bằng chứng vận hành**.

> Snapshot **25/08/2026**. Phần AWS bám AWS Well-Architected, EKS Best Practices và sáu domain của AWS Certified DevOps Engineer – Professional `DOP-C02`. GitHub Actions dùng OIDC/short-lived credentials; không hướng dẫn lưu AWS access key dài hạn.

## Kết quả cần đạt

- Đọc được Linux process, filesystem, permission, DNS/TCP/TLS và chẩn đoán request end-to-end.
- Thiết kế CI/CD tạo artifact bất biến, có quality gate, provenance, rollout và rollback tự động.
- Build container nhỏ/an toàn; vận hành Kubernetes bằng probes, resource requests/limits, autoscaling và disruption controls.
- Quản lý infrastructure bằng Terraform/IaC với remote state, locking, review plan, drift và policy guardrails.
- Xây observability theo metrics/logs/traces, SLI/SLO, error budget và alert có hành động.
- Thực hiện incident response, runbook, postmortem không đổ lỗi và đo hiệu quả delivery.
- Thiết kế AWS multi-account, IAM federation, pipeline, ECS/EKS/Lambda, CloudWatch và automated remediation.
- Trả lời câu hỏi phỏng vấn bằng trade-off và failure scenario, không chỉ định nghĩa tool.

## Bản đồ nội dung

| Chương | Nội dung |
|---|---|
| [01](notes/01-devops-foundations.md) | DevOps foundations, flow, ownership và maturity |
| [02](notes/02-linux-networking-git.md) | Linux, shell, process, filesystem, DNS/TCP/TLS và Git |
| [03](notes/03-ci-cd-artifacts-deployment.md) | CI/CD, artifact, test gates, deployment và rollback |
| [04](notes/04-containers-docker.md) | Container, Docker image, runtime, networking và security |
| [05](notes/05-kubernetes-platform.md) | Kubernetes production, scheduling, probes, scaling và GitOps |
| [06](notes/06-iac-terraform.md) | IaC, Terraform state/plan/apply, module, drift và policy |
| [07](notes/07-observability-sre.md) | Observability, SLI/SLO, error budget, alert và capacity |
| [08](notes/08-devsecops-supply-chain.md) | DevSecOps, secrets, SBOM, signing, provenance và policy |
| [09](notes/09-aws-devops-services.md) | Bản đồ dịch vụ AWS dành cho DevOps |
| [10](notes/10-aws-cicd-reference-architectures.md) | Pipeline tham chiếu cho ECS, EKS và Lambda |
| [11](notes/11-incident-change-cost.md) | Incident, change management, DR, FinOps và platform engineering |
| [12](notes/12-devops-basic-interview-questions.md) | 35 câu DevOps nền tảng có đáp án |
| [13](notes/13-aws-devops-interview-questions.md) | 35 câu AWS DevOps có đáp án |

Học theo [lộ trình 16 tuần](ROADMAP.md), làm [lab](labs/README.md) và dùng các [template vận hành](templates/README.md). Các lab AWS mặc định chỉ `validate`, `plan` hoặc mô phỏng; không tự tạo resource có phí.

## Mô hình tư duy xuyên suốt

```text
Commit
  -> kiểm tra nhanh
  -> tạo artifact bất biến + provenance
  -> kiểm tra security/policy
  -> deploy dần theo fault boundary
  -> đo SLI/business KPI
  -> tự dừng hoặc rollback khi vi phạm
  -> học từ telemetry và incident
```

Mỗi thay đổi production phải trả lời:

1. **Ai sở hữu?** Service, pipeline, infrastructure, alert và runbook có owner nào?
2. **Thay đổi gì?** Commit, artifact digest, IaC plan, database migration và config version nào?
3. **Blast radius?** Account, region, AZ, cluster, namespace, tenant hay percentage traffic nào bị tác động?
4. **Bằng chứng an toàn?** Test, policy, SLO gate, canary metric và rollback signal là gì?
5. **Phục hồi thế nào?** Rollback, roll-forward, restore hoặc failover đã được diễn tập chưa?

## Những ngộ nhận cần bỏ

- DevOps không phải tên một người “làm hết deployment”; đó là cách tổ chức flow và feedback giữa build và run.
- Có pipeline không đồng nghĩa có continuous delivery; pipeline chậm, manual và không rollback vẫn là batch process được bọc YAML.
- Kubernetes không tự tạo reliability. Sai probes, thiếu requests/limits hoặc một cluster cho mọi blast radius có thể làm hệ thống kém ổn định hơn.
- Terraform state có thể chứa secret và là dữ liệu điều phối quan trọng; không commit state vào Git.
- Monitoring nhiều metric không đồng nghĩa observability; alert phải gắn user impact, owner và hành động.
- Multi-AZ không thay thế backup/restore và không mặc nhiên đáp ứng multi-region DR.
- Tự động hóa nguy hiểm nếu thiếu rate limit, approval, dry-run, audit và kill switch.

