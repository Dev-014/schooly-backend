CREATE TABLE IF NOT EXISTS school_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    plan_id BIGINT NOT NULL REFERENCES subscription_plans(id),
    billing_period VARCHAR(50) NOT NULL DEFAULT 'YEARLY', -- QUARTERLY, YEARLY, MONTHLY
    total_students INT NOT NULL DEFAULT 0,
    total_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    amount_paid NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    remaining_amount NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS school_subscription_installments (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES school_subscriptions(id) ON DELETE CASCADE,
    installment_number INT NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, PAID, OVERDUE
    paid_date DATE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster lookup by school_id
CREATE INDEX idx_school_subscriptions_school_id ON school_subscriptions(school_id);
CREATE INDEX idx_school_subscription_installments_sub_id ON school_subscription_installments(subscription_id);

-- Insert sample subscriptions for existing schools (assuming plans are there)
-- For School 1 (School Code: 123456)
INSERT INTO school_subscriptions (school_id, plan_id, billing_period, total_students, total_amount, amount_paid, remaining_amount, status, start_date, end_date)
VALUES (1, (SELECT id FROM subscription_plans ORDER BY id ASC LIMIT 1), 'QUARTERLY', 1250, 120000.00, 30000.00, 90000.00, 'ACTIVE', '2026-04-01', '2027-03-31')
ON CONFLICT DO NOTHING;

INSERT INTO school_subscription_installments (subscription_id, installment_number, amount, due_date, status, paid_date)
VALUES 
((SELECT id FROM school_subscriptions WHERE school_id = 1 LIMIT 1), 1, 30000.00, '2026-04-01', 'PAID', '2026-03-25'),
((SELECT id FROM school_subscriptions WHERE school_id = 1 LIMIT 1), 2, 30000.00, '2026-07-01', 'PENDING', NULL),
((SELECT id FROM school_subscriptions WHERE school_id = 1 LIMIT 1), 3, 30000.00, '2026-10-01', 'PENDING', NULL),
((SELECT id FROM school_subscriptions WHERE school_id = 1 LIMIT 1), 4, 30000.00, '2027-01-01', 'PENDING', NULL)
ON CONFLICT DO NOTHING;
