# 02 — SQL từ cơ bản tới nâng cao

## Nhóm lệnh

- **DDL:** `CREATE`, `ALTER`, `DROP`; thay đổi schema/catalog.
- **DML:** `SELECT`, `INSERT`, `UPDATE`, `DELETE`, `MERGE`.
- **TCL:** `BEGIN`, `COMMIT`, `ROLLBACK`, `SAVEPOINT`.
- **DCL:** `GRANT`, `REVOKE`; nên cấp quyền tối thiểu theo role.

Syntax và transaction semantics của DDL khác giữa engine. Không giả định migration rollback được chỉ vì application transaction rollback được.

## Logical query processing order

Mental model hữu ích: `FROM/JOIN` → `WHERE` → `GROUP BY` → `HAVING` → window functions → `SELECT` → `DISTINCT` → `ORDER BY` → `LIMIT/OFFSET`.

Optimizer được phép biến đổi vật lý miễn giữ semantics. Alias trong `SELECT` vì thế thường chưa tồn tại ở `WHERE`.

```sql
SELECT customer_id,
       SUM(total_amount) AS revenue
FROM orders
WHERE created_at >= :from_time
  AND status = :status
GROUP BY customer_id
HAVING SUM(total_amount) >= :min_revenue
ORDER BY revenue DESC
FETCH FIRST 20 ROWS ONLY;
```

Luôn bind parameter. String concatenation vừa tạo SQL injection vừa làm plan/cache kém ổn định.

## NULL và three-valued logic

`NULL` là unknown/missing, không bằng chính nó. Dùng `IS NULL`, không dùng `= NULL`. Predicate có thể trả `TRUE`, `FALSE`, `UNKNOWN`; `WHERE` chỉ giữ `TRUE`.

Các bẫy:

- `NOT IN` có một `NULL` có thể làm toàn bộ kết quả thành unknown; ưu tiên `NOT EXISTS` khi phù hợp.
- `COUNT(column)` bỏ qua null; `COUNT(*)` đếm row.
- `LEFT JOIN` rồi filter cột bảng phải trong `WHERE` có thể vô tình biến thành inner join; cân nhắc đặt predicate trong `ON`.

## Join và set operation

- `INNER`: chỉ row match.
- `LEFT/RIGHT/FULL`: giữ phía tương ứng; MySQL không có native `FULL OUTER JOIN`.
- `CROSS`: tích Descartes, hữu ích nhưng nguy hiểm nếu vô tình thiếu join condition.
- `EXISTS`: diễn đạt semi-join “có ít nhất một”.
- `UNION` loại duplicate; `UNION ALL` không sort/deduplicate và thường rẻ hơn.

Không suy ra join order vật lý từ thứ tự SQL; đọc plan.

## CTE, recursive CTE và window function

CTE làm query dễ đọc và recursive CTE xử lý hierarchy. Tùy engine/version, CTE có thể inline hoặc materialize; đừng coi nó mặc định là performance optimization.

```sql
SELECT order_id, customer_id, created_at, total_amount,
       ROW_NUMBER() OVER (
           PARTITION BY customer_id ORDER BY created_at DESC
       ) AS recency_rank,
       SUM(total_amount) OVER (
           PARTITION BY customer_id ORDER BY created_at
       ) AS running_total
FROM orders;
```

Window function giữ từng row, khác `GROUP BY` làm co tập row.

## Pagination

Offset pagination đơn giản nhưng page sâu phải bỏ qua nhiều row và dễ duplicate/missing khi concurrent write. Keyset pagination dùng tuple sắp xếp ổn định:

```sql
SELECT id, created_at, total_amount
FROM orders
WHERE (created_at, id) < (:last_created_at, :last_id)
ORDER BY created_at DESC, id DESC
FETCH FIRST 50 ROWS ONLY;
```

MySQL/PostgreSQL có row comparison nhưng portability có thể khác; viết predicate mở rộng nếu framework/database yêu cầu. Index phải theo `(created_at DESC, id DESC)` hoặc layout tương đương.

## Upsert và concurrency

- PostgreSQL: `INSERT ... ON CONFLICT ... DO UPDATE`.
- MySQL: `INSERT ... ON DUPLICATE KEY UPDATE`.
- SQL `MERGE` có ở PostgreSQL hiện đại và MySQL mới, nhưng semantics/locking khác nhau.
- MongoDB: update với `{upsert: true}`.

Upsert chỉ an toàn cho uniqueness nếu database có unique constraint/index. “Check rồi insert” bằng hai statement có race.

## Sargability

Predicate sargable cho phép index xác định range/lookup:

```sql
-- Kém: function trên indexed column
WHERE DATE(created_at) = :day

-- Tốt hơn
WHERE created_at >= :day_start
  AND created_at <  :next_day_start
```

Functional index có thể giúp nhưng tăng write/storage cost và coupling với expression.

## Data type

- Tiền: integer minor unit hoặc `DECIMAL/NUMERIC`, không dùng binary floating point.
- Thời gian: lưu instant với timezone semantics rõ; business local date là type khác instant.
- ID: sequential/bigint hiệu quả cho B-tree; random UUID phân tán write nhưng lớn và locality kém. UUIDv7 cân bằng uniqueness với time locality.
- Text: chọn collation/case/accent semantics ngay từ đầu.
- Boolean/status: constraint hoặc reference table bảo vệ allowed states.

## SQL review checklist

- Không `SELECT *` trong contract lâu dài.
- Có deterministic `ORDER BY` khi pagination.
- Predicate dùng bind parameter và sargable.
- Join cardinality có được dự đoán không?
- Có unique/foreign/check constraint bảo vệ invariant không?
- Query plan được đo trên data distribution gần production chưa?
- Transaction ngắn và retry policy xử lý deadlock/serialization failure chưa?
