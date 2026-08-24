# Chặng 0 - Self-assessment và baseline

Thời lượng: 1-2 ngày. Chỉ dùng kết quả này để ưu tiên học, không dùng để “chấm cấp bậc”.

## Thang điểm

Tự chấm mỗi năng lực từ 0 đến 3:

- `0`: chưa giải thích được;
- `1`: hiểu khái niệm, chưa tự triển khai;
- `2`: đã triển khai và debug được;
- `3`: đã vận hành production, đo đạc và bảo vệ trade-off.

| Năng lực | Điểm hiện tại | Mục tiêu | Bằng chứng cần tạo |
|---|---:|---:|---|
| Java 21/25 language features |  | 3 | Domain refactor + test |
| Virtual Threads/capacity planning |  | 3 | Benchmark + thread dump |
| Spring Boot migration/AOT |  | 2 | Migration report |
| Modulith/hexagonal architecture |  | 3 | Module verification + ADR |
| Hibernate/SQL performance |  | 3 | Query plan + load test |
| Outbox/CDC/idempotency |  | 3 | Failure-injection test |
| Saga/CQRS/Event Sourcing |  | 2 | Decision matrix + PoC |
| Security/observability/resilience |  | 3 | Threat model + dashboard/runbook |
| Node.js/full-stack comparison |  | 2 | Same-workload comparison |

## Baseline exercise

Chọn một API nghiệp vụ có gọi database và một downstream HTTP service. Ghi lại:

- lưu lượng trung bình/peak và concurrency;
- p50/p95/p99 latency, error rate;
- kích thước HTTP pool, DB pool, platform-thread pool;
- transaction boundary và timeout budget;
- hành vi khi downstream chậm 5 giây hoặc database pool hết connection.

Nếu không có hệ thống thật, dùng project `01-code-projects/07-virtual-threads-system-design` làm baseline.

## Kết quả đầu ra

Tạo riêng trong nhật ký cá nhân:

- `baseline.md`: số đo và giả thuyết bottleneck;
- `skills.md`: bảng điểm trên;
- `goals.md`: ba lỗ hổng lớn nhất cần đóng trong 16 tuần.
