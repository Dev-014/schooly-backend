-- ============================================================================
-- V132__sync_front_office_permissions_and_roles.sql
-- Synchronizes granular Front Office permissions, audiences, and role assignments
-- ============================================================================

-- 1. Insert missing front office permissions into permission_definitions if not existing
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission, audience)
VALUES 
    ('perm_front_office_entrance_exam_edit', 'front_office.entrance_exam.edit', 'FRONT_OFFICE', 'entrance_exam', 'edit', 'Manage Entrance Exam', 'Allows managing entrance exam candidates and evaluations', '["school"]'::jsonb, true, true, '["staff"]'::jsonb),
    ('perm_front_office_complaints_edit', 'front_office.complaints.edit', 'FRONT_OFFICE', 'complaints', 'edit', 'Manage Complaints', 'Allows resolving and managing student/parent complaints', '["school"]'::jsonb, true, true, '["staff"]'::jsonb),
    ('perm_front_office_setup_front_office_edit', 'front_office.setup_front_office.edit', 'FRONT_OFFICE', 'setup_front_office', 'edit', 'Manage Setup Front Office', 'Allows configuring front office master settings', '["school"]'::jsonb, true, true, '["staff"]'::jsonb)
ON CONFLICT (id) DO NOTHING;

-- 2. Ensure all Front Office permissions have audience = '["staff"]'
UPDATE permission_definitions 
SET audience = '["staff"]'::jsonb
WHERE module_key = 'FRONT_OFFICE';

-- 3. Assign all administrative Front Office permissions to Super Admin and School Admin
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.role_id, pd.id, 'school', '{}'::jsonb
FROM (VALUES ('role_super_admin_global'), ('role_school_admin_global')) AS r(role_id)
CROSS JOIN permission_definitions pd
WHERE pd.module_key = 'FRONT_OFFICE'
ON CONFLICT DO NOTHING;

-- 4. Deduplicate any duplicate role_permissions entries
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.ctid < b.ctid 
  AND COALESCE(a.school_id, 0) = COALESCE(b.school_id, 0) 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id;
