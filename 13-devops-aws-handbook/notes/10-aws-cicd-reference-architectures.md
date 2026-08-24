# 10 — AWS CI/CD reference architectures

## 1. Nguyên tắc chung

```text
Git provider
  -> CI identity (OIDC)
  -> build/test/scan
  -> immutable artifact in tooling account
  -> sign/attest
  -> assume environment deployment role
  -> progressive deploy
  -> CloudWatch/SLO gates
  -> automatic rollback or stop
```

- Một artifact digest đi qua mọi environment.
- Account production không trust mọi branch/repository.
- Deployment role không đồng thời là runtime role.
- Pipeline không có direct database admin trừ migration role bị giới hạn.
- Log archive/audit tách khỏi workload account.

## 2. GitHub Actions → AWS bằng OIDC

Trust policy concept:

```json
{
  "Effect": "Allow",
  "Principal": {
    "Federated": "arn:aws:iam::ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com"
  },
  "Action": "sts:AssumeRoleWithWebIdentity",
  "Condition": {
    "StringEquals": {
      "token.actions.githubusercontent.com:aud": "sts.amazonaws.com",
      "token.actions.githubusercontent.com:sub": "repo:ORG/REPO:environment:prod"
    }
  }
}
```

Đây là skeleton, không copy nguyên vào production. Phải kiểm tra immutable `sub` format cho repository hiện tại, account ID, environment protection, audience và IAM permissions. Production environment yêu cầu branch/tag rules và reviewer phù hợp.

## 3. ECS Fargate pipeline

```text
PR -> unit/integration/SAST/SCA/IaC scan
main -> build image -> ECR digest -> SBOM/sign
staging -> ECS task definition revision -> smoke/load
prod -> CodeDeploy blue/green -> canary/linear traffic
     -> CloudWatch alarms -> continue/rollback
```

Thiết kế:

- ALB hai target groups cho blue/green.
- Container health + target health và application SLI.
- Task role chỉ quyền business dependency; execution role dùng pull image/log.
- DB expand migration trước app; contract migration sau rollback window.
- ECS service autoscaling không vượt downstream capacity budget.

Failure cần luyện:

- image pull/permission sai;
- target health pass nhưng business synthetic fail;
- canary error tăng chỉ ở một AZ;
- rollback app nhưng schema đã contract;
- task scale-out làm RDS connection saturation.

## 4. EKS GitOps pipeline

```text
CI -> build/sign image -> ECR digest
   -> update digest in environment Git repo
GitOps controller in cluster -> reconcile Helm/Kustomize
progressive controller -> canary analysis
admission policy -> verify signature/security policy
```

Boundary:

- CI không cần cluster-admin credential.
- GitOps controller chỉ scope cluster/namespace cần quản.
- Environment repo branch protection và CODEOWNERS.
- Secret lấy bằng workload identity/external secret mechanism, không plain Git.
- EKS control plane, node, add-on và workload upgrades có pipeline khác nhau.

Failure cần luyện:

- bad manifest auto-sync toàn cluster;
- controller compromised;
- CNI IP exhaustion làm pod Pending;
- PDB/topology sai chặn node upgrade;
- image signature policy outage chặn emergency rollout.

## 5. Lambda pipeline

```text
build package/image -> publish version -> update alias via CodeDeploy
    -> 10% canary -> CloudWatch alarm/synthetic -> 100% hoặc rollback alias
```

- Không deploy `$LATEST` như production contract.
- Alias trỏ version và tham gia traffic shifting.
- Alarm gồm error/throttle/duration và business outcome.
- Provisioned concurrency/cold start là cost-performance trade-off.
- Rollback alias không undo message đã xử lý hoặc external side effect; handler phải idempotent.

## 6. Cross-account pipeline

Đề xuất account:

```text
organization
├── security / log archive
├── tooling / artifact
├── development
├── staging
└── production
```

Tooling pipeline assume role từng environment. Artifact bucket/ECR policy cho phép pull đúng principal; KMS key policy hỗ trợ cross-account theo scope. CloudTrail organization trail đưa log về log archive có retention/immutability phù hợp.

Không để pipeline role production có `iam:*`, `organizations:*` hoặc `sts:AssumeRole` không giới hạn.

## 7. Deployment gates

### Trước deploy

- artifact signature/provenance verified;
- change set/plan review;
- quota/capacity và maintenance/event freeze;
- backup/schema compatibility;
- on-call và rollback path ready.

### Trong deploy

- technical SLI + business synthetic;
- per-AZ/cohort comparison;
- minimum sample và bake time;
- alarm data đủ, không coi missing data là success mặc định.

### Sau deploy

- backlog, saturation, cost/unit, downstream error;
- audit artifact/config/schema versions;
- cleanup blue environment/old flags theo rollback window;
- record deployment outcome cho delivery metrics.

## 8. Chọn native AWS hay external tool

| Chọn | Khi phù hợp |
|---|---|
| CodePipeline/CodeBuild/CodeDeploy | AWS-centric, muốn managed integration và IAM-native |
| GitHub Actions + AWS OIDC | source/workflow ở GitHub, muốn developer workflow thống nhất |
| Jenkins/self-hosted | plugin/custom network/legacy đặc biệt và chấp nhận vận hành controller/runner |
| GitOps | Kubernetes desired-state, audit/reconcile/pull model |

Nhiều tổ chức dùng kết hợp: GitHub Actions làm CI, ECR lưu artifact, Argo CD/Flux làm CD cho EKS, CloudWatch làm rollout signal. Boundary và ownership quan trọng hơn “một tool cho tất cả”.

Nguồn: [GitHub OIDC with AWS](https://docs.github.com/en/actions/how-tos/secure-your-work/security-harden-deployments/oidc-in-aws), [CodeDeploy configurations](https://docs.aws.amazon.com/codedeploy/latest/userguide/deployment-configurations.html), [AWS automated deployment](https://docs.aws.amazon.com/wellarchitected/latest/reliability-pillar/rel_tracking_change_management_automated_changemgmt.html).

