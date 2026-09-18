-- V77__add_teacher_fee_permissions.sql
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES
('perm_fees_my_class_fees_view', 'fees.my_class_fees.view', 'FEES', 'my_class_fees', 'view', 'Access My Class Fees', 'Allows access to the My Class Fees screen', '["school"]', false, true),
('perm_fees_reminder_history_view', 'fees.reminder_history.view', 'FEES', 'reminder_history', 'view', 'Access Reminder History', 'Allows access to the Reminder History screen', '["school"]', false, true),
('perm_fees_my_class_fees_send_reminder', 'fees.my_class_fees.send_reminder', 'FEES', 'my_class_fees', 'send_reminder', 'Send Fee Reminders', 'Allows sending fee reminders to parents', '["school"]', false, true),
('perm_fees_my_class_fees_view_amount', 'fees.my_class_fees.view_amount', 'FEES', 'my_class_fees', 'view_amount', 'View Pending Amount', 'Allows viewing the actual pending fee amount of students', '["school"]', true, true)
ON CONFLICT (permission_key) DO NOTHING;
