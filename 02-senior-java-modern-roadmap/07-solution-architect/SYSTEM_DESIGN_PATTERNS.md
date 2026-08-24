# System Design Patterns

Pattern catalog này được tổ chức theo vấn đề kiến trúc. Mỗi lần chọn pattern phải ghi rõ context, forces, consequence, failure modes và cách đo hiệu quả.

## 1. Quy trình thiết kế

1. Xác định business capabilities, actors và critical journeys.
2. Thu thập constraints: time, budget, compliance, platform, team và legacy.
3. Viết quality attribute scenarios có số đo: availability, latency, throughput, security, modifiability, operability và cost.
4. Ước lượng capacity và data growth theo order of magnitude.
5. Xác định ownership/data boundary và consistency requirements.
6. Vẽ baseline, data flow, trust boundary và failure domains.
7. Đề xuất ít nhất hai option; so sánh trade-off/risk/cost.
8. Chốt ADR, fitness functions, transition architecture và rollback.

## 2. Decomposition và modernization

### Modular Monolith

Một deployment, module boundary rõ, database có ownership logic. Là default mạnh khi domain/team chưa đủ lý do trả operational cost của microservices.

### Bounded Context / Service per Business Capability

Tách theo language, invariant và ownership thay vì technical layer hoặc table. Mỗi service phải có lý do độc lập về release, scale, availability hoặc team autonomy.

### Strangler Fig

Đặt routing/facade quanh legacy, chuyển từng capability sang implementation mới và theo dõi traffic. Cần plan cho data ownership, dual-run, reconciliation và điểm xóa code cũ.

### Branch by Abstraction

Tạo abstraction trong cùng codebase rồi chuyển implementation dần. Phù hợp refactor/migration khó hoàn thành trong một release.

### Anti-Corruption Layer

Cô lập model/semantics legacy hoặc vendor. Giảm coupling nhưng thêm mapping, latency và nơi cần observability.

## 3. Communication và workflow

| Pattern | Phù hợp | Rủi ro chính |
|---|---|---|
| Request/Response | Cần kết quả ngay, flow ngắn | Temporal coupling, cascading latency/failure |
| Publish/Subscribe | Nhiều consumer phản ứng độc lập | Eventual consistency, duplicate/order/schema |
| Work Queue | Phân phối job và absorb burst | Queue delay, poison job, fairness |
| Saga | Transaction qua nhiều owner | Compensation, stuck workflow, audit |
| Process Manager | Workflow dài cần explicit state | Central complexity/ownership |
| Outbox + Inbox | DB/event atomic intent + dedup | Relay lag, cleanup, operational overhead |

Ưu tiên synchronous cho query/validation cần phản hồi tức thì; asynchronous cho decoupling/buffering/workflow khi business chấp nhận eventual result. Không dùng Kafka chỉ để tránh thiết kế API timeout.

## 4. Data patterns

### Database per Service

Mỗi service sở hữu schema và chỉ expose qua contract. Tăng autonomy nhưng join/reporting/transaction xuyên service trở nên khó.

### CQRS + Materialized View

Tách write model và read projection khi query shape/scale khác mạnh. Cần freshness SLA, replay/rebuild và reconciliation.

### Event Sourcing

Event là source of truth khi audit/temporal reconstruction là core requirement. Chi phí gồm schema evolution, projection rebuild, operational tooling và mental model; không dùng chỉ vì “event-driven”.

### Cache-Aside

Application đọc cache, miss thì đọc source và populate. Phải quyết định TTL, invalidation, stampede, stale data, negative caching và behavior khi cache down.

### Partitioning/Sharding

Chọn partition key theo access pattern, cardinality, hotspot, rebalancing và tenant isolation. Cross-shard query/transaction là cost cần tính trước.

### Read Replica

Scale read và DR nhưng có replication lag. Không route read-after-write journey sang replica nếu không có consistency mechanism.

## 5. Resilience và stability

### Timeout/Deadline

Mọi remote call phải bounded. Deadline end-to-end tốt hơn mỗi hop tự chọn timeout không liên quan.

### Retry with Backoff/Jitter

Chỉ retry transient failure và idempotent operation, trong retry budget. Retry amplification ở nhiều layer là anti-pattern.

### Circuit Breaker

Ngừng gọi dependency đang lỗi để tiết kiệm tài nguyên và cho nó hồi phục. Không sửa được slow database nội bộ hoặc thiếu capacity.

### Bulkhead/Cell-based Architecture

Chia resource/failure domain theo tenant/region/workload. Cell giảm blast radius và hỗ trợ scale lặp lại nhưng tăng routing, deployment và data-placement complexity.

