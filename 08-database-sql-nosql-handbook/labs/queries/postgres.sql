-- 1. Order detail. So sánh với ORM N+1.
SELECT o.id, o.status, o.total_amount, o.created_at,
       c.email,
       i.line_no, i.product_name_snapshot, i.unit_price, i.quantity
FROM orders o
JOIN customers c ON c.id = o.customer_id
JOIN order_items i ON i.order_id = o.id
WHERE o.id = 1
ORDER BY i.line_no;

-- 2. Revenue theo customer trong 30 ngày.
SELECT c.id, c.email, SUM(o.total_amount) AS revenue
FROM customers c
JOIN orders o ON o.customer_id = c.id
WHERE o.status = 'PAID'
  AND o.created_at >= clock_timestamp() - interval '30 days'
GROUP BY c.id, c.email
ORDER BY revenue DESC;

-- 3. Keyset pagination.
SELECT id, status, total_amount, created_at
FROM orders
WHERE customer_id = 1
  AND (created_at, id) < (clock_timestamp(), 9223372036854775807)
ORDER BY created_at DESC, id DESC
FETCH FIRST 20 ROWS ONLY;

-- 4. Execution plan. Seed nhỏ có thể sequential scan là đúng cost decision.
EXPLAIN (ANALYZE, BUFFERS, WAL, SETTINGS)
SELECT id, status, total_amount, created_at
FROM orders
WHERE customer_id = 1
ORDER BY created_at DESC, id DESC
FETCH FIRST 20 ROWS ONLY;

-- 5. Atomic inventory decrement. psql hiển thị UPDATE 1 khi thành công.
BEGIN;
UPDATE products
SET stock = stock - 3, version = version + 1
WHERE id = 2 AND stock >= 3
RETURNING id, stock, version;
ROLLBACK;

-- 6. Claim outbox batch cho nhiều worker; transaction phải ngắn.
BEGIN;
SELECT id, aggregate_id, event_type, payload, occurred_at
FROM outbox_events
WHERE published_at IS NULL
ORDER BY occurred_at, id
FOR UPDATE SKIP LOCKED
FETCH FIRST 100 ROWS ONLY;
ROLLBACK;
