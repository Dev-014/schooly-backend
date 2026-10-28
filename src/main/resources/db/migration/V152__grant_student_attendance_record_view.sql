-- ============================================================================
-- V152__grant_student_attendance_record_view.sql
-- Grants attendance.attendance_record.view to role_student_global with 'own' scope
-- and aligns audience metadata in permission_definitions
-- ============================================================================

-- 1. Ensure audience of perm_attendance_view ('attendance.attendance_record.view') includes student and parent
UPDATE permission_definitions
SET audience = '["school", "staff", "student", "parent"]'::jsonb
WHERE id = 'perm_attendance_view' OR permission_key = 'attendance.attendance_record.view';

-- 2. Ensure role_student_global has perm_attendance_view ('attendance.attendance_record.view') with 'own' scope
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_student_global', 'perm_attendance_view', 'own', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_student_global'
      AND rp.permission_id = 'perm_attendance_view'
      AND rp.school_id IS NULL
);

-- 3. Also ensure role_student_global has perm_attendance_attendance_my_records_view with 'own' scope
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_student_global', 'perm_attendance_attendance_my_records_view', 'own', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_student_global'
      AND rp.permission_id = 'perm_attendance_attendance_my_records_view'
      AND rp.school_id IS NULL
);

-- 4. Deduplicate any duplicate role_permissions for student and parent attendance
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.id > b.id 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id 
  AND (a.school_id = b.school_id OR (a.school_id IS NULL AND b.school_id IS NULL));
