-- ============================================================================
-- V136__sync_settings_module_permissions_and_roles.sql
-- Synchronizes Settings module permissions, session setting, and role assignments
-- ============================================================================

-- 1. Insert or update granular Settings permissions in permission_definitions
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission, audience)
VALUES 
    ('perm_settings_session_setting_view', 'settings.session_setting.view', 'SETTINGS', 'session_setting', 'view', 'Access Session Setting', 'Allows access to the Session Setting screen', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_settings_roles__legacy__view', 'settings.roles__legacy_.view', 'SETTINGS', 'roles__legacy_', 'view', 'Access Roles (Legacy)', 'Allows access to the Roles (Legacy) screen', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_settings_roles___permissions_view', 'settings.roles___permissions.view', 'SETTINGS', 'roles___permissions', 'view', 'Access Roles & Permissions', 'Allows access to the Roles & Permissions screen', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_settings_academic_setup_view', 'settings.academic_setup.view', 'SETTINGS', 'academic_setup', 'view', 'Access Academic Setup', 'Allows access to the Academic Setup screen', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_settings_support_view', 'settings.support.view', 'SETTINGS', 'support', 'view', 'Access Support', 'Allows access to the Support screen', '["school"]'::jsonb, false, true, '["staff"]'::jsonb),
    ('perm_settings_edit', 'settings.academic.edit', 'SETTINGS', 'academic', 'edit', 'Manage Settings', 'Allows editing global school settings', '["school"]'::jsonb, true, true, '["staff"]'::jsonb)
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

-- 2. Assign all administrative Settings permissions to Super Admin
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_super_admin_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.module_key = 'SETTINGS'
ON CONFLICT DO NOTHING;

-- 3. Deduplicate any duplicate role_permissions entries
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.ctid < b.ctid 
  AND COALESCE(a.school_id, 0) = COALESCE(b.school_id, 0) 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id;
