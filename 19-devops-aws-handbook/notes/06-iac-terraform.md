# 06 — Infrastructure as Code và Terraform

## 1. IaC là gì?

IaC mô tả desired infrastructure bằng code có version, review, test và pipeline. Mục tiêu không chỉ “tạo resource nhanh” mà là thay đổi lặp lại được, audit được và giảm configuration drift.

Không phải mọi automation là IaC. Script API không lưu state/desired model vẫn hữu ích, nhưng lifecycle và drift semantics khác Terraform/CloudFormation.

## 2. Terraform workflow

```text
fmt -> validate -> lint/security/policy -> speculative plan trên PR
    -> review/approve -> final saved plan -> apply chính plan đó
    -> post-deploy verification -> drift detection
```

- `init`: backend/module/provider initialization.
- `validate`: syntax/internal consistency, không chứng minh architecture đúng.
- `plan`: refresh state, so sánh config/state/remote và đề xuất actions.
- `apply`: thực hiện actions; có thể partial success và state được cập nhật phần đã làm.
- `destroy`: destructive; cần guardrail và exact target/context.

Trong automation, saved plan đảm bảo apply đúng tập actions đã review tốt hơn chạy plan mới mù quáng. Nhưng plan có thể chứa sensitive values; bảo vệ artifact và TTL.

## 3. State

State ánh xạ resource address trong config với object thật và giữ metadata/cached attributes.

Quy tắc:

- remote backend có encryption, versioning, access control, audit và locking;
- tách state theo blast radius/lifecycle/team/environment;
- không commit state vào Git;
- coi state là sensitive vì có thể chứa secret dù variable được đánh dấu sensitive;
- backup/restore state và test quy trình;
- không sửa JSON bằng tay; dùng `terraform state`, `import`, `moved` block phù hợp.

Lock ngăn concurrent writers nếu backend hỗ trợ. `force-unlock` chỉ dùng khi chắc chắn không có writer thật và lock ID đúng; bỏ lock sai có thể gây hai apply đồng thời.

## 4. Partial apply và rollback

Terraform không transactionally rollback mọi cloud API. Nếu apply lỗi giữa chừng:

1. Dừng concurrent run và giữ log/plan.
2. Đọc state/remote thực tế; xác định resource nào đã tạo/đổi.
3. Sửa nguyên nhân hoặc config theo desired state.
4. Chạy plan mới và review.
5. Apply để converge; chỉ state surgery khi có runbook và backup.

Không giả định `terraform apply` thất bại nghĩa “không thay đổi gì”.

## 5. Module design

- Module có boundary theo capability/lifecycle, không phải “module cho mọi AWS resource”.
- Input ít, type/validation rõ; output chỉ contract cần thiết.
- Pin module/provider versions và commit lock file theo workflow phù hợp.
- Không nhồi environment condition vào một mega-module.
- Tạo example/test và migration notes khi breaking change.
- Tag/owner/cost/security defaults bằng module và policy, nhưng có exception path.

## 6. Environment và account

Terraform workspace chỉ đổi state instance; không tự tạo security isolation. Production nên có account/project, identity, state backend và approval boundary phù hợp.

Không truyền mọi thứ qua `terraform_remote_state`; consumer có thể cần quyền đọc toàn state. Ưu tiên publish contract tối thiểu qua parameter/config registry khi phù hợp.

## 7. Drift

Drift có thể do emergency manual change, external controller, provider default hoặc unmanaged resource.

- Detect bằng scheduled plan/config service.
- Phân biệt approved break-glass với unauthorized change.
- Reconcile: import/update config, hoặc apply để revert manual change.
- Post-incident phải đóng loop: code hóa change, rotate break-glass credential, update runbook.

## 8. Policy as code

Policy chặn hoặc cảnh báo:

- public storage/network exposure;
- wildcard admin và missing encryption;
- resource không tag owner/cost/data classification;
- production single-AZ hoặc backup tắt;
- destructive replacement ngoài maintenance window.

Policy cần test case, version, owner, severity, exception có expiry. Quá nhiều false positive khiến team bypass toàn bộ guardrail.

## 9. Terraform hay CloudFormation/CDK?

| Tiêu chí | Terraform | CloudFormation/CDK |
|---|---|---|
| Provider | multi-provider | AWS-native |
| State | backend do team chọn/quản lý | AWS quản lý stack state |
| Language | HCL | YAML/JSON hoặc CDK languages |
| AWS feature timing | phụ thuộc provider release | thường tích hợp AWS-native sớm |
| Failure semantics | partial apply/converge | stack events/rollback tùy cấu hình/resource |

Chọn theo operating model, ecosystem, skills, control plane và exit strategy; không chỉ theo syntax.

Nguồn: [Terraform workflow](https://developer.hashicorp.com/terraform/cli/run), [Terraform state](https://developer.hashicorp.com/terraform/language/state), [state locking](https://developer.hashicorp.com/terraform/language/state/locking).

