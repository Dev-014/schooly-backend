-- Remove FRONT_OFFICE module from platform_modules and its entitlements
DELETE FROM school_module_access WHERE module_id IN (SELECT id FROM platform_modules WHERE code = 'FRONT_OFFICE');
DELETE FROM plan_modules WHERE module_id IN (SELECT id FROM platform_modules WHERE code = 'FRONT_OFFICE');
DELETE FROM platform_modules WHERE code = 'FRONT_OFFICE';

-- Remove FRONT_OFFICE permissions from permission_definitions and role_permissions
DELETE FROM role_permissions WHERE permission_id IN (SELECT id FROM permission_definitions WHERE module_key = 'FRONT_OFFICE');
DELETE FROM permission_definitions WHERE module_key = 'FRONT_OFFICE';
