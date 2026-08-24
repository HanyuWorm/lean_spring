# 04 — Index và execution plan

## Index là cấu trúc dữ liệu có giá

Index đổi thêm storage, RAM, WAL/redo và write cost để giảm read cost. Mỗi index cần trả lời: query nào dùng, selectivity bao nhiêu, tần suất read/write, có trùng prefix với index khác không.

### Các loại quan trọng

- **B-tree:** equality, range, ordered traversal; mặc định phổ biến.
- **Hash:** equality; phạm vi và implementation phụ thuộc engine.
- **Composite:** thứ tự column quyết định prefix/range/order hữu dụng.
- **Covering:** chứa đủ column để tránh/giảm table lookup; MySQL secondary leaf đã chứa PK, PostgreSQL có `INCLUDE`.
- **Unique:** vừa performance vừa enforcement.
- **Partial/filtered:** chỉ index subset; mạnh ở PostgreSQL.
- **Expression/functional:** index kết quả function/expression.
- **GIN/GiST:** PostgreSQL cho array, JSONB, full-text, range/spatial tùy operator class.
- **BRIN:** PostgreSQL cho bảng rất lớn có correlation vật lý như time-series append.
- **TTL/text/geo/wildcard:** MongoDB theo workload cụ thể.

## Composite index và leftmost prefix

Với `(tenant_id, status, created_at DESC)`, engine thường dùng tốt cho:

- tenant;
- tenant + status;
- tenant + status + range/order created_at.

Không kỳ vọng nó tối ưu query chỉ theo `status` hoặc `created_at`. Quy tắc thực dụng: equality columns trước, rồi range/order, nhưng phải xác nhận distribution và plan. Column selectivity không phải luật duy nhất.

## Selectivity, cardinality và correlation

Index trên boolean thường không hữu ích nếu mỗi giá trị trả phần lớn table, nhưng partial index trên hàng `active = true` hiếm có thể tốt. Data skew làm average statistic sai. Histogram/extended statistics giúp optimizer hiểu distribution/correlation, nhưng vẫn cần quan sát plan thực tế.

## Đọc plan theo thứ tự

1. Query trả bao nhiêu row thực tế và cần bao nhiêu?
2. Estimate lệch actual bao nhiêu? Nếu lệch lớn, xem stats/skew/correlation.
3. Node nào tốn elapsed time, loops, rows removed?
4. Access là full scan, index range/lookup hay bitmap?
5. Join algorithm: nested loop, hash, merge; input size có phù hợp?
6. Sort/hash có spill ra disk không?
7. Buffer/cache hit và physical read?
8. Lock wait/I/O/network có nằm ngoài plan không?

`EXPLAIN ANALYZE` **thực thi query**. Với write hoặc query nặng, dùng transaction rollback/lab/replica an toàn; không tùy tiện chạy production.

### PostgreSQL

```sql
EXPLAIN (ANALYZE, BUFFERS, WAL, SETTINGS)
SELECT id, total_amount
FROM orders
WHERE customer_id = 42
ORDER BY created_at DESC
LIMIT 20;
```

### MySQL

```sql
EXPLAIN ANALYZE
SELECT id, total_amount
FROM orders
WHERE customer_id = 42
ORDER BY created_at DESC
LIMIT 20;
```

MongoDB dùng `.explain("executionStats")` và so `totalDocsExamined`, `totalKeysExamined`, `nReturned`.

## Common anti-patterns

- function/cast implicit trên indexed column;
- leading wildcard `LIKE '%term'` trên B-tree;
- `OR` không phù hợp làm plan khó dùng index;
- fetch quá nhiều row rồi lọc/sort ở application;
- N+1 ORM queries;
- offset page sâu;
- index mọi column “phòng khi cần”;
- duplicate indexes hoặc composite indexes chỉ khác suffix không dùng;
- dùng hint trước khi sửa stats/query/model;
- benchmark dataset quá nhỏ luôn nằm trong cache.

## Plan regression

Plan có thể đổi do stats, volume/skew, parameter, version, config hoặc index. Production practice:

- lưu query fingerprint và p95/p99;
- theo dõi rows examined/returned, buffers, temp spill;
- capture representative plans;
- test upgrade với production-like statistics/data distribution;
- dùng canary và rollback/roll-forward plan;
- tránh hard-code hint trừ khi có ownership và review định kỳ.

## Index review checklist

- Critical query có index theo predicate + ordering không?
- Index có quá rộng hoặc duplicate không?
- PK width ảnh hưởng secondary index ra sao?
- Index-only scan có thật sự tránh heap/table lookup trên engine này không?
- Write amplification và maintenance window?
- Online create/drop semantics theo engine/version?
- Sau deploy đã đo plan và tail latency chưa?
