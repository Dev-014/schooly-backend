-- V61__grant_granular_permissions_to_system_roles.sql
-- Grants the new granular permissions to the default system roles (Teacher, Student, Parent)

-- 1. Ensure 'Student' and 'Parent' roles exist for all schools
INSERT INTO roles (id, school_id, name, description, is_system_role, archetype)
SELECT 
    'role_student_' || s.id, s.id, 'Student', 'Student access role', true, 'STUDENT'
FROM schools s
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE school_id = s.id AND name = 'Student');

INSERT INTO roles (id, school_id, name, description, is_system_role, archetype)
SELECT 
    'role_parent_' || s.id, s.id, 'Parent', 'Parent access role', true, 'PARENT'
FROM schools s
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE school_id = s.id AND name = 'Parent');

INSERT INTO roles (id, school_id, name, description, is_system_role, archetype)
SELECT 
    'role_teacher_' || s.id, s.id, 'Teacher', 'Teacher access role', true, 'STAFF'
FROM schools s
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE school_id = s.id AND name = 'Teacher');

-- 2. Migrate existing users from `user_school_roles` enum table to the new RBAC `user_role_mappings`
-- For Teachers
INSERT INTO user_role_mappings (school_id, user_id, role_id, is_active)
SELECT usr.school_id, usr.user_id, r.id, true
FROM user_school_roles usr
JOIN roles r ON r.school_id = usr.school_id AND r.name = 'Teacher'
WHERE usr.role = 'TEACHER'
ON CONFLICT DO NOTHING;

-- For Students
INSERT INTO user_role_mappings (school_id, user_id, role_id, is_active)
SELECT usr.school_id, usr.user_id, r.id, true
FROM user_school_roles usr
JOIN roles r ON r.school_id = usr.school_id AND r.name = 'Student'
WHERE usr.role = 'STUDENT'
ON CONFLICT DO NOTHING;

-- For Parents
INSERT INTO user_role_mappings (school_id, user_id, role_id, is_active)
SELECT usr.school_id, usr.user_id, r.id, true
FROM user_school_roles usr
JOIN roles r ON r.school_id = usr.school_id AND r.name = 'Parent'
WHERE usr.role = 'PARENT'
ON CONFLICT DO NOTHING;

-- 3. Clear out all old/legacy permissions for these roles to start fresh
DELETE FROM role_permissions 
WHERE role_id IN (SELECT id FROM roles WHERE name IN ('Teacher', 'Student', 'Parent'));

-- 4. Insert new granular permissions for TEACHER
INSERT INTO role_permissions (role_id, permission_id, school_id, scope_type, scope_config)
SELECT r.id, pd.id, r.school_id, 'school', '{}'::jsonb
FROM roles r
CROSS JOIN permission_definitions pd
WHERE r.name = 'Teacher'
AND pd.permission_key IN (
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

-- 5. Insert new granular permissions for STUDENT
INSERT INTO role_permissions (role_id, permission_id, school_id, scope_type, scope_config)
SELECT r.id, pd.id, r.school_id, 'school', '{}'::jsonb
FROM roles r
CROSS JOIN permission_definitions pd
WHERE r.name = 'Student'
AND pd.permission_key IN (
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

-- 6. Insert new granular permissions for PARENT
INSERT INTO role_permissions (role_id, permission_id, school_id, scope_type, scope_config)
SELECT r.id, pd.id, r.school_id, 'school', '{}'::jsonb
FROM roles r
CROSS JOIN permission_definitions pd
WHERE r.name = 'Parent'
AND pd.permission_key IN (
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