### Load Shedding/Admission Control

Từ chối sớm khi vượt capacity để giữ hệ thống trong vùng ổn định. Virtual Threads làm pattern này quan trọng hơn vì thread không còn là giới hạn concurrency tự nhiên.

### Fallback/Graceful Degradation

Chỉ trả dữ liệu cũ/thiếu khi business chấp nhận và response nói rõ freshness/quality. Không che data corruption hoặc authorization failure.

### Dead Letter + Reconciliation

DLQ là nơi cách ly để điều tra, không phải nghĩa địa. Cần owner, alert, replay idempotent, retention và reconciliation với source of truth.

## 6. Scalability và performance

- **Stateless compute:** scale ngang dễ hơn; state bền vững nằm ở owner phù hợp.
- **Queue-based load leveling:** absorb burst nhưng tăng latency và cần backlog SLO.
- **Competing consumers:** tăng throughput tới giới hạn partition/downstream.
- **Data locality:** đưa compute gần data/cache, giảm chatty cross-region/service calls.
- **CDN/edge caching:** offload static/public cacheable content; key/invalidation/security phải rõ.
- **Fan-out control:** giới hạn concurrency, deadline và partial result policy.
- **Precomputation/materialized view:** đổi write/storage complexity lấy read latency.

Capacity không chỉ là request/second. Phải tính peak concurrency, payload, CPU time, connection hold time, storage growth, event rate, partition count và recovery/replay rate.

## 7. Availability và disaster recovery

| Pattern | Mục tiêu | Câu hỏi bắt buộc |
|---|---|---|
| Active-passive | Recovery tương đối đơn giản | Failover time, data loss, test frequency? |
| Active-active | Latency/availability đa vùng | Conflict, routing, split brain, cost? |
| Backup/restore | Chống delete/corruption/ransomware | Restore đã test? RPO/RTO thực tế? |
| Multi-AZ | Chịu lỗi hạ tầng cục bộ | App, DB, broker đều phân tán đúng chưa? |
| Cell isolation | Giảm blast radius | Tenant routing và cell evacuation thế nào? |

Replication không thay thế backup; high availability không đồng nghĩa disaster recovery.

## 8. Deployment và evolution

- **Rolling:** tiết kiệm tài nguyên, cần version compatibility trong thời gian mixed deployment.
- **Blue/Green:** rollback traffic nhanh, tốn double environment và database vẫn cần migration plan.
- **Canary:** giới hạn blast radius, cần metric/automated analysis đủ tin cậy.
- **Feature flag:** tách deploy khỏi release, nhưng flag cần owner/expiry và không thay thế backward compatibility.
- **Expand and Contract:** thêm schema/contract mới, migrate consumers/data, rồi mới xóa cũ.
- **Parallel Run/Shadow Traffic:** so sánh output/performance trước cutover; bảo vệ PII và ngăn side effect kép.

## 9. Security architecture patterns

- Zero-trust giữa workload: identity, authenticated/encrypted channel, least privilege.
- Defense in depth: gateway policy + service authorization + database constraint/audit.
- Token exchange/delegation thay vì forward user token tới mọi nơi không kiểm soát.
- Secret lifecycle: generate, distribute, rotate, revoke và audit.
- Data classification dẫn đến encryption, masking, retention, residency và access logging.
- Threat modeling cho trust boundary, không chỉ scan dependency.

## 10. Observability và operability

- Correlation/causation ID xuyên sync và async boundary.
- RED cho service, USE cho resource; metric gắn SLO và low cardinality.
- Health check phân biệt liveness/readiness/startup; không restart loop khi dependency ngoài đang down.
- Audit log khác application log; cần integrity, actor, action và retention.
- Runbook và game day là một phần kiến trúc, không phải việc “sau khi go-live”.

## 11. Decision matrix mẫu

Chấm 1-5 và ghi bằng chứng, không dùng điểm số giả chính xác để che judgment.

| Tiêu chí | Trọng số | Option A | Option B | Evidence/Risk |
|---|---:|---:|---:|---|
| Business fit | 5 |  |  |  |
| Availability/SLO | 5 |  |  |  |
| Security/compliance | 5 |  |  |  |
| Data consistency | 4 |  |  |  |
| Operability | 4 |  |  |  |
| Delivery time | 4 |  |  |  |
| Team capability | 3 |  |  |  |
| Cost/TCO | 3 |  |  |  |
| Vendor lock-in/reversibility | 2 |  |  |  |

## 12. Anti-patterns

