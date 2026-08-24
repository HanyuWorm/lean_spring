# 04 — API và application security

## OWASP API Top 10 2023 dưới góc thiết kế

- BOLA/object authorization: authorize object sau khi resolve tenant/owner.
- Broken authentication: IdP chuẩn, throttling, recovery/session/token an toàn.
- Object property authorization: allowlist field đọc/ghi, không bind entity trực tiếp.
- Unrestricted resource consumption: quota cho CPU, memory, DB, upload, fan-out và cost.
- Function authorization: admin/action authorization phía server.
- Sensitive business flow: velocity/risk/device/business-limit, không chỉ request rate.
- SSRF: destination allowlist, URL parser chuẩn, egress firewall/proxy, chặn metadata/private ranges.
- Misconfiguration: hardened baseline, patch, no debug/default credential.
- Inventory: owner/version/exposure/deprecation và shadow API discovery.
- Unsafe API consumption: validate upstream data, timeout/limit, verify TLS/signature.

## Request pipeline

```text
CDN/DDoS -> WAF/rate -> gateway -> authentication -> coarse policy
 -> service -> object/business authorization -> validation -> transaction
 -> audit + security telemetry
```

Gateway không biết đủ domain context để thay authorization trong service.

## Input/output

- Parse một lần bằng canonical rules; reject ambiguity, duplicate keys/headers khi nguy hiểm.
- Schema + size/depth/count limit; allowlist enum/field/operator.
- Parameterized SQL/command; contextual output encoding.
- File upload kiểm tra size/type/content, đổi tên, store ngoài web root, scan/sandbox.
- Không deserialize arbitrary type; ký webhook và kiểm timestamp/replay ID.
- Error response không lộ stack/secret/internal ID nhưng có correlation ID.

## Browser security

Cookie session: `Secure`, `HttpOnly`, `SameSite` phù hợp, CSRF protection cho cookie-authenticated state change. CORS không phải auth; allow exact trusted origin và tránh wildcard với credential. CSP giảm XSS impact nhưng không thay output encoding/sanitization.

## Abuse và availability

Rate limit theo IP thôi dễ bypass/NAT unfair. Kết hợp principal, tenant, API key, device, operation và cost unit. Có global + per-tenant quota, concurrency limit, bounded queue, timeout, payload/page limits. Retry budget và idempotency chống amplification.

## Verification

Mapping acceptance criteria vào exact ASVS 5.0.0 requirement ID. SAST/DAST/SCA/fuzzing hỗ trợ; authorization/business abuse cần integration/property/concurrency tests và manual review.
