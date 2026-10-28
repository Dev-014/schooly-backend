-- ============================================================================
-- V130__sync_fees_module_permissions_and_roles.sql
-- Synchronizes granular Fees permissions, audiences, and role assignments
-- ============================================================================

-- 1. Insert missing fee permissions into permission_definitions
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission, audience)
VALUES 
    ('perm_fees_fee_master_view', 'fees.fee_master.view', 'FEES', 'fee_master', 'view', 'Access Fee Master', 'Configure fee categories, groups, and structures.', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_fees_collection_plans_view', 'fees.collection_plans.view', 'FEES', 'collection_plans', 'view', 'Access Collection Plans', 'Manage fee installment schedules and collection plans.', '["school"]'::jsonb, false, true, '["staff"]'::jsonb)
ON CONFLICT (id) DO NOTHING;

-- 2. Set accurate audience boundaries
UPDATE permission_definitions 
SET audience = '["student", "parent"]'::jsonb
WHERE id = 'perm_fees_student_fees_view';

UPDATE permission_definitions 
SET audience = '["staff"]'::jsonb
WHERE id IN ('perm_fees_my_class_fees_view', 'perm_fees_my_class_fees_send_reminder', 'perm_fees_my_class_fees_view_amount');

-- 3. Assign administrative fee permissions to Super Admin and School Admin
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.role_id, pd.id, 'school', '{}'::jsonb
FROM (VALUES ('role_super_admin_global'), ('role_school_admin_global')) AS r(role_id)
CROSS JOIN permission_definitions pd
WHERE pd.module_key = 'FEES'
  AND pd.id IN (
    'perm_fees_fee_master_view',
    'perm_fees_collect_fees_view',
    'perm_fees_collection_plans_view',
    'perm_fees_search_due_fees_view',
    'perm_fees_demand_notices_view',
    'perm_fees_payment_receipts_view',
    'perm_fees_reminder_history_view',
    'perm_fees_fee_installments_view',
    'perm_fee_view',
    'perm_fee_edit',
    'perm_fee_refund'
  )
ON CONFLICT DO NOTHING;

-- 4. Revoke personal student/teacher fee views from School Admin and Super Admin
DELETE FROM role_permissions
WHERE role_id IN ('role_super_admin_global', 'role_school_admin_global')
  AND permission_id IN ('perm_fees_student_fees_view', 'perm_fees_my_class_fees_view');

-- 5. Ensure Student role has access to their personal fee view
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_student_global', 'perm_fees_student_fees_view', 'own', '{}'::jsonb
ON CONFLICT DO NOTHING;

-- 6. Ensure Teacher role has access to assigned class fee register
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_teacher_global', 'perm_fees_my_class_fees_view', 'class', '{}'::jsonb
ON CONFLICT DO NOTHING;

-- 7. Deduplicate any duplicate role_permissions entries
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.ctid < b.ctid 
  AND COALESCE(a.school_id, 0) = COALESCE(b.school_id, 0) 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id;

