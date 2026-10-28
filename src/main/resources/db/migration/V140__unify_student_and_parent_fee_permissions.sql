-- ============================================================================
-- V140__unify_student_and_parent_fee_permissions.sql
-- Unifies Student and Parent fee view permissions under 'fees.student_fees.view'
-- ============================================================================

-- 1. Update permission definition name, description, scopes, and audience
UPDATE permission_definitions
SET name = 'Student & Child Fees',
    description = 'View fee dues, invoices, and payment receipts for self (students) or linked children (parents).',
    supported_scope_types = '["own", "linked", "school"]'::jsonb,
    audience = '["student", "parent", "staff"]'::jsonb
WHERE id = 'perm_fees_student_fees_view' OR permission_key = 'fees.student_fees.view';

-- 2. Ensure role_parent_global has access to perm_fees_student_fees_view with 'linked' scope
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_parent_global', 'perm_fees_student_fees_view', 'linked', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_parent_global'
      AND rp.permission_id = 'perm_fees_student_fees_view'
      AND (rp.school_id IS NULL OR rp.school_id = 0)
);

UPDATE role_permissions
SET scope_type = 'linked'
WHERE role_id = 'role_parent_global'
  AND permission_id = 'perm_fees_student_fees_view';

-- 3. Ensure role_student_global has access with 'own' scope
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_student_global', 'perm_fees_student_fees_view', 'own', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_student_global'
      AND rp.permission_id = 'perm_fees_student_fees_view'
      AND (rp.school_id IS NULL OR rp.school_id = 0)
);

-- 4. Deduplicate any duplicate role_permissions entries
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.id > b.id 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id 
  AND (a.school_id = b.school_id OR (a.school_id IS NULL AND b.school_id IS NULL));
