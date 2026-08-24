# Terraform local-state lab

Lab này dùng resource built-in `terraform_data`, không gọi AWS và không tạo chi phí cloud.

```bash
terraform init
terraform fmt -check
terraform validate
terraform plan -out=tfplan
terraform apply tfplan
terraform output
```

## Mục tiêu

- Quan sát quan hệ giữa configuration, plan, apply và `terraform.tfstate`.
- Thay `environment`, review plan rồi apply saved plan.
- Mở state để hiểu vì sao nó phải được coi là sensitive; không commit state.
- Tạo lỗi validation bằng `environment = "production"` và giải thích vì sao guardrail gần source vẫn chưa thay policy ở pipeline/cloud account.

## Nâng cao

Thiết kế migration từ local state sang remote backend. Bản design phải nói rõ:

- encryption và KMS/key ownership;
- versioning/backup và recovery;
- locking/concurrent writers;
- IAM least privilege và audit;
- state boundary theo environment/blast radius;
- secret exposure trong state/plan;
- quy trình `force-unlock` và stop condition.

Không chạy `terraform force-unlock` như một thao tác thử nghiệm trên shared state.

