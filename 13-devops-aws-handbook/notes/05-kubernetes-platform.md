# 05 — Kubernetes và platform engineering

## 1. Control loop

Kubernetes là hệ thống reconciliation: controller liên tục so sánh desired state với observed state rồi hành động để thu hẹp chênh lệch. Không nên xem YAML như script chạy một lần.

| Object | Vai trò |
|---|---|
| Pod | đơn vị scheduling; thường ephemeral |
| Deployment | rollout/replica cho stateless workload |
| StatefulSet | identity/order/storage ổn định hơn cho stateful workload |
| Service | endpoint ổn định và load distribution tới pod |
| Ingress/Gateway | HTTP routing vào cluster; cần controller implementation |
| ConfigMap/Secret | config data; Secret không tự mã hóa an toàn chỉ vì tên object |
| Job/CronJob | công việc hữu hạn/lịch; cần idempotency và concurrency policy |

## 2. Scheduling và resource

- `requests` dùng cho scheduling và QoS; quá thấp dẫn overcommit/node pressure.
- `limits` chặn usage; memory vượt limit có thể OOM kill, CPU vượt limit bị throttle.
- Namespace quota không thay requests/limits per workload.
- Affinity/anti-affinity/topology spread điều khiển placement; đừng hard-code hostname.
- Taint/toleration dành cho workload chấp nhận node đặc biệt, không phải authorization.

Right-sizing dựa p95/p99, burst, startup và headroom; vertical recommendation không tự chứng minh SLO.

## 3. Ba loại probe

| Probe | Câu hỏi | Khi fail |
|---|---|---|
| Startup | app đã khởi động xong chưa? | chặn liveness/readiness tới khi startup thành công |
| Readiness | pod có nên nhận traffic lúc này không? | bỏ khỏi endpoints, không restart |
| Liveness | process có kẹt và cần restart không? | kubelet restart container |

Liveness không nên phụ thuộc DB/Kafka. Nếu DB outage làm mọi pod restart, hệ thống tạo restart storm ngay khi dependency đang yếu. Readiness có thể phản ánh khả năng phục vụ nhưng cần tránh làm mất toàn bộ capacity do dependency tạm chậm.

## 4. Safe rollout

- `maxUnavailable` và `maxSurge` phải phù hợp capacity/SLO.
- `preStop` không thay readiness; application cần drain đúng.
- `terminationGracePeriodSeconds` lớn hơn worst-case drain hợp lý.
- PodDisruptionBudget bảo vệ khỏi voluntary disruption, không cứu node crash.
- Spread replica qua node/AZ; xác minh storage/network/LB cũng đa AZ.
- Schema/API tương thích khi old/new pod chạy đồng thời.

## 5. Autoscaling

- HPA scale workload theo metric; metric trễ và stabilization quan trọng.
- Cluster Autoscaler/Karpenter/EKS Auto Mode giải quyết node capacity, không thay HPA.
- Scale pod nhanh hơn node có thể tạo Pending backlog.
- Scale app không giúp nếu database/third-party đã saturated.
- Queue workload nên scale theo backlog age/rate, không chỉ CPU.

## 6. Security boundary

- RBAC least privilege; tách human, workload và automation identity.
- Pod Security Standards/admission policy chặn privileged, host path/network không cần thiết.
- NetworkPolicy default deny chỉ có hiệu lực khi network plugin thực thi.
- Secret encryption at rest, external secret store và rotation; tránh secret trong Git/plain env dump.
- Image digest, signature verification và admission policy.
- Namespace là logical boundary, không mặc nhiên đủ cho hostile multi-tenancy.

## 7. GitOps

GitOps dùng pull-based reconciler để đưa cluster về desired state từ version control.

Ưu điểm:

- audit/review/drift correction;
- cluster không cần mở inbound deploy path từ CI;
- rollback manifest rõ.

Rủi ro:

- Git chứa secret hoặc generated noise;
- controller có quyền quá rộng;
- auto-sync propagates bad config nhanh;
- database/external side effect không rollback chỉ bằng Git revert.

Pipeline tốt: CI build/sign image; bot cập nhật digest trong environment repo; reconciler deploy; health controller đánh giá rollout.

## 8. Production checklist

- Control plane/data plane/upgrade ownership rõ.
- Version skew và add-on compatibility được test.
- Backup + restore etcd/config và persistent data theo scope thực tế.
- Requests/limits/probes/PDB/topology/rollout policy đầy đủ.
- Audit log, cluster/workload telemetry và cost allocation.
- Runbook node pressure, DNS failure, CNI/IP exhaustion, image pull, certificate expiry.
- Game day node/AZ loss và control-plane/API throttling.

Nguồn: [Kubernetes production environment](https://kubernetes.io/docs/setup/production-environment/), [Amazon EKS Best Practices](https://docs.aws.amazon.com/eks/latest/best-practices/introduction.html).

