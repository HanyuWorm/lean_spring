CREATE TABLE customers (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    email       VARCHAR(320) NOT NULL,
    full_name   VARCHAR(200) NOT NULL,
    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_customers_email UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE products (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    sku         VARCHAR(64) NOT NULL,
    name        VARCHAR(200) NOT NULL,
    price       DECIMAL(19, 2) NOT NULL,
    stock       INT NOT NULL,
    version     BIGINT NOT NULL DEFAULT 0,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT ck_products_price CHECK (price >= 0),
    CONSTRAINT ck_products_stock CHECK (stock >= 0)
) ENGINE = InnoDB;

CREATE TABLE orders (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id      BIGINT NOT NULL,
    idempotency_key  VARCHAR(100) NOT NULL,
    status           VARCHAR(24) NOT NULL,
    total_amount     DECIMAL(19, 2) NOT NULL,
    created_at       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers (id),
    CONSTRAINT uk_orders_idempotency UNIQUE (customer_id, idempotency_key),
    CONSTRAINT ck_orders_total CHECK (total_amount >= 0),
    CONSTRAINT ck_orders_status CHECK (status IN ('PENDING', 'PAID', 'CANCELLED')),
    INDEX ix_orders_customer_created (customer_id, created_at DESC, id DESC),
    INDEX ix_orders_status_created (status, created_at DESC)
) ENGINE = InnoDB;

CREATE TABLE order_items (
    order_id              BIGINT NOT NULL,
    line_no               INT NOT NULL,
    product_id            BIGINT NOT NULL,
    product_name_snapshot VARCHAR(200) NOT NULL,
    unit_price            DECIMAL(19, 2) NOT NULL,
    quantity              INT NOT NULL,
    PRIMARY KEY (order_id, line_no),
    CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_items_price CHECK (unit_price >= 0),
    CONSTRAINT ck_items_quantity CHECK (quantity > 0),
    INDEX ix_items_product (product_id)
) ENGINE = InnoDB;

CREATE TABLE outbox_events (
    id              CHAR(36) PRIMARY KEY,
    aggregate_type  VARCHAR(80) NOT NULL,
    aggregate_id    VARCHAR(80) NOT NULL,
    event_type      VARCHAR(120) NOT NULL,
    payload         JSON NOT NULL,
    occurred_at     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    published_at    TIMESTAMP(6) NULL,
    INDEX ix_outbox_unpublished (published_at, occurred_at)
) ENGINE = InnoDB;
