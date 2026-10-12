-- V125: Sync missing attendance permissions to SUPER_ADMIN and ensure full parity

-- Grant all ATTENDANCE permissions to global SUPER_ADMIN role if missing
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.id, pd.id, 'school', '{}'::jsonb
FROM roles r
CROSS JOIN permission_definitions pd
WHERE r.archetype = 'SUPER_ADMIN'
  AND pd.module_key = 'ATTENDANCE'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = pd.id
      AND rp.school_id IS NULL
  );

-- Also ensure global SCHOOL_ADMIN role has all ATTENDANCE permissions if missing
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.id, pd.id, 'school', '{}'::jsonb
FROM roles r
CROSS JOIN permission_definitions pd
WHERE r.archetype = 'SCHOOL_ADMIN'
  AND pd.module_key = 'ATTENDANCE'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = pd.id
      AND rp.school_id IS NULL
  );
