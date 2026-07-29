-- V32__add_per_student_pricing_and_school_relations.sql

-- 1. Enhance subscription_plans table
ALTER TABLE subscription_plans
    ADD COLUMN billing_model VARCHAR(50) DEFAULT 'PER_STUDENT',
    ADD COLUMN price_per_student NUMERIC(10, 2);

-- Update existing records so they have a default value instead of null
UPDATE subscription_plans SET price_per_student = 149.99, billing_model = 'PER_STUDENT' WHERE price_per_student IS NULL;

-- 2. Enhance schools table
ALTER TABLE schools
    ADD COLUMN plan_id BIGINT,
    ADD COLUMN custom_price NUMERIC(10, 2),
    ADD COLUMN subscription_start DATE,
    ADD COLUMN renewal_date DATE,
    ADD COLUMN payment_status VARCHAR(50) DEFAULT 'PAID',
    ADD COLUMN onboarding_status VARCHAR(50) DEFAULT 'LIVE';

-- Link existing schools to the basic plan safely
-- We will link to the first active plan if it exists
DO $$
DECLARE
    default_plan_id BIGINT;
BEGIN
    SELECT id INTO default_plan_id FROM subscription_plans ORDER BY id LIMIT 1;
    IF default_plan_id IS NOT NULL THEN
        UPDATE schools SET plan_id = default_plan_id WHERE plan_id IS NULL;
    END IF;
END $$;

-- Add the foreign key constraint
ALTER TABLE schools
    ADD CONSTRAINT fk_schools_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans (id) ON DELETE SET NULL;
