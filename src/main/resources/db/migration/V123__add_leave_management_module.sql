-- 1. Insert LEAVE_MANAGEMENT module
INSERT INTO platform_modules (id, code, name, description, is_default, status, created_at, updated_at, category, add_on_price, target_roles)
VALUES (22, 'LEAVE_MANAGEMENT', 'Leave Management', 'Global leave requests for staff and students', TRUE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'OPERATION', '0.00', 'ADMIN,TEACHER,STUDENT')
ON CONFLICT (id) DO NOTHING;

-- 2. Add permissions to permission_definitions
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, audience, requires_assignment, is_sensitive, is_system_permission, is_active, created_at)
VALUES 
('perm_leave_student_view', 'leave_management.student_leaves.view', 'LEAVE_MANAGEMENT', 'student_leaves', 'view', 'View Student Leaves', 'Allows access to student leave requests', '["school", "staff", "student", "parent"]'::jsonb, FALSE, FALSE, FALSE, TRUE, CURRENT_TIMESTAMP),
('perm_leave_staff_view', 'leave_management.staff_leaves.view', 'LEAVE_MANAGEMENT', 'staff_leaves', 'view', 'View Staff Leaves', 'Allows access to staff leave requests', '["school", "staff"]'::jsonb, FALSE, FALSE, FALSE, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 3. Migrate existing role permissions
UPDATE role_permissions SET permission_id = 'perm_leave_student_view' WHERE permission_id = 'perm_attendance_student_leave_view';
UPDATE role_permissions SET permission_id = 'perm_leave_staff_view' WHERE permission_id = 'perm_staff_hr_leaves_view';

-- 4. Delete old permissions
DELETE FROM permission_definitions WHERE id IN ('perm_attendance_student_leave_view', 'perm_staff_hr_leaves_view');
