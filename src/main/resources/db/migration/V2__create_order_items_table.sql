CREATE TABLE order_items (
         id BIGSERIAL PRIMARY KEY,
         order_id BIGINT NOT NULL,
         sku VARCHAR(50) NOT NULL,
         quantity INT NOT NULL,
         unit_price DECIMAL(10,2) NOT NULL,
         subtotal DECIMAL(10,2) NOT NULL,
         CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id)
);