- Distributed monolith: service tách deployment nhưng release/data/failure vẫn coupling.
- Shared database integration không có ownership.
- Event-driven mọi thứ, kể cả query cần response tức thì.
- Retry storm, infinite queue, unbounded concurrency.
- Cache dùng để che query/data model kém.
- Multi-region trước khi có RTO/RPO và conflict semantics.
- Microservice per entity/table hoặc per small team.
- Dual write DB + broker không có atomicity/reconciliation.
- Architecture diagram chỉ có boxes/arrows, không protocol, data, trust/failure boundary.
- “Exactly once” không định nghĩa scope và business effect.

## 13. Câu hỏi phỏng vấn Solution Architect

### 1. Khi nào modular monolith tốt hơn microservices trong ba năm tới?

**Trả lời:** Khi team/domain còn nhỏ, phần lớn module cùng release/scale/SLO, transaction đồng bộ có giá trị và bottleneck chưa cần tách vật lý. Package/module boundary, ownership, architecture tests và observability vẫn phải rõ để giữ option extraction. Microservices chỉ thắng khi autonomy về team/release/scale/failure/compliance đủ trả chi phí network, data consistency và operations.

### 2. Làm sao chuyển shared database sang database-per-service không big bang?

**Trả lời:** Map table/data owner và cấm write mới xuyên boundary; đặt API/ACL quanh owner. Tách từng capability bằng branch-by-abstraction/strangler, dùng expand-contract schema, backfill + CDC/outbox để đồng bộ tạm, shadow read/compare và reconciliation. Chuyển writer trước hoặc theo migration plan rõ, rồi consumers, cuối cùng revoke quyền/drop path cũ. Dual-write không có recovery không phải migration plan.

### 3. Chọn consistency model nào cho order, payment, inventory và catalog?

**Trả lời:** Theo invariant/journey, không theo entity label. Order transition và payment ledger cần strong consistency trong owner; payment call idempotent và outcome có thể `UNKNOWN`. Inventory reserve/decrement cần atomic conditional update trong shard/owner nhưng order nhìn reservation eventual qua Saga. Catalog/search/cache chấp nhận eventual với freshness SLA. Cross-owner dùng state machine, outbox và reconciliation thay distributed transaction mặc định.

### 4. Hệ thống chịu được một region mất hoàn toàn ra sao? RPO/RTO bao nhiêu?

**Trả lời:** RPO/RTO là business input theo data tier. Thiết kế gồm traffic failover, compute/config/secrets sẵn, replication/backup độc lập corruption, dependency/identity/DNS và runbook đã diễn tập. Active-passive đơn giản hơn nhưng RTO lớn; active-active giảm RTO nhưng conflict/split-brain/cost cao. Không hứa RPO=0 nếu replication async; đo bằng game day/restore, không bằng diagram.

### 5. Khi traffic tăng 10 lần, bottleneck đầu tiên và kế hoạch kiểm chứng là gì?

**Trả lời:** Không đoán “DB” hoặc “CPU”. Lập concurrency/storage/network model từ arrival rate × service/hold time, xác định limit ở gateway/app pools/DB locks/IO/broker/third party. Load test traffic/data/hot-key distribution thật theo từng bậc, quan sát p95/p99, queue/wait, utilization, error/retry và throughput plateau. Failure test downstream slow để thấy feedback loop; tối ưu bottleneck đầu rồi test lại.

### 6. Pattern nào giảm blast radius? Chi phí vận hành của nó là gì?

**Trả lời:** Bulkhead/cell/shard theo tenant/workload, per-dependency concurrency limit, queue isolation và progressive delivery. Cost là duplicated capacity/data, routing/placement, uneven utilization, config/deploy nhiều cell, cross-cell operation và incident tooling. Chọn isolation key theo failure domain và có cell evacuation/rebalancing plan.

### 7. Làm sao rollback application khi database schema đã migrate?

**Trả lời:** Dùng expand-and-contract: thêm schema backward-compatible, deploy code đọc/ghi tương thích, backfill/verify, chuyển traffic rồi mới contract ở release sau. Rollback app trong mixed-version window vẫn dùng schema mở rộng. Migration destructive thường roll-forward bằng corrective migration; restore DB là disaster operation và có thể mất write mới, không phải nút rollback thường ngày.

### 8. Bạn loại bỏ pattern/công nghệ nào khỏi target architecture và vì sao?

**Trả lời:** Loại thứ không phục vụ driver hoặc không có owner/operational maturity: ví dụ microservices/Kafka/Kubernetes/multi-region/event sourcing nếu scale, autonomy, audit hay availability không yêu cầu. Nêu complexity/cost/failure modes được loại, phương án đơn giản thay thế, trigger/metric để xem xét lại. Khả năng nói “không” có evidence quan trọng hơn danh sách công nghệ dài.
