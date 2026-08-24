# System Design Security Handbook

Track dành cho senior backend engineer và solution architect thiết kế hệ thống an toàn khi chạy **on-premises/no-cloud, cloud hoặc hybrid/multi-cloud**. Security được xử lý như quality attribute có threat, control, evidence và owner; không phải danh sách sản phẩm mua thêm cuối dự án.

> Baseline ngày **24/08/2026**: NIST CSF 2.0, NIST SP 800-207/207A Zero Trust, NIST SP 800-63-4 Digital Identity, OWASP ASVS 5.0.0, OWASP API Security Top 10 2023, OAuth 2.0 Security BCP RFC 9700 và SLSA 1.2.

## Kết quả cần đạt

- Chuyển business impact thành security requirements và risk treatment.
- Vẽ trust boundary/data flow rồi threat-model bằng STRIDE/abuse case.
- Thiết kế identity cho human, workload và device; authorization chống IDOR/BOLA.
- Bảo vệ API, dữ liệu, network, key/secret và software supply chain.
- Chọn control khác nhau cho on-prem, IaaS, PaaS, serverless và hybrid.
- Thiết kế detection, incident response, backup/DR và ransomware recovery.
- Tạo security architecture review có evidence, residual risk và owner.

## Nội dung

| Chương | Trọng tâm |
|---|---|
| [01](notes/01-security-foundations.md) | CIA, risk, CSF 2.0, secure-by-design |
| [02](notes/02-threat-modeling.md) | DFD, trust boundary, STRIDE, abuse case |
| [03](notes/03-identity-authentication-authorization.md) | IAM, OIDC/OAuth, passkey, RBAC/ABAC/ReBAC |
| [04](notes/04-api-application-security.md) | API security, session/token, input/output, SSRF |
| [05](notes/05-data-crypto-secrets.md) | classification, encryption, KMS/HSM, secret lifecycle |
| [06](notes/06-network-zero-trust.md) | segmentation, ZTNA, mTLS, ingress/egress |
| [07](notes/07-distributed-systems-security.md) | microservices, events, replay, multi-tenant |
| [08](notes/08-on-prem-no-cloud-design.md) | DC/on-prem identity, PKI, network, HA, backup |
| [09](notes/09-cloud-hybrid-security.md) | shared responsibility, landing zone, federation |
| [10](notes/10-container-kubernetes-supply-chain.md) | image, K8s, SBOM, signing, SLSA |
| [11](notes/11-detection-incident-resilience.md) | telemetry, SIEM, IR, DR, ransomware |
| [12](notes/12-reference-architectures.md) | reference designs cloud và no-cloud |
| [13](notes/13-review-checklist-and-questions.md) | checklist và 40 câu hỏi có đáp án |

Thực hành bằng [case study](labs/ecommerce-security-case-study.md) và các template trong [templates](templates/README.md). Nguồn chính thức ở [SOURCES.md](SOURCES.md).

## Nguyên tắc ngắn

- Xác thực không thay thế authorization; mọi object/action phải được authorize phía server.
- Private subnet/VPN không tạo implicit trust. Identity, device/workload state và context mới là input quyết định.
- Encrypt không sửa được quyền truy cập sai; key access là phần của data access.
- WAF là defense-in-depth, không vá business authorization.
- Secret không nằm trong source, image, log hoặc long-lived environment không được quản trị.
- Audit log phải chống sửa/xóa bởi chính workload bị compromise.
- Backup cần immutable/offline copy và restore drill.
- Mọi control phải có owner, telemetry, failure mode và cách kiểm chứng.
