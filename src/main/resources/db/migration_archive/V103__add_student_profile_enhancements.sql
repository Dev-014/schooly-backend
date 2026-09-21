-- V103__add_student_profile_enhancements.sql
-- Add fields to student table to support complete student profile views and self-service editing

ALTER TABLE student ADD COLUMN IF NOT EXISTS phone VARCHAR(30);
ALTER TABLE student ADD COLUMN IF NOT EXISTS email VARCHAR(100);
ALTER TABLE student ADD COLUMN IF NOT EXISTS caste VARCHAR(100);
ALTER TABLE student ADD COLUMN IF NOT EXISTS permanent_address TEXT;

ALTER TABLE student ADD COLUMN IF NOT EXISTS father_name VARCHAR(100);
ALTER TABLE student ADD COLUMN IF NOT EXISTS father_phone VARCHAR(30);
ALTER TABLE student ADD COLUMN IF NOT EXISTS father_email VARCHAR(100);
ALTER TABLE student ADD COLUMN IF NOT EXISTS father_occupation VARCHAR(100);

ALTER TABLE student ADD COLUMN IF NOT EXISTS mother_name VARCHAR(100);
ALTER TABLE student ADD COLUMN IF NOT EXISTS mother_phone VARCHAR(30);
ALTER TABLE student ADD COLUMN IF NOT EXISTS mother_email VARCHAR(100);
ALTER TABLE student ADD COLUMN IF NOT EXISTS mother_occupation VARCHAR(100);

ALTER TABLE student ADD COLUMN IF NOT EXISTS bank_name VARCHAR(100);
ALTER TABLE student ADD COLUMN IF NOT EXISTS bank_account_no VARCHAR(50);
ALTER TABLE student ADD COLUMN IF NOT EXISTS bank_ifsc VARCHAR(50);

ALTER TABLE student ADD COLUMN IF NOT EXISTS student_house_name VARCHAR(100);
ALTER TABLE student ADD COLUMN IF NOT EXISTS academic_merit TEXT;

-- Add permission definitions for student profile view and edit
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, requires_assignment, is_system_permission, is_active)
VALUES (
    'perm_students_student_profile_view',
    'students.student_profile.view',
    'STUDENTS',
    'student_profile',
    'view',
    'Access Student Profile',
    'Allows viewing student profile details',
    '["GLOBAL", "BRANCH"]',
    false,
    true,
    true
),
(
    'perm_students_student_profile_edit',
    'students.student_profile.edit',
    'STUDENTS',
    'student_profile',
    'edit',
    'Edit Student Profile',
    'Allows updating student profile details',
    '["GLOBAL", "BRANCH"]',
    false,
    true,
    true
)
ON CONFLICT (permission_key) DO NOTHING;

-- Grant permissions to roles
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_super_admin_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.id IN ('perm_students_student_profile_view', 'perm_students_student_profile_edit')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_school_admin_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.id IN ('perm_students_student_profile_view', 'perm_students_student_profile_edit')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_student_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.id IN ('perm_students_student_profile_view', 'perm_students_student_profile_edit')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_parent_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.id = 'perm_students_student_profile_view'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_teacher_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.id = 'perm_students_student_profile_view'
ON CONFLICT DO NOTHING;
