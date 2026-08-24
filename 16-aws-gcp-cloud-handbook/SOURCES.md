# Nguồn chính thức

Kiểm tra ngày **24/08/2026**. Product availability/pricing/quota thay đổi theo region và thời gian; luôn kiểm tra console/docs trước production.

## AWS

- [AWS Well-Architected Framework](https://docs.aws.amazon.com/wellarchitected/latest/framework/welcome.html)
- [AWS Well-Architected definitions và 6 pillars](https://docs.aws.amazon.com/wellarchitected/latest/framework/definitions.html)
- [AWS Security Pillar](https://docs.aws.amazon.com/wellarchitected/latest/security-pillar/welcome.html)
- [AWS Reliability Pillar](https://docs.aws.amazon.com/wellarchitected/latest/reliability-pillar/welcome.html)
- [AWS Organizations best practices](https://docs.aws.amazon.com/organizations/latest/userguide/orgs_best-practices.html)
- [AWS Control Tower multi-account strategy](https://docs.aws.amazon.com/controltower/latest/userguide/aws-multi-account-landing-zone.html)
- [AWS service documentation index](https://docs.aws.amazon.com/)

## Google Cloud

- [Google Cloud Well-Architected Framework](https://cloud.google.com/architecture/framework)
- [Security, privacy and compliance pillar](https://cloud.google.com/architecture/framework/security)
- [Reliability pillar](https://cloud.google.com/architecture/framework/reliability)
- [Resource hierarchy](https://cloud.google.com/resource-manager/docs/cloud-platform-resource-hierarchy)
- [IAM service-account best practices](https://cloud.google.com/iam/docs/best-practices-service-accounts)
- [Workload Identity Federation](https://cloud.google.com/iam/docs/workload-identity-federation)
- [Cloud Run functions release notes](https://cloud.google.com/functions/docs/release-notes)
- [Deployment Manager deprecation](https://cloud.google.com/deployment-manager/docs/deprecations)
- [Google Cloud product documentation](https://cloud.google.com/docs)

## Current naming/deprecation notes

- Cloud Functions (2nd gen) hiện là Cloud Run functions; Cloud Functions API vẫn được hỗ trợ.
- Google Cloud Deployment Manager hết support từ 01/04/2026; existing deployments còn transition window nhưng greenfield phải dùng Terraform/Infrastructure Manager hoặc công cụ IaC phù hợp.
- “Tương đương” trong service map nghĩa là cùng capability gần nhất, không bảo đảm semantics/SLA/pricing/API giống nhau.
