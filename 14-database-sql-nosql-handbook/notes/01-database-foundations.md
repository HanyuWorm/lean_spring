# 01 — Database foundations

## Mental model: một write đi đâu?

Một lệnh ghi thường đi qua parser/planner, transaction manager, buffer/cache, trang dữ liệu và transaction log. Engine có thể xác nhận commit sau khi log bền vững trước khi page dữ liệu được flush. Đây là lý do WAL/redo log cho phép phục hồi crash mà không ép random-write toàn bộ page tại mỗi commit.

```text
client -> connection/session -> parser/planner -> executor
                                      |             |
                                      v             v
                                catalog/stats   buffer cache
                                                    |
commit -> WAL / redo / oplog -> durable storage <- checkpoint
```

- **Page/block:** đơn vị I/O nội bộ; row/document không nhất thiết được đọc riêng lẻ từ disk.
- **Buffer cache:** giữ page nóng trong RAM. “Database cần nhiều RAM” thường nghĩa là working set và index cần được cache.
- **WAL/redo:** ghi tuần tự mô tả thay đổi trước khi data page bền vững.
- **Undo/old versions:** hỗ trợ rollback và/hoặc MVCC tùy engine.
- **Checkpoint:** rút ngắn recovery nhưng có thể tạo I/O burst nếu cấu hình kém.
- **Statistics:** mô tả phân phối/cardinality để optimizer ước lượng plan; stats cũ tạo plan sai dù SQL không đổi.

## OLTP, OLAP và serving

| Dimension | OLTP | OLAP |
|---|---|---|
| Workload | Nhiều transaction ngắn | Scan/aggregate lớn |
| Model | Thường normalized | Star/snowflake, columnar |
| Latency | Millisecond, tail latency quan trọng | Seconds/minutes chấp nhận được |
| Storage | Row-oriented phổ biến | Column-oriented phổ biến |
| Tối ưu | point lookup, concurrency | compression, vectorized scan |

Không chạy báo cáo không giới hạn trên primary OLTP. Tách workload bằng replica, CDC sang warehouse hoặc giới hạn tài nguyên, tùy freshness requirement.

## ACID chính xác là gì?

- **Atomicity:** toàn bộ transaction commit hoặc rollback.
- **Consistency:** transaction đưa database từ trạng thái thỏa invariant sang trạng thái thỏa invariant. Đây không phải “mọi replica luôn giống nhau”.
- **Isolation:** kết quả concurrent có mức tương đương tuần tự theo isolation level đã chọn.
- **Durability:** commit sống qua loại failure trong durability contract; cần hiểu `fsync`, replication acknowledgement và storage guarantee.

ACID không loại bỏ lỗi nghiệp vụ. Nếu invariant không được encode bằng constraint, atomic conditional update hoặc serialization phù hợp, database vẫn có thể commit dữ liệu sai.

## Consistency vocabulary

- **Strong/linearizable:** operation quan sát như xảy ra tức thời trong một thứ tự toàn cục.
- **Serializable:** kết quả transaction tương đương một thứ tự tuần tự; không đồng nghĩa linearizability theo thời gian thực.
- **Snapshot isolation:** transaction đọc một snapshot; vẫn có thể gặp write skew.
- **Read-your-writes:** session/user đọc thấy write của chính mình.
- **Monotonic reads:** không đọc ngược về version cũ hơn.
- **Eventual consistency:** nếu ngừng update, replica cuối cùng hội tụ; không nêu bounded staleness nếu không có cơ chế đo/giới hạn.

## Row store, document store, key-value và columnar

Chọn layout theo unit of access:

- row store phù hợp transaction trên record và join có chọn lọc;
- document store phù hợp aggregate được đọc/ghi cùng lifecycle;
- key-value phù hợp lookup chính xác theo key với value opaque;
- columnar phù hợp scan một số cột trên lượng row lớn;
- graph phù hợp traversal nhiều hop thay đổi động.

## Capacity variables phải biết

`data size`, `growth/day`, `retention`, `read/write QPS`, `peak factor`, `row/document size`, `working set`, `index size`, `connections`, `transaction duration`, `replication lag`, `backup window`, `RPO/RTO`, `p95/p99 latency`.

Throughput không đủ. Một hệ thống có average 5 ms vẫn có thể hỏng SLO nếu p99 là 5 giây do lock hoặc checkpoint.

## Checklist tự kiểm tra

1. Commit acknowledgement bảo vệ trước những failure nào?
2. Working set có vừa RAM sau khi cộng index và headroom không?
3. Query analytical có cạnh tranh I/O/CPU với OLTP không?
4. Statistics và maintenance được cập nhật ra sao?
5. Consistency contract của từng read endpoint là gì?
6. Restore đã được diễn tập và đo RTO chưa?
