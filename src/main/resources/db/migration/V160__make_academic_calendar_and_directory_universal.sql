-- ============================================================================
-- Migration: V160__make_academic_calendar_and_directory_universal.sql
-- Description: Align audience, module key, scopes, and default role grants for
--              Academic Calendar, Subject Directory, and Student Timetable
--              to officially establish them as Universal Views, and fix
--              mislabeled student/parent attendance and report permissions.
-- ============================================================================

-- 1. Align module_key, audience, supported_scope_types, and labels for Universal Academic Views
UPDATE permission_definitions
SET 
    module_key = 'ACADEMICS',
    audience = '["school", "staff", "student", "parent"]'::jsonb,
    supported_scope_types = '["school", "own", "linked"]'::jsonb,
    name = CASE 
        WHEN id = 'perm_academic_parent_calendar_view' THEN 'View Academic Calendar'
        ELSE name
    END,
    description = CASE
        WHEN id = 'perm_academic_parent_calendar_view' THEN 'Allows viewing the school academic calendar, events, and milestones.'
        ELSE description
    END
WHERE id IN (
    'perm_academic_subject_directory_view',
    'perm_academic_parent_calendar_view',
    'perm_academic_student_timetable_view'
);

-- 2. Correct audience for student/parent personal views that were defaulted to staff
UPDATE permission_definitions
SET 
    audience = '["student", "parent"]'::jsonb,
    supported_scope_types = '["own", "linked"]'::jsonb
WHERE id IN (
    'perm_attendance_attendance_my_records_view',
    'perm_dashboard_student_reports_view',
    'perm_homework_submission_edit'
);

UPDATE permission_definitions
SET 
    audience = '["student", "parent", "staff"]'::jsonb,
    supported_scope_types = '["school", "own", "linked"]'::jsonb
WHERE id = 'perm_homework_submission_view';

-- 3. Grant Universal Academic Views to Super Admin (Global / School scope)
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_super_admin_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.id IN (
    'perm_academic_subject_directory_view',
    'perm_academic_parent_calendar_view',
    'perm_academic_student_timetable_view'
)
ON CONFLICT DO NOTHING;

-- 4. Grant Universal Academic Views to School Admin
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_school_admin_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.id IN (
    'perm_academic_subject_directory_view',
    'perm_academic_parent_calendar_view',
    'perm_academic_student_timetable_view'
)
ON CONFLICT DO NOTHING;

-- 5. Grant Academic Calendar and Subject Directory to Teacher role
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_teacher_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.id IN (
    'perm_academic_subject_directory_view',
    'perm_academic_parent_calendar_view'
)
ON CONFLICT DO NOTHING;

