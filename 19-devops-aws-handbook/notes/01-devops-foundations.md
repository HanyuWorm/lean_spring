# 01 — DevOps foundations

## 1. DevOps thực sự tối ưu gì?

DevOps tối ưu dòng giá trị từ ý tưởng đến kết quả production và vòng phản hồi quay về đội phát triển. Ba mục tiêu phải đồng thời tồn tại:

- **Flow:** thay đổi nhỏ đi qua hệ thống nhanh, ít handoff và queue.
- **Feedback:** lỗi build, security, runtime và user impact quay về đúng owner sớm.
- **Learning:** incident và metric làm thay đổi code, architecture, test và quy trình.

Nếu chỉ tăng deployment frequency nhưng change failure tăng và đội on-call kiệt sức, đó không phải cải tiến bền vững.

## 2. CI, delivery, deployment và release

| Khái niệm | Ý nghĩa |
|---|---|
| Continuous Integration | Merge thay đổi nhỏ thường xuyên; mỗi thay đổi được build/test tự động |
| Continuous Delivery | Main luôn ở trạng thái có thể deploy; production thường còn gate có chủ đích |
| Continuous Deployment | Thay đổi đạt gate tự đi production không cần phê duyệt thủ công |
| Release | Cho user sử dụng capability; có thể tách khỏi deployment bằng feature flag |

Tách deploy khỏi release giúp đưa code tối vào production, xác minh kỹ thuật rồi bật dần theo tenant/percentage. Feature flag phải có owner, expiry và cleanup; nếu không nó trở thành nhánh logic vĩnh viễn.

## 3. Dòng giá trị và bottleneck

Đo ít nhất:

- thời gian coding, review, chờ runner, test, approval và deployment;
- tỷ lệ pipeline fail do code so với flaky infrastructure/test;
- batch size, deployment frequency, change lead time;
- change failure, failed-deployment recovery và rework;
- thời gian phát hiện, acknowledge, mitigate và resolve incident.

DORA Core thường dùng bốn delivery metrics: change lead time, deployment frequency, change fail percentage và failed deployment recovery time. Báo cáo DORA 2024 còn phân tích deployment rework rate; không biến metric thành KPI thưởng-phạt vì đội sẽ tối ưu con số thay vì outcome.

## 4. Ownership model

“You build it, you run it” không có nghĩa developer tự làm mọi platform task. Mô hình tốt:

- product team sở hữu service behavior, SLO, dashboard và runbook;
- platform team cung cấp paved road/self-service với guardrails;
- security cung cấp policy, threat intelligence và exception process;
- SRE tập trung reliability engineering, SLO/error budget và giảm toil;
- incident commander điều phối, không nhất thiết là người sửa kỹ thuật.

RACI không thay thế ownership runtime. Mỗi alert phải có đúng một owning team, severity, action và escalation.

## 5. Maturity theo capability, không theo tool

### Level 1 — Repeatable

- Git là nguồn sự thật; build/test có script.
- Runbook cho thao tác thường gặp.
- Backup có lịch nhưng phải bắt đầu restore test.

### Level 2 — Automated

- CI tạo artifact immutable.
- IaC và config được review.
- Deploy tự động tới non-production.

### Level 3 — Safe delivery

- Progressive delivery, health gate, automatic rollback.
- SLO và alert theo user impact.
- Security checks có policy/exception rõ.

### Level 4 — Self-service platform

- Golden path, template, environment on demand.
- Policy as code và workload identity.
- Team đo developer experience và cognitive load.

### Level 5 — Adaptive

- Chaos/game day, automated remediation có guardrails.
- Capacity/cost/reliability feedback vào design.
- Postmortem action được ưu tiên như product work.

## 6. Anti-pattern

- DevOps team trở thành ticket queue giữa dev và ops.
- Mọi thay đổi production yêu cầu một người chạy lệnh thủ công.
- Pipeline build lại artifact cho từng environment.
- Alert theo CPU đơn lẻ nhưng không biết user journey nào hỏng.
- Dùng deployment frequency làm mục tiêu cá nhân.
- Platform tạo quá nhiều abstraction khiến developer không debug được tầng dưới.

## 7. Checklist đánh giá một tổ chức

- Bao lâu một commit tốt tới production?
- Phần lớn thời gian là làm hay chờ?
- Có thể truy ngược production artifact về commit, dependency và pipeline run không?
- Ai có thể deploy và bằng identity ngắn hạn nào?
- Rollback đã test hay chỉ viết trong tài liệu?
- Một incident lặp lại có tạo regression test/guardrail không?
- Team dành bao nhiêu thời gian cho toil?
- SLO có tác động đến ưu tiên feature/reliability không?

Nguồn: [AWS Operational Excellence](https://docs.aws.amazon.com/wellarchitected/latest/operational-excellence-pillar/operational-excellence.html), [DORA research](https://dora.dev/research/).

