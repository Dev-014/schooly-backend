-- ============================================================================
-- Migration: V149__grant_staff_leave_view_to_teacher_role.sql
-- Description: Grant perm_leave_staff_view (leave_management.staff_leaves.view)
--              to role_teacher_global with 'own' scope so teachers can view
--              and manage their own staff leaves and fetch school leave types.
-- ============================================================================

INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_teacher_global', pd.id, 'own', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key = 'leave_management.staff_leaves.view'
  AND NOT EXISTS (
      SELECT 1
      FROM role_permissions rp
      WHERE rp.role_id = 'role_teacher_global'
        AND rp.permission_id = pd.id
        AND rp.school_id IS NULL
  );
