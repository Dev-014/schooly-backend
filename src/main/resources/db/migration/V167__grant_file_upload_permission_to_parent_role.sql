-- ============================================================================
-- Migration: V167__grant_file_upload_permission_to_parent_role.sql
-- Description: Grant file upload, replace, and delete permissions to Parent role
--              for child document workflows (leave attachments, homework submissions,
--              admission documents, and profile records).
-- ============================================================================

-- 1. Ensure permission_definitions for files module reflect student & parent audience
UPDATE public.permission_definitions
SET audience = '["school", "staff", "student", "parent"]'::jsonb,
    supported_scope_types = '["school", "own", "linked"]'::jsonb
WHERE id IN ('perm_files_upload', 'perm_files_replace')
   OR permission_key IN ('files.file.upload', 'files.file.replace');

-- 2. Grant perm_files_upload to role_parent_global with 'linked' scope
INSERT INTO public.role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_parent_global', 'perm_files_upload', 'linked', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM public.role_permissions rp 
    WHERE rp.role_id = 'role_parent_global' 
      AND rp.permission_id = 'perm_files_upload' 
      AND rp.school_id IS NULL
);

-- 3. Grant perm_files_replace to role_parent_global (linked) and role_student_global (own)
INSERT INTO public.role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_parent_global', 'perm_files_replace', 'linked', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM public.role_permissions rp 
    WHERE rp.role_id = 'role_parent_global' 
      AND rp.permission_id = 'perm_files_replace' 
      AND rp.school_id IS NULL
);

INSERT INTO public.role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_student_global', 'perm_files_replace', 'own', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM public.role_permissions rp 
    WHERE rp.role_id = 'role_student_global' 
      AND rp.permission_id = 'perm_files_replace' 
      AND rp.school_id IS NULL
);

-- 4. Deduplicate any duplicate role_permissions for safety
DELETE FROM public.role_permissions a 
USING public.role_permissions b 
WHERE a.id > b.id 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id 
  AND (a.school_id = b.school_id OR (a.school_id IS NULL AND b.school_id IS NULL));

