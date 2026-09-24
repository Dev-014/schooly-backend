-- Fix Income & Expense Permissions Module Key By ID
UPDATE permission_definitions 
SET module_key = 'ACCOUNTING' 
WHERE id IN (
    'perm_income_view',
    'perm_income_edit',
    'perm_expense_view',
    'perm_expense_edit',
    'perm_income_add_income_view',
    'perm_income_search_income_view',
    'perm_income_income_head_view',
    'perm_expense_add_expense_view',
    'perm_expense_search_expense_view',
    'perm_expense_expense_head_view'
);
