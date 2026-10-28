-- ============================================================================
-- V137__move_session_setting_to_academics.sql
-- Relocates Session Setting to Academics module as Session Configure
-- ============================================================================

-- 1. Insert or update the canonical permission definition under ACADEMICS module
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission, audience)
VALUES 
    ('perm_academics_session_configure_view', 'academics.session_configure.view', 'ACADEMICS', 'session_configure', 'view', 'Access Session Configure', 'Allows access to the Session Configure screen', '["school"]'::jsonb, false, true, '["staff"]'::jsonb)
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

-- 2. Clean up legacy perm_settings_session_setting_view
DELETE FROM role_permissions WHERE permission_id = 'perm_settings_session_setting_view';
DELETE FROM permission_definitions WHERE id = 'perm_settings_session_setting_view';

-- 3. Assign academics.session_configure.view to Super Admin and School Admin roles
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
VALUES 
    (NULL, 'role_super_admin_global', 'perm_academics_session_configure_view', 'school', '{}'::jsonb),
    (NULL, 'role_school_admin_global', 'perm_academics_session_configure_view', 'school', '{}'::jsonb)
ON CONFLICT DO NOTHING;

-- 4. Deduplicate any duplicate role_permissions entries
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.ctid < b.ctid 
  AND COALESCE(a.school_id, 0) = COALESCE(b.school_id, 0) 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id;
