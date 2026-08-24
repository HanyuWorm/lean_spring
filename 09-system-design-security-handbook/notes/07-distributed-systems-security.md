# 07 — Security cho distributed systems

## Microservice identity và policy

Mỗi service có identity riêng, audience-scoped credential, least privilege tới DB/topic/API. Không forward end-user token tùy tiện qua mọi hop; dùng token exchange/on-behalf-of hoặc propagate signed minimal context theo threat model. Audit giữ actor chain: end user → calling workload → target workload.

## Messaging

- ACL theo producer/consumer/topic và environment/tenant.
- TLS + broker identity; secret ngắn hạn.
- Message schema, size, TTL, key/version và data classification.
- Signature/MAC khi cần end-to-end integrity ngoài broker trust.
- Replay/dedup bằng event ID, aggregate sequence/timestamp và bounded window.
- Poison message quarantine không lộ PII; DLQ có retention/access/repair owner.
- Consumer validate payload như external input.

## Multi-tenancy

Tenant context phải đến từ authenticated principal/route, không tin body/header tùy ý. Enforce tenant trong API, query/index/row policy và cache key. Tách encryption key/account/project/cluster khi impact hoặc compliance yêu cầu. Test cross-tenant ở object, search, export, async job, log và backup restore.

## Saga và authorization

Authorization tại command start có thể stale khi step chạy sau. Lưu immutable decision context tối thiểu, re-authorize action nhạy cảm, expire workflow, authenticate compensating command và idempotent mọi handler. Không để internal event trở thành “trusted admin command”.

## Webhook

TLS, allow exact endpoint, HMAC/asymmetric signature trên raw canonical body, timestamp + event ID chống replay, rotate key có overlap, respond nhanh rồi queue, idempotent consumer và reconciliation API.

## Dependency failure

Fail closed cho permission/critical policy; fail open chỉ khi business owner chấp nhận impact và có bounded mode/audit. Cache authorization phải có TTL/revocation semantics. IdP/KMS/policy engine outage là architecture scenario cần capacity và DR.
