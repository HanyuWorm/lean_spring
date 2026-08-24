# 12 — 35 câu hỏi DevOps nền tảng có đáp án

## 1. DevOps là role, team hay culture?

**Trả lời:** DevOps là tập hợp nguyên tắc/capability tối ưu flow, feedback và learning giữa build và run. Tổ chức có thể có DevOps engineer hoặc platform team, nhưng nếu họ trở thành ticket queue đứng giữa dev và ops thì vẫn giữ silo cũ. Bằng chứng DevOps tốt là thay đổi nhỏ, ownership rõ, delivery an toàn và incident tạo learning loop.

## 2. CI khác continuous delivery và continuous deployment thế nào?

**Trả lời:** CI merge thường xuyên và kiểm tra tự động. Continuous delivery giữ main luôn deployable nhưng production có thể có gate; continuous deployment tự đưa mọi thay đổi đạt gate lên production. Release capability cho user có thể tách deployment bằng feature flag.

## 3. Vì sao “build once, promote many” quan trọng?

**Trả lời:** Nếu build lại mỗi environment, bytes production khác bytes staging đã test và provenance bị đứt. Build một artifact immutable, định danh bằng digest rồi inject config lúc runtime. Database migration/chart/policy cũng cần version tương thích với artifact.

## 4. Deployment khác release thế nào?

**Trả lời:** Deployment đưa code/config vào environment; release cho user dùng behavior. Feature flag tách hai thời điểm, cho phép dark deployment/canary theo cohort. Nhưng flag cần owner, expiry, cleanup và test cả hai trạng thái.

## 5. Immutable infrastructure giải quyết gì?

**Trả lời:** Thay vì patch host đang chạy và tích lũy drift, tạo image/template mới rồi replace instance. Nó cải thiện reproducibility và rollback artifact, nhưng dữ liệu, schema và external side effects vẫn cần migration/backup riêng.

## 6. Rolling, blue/green và canary chọn thế nào?

**Trả lời:** Rolling ít tốn capacity nhưng old/new cùng tồn tại. Blue/green chuyển traffic và rollback nhanh nhưng tốn môi trường kép. Canary giảm blast radius và thu evidence thật nhưng cần metric, sample, bake time và traffic segmentation đúng. Chọn theo state, capacity, SLO và khả năng rollback.

## 7. Tại sao rollback application có thể thất bại?

**Trả lời:** Schema đã contract, data đã biến đổi, message/side effect đã phát hoặc config contract đã đổi. Vì vậy dùng expand/contract, idempotency và xác định lúc nào roll-forward an toàn hơn. Rollback phải được test end-to-end, không chỉ giữ image cũ.

## 8. Idempotency quan trọng gì trong automation?

**Trả lời:** Retry do timeout/network có thể chạy cùng thao tác nhiều lần. Automation idempotent đưa hệ thống về cùng desired result, dùng unique operation ID/conditional update và kiểm tra observed state. Idempotent không đồng nghĩa thread-safe hay transactionally atomic.

## 9. Load average cao có đồng nghĩa CPU đầy không?

**Trả lời:** Không. Linux load còn có task runnable và task chờ uninterruptible I/O. Phải xem CPU breakdown, run queue, I/O wait, disk latency và process state; tăng CPU khi disk/NFS kẹt không giải quyết root cause.

## 10. Disk còn trống nhưng service không tạo được file vì sao?

**Trả lời:** Có thể hết inode, quota, permission, read-only mount hoặc process đạt FD limit. File đã delete nhưng process còn mở cũng giữ block. Kiểm tra `df -h`, `df -i`, `lsof +L1`, mount và `ulimit` trước kết luận.

## 11. Ping thành công có chứng minh application hoạt động không?

**Trả lời:** Không. ICMP reachability khác DNS, TCP port, TLS, HTTP, auth và dependency. Chẩn đoán theo layer bằng resolver, TCP connect, TLS handshake rồi HTTP/application telemetry.

## 12. HTTP 502, 503 và 504 thường nói gì?

