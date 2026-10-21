-- ============================================================================
-- Migration: V146__scope_attendance_registers_for_teachers.sql
-- Description: Enable 'class', 'section', and 'school' scopes for Take Register
--              and Subject Attendance, and set default scope to 'class' for teachers.
-- ============================================================================

-- 1. Update supported_scope_types on permission_definitions
UPDATE permission_definitions
SET supported_scope_types = '["class", "section", "school"]'::jsonb
WHERE permission_key IN (
    'attendance.attendance_register.view',
    'attendance.subject_attendance.view',
    'attendance.subject_attendance.edit'
);

-- 2. Update role_permissions for global teacher role to scope_type = 'class'
UPDATE role_permissions
SET scope_type = 'class'
WHERE role_id = 'role_teacher_global'
  AND permission_id IN (
      SELECT id FROM permission_definitions 
      WHERE permission_key IN (
          'attendance.attendance_register.view',
          'attendance.subject_attendance.view',
          'attendance.subject_attendance.edit'
      )
  );

-- 3. Also update any school-specific teacher roles to scope_type = 'class' if currently 'school'
UPDATE role_permissions rp
SET scope_type = 'class'
FROM roles r, permission_definitions pd
WHERE rp.role_id = r.id
  AND rp.permission_id = pd.id
  AND pd.permission_key IN (
      'attendance.attendance_register.view',
      'attendance.subject_attendance.view',
      'attendance.subject_attendance.edit'
  )
  AND (r.archetype = 'STAFF' OR LOWER(r.name) LIKE '%teacher%')
  AND r.id != 'role_school_admin_global'
  AND r.id != 'role_super_admin_global';
