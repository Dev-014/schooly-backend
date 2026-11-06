-- ============================================================================
-- V170__fix_fee_permissions_audience.sql
-- Corrects the audience configuration and scope types for fee permissions.
-- 1. Student & Child Fees is strictly for students and parents ('own', 'linked').
-- 2. Fee Master, Collect Fees, Due Fees, etc. are for School Admin ('school').
-- ============================================================================

-- 1. Fix Student & Child Fees audience and supported scopes
UPDATE permission_definitions
SET audience = '["student", "parent"]'::jsonb,
    supported_scope_types = '["own", "linked"]'::jsonb
WHERE id = 'perm_fees_student_fees_view' OR permission_key = 'fees.student_fees.view';

-- Revoke student_fees view from school admin and super admin global roles
DELETE FROM role_permissions
WHERE permission_id = 'perm_fees_student_fees_view'
  AND role_id IN ('role_school_admin_global', 'role_super_admin_global');

-- 2. Fix Fee Master audience to school (Admin)
UPDATE permission_definitions
SET audience = '["school"]'::jsonb
WHERE id = 'perm_fees_fee_master_view' OR permission_key = 'fees.fee_master.view';

-- 3. Fix Fee Admin operational permissions audience to school (Admin)
UPDATE permission_definitions
SET audience = '["school"]'::jsonb
WHERE permission_key IN (
    'fees.collect_fees.view',
    'fees.collection_plans.view',
    'fees.demand_notices.view',
    'fees.fee_installments.view',
    'fees.payment_receipts.view',
    'fees.reminder_history.view',
    'fees.search_due_fees.view',
    'fees.fee_invoice.edit',
    'fees.fee_collection.refund'
);

