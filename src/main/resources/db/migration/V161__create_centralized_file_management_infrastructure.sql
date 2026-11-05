-- ============================================================================
-- Migration: V161__create_centralized_file_management_infrastructure.sql
-- Description: Centralized File & Document Management Infrastructure
--              Creates files, file_attachments, file_audit_logs, file_processing_jobs,
--              registers the FILES module in platform_modules, seeds permission_definitions,
--              and grants role_permissions.
-- ============================================================================

-- 1. Create files master table
CREATE TABLE IF NOT EXISTS public.files (
    id UUID PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES public.schools(id) ON DELETE RESTRICT,
    original_file_name VARCHAR(255) NOT NULL,
    storage_provider VARCHAR(50) NOT NULL DEFAULT 'S3',
    bucket_name VARCHAR(100) NOT NULL,
    storage_key VARCHAR(500) NOT NULL UNIQUE,
    mime_type VARCHAR(100) NOT NULL,
    file_extension VARCHAR(20) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    checksum_sha256 VARCHAR(64),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_UPLOAD',
    uploaded_by BIGINT REFERENCES public.users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_files_school_status ON public.files(school_id, status) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_files_storage_key ON public.files(storage_key);
CREATE INDEX IF NOT EXISTS idx_files_uploaded_by ON public.files(uploaded_by);

-- 2. Create file_attachments table (polymorphic linkage)
CREATE TABLE IF NOT EXISTS public.file_attachments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES public.schools(id) ON DELETE RESTRICT,
    file_id UUID NOT NULL REFERENCES public.files(id) ON DELETE CASCADE,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    created_by BIGINT REFERENCES public.users(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_file_attachments_lookup ON public.file_attachments(school_id, entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_file_attachments_file_id ON public.file_attachments(file_id);

-- 3. Create file_audit_logs table
CREATE TABLE IF NOT EXISTS public.file_audit_logs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    school_id BIGINT REFERENCES public.schools(id) ON DELETE SET NULL,
    file_id UUID REFERENCES public.files(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES public.users(id) ON DELETE SET NULL,
    action VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),
    details JSONB,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_file_audit_logs_file_id ON public.file_audit_logs(file_id);
CREATE INDEX IF NOT EXISTS idx_file_audit_logs_school_user ON public.file_audit_logs(school_id, user_id);

-- 4. Create file_processing_jobs table
CREATE TABLE IF NOT EXISTS public.file_processing_jobs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES public.schools(id) ON DELETE CASCADE,
    file_id UUID NOT NULL REFERENCES public.files(id) ON DELETE CASCADE,
    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'QUEUED',
    retry_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP WITHOUT TIME ZONE,
    completed_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_file_processing_jobs_status ON public.file_processing_jobs(status, job_type);

-- 5. Register FILES core module in platform_modules
INSERT INTO public.platform_modules (code, name, description, is_default, status, category, add_on_price, target_roles, created_at, updated_at)
SELECT 'FILES', 'File & Document Management', 'Centralized multi-tenant file and document repository', TRUE, 'ACTIVE', 'CORE', '0.00', 'ADMIN,TEACHER,STUDENT,PARENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM public.platform_modules WHERE code = 'FILES');

-- Assign FILES module to all existing subscription plans
INSERT INTO public.plan_modules (plan_id, module_id)
SELECT p.id, pm.id
FROM public.subscription_plans p
CROSS JOIN public.platform_modules pm
WHERE pm.code = 'FILES'
  AND NOT EXISTS (
      SELECT 1 FROM public.plan_modules plm 
      WHERE plm.plan_id = p.id AND plm.module_id = pm.id
  );

-- Assign FILES module to all schools in school_module_access
INSERT INTO public.school_module_access (school_id, module_id, enabled, enabled_at)
SELECT s.id, pm.id, TRUE, CURRENT_TIMESTAMP
FROM public.schools s
CROSS JOIN public.platform_modules pm
WHERE pm.code = 'FILES'
  AND NOT EXISTS (
      SELECT 1 FROM public.school_module_access sma 
      WHERE sma.school_id = s.id AND sma.module_id = pm.id
  );

-- 6. Register permission definitions
INSERT INTO public.permission_definitions (
    id, permission_key, module_key, resource_key, action_key, name, description, audience, supported_scope_types, requires_assignment, is_sensitive, is_system_permission, is_active, created_at
)
VALUES
('perm_files_upload', 'files.file.upload', 'FILES', 'file', 'upload', 'Upload Files', 'Allows users to initiate and complete file uploads', '["school", "staff", "student", "parent"]'::jsonb, '["school", "own", "linked"]'::jsonb, FALSE, FALSE, TRUE, TRUE, CURRENT_TIMESTAMP),
('perm_files_view', 'files.file.view', 'FILES', 'file', 'view', 'View Files', 'Allows users to view file metadata and previews', '["school", "staff", "student", "parent"]'::jsonb, '["school", "own", "linked"]'::jsonb, FALSE, FALSE, TRUE, TRUE, CURRENT_TIMESTAMP),
('perm_files_download', 'files.file.download', 'FILES', 'file', 'download', 'Download Files', 'Allows users to download active files', '["school", "staff", "student", "parent"]'::jsonb, '["school", "own", "linked"]'::jsonb, FALSE, FALSE, TRUE, TRUE, CURRENT_TIMESTAMP),
('perm_files_replace', 'files.file.replace', 'FILES', 'file', 'replace', 'Replace Files', 'Allows users to replace an existing document with a new version', '["school", "staff"]'::jsonb, '["school", "own"]'::jsonb, FALSE, FALSE, TRUE, TRUE, CURRENT_TIMESTAMP),
('perm_files_delete', 'files.file.delete', 'FILES', 'file', 'delete', 'Delete Files', 'Allows users to soft-delete files', '["school", "staff"]'::jsonb, '["school", "own"]'::jsonb, FALSE, FALSE, TRUE, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 7. Seed role_permissions for global roles
-- Global Admin roles get all 5 permissions (school scope)
INSERT INTO public.role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.role_id, p.perm_id, 'school', '{}'::jsonb
FROM (VALUES 
    ('role_super_admin_global'),
    ('role_school_admin_global')
) AS r(role_id)
CROSS JOIN (VALUES 
    ('perm_files_upload'),
    ('perm_files_view'),
    ('perm_files_download'),
    ('perm_files_replace'),
    ('perm_files_delete')
) AS p(perm_id)
WHERE NOT EXISTS (
    SELECT 1 FROM public.role_permissions rp 
    WHERE rp.role_id = r.role_id AND rp.permission_id = p.perm_id AND rp.school_id IS NULL
);

-- Teacher role gets upload, view, download, replace (school scope)
INSERT INTO public.role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_teacher_global', p.perm_id, 'school', '{}'::jsonb
FROM (VALUES 
    ('perm_files_upload'),
    ('perm_files_view'),
    ('perm_files_download'),
    ('perm_files_replace')
) AS p(perm_id)
WHERE NOT EXISTS (
    SELECT 1 FROM public.role_permissions rp 
    WHERE rp.role_id = 'role_teacher_global' AND rp.permission_id = p.perm_id AND rp.school_id IS NULL
);

-- Student role gets upload, view, download (own scope)
INSERT INTO public.role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_student_global', p.perm_id, 'own', '{}'::jsonb
FROM (VALUES 
    ('perm_files_upload'),
    ('perm_files_view'),
    ('perm_files_download')
) AS p(perm_id)
WHERE NOT EXISTS (
    SELECT 1 FROM public.role_permissions rp 
    WHERE rp.role_id = 'role_student_global' AND rp.permission_id = p.perm_id AND rp.school_id IS NULL
);

-- Parent role gets view, download (linked scope)
INSERT INTO public.role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_parent_global', p.perm_id, 'linked', '{}'::jsonb
FROM (VALUES 
    ('perm_files_view'),
    ('perm_files_download')
) AS p(perm_id)
WHERE NOT EXISTS (
    SELECT 1 FROM public.role_permissions rp 
    WHERE rp.role_id = 'role_parent_global' AND rp.permission_id = p.perm_id AND rp.school_id IS NULL
);

