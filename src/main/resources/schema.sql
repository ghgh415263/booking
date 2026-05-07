-- =========================================
-- DROP (역순 삭제)
-- =========================================
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS member_point;

-- =========================================
-- PRODUCT
-- =========================================
CREATE TABLE product (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         price DECIMAL(19,2) NOT NULL,
                         check_in_at DATETIME NULL,
                         check_out_at DATETIME NULL,
                         total_stock INT NOT NULL DEFAULT 0,
                         reserved_stock INT NOT NULL DEFAULT 0,
                         event_product BOOLEAN NOT NULL DEFAULT FALSE
);

-- =========================================
-- MEMBER_POINT
-- =========================================
CREATE TABLE member_point (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              member_id BIGINT NOT NULL UNIQUE,
                              balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
                              reserved DECIMAL(19,2) NOT NULL DEFAULT 0.00
);

-- =========================================
-- ORDERS
-- =========================================
CREATE TABLE orders (
                        order_id VARCHAR(50) PRIMARY KEY,
                        member_id BIGINT NOT NULL,
                        total_amount DECIMAL(15,2) NOT NULL,
                        idempotency_key VARCHAR(50) NOT NULL UNIQUE,
                        status VARCHAR(30) NOT NULL,
                        paid_at DATETIME NULL,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_orders_member_id ON orders(member_id);
CREATE INDEX idx_orders_status ON orders(status);

-- =========================================
-- ORDER_ITEM
-- =========================================
CREATE TABLE order_item (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            order_id VARCHAR(50) NOT NULL,
                            product_id BIGINT NOT NULL,
                            product_name VARCHAR(255) NOT NULL,
                            price DECIMAL(19,2) NOT NULL,
                            quantity INT NOT NULL,
                            total_price DECIMAL(19,2) NOT NULL,

                            CONSTRAINT fk_order_item_order
                                FOREIGN KEY (order_id)
                                    REFERENCES orders(order_id)
                                    ON DELETE CASCADE,

                            CONSTRAINT fk_order_item_product
                                FOREIGN KEY (product_id)
                                    REFERENCES product(id)
);

CREATE INDEX idx_order_item_order_id ON order_item(order_id);

-- =========================================
-- PAYMENT
-- =========================================
CREATE TABLE payment (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         order_id VARCHAR(50) NOT NULL,
                         method VARCHAR(20) NOT NULL,
                         amount DECIMAL(19,2) NOT NULL,
                         status VARCHAR(20) NOT NULL,
                         approved_at DATETIME NULL,
                         payment_key VARCHAR(100) NULL UNIQUE,
                         failed_count INT NOT NULL DEFAULT 0,
                         fail_reason VARCHAR(255) NULL,

                         CONSTRAINT fk_payment_order
                             FOREIGN KEY (order_id)
                                 REFERENCES orders(order_id)
                                 ON DELETE CASCADE
);

CREATE INDEX idx_payment_order_id ON payment(order_id);