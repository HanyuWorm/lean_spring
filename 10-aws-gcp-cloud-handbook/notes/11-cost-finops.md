# 11 — Cost và FinOps

## Cost drivers

Compute instance/runtime, provisioned capacity, storage GB + operation/retrieval, database I/O/backup, request/event, logs/metrics cardinality, NAT/LB/public IPv4, cross-zone/region và Internet egress, licenses/support.

## AWS tools

Cost Explorer, Cost and Usage Report/Data Exports, Budgets, Cost Anomaly Detection, Compute Optimizer, Savings Plans/Reserved Instances, Spot, S3 lifecycle. Organizations/account/tag/Cost Categories cho allocation.

## GCP tools

Cloud Billing reports/export to BigQuery, Budgets/alerts, Recommender, committed use discounts, Spot VMs, storage lifecycle. Organization/folder/project/labels cho allocation.

Budget alert không hard-stop mặc định. Nếu tự động shutdown phải tránh production/backup/security resources và có approval.

## Practice

- Showback/chargeback và owner cho unallocated cost.
- Unit economics: cost/order, tenant, GB processed, model inference.
- Rightsize sau đo p95/p99 và headroom; không chỉ average CPU.
- Commitment mua sau khi baseline ổn định, hiểu term/scope/coverage.
- Schedule non-prod, delete orphan disk/snapshot/IP/LB/log.
- Egress/cross-zone architecture review trước scale.
- Cost anomaly + forecast + monthly architecture review.

FinOps không phải chỉ giảm bill: tối đa business value trong SLO/security/reliability constraints.
