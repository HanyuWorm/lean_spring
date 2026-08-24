INSERT INTO customers (id, email, full_name) VALUES
    (1, 'an@example.com', 'Nguyen An'),
    (2, 'binh@example.com', 'Tran Binh'),
    (3, 'chi@example.com', 'Le Chi');

INSERT INTO products (id, sku, name, price, stock) VALUES
    (1, 'JAVA-21', 'Modern Java 21', 39.90, 100),
    (2, 'DB-ARCH', 'Database Architecture', 49.90, 50),
    (3, 'SPRING-4', 'Spring Boot 4', 44.90, 75);

INSERT INTO orders (id, customer_id, idempotency_key, status, total_amount, created_at) VALUES
    (1, 1, 'checkout-001', 'PAID', 89.80, CURRENT_TIMESTAMP(6) - INTERVAL 5 DAY),
    (2, 1, 'checkout-002', 'PENDING', 44.90, CURRENT_TIMESTAMP(6) - INTERVAL 1 DAY),
    (3, 2, 'checkout-003', 'PAID', 49.90, CURRENT_TIMESTAMP(6) - INTERVAL 2 DAY);

INSERT INTO order_items
    (order_id, line_no, product_id, product_name_snapshot, unit_price, quantity)
VALUES
    (1, 1, 1, 'Modern Java 21', 39.90, 1),
    (1, 2, 2, 'Database Architecture', 49.90, 1),
    (2, 1, 3, 'Spring Boot 4', 44.90, 1),
    (3, 1, 2, 'Database Architecture', 49.90, 1);

INSERT INTO outbox_events
    (id, aggregate_type, aggregate_id, event_type, payload)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'Order', '1', 'OrderPaid',
     JSON_OBJECT('orderId', 1, 'customerId', 1, 'totalAmount', 89.80));
