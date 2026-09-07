-- V90__seed_global_system_roles_permissions.sql

-- 1. Clear out all existing permissions for the global system roles to start fresh
DELETE FROM role_permissions 
WHERE role_id IN (
    'role_super_admin_global', 
    'role_school_admin_global', 
    'role_teacher_global', 
    'role_student_global', 
    'role_parent_global'
);

-- 2. Super Admin gets EVERYTHING unconditionally
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_super_admin_global', id, 'school', '{}'::jsonb
FROM permission_definitions;

-- 3. School Admin gets almost everything, except maybe system-level things if any exist (we'll just give all for now)
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_school_admin_global', id, 'school', '{}'::jsonb
FROM permission_definitions;

-- 4. Teacher Permissions
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_teacher_global', id, 'school', '{}'::jsonb
FROM permission_definitions
WHERE permission_key IN (
    'attendance.attendance_record.view',
    'attendance.attendance_record.edit',
    'student.student.view',
    'academic.timetable_period.view',
    'academic.class_setup.view',
    'homework.assignment.view',
    'online_class.session.view',
    'lesson_module.plan.view',
    'downloads.material.view',
    'exams.marks.view',
    'exams.marks.edit',
    'exams_results.marks_entry.view',
    'exams_results.report_card.view',
    'exams_results.exam_schedule.view',
    'fees.search_due_fees.view',
    'academics.teacher_timetable.view',
    'academics.class_timetable.view',
    'academic.assignment.edit'
);

-- 5. Student Permissions
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_student_global', id, 'school', '{}'::jsonb
FROM permission_definitions
WHERE permission_key IN (
    'attendance.attendance_record.view',
    'student.student.view',
    'academic.timetable_period.view',
    'academic.class_setup.view',
    'homework.assignment.view',
    'online_class.session.view',
    'online_exams.exam.view',
    'lesson_module.plan.view',
    'downloads.material.view',
    'library.book.view',
    'exams.marks.view',
    'exams_results.report_card.view',
    'exams_results.exam_schedule_student.view',
    'dashboard.student_reports.view',
    'attendance.attendance_my_records.view',
    'academics.class_timetable.view',
    'academics.daily_class_timetable.view'
);

-- 6. Parent Permissions
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_parent_global', id, 'school', '{}'::jsonb
FROM permission_definitions
WHERE permission_key IN (
    'attendance.attendance_record.view',
    'student.student.view',
    'academic.timetable_period.view',
    'academic.class_setup.view',
    'homework.assignment.view',
    'fees.fee_invoice.view',
    'communication.message.view',
    'family.family_profile.view',
    'online_exams.exam.view',
    'exams.marks.view',
    'exams_results.report_card.view',
    'exams_results.exam_schedule_student.view',
    'dashboard.student_reports.view',
    'fees.search_due_fees.view',
    'fees.payment_receipts.view',
    'attendance.attendance_my_records.view',
    'academics.class_timetable.view'
);
