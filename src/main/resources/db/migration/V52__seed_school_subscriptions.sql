-- Seed subscription plans if none exist (defensive)
INSERT INTO subscription_plans (name, description, annual_price, monthly_price, status, created_at, updated_at)
SELECT 'Enterprise Suite', 'Full-featured enterprise plan', 500000.00, 45000.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE name = 'Enterprise Suite');

-- Seed subscriptions for existing schools that don't have one
INSERT INTO school_subscriptions (
    school_id, plan_id, billing_period, total_students, total_amount, amount_paid, remaining_amount, status, start_date, end_date, created_at, updated_at
)
SELECT 
    s.id,
    (SELECT id FROM subscription_plans ORDER BY id LIMIT 1),
    'YEARLY',
    5000,
    150000.00,
    50000.00,
    100000.00,
    'ACTIVE',
    CURRENT_DATE - INTERVAL '1 month',
    CURRENT_DATE + INTERVAL '11 months',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM schools s
WHERE NOT EXISTS (
    SELECT 1 FROM school_subscriptions ss WHERE ss.school_id = s.id
);

-- Seed installments for the newly created subscriptions
INSERT INTO school_subscription_installments (
    subscription_id, installment_number, amount, due_date, status, paid_date, created_at, updated_at
)
SELECT 
    ss.id,
    1,
    50000.00,
    CURRENT_DATE - INTERVAL '1 month',
    'PAID',
    CURRENT_DATE - INTERVAL '25 days',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM school_subscriptions ss
WHERE NOT EXISTS (
    SELECT 1 FROM school_subscription_installments ssi WHERE ssi.subscription_id = ss.id AND ssi.installment_number = 1
);

INSERT INTO school_subscription_installments (
    subscription_id, installment_number, amount, due_date, status, paid_date, created_at, updated_at
)
SELECT 
    ss.id,
    2,
    50000.00,
    CURRENT_DATE + INTERVAL '2 months',
    'PENDING',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM school_subscriptions ss
WHERE NOT EXISTS (
    SELECT 1 FROM school_subscription_installments ssi WHERE ssi.subscription_id = ss.id AND ssi.installment_number = 2
);

INSERT INTO school_subscription_installments (
    subscription_id, installment_number, amount, due_date, status, paid_date, created_at, updated_at
)
SELECT 
    ss.id,
    3,
    50000.00,
    CURRENT_DATE + INTERVAL '5 months',
    'PENDING',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM school_subscriptions ss
WHERE NOT EXISTS (
    SELECT 1 FROM school_subscription_installments ssi WHERE ssi.subscription_id = ss.id AND ssi.installment_number = 3
);
