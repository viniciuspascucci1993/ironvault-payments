CREATE TABLE payments (
    id UUID PRIMARY KEY,
    amount NUMERIC(15,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,

    status VARCHAR(30) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,

    description VARCHAR(255),
    external_id VARCHAR(100),
    failure_reason VARCHAR(255),

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);