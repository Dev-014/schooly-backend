-- Enable the ACCOUNTING module for all schools so permissions aren't filtered out
UPDATE school_module_access 
SET enabled = true 
WHERE module_id = (SELECT id FROM platform_modules WHERE code = 'ACCOUNTING');
