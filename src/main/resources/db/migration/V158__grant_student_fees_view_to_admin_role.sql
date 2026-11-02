-- ============================================================================
-- V158__grant_student_fees_view_to_admin_role.sql
-- Grants fees.student_fees.view to School Admin role to allow admin/staff
-- to view student fee dues, payments, and summaries.
-- ============================================================================

-- 1. Ensure permission definition supports school scope and staff audience
UPDATE permission_definitions
SET supported_scope_types = '["own", "linked", "school"]'::jsonb,
    audience = '["school", "staff", "student", "parent"]'::jsonb
WHERE id = 'perm_fees_student_fees_view' OR permission_key = 'fees.student_fees.view';

-- 2. Grant perm_fees_student_fees_view to role_school_admin_global with school scope
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_school_admin_global', 'perm_fees_student_fees_view', 'school', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_school_admin_global'
      AND rp.permission_id = 'perm_fees_student_fees_view'
      AND (rp.school_id IS NULL OR rp.school_id = 0)
);

UPDATE role_permissions
SET scope_type = 'school'
WHERE role_id = 'role_school_admin_global'
  AND permission_id = 'perm_fees_student_fees_view';

-- 3. Grant perm_fees_student_fees_view to role_super_admin_global with school scope
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_super_admin_global', 'perm_fees_student_fees_view', 'school', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_super_admin_global'
      AND rp.permission_id = 'perm_fees_student_fees_view'
      AND (rp.school_id IS NULL OR rp.school_id = 0)
);

UPDATE role_permissions
SET scope_type = 'school'
WHERE role_id = 'role_super_admin_global'
  AND permission_id = 'perm_fees_student_fees_view';

-- 4. Deduplicate any duplicate role_permissions
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.id > b.id 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id 
  AND (a.school_id = b.school_id OR (a.school_id IS NULL AND b.school_id IS NULL));

