-- ============================================================================
-- Migration: V153__add_class_timetable_edit_permission.sql
-- Description: Add granular class timetable edit permission to permission_definitions
--              and assign defaults.
--              - academics.class_timetable.edit: Granted to School Admin & Super Admin.
--                Class Teachers can edit their assigned class timetable contextually.
-- ============================================================================

INSERT INTO permission_definitions (
    id, permission_key, module_key, resource_key, action_key, name, description,
    audience, requires_assignment, is_sensitive, is_system_permission, is_active, created_at, supported_scope_types
)
VALUES 
(
    'perm_academics_class_timetable_edit',
    'academics.class_timetable.edit',
    'ACADEMICS',
    'class_timetable',
    'edit',
    'Edit Class Timetable',
    'Allows creating, updating, and saving class timetable period assignments',
    '["school", "staff"]'::jsonb,
    FALSE,
    FALSE,
    FALSE,
    TRUE,
    CURRENT_TIMESTAMP,
    '["school", "assigned", "class"]'::jsonb
)
ON CONFLICT (id) DO UPDATE SET
    permission_key = EXCLUDED.permission_key,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    supported_scope_types = EXCLUDED.supported_scope_types;

-- Grant class_timetable.edit to Super Admin and School Admin
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_super_admin_global', 'perm_academics_class_timetable_edit', 'school', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions WHERE role_id = 'role_super_admin_global' AND permission_id = 'perm_academics_class_timetable_edit' AND school_id IS NULL
);

INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_school_admin_global', 'perm_academics_class_timetable_edit', 'school', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions WHERE role_id = 'role_school_admin_global' AND permission_id = 'perm_academics_class_timetable_edit' AND school_id IS NULL
);
