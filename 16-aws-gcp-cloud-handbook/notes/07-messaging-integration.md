# 07 — Messaging và integration

| Pattern | AWS | GCP | Key point |
|---|---|---|---|
| Work queue | SQS Standard/FIFO | Pub/Sub subscription | consumer competing, retry/DLQ |
| Pub/sub fan-out | SNS + SQS | Pub/Sub topics/subscriptions | mỗi subscriber có delivery state |
| Event bus/routing | EventBridge | Eventarc | route event theo source/filter tới targets |
| Stream | Kinesis Data Streams/MSK | Pub/Sub/Dataflow; Managed Service for Apache Kafka | ordered partitions và replay |
| Workflow | Step Functions | Workflows | orchestration state/retry/compensation |
| Scheduler | EventBridge Scheduler | Cloud Scheduler | time-based trigger, not durable business workflow |
| API management | API Gateway | API Gateway/Apigee | publish/auth/quota/analytics; Apigee mạnh enterprise API lifecycle |

## Semantics

At-least-once là baseline an toàn: consumer idempotent, unique event/command ID, retry backoff và DLQ/reconciliation. FIFO/ordering chỉ trong documented scope (message group/partition/ordering key), throughput và failure có trade-off.

## SQS/SNS/EventBridge

SQS lưu queue và visibility timeout; delete sau xử lý. SNS fan-out push tới subscriptions. EventBridge route events theo rule/schema/integration. Không dùng event bus như database audit duy nhất; retention/replay capability phải kiểm tra exact service/config.

## Pub/Sub/Eventarc

Pub/Sub topic tách publishers khỏi subscriptions; ack deadline, redelivery, retention, ordering key và dead-letter config. Eventarc chuẩn hóa routing event tới Cloud Run/GKE/workflows. Exactly-once feature nếu dùng vẫn cần định nghĩa region/subscription/client và external side effects.

## Design checklist

Payload schema/version/size/classification, ordering key, retry/DLQ, retention/replay, idempotency, backpressure, poison data, producer/consumer IAM, encryption, observability/lag và cost per request/throughput.
