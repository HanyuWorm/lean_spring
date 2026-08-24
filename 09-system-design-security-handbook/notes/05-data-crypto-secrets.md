# 05 — Data security, cryptography và secrets

## Data lifecycle

Discover/classify → collect/minimize → use/share → store/backup → retain/archive → delete. Mỗi bước có owner, purpose, geography, access, encryption, audit và deletion evidence. Tokenization/pseudonymization giảm exposure nhưng mapping table/key vẫn nhạy cảm.

## Encryption

- **In transit:** TLS end-to-end theo trust boundary; verify hostname/certificate, không tắt validation.
- **At rest:** bảo vệ media/snapshot theft; không tự chặn principal đã được DB/storage authorize.
- **Application/field level:** giảm trust vào storage/operator nhưng làm query, rotation, recovery khó.
- **In use/confidential computing:** dành cho threat model cần bảo vệ memory/workload boundary.

Không tự thiết kế algorithm/protocol. Dùng thư viện chuẩn, authenticated encryption (AEAD) và crypto agility. Hash password bằng password KDF được khuyến nghị, unique salt và server-side pepper nếu lifecycle được quản trị; không dùng SHA-256 thuần.

## Envelope encryption

Data key mã hóa payload; key-encryption key trong KMS/HSM mã hóa data key. Lưu encrypted data key cùng ciphertext. KMS policy, audit và separation of duties quan trọng hơn tên algorithm. Key rotation phải phân biệt rotate wrapping key với re-encrypt toàn data.

## Secret lifecycle

Create → distribute just-in-time → use in memory → rotate → revoke → audit. Secret manager không cứu secret đã bake vào image/log. Ưu tiên workload identity/short-lived token thay static secret; scan repository/history/artifact/log và có emergency rotation runbook.

## Key/secrets design questions

- Ai được encrypt, decrypt, rotate, disable và delete?
- Application/data admin có tự grant key access không?
- Region/residency, HSM/FIPS/compliance requirement?
- Mất KMS/PKI/secret manager thì workload degrade thế nào?
- Backup key và restore key được bảo vệ/test độc lập chưa?
- Crypto-shredding có phù hợp retention/legal hold và backup không?
