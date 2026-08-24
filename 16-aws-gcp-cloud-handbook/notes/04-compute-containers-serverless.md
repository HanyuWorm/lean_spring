# 04 — Compute, containers và serverless

## Service map

| Model | AWS | GCP | Dùng khi |
|---|---|---|---|
| VM | EC2 | Compute Engine | OS/runtime/control hoặc legacy/special hardware |
| Managed instance group | Auto Scaling Group | Managed Instance Group | fleet VM tự heal/scale |
| Container orchestrator | ECS | không exact; Cloud Run/GKE | AWS-native containers không cần Kubernetes |
| Kubernetes | EKS | GKE | cần K8s API/ecosystem/control |
| Serverless container | ECS Fargate/App Runner | Cloud Run | HTTP/job container, scale-to-zero/managed platform |
| Function | Lambda | Cloud Run functions | event handler ngắn, integration-centric |
| Batch | AWS Batch | Batch | queued batch/HPC workloads |

## VM

Chọn family CPU/memory/storage/GPU/ARM, image hardening, immutable replacement, autoscaling và multi-zone. Spot EC2/Spot VM rẻ nhưng bị reclaim; workload phải checkpoint/retry và không đặt critical singleton.

## Containers

- **ECS:** AWS-native scheduler, task definition/service, chạy EC2 hoặc Fargate.
- **EKS/GKE:** managed Kubernetes control plane; node management vẫn tùy mode.
- **Fargate/GKE Autopilot:** provider quản node capacity nhiều hơn, đổi lại constraint/pricing khác.
- **Cloud Run:** deploy container stateless qua revision/service/job, autoscale kể cả zero theo config; request concurrency và cold start/downstream pool phải tune.
- **App Runner:** managed AWS web container đơn giản; ít control hơn ECS/EKS.

## Functions

Lambda và Cloud Run functions hợp event glue/short handlers. Thiết kế idempotency, timeout, retry/DLQ, concurrency cap, package/runtime lifecycle và downstream connection. Không giả định exactly-once. GCP gọi Cloud Functions 2nd gen là Cloud Run functions; code/runtime mới cần xem support schedule.

## Chọn model

VM cho control/compatibility; managed container cho portable image và service dài; Kubernetes khi platform/API/ecosystem complexity có giá trị; serverless khi traffic variable và team muốn giảm infrastructure management. Tính operational skill, cold start, quota, networking, observability, lock-in và unit cost.
