-- ============================================================================
-- V134__sync_expense_module_permissions_and_roles.sql
-- Synchronizes granular Expense permissions, module keys, audiences, and role assignments
-- ============================================================================

-- 1. Insert or update granular expense permissions in permission_definitions
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission, audience)
VALUES 
    ('perm_expense_add_expense_view', 'expense.add_expense.view', 'EXPENSE', 'add_expense', 'view', 'Access Add Expense', 'Allows access to the Add Expense screen', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_expense_search_expense_view', 'expense.search_expense.view', 'EXPENSE', 'search_expense', 'view', 'Access Search Expense', 'Allows access to the Search Expense screen', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_expense_expense_head_view', 'expense.expense_head.view', 'EXPENSE', 'expense_head', 'view', 'Access Expense Head', 'Allows access to the Expense Head screen', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_expense_view', 'expense.transaction.view', 'EXPENSE', 'transaction', 'view', 'View Expenses', 'Allows viewing school expenses', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_expense_edit', 'expense.transaction.edit', 'EXPENSE', 'transaction', 'edit', 'Edit Expenses', 'Allows creating, updating, and deleting expense records and heads', '["school"]'::jsonb, true, true, '["staff"]'::jsonb)
ON CONFLICT (id) DO UPDATE SET
    permission_key = EXCLUDED.permission_key,
    module_key = EXCLUDED.module_key,
    resource_key = EXCLUDED.resource_key,
    action_key = EXCLUDED.action_key,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    audience = EXCLUDED.audience;

-- 2. Unify module_key to 'EXPENSE' and audience to '["staff"]' for any expense permissions
UPDATE permission_definitions 
SET module_key = 'EXPENSE',
    audience = '["staff"]'::jsonb
WHERE id LIKE 'perm_expense_%' OR permission_key LIKE 'expense.%';

-- 3. Assign all administrative Expense permissions to Super Admin and School Admin
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.role_id, pd.id, 'school', '{}'::jsonb
FROM (VALUES ('role_super_admin_global'), ('role_school_admin_global')) AS r(role_id)
CROSS JOIN permission_definitions pd
WHERE pd.module_key = 'EXPENSE'
ON CONFLICT DO NOTHING;

-- 4. Deduplicate any duplicate role_permissions entries
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.ctid < b.ctid 
  AND COALESCE(a.school_id, 0) = COALESCE(b.school_id, 0) 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id;
