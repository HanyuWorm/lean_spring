# 13 — 35 câu hỏi AWS DevOps có đáp án

## 1. Vì sao nên dùng multi-account thay vì một account cho mọi environment?

**Trả lời:** Account là isolation, quota, billing và blast-radius boundary mạnh hơn tag/VPC. Tách prod/non-prod/security/log archive/shared tooling giảm credential và deployment impact. AWS Organizations/SCP tạo guardrail chung; vẫn cần network, identity và data-sharing design.

## 2. SCP khác IAM policy thế nào?

**Trả lời:** SCP đặt maximum available permissions cho account/OU, không cấp quyền. Principal còn cần identity/resource policy allow và không bị explicit deny. SCP không áp cho management account theo cùng cách member account và cần break-glass/testing trước rollout rộng.

## 3. IAM role policy và trust policy khác gì?

**Trả lời:** Role permissions policy nói session của role được làm gì; trust policy nói ai/cơ chế nào được assume role và conditions. Role có admin permissions nhưng trust chặt vẫn blast radius khác trust `Principal:*`; phải review cả hai cùng SCP/boundary/session/resource policy.

## 4. Vì sao GitHub Actions nên dùng OIDC thay AWS access key?

**Trả lời:** OIDC đổi JWT của workflow lấy STS credentials ngắn hạn, không lưu key dài hạn trong GitHub secret. IAM trust giới hạn `aud`, repository/ref/environment `sub`; role policy least privilege. Token ngắn hạn không bù cho trust condition quá rộng.

## 5. Điều gì thay đổi với GitHub OIDC `sub` trong năm 2026?

**Trả lời:** Theo GitHub Docs, repository tạo sau 15/07/2026 hoặc opt-in immutable subject claims có thể đưa immutable owner/repository IDs vào `sub`. Trust policy phải khớp format thực tế; không copy policy chỉ dùng tên repository mà không kiểm tra. Protected environment tiếp tục là boundary hữu ích cho production.

## 6. CodePipeline, CodeBuild và CodeDeploy khác nhau thế nào?

**Trả lời:** CodePipeline orchestration stages/actions/artifacts/gates. CodeBuild chạy build/test trong environment managed. CodeDeploy triển khai tới EC2/on-prem, ECS hoặc Lambda với deployment configurations phù hợp. Có thể thay source/build action bằng GitHub/Jenkins nhưng vẫn giữ boundary rõ.

## 7. Thiết kế artifact store cross-account cần chú ý gì?

**Trả lời:** Bucket/ECR resource policy, deployment role, KMS key policy, region và provenance phải đồng bộ. Cho prod pull đúng immutable digest, không cấp tooling role quyền runtime rộng. Log artifact access và quản retention/version/replication theo RTO.

## 8. CodeDeploy “minimum healthy hosts” bảo vệ gì trên EC2?

**Trả lời:** Nó giới hạn số host có thể unavailable trong deployment. Cần tính theo AZ và actual spare capacity; cấu hình phần trăm có rounding/edge case ở fleet nhỏ. Health technical pass chưa chứng minh business journey, nên thêm CloudWatch/synthetic verification.

## 9. ECS blue/green hoạt động khái quát thế nào?

**Trả lời:** CodeDeploy tạo replacement task set/target group, kiểm tra health rồi shift traffic canary/linear/all-at-once. Alarm có thể rollback traffic. Database/queue side effects không rollback theo target group; app/schema phải compatible và handler idempotent.

## 10. ECS task role khác task execution role thế nào?

**Trả lời:** Task role là quyền application code gọi AWS API. Execution role cho ECS agent pull ECR image, gửi log/lấy secret theo cấu hình. Gộp/quá rộng làm app có quyền infrastructure không cần thiết.

## 11. ECS autoscaling có thể làm RDS sập ra sao?

**Trả lời:** Mỗi task có connection pool; tăng task nhân tổng max connections và query concurrency. Giới hạn pool/task, max tasks theo DB budget, dùng proxy khi phù hợp, admission control và scale signal gắn backlog/latency. Không tăng RDS connection chỉ để che query/transaction dài.

## 12. AWS và customer chịu trách nhiệm gì với EKS?

