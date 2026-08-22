-- Seed Default Roles for School 1
INSERT INTO roles (id, school_id, name, description, is_system_role)
VALUES
('role_school_admin', 1, 'School Admin', 'Full administrative oversight of school operations and setup', true),
('role_teacher', 1, 'Teacher', 'Faculty members with classroom access', true),
('role_accountant', 1, 'Accountant', 'Finance and fee management', true);

-- Grant some basic permissions to School Admin
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type)
VALUES
(1, 'role_school_admin', 'perm_attendance_view', 'SCHOOL'),
(1, 'role_school_admin', 'perm_attendance_edit', 'SCHOOL'),
(1, 'role_school_admin', 'perm_student_view', 'SCHOOL'),
(1, 'role_school_admin', 'perm_student_edit', 'SCHOOL'),
(1, 'role_school_admin', 'perm_fee_view', 'SCHOOL'),
(1, 'role_school_admin', 'perm_fee_refund', 'SCHOOL');

-- Grant permissions to Teacher
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type)
VALUES
(1, 'role_teacher', 'perm_attendance_view', 'CLASS'),
(1, 'role_teacher', 'perm_attendance_edit', 'CLASS'),
(1, 'role_teacher', 'perm_student_view', 'CLASS');

-- Map user 1 (the superadmin/demo user) to School Admin role
INSERT INTO user_role_mappings (school_id, user_id, role_id, is_active)
VALUES
(1, 1, 'role_school_admin', true);
