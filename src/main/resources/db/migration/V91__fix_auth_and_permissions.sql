-- V91__fix_auth_and_permissions.sql

-- 1. Align legacy module_key values in permission_definitions with actual platform_modules
UPDATE permission_definitions SET module_key = 'ACADEMICS' WHERE module_key = 'ACADEMIC';
UPDATE permission_definitions SET module_key = 'STAFF_HR' WHERE module_key = 'STAFF';

-- 2. Ensure DASHBOARD module exists in platform_modules
INSERT INTO platform_modules (code, name, is_default, status, category)
SELECT 'DASHBOARD', 'Dashboard', true, 'ACTIVE', 'CORE'
WHERE NOT EXISTS (SELECT 1 FROM platform_modules WHERE code = 'DASHBOARD');

-- 3. Assign DASHBOARD module to all existing schools in school_module_access
INSERT INTO school_module_access (school_id, module_id, enabled, enabled_at)
SELECT s.id, pm.id, true, CURRENT_TIMESTAMP
FROM schools s
CROSS JOIN platform_modules pm
WHERE pm.code = 'DASHBOARD'
  AND NOT EXISTS (
      SELECT 1 FROM school_module_access sma 
      WHERE sma.school_id = s.id AND sma.module_id = pm.id
  );


-- 4. Clean up legacy role inheritance: If a user has a custom role (UUID format, not starting with 'role_'),
-- they should not also have the generic 'role_teacher_global' or legacy 'TEACHER' mapping overriding their custom permissions.
DELETE FROM user_role_mappings 
WHERE role_id = 'role_teacher_global' 
AND user_id IN (
    SELECT m.user_id 
    FROM user_role_mappings m 
    WHERE m.role_id NOT LIKE 'role_%'
);

DELETE FROM user_school_roles 
WHERE role = 'TEACHER' 
AND user_id IN (
    SELECT m.user_id 
    FROM user_role_mappings m 
    WHERE m.role_id NOT LIKE 'role_%'
);
