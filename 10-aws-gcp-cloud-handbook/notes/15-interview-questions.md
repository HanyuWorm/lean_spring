# 15 — 50 câu hỏi có đáp án

1. **Region và zone?** Region là geography; zone là failure domain trong region. Multi-zone chịu zone, không tự chịu region.
2. **Control/data plane?** Control tạo/config resource; data xử lý workload traffic. Thiết kế failure riêng.
3. **Shared responsibility?** Provider và customer chia control theo service model; IAM/data/config/app luôn cần customer ownership.
4. **AWS account?** Resource isolation, quota, billing, security boundary; không phải IAM user.
5. **GCP project?** Resource/API/quota/IAM/billing association boundary dưới folder/organization.
6. **SCP grant permission?** Không; đặt maximum, identity/resource policy vẫn phải allow.
7. **Organization Policy?** GCP constraints kế thừa để guardrail resource configuration.
8. **Landing zone?** Governed baseline cho hierarchy, identity, network, logs, policy, security và vending.
9. **Human access?** Federated SSO, MFA, JIT/permission sets; không static admin users/keys.
10. **Workload access?** Attached role/service account hoặc federation lấy short-lived token.
11. **AWS/GCP VPC khác?** AWS VPC regional/subnet AZ; GCP VPC global/subnet regional.
12. **Security Group vs NACL?** SG stateful resource interface; NACL stateless subnet filter. GCP không mapping NACL 1:1.
13. **NAT làm gì?** Private source outbound; không tự là complete egress firewall và có cost/HA scope.
14. **PrivateLink/PSC?** Private producer-consumer service access không cần public Internet/peering rộng.
15. **ALB vs NLB?** L7 HTTP routing/TLS vs L4 high-performance TCP/UDP/static IP needs.
16. **Direct Connect/Interconnect có encrypted?** Private circuit không mặc định thỏa encryption; chọn MACsec/IPsec theo support/requirement.
17. **VM khi nào?** Legacy, OS/kernel/control/special hardware; đổi lại patch/capacity responsibility.
18. **Kubernetes khi nào?** Khi K8s ecosystem/platform/portability/multi-workload value lớn hơn complexity.
19. **Cloud Run/Fargate?** Provider quản server/node hơn; runtime/network/cost/limit semantics khác.
20. **Function pitfalls?** Retry duplicate, cold start, timeout, concurrency/downstream pool, package/runtime EOL.
21. **Serverless tự scale có làm DB chết?** Có; cap concurrency/instances và connection pool/admission.
22. **Object vs file?** Object API bucket/key không POSIX; file có directory/locking semantics và metadata contention.
23. **Versioning là backup?** Chưa; privileged delete/account compromise cần retention/immutable separate backup.
24. **RDS Multi-AZ vs read replica?** HA failover và read scaling là roles khác; exact topology/version kiểm docs.
25. **Aurora vs AlloyDB?** Cùng cloud-optimized relational category, không identical engine/features/API/SLA.
26. **DynamoDB partition key?** Quyết định placement/distribution/access; hot key làm throttle/skew.
27. **Firestore khi nào?** Document/mobile/web/server patterns; indexes/security rules/transaction model cần fit.
28. **Spanner?** Distributed relational với strong consistency/global options; schema/key/hotspot/cost trade-off.
29. **BigQuery vs OLTP?** Analytical scans/warehouse; không dùng thay transactional DB.
30. **Queue vs pub/sub?** Queue competing consumers; pub/sub mỗi subscription nhận logical copy. Pub/Sub kết hợp topic/subscriptions.
31. **Exactly-once?** Chỉ trong documented boundary; external DB/HTTP side effects vẫn cần idempotency.
32. **Ordering?** Chỉ theo message group/partition/ordering key và availability/throughput trade-off.
33. **DLQ đủ chưa?** Cần alert, owner, inspect/redact, replay/reconciliation và retention.
34. **KMS vs Secrets Manager?** KMS quản cryptographic keys/operations; secret manager quản secret values/versions/rotation.
35. **WAF có bảo vệ BOLA?** Không hiểu ownership/business đầy đủ; authorization ở service.
36. **CloudTrail/Audit Logs?** Control/data access audit theo service/config; centralize immutable và enable needed data events.
37. **OpenTelemetry?** Vendor-neutral instrumentation/export; backend pricing/query/retention vẫn lock-in.
38. **High availability vs DR?** HA xử lý failure thường xuyên tự động; DR phục hồi catastrophe/region/corruption theo RPO/RTO.
39. **RPO/RTO?** Data-loss window và recovery-time target, theo failure scope.
40. **Active-active khó gì?** Data ownership/conflict, global routing, dependency, deploy, capacity và incident.
41. **Backup restore test?** Vì backup success không chứng minh key/tool/data/invariant phục hồi được trong RTO.
42. **Spot dùng thế nào?** Interruptible workload có retry/checkpoint/multi-pool; không critical singleton.
43. **Commitment discount?** Đổi cam kết term/spend/resource lấy giá; mua sau baseline/capacity evidence.
44. **Egress cost?** Internet, cross-region/zone và NAT paths; model data flows trước scale.
45. **IaC state nhạy cảm?** Có topology và có thể secret; encrypt, strict IAM, lock/version/backup.
46. **CI cloud auth?** OIDC workload federation với exact claims và temporary least-privilege role.
47. **Deployment Manager status?** GCP hết support 01/04/2026; dùng Terraform/Infrastructure Manager cho mới.
48. **Cloud Functions tên mới?** 2nd gen là Cloud Run functions; API cũ vẫn supported theo docs.
49. **Multi-cloud có tăng reliability?** Chỉ khi ứng dụng/data/operations/failover thực sự độc lập và tested; thường tăng complexity.
50. **Chọn AWS hay GCP?** Workload/data/AI/geography/team/contract/security/reliability/cost/exit evidence, không chọn bằng slogan.
