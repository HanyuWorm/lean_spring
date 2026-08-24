# 10 — Container, Kubernetes và software supply chain

## Supply-chain path

```text
developer identity -> source/review -> build runner -> dependency
 -> artifact/SBOM/provenance/signature -> registry -> admission -> runtime
```

Mỗi hop cần identity, integrity, least privilege, isolated secret và audit. SLSA 1.2 giúp mô tả mức assurance/provenance; SBOM là inventory, không tự chứng minh artifact sạch.

## Build

- Protected branch, reviewed change, signed/traceable commit theo policy.
- Ephemeral isolated runner; CI OIDC federation thay cloud key.
- Pin dependency/action/image bằng version/digest, verify source/signature.
- SCA, secret scan, SAST và license policy; patch SLA theo exploitability.
- Reproducible/hermetic build khi risk yêu cầu; generate provenance và SBOM.
- Sign artifact bằng keyless/short-lived identity hoặc protected signing service.

## Container

Minimal trusted base, non-root, read-only filesystem, drop capabilities, seccomp/AppArmor/SELinux, no secret in layer, pin digest, scan at build và registry/runtime. Image signature/admission policy chống unapproved artifact.

## Kubernetes

- API endpoint/admin access hạn chế; RBAC least privilege và separate namespaces/accounts.
- Workload identity thay mounted static cloud key.
- Pod Security Standards/admission policy; deny privileged/hostPath/hostNetwork mặc định.
- NetworkPolicy ingress/egress, secret encryption/KMS, audit logs.
- etcd/control-plane backup/DR; version/patch cadence.
- Resource request/limit/quota và priority chống noisy neighbor/DoS.
- Protect GitOps/controller/service-mesh/operator vì chúng có cluster-wide leverage.

## Runtime detection

Detect unexpected process, shell, file modification, privileged syscall, crypto-mining, unusual egress và service account use. Runtime agent không thay preventive hardening; alert phải map tới pod image digest, workload identity, deployment và commit.
