-- V95__fix_academic_student_permissions.sql
--
-- Problem: V90 granted academic.class_setup.view and academic.timetable_period.view 
-- to Student and Parent roles. These are generic catalog-level keys that the 
-- DynamicSidebar routes now use only for admin-level screens.
--
-- Fix: Remove these broad generic keys from Student/Parent.
-- Grant only the specific screen-level keys they legitimately need.
-- (Class Timetable and Daily Class Timetable for Students)

-- 1. Remove over-broad generic academic permissions from Student role
DELETE FROM role_permissions
WHERE role_id = 'role_student_global'
  AND permission_id IN (
    SELECT id FROM permission_definitions
    WHERE permission_key IN (
        'academic.class_setup.view',
        'academic.timetable_period.view'
    )
);

-- 2. Remove over-broad generic academic permissions from Parent role
DELETE FROM role_permissions
WHERE role_id = 'role_parent_global'
  AND permission_id IN (
    SELECT id FROM permission_definitions
    WHERE permission_key IN (
        'academic.class_setup.view',
        'academic.timetable_period.view'
    )
);

-- 3. Ensure Student has the granular screen-level timetable permissions
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_student_global', id, 'school', '{}'::jsonb
FROM permission_definitions
WHERE permission_key IN (
    'academics.class_timetable.view',
    'academics.daily_class_timetable.view'
)
ON CONFLICT DO NOTHING;
