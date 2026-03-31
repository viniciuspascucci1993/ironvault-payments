CREATE TABLE idempotency (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    request_hash VARCHAR(255),
    response TEXT,
    created_at TIMESTAMP NOT NULL
);