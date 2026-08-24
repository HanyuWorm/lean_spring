# 09 — Observability và operations

| Capability | AWS | GCP |
|---|---|---|
| Metrics/alarms | CloudWatch Metrics/Alarms | Cloud Monitoring |
| Logs | CloudWatch Logs | Cloud Logging |
| Traces | X-Ray/CloudWatch/Application Signals + OpenTelemetry | Cloud Trace + OpenTelemetry |
| Audit | CloudTrail | Cloud Audit Logs |
| Config/assets | AWS Config/Resource Explorer | Cloud Asset Inventory |
| Systems management | Systems Manager | VM Manager/OS Config + service-specific tools |
| Managed Prometheus | Amazon Managed Service for Prometheus | Managed Service for Prometheus |

## Telemetry design

Metrics cho aggregate/trend/alert; logs cho discrete event/context; traces cho distributed request critical path; profiles cho code CPU/memory. Correlate bằng trace/request/business ID nhưng không log token/PII.

OpenTelemetry giảm instrumentation lock-in nhưng backend semantics/cost vẫn khác. Sample theo head/tail/risk, giữ error/high-value traces và quản cardinality. User-defined IDs trong metric labels có thể phá cost/performance.

## SLO

SLI theo user outcome: successful checkout latency/availability, event freshness, data durability. Alert trên burn rate/error budget hơn CPU đơn lẻ. Dashboard có traffic, errors, latency, saturation, dependency/quota và deployment annotations.

## Operational readiness

Runbook, on-call, health probes đúng, graceful degradation, quota alarm, dependency dashboard, backup/failover test, certificate/secret expiry, patch/runtime EOL và cost anomaly. Control-plane API outage và telemetry outage là failure scenarios riêng.
