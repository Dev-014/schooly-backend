-- ============================================================================
-- Migration: V145__scope_search_due_fees_for_teachers.sql
-- Description: Enable 'class' and 'school' scopes on fees.search_due_fees.view
--              and set default scope to 'class' for teacher roles.
-- ============================================================================

-- 1. Update supported scope types on permission definition to support both class and school
UPDATE permission_definitions
SET supported_scope_types = '["class", "school"]'::jsonb
WHERE permission_key = 'fees.search_due_fees.view';

-- 2. Update role_permissions for global teacher role to scope_type = 'class'
UPDATE role_permissions
SET scope_type = 'class'
WHERE role_id = 'role_teacher_global'
  AND permission_id IN (
      SELECT id FROM permission_definitions WHERE permission_key = 'fees.search_due_fees.view'
  );

-- 3. Also update any school-specific teacher roles to scope_type = 'class' if currently 'school'
UPDATE role_permissions rp
SET scope_type = 'class'
FROM roles r, permission_definitions pd
WHERE rp.role_id = r.id
  AND rp.permission_id = pd.id
  AND pd.permission_key = 'fees.search_due_fees.view'
  AND (r.archetype = 'STAFF' OR LOWER(r.name) LIKE '%teacher%')
  AND r.id != 'role_school_admin_global'
  AND r.id != 'role_super_admin_global';
