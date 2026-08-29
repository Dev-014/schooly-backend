SELECT p.id, p.code, pm.module_key 
FROM subscription_plans p 
JOIN plan_modules pm ON p.id = pm.plan_id 
WHERE p.code = 'GROWTH';
