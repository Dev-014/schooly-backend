-- V127: Add student and parent academic portal permissions and remove administrative academic module access

-- 1. Insert student and parent portal permissions into permission_definitions
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES 
    ('perm_academic_student_timetable_view', 'academic.student_timetable.view', 'ACADEMIC', 'student_timetable', 'view', 'View Student Timetable', 'Access the student''s personal timetable.', '["own", "linked"]'::jsonb, false, true),
    ('perm_academic_parent_calendar_view', 'academic.parent_calendar.view', 'ACADEMIC', 'parent_calendar', 'view', 'View Parent Calendar', 'Access the parent academic calendar.', '["linked"]'::jsonb, false, true),
    ('perm_academic_subject_directory_view', 'academic.subject_directory.view', 'ACADEMIC', 'subject_directory', 'view', 'View Subject Directory', 'Access the course and subject directory.', '["own", "linked"]'::jsonb, false, true)
ON CONFLICT (id) DO NOTHING;

-- 2. Assign student portal permissions to Student role
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_student_global', pd.id, 'own', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.id IN ('perm_academic_student_timetable_view', 'perm_academic_subject_directory_view')
ON CONFLICT DO NOTHING;

-- 3. Assign parent portal permissions to Parent role
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_parent_global', pd.id, 'linked', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.id IN ('perm_academic_student_timetable_view', 'perm_academic_parent_calendar_view', 'perm_academic_subject_directory_view')
ON CONFLICT DO NOTHING;

-- 4. Remove administrative Academic Module permissions from Student and Parent roles
-- This ensures students/parents only access their dedicated personal timetable portal and never administrative setup screens
DELETE FROM role_permissions
WHERE role_id IN ('role_student_global', 'role_parent_global')
  AND permission_id IN ('perm_academics_class_timetable_view', 'perm_academics_daily_class_timetable_view');
