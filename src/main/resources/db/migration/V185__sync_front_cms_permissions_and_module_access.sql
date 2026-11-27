-- ============================================================================
-- V185__sync_front_cms_permissions_and_module_access.sql
-- Adds granular Front CMS permissions to permission_definitions, grants them
-- to Super Admin and School Admin roles, and enables FRONT_CMS in plan_modules
-- and school_module_access so it appears in both /super-admin/roles and the
-- Admin Portal sidebar.
-- ============================================================================

-- 1. Insert Front CMS permissions into permission_definitions
INSERT INTO permission_definitions (
    id, permission_key, module_key, resource_key, action_key,
    name, description, supported_scope_types, requires_assignment,
    is_sensitive, is_system_permission, is_active, audience
)
VALUES
    ('perm_front_cms_events_view', 'front_cms.events.view', 'FRONT_CMS', 'events', 'view', 'View Events', 'Allows viewing Front CMS events', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),
    ('perm_front_cms_events_edit', 'front_cms.events.edit', 'FRONT_CMS', 'events', 'edit', 'Manage Events', 'Allows creating, editing, and deleting Front CMS events', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),

    ('perm_front_cms_gallery_view', 'front_cms.gallery.view', 'FRONT_CMS', 'gallery', 'view', 'View Gallery', 'Allows viewing Front CMS photo galleries', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),
    ('perm_front_cms_gallery_edit', 'front_cms.gallery.edit', 'FRONT_CMS', 'gallery', 'edit', 'Manage Gallery', 'Allows creating, editing, and deleting Front CMS galleries', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),

    ('perm_front_cms_notice_view', 'front_cms.notice.view', 'FRONT_CMS', 'notice', 'view', 'View Notices', 'Allows viewing Front CMS public notices', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),
    ('perm_front_cms_notice_edit', 'front_cms.notice.edit', 'FRONT_CMS', 'notice', 'edit', 'Manage Notices', 'Allows publishing, editing, and deleting Front CMS notices', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),

    ('perm_front_cms_media_manager_view', 'front_cms.media_manager.view', 'FRONT_CMS', 'media_manager', 'view', 'View Media Manager', 'Allows viewing uploaded Front CMS media assets', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),
    ('perm_front_cms_media_manager_edit', 'front_cms.media_manager.edit', 'FRONT_CMS', 'media_manager', 'edit', 'Manage Media Manager', 'Allows uploading and deleting Front CMS media assets', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),

    ('perm_front_cms_pages_view', 'front_cms.pages.view', 'FRONT_CMS', 'pages', 'view', 'View Pages', 'Allows viewing Front CMS website pages', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),
    ('perm_front_cms_pages_edit', 'front_cms.pages.edit', 'FRONT_CMS', 'pages', 'edit', 'Manage Pages', 'Allows creating, editing, and publishing Front CMS pages', '["school"]'::jsonb, false, true, true, true, '["staff"]'::jsonb),

    ('perm_front_cms_menus_view', 'front_cms.menus.view', 'FRONT_CMS', 'menus', 'view', 'View Menus', 'Allows viewing Front CMS navigation menus', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),
    ('perm_front_cms_menus_edit', 'front_cms.menus.edit', 'FRONT_CMS', 'menus', 'edit', 'Manage Menus', 'Allows configuring Front CMS navigation menus', '["school"]'::jsonb, false, true, true, true, '["staff"]'::jsonb),

    ('perm_front_cms_banner_image_view', 'front_cms.banner_image.view', 'FRONT_CMS', 'banner_image', 'view', 'View Banner Images', 'Allows viewing Front CMS homepage banners', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),
    ('perm_front_cms_banner_image_edit', 'front_cms.banner_image.edit', 'FRONT_CMS', 'banner_image', 'edit', 'Manage Banner Images', 'Allows uploading and managing Front CMS homepage banners', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),

    ('perm_front_cms_testimonials_view', 'front_cms.testimonials.view', 'FRONT_CMS', 'testimonials', 'view', 'View Testimonials', 'Allows viewing Front CMS testimonials', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),
    ('perm_front_cms_testimonials_edit', 'front_cms.testimonials.edit', 'FRONT_CMS', 'testimonials', 'edit', 'Manage Testimonials', 'Allows adding and moderating Front CMS testimonials', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),

    ('perm_front_cms_setting_view', 'front_cms.setting.view', 'FRONT_CMS', 'setting', 'view', 'View Front CMS Settings', 'Allows viewing Front CMS website configuration', '["school"]'::jsonb, false, false, true, true, '["staff"]'::jsonb),
    ('perm_front_cms_setting_edit', 'front_cms.setting.edit', 'FRONT_CMS', 'setting', 'edit', 'Manage Front CMS Settings', 'Allows updating Front CMS website configuration', '["school"]'::jsonb, false, true, true, true, '["staff"]'::jsonb)
ON CONFLICT (id) DO NOTHING;

-- 2. Grant all Front CMS permissions to Super Admin and School Admin global roles
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.role_id, pd.id, 'school', '{}'::jsonb
FROM (VALUES ('role_super_admin_global'), ('role_school_admin_global')) AS r(role_id)
CROSS JOIN permission_definitions pd
WHERE pd.module_key = 'FRONT_CMS'
ON CONFLICT DO NOTHING;

-- 3. Link FRONT_CMS (CORE default module) to all subscription plans
INSERT INTO plan_modules (plan_id, module_id)
SELECT sp.id, pm.id
FROM subscription_plans sp
CROSS JOIN platform_modules pm
WHERE pm.code = 'FRONT_CMS'
ON CONFLICT DO NOTHING;

-- 4. Enable FRONT_CMS in school_module_access for all existing schools where not yet configured
INSERT INTO school_module_access (school_id, module_id, enabled, enabled_at, updated_at)
SELECT s.id, pm.id, true, NOW(), NOW()
FROM schools s
CROSS JOIN platform_modules pm
WHERE pm.code = 'FRONT_CMS'
  AND NOT EXISTS (
      SELECT 1 FROM school_module_access sma
      WHERE sma.school_id = s.id AND sma.module_id = pm.id
  );
