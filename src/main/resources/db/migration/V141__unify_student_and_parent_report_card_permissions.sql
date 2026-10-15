-- ============================================================================
-- V141__unify_student_and_parent_report_card_permissions.sql
-- Unifies Student and Parent exam report card permissions
-- ============================================================================

-- 1. Update permission definition for student exam results
UPDATE permission_definitions
SET name = 'Student & Child Exam Results',
    description = 'Allows viewing of exam marks and downloading report cards for students and linked children.',
    supported_scope_types = '["own", "linked", "school"]'::jsonb,
    audience = '["student", "parent", "staff"]'::jsonb
WHERE id = 'student_exams_results_view' OR permission_key = 'student.exams.results.view';

-- 2. Update permission definition for report card view
UPDATE permission_definitions
SET name = 'Report Card',
    description = 'Allows viewing student academic report cards.',
    supported_scope_types = '["own", "linked", "school"]'::jsonb,
    audience = '["staff", "parent", "student"]'::jsonb
WHERE id = 'perm_exams_results_report_card_view' OR permission_key = 'exams_results.report_card.view';

-- 3. Grant student_exams_results_view to role_parent_global with 'linked' scope
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_parent_global', 'student_exams_results_view', 'linked', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_parent_global'
      AND rp.permission_id = 'student_exams_results_view'
      AND (rp.school_id IS NULL OR rp.school_id = 0)
);

UPDATE role_permissions
SET scope_type = 'linked'
WHERE role_id = 'role_parent_global'
  AND permission_id = 'student_exams_results_view';

-- 4. Grant perm_exams_results_report_card_view to role_parent_global with 'linked' scope
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_parent_global', 'perm_exams_results_report_card_view', 'linked', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_parent_global'
      AND rp.permission_id = 'perm_exams_results_report_card_view'
      AND (rp.school_id IS NULL OR rp.school_id = 0)
);

UPDATE role_permissions
SET scope_type = 'linked'
WHERE role_id = 'role_parent_global'
  AND permission_id = 'perm_exams_results_report_card_view';

-- 5. Deduplicate any duplicate role_permissions entries
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.id > b.id 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id 
  AND (a.school_id = b.school_id OR (a.school_id IS NULL AND b.school_id IS NULL));
