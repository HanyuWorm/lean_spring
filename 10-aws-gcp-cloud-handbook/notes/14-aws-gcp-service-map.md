# 14 — AWS ↔ GCP service map

| Capability | AWS | Google Cloud | Khác biệt cần học |
|---|---|---|---|
| Organization | Organizations/OUs/Accounts | Organization/Folders/Projects | account và project không hoàn toàn cùng boundary |
| Landing zone | Control Tower | Enterprise foundations/landing-zone blueprints | orchestration/guardrail implementation khác |
| Workforce | IAM Identity Center | Cloud Identity/Workforce Identity Federation | directory/federation model khác |
| Workload identity | IAM roles, STS, Roles Anywhere/OIDC | service accounts, Workload Identity Federation | trust/policy/effective permissions khác |
| VM | EC2 | Compute Engine | family, disk, network, pricing khác |
| Autoscaled VM | ASG | MIG | health/update semantics khác |
| Containers | ECS/Fargate/App Runner | Cloud Run/GKE Autopilot | ECS không có exact GCP peer |
| Kubernetes | EKS | GKE | modes/add-ons/networking khác |
| Functions | Lambda | Cloud Run functions | limits/runtime/event integration khác |
| Object | S3 | Cloud Storage | consistency/features/classes/pricing khác |
| Block | EBS | Persistent Disk/Hyperdisk | performance/scope/attachment khác |
| Shared file | EFS/FSx | Filestore | specialized families khác |
| Managed SQL | RDS | Cloud SQL | engines/HA/maintenance khác |
| Cloud PostgreSQL | Aurora PostgreSQL | AlloyDB | compatibility/performance/extensions khác |
| Distributed SQL | Aurora DSQL | Spanner | API/SQL/consistency/topology không tương đương tuyệt đối |
| Key-value/document | DynamoDB | Firestore | data model/query/capacity khác |
| Wide column | Keyspaces/DynamoDB patterns | Bigtable | Cassandra/Dynamo/Bigtable models khác |
| Cache | ElastiCache | Memorystore | engine/version/HA khác |
| Warehouse | Redshift | BigQuery | cluster/serverless/cost model khác |
| ETL/catalog | Glue | Dataflow/Dataproc/Dataplex components | không có một mapping duy nhất |
| Queue | SQS | Pub/Sub subscription | visibility/ack/order semantics khác |
| Fan-out | SNS | Pub/Sub | GCP topic/subscription kết hợp pub-sub + queue semantics |
| Event routing | EventBridge | Eventarc | sources/targets/schema khác |
| Kafka | MSK | Managed Service for Apache Kafka | region/features/operation khác |
| Workflow | Step Functions | Workflows | state language/integration/pricing khác |
| API management | API Gateway | API Gateway/Apigee | Apigee thiên enterprise API product/lifecycle |
| DNS | Route 53 | Cloud DNS | routing/health capabilities khác |
| CDN | CloudFront | Cloud CDN | origin/edge/config khác |
| L7/L4 LB | ALB/NLB/GWLB | Cloud Load Balancing families | AWS regional-centric; GCP có global anycast families |
| NAT | NAT Gateway | Cloud NAT | HA/scope/pricing khác |
| Hub network | Transit Gateway/Cloud WAN | Network Connectivity Center | routing/attachment model khác |
| Private service | PrivateLink | Private Service Connect | producer/consumer setup khác |
| Hybrid private | Direct Connect | Cloud Interconnect | locations/redundancy/encryption khác |
| WAF/DDoS | WAF/Shield | Cloud Armor | rules/tiering/integration khác |
| KMS/HSM | KMS/CloudHSM | Cloud KMS/Cloud HSM | key hierarchy/import/attestation khác |
| Secrets | Secrets Manager/Parameter Store | Secret Manager | rotation/integration/pricing khác |
| Threat posture | GuardDuty/Security Hub/Inspector/Macie | Security Command Center/Artifact Analysis/Sensitive Data Protection | capability split khác |
| Metrics/logs | CloudWatch | Cloud Monitoring/Logging | data model/query/pricing khác |
| Audit | CloudTrail | Cloud Audit Logs | event coverage/config/retention khác |
| IaC native | CloudFormation/CDK | Infrastructure Manager (Terraform) | GCP Deployment Manager deprecated |
| Registry/build | ECR/CodeBuild | Artifact Registry/Cloud Build | supply-chain integrations khác |
| Cost | Cost Explorer/CUR/Budgets | Billing reports/export/Budgets | allocation/commitment models khác |

Không chọn cloud bằng số lượng ô tick. Chọn theo business geography, team skill, service fit, ecosystem/contract, data/AI need, reliability/security evidence, cost và exit strategy.
