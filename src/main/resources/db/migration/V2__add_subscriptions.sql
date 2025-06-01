-- Simple subscription table for FREE tier focus
CREATE TABLE subscriptions (
                               id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                               user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               plan_type VARCHAR(20) NOT NULL CHECK (plan_type IN ('FREE', 'PLUS', 'PRO')),
                               status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'CANCELLED', 'EXPIRED')),
                               start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               end_date TIMESTAMP,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index and unique constraint
CREATE INDEX idx_subscriptions_user_id ON subscriptions(user_id);
ALTER TABLE subscriptions ADD CONSTRAINT unique_user_subscription UNIQUE (user_id);

-- Give all existing users FREE subscriptions
INSERT INTO subscriptions (user_id, plan_type, status, start_date)
SELECT id, 'FREE', 'ACTIVE', COALESCE(created_at, CURRENT_TIMESTAMP)
FROM users;