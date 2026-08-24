# 05 — Security cho Node.js và Next.js

## Security boundary

Mọi input từ HTTP, message, file, env, database và third-party response đều không đáng tin. TypeScript không thay runtime validation. Parse/validate/normalize tại boundary, authorize gần dữ liệu và chỉ trả DTO tối thiểu.

## Authentication, session và authorization

- **Authentication** xác minh identity.
- **Session** duy trì auth state giữa requests.
- **Authorization** quyết định action/resource cụ thể.

UI ẩn button không phải authorization. Proxy/middleware có thể redirect sớm, nhưng secure check vẫn phải chạy trong use case/DAL/Server Action/Route Handler gần data.

### Cookie session

Cookie auth nên `HttpOnly`, `Secure`, `SameSite` phù hợp và có bounded lifetime/rotation. Session ID opaque + server store dễ revoke; encrypted/signed stateless token giảm lookup nhưng revoke/size/rotation phức tạp hơn.

### JWT

Validate algorithm, issuer, audience, expiry/not-before và key rotation. Không decode rồi tin. Không đặt secret/PII nhạy cảm trong payload chỉ vì token được ký; ký không mã hóa. Access token ngắn hạn, refresh-token rotation/reuse detection theo threat model.

## CSRF và CORS

CSRF áp dụng khi browser tự gắn credential như cookie. Dùng SameSite + CSRF token/origin validation tùy flow. CORS chỉ là browser policy, không phải authentication và không bảo vệ server khỏi non-browser client.

## Injection

- SQL: parameterized query, không nối chuỗi.
- Command: tránh shell; nếu cần, dùng fixed executable/arguments và least privilege.
- Path traversal: resolve/allowlist path, không ghép user path tùy ý.
- Header/CRLF: dùng framework APIs và validate.
- XSS: output encoding theo context; React escape text nhưng `dangerouslySetInnerHTML`, URL và third-party content vẫn cần xử lý.

## SSRF

Backend fetch URL từ user có thể truy cập metadata/internal network. Allowlist scheme/host/port, resolve DNS/IP và chặn private/link-local ranges, giới hạn redirect, timeout, response size. DNS rebinding cần thiết kế ở network/client layer, không chỉ regex URL.

## Prototype pollution

Không merge recursively untrusted object vào config/prototype-sensitive target. Dùng validator bỏ unknown properties, map field explicit, dependency đã vá. Tránh tin `__proto__`, `constructor`, `prototype` keys.

## ReDoS và expensive input

Regex có catastrophic backtracking hoặc JSON/compression payload lớn có thể block event loop. Bound length trước validation, chọn regex an toàn, timeout/worker cho CPU task phù hợp và rate/concurrency limit.

## Dependency/supply chain

- commit lockfile và `npm ci` trong CI;
- pin/controlled ranges theo policy;
- audit transitive dependency, install scripts và package provenance;
- automated update + build/test, không blind `npm audit fix --force`;
- SBOM/signature/container scanning theo risk;
- giảm dependency không cần thiết, nhất là package nhỏ có quyền install/runtime lớn.

## Secret và environment

Không commit `.env`; production dùng secret manager/runtime injection. Validate env khi startup, fail fast và redact log. Biến `NEXT_PUBLIC_*` được đưa về client bundle nên không bao giờ chứa secret. Server Component không tự động an toàn nếu truyền secret object vào Client Component.

## Node Permission Model

Permission Model có thể hạn chế filesystem, network, child process, workers và native capabilities. Nó là seat belt cho trusted code, không phải sandbox chống malicious dependency. Kết hợp container non-root, read-only filesystem, seccomp/network policy và minimal credentials.

## Next.js-specific

- Server Action có thể bị gọi bằng POST trực tiếp: validate + authenticate + authorize từng action.
- Route params/search params/form data đều là user input.
- Dùng DAL server-only, DTO tối thiểu và `server-only` guard.
- Cache key không được trộn personalized data giữa users/tenants.
- `proxy.ts` thích hợp optimistic redirect/routing, không là authorization duy nhất.
- Revalidate/invalidate chỉ sau mutation thành công và đúng tenant/tag.

## Security checklist

- Threat model có assets, actors, trust boundaries và abuse cases?
- Runtime schema có reject unknown/oversized payload?
- Authz ở mỗi data mutation/read nhạy cảm?
- Cookie/token/CSRF/CORS đúng browser model?
- Outbound URL, redirect và response size chống SSRF?
- Log/error/trace có redaction?
- Dependency, Node/Next security release và secret rotation có owner?
