-- V005: Create subscriptions table
CREATE TABLE subscriptions (
                               id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                               user_id BIGINT NOT NULL,
                               plan_type VARCHAR(20) NOT NULL CHECK (plan_type IN ('FREE', 'PLUS', 'PRO')),
                               status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'CANCELLED', 'EXPIRED')),
                               start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               end_date TIMESTAMP,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Foreign key and constraints
ALTER TABLE subscriptions
    ADD CONSTRAINT fk_subscriptions_user_id
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE subscriptions
    ADD CONSTRAINT unique_user_subscription UNIQUE (user_id);

-- Indexes
CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);