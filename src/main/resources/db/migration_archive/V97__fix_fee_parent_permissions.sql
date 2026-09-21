-- V97__fix_fee_parent_permissions.sql
--
-- Problem: V90 granted fees.fee_invoice.view to Parent role. Because every Fees
-- sidebar route used this generic key, Parent could see: Collection Plans,
-- Search Due Fees, Demand Notices, Payment Receipts, My Class Fees, Reminder History.
-- Parents should only see: My Fees (student_fees), Payment Receipts, and Search Due Fees
-- (to look up their child's dues). Collection Plans, Demand Notices, My Class Fees,
-- Reminder History and fee-master / collect-fees are admin-only screens.
--
-- Fix:
-- 1. Remove the generic fees.fee_invoice.view from Parent (it leaked access to everything).
-- 2. Ensure Parent retains only the granular screen permissions they need.
-- 3. Fix Student role to have fees.student_fees.view (not fees.student_fees.view already there,
--    but ensure it's present).

-- 1. Remove over-broad fee_invoice generic permission from Parent
DELETE FROM role_permissions
WHERE role_id = 'role_parent_global'
  AND permission_id IN (
    SELECT id FROM permission_definitions
    WHERE permission_key = 'fees.fee_invoice.view'
);

-- 2. Ensure Parent has only the granular screen-level fee permissions they need
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_parent_global', id, 'school', '{}'::jsonb
FROM permission_definitions
WHERE permission_key IN (
    'fees.student_fees.view',       -- My Fees (their child's fees)
    'fees.payment_receipts.view',   -- Payment Receipts
    'fees.search_due_fees.view'     -- Search Due Fees (to check dues)
)
ON CONFLICT DO NOTHING;

-- 3. Remove generic fee_invoice.view from Student (not in V90, but in V83/other migrations)
DELETE FROM role_permissions
WHERE role_id = 'role_student_global'
  AND permission_id IN (
    SELECT id FROM permission_definitions
    WHERE permission_key = 'fees.fee_invoice.view'
);

-- 4. Ensure Student has fees.student_fees.view (their personal fee page)
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_student_global', id, 'school', '{}'::jsonb
FROM permission_definitions
WHERE permission_key = 'fees.student_fees.view'
ON CONFLICT DO NOTHING;
