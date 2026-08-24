# 06 — Replication, partitioning, sharding và CDC

## Bốn khái niệm không được trộn

- **Replication:** nhiều bản sao cùng logical data để HA/read scale.
- **Partitioning:** chia một logical table/collection thành phần trong cùng logical database.
- **Sharding:** chia data qua nhiều node/cluster ownership; query/transaction/ops phức tạp hơn.
- **Caching/projection:** bản sao phục vụ access pattern, thường có consistency khác source.

## Replication contract

Phải trả lời:

- synchronous hay asynchronous acknowledgement;
- commit cần bao nhiêu replica/node xác nhận;
- replica lag được đo theo time/bytes/LSN/opTime thế nào;
- read routing và read-after-write;
- failover tự động hay thủ công, ai có quyền promote;
- fencing chống old primary tiếp tục ghi;
- RPO khi region/node mất;
- backup có độc lập với replication không.

Replication không phải backup: delete/corruption/application bug cũng được replicate.

### Read-after-write options

1. Đọc primary trong một cửa sổ sau write.
2. Session consistency/token/LSN và chờ replica catch up.
3. Sticky routing theo user.
4. Trả representation từ write response.
5. Chấp nhận stale nhưng nói rõ UI/SLA.

Không route mọi read sang replica rồi mong user tự hiểu vì sao dữ liệu vừa tạo biến mất.

## RPO và RTO

- **RPO:** tối đa bao nhiêu dữ liệu theo thời gian có thể mất.
- **RTO:** tối đa bao lâu để khôi phục dịch vụ.

Hai con số này quyết định topology, sync mode, backup frequency, cross-region và chi phí. “Zero RPO/RTO” cần định nghĩa failure scope và thường rất đắt.

## Partitioning

Range partition theo thời gian hỗ trợ retention/pruning, nhưng row mới có thể tạo hot partition. Hash phân phối đều hơn nhưng khó range scan. List theo tenant/region dễ quản trị nhưng skew.

Partitioning không tự làm query nhanh. Predicate phải cho phép partition pruning; quá nhiều partition làm planning/catalog/maintenance nặng. Unique constraint toàn cục có giới hạn khác nhau theo engine.

## Shard key

Shard key tốt cần:

- cardinality đủ cao;
- phân phối write/read đều;
- xuất hiện trong critical queries để target shard;
- không thay đổi;
- tránh monotonic hotspot trừ khi có hashing/bucketing;
- cho phép growth/rebalancing.

Nếu query không có shard key, scatter-gather tăng tail latency và tải toàn cluster. Cross-shard transaction/join làm coordination và failure modes tăng mạnh.

## Hot key và celebrity tenant

Average distribution che hot partition. Biện pháp có thể là bucketing, salted key, isolate tenant, queue serialized command, split aggregate hoặc cache; mỗi cách thay đổi ordering/query complexity. Không “hash mọi thứ” nếu cần range/query co-location.

## CDC và transactional outbox

Dual write DB rồi broker không atomic: crash giữa hai write gây mất hoặc ghost event. Outbox ghi business state và event record trong cùng local transaction; CDC/relay publish sau.

Consumer vẫn phải idempotent vì delivery thường at-least-once. Theo dõi:

- outbox age/lag;
- publish retry/dead-letter;
- schema/version compatibility;
- ordering key;
- duplicate và reconciliation;
- retention/cleanup.

Exactly-once của một broker không tự tạo exactly-once end-to-end qua database và external side effects.

## Backup và PITR

Backup plan cần full/base backup + log archive/oplog strategy phù hợp. Phải mã hóa, tách quyền, kiểm tra checksum, có retention và restore drill. Test:

1. restore vào environment cô lập;
2. replay tới timestamp/transaction mong muốn;
3. kiểm tra schema, row/document count và invariant;
4. đo RTO thật;
5. ghi lại version/tool/credential dependency.

## Failover dangers

- split brain/dual primary;
- stale replica được promote làm data loss;
- DNS/connection pool giữ endpoint cũ;
- application retry write không idempotent;
- replica mới thiếu capacity/cache lạnh;
- failback không được thiết kế;
- backup/CDC slot/binlog retention bị ảnh hưởng.

Architecture diagram phải có failure and recovery path, không chỉ happy-path arrows.
