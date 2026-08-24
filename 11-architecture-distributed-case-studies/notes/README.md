# Study Notes

Các note này không biến một repository demo thành production blueprint. Mỗi bài tách rõ: invariant cần bảo vệ, failure model, cơ chế correctness, cơ chế tối ưu và cách kiểm chứng.

## Thứ tự đọc

1. [End-to-end Flash Sale](05-end-to-end-flash-sale.md)
2. [Race Condition & trừ tồn kho](01-race-condition-inventory.md)
3. [Duplicate Processing & Idempotency](02-idempotency-duplicate-processing.md)
4. [Kafka Ordering & Partitioning](03-kafka-ordering-partitioning.md)
5. [Virtual Threads, HikariCP & Backpressure](04-virtual-threads-backpressure.md)

## Bốn câu trả lời ngắn để phỏng vấn

- `SELECT ... FOR UPDATE` không sai, nhưng khi nhiều request cùng tranh một row, transaction chờ lock vẫn giữ connection. Lock wait kéo dài làm cạn pool, hàng đợi HTTP tăng và hệ thống rơi vào congestion collapse.
- Redis Lua chuyển phép kiểm tra-và-trừ thành một atomic operation rất ngắn. Caffeine chỉ nên giảm read traffic cho dữ liệu ít đổi; không được xem local cache là nguồn sự thật của tồn kho.
- Kafka có at-least-once ở consumer path, vì vậy correctness đến từ inbox/unique constraint và transaction nghiệp vụ. Redis `SET NX` chỉ là fast gate, không thay thế ràng buộc DB.
- Virtual Threads giảm chi phí chờ I/O của Java thread, không tăng số connection mà database xử lý được. Pool nhỏ có chủ đích, admission control, deadline và timeout mới bảo vệ DB.

## Nguyên tắc xuyên suốt

1. Viết invariant trước khi chọn công nghệ, ví dụ `available >= 0`, một payment chỉ capture một lần, version của aggregate tăng đơn điệu.
2. DB unique/constraint/transaction là lớp correctness cuối; cache, bloom filter và rate limiter là lớp giảm tải.
3. Mọi network call có thể thành công ở phía remote nhưng timeout ở phía caller. Retry phải đi cùng idempotency.
4. Eventual consistency phải có state machine, SLA hội tụ, retry, DLQ và reconciliation; không chỉ nói “dùng Kafka”.
5. Benchmark phải báo p50/p95/p99, error rate, resource saturation và cấu hình; không suy rộng con số throughput từ máy khác.
