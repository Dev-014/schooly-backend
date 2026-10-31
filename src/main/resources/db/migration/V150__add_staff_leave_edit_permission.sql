-- ============================================================================
-- Migration: V150__add_staff_leave_edit_permission.sql
-- Description: Add granular leave edit/approval permissions for staff leaves
--              and student leaves to permission_definitions and assign defaults.
--              - leave_management.staff_leaves.edit: Granted to School Admin & Super Admin.
--                Can be granted to custom HR/Leadership roles.
--              - leave_management.student_leaves.edit: Granted to School Admin, Super Admin,
--                and Teachers (for student approvals).
-- ============================================================================

INSERT INTO permission_definitions (
    id, permission_key, module_key, resource_key, action_key, name, description,
    audience, requires_assignment, is_sensitive, is_system_permission, is_active, created_at, supported_scope_types
)
VALUES 
(
    'perm_leave_staff_edit',
    'leave_management.staff_leaves.edit',
    'LEAVE_MANAGEMENT',
    'staff_leaves',
    'edit',
    'Approve & Manage Staff Leaves',
    'Allows approving, rejecting, and managing staff leave requests',
    '["school", "staff"]'::jsonb,
    FALSE,
    FALSE,
    FALSE,
    TRUE,
    CURRENT_TIMESTAMP,
    '["school", "assigned"]'::jsonb
),
(
    'perm_leave_student_edit',
    'leave_management.student_leaves.edit',
    'LEAVE_MANAGEMENT',
    'student_leaves',
    'edit',
    'Approve & Manage Student Leaves',
    'Allows approving, rejecting, and managing student leave requests',
    '["school", "staff"]'::jsonb,
    FALSE,
    FALSE,
    FALSE,
    TRUE,
    CURRENT_TIMESTAMP,
    '["school", "assigned", "class"]'::jsonb
)
ON CONFLICT (id) DO UPDATE SET
    permission_key = EXCLUDED.permission_key,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    supported_scope_types = EXCLUDED.supported_scope_types;

-- Grant staff_leaves.edit to Super Admin and School Admin
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_super_admin_global', 'perm_leave_staff_edit', 'school', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions WHERE role_id = 'role_super_admin_global' AND permission_id = 'perm_leave_staff_edit' AND school_id IS NULL
);

INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_school_admin_global', 'perm_leave_staff_edit', 'school', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions WHERE role_id = 'role_school_admin_global' AND permission_id = 'perm_leave_staff_edit' AND school_id IS NULL
);

-- Grant student_leaves.edit to Super Admin, School Admin, and Teachers
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.role_id, 'perm_leave_student_edit', 'school', '{}'::jsonb
FROM (VALUES ('role_super_admin_global'), ('role_school_admin_global'), ('role_teacher_global')) AS r(role_id)
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions WHERE role_id = r.role_id AND permission_id = 'perm_leave_student_edit' AND school_id IS NULL
);
