CREATE TABLE orders (
        id BIGSERIAL PRIMARY KEY,
        customer_id BIGINT NOT NULL,
        credit_card_number VARCHAR(20) NOT NULL,
        status VARCHAR(20) NOT NULL,
        total_amount DECIMAL(10,2) NOT NULL,
        created_at TIMESTAMP NOT NULL,
        updated_at TIMESTAMP NOT NULL
);