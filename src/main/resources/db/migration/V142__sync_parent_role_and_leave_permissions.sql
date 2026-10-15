-- ============================================================================
-- V142__sync_parent_role_and_leave_permissions.sql
-- Synchronizes LEAVE_MANAGEMENT module, parent role permissions, and test profiles
-- ============================================================================

-- 1. Ensure LEAVE_MANAGEMENT module exists in platform_modules with complete target roles
INSERT INTO platform_modules (code, name, description, is_default, status, category, add_on_price, target_roles)
VALUES ('LEAVE_MANAGEMENT', 'Leave Management', 'Global leave requests for staff, students, and parents', TRUE, 'ACTIVE', 'OPERATION', 0.00, 'ADMIN,TEACHER,STUDENT,PARENT')
ON CONFLICT (code) DO UPDATE 
SET target_roles = 'ADMIN,TEACHER,STUDENT,PARENT', is_default = TRUE, status = 'ACTIVE';

-- 2. Update target_roles for core modules accessed by Parents & Students
UPDATE platform_modules 
SET target_roles = 'ADMIN,TEACHER,STUDENT,PARENT' 
WHERE code IN ('LEAVE_MANAGEMENT', 'EXAMS_RESULTS', 'FEES', 'ATTENDANCE', 'ACADEMICS', 'STUDENTS', 'HOMEWORK', 'COMMUNICATION');

-- 3. Enable LEAVE_MANAGEMENT across all existing schools in school_module_access
INSERT INTO school_module_access (school_id, module_id, enabled, enabled_at, updated_at)
SELECT s.id, pm.id, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM schools s
CROSS JOIN platform_modules pm
WHERE pm.code = 'LEAVE_MANAGEMENT'
  AND NOT EXISTS (
      SELECT 1 FROM school_module_access sma 
      WHERE sma.school_id = s.id AND sma.module_id = pm.id
  );

UPDATE school_module_access sma
SET enabled = TRUE, updated_at = CURRENT_TIMESTAMP
FROM platform_modules pm
WHERE sma.module_id = pm.id AND pm.code = 'LEAVE_MANAGEMENT';

-- 4. Ensure LEAVE_MANAGEMENT is part of all active subscription plans
INSERT INTO plan_modules (plan_id, module_id)
SELECT sp.id, pm.id
FROM subscription_plans sp
CROSS JOIN platform_modules pm
WHERE pm.code = 'LEAVE_MANAGEMENT'
ON CONFLICT DO NOTHING;

-- 5. Update permission definitions for leave management scopes & audience
UPDATE permission_definitions
SET supported_scope_types = '["own", "linked", "school"]'::jsonb,
    audience = '["school", "staff", "student", "parent"]'::jsonb
WHERE id = 'perm_leave_student_view' OR permission_key = 'leave_management.student_leaves.view';

UPDATE permission_definitions
SET supported_scope_types = '["own", "assigned", "school"]'::jsonb,
    audience = '["school", "staff"]'::jsonb
WHERE id = 'perm_leave_staff_view' OR permission_key = 'leave_management.staff_leaves.view';

-- 6. Ensure role_parent_global has all required parent-facing permissions
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_parent_global', pd.id, 
       CASE 
         WHEN pd.id IN ('perm_communication_send', 'perm_library_view') THEN 'school' 
         ELSE 'linked' 
       END, 
       '{}'::jsonb
FROM permission_definitions pd
WHERE pd.id IN (
    'perm_leave_student_view',
    'perm_fees_student_fees_view',
    'perm_fee_view',
    'perm_exams_results_report_card_view',
    'student_exams_results_view',
    'student_exams_admit_card_view',
    'student_exams_timetable_view',
    'student_exams_syllabus_view',
    'perm_exams_results_exam_schedule_student_view',
    'perm_attendance_attendance_my_records_view',
    'perm_attendance_view',
    'perm_academic_student_timetable_view',
    'perm_academic_subject_directory_view',
    'perm_academic_parent_calendar_view',
    'perm_homework_view',
    'perm_downloads_view',
    'perm_communication_send',
    'perm_library_view',
    'perm_discipline_view',
    'perm_dashboard_student_reports_view'
)
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_parent_global'
      AND rp.permission_id = pd.id
      AND rp.school_id IS NULL
);

