# Case study — Secure commerce platform

## Context

Public web/mobile, partner API, admin refund portal, payment provider webhook, MySQL/PostgreSQL order DB, Kafka events và object storage invoices. Yêu cầu: 10k RPS peak, PCI scope phải giảm, multi-tenant merchants, RPO 5 phút/RTO 1 giờ.

## Bài tập

1. Vẽ DFD và đánh dấu Internet, partner, admin, service, data, CI/CD, logging/backup boundaries.
2. Classify credential, PII, payment token, invoice, audit và event.
3. Viết attack trees cho account takeover, refund fraud, cross-tenant export, webhook replay và CI compromise.
4. Thiết kế OIDC/PKCE cho customer, phishing-resistant/JIT cho admin, mTLS/private-key auth cho partner, workload identity cho service.
5. Thiết kế object/function/property authorization; test merchant A không đọc/export/refund merchant B.
6. Rate/cost limit checkout, search, export, login/recovery và refund riêng.
7. Chọn envelope encryption, key roles, secret-free CI, rotation và mass-decrypt detection.
8. Viết hai deployment: on-prem và cloud; RACI control khác nhau.
9. Thiết kế signed webhook với timestamp/event ID/idempotency/reconciliation.
10. Tabletop: CI identity bị chiếm và image độc được deploy; containment/recovery/evidence?

## Acceptance criteria

- Ít nhất 20 threats, 10 abuse cases và mọi high risk có control/evidence/owner.
- Security requirements mapping ASVS 5.0.0 exact IDs.
- Không static cloud/service credential; break-glass/tested recovery rõ.
- Audit log nằm ngoài workload admin deletion path.
- Restore/ransomware plan nêu identity/key/artifact recovery order.
