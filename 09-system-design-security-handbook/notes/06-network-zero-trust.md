# 06 — Network và Zero Trust

## Zero Trust đúng nghĩa

NIST SP 800-207 loại bỏ implicit trust chỉ dựa vào network location/ownership. Mỗi access decision dùng identity của user/workload/device, resource, context và policy; liên tục đánh giá phù hợp risk. Zero Trust không đồng nghĩa “mua một VPN mới” hay “mTLS là đủ”.

## Zones và flows

Phân vùng theo trust/data/function: Internet edge, DMZ/ingress, application, data, management, security tooling, backup và build. Default deny east-west/egress; allow flow cụ thể có owner. Management plane không đi chung unrestricted path với user traffic.

## Ingress

DDoS absorption → CDN/WAF → load balancer/gateway → workload. TLS termination point và re-encryption phải rõ. Preserve/validate client identity headers chỉ từ trusted proxy; strip spoofable forwarding headers ở edge.

## Egress

Egress là control chống SSRF/exfiltration/supply-chain callback. Dùng DNS/proxy/firewall allowlist theo workload khi khả thi, private endpoints cho managed services, chặn instance metadata từ untrusted process và log destination/bytes/deny.

## Service-to-service

mTLS xác thực channel/workload nhưng authorization vẫn cần. Certificate ngắn hạn, automated issuance/rotation, SPIFFE-like identity hoặc platform workload identity. Service mesh tăng consistent enforcement/telemetry nhưng thêm control-plane, certificate và bypass complexity.

## On-prem và hybrid connectivity

VPN/IPsec cung cấp encrypted tunnel; private circuit không tự encrypt hoặc authorize. Thiết kế dual link/device/path, BGP route filtering, DNS split-horizon có kiểm soát, overlapping CIDR plan và failure behavior khi cloud/on-prem identity dependency mất.
