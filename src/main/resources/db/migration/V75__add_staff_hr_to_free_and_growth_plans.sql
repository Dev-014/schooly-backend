-- V75__add_staff_hr_to_free_and_growth_plans.sql

-- Add STAFF_HR to FREE plan
INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM subscription_plans p, platform_modules m
WHERE p.code = 'FREE' AND m.code = 'STAFF_HR'
ON CONFLICT DO NOTHING;

-- Add STAFF_HR to GROWTH plan
INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM subscription_plans p, platform_modules m
WHERE p.code = 'GROWTH' AND m.code = 'STAFF_HR'
ON CONFLICT DO NOTHING;
