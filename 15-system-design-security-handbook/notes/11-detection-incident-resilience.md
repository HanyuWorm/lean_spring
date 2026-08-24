# 11 — Detection, incident response và resilience

## Security telemetry

Thu thập identity/login/privilege, control-plane audit, API authorization deny, WAF/gateway, network/DNS/egress, host/runtime, database/data access, KMS/secret, CI/CD/artifact và backup. Chuẩn hóa timestamp/correlation; redact token/PII; centralize vào nơi workload admin không xóa được.

Log mọi request không phải mục tiêu. Mục tiêu là answer: ai, từ đâu, dùng identity nào, làm gì trên resource nào, result, before/after nhạy cảm và trace tới deploy.

## Detection engineering

Mỗi rule có threat/use case, data dependency, threshold/baseline, expected false positive, severity, owner, runbook và test. Theo dõi telemetry gap và rule health. Ví dụ: root/break-glass use, impossible travel, new access key, mass decrypt/download, public bucket/policy change, disabled logging, unusual egress, backup retention change.

## Incident lifecycle

Prepare → detect/analyze → contain → eradicate → recover → learn. Preserve evidence/chain of custody trước destructive action khi phù hợp. Containment credential compromise thường gồm revoke session/key, isolate workload, block path và rotate downstream secrets; restart đơn thuần không đủ.

## Recovery

- RPO/RTO theo scenario, không chỉ hardware failure.
- Immutable/offline backup và separate admin plane.
- Clean-room restore, known-good artifact/config, malware scan.
- Restore identity/DNS/KMS dependencies theo order.
- Reconcile data và rotate all potentially exposed credentials.
- Business validation trước reopen traffic.

## Tabletop tối thiểu

IdP compromise, leaked CI credential, malicious dependency, ransomware, public data exposure, DDoS/resource exhaustion và insider mass export. Với mỗi scenario: detect signal, decision owner, communication/legal, containment blast radius, recovery evidence và customer impact.
