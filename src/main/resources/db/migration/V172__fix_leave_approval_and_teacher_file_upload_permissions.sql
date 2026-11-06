-- ============================================================================
-- Migration: V172__fix_leave_approval_and_teacher_file_upload_permissions.sql
-- Description: 
--   1. Drop fk_staff_leave_approver on staff_leaves to allow school admins,
--      super admins, and management without staff records to approve leaves.
--   2. Grant files module permissions (upload, view, download, replace) to all
--      staff, teacher, and school-specific custom roles.
-- ============================================================================

-- 1. Drop foreign key constraint on staff_leaves.approved_by
ALTER TABLE public.staff_leaves DROP CONSTRAINT IF EXISTS fk_staff_leave_approver;

-- 2. Ensure permission_definitions for files module reflect staff and teacher audience
UPDATE public.permission_definitions
SET audience = '["school", "staff", "student", "parent"]'::jsonb,
    supported_scope_types = '["school", "own", "linked"]'::jsonb
WHERE module_key = 'FILES';

-- 3. Grant perm_files_upload, perm_files_download, perm_files_view, perm_files_replace
-- to all roles where archetype IN ('STAFF', 'TEACHER', 'SCHOOL_ADMIN') or is_system_role = FALSE
INSERT INTO public.role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT r.school_id, r.id, p.perm_id, 'school', '{}'::jsonb
FROM public.roles r
CROSS JOIN (VALUES 
    ('perm_files_upload'),
    ('perm_files_view'),
    ('perm_files_download'),
    ('perm_files_replace')
) AS p(perm_id)
WHERE (r.archetype IN ('STAFF', 'TEACHER', 'SCHOOL_ADMIN') OR r.is_system_role = FALSE OR r.id = 'role_teacher_global')
  AND NOT EXISTS (
      SELECT 1 FROM public.role_permissions rp 
      WHERE rp.role_id = r.id 
        AND rp.permission_id = p.perm_id 
        AND (rp.school_id = r.school_id OR (rp.school_id IS NULL AND r.school_id IS NULL))
  );

-- 4. Deduplicate any duplicate role_permissions for safety
DELETE FROM public.role_permissions a 
USING public.role_permissions b 
WHERE a.id > b.id 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id 
  AND (a.school_id = b.school_id OR (a.school_id IS NULL AND b.school_id IS NULL));

