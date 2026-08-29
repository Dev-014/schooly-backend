SELECT p.id, p.code, pm.module_id, pl.module_key 
FROM subscription_plans p 
JOIN plan_modules pm ON p.id = pm.plan_id 
JOIN platform_modules pl ON pm.module_id = pl.id
WHERE p.code = 'GROWTH';
