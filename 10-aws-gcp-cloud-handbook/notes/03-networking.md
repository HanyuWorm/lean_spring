# 03 — Networking

## Core components

| Khái niệm | AWS | GCP | Làm gì |
|---|---|---|---|
| Virtual network | VPC | VPC | logical private network |
| Subnet | AZ-scoped | regional | cấp IP và đặt workload |
| Stateful firewall | Security Group | VPC firewall policy/rule | allow traffic theo instance/interface/tag/account |
| Stateless subnet filter | Network ACL | không mapping 1:1 | filter subnet boundary |
| Internet egress | NAT Gateway | Cloud NAT | private workload outbound không nhận inbound trực tiếp |
| Hub routing | Transit Gateway/Cloud WAN | Network Connectivity Center | kết nối nhiều VPC/hybrid |
| Private service | PrivateLink/VPC endpoints | Private Service Connect/private access | truy cập service bằng private path |
| DNS | Route 53/Resolver | Cloud DNS | public/private name resolution |
| CDN | CloudFront | Cloud CDN/Media CDN | cache content tại edge |
| WAF/DDoS | WAF/Shield | Cloud Armor | L7 filtering và DDoS protection |

NAT gateway, cross-zone/region và egress có thể là cost lớn. NAT không phải firewall toàn năng; dùng egress policy/proxy/DNS logging khi threat model cần.

## Load balancing

AWS ALB cho HTTP/L7, NLB cho TCP/UDP/L4, GWLB cho network appliances. GCP Cloud Load Balancing có global/regional và application/proxy/network passthrough families. Chọn theo protocol, client IP, TLS, global anycast, private/public, zonal failure và target type.

## Addressing

Plan CIDR trước hybrid/multi-account/project để tránh overlap. IPv6, private DNS, service discovery, outbound source IP và partner allowlist cần roadmap. Không hard-code IP của managed service khi có DNS/service endpoint.

## Hybrid

- AWS Site-to-Site VPN/Direct Connect; GCP Cloud VPN/Cloud Interconnect.
- Dùng redundant devices, links, locations và dynamic routing/BGP.
- Private circuit không tự đồng nghĩa encryption; MACsec/IPsec tùy requirement/service.
- Route advertisement/filter, asymmetric routing, MTU, DNS và failover test.

## Review

Mọi flow cần source/destination identity, port/protocol, encryption, direction, owner và logging. Public IP chỉ khi requirement; admin dùng SSM/IAP/ZTNA chứ không mở SSH/RDP rộng.
