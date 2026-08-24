# 13 — Review checklist và 40 câu hỏi

## Architecture review checklist

- Asset/data class, business impact, regulatory/residency và retention đã rõ?
- DFD/trust boundary/protocol/identity và threat model cập nhật?
- Human/workload/device identity, MFA/federation/credential lifecycle?
- Authorization object/function/property/tenant và deny-path tests?
- Ingress/egress/east-west/admin segmentation?
- Encryption/key/secrets owner, rotation, revocation và outage mode?
- API abuse/resource limit, SSRF, file/webhook, dependency trust?
- CI/CD, SBOM/provenance/signing/admission/runtime hardening?
- Immutable correlated audit, detection owner/runbook?
- Backup/restore/ransomware, RPO/RTO và incident tabletop?
- Shared responsibility/RACI, residual risks và acceptance owner?

## 40 câu hỏi có đáp án ngắn

1. **Threat khác vulnerability?** Threat là actor/event có thể gây harm; vulnerability là weakness bị khai thác; risk kết hợp likelihood và impact lên asset.
2. **CIA?** Confidentiality, Integrity, Availability; thêm authenticity/accountability/privacy theo context.
3. **CSF 2.0 mới gì?** Sáu function, nổi bật `Govern` tách governance/risk/supply-chain khỏi Identify.
4. **Defense in depth?** Nhiều control độc lập tương đối qua layers; không phải mua nhiều tool cùng một failure credential.
5. **STRIDE dùng làm gì?** Prompt tìm spoofing, tampering, repudiation, disclosure, DoS, elevation tại DFD element/flow.
6. **Zero Trust?** Không implicit trust do network; evaluate identity/resource/context liên tục và least privilege.
7. **AuthN vs AuthZ?** AuthN chứng minh ai; AuthZ quyết định được làm gì trên resource/context.
8. **BOLA/IDOR?** API nhận object ID nhưng không kiểm principal có quyền trên object; sửa bằng server-side object authorization.
9. **RBAC/ABAC/ReBAC?** Role đơn giản; attribute linh hoạt; relationship hợp sharing/graph. Có thể kết hợp.
10. **JWT hay opaque?** JWT local validation nhưng revocation/freshness khó; opaque introspection central control nhưng thêm dependency.
11. **OIDC vs OAuth?** OIDC authentication/identity; OAuth delegated authorization/API access.
12. **Flow browser/mobile?** Authorization Code + PKCE S256; exact redirect; không implicit/password grant.
13. **DPoP/mTLS token?** Bind token với key để token bị lấy không dùng được nếu thiếu private key.
14. **Passkey có lợi gì?** Public-key, origin-bound, phishing-resistant; vẫn cần recovery/device lifecycle.
15. **Service account key vì sao xấu?** Long-lived, copy được, khó attribution/rotation; dùng workload federation/attached identity.
16. **mTLS đủ cho microservice?** Không; xác thực peer/channel, vẫn cần authorization và cert lifecycle.
17. **WAF sửa logic auth?** Không; WAF không hiểu ownership/business invariant đầy đủ.
18. **SSRF control?** URL canonicalization + destination allowlist + DNS/IP validation + egress + metadata protection.
19. **CORS là security boundary?** Chỉ policy browser; non-browser client bỏ qua, nên API vẫn AuthN/AuthZ.
20. **Rate limit theo IP đủ?** Không; NAT/bot/proxy và business flow cần principal/tenant/device/cost/concurrency limits.
21. **Encrypt at rest bảo vệ gì?** Media/snapshot/storage-layer exposure; không chặn authorized app query sai.
22. **Envelope encryption?** Data key mã hóa data, KMS key wrap data key; hỗ trợ scale/rotation/access audit.
23. **Secret manager đủ chưa?** Không nếu secret vẫn long-lived/bake/log; cần identity, rotation/revocation và least privilege.
24. **Key rotation có luôn re-encrypt data?** Không; có thể rewrap encrypted data keys. Phân biệt key version và crypto policy.
25. **Private subnet an toàn?** Không tự động; SSRF/compromised workload/route/misconfig vẫn tấn công.
26. **Egress control vì sao?** Giảm SSRF, exfiltration, callback; tăng dependency inventory và operational complexity.
27. **Broker tin message nội bộ?** Không; authenticate producer, ACL, validate schema, replay/dedupe và audit.
28. **Multi-tenant control chính?** Trusted tenant context + authorization/query/cache isolation + cross-tenant tests.
29. **Shared responsibility?** Provider bảo vệ cloud infrastructure; customer responsibility thay đổi theo service nhưng data/IAM/config/app vẫn thuộc customer.
30. **Landing zone?** Baseline organization/account/project, identity, policy, network, logs, KMS và vending automation.
31. **Cloud hay on-prem an toàn hơn?** Phụ thuộc control capability/operation; ownership khác, không có đáp án mặc định.
32. **SBOM chứng minh an toàn?** Không; nó là inventory. Cần vulnerability/exploitability, provenance/signature/policy và runtime control.
33. **SLSA?** Framework/spec tăng integrity/provenance của software supply chain theo mức/capability.
34. **Image scan đủ?** Không; còn source/build identity/signing/admission/config/runtime và zero-day.
35. **Immutable log?** Workload/admin bị compromise không thể sửa/xóa trong retention; có separate identity/storage control.
36. **SIEM có cần log mọi thứ?** Cần relevant, normalized, retained telemetry và tested detections; volume không bằng coverage.
37. **Ransomware backup?** Separate admin, immutable/offline, clean restore, known-good identity/artifact và drill.
38. **Fail open hay closed?** Theo harm/business; critical permission thường closed, availability mode phải bounded/audited/approved.
39. **Residual risk ai nhận?** Business/risk owner có authority, không mặc định giao developer/security team.
40. **Security design hoàn thành khi nào?** Requirements/threats có controls, evidence/tests, telemetry/runbooks, residual owner và review trigger.
