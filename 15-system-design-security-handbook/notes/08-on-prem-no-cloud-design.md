# 08 — Thiết kế security khi không dùng cloud

No-cloud nghĩa là tổ chức sở hữu nhiều control plane hơn: facility, hardware lifecycle, hypervisor, network, OS, PKI, database, backup và detection. Nó không mặc định an toàn hơn hay kém hơn cloud.

## Reference capabilities

| Capability | Thành phần | Trách nhiệm |
|---|---|---|
| Identity | AD/LDAP/IdP, MFA, PAM | human identity, federation, privileged JIT |
| Workload identity | internal PKI/SPIFFE/Vault | cert/token ngắn hạn cho service |
| Edge | redundant ISP/router, DDoS service, WAF/LB | absorb/filter/terminate ingress |
| Segmentation | firewall/VRF/VLAN/host policy | isolate zone và east-west flow |
| Compute | hypervisor/bare metal/K8s | hardening, patch, isolation, capacity |
| Data | DB/storage/HSM | access, encryption, replication, backup |
| Management | bastion/ZTNA/PAM | admin path riêng, recorded sessions |
| Detection | EDR/NDR/SIEM/SOAR | endpoint/network/log correlation/response |
| Recovery | immutable/offline backup, clean room | ransomware-safe restore |

## Critical design points

- Out-of-band management network tách production; BMC/iLO không public/user LAN.
- HA cho DNS, NTP, IdP, PKI/OCSP, secret manager và logging—các dependency dễ bị quên.
- Asset/firmware/OS inventory và patch SLA; EOL hardware/software là risk.
- Physical access, media disposal, environmental power/cooling/fire control.
- Golden image/config as code và drift detection.
- Backup theo 3-2-1, ít nhất một bản immutable/offline, restore vào clean environment.
- Vendor remote access time-bound, MFA, allowlisted, recorded và disabled by default.

## Ransomware scenario

Giả định domain admin và backup operator bị compromise. Backup system phải có identity/admin boundary khác, retention lock không bị workload xóa, delayed/anomaly detection và recovery credential offline. Restore order: identity/PKI/DNS → management/security → data → apps; quét và rotate credential trước reconnect.
