-- V91__fix_auth_and_permissions.sql
-- Comprehensive fixes for authorization mismatches

-- 1. Fix the accidental mapping of SUPER_ADMIN to role_school_admin_global from V89
UPDATE user_role_mappings 
SET role_id = 'role_super_admin_global' 
WHERE user_id IN (
    SELECT user_id 
    FROM user_school_roles 
    WHERE role = 'SUPER_ADMIN'
);

-- 3. Add missing DASHBOARD module to platform_modules
INSERT INTO platform_modules (code, name, description, is_default)
VALUES ('DASHBOARD', 'Dashboard', 'Dashboard and Analytics', true)
ON CONFLICT (code) DO NOTHING;

-- Assign DASHBOARD to all existing schools
INSERT INTO school_module_access (school_id, module_id, enabled)
SELECT s.id, m.id, true
FROM schools s
CROSS JOIN platform_modules m
WHERE m.code = 'DASHBOARD'
ON CONFLICT DO NOTHING;

-- 4. Empty permissions for role_teacher_global (base staff should have no permissions by default)
DELETE FROM role_permissions WHERE role_id = 'role_teacher_global';

-- 2. Align mismatched permission module_keys with platform_modules table
UPDATE permission_definitions SET module_key = 'ACADEMICS' WHERE module_key = 'ACADEMIC';
UPDATE permission_definitions SET module_key = 'STAFF_HR' WHERE module_key = 'STAFF';
