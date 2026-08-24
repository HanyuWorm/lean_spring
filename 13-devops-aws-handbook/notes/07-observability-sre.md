# 07 — Observability và SRE

## 1. Monitoring khác observability

- Monitoring trả lời câu hỏi đã biết bằng dashboard/alert định trước.
- Observability giúp suy luận trạng thái nội tại từ output khi gặp câu hỏi chưa biết.
- Telemetry gồm metrics, logs, traces và có thể bổ sung profiles, events, RUM/synthetics.

Thu thập mọi thứ không tự tạo observability. Cần semantic convention, correlation, retention, ownership và câu hỏi vận hành.

## 2. RED và USE

### Service — RED

- Rate: request/transaction rate.
- Errors: lỗi theo user-visible outcome, không chỉ HTTP 5xx.
- Duration: distribution/p50/p95/p99, không chỉ average.

### Resource — USE

- Utilization: phần trăm resource bận.
- Saturation: queue/wait/throttling.
- Errors: hardware/kernel/provider errors.

DB pool pending/acquire time thường có giá trị hơn chỉ nhìn active connections.

## 3. Cardinality

Label như `user_id`, `order_id`, raw URL hay exception message có cardinality không giới hạn, làm tăng memory/cost và query chậm. ID chi tiết nằm trong trace/log; metric label chỉ dùng dimension bounded như route, method, status class, region.

## 4. SLI, SLO và SLA

- **SLI:** phép đo, ví dụ tỷ lệ request hợp lệ dưới 300 ms.
- **SLO:** target nội bộ trong window, ví dụ 99.9%/30 ngày.
- **SLA:** cam kết bên ngoài và consequence khi vi phạm.

Availability SLI mẫu:

```text
good events / valid events
```

Phải định nghĩa valid event, exclusion, client error, dependency failure và source telemetry. Average uptime theo instance không đại diện user journey.

## 5. Error budget

Error budget là phần không reliability còn được phép trong SLO window. Nó giúp cân bằng tốc độ thay đổi và reliability:

- budget khỏe: tiếp tục delivery/experiment;
- burn nhanh: giảm blast radius, đóng risky change, ưu tiên reliability;
- hết budget: thực hiện policy đã thống nhất, không dùng để đổ lỗi cá nhân.

Multi-window burn-rate alert phát hiện cả cháy nhanh và cháy chậm tốt hơn cảnh báo “SLO dưới 99.9%” cuối tháng.

## 6. Alert tốt

Alert phải có:

- user/business impact hoặc precursor đáng tin;
- severity và urgency;
- owner/on-call route;
- dashboard/context/change gần nhất;
- runbook và first safe action;
- dedup/silence/escalation;
- test định kỳ.

Page khi cần hành động ngay. Ticket cho việc có thể xử lý giờ làm. Dashboard cho khám phá. Alert không hành động được là noise.

## 7. Distributed tracing

- Propagate trace context qua HTTP/message; không dùng trace ID làm auth.
- Span boundary theo remote call/meaningful operation; tránh span cho mọi method.
- Sampling phải giữ lỗi/slow trace phù hợp nhưng kiểm soát cost.
- Async messaging cần producer/consumer semantic và correlation với message/event ID.
- Trace không thay logs audit hoặc metrics aggregate.

## 8. Capacity và load test

- Load test theo arrival pattern, payload, hot key và dependency thật/đại diện.
- Đo throughput tại SLO, saturation point và recovery sau overload.
- Test retry storm, queue backlog, downstream slow, node/AZ loss.
- Capacity plan gồm quota, IP, connection, partition, storage growth và human on-call capacity.
- Autoscaling có delay; cần headroom và admission control.

## 9. Toil

Toil là công việc manual, lặp lại, automatable, reactive, không tạo giá trị bền vững và tăng tuyến tính theo service growth. Không phải mọi operational work là toil: incident learning, capacity design và game day có thể tạo giá trị lâu dài.

Nguồn: [OpenTelemetry](https://opentelemetry.io/docs/), [AWS implement observability](https://docs.aws.amazon.com/wellarchitected/latest/operational-excellence-pillar/implement-observability.html).