**Trả lời:** 502 là proxy nhận response không hợp lệ/connection lỗi từ upstream; 503 thường không có healthy capacity hoặc service chủ động unavailable; 504 là proxy hết thời gian chờ upstream. Đây là nơi phát hiện, không chắc nơi gây lỗi; cần trace và upstream saturation/change evidence.

## 13. Vì sao retry ở mọi tầng nguy hiểm?

**Trả lời:** Attempts nhân nhau tạo retry amplification đúng lúc dependency yếu. Retry chỉ cho lỗi transient, operation idempotent, có exponential backoff+jitter, budget/deadline và giới hạn attempts. Admission control/load shedding thường cần trước retry.

## 14. Docker image layer và cache nên thiết kế thế nào?

**Trả lời:** Dependency/stable input đặt trước, source thay đổi thường xuyên đặt sau; multi-stage loại build tools khỏi runtime. `.dockerignore` giảm context và leak. Cache chỉ tăng tốc, build phải đúng khi cache rỗng.

## 15. PID 1 và signal có vấn đề gì trong container?

**Trả lời:** PID 1 có semantics signal/reap child đặc biệt. Entrypoint shell sai có thể không forward `SIGTERM`, làm orchestrator chờ rồi `SIGKILL`. Dùng exec-form entrypoint hoặc init phù hợp và app graceful shutdown trước grace period.

## 16. Container bị OOMKilled dù Java heap chưa đạt limit vì sao?

**Trả lời:** Cgroup memory gồm heap, native allocations, thread stacks, direct buffers và overhead; kernel kill theo tổng usage. Cần container-aware JVM sizing, native headroom, metric RSS/cgroup, heap/native analysis và giới hạn concurrency, không chỉ tăng `-Xmx`.

## 17. Startup, readiness và liveness probe khác gì?

**Trả lời:** Startup bảo vệ app khởi động chậm khỏi liveness. Readiness quyết định có nhận traffic, fail không restart. Liveness chỉ phát hiện trạng thái process cần restart. Đưa DB vào liveness có thể tạo restart storm trong dependency outage.

## 18. Kubernetes requests và limits dùng làm gì?

**Trả lời:** Scheduler dùng requests để placement; requests còn tác động QoS và capacity planning. Memory vượt limit dễ OOM kill; CPU vượt limit bị throttle. Requests quá thấp làm overcommit, quá cao lãng phí/Pod Pending; phải right-size theo workload/SLO.

## 19. PodDisruptionBudget có đảm bảo HA không?

**Trả lời:** Không. PDB chỉ hạn chế voluntary disruptions như drain, không cứu node crash/AZ loss và không tạo replica/topology. Nó còn có thể chặn upgrade nếu đặt quá chặt; kết hợp replica, spread, capacity và rollout plan.

## 20. HPA có thể làm hệ thống tệ hơn thế nào?

**Trả lời:** Scale pod làm tăng connection/request tới DB hoặc third-party đã saturated. Metric delay có thể gây oscillation; node capacity chưa có làm pod Pending. Cần downstream budget, stabilization, max replicas, headroom và load shedding.

## 21. Kubernetes Secret có an toàn mặc định không?

**Trả lời:** Base64 không phải encryption. Cần encryption at rest, RBAC, audit, external secret/workload identity, rotation và tránh leak qua environment/log/debug. Namespace đơn thuần không phải security boundary mạnh.

## 22. Debug `CrashLoopBackOff` theo thứ tự nào?

**Trả lời:** Xem pod events, container last state/exit code, previous logs, command/config/secret, probe, OOM/resource và dependency. `CrashLoopBackOff` là backoff symptom chứ không phải root cause. Không xóa pod liên tục làm mất evidence.

## 23. IaC khác script provisioning gì?

**Trả lời:** IaC declarative thường có desired state, plan/reconciliation, state/lifecycle và idempotent convergence; script imperative mô tả bước. Cả hai hữu ích, nhưng failure/retry/drift semantics khác. Điều cốt lõi là version, review, test và audit.

