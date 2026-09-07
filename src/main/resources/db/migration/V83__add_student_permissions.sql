-- Add permission definition for student fees
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, requires_assignment, is_system_permission, is_active)
VALUES (
    'perm_fees_student_fees_view',
    'fees.student_fees.view',
    'FEES',
    'student_fees',
    'view',
    'Access Student Fees',
    'Allows a student to view their own fees',
    '["GLOBAL"]',
    false,
    true,
    true
)
ON CONFLICT (permission_key) DO NOTHING;

-- Grant permissions to the STUDENT role
-- 1. fees.student_fees.view
-- 2. attendance.attendance_my_records.view

DO $$
DECLARE
    v_role_id VARCHAR := 'role_student_global';
    perm_student_fees VARCHAR := 'perm_fees_student_fees_view';
    perm_my_attendance VARCHAR := 'perm_attendance_attendance_my_records_view';
BEGIN
    IF NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = v_role_id AND permission_id = perm_student_fees AND scope_type = 'GLOBAL') THEN
        INSERT INTO role_permissions (role_id, permission_id, scope_type) VALUES (v_role_id, perm_student_fees, 'GLOBAL');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = v_role_id AND permission_id = perm_my_attendance AND scope_type = 'GLOBAL') THEN
        INSERT INTO role_permissions (role_id, permission_id, scope_type) VALUES (v_role_id, perm_my_attendance, 'GLOBAL');
    END IF;
END $$;
