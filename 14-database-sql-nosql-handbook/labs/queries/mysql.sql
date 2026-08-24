-- 1. Order detail. So sánh nested lookup với query ORM N+1.
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
  AND o.created_at >= CURRENT_TIMESTAMP - INTERVAL 30 DAY
GROUP BY c.id, c.email
ORDER BY revenue DESC;

-- 3. Keyset pagination theo index (customer_id, created_at, id).
-- Thay boundary bằng row cuối page trước.
SELECT id, status, total_amount, created_at
FROM orders
WHERE customer_id = 1
  AND (created_at, id) < (CURRENT_TIMESTAMP, 9223372036854775807)
ORDER BY created_at DESC, id DESC
LIMIT 20;

-- 4. Execution plan. Dataset seed nhỏ có thể table scan là hợp lý.
EXPLAIN ANALYZE
SELECT id, status, total_amount, created_at
FROM orders
WHERE customer_id = 1
ORDER BY created_at DESC, id DESC
LIMIT 20;

-- 5. Atomic inventory decrement; ROW_COUNT() phải là 1 để thành công.
START TRANSACTION;
UPDATE products
SET stock = stock - 3, version = version + 1
WHERE id = 2 AND stock >= 3;
SELECT ROW_COUNT() AS affected_rows;
ROLLBACK;

-- 6. Outbox relay query. Production workers cần lock/claim strategy.
SELECT id, aggregate_id, event_type, payload, occurred_at
FROM outbox_events
WHERE published_at IS NULL
ORDER BY occurred_at, id
LIMIT 100;