## 24. Vì sao Terraform state nhạy cảm?

**Trả lời:** State ánh xạ resource thật và có thể chứa attribute/secret. Mất state gây khó quản lý; lộ state gây security incident. Dùng remote backend mã hóa, access control, versioning, locking/audit; không commit Git.

## 25. `terraform plan` có đảm bảo apply thành công không?

**Trả lời:** Không. Quota, permission, concurrent change, provider/API error và state drift có thể xuất hiện sau plan. Saved plan giúp apply đúng actions đã review, nhưng vẫn cần lock, final validation và xử lý partial apply.

## 26. Vì sao state locking cần thiết?

**Trả lời:** Nó ngăn hai writers đồng thời làm state/resource lệch. Lock chỉ hoạt động nếu backend hỗ trợ. `force-unlock` khi writer còn chạy có thể gây corruption; chỉ dùng đúng lock ID sau khi xác minh owner/run đã chết.

## 27. Drift là gì và xử lý ra sao?

**Trả lời:** Drift là remote khác desired config/state do manual change, external controller hoặc default. Scheduled plan/config audit phát hiện; sau đó import/code hóa approved change hoặc apply để revert. Emergency change phải quay lại IaC sau incident.

## 28. Monitoring khác observability thế nào?

**Trả lời:** Monitoring theo dõi câu hỏi đã biết; observability hỗ trợ suy luận câu hỏi chưa biết từ telemetry. Metrics/logs/traces chưa đủ nếu semantic không thống nhất, không correlation, cardinality uncontrolled và không gắn owner/user impact.

## 29. High-cardinality metric nguy hiểm gì?

**Trả lời:** Label như user/order/raw URL tạo số time series tăng không giới hạn, gây memory/cost/query latency. Dùng bounded dimensions cho metrics; để ID chi tiết trong trace/log và kiểm soát retention/sampling.

## 30. SLI, SLO và SLA khác nhau thế nào?

**Trả lời:** SLI là phép đo; SLO là target nội bộ trong window; SLA là cam kết bên ngoài kèm consequence. SLI phải đo user outcome và định nghĩa good/valid events, không lấy CPU hoặc instance uptime thay availability journey.

## 31. Error budget dùng để làm gì?

**Trả lời:** Nó lượng hóa phần unreliability được phép theo SLO, tạo cơ chế cân bằng feature và reliability. Burn nhanh dẫn tới giảm change risk/ưu tiên remediation theo policy. Không dùng budget để phạt cá nhân hoặc che SLA violation.

## 32. Alert tốt có đặc điểm gì?

**Trả lời:** Có impact/precursor đáng tin, owner, severity, context, runbook và hành động ngay. Page chỉ khi cần phản ứng khẩn; ticket cho việc trì hoãn được. Alert không action hoặc lặp nhiều là toil/noise cần xóa hoặc redesign.

## 33. Incident Commander có phải người kỹ thuật giỏi nhất không?

**Trả lời:** Không nhất thiết. IC quản mục tiêu, ưu tiên, role, escalation và decision log để kỹ sư tập trung mitigation. Người sửa trực tiếp thường là operations/technical lead; tách communication và scribe khi incident lớn.

## 34. Postmortem “blameless” có nghĩa không ai chịu trách nhiệm?

**Trả lời:** Không. Nó tránh phán xét cá nhân để phân tích điều kiện hệ thống, nhưng action vẫn có owner/deadline và team chịu trách nhiệm cải tiến. Dừng ở “human error” bỏ lỡ guardrail, interface, workload và incentive gây lỗi.

## 35. Toil là gì và giảm thế nào?

**Trả lời:** Toil là manual, lặp lại, reactive, automatable, giá trị không bền và tăng tuyến tính. Đo nguồn toil, loại bỏ alert vô ích, self-service/automation thao tác chuẩn và sửa root cause. Automation phải có guardrail; nếu chỉ tự động hóa quy trình xấu, blast radius tăng.

