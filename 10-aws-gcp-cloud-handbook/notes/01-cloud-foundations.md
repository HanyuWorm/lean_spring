# 01 — Cloud foundations

## Thành phần vật lý/logical

- **Region:** khu vực địa lý gồm nhiều zone; data residency, latency, price và service availability khác nhau.
- **Availability Zone/Zone:** failure domain độc lập tương đối trong region. Deploy nhiều zone để chịu một zone failure.
- **Point of Presence/Edge:** vị trí gần user cho CDN/DNS/DDoS/edge compute.
- **Control plane:** API quản lý resource/config/IAM; **data plane:** xử lý traffic/data workload. Failure/security của hai plane khác nhau.
- **Quota:** giới hạn service/account/project/region; là dependency cần capacity planning.

AWS VPC/subnet có scope khác GCP: AWS VPC regional và subnet nằm một AZ; GCP VPC global và subnet regional. Đây là khác biệt nền tảng khi map network.

## Service models

| Model | Bạn quản lý | Provider tăng trách nhiệm ở |
|---|---|---|
| IaaS VM | OS, runtime, app, data, network config | facility/hardware/hypervisor |
| Managed container/K8s | image/app/policy; có thể còn node | control plane/node tùy mode |
| PaaS/serverless | code/image/config/data/IAM | OS/runtime platform/scaling |
| SaaS | identity/config/data usage | application platform/operation |

Shared responsibility phải viết theo exact service/mode; EKS managed control plane khác ECS Fargate, GKE Standard khác Autopilot.

## Six pillars

AWS và GCP đều tổ chức guidance hiện tại quanh: operational excellence, security, reliability, performance efficiency/optimization, cost optimization và sustainability. Pillar là review lens, không phải trade-off tuyệt đối: tăng multi-region reliability có thể tăng cost/complexity/carbon và consistency latency.

## Deployment archetypes

- **Single-zone:** dev hoặc workload chấp nhận zone outage.
- **Regional multi-zone:** baseline production phổ biến.
- **Multi-region active-passive:** DR/failover, replication/RPO và cold/warm capacity.
- **Active-active:** latency/availability cao nhưng data conflict, routing, deployment và incident khó.
- **Hybrid:** on-prem + cloud vì migration, latency, regulation hoặc capability.
- **Multi-cloud:** business requirement cụ thể; không phải checkbox resilience tự động.

## Cloud economics

Tính cả compute time, storage class/operations, provisioned capacity, requests, network egress/cross-zone/cross-region, logging, backup, NAT/LB/IP, licenses và support. Tag/label + account/project allocation + unit economics (`cost/order`, `cost/tenant`) tốt hơn chỉ xem hóa đơn tổng.
