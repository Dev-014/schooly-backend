-- ============================================================================
-- Migration: V174__grant_subject_attendance_to_students_parents_and_staff.sql
-- Description:
--   1. Update permission_definitions for 'attendance.subject_attendance.view'
--      to support 'own' and 'linked' scopes, and include student and parent audience.
--   2. Grant 'perm_attendance_subject_attendance_view' to role_student_global ('own')
--      and role_parent_global ('linked').
--   3. Grant 'perm_attendance_subject_attendance_view' to all teacher and staff roles
--      (both global and school-specific).
-- ============================================================================

-- 1. Update supported_scope_types and audience on permission_definitions
UPDATE permission_definitions
SET supported_scope_types = '["class", "section", "school", "own", "linked"]'::jsonb,
    audience = '["school", "staff", "student", "parent"]'::jsonb
WHERE id = 'perm_attendance_subject_attendance_view' 
   OR permission_key = 'attendance.subject_attendance.view';

-- 2. Grant to role_student_global with 'own' scope
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_student_global', 'perm_attendance_subject_attendance_view', 'own', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_student_global'
      AND rp.permission_id = 'perm_attendance_subject_attendance_view'
      AND rp.school_id IS NULL
);

-- 3. Grant to role_parent_global with 'linked' scope
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_parent_global', 'perm_attendance_subject_attendance_view', 'linked', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_parent_global'
      AND rp.permission_id = 'perm_attendance_subject_attendance_view'
      AND rp.school_id IS NULL
);

-- 4. Grant to role_teacher_global with 'class' scope
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_teacher_global', 'perm_attendance_subject_attendance_view', 'class', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_teacher_global'
      AND rp.permission_id = 'perm_attendance_subject_attendance_view'
      AND rp.school_id IS NULL
);

-- 5. Grant to all active staff/teacher/admin roles across all schools
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT r.school_id, r.id, 'perm_attendance_subject_attendance_view',
       CASE 
         WHEN r.archetype = 'SCHOOL_ADMIN' OR LOWER(r.name) LIKE '%admin%' OR LOWER(r.name) LIKE '%principal%' OR LOWER(r.name) LIKE '%owner%' THEN 'school'
         WHEN LOWER(r.name) LIKE '%section%' THEN 'section'
         ELSE 'class'
       END,
       '{}'::jsonb
FROM roles r
WHERE (r.archetype IN ('STAFF', 'TEACHER', 'SCHOOL_ADMIN') OR r.is_system_role = FALSE)
  AND r.id NOT IN ('role_student_global', 'role_parent_global')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id
        AND rp.permission_id = 'perm_attendance_subject_attendance_view'
        AND (rp.school_id = r.school_id OR (rp.school_id IS NULL AND r.school_id IS NULL))
  );

-- 6. Grant to any school-specific student roles with 'own' scope and parent roles with 'linked' scope
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT r.school_id, r.id, 'perm_attendance_subject_attendance_view', 'own', '{}'::jsonb
FROM roles r
WHERE r.archetype = 'STUDENT'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id
        AND rp.permission_id = 'perm_attendance_subject_attendance_view'
        AND (rp.school_id = r.school_id OR (rp.school_id IS NULL AND r.school_id IS NULL))
  );

INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT r.school_id, r.id, 'perm_attendance_subject_attendance_view', 'linked', '{}'::jsonb
FROM roles r
WHERE r.archetype = 'PARENT'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions rp
      WHERE rp.role_id = r.id
        AND rp.permission_id = 'perm_attendance_subject_attendance_view'
        AND (rp.school_id = r.school_id OR (rp.school_id IS NULL AND r.school_id IS NULL))
  );

-- 7. Deduplicate any duplicate role_permissions for safety
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.id > b.id 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id 
  AND (a.school_id = b.school_id OR (a.school_id IS NULL AND b.school_id IS NULL));

