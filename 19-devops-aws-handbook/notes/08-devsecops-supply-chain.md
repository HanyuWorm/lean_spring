# 08 — DevSecOps và software supply chain

## 1. Shift left và shield right

- Shift left: threat model, secure defaults, dependency/IaC scan và test sớm.
- Shield right: runtime identity, detection, WAF/network policy, isolation và incident response.

Không thể scan hết security. Design flaw, abuse case, leaked runtime credential và authorization bug cần nhiều lớp control.

## 2. Threat model pipeline

Assets:

- source, secret, signing key, artifact, registry, runner, cloud role, IaC state;
- branch protection, approval và audit history.

Threats:

- dependency/action bị compromise;
- untrusted PR lấy secret;
- self-hosted runner persistence;
- artifact bị thay sau test;
- cloud role trust quá rộng;
- log/cache/artifact làm lộ token;
- maintainer account bị chiếm.

Controls:

- MFA/passkey, protected branch, CODEOWNERS;
- least-privilege token và OIDC federation;
- ephemeral/isolated runners;
- dependency pinning, SBOM, signature/provenance;
- environment approval và immutable artifact promotion;
- audit/detection và break-glass process.

## 3. Secret management

- Secret không nằm trong Git, image, Terraform plan artifact công khai hay command line dễ log.
- Dùng workload identity/role để tránh secret khi có thể.
- Secret store có encryption, access policy, audit và rotation.
- Rotation gồm cả producer và consumer, overlap window và revoke old credential.
- Mask log không đảm bảo dữ liệu chưa từng rời boundary.

## 4. GitHub Actions → AWS bằng OIDC

Workflow cần `id-token: write` để xin JWT; action đổi JWT lấy AWS STS credentials ngắn hạn. IAM trust policy phải giới hạn `aud` và `sub` theo repository/ref hoặc protected environment.

```yaml
permissions:
  contents: read
  id-token: write
```

Theo tài liệu GitHub hiện hành, repository tạo sau **15/07/2026** hoặc đã opt-in immutable subject claims có thể dùng owner/repository IDs trong `sub`. Không copy trust policy cũ mà không kiểm tra claim thực tế. AWS không hỗ trợ custom claims cho GitHub OIDC; dùng condition keys được hỗ trợ và environment protection.

`id-token: write` chỉ cho workflow yêu cầu OIDC token; quyền AWS thật do trust policy và IAM role policy quyết định.

## 5. Scanning matrix

| Loại | Tìm gì | Giới hạn |
|---|---|---|
| SAST | code pattern/data flow | false positive, thiếu runtime context |
| SCA | dependency/CVE/license | CVE không đồng nghĩa reachable/exploitable |
| Secret scan | token/key pattern | không thay rotation/detection |
| IaC scan | misconfiguration | provider/runtime default có thể khác |
| Image scan | OS/app packages | image sạch lúc build có thể lỗi thời sau đó |
| DAST | behavior của app chạy | coverage phụ thuộc crawl/auth/test data |

Gate theo severity, exploitability, reachability, asset criticality và exception expiry.

## 6. SBOM, signing và provenance

- SBOM: inventory component/version; không tự chứng minh artifact an toàn.
- Signature: ai/khoá nào ký bytes/digest; phải bảo vệ identity/key và verify policy.
- Provenance/attestation: artifact được build ở đâu, từ source nào, bằng process nào.
- Registry immutability và digest promotion nối test result với bytes production.

Verification phải diễn ra tại deploy/admission, không chỉ “có file SBOM trong artifact”.

## 7. Policy và exception

- Policy as code được version/test/review.
- Deny với control chắc chắn, warn/report khi maturity chưa đủ.
- Exception có owner, scope, lý do, compensating control, expiry và review.
- Break-glass identity không dùng hằng ngày, được alert/audit và diễn tập revoke.

## 8. Supply-chain incident

1. Đóng/restrict pipeline và credential bị nghi ngờ.
2. Xác định artifact/digest, pipeline runs và environments bị ảnh hưởng.
3. Rotate/revoke token, signing identity và cloud sessions.
4. Rebuild từ trusted source/builder; verify provenance.
5. Redeploy/rollback theo blast radius.
6. Tìm persistence trên runner, registry, branch và cloud account.
7. Cập nhật guardrail và consumer verification.

Nguồn: [GitHub Actions security](https://docs.github.com/en/actions/how-tos/secure-your-work), [GitHub OIDC with AWS](https://docs.github.com/en/actions/how-tos/secure-your-work/security-harden-deployments/oidc-in-aws).

