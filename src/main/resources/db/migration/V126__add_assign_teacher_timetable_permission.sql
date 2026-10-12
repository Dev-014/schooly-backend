-- V126: Add assign_teacher_timetable permission and ensure complete ACADEMICS parity for SUPER_ADMIN and SCHOOL_ADMIN

INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES (
    'perm_academics_assign_teacher_timetable_view',
    'academics.assign_teacher_timetable.view',
    'ACADEMICS',
    'assign_teacher_timetable',
    'view',
    'Access Assign Teacher Timetable',
    'Allows access to the Assign Teacher Timetable screen',
    '["school"]'::jsonb,
    false,
    true
)
ON CONFLICT (id) DO NOTHING;

-- Map to SUPER_ADMIN
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.id, pd.id, 'school', '{}'::jsonb
FROM roles r
CROSS JOIN permission_definitions pd
WHERE r.archetype = 'SUPER_ADMIN'
  AND pd.id = 'perm_academics_assign_teacher_timetable_view'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = pd.id
      AND rp.school_id IS NULL
  );

-- Map to SCHOOL_ADMIN
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.id, pd.id, 'school', '{}'::jsonb
FROM roles r
CROSS JOIN permission_definitions pd
WHERE r.archetype = 'SCHOOL_ADMIN'
  AND pd.id = 'perm_academics_assign_teacher_timetable_view'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = pd.id
      AND rp.school_id IS NULL
  );

-- Ensure all other ACADEMICS permissions are mapped to global SUPER_ADMIN and SCHOOL_ADMIN
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.id, pd.id, 'school', '{}'::jsonb
FROM roles r
CROSS JOIN permission_definitions pd
WHERE r.archetype IN ('SUPER_ADMIN', 'SCHOOL_ADMIN')
  AND pd.module_key = 'ACADEMICS'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = pd.id
      AND rp.school_id IS NULL
  );
