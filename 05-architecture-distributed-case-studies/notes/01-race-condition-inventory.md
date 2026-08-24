# Race Condition & Trừ Tồn Kho

## 1. Race condition thật sự

Hai request cùng đọc `stock = 1`, cùng thấy hợp lệ rồi cùng ghi `0` hoặc cùng tạo order. Check-then-act tách rời không bảo vệ invariant `stock >= 0`.

Một baseline tốt trước khi thêm Redis là atomic conditional update:

```sql
update inventory
set available = available - :quantity,
    version = version + 1
where sku_id = :sku_id
  and available >= :quantity;
```

Chỉ tạo order khi update count bằng `1`; `0` nghĩa là hết hàng hoặc version không còn hợp lệ. Cách này thường đủ cho tải vừa, đơn giản hơn một distributed reservation system.

## 2. Vì sao `SELECT ... FOR UPDATE` làm cạn connection pool

Luồng pessimistic lock thường là:

```text
begin -> borrow connection -> select row for update -> business work
      -> insert/update -> commit -> return connection
```

Khi 1.000 request cùng tranh một SKU, chỉ một transaction giữ row lock; các transaction khác chờ lock nhưng vẫn giữ connection. Với Hikari size 30, gần như cả 30 connection bị buộc vào một hot row. Request mới chờ pool, HTTP threads/virtual threads tích tụ, timeout gây retry và retry lại tăng tải. Đây là positive feedback dẫn tới congestion collapse.

Theo Little's Law, số operation đồng thời `L = lambda * W`. Nếu arrival rate là 2.000/s và lock làm thời gian giữ connection tăng từ 10 ms lên 500 ms, concurrency cần tăng từ khoảng 20 lên 1.000. Mở pool lên 1.000 chỉ chuyển hàng đợi vào DB, tăng context switching, memory, lock-table pressure và có thể ảnh hưởng mọi query khác.

`FOR UPDATE` vẫn hợp lý khi contention thấp, transaction cực ngắn và correctness cần serialization. Vấn đề là dùng nó làm admission queue cho một flash sale hot key.

## 3. Redis Lua giúp ở đâu

Lua gộp check và decrement thành một atomic command chạy gần dữ liệu:

```lua
-- KEYS[1] stock; KEYS[2] buyer marker; ARGV[1] user; ARGV[2] ttl
if redis.call('EXISTS', KEYS[2]) == 1 then return {2, 'DUPLICATE'} end
local stock = tonumber(redis.call('GET', KEYS[1]) or '-1')
if stock <= 0 then return {0, 'SOLD_OUT'} end
redis.call('DECR', KEYS[1])
redis.call('SET', KEYS[2], ARGV[1], 'EX', ARGV[2])
return {1, stock - 1}
```

Nó giảm số request xuống DB từ “mọi người bấm mua” thành “số reservation thắng + retry cần thiết”. Command ngắn, không giữ JDBC connection và serialization xảy ra trên Redis event loop.

Nhưng Lua không tự giải quyết durability giữa Redis, Kafka và DB. Cần reservation ID, TTL, release idempotent, event relay/retry và reconciler. Với Redis Cluster, key của một script phải cùng hash slot.

## 4. Caffeine có và không có tác dụng gì

Caffeine giảm network hop cho catalog/campaign/config và có thể loại request chắc chắn không hợp lệ. Nó không nên là authoritative counter:

- mỗi JVM có một bản sao;
- invalidation có độ trễ;
- restart làm mất state;
- `stock = 0` cũ có thể gây undersell, `stock > 0` cũ có thể tạo tải thừa.

Mẫu an toàn: local cache là hint/read optimization; Redis/DB mới quyết định reservation. Cache key có version, TTL có jitter, refresh-ahead cho hot read, và single-flight để tránh cache stampede.

## 5. Con số “nhanh hơn 20–50 lần”

Không nên xem là hằng số. Mức cải thiện phụ thuộc latency DB/Redis, contention distribution, transaction length, network, durability setting, payload và hardware. Redis + local cache có thể tăng throughput rất lớn vì loại DB lock/write cho phần lớn loser, nhưng phải đo trên workload của mình.

Benchmark cần ít nhất:

- cùng invariant và durability, không so một luồng sync bền với một luồng async chưa persist;
- cùng distribution, gồm một hot SKU chứ không chỉ key đều;
- throughput thành công, sold-out rejection và total requests tách riêng;
- p50/p95/p99/max, timeout/error/retry rate;
- Hikari active/pending, DB lock wait, Redis latency, Kafka lag;
- kiểm tra cuối: `reserved = confirmed + released + pending hợp lệ` và không oversell.

## 6. Decision guide

| Tình huống | Chọn trước |
|---|---|
| Tải vừa, một DB, transaction ngắn | conditional `UPDATE ... available >= qty` |
| Contention thấp nhưng workflow cần đọc row | `FOR UPDATE` + lock timeout ngắn |
| Burst cực lớn, nhiều loser, hot SKU | Redis Lua reservation + queue + reconciliation |
| Nhiều region | phân bổ quota theo region hoặc single-writer; không giả định Redis cross-region atomic miễn phí |

## 7. Anti-pattern

- `synchronized` trong controller: chỉ khóa một JVM và giữ request lâu.
- Distributed lock rồi vẫn thực hiện network call trong critical section.
- Đọc stock từ cache, sau đó enqueue mà không atomic reserve.
- Retry ngay không jitter khi nhận timeout/429.
- Tăng Hikari pool để “hết pending” mà không đo capacity DB.
