-- V79__assign_teacher_fee_permissions_to_role.sql

-- Assign the newly created teacher fee permissions to the global Teacher role
INSERT INTO role_permissions (role_id, permission_id, scope_type)
VALUES 
('role_teacher_global', 'perm_fees_my_class_fees_view', 'school'),
('role_teacher_global', 'perm_fees_reminder_history_view', 'school'),
('role_teacher_global', 'perm_fees_my_class_fees_send_reminder', 'school'),
('role_teacher_global', 'perm_fees_my_class_fees_view_amount', 'school');