**Trả lời:** AWS quản managed control plane/etcd availability. Customer vẫn chịu workload, IAM/RBAC, network, pod security, data, add-ons, node/data-plane choice và upgrade nhiều thành phần. Managed node group có patched AMI nhưng customer phải triển khai update.

## 13. EKS Pod Identity/IRSA tốt hơn node IAM role thế nào?

**Trả lời:** Nó cấp AWS permissions theo Kubernetes service account/workload thay vì mọi pod trên node thừa hưởng node role rộng. Vẫn cần trust/association và IAM policy least privilege, namespace/service-account governance và ngăn pod dùng credential source khác.

## 14. Chuẩn bị EKS upgrade production ra sao?

**Trả lời:** Kiểm tra version skew/deprecated APIs/add-on/CNI/CSI/ingress compatibility, test non-prod, capacity/PDB và rollback/migration path. Upgrade control plane, add-ons và nodes theo support matrix; roll node mới và drain có kiểm soát. Backup data/config và game day trước deadline support.

## 15. Vì sao Lambda production nên dùng version và alias?

**Trả lời:** Published version immutable; alias là stable pointer và hỗ trợ traffic shifting. CodeDeploy canary/linear dựa alias và CloudWatch alarm. `$LATEST` thay đổi được nên không phải production release identity tốt.

## 16. CloudWatch, CloudTrail và AWS Config khác nhau thế nào?

**Trả lời:** CloudWatch quan sát metrics/logs/alarms/application operations; CloudTrail audit AWS API activity; Config ghi configuration history/evaluate rules. Một API call có trong CloudTrail không chứng minh resource đang compliant, và Config không thay workload telemetry.

## 17. CloudWatch alarm làm deployment rollback có rủi ro gì?

**Trả lời:** Metric delay, insufficient data, threshold/sample sai hoặc alarm không theo cohort có thể miss lỗi/rollback giả. Định nghĩa missing-data semantics, bake time, composite/business signal và test alarm. Rollback automation cần stop/override/audit.

## 18. X-Ray và OpenTelemetry nên dùng thế nào?

**Trả lời:** OpenTelemetry chuẩn hóa instrumentation/export; CloudWatch/X-Ray có thể là backend/destination tùy setup. Propagate context qua service/message, control sampling/cardinality và dùng AWS/application attributes nhất quán. Trace không thay audit log hay aggregate metrics.

## 19. EventBridge automated remediation cần guardrail nào?

**Trả lời:** Event pattern exact, idempotency, bounded target, execution role least privilege, retry/DLQ, concurrency limit và audit. High-blast-radius action cần approval/dry-run/canary. Tránh rule loop nơi remediation tạo event kích hoạt chính nó.

## 20. Systems Manager giúp giảm SSH/bastion thế nào?

**Trả lời:** Session Manager cung cấp audited access qua managed instance/role và control plane thay inbound SSH, còn Automation/Run Command chạy procedure có kiểm soát. Vẫn phải bảo vệ IAM, document permissions, logging, network endpoints và break-glass.

## 21. CloudFormation change set và drift detection giải quyết gì?

**Trả lời:** Change set preview stack actions trước execute; drift detection so sánh resource supported với template expected. Preview không bảo đảm API/quota/runtime success, drift coverage không tuyệt đối. Destructive replacement và data resource vẫn cần review/backup.

## 22. Terraform state backend trên AWS nên có gì?

**Trả lời:** S3 backend với encryption, versioning, least-privilege bucket/KMS access và locking mechanism được provider/backend version hiện hành hỗ trợ; audit CloudTrail và recovery procedure. Tách state theo environment/blast radius. Không đưa state/plan vào public CI artifact.

## 23. Vì sao có IAM allow nhưng KMS vẫn AccessDenied?

**Trả lời:** KMS authorization còn phụ thuộc key policy, grants, region/key, encryption context và explicit denies/SCP. Cross-account cần key policy cho external account/principal cùng IAM allow. Debug CloudTrail request và policy evaluation, không thêm `kms:*` mù quáng.

## 24. Secrets Manager khác Parameter Store thế nào?

