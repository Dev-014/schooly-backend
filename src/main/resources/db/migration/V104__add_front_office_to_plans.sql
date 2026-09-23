-- V104__add_front_office_to_plans.sql
-- Add FRONT_OFFICE module to all standard subscription plans so schools have entitlement access to front office APIs

INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id 
FROM subscription_plans p, platform_modules m
WHERE p.code IN ('FREE', 'GROWTH', 'PREMIUM', 'ENTERPRISE') 
  AND m.code = 'FRONT_OFFICE'
ON CONFLICT DO NOTHING;
