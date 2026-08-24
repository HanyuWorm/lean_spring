# Lộ trình thực hành 6 tuần

## Tuần 1 — Storage và SQL đúng

- Đọc chương 01-02.
- Khởi động ba database bằng Docker Compose.
- Viết query cho order detail, revenue theo ngày, top customer và keyset pagination.
- Giải thích logical processing order của `SELECT` và hành vi của `NULL`.

**Exit criteria:** không dùng `SELECT *` ở API query; mọi input dùng bind parameter; phân biệt decimal với floating point, timestamp với local date.

## Tuần 2 — Data modeling

- Đọc chương 03 và 07.
- Model cùng commerce domain bằng normalized tables và Mongo aggregate.
- Viết invariant: stock không âm, idempotency key duy nhất, order total không âm.
- Quyết định embed/reference dựa trên lifecycle và query pattern.

**Exit criteria:** mỗi denormalization đều có owner, source of truth và repair strategy.

## Tuần 3 — Index, plan và transaction

- Đọc chương 04-05.
- Chạy `EXPLAIN (ANALYZE, BUFFERS)` trên PostgreSQL và `EXPLAIN ANALYZE` trên MySQL.
- Xóa/thêm index trong môi trường lab và đo sự khác biệt.
- Chạy hai session cạnh tranh inventory; thử optimistic version và atomic update.

**Exit criteria:** phân biệt estimate với actual, scan với lookup, blocking với deadlock, lost update với write skew.

## Tuần 4 — Scale và reliability

- Đọc chương 06 và 12.
- Đặt SLO, RPO, RTO; chọn sync/async replication.
- Thiết kế read-after-write cho màn hình vừa tạo order.
- Viết runbook backup, restore, failover và schema migration expand-contract.

**Exit criteria:** biết dữ liệu nào có thể mất, stale bao lâu, ai kích hoạt failover và cách chống split brain.

## Tuần 5 — Product deep dive

- Đọc chương 08-10 và changelog.
- MySQL: giải thích clustered primary key và next-key lock.
- PostgreSQL: giải thích dead tuples, VACUUM và connection budgeting.
- MongoDB: giải thích read/write concern, shard key và single-document atomicity.

**Exit criteria:** không áp dụng mental model của một engine sang engine khác một cách máy móc.

## Tuần 6 — Solution architecture

- Đọc chương 11, làm câu hỏi chương 14.
- Viết ADR chọn database cho payment ledger, product catalog, session store và search.
- Review capacity: data growth, QPS, working set, hot key, connection, replication lag.
- Trình bày failure modes và migration/exit strategy.

**Capstone:** commerce platform có PostgreSQL/MySQL làm system of record, outbox/CDC tạo projection MongoDB/search/cache. Bảo vệ vì sao từng store tồn tại và điều gì xảy ra khi projection chậm hoặc mất.
