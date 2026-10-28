-- ============================================================================
-- V133__sync_income_module_permissions_and_roles.sql
-- Synchronizes granular Income permissions, module keys, audiences, and role assignments
-- ============================================================================

-- 1. Insert or update granular income permissions in permission_definitions
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission, audience)
VALUES 
    ('perm_income_add_income_view', 'income.add_income.view', 'INCOME', 'add_income', 'view', 'Access Add Income', 'Allows access to the Add Income screen', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_income_search_income_view', 'income.search_income.view', 'INCOME', 'search_income', 'view', 'Access Search Income', 'Allows access to the Search Income screen', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_income_income_head_view', 'income.income_head.view', 'INCOME', 'income_head', 'view', 'Access Income Head', 'Allows access to the Income Head screen', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_income_view', 'income.transaction.view', 'INCOME', 'transaction', 'view', 'View Income', 'Allows viewing school income records', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_income_edit', 'income.transaction.edit', 'INCOME', 'transaction', 'edit', 'Edit Income', 'Allows creating, updating, and deleting income records and heads', '["school"]'::jsonb, true, true, '["staff"]'::jsonb)
ON CONFLICT (id) DO UPDATE SET
    permission_key = EXCLUDED.permission_key,
    module_key = EXCLUDED.module_key,
    resource_key = EXCLUDED.resource_key,
    action_key = EXCLUDED.action_key,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    audience = EXCLUDED.audience;

-- 2. Unify module_key to 'INCOME' and audience to '["staff"]' for any income permissions
UPDATE permission_definitions 
SET module_key = 'INCOME',
    audience = '["staff"]'::jsonb
WHERE id LIKE 'perm_income_%' OR permission_key LIKE 'income.%';

-- 3. Assign all administrative Income permissions to Super Admin and School Admin
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.role_id, pd.id, 'school', '{}'::jsonb
FROM (VALUES ('role_super_admin_global'), ('role_school_admin_global')) AS r(role_id)
CROSS JOIN permission_definitions pd
WHERE pd.module_key = 'INCOME'
ON CONFLICT DO NOTHING;

-- 4. Deduplicate any duplicate role_permissions entries
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.ctid < b.ctid 
  AND COALESCE(a.school_id, 0) = COALESCE(b.school_id, 0) 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id;
