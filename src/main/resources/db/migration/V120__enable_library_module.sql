-- V120__enable_library_module.sql
-- Enable the LIBRARY module for all schools so permissions are not filtered out by EntitlementService

UPDATE school_module_access 
SET enabled = true 
WHERE module_id = (SELECT id FROM platform_modules WHERE code = 'LIBRARY');
