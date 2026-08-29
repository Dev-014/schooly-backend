SELECT sm.school_id, pm.module_key, sm.is_active 
FROM school_modules sm 
JOIN platform_modules pm ON sm.module_id = pm.id 
WHERE sm.school_id = 4 AND pm.module_key = 'STUDENT_INFO';
