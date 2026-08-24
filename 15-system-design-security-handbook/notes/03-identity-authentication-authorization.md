# 03 — Identity, authentication và authorization

## Ba câu hỏi khác nhau

- **Identification:** principal tuyên bố là ai.
- **Authentication (AuthN):** bằng chứng danh tính đủ assurance không.
- **Authorization (AuthZ):** principal này được làm action nào trên resource nào trong context nào.

User đăng nhập hợp lệ vẫn có thể khai thác BOLA/IDOR nếu API không authorize object.

## Human identity

- Central IdP/federation; joiner-mover-leaver tự động.
- Phishing-resistant MFA/passkey cho privileged và high-risk flow.
- Risk-based/step-up auth không thay baseline secure auth.
- Recovery là authentication flow: bảo vệ helpdesk, SIM/email takeover.
- Privileged access JIT/time-bound, approval, session recording phù hợp.
- Break-glass tách credential, kiểm thử và alert mọi lần dùng.

NIST SP 800-63-4 tách Identity Assurance, Authentication Assurance và Federation Assurance. Chọn assurance theo harm/risk, không chọn “MFA=true” chung cho mọi use case.

## Workload identity

Mỗi service/job/deployment có identity riêng, credential ngắn hạn từ platform/federation. Tránh shared service account và static access key. Bind trust vào issuer, audience, repository/workflow/environment hoặc node/pod identity; log delegation chain.

## OAuth 2.0/OIDC

- OIDC là identity layer trên OAuth; ID token cho client biết authentication, access token cho resource server authorize API.
- Browser/mobile dùng Authorization Code + PKCE (`S256`), exact redirect URI.
- Không dùng implicit grant; không dùng Resource Owner Password Credentials.
- Validate issuer, audience, signature/algorithm/key, expiry/not-before và context claims.
- Access token ngắn, audience/scope hẹp; refresh token rotation hoặc sender-constrained.
- RFC 9700 khuyến nghị mTLS/DPoP cho sender-constrained token nơi threat model cần.
- Token không đặt trong URL/log; browser session cân nhắc BFF + secure `HttpOnly`, `SameSite` cookie.

## Authorization models

- **RBAC:** quyền theo role; dễ hiểu nhưng role explosion.
- **ABAC:** policy theo attributes user/resource/context; linh hoạt nhưng khó debug/govern.
- **ReBAC:** quyền theo relationship graph; hợp sharing/hierarchy.
- **ACL:** danh sách trên resource; trực tiếp nhưng quản trị scale khó.

Thường kết hợp: coarse role + resource ownership/tenant + context. Policy Decision Point quyết định, Policy Enforcement Point bắt buộc enforce; default deny và test deny paths.

## Token/session revocation

JWT tự chứa giảm lookup nhưng revocation/claim freshness khó. Opaque token/introspection dễ central control nhưng thêm dependency. Không nhét quyền thay đổi nhanh vào token sống lâu. Rotation, session version, denylist và short expiry là trade-off; logout UI không tự thu hồi token mọi nơi.
