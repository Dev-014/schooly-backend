-- V84__seed_school_module_access.sql
-- Ensure that school_module_access is populated based on the school's active subscription plan

INSERT INTO school_module_access (school_id, module_id, enabled)
SELECT DISTINCT ss.school_id, pm.module_id, true
FROM school_subscriptions ss
JOIN plan_modules pm ON pm.plan_id = ss.plan_id
WHERE ss.status = 'ACTIVE'
AND NOT EXISTS (
    SELECT 1 FROM school_module_access sma 
    WHERE sma.school_id = ss.school_id AND sma.module_id = pm.module_id
);
