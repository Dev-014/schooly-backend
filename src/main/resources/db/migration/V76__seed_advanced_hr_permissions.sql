-- V76__seed_advanced_hr_permissions.sql

-- 1. Insert advanced HR permissions into permission_definitions
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, is_system_permission, is_active)
VALUES 
('perm_staff_hr_departments_view', 'staff_hr.departments.view', 'STAFF_HR', 'departments', 'view', 'View Departments', 'View and manage HR departments', true, true),
('perm_staff_hr_designations_view', 'staff_hr.designations.view', 'STAFF_HR', 'designations', 'view', 'View Designations', 'View and manage HR designations', true, true),
('perm_staff_hr_recruitment_view', 'staff_hr.recruitment.view', 'STAFF_HR', 'recruitment', 'view', 'View Recruitment', 'View and manage Recruitment candidates', true, true),
('perm_staff_hr_attendance_view', 'staff_hr.staff_attendance.view', 'STAFF_HR', 'staff_attendance', 'view', 'View Staff Attendance', 'View and manage Staff Attendance', true, true),
('perm_staff_hr_leaves_view', 'staff_hr.staff_leaves.view', 'STAFF_HR', 'staff_leaves', 'view', 'View Staff Leaves', 'View and manage Staff Leaves', true, true),
('perm_staff_hr_tasks_view', 'staff_hr.staff_tasks.view', 'STAFF_HR', 'staff_tasks', 'view', 'View Staff Tasks', 'View and manage Staff Tasks', true, true)
ON CONFLICT (permission_key) DO NOTHING;

-- 2. Grant these permissions to all roles with archetype = 'ADMIN'
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permission_definitions p
WHERE r.archetype = 'ADMIN'
AND p.permission_key IN (
    'staff_hr.departments.view',
    'staff_hr.designations.view',
    'staff_hr.recruitment.view',
    'staff_hr.staff_attendance.view',
    'staff_hr.staff_leaves.view',
    'staff_hr.staff_tasks.view'
)
ON CONFLICT DO NOTHING;
