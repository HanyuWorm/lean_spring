# 02 — Threat modeling

## Quy trình

1. Xác định scope, assumptions, assets và business impact.
2. Vẽ data-flow diagram: process, datastore, external actor, data flow.
3. Đánh dấu trust boundary, protocol, identity và data classification.
4. Hỏi “điều gì có thể sai?” bằng STRIDE + abuse/business cases.
5. Chọn mitigation theo eliminate → prevent → detect/respond → recover.
6. Validate bằng test/evidence; theo dõi residual risk.
7. Review lại khi data flow, identity, dependency hoặc exposure đổi.

## STRIDE

| Threat | Câu hỏi | Control ví dụ |
|---|---|---|
| Spoofing | Ai có thể giả danh user/service? | phishing-resistant MFA, workload identity, mTLS |
| Tampering | Ai sửa request/event/artifact? | signature/MAC, immutable log, code signing |
| Repudiation | Actor có thể phủ nhận? | correlated audit, trusted time, append-only storage |
| Information disclosure | Data lộ ở đâu? | least privilege, encryption, redaction, egress control |
| Denial of service | Resource nào có thể bị cạn? | quotas, bounded queue, rate limit, isolation |
| Elevation of privilege | Đường nào lên quyền cao? | deny-by-default, JIT, separation of duties |

STRIDE là prompt, không phải risk score. Với API nghiệp vụ thêm abuse case: coupon farming, inventory hoarding, credential stuffing, mass scraping, refund fraud.

## DFD annotation bắt buộc

Với mỗi flow ghi: protocol/TLS, source/destination identity, authentication, authorization point, data class, encryption/key, replay/idempotency, timeout/rate, log/audit và failure behavior.

## Attack tree

```text
Mục tiêu: chiếm tài khoản admin
├─ Phish credential
│  ├─ password + weak MFA
│  └─ session/token theft
├─ Abuse recovery/helpdesk
├─ Compromise IdP/admin device
└─ Escalate từ workload
   ├─ SSRF lấy metadata credential
   └─ overly broad machine role
```

Control ưu tiên cắt nhánh có leverage lớn: phishing-resistant auth, recovery governance, short-lived workload identity, metadata hardening và least privilege.

## Risk register tối thiểu

`ID | asset | threat/scenario | precondition | impact | likelihood | controls | evidence | residual risk | owner | due/review`.

CVSS xếp severity vulnerability, không thay business-risk assessment. Threat model không hoàn thành nếu chỉ có diagram mà không có decision/owner.
