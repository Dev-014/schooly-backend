-- Add Income & Expense Permissions
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES
('perm_income_income_head_view', 'perm_income_income_head_view', 'ACADEMICS', 'INCOME_HEAD', 'VIEW', 'View Income Heads', 'View income heads', '["school", "assigned"]'::jsonb, false, true),
('perm_income_view', 'perm_income_view', 'ACADEMICS', 'INCOME', 'VIEW', 'View Income', 'View incomes', '["school", "assigned"]'::jsonb, false, true),
('perm_income_edit', 'perm_income_edit', 'ACADEMICS', 'INCOME', 'EDIT', 'Edit Income', 'Edit incomes', '["school", "assigned"]'::jsonb, true, true),
('perm_expense_expense_head_view', 'perm_expense_expense_head_view', 'ACADEMICS', 'EXPENSE_HEAD', 'VIEW', 'View Expense Heads', 'View expense heads', '["school", "assigned"]'::jsonb, false, true),
('perm_expense_view', 'perm_expense_view', 'ACADEMICS', 'EXPENSE', 'VIEW', 'View Expense', 'View expenses', '["school", "assigned"]'::jsonb, false, true),
('perm_expense_edit', 'perm_expense_edit', 'ACADEMICS', 'EXPENSE', 'EDIT', 'Edit Expense', 'Edit expenses', '["school", "assigned"]'::jsonb, true, true)
ON CONFLICT (id) DO NOTHING;

-- Grant permissions to Super Admin and School Admin
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT r.id, pd.id, 'school', '{}'::jsonb
FROM roles r
CROSS JOIN permission_definitions pd
WHERE r.archetype IN ('SUPER_ADMIN', 'SCHOOL_ADMIN')
  AND pd.permission_key IN (
    'perm_income_income_head_view',
    'perm_income_view',
    'perm_income_edit',
    'perm_expense_expense_head_view',
    'perm_expense_view',
    'perm_expense_edit'
  )
ON CONFLICT DO NOTHING;
