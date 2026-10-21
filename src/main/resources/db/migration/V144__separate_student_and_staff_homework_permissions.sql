-- ============================================================================
-- V144__separate_student_and_staff_homework_permissions.sql
-- Decouples staff assignment authoring from student/parent homework viewing.
-- Introduces canonical perm_homework_portfolio_view and scopes assignment authoring to staff.
-- ============================================================================

-- 1. Insert or update canonical perm_homework_portfolio_view in permission_definitions
INSERT INTO permission_definitions (
    id, 
    permission_key, 
    module_key, 
    resource_key, 
    action_key, 
    name, 
    description, 
    supported_scope_types, 
    is_sensitive, 
    is_system_permission, 
    audience
) VALUES (
    'perm_homework_portfolio_view',
    'homework.portfolio.view',
    'HOMEWORK',
    'portfolio',
    'view',
    'My Homework & Portfolio',
    'Allows students and parents to view assigned homework, deadlines, and submissions.',
    '["own", "linked", "school"]'::jsonb,
    false,
    true,
    '["student", "parent", "staff"]'::jsonb
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    supported_scope_types = EXCLUDED.supported_scope_types,
    audience = EXCLUDED.audience;

-- 2. Restrict homework.assignment.view and homework.assignment.edit audience strictly to staff
UPDATE permission_definitions
SET audience = '["staff"]'::jsonb,
    name = CASE 
        WHEN permission_key = 'homework.assignment.view' THEN 'View Assignment Authoring'
        ELSE 'Assign Homework'
    END,
    description = CASE 
        WHEN permission_key = 'homework.assignment.view' THEN 'Allows staff to access the assignment authoring and editing workspace.'
        ELSE 'Allows staff to create, update, and publish homework assignments.'
    END
WHERE id IN ('perm_homework_view', 'perm_homework_edit')
   OR permission_key IN ('homework.assignment.view', 'homework.assignment.edit');

-- 3. Grant perm_homework_portfolio_view to Student (own scope) and Parent (linked scope)
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_student_global', 'perm_homework_portfolio_view', 'own', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_student_global'
      AND rp.permission_id = 'perm_homework_portfolio_view'
      AND rp.school_id IS NULL
);

INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_parent_global', 'perm_homework_portfolio_view', 'linked', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_parent_global'
      AND rp.permission_id = 'perm_homework_portfolio_view'
      AND rp.school_id IS NULL
);

-- 4. Grant perm_homework_portfolio_view to administrative and teacher roles (school scope)
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, r.role_id, 'perm_homework_portfolio_view', 'school', '{}'::jsonb
FROM (VALUES ('role_super_admin_global'), ('role_school_admin_global'), ('role_teacher_global')) AS r(role_id)
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.role_id
      AND rp.permission_id = 'perm_homework_portfolio_view'
      AND rp.school_id IS NULL
);

-- 5. Ensure Student role has permission to submit homework answers
INSERT INTO role_permissions (school_id, role_id, permission_id, scope_type, scope_config)
SELECT NULL, 'role_student_global', 'perm_homework_submission_edit', 'own', '{}'::jsonb
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = 'role_student_global'
      AND rp.permission_id = 'perm_homework_submission_edit'
      AND rp.school_id IS NULL
);

-- 6. Revoke staff authoring and screen permissions from Student and Parent roles
DELETE FROM role_permissions
WHERE role_id IN ('role_student_global', 'role_parent_global')
  AND permission_id IN ('perm_homework_view', 'perm_homework_homework_view');

-- 7. Deduplicate any duplicate role_permissions entries
DELETE FROM role_permissions a 
USING role_permissions b 
WHERE a.id > b.id 
  AND a.role_id = b.role_id 
  AND a.permission_id = b.permission_id 
  AND (a.school_id = b.school_id OR (a.school_id IS NULL AND b.school_id IS NULL));
