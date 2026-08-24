# 12 — Reference architectures

## Internet commerce — cloud

```text
Internet -> DDoS/CDN/WAF -> public LB/API gateway
  -> private workload identity services -> private DB/cache/broker
                    |                         |
                    +-> KMS/secret            +-> encrypted backup
All control/data/audit logs -> isolated security/log account -> SIEM
CI OIDC -> isolated build -> signed artifact -> registry -> policy admission
```

Controls: multi-account/project landing zone, no inbound SSH, JIT admin, private endpoints, egress policy, object/business authorization, idempotency/anti-abuse, key separation, immutable audit/backup. Multi-region chỉ khi RTO/RPO và data consistency justify.

## Internet commerce — on-prem/no-cloud

```text
Dual ISP/DDoS -> redundant edge FW -> WAF/LB DMZ
 -> app zone/K8s -> data zone
management zone -> PAM/bastion -> all admin planes
security zone <- network/endpoint/app/audit logs
backup zone + offline/immutable copy -> recovery clean room
IdP/PKI/DNS/NTP redundant across failure domains
```

Không mở management từ Internet/corporate LAN trực tiếp. Firewall flow allowlist, workload certificates ngắn hạn, separate backup identity, tested failover power/network/storage.

## B2B integration

Dedicated client identity/tenant, mTLS/private key JWT, audience/scope, per-partner quota, payload schema/size, signed webhook, IP allowlist chỉ defense-in-depth, partner-specific kill switch và reconciliation. Không shared API key cho nhiều partner.

## Admin plane

Workforce federation → phishing-resistant MFA → managed device/context → ZTNA/PAM → JIT role → audited session. Admin API/domain tách user plane; change qua IaC/pipeline; break-glass two-person + real-time alert.

## Design choice cloud vs no-cloud

So sánh control ownership, available skills, patch latency, managed security scope, data locality, connectivity, elasticity, supply-chain/provider concentration, exit strategy và total cost. Requirement “không cloud” không bỏ threat modeling; “cloud-first” không bỏ shared-responsibility matrix.
