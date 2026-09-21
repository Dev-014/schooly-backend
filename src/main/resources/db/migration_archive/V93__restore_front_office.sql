INSERT INTO platform_modules (code, name, description, is_default, status, category, add_on_price) 
VALUES ('FRONT_OFFICE', 'Front Office', 'Admission enquiries, visitor logs, and complaints', TRUE, 'ACTIVE', 'CORE', 0.00)
ON CONFLICT (code) DO NOTHING;

INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_system_permission, is_active)
VALUES 
('perm_front_office_admission_enquiry_view', 'front_office.admission_enquiry.view', 'FRONT_OFFICE', 'admission_enquiry', 'view', 'Access Admission Enquiry', 'Allows access to the Admission Enquiry screen', '["school"]', false, true),
('perm_front_office_visitor_book_view', 'front_office.visitor_book.view', 'FRONT_OFFICE', 'visitor_book', 'view', 'Access Visitor Book', 'Allows access to the Visitor Book screen', '["school"]', false, true),
('perm_front_office_gate_pass_view', 'front_office.gate_pass.view', 'FRONT_OFFICE', 'gate_pass', 'view', 'Access Gate Pass', 'Allows access to the Gate Pass screen', '["school"]', false, true),
('perm_front_office_entrance_exam_view', 'front_office.entrance_exam.view', 'FRONT_OFFICE', 'entrance_exam', 'view', 'Access Entrance Exam', 'Allows access to the Entrance Exam screen', '["school"]', false, true),
('perm_front_office_parcel_receive_view', 'front_office.parcel_receive.view', 'FRONT_OFFICE', 'parcel_receive', 'view', 'Access Parcel Receive', 'Allows access to the Parcel Receive screen', '["school"]', false, true),
('perm_front_office_parcel_dispatch_view', 'front_office.parcel_dispatch.view', 'FRONT_OFFICE', 'parcel_dispatch', 'view', 'Access Parcel Dispatch', 'Allows access to the Parcel Dispatch screen', '["school"]', false, true),
('perm_front_office_complaints_view', 'front_office.complaints.view', 'FRONT_OFFICE', 'complaints', 'view', 'Access Complaints', 'Allows access to the Complaints screen', '["school"]', false, true),
('perm_front_office_setup_front_office_view', 'front_office.setup_front_office.view', 'FRONT_OFFICE', 'setup_front_office', 'view', 'Access Setup Front Office', 'Allows access to the Setup Front Office screen', '["school"]', false, true),
('perm_front_office_front_desk_view', 'front_office.front_desk.view', 'FRONT_OFFICE', 'front_desk', 'view', 'Access Front Desk', 'Allows access to the Front Desk screen', '["school"]', false, true),
('perm_front_office_postal_records_view', 'front_office.postal_records.view', 'FRONT_OFFICE', 'postal_records', 'view', 'Access Postal Records', 'Allows access to the Postal Records screen', '["school"]', false, true),
('perm_front_office_inventory_view', 'front_office.inventory.view', 'FRONT_OFFICE', 'inventory', 'view', 'Access Inventory', 'Allows access to the Inventory screen', '["school"]', false, true),
('perm_front_office_library_view', 'front_office.library.view', 'FRONT_OFFICE', 'library', 'view', 'Access Library', 'Allows access to the Library screen', '["school"]', false, true)
ON CONFLICT (id) DO NOTHING;

-- Grant permissions to global school admin role
INSERT INTO role_permissions (role_id, permission_id, scope_type)
SELECT 'role_school_admin_global', id, 'school'
FROM permission_definitions WHERE module_key = 'FRONT_OFFICE';