-- Ensure scope is 'linked' for student-specific permissions in role_parent_global
UPDATE role_permissions
SET scope_type = 'linked'
WHERE role_id = 'role_parent_global'
  AND school_id IS NULL
  AND permission_id NOT IN ('perm_communication_send', 'perm_library_view');

-- 7. Ensure test/demo users are properly linked in users, user_role_mappings, and student_parents
-- Link User 7 (Rajesh Malhotra, 6666666666) as Parent in school 1000 and 1001
INSERT INTO user_role_mappings (school_id, user_id, role_id, academic_session_id, is_active)
SELECT s_id, 7, 'role_parent_global', NULL, TRUE
FROM (VALUES (1000::bigint), (1001::bigint)) AS t(s_id)
WHERE EXISTS (SELECT 1 FROM users WHERE id = 7)
  AND NOT EXISTS (
      SELECT 1 FROM user_role_mappings urm 
      WHERE urm.user_id = 7 AND urm.school_id = t.s_id AND urm.role_id = 'role_parent_global'
  );

-- Link User 7 to students 4 and 5 in school 1000
INSERT INTO student_parents (student_id, parent_user_id, relation, is_primary)
SELECT s_id, 7, 'FATHER', TRUE
FROM (VALUES (4::bigint), (5::bigint)) AS t(s_id)
WHERE EXISTS (SELECT 1 FROM users WHERE id = 7)
  AND EXISTS (SELECT 1 FROM student WHERE id = t.s_id)
ON CONFLICT (student_id, parent_user_id) DO NOTHING;

-- Link User 4 (Michael Parent, 7777777777) as Parent in school 1000
UPDATE users SET name = 'Michael Parent' WHERE id = 4;

INSERT INTO user_role_mappings (school_id, user_id, role_id, academic_session_id, is_active)
SELECT 1000, 4, 'role_parent_global', NULL, TRUE
WHERE EXISTS (SELECT 1 FROM users WHERE id = 4)
  AND NOT EXISTS (
      SELECT 1 FROM user_role_mappings urm 
      WHERE urm.user_id = 4 AND urm.school_id = 1000 AND urm.role_id = 'role_parent_global'
  );

-- Link User 4 to students 17 and 23 in school 1000
INSERT INTO student_parents (student_id, parent_user_id, relation, is_primary)
SELECT s_id, 4, 'FATHER', TRUE
FROM (VALUES (17::bigint), (23::bigint)) AS t(s_id)
WHERE EXISTS (SELECT 1 FROM users WHERE id = 4)
  AND EXISTS (SELECT 1 FROM student WHERE id = t.s_id)
ON CONFLICT (student_id, parent_user_id) DO NOTHING;

-- Link User 2 (John Admin, 9999999999) to School 1000 as School Admin
INSERT INTO user_role_mappings (school_id, user_id, role_id, academic_session_id, is_active)
SELECT 1000, 2, 'role_school_admin_global', NULL, TRUE
WHERE EXISTS (SELECT 1 FROM users WHERE id = 2)
  AND NOT EXISTS (
      SELECT 1 FROM user_role_mappings urm 
      WHERE urm.user_id = 2 AND urm.school_id = 1000 AND urm.role_id = 'role_school_admin_global'
  );

-- Link User 3 (Sarah Teacher, 8888888888) to School 1000 and 1001 as Teacher
INSERT INTO user_role_mappings (school_id, user_id, role_id, academic_session_id, is_active)
SELECT s_id, 3, 'role_teacher_global', NULL, TRUE
FROM (VALUES (1000::bigint), (1001::bigint)) AS t(s_id)
WHERE EXISTS (SELECT 1 FROM users WHERE id = 3)
  AND NOT EXISTS (
      SELECT 1 FROM user_role_mappings urm 
      WHERE urm.user_id = 3 AND urm.school_id = t.s_id AND urm.role_id = 'role_teacher_global'
  );

-- 8. Deduplicate any duplicate role_permissions
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.id > b.id 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id 
  AND (a.school_id = b.school_id OR (a.school_id IS NULL AND b.school_id IS NULL));
