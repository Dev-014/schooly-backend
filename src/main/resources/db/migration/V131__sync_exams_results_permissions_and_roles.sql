-- ============================================================================
-- V131__sync_exams_results_permissions_and_roles.sql
-- Synchronizes granular Exams & Results permissions, audiences, and role assignments
-- ============================================================================

-- 1. Ensure audience is properly scoped for Exams & Results permissions
UPDATE permission_definitions 
SET audience = '["student", "parent"]'::jsonb
WHERE id IN (
    'student_exams_timetable_view',
    'student_exams_admit_card_view',
    'student_exams_results_view',
    'student_exams_syllabus_view'
);

UPDATE permission_definitions 
SET audience = '["staff"]'::jsonb
WHERE module_key = 'EXAMS_RESULTS'
  AND id NOT IN (
    'student_exams_timetable_view',
    'student_exams_admit_card_view',
    'student_exams_results_view',
    'student_exams_syllabus_view'
  );

-- 2. Assign all administrative Exams & Results permissions to Super Admin and School Admin
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.role_id, pd.id, 'school', '{}'::jsonb
FROM (VALUES ('role_super_admin_global'), ('role_school_admin_global')) AS r(role_id)
CROSS JOIN permission_definitions pd
WHERE pd.module_key = 'EXAMS_RESULTS'
  AND pd.id NOT IN (
    'student_exams_timetable_view',
    'student_exams_admit_card_view',
    'student_exams_results_view',
    'student_exams_syllabus_view'
  )
ON CONFLICT DO NOTHING;

-- 3. Revoke personal student exam views from School Admin and Super Admin
DELETE FROM role_permissions
WHERE role_id IN ('role_super_admin_global', 'role_school_admin_global')
  AND permission_id IN (
    'student_exams_timetable_view',
    'student_exams_admit_card_view',
    'student_exams_results_view',
    'student_exams_syllabus_view'
  );

-- 4. Ensure Student role has access to their personal exam views
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_student_global', pd.id, 'own', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.id IN (
    'student_exams_timetable_view',
    'student_exams_admit_card_view',
    'student_exams_results_view',
    'student_exams_syllabus_view'
)
ON CONFLICT DO NOTHING;

-- 5. Deduplicate any duplicate role_permissions entries
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.ctid < b.ctid 
  AND COALESCE(a.school_id, 0) = COALESCE(b.school_id, 0) 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id;
