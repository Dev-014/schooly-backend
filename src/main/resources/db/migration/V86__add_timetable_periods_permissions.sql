-- Insert timetable period permissions
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES
('perm_academic_timetable_period_view', 'academic.timetable_period.view', 'ACADEMIC', 'timetable_period', 'view', 'View Timetable Periods', 'View timetable periods configuration.', '["school"]', false, true),
('perm_academic_timetable_period_edit', 'academic.timetable_period.edit', 'ACADEMIC', 'timetable_period', 'edit', 'Manage Timetable Periods', 'Create, edit, or delete timetable periods.', '["school"]', true, true)
ON CONFLICT (id) DO NOTHING;

-- Assign permissions to SUPER_ADMIN
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_super_admin_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key IN (
    'academic.timetable_period.view',
    'academic.timetable_period.edit'
)
ON CONFLICT DO NOTHING;

-- Assign permissions to SCHOOL_ADMIN
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_school_admin_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key IN (
    'academic.timetable_period.view',
    'academic.timetable_period.edit'
)
ON CONFLICT DO NOTHING;
