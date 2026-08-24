# Chặng 5 - Java versus Node.js và full-stack delivery

Thời lượng: tuần 15. Mục tiêu không phải chuyển stack, mà là đủ hiểu để chọn công nghệ và giao tiếp với full-stack team.

## 1. So sánh concurrency model

| Khía cạnh | Java Virtual Threads | Node.js |
|---|---|---|
| Business code I/O | Imperative blocking style | Async/Promise style |
| Scheduling chính | Nhiều virtual thread trên carrier threads | Event loop + async runtime |
| CPU-bound | Cần giới hạn/parallelism có chủ đích | Phải tránh block event loop; worker/process |
| Backpressure | Pool/semaphore/rate limiter/queue | Stream/queue/concurrency limiter |
| Context | ScopedValue/ThreadLocal/telemetry context | AsyncLocalStorage/context propagation |
| Failure hay gặp | Unbounded concurrency làm cạn downstream | Event-loop lag, unhandled async failure |

Không kết luận bằng syntax. So sánh cùng workload, cùng database/downstream limit và cùng SLO.

## 2. Hibernate versus Prisma/TypeORM

Đánh giá:

- unit of work/identity map và change tracking;
- relation loading và N+1;
- transaction API và isolation;
- migration workflow;
- type safety/query expressiveness;
- raw SQL escape hatch;
- observability và connection pooling.

Mỗi ORM có “happy path” khác nhau; senior phải đọc SQL, transaction và pool behavior bất kể abstraction.

## 3. Mini comparison lab

Triển khai cùng API `POST /orders` và `GET /orders/{id}`:

- Java 21 + Spring Boot + Virtual Threads;
- Node.js + TypeScript với framework/ORM team đang dùng;
- PostgreSQL schema giống nhau;
- idempotency key, validation và downstream giả lập giống nhau;
- cùng test script và concurrency levels.

Report phải có developer ergonomics, startup/memory, p99, CPU, connection wait, failure handling và observability. Một benchmark nhỏ không được dùng để tuyên bố stack nào “nhanh hơn” trong mọi trường hợp.

## 4. Full-cycle delivery checklist

- Next.js/React client hiểu BFF contract và error model.
- OpenAPI/schema versioning và generated client nếu phù hợp.
- Docker image, SBOM, non-root runtime và health probes.
- IaC có environment separation, secret management và rollback.
- CI chạy unit/integration/architecture/security checks.
- SLO dashboard, alert, runbook và post-deploy verification.

## Câu hỏi phỏng vấn

1. Event-loop lag và carrier-thread saturation được quan sát khác nhau thế nào?
2. Virtual Threads có khiến reactive programming hết cần thiết không?
3. ORM abstraction rò rỉ ở những điểm nào?
4. Bạn chọn Java hay Node cho BFF dựa trên team/system constraints nào?

