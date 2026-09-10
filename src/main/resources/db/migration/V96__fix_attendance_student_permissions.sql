-- V96__fix_attendance_student_permissions.sql
--
-- Problem: V90 granted attendance.attendance_record.view (and .edit) to Student role.
-- These are generic catalog-level data-access keys. Because all Attendance sidebar
-- routes were using this generic key as their requiredPermission, Students could see
-- Take Register, Analytics, Student Attendance, Student Leave, and Attendance Reports.
--
-- Fix:
-- 1. Remove generic attendance.attendance_record.view and .edit from Student & Parent.
-- 2. Ensure Student has only attendance.attendance_my_records.view (their personal view).
-- 3. Remove generic attendance.attendance_record.view from Parent too (they shouldn't
--    be able to take attendance).

-- 1. Remove over-broad generic attendance permissions from Student role
DELETE FROM role_permissions
WHERE role_id = 'role_student_global'
  AND permission_id IN (
    SELECT id FROM permission_definitions
    WHERE permission_key IN (
        'attendance.attendance_record.view',
        'attendance.attendance_record.edit'
    )
);

-- 2. Remove over-broad generic attendance permissions from Parent role
DELETE FROM role_permissions
WHERE role_id = 'role_parent_global'
  AND permission_id IN (
    SELECT id FROM permission_definitions
    WHERE permission_key IN (
        'attendance.attendance_record.view',
        'attendance.attendance_record.edit'
    )
);

-- 3. Ensure Student has the granular "My Attendance" screen-level permission
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_student_global', id, 'school', '{}'::jsonb
FROM permission_definitions
WHERE permission_key = 'attendance.attendance_my_records.view'
ON CONFLICT DO NOTHING;

-- 4. Ensure Teacher still has take-register capability via the granular key
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_teacher_global', id, 'school', '{}'::jsonb
FROM permission_definitions
WHERE permission_key IN (
    'attendance.attendance_register.view',
    'attendance.student_attendance.view',
    'attendance.attendance_analytics.view'
)
ON CONFLICT DO NOTHING;
