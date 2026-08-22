-- V62__refactor_to_global_system_roles.sql

-- 1. Alter schema to allow global roles and global permissions
ALTER TABLE roles ALTER COLUMN school_id DROP NOT NULL;
ALTER TABLE role_permissions ALTER COLUMN school_id DROP NOT NULL;

-- 2. Create the True Global System Roles
INSERT INTO roles (id, name, description, is_system_role, archetype)
VALUES 
('role_super_admin_global', 'Super Admin', 'Global platform administrator', true, 'SUPER_ADMIN'),
('role_school_admin_global', 'School Admin', 'School administrator', true, 'SCHOOL_ADMIN'),
('role_teacher_global', 'Teacher', 'Faculty members with classroom access', true, 'STAFF'),
('role_student_global', 'Student', 'Student access role', true, 'STUDENT'),
('role_parent_global', 'Parent', 'Parent access role', true, 'PARENT');

-- 3. Remap users from duplicate system roles to global system roles
UPDATE user_role_mappings urm
SET role_id = 'role_school_admin_global'
FROM roles r
WHERE urm.role_id = r.id AND r.name = 'School Admin' AND r.is_system_role = true;

UPDATE user_role_mappings urm
SET role_id = 'role_teacher_global'
FROM roles r
WHERE urm.role_id = r.id AND r.name = 'Teacher' AND r.is_system_role = true;

UPDATE user_role_mappings urm
SET role_id = 'role_student_global'
FROM roles r
WHERE urm.role_id = r.id AND r.name = 'Student' AND r.is_system_role = true;

UPDATE user_role_mappings urm
SET role_id = 'role_parent_global'
FROM roles r
WHERE urm.role_id = r.id AND r.name = 'Parent' AND r.is_system_role = true;

-- 4. Remap role_permissions from redundant roles to global roles
DELETE FROM role_permissions
WHERE role_id IN (SELECT id FROM roles WHERE is_system_role = true AND school_id IS NOT NULL);

-- Seed new global permissions
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_teacher_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key IN (
    'attendance.attendance_register.view',
    'attendance.attendance_reports.view',
    'academics.teacher_timetable.view',
    'academics.class_timetable.view',
    'homework.homework.view',
    'homework.hw_report.view',
    'exams_results.marks_entry.view',
    'exams_results.teacher_remark.view',
    'downloads.study_material.view',
    'downloads.upload_content.view',
    'online_class.teacher_timetable.view',
    'online_class.online_class_management.view',
    'lesson_module.manage_planner.view',
    'lesson_module.lesson.view'
);

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_student_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key IN (
    'dashboard.student_reports.view',
    'attendance.attendance_my_records.view',
    'academics.class_timetable.view',
    'academics.daily_class_timetable.view',
    'homework.homework.view',
    'exams_results.report_card.view',
    'exams_results.exam_schedule_student.view',
    'downloads.study_material.view',
    'downloads.syllabus_download.view',
    'online_class.online_class_management.view',
    'lesson_module.lesson.view',
    'library.book_list.view'
);

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_parent_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key IN (
    'dashboard.student_reports.view',
    'attendance.attendance_my_records.view',
    'academics.class_timetable.view',
    'homework.homework.view',
    'exams_results.report_card.view',
    'exams_results.exam_schedule_student.view',
    'fees.search_due_fees.view',
    'fees.payment_receipts.view',
    'communication.message_history.view'
);

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_school_admin_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key IN (
    'attendance.attendance_register.view',
    'attendance.attendance_reports.view',
    'students.student_directory.view',
    'fees.search_due_fees.view',
    'fees.payment_receipts.view'
);

-- 5. Delete all duplicate system roles
DELETE FROM roles
WHERE is_system_role = true AND school_id IS NOT NULL;
