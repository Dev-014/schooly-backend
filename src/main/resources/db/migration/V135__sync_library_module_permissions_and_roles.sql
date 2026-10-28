-- ============================================================================
-- V135__sync_library_module_permissions_and_roles.sql
-- Synchronizes granular Library permissions, module keys, audiences, and role assignments
-- ============================================================================

-- 1. Insert or update granular library permissions in permission_definitions
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission, audience)
VALUES 
    ('perm_library_issue_return_report_view', 'library.issue_return_report.view', 'LIBRARY', 'issue_return_report', 'view', 'Access Issue Return Report', 'Allows viewing library issue and return reports', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_library_library_member_directory_view', 'library.library_member_directory.view', 'LIBRARY', 'library_member_directory', 'view', 'Access Library Member Directory', 'Allows viewing library member directory and member cards', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_library_issue___return_dashboard_view', 'library.issue_return_dashboard.view', 'LIBRARY', 'issue_return_dashboard', 'view', 'Access Issue & Return Dashboard', 'Allows managing book issues, returns, and circulation dashboard', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_library_book_list_view', 'library.book_list.view', 'LIBRARY', 'book_list', 'view', 'Access Book List', 'Allows viewing book list and catalog inventory', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_library_view', 'library.book.view', 'LIBRARY', 'book', 'view', 'View Library', 'Allows viewing library catalog and issued books', '["school", "linked"]'::jsonb, false, true, '["staff", "student", "parent"]'::jsonb),
    ('perm_library_edit', 'library.book.edit', 'LIBRARY', 'book', 'edit', 'Manage Library', 'Allows creating, updating, issuing, and managing books and library members', '["school"]'::jsonb, true, true, '["staff"]'::jsonb)
ON CONFLICT (id) DO UPDATE SET
    permission_key = EXCLUDED.permission_key,
    module_key = EXCLUDED.module_key,
    resource_key = EXCLUDED.resource_key,
    action_key = EXCLUDED.action_key,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    supported_scope_types = EXCLUDED.supported_scope_types,
    is_sensitive = EXCLUDED.is_sensitive,
    audience = EXCLUDED.audience;

-- 2. Standardize legacy V2 key if present with three underscores
UPDATE permission_definitions
SET permission_key = 'library.issue_return_dashboard.view',
    module_key = 'LIBRARY',
    audience = '["staff"]'::jsonb
WHERE id = 'perm_library_issue___return_dashboard_view';

-- 3. Assign all administrative Library permissions to Super Admin only (School Admin is configured explicitly)
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_super_admin_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.module_key = 'LIBRARY'
ON CONFLICT DO NOTHING;

-- 4. Grant universal book view to Student and Teacher roles
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.role_id, 'perm_library_view', 'school', '{}'::jsonb
FROM (VALUES ('role_student_global'), ('role_teacher_global'), ('role_parent_global')) AS r(role_id)
ON CONFLICT DO NOTHING;

-- 5. Deduplicate any duplicate role_permissions entries
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.ctid < b.ctid 
  AND COALESCE(a.school_id, 0) = COALESCE(b.school_id, 0) 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id;