**Trả lời:** Secrets Manager tập trung secret lifecycle/rotation/integration; Parameter Store phù hợp config và secure strings với capability/pricing khác. Chọn theo rotation, size, throughput, policy, cross-account và operational needs. Tốt hơn nữa là workload role không cần static secret khi service hỗ trợ.

## 25. ECR image tag immutability và scanning có đủ không?

**Trả lời:** Không. Immutability chống overwrite tag; scan phát hiện known vulnerabilities theo coverage/time. Deploy bằng digest, sinh SBOM/provenance, sign/verify, lifecycle/rebuild base image và runtime/admission controls. CVE triage theo exploitability/reachability.

## 26. Golden AMI pipeline cần những bước nào?

**Trả lời:** Pin base, patch/harden, install tối thiểu, test chức năng/security, scan, publish immutable AMI, share có kiểm soát, canary instance refresh và deprecate old AMI. Lưu source recipe/provenance; không SSH patch production làm drift.

## 27. Multi-AZ khác multi-region thế nào?

**Trả lời:** Multi-AZ chịu failure trong một region với latency/consistency thường dễ hơn. Multi-region xử lý regional disaster hoặc global latency nhưng thêm replication, routing, data consistency, failover/failback và cost. Multi-AZ không tự đáp ứng regional RTO.

## 28. RPO và RTO ảnh hưởng kiến trúc AWS ra sao?

**Trả lời:** RPO quyết định replication/backup frequency và data-loss tolerance; RTO quyết định mức warm capacity/automation/failover. Pilot light, warm standby, active/passive và active/active có cost/complexity khác. Phải đo bằng restore/failover exercise.

## 29. Replication có thay backup không?

**Trả lời:** Không. Replication có thể sao chép delete/corruption/ransomware nhanh. Backup cần version/PITR/immutability, separate access/account/region theo threat model và restore test. Backup success metric không bằng recovery success.

## 30. Auto Scaling không scale dù CPU cao có thể do đâu?

**Trả lời:** Alarm warmup/cooldown, metric dimension, min=max, IAM/service-linked role, suspended process, quota/capacity/instance type/AZ hoặc launch failure. Xem scaling activities/CloudWatch/events; không chỉ chỉnh threshold. Với target tracking còn xem metric validity.

## 31. Pipeline cross-account nên assume role theo mô hình nào?

**Trả lời:** Tooling identity assume deployment role riêng trong từng account; trust giới hạn tooling role/OIDC condition và optionally external/session tags. Deployment role chỉ có resource/actions của stack đó. Artifact/KMS access tách khỏi runtime role và mọi assume được CloudTrail audit.

## 32. Break-glass access nên thiết kế thế nào?

**Trả lời:** Identity/role riêng không dùng hằng ngày, strong MFA, credential/access path được bảo vệ, time-bound approval nếu có thể, alert ngay khi dùng, full audit và retrospective. Phải test định kỳ; break-glass không được phụ thuộc chính hệ thống đang outage.

## 33. Deploy app thành công nhưng database migration lỗi giữa chừng xử lý sao?

**Trả lời:** Dừng rollout, xác định migration transactional/non-transactional và exact schema state; không rollback app mù nếu compatibility đã đổi. Dùng expand/contract, migration idempotent/checkpoint, backup và roll-forward plan. Tách migration role/pipeline và observe lock/duration.

## 34. Tối ưu AWS cost mà không phá reliability thế nào?

**Trả lời:** Đo unit cost và utilization; loại idle, right-size, lifecycle logs/images/snapshots, giảm NAT/data transfer, chọn commitment/Spot cho workload chịu được interruption. Giữ SLO/headroom và test interruption. Cost anomaly/budget có owner và action.

## 35. Một pipeline production AWS “đạt” cần bằng chứng gì?

**Trả lời:** Traceability commit→digest→environment; test/security/provenance; OIDC/least privilege; IaC plan/change review; progressive rollout theo fault boundary; SLI/business gates; rollback/roll-forward đã test; audit/incident/runbook; RPO/RTO và cost/quota. “Workflow xanh” chỉ là một tín hiệu, không phải bằng chứng hệ thống an toàn.

