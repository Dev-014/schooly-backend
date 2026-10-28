-- ============================================================================
-- Migration: V148__grant_admit_card_issue_to_teacher_role.sql
-- Description: The global Teacher role could only VIEW the Admit Card screen
--              (exams_results.admit_card_students.view). Teachers could preview
--              admit cards but never saw the Generate / Release actions, which are
--              guarded by exams_results.admit_card_students.edit ("Issue Admit Cards").
--              Grant the edit permission to the global Teacher role so teachers can
--              generate admit cards. School admins can still revoke it per role from
--              Settings -> Roles & Permissions, and custom roles can be granted it.
-- ============================================================================

INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_teacher_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key = 'exams_results.admit_card_students.edit'
  AND NOT EXISTS (
      SELECT 1
      FROM role_permissions rp
      WHERE rp.role_id = 'role_teacher_global'
        AND rp.permission_id = pd.id
        AND rp.school_id IS NULL
  );
