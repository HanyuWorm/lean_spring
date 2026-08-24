# 03 — CI/CD, artifact và deployment safety

## 1. Pipeline chuẩn

```text
PR: lint -> unit -> SAST/dependency/IaC scan -> build check
main: build once -> integration/contract -> SBOM/sign -> publish artifact
deploy: promote digest -> environment checks -> progressive rollout
        -> synthetic/SLI gate -> complete hoặc automatic rollback
```

Tối ưu feedback: test nhanh chạy trước; test đắt chạy song song khi có thể. Nhưng không bỏ quality gate chỉ để pipeline “xanh nhanh”.

## 2. Build once, promote many

Build lại theo môi trường tạo ra artifact khác nhau và mất bằng chứng staging đã test đúng thứ production chạy. Cách đúng:

- build một lần từ commit sạch;
- gắn version và content digest;
- publish vào registry có retention/immutability;
- inject config/secrets khi runtime;
- promote chính digest qua environment;
- lưu provenance: source SHA, builder, dependency lock, test và scan result.

Artifact không phải chỉ JAR/image. Database migration, Helm chart, Terraform module và policy bundle cũng phải version.

## 3. Quality gates

| Gate | Mục đích | Không nên làm |
|---|---|---|
| Unit/static | feedback nhanh | phụ thuộc environment ngoài |
| Integration | boundary thật | dùng data dùng chung không reset |
| Contract | compatibility producer/consumer | thay thế mọi end-to-end test |
| Security | phát hiện known risk/policy | chặn mọi CVE không xét reachability/severity |
| Performance | regression/capacity | chỉ đo average latency |
| Resilience | failover/retry/timeout | chạy phá production không guardrail |

Exception security phải có owner, lý do, compensating control và expiry.

## 4. Deployment strategies

### Rolling

- Thay dần instance/pod; cost thấp.
- Có lúc hai version cùng chạy, nên API/schema phải tương thích.
- Rollback có thể chậm nếu rollout lớn.

### Blue/green

- Hai environment; switch traffic nhanh và rollback đơn giản.
- Cost/capacity cao hơn; state/schema/external side effects không rollback theo traffic.

### Canary

- Đưa tỷ lệ nhỏ traffic hoặc cohort vào version mới.
- Cần đủ sample, metric đúng, bake time và automated analysis.
- Canary theo 1% traffic nhưng toàn bộ traffic đó cùng một tenant/AZ có thể đánh giá sai.

### Feature flag

- Tách code deployment và feature release.
- Không sửa được binary/config bug nằm ngoài path được flag.
- Có debt cleanup và test cả on/off trong thời gian tồn tại.

## 5. Database migration

Dùng expand/contract:

1. Expand schema tương thích version cũ và mới.
2. Deploy code có thể đọc/ghi trong giai đoạn chuyển tiếp.
3. Backfill có throttle, checkpoint và observability.
4. Chuyển read path, xác minh consistency.
5. Contract chỉ khi không còn consumer cũ và rollback window kết thúc.

Không gộp drop column với code ngừng dùng column trong cùng rollout nếu nhiều instance/version đồng tồn tại.

## 6. Rollback và roll-forward

- Rollback hợp với artifact/stateless/config có backward compatibility.
- Roll-forward hợp khi migration/data side effect không thể đảo an toàn.
- Cả hai cần quyết định trước: trigger, owner, deadline và validation.
- “Redeploy phiên bản cũ” không đủ nếu queue message/schema/feature state đã đổi.

## 7. Pipeline security

- Pin third-party action theo immutable commit SHA khi risk yêu cầu.
- `permissions` mặc định read-only, cấp theo job.
- OIDC short-lived token thay static cloud credentials.
- PR không tin cậy không được chạy code với production secret.
- Self-hosted runner phải cô lập, ephemeral nếu có thể, vá định kỳ và không dùng chung trust zone.
- Log/artifact không chứa token, Terraform state hay customer data.

## 8. Pipeline metrics

- queue time, duration và success rate theo stage;
- flaky test rate và rerun count;
- artifact promotion lead time;
- deploy frequency, change failure, recovery time;
- rollback rate và rollback success time;
- manual intervention/toil trên mỗi release.

Nguồn: [AWS CI/CD guidance](https://docs.aws.amazon.com/prescriptive-guidance/latest/aws-caf-platform-perspective/ci-cd.html), [GitHub Actions security](https://docs.github.com/en/actions/how-tos/secure-your-work).

