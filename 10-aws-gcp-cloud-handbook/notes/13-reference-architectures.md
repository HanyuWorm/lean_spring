# 13 — Reference architectures cho Spring Boot

## AWS regional commerce

```text
Route 53 -> CloudFront + WAF/Shield -> ALB/API Gateway
 -> ECS Fargate/EKS (multi-AZ Spring Boot)
 -> Aurora/RDS Multi-AZ, ElastiCache, S3
 -> SQS/SNS/EventBridge/MSK
CloudWatch/OTel + CloudTrail -> central log/security account
IAM roles -> KMS/Secrets Manager; CI OIDC -> ECR -> signed deploy
```

Chọn ALB cho service HTTP/container; API Gateway khi cần managed API auth/quota/integration; có thể dùng cả hai nhưng tránh duplicate responsibility. Hikari pool budget theo tổng tasks; Lambda/Fargate autoscale không được vượt DB connections.

## GCP regional commerce

```text
Cloud DNS -> global/regional Load Balancing + Cloud CDN/Armor
 -> Cloud Run hoặc GKE (multi-zone Spring Boot)
 -> AlloyDB/Cloud SQL HA, Memorystore, Cloud Storage
 -> Pub/Sub/Eventarc/Managed Kafka
Cloud Monitoring/Logging/Trace + Audit Logs -> central security project
Workload Identity -> KMS/Secret Manager; CI WIF -> Artifact Registry
```

Cloud Run concurrency/instances phải giới hạn theo DB pool/capacity. Serverless VPC access/direct egress, startup/cold latency và request timeout cần test. GKE dùng khi K8s/platform requirement đủ lớn.

## Event flow

Order transaction ghi outbox; CDC/relay publish. SQS/Pub/Sub delivery at-least-once → idempotent consumer → DLQ + replay/reconciliation. Ordering theo aggregate key trong documented scope. Không dual-write DB và broker.

## Multi-region decision

Chỉ triển khai sau khi định nghĩa data writer model, RPO/RTO, failover/fencing, session/cache/event replication, DNS/global LB health, capacity/quota và game day. Active-active stateless dễ hơn stateful; có thể global edge + single writer region + warm standby.
