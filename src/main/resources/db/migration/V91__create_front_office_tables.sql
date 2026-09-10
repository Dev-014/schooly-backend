-- V91__create_front_office_tables.sql
-- Foundational tables and sample data for Admin Front Office Submodules

-- 1. Admission Enquiry & Follow-ups
CREATE TABLE IF NOT EXISTS admission_enquiries (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    enquiry_number VARCHAR(50),
    student_name VARCHAR(255) NOT NULL,
    parent_name VARCHAR(255),
    phone VARCHAR(50),
    email VARCHAR(255),
    enquiry_date DATE NOT NULL,
    next_follow_up_date DATE,
    source VARCHAR(100),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    class_id BIGINT,
    number_of_children INT DEFAULT 1,
    assigned_to BIGINT,
    detailed_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_admission_enquiry_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_admission_enquiry_class FOREIGN KEY (class_id) REFERENCES school_classes (id) ON DELETE SET NULL,
    CONSTRAINT fk_admission_enquiry_assigned_staff FOREIGN KEY (assigned_to) REFERENCES staff (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_admission_enquiries_school_id ON admission_enquiries (school_id);
CREATE INDEX IF NOT EXISTS idx_admission_enquiries_status ON admission_enquiries (status);
CREATE INDEX IF NOT EXISTS idx_admission_enquiries_date ON admission_enquiries (enquiry_date);

CREATE TABLE IF NOT EXISTS admission_enquiry_follow_ups (
    id BIGSERIAL PRIMARY KEY,
    enquiry_id BIGINT NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    notes TEXT,
    follow_up_date TIMESTAMP NOT NULL,
    recorded_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_enquiry_follow_up_enquiry FOREIGN KEY (enquiry_id) REFERENCES admission_enquiries (id) ON DELETE CASCADE,
    CONSTRAINT fk_enquiry_follow_up_staff FOREIGN KEY (recorded_by) REFERENCES staff (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_admission_enquiry_follow_ups_enquiry_id ON admission_enquiry_follow_ups (enquiry_id);

-- 2. Visitor Book
CREATE TABLE IF NOT EXISTS visitor_logs (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    visitor_name VARCHAR(255) NOT NULL,
    purpose VARCHAR(100) NOT NULL,
    meeting_with VARCHAR(255),
    meeting_with_staff_id BIGINT,
    phone VARCHAR(50),
    email VARCHAR(255),
    number_of_people INT DEFAULT 1,
    id_card_number VARCHAR(100),
    visit_date DATE NOT NULL,
    time_in TIME NOT NULL,
    est_time_out TIME,
    time_out TIME,
    status VARCHAR(50) DEFAULT 'ON_SITE',
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_visitor_logs_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_visitor_logs_staff FOREIGN KEY (meeting_with_staff_id) REFERENCES staff (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_visitor_logs_school_id ON visitor_logs (school_id);
CREATE INDEX IF NOT EXISTS idx_visitor_logs_status ON visitor_logs (status);
CREATE INDEX IF NOT EXISTS idx_visitor_logs_date ON visitor_logs (visit_date);

-- 3. Gate Passes
CREATE TABLE IF NOT EXISTS gate_passes (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    pass_number VARCHAR(50),
    person_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    student_id BIGINT,
    staff_id BIGINT,
    class_or_department VARCHAR(100),
    reason_for_exit TEXT NOT NULL,
    pass_date DATE NOT NULL,
    exit_time TIME NOT NULL,
    expected_return_time TIME,
    approved_by VARCHAR(255) NOT NULL,
    approved_by_staff_id BIGINT,
    status VARCHAR(50) DEFAULT 'APPROVED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_gate_passes_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_gate_passes_student FOREIGN KEY (student_id) REFERENCES students (id) ON DELETE SET NULL,
    CONSTRAINT fk_gate_passes_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE SET NULL,
    CONSTRAINT fk_gate_passes_approver FOREIGN KEY (approved_by_staff_id) REFERENCES staff (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_gate_passes_school_id ON gate_passes (school_id);
CREATE INDEX IF NOT EXISTS idx_gate_passes_status ON gate_passes (status);
CREATE INDEX IF NOT EXISTS idx_gate_passes_date ON gate_passes (pass_date);

-- 4. Parcel Receives (Inbound)
CREATE TABLE IF NOT EXISTS parcel_receives (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    sender_name VARCHAR(255) NOT NULL,
    contact_number VARCHAR(50),
    recipient_name VARCHAR(255),
    recipient_type VARCHAR(50),
    item_details TEXT NOT NULL,
    date_received DATE NOT NULL,
    received_by VARCHAR(255) NOT NULL,
    received_by_staff_id BIGINT,
    status VARCHAR(50) DEFAULT 'RECEIVED',
    collected_at TIMESTAMP,
    collected_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parcel_receives_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_parcel_receives_staff FOREIGN KEY (received_by_staff_id) REFERENCES staff (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_parcel_receives_school_id ON parcel_receives (school_id);
CREATE INDEX IF NOT EXISTS idx_parcel_receives_status ON parcel_receives (status);
CREATE INDEX IF NOT EXISTS idx_parcel_receives_date ON parcel_receives (date_received);

-- 5. Parcel Dispatches (Outbound)
CREATE TABLE IF NOT EXISTS parcel_dispatches (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    receiver_name VARCHAR(255) NOT NULL,
    receiver_institution VARCHAR(255),
    delivery_address TEXT NOT NULL,
    phone_number VARCHAR(50),
    dispatch_date DATE NOT NULL,
    item_details TEXT NOT NULL,
    courier_name VARCHAR(100) NOT NULL,
    tracking_number VARCHAR(100),
    status VARCHAR(50) DEFAULT 'IN_TRANSIT',
    delivered_date DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_parcel_dispatches_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_parcel_dispatches_school_id ON parcel_dispatches (school_id);
CREATE INDEX IF NOT EXISTS idx_parcel_dispatches_status ON parcel_dispatches (status);
CREATE INDEX IF NOT EXISTS idx_parcel_dispatches_date ON parcel_dispatches (dispatch_date);

-- 6. Add granular permissions if not already present
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES
('perm_front_office_admission_enquiry_edit', 'front_office.admission_enquiry.edit', 'FRONT_OFFICE', 'admission_enquiry', 'edit', 'Manage Admission Enquiry', 'Allows managing admission enquiries', '["school"]', false, true),
('perm_front_office_visitor_book_edit', 'front_office.visitor_book.edit', 'FRONT_OFFICE', 'visitor_book', 'edit', 'Manage Visitor Book', 'Allows managing visitor entries', '["school"]', false, true),
('perm_front_office_gate_pass_edit', 'front_office.gate_pass.edit', 'FRONT_OFFICE', 'gate_pass', 'edit', 'Manage Gate Pass', 'Allows managing and approving gate passes', '["school"]', false, true),
('perm_front_office_parcel_receive_edit', 'front_office.parcel_receive.edit', 'FRONT_OFFICE', 'parcel_receive', 'edit', 'Manage Parcel Receive', 'Allows managing parcel receipts', '["school"]', false, true),
('perm_front_office_parcel_dispatch_edit', 'front_office.parcel_dispatch.edit', 'FRONT_OFFICE', 'parcel_dispatch', 'edit', 'Manage Parcel Dispatch', 'Allows managing parcel dispatches', '["school"]', false, true)
ON CONFLICT (id) DO NOTHING;

-- Grant permissions to global system roles
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_super_admin_global', id, 'school', '{}'::jsonb
FROM permission_definitions
WHERE permission_key LIKE 'front_office.%'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_school_admin_global', id, 'school', '{}'::jsonb
FROM permission_definitions
WHERE permission_key LIKE 'front_office.%'
ON CONFLICT DO NOTHING;

-- 7. Seed sample data for schools
DO $$
DECLARE
    rec RECORD;
    v_enq_id BIGINT;
BEGIN
    FOR rec IN SELECT id FROM schools LOOP
        -- Seed Enquiries
        IF NOT EXISTS (SELECT 1 FROM admission_enquiries WHERE school_id = rec.id) THEN
            INSERT INTO admission_enquiries (school_id, enquiry_number, student_name, parent_name, phone, email, enquiry_date, next_follow_up_date, source, status, detailed_notes)
            VALUES (rec.id, '#ADM-2023-081', 'Ethan James', 'Robert James', '+1234 567 8901', 'robert.james@example.com', CURRENT_DATE - INTERVAL '15 days', CURRENT_DATE + INTERVAL '5 days', 'Website', 'CONVERTED', 'Enrolled in Grade 9.');

            INSERT INTO admission_enquiries (school_id, enquiry_number, student_name, parent_name, phone, email, enquiry_date, next_follow_up_date, source, status, detailed_notes)
            VALUES (rec.id, '#ADM-2023-084', 'Sophia Miller', 'David Miller', '+1987 654 3210', 'david.miller@example.com', CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE, 'Referral', 'ACTIVE', 'Parent is interested in the International Baccalaureate program. Specifically asked about science labs and extra-curricular athletic programs.')
            RETURNING id INTO v_enq_id;

            IF v_enq_id IS NOT NULL THEN
                INSERT INTO admission_enquiry_follow_ups (enquiry_id, action_type, notes, follow_up_date)
                VALUES 
                (v_enq_id, 'Inbound Call', 'Parent called inquiring about curriculum and scholarship opportunities.', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
                (v_enq_id, 'Document Upload', 'Submitted previous academic transcripts and birth certificate.', CURRENT_TIMESTAMP - INTERVAL '1 day');
            END IF;

            INSERT INTO admission_enquiries (school_id, enquiry_number, student_name, parent_name, phone, email, enquiry_date, next_follow_up_date, source, status, detailed_notes)
            VALUES (rec.id, '#ADM-2023-079', 'Liam Wilson', 'Sarah Wilson', '+1555 019 2233', 'sarah.wilson@example.com', CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '2 days', 'Social Media', 'PASSIVE', 'Awaiting parent confirmation for campus tour.');

            INSERT INTO admission_enquiries (school_id, enquiry_number, student_name, parent_name, phone, email, enquiry_date, next_follow_up_date, source, status, detailed_notes)
            VALUES (rec.id, '#ADM-2023-075', 'Ava Garcia', 'Maria Garcia', '+1321 456 7890', 'maria.garcia@example.com', CURRENT_DATE - INTERVAL '25 days', CURRENT_DATE - INTERVAL '3 days', 'Website', 'LOST', 'Selected another school closer to residence.');
        END IF;

        -- Seed Visitors
        IF NOT EXISTS (SELECT 1 FROM visitor_logs WHERE school_id = rec.id) THEN
            INSERT INTO visitor_logs (school_id, visitor_name, purpose, meeting_with, phone, number_of_people, visit_date, time_in, status, note)
            VALUES (rec.id, 'Marcus Sterling', 'Interview', 'Meeting with Admin', '+1 (202) 555-0156', 1, CURRENT_DATE, '09:15:00', 'ON_SITE', 'Scheduled job interview for Senior Math position');

            INSERT INTO visitor_logs (school_id, visitor_name, purpose, meeting_with, phone, number_of_people, visit_date, time_in, est_time_out, time_out, status, note)
            VALUES (rec.id, 'Amara Williams', 'Admission', 'Parent Inquiry', '+1 (202) 555-0198', 2, CURRENT_DATE, '08:30:00', '10:00:00', '10:00:00', 'CHECKED_OUT', 'Campus tour and fee inquiry');

            INSERT INTO visitor_logs (school_id, visitor_name, purpose, meeting_with, phone, number_of_people, visit_date, time_in, status, note)
            VALUES (rec.id, 'James Davidson', 'Delivery', 'Supply Delivery', '+1 (202) 555-0144', 1, CURRENT_DATE, '10:45:00', 'ON_SITE', 'Delivering chemistry lab supplies');

            INSERT INTO visitor_logs (school_id, visitor_name, purpose, meeting_with, phone, number_of_people, visit_date, time_in, est_time_out, time_out, status, note)
            VALUES (rec.id, 'Robert King', 'Other', 'Maintenance Check', '+1 (202) 555-0122', 1, CURRENT_DATE, '07:45:00', '09:30:00', '09:30:00', 'CHECKED_OUT', 'HVAC routine inspection');
        END IF;

        -- Seed Gate Passes
        IF NOT EXISTS (SELECT 1 FROM gate_passes WHERE school_id = rec.id) THEN
            INSERT INTO gate_passes (school_id, pass_number, person_name, role, class_or_department, reason_for_exit, pass_date, exit_time, approved_by, status)
            VALUES 
            (rec.id, 'GP-2023-0101', 'James S. Wilson', 'STUDENT', 'Class 12-A', 'Medical Appointment', CURRENT_DATE, '14:30:00', 'Dr. Sarah Jenkins', 'APPROVED'),
            (rec.id, 'GP-2023-0102', 'Maria Lopez', 'STAFF', 'Admin Dept', 'Official Bank Visit', CURRENT_DATE, '11:15:00', 'Principal Office', 'APPROVED'),
            (rec.id, 'GP-2023-0103', 'Rahul Kapoor', 'STUDENT', 'Class 9-C', 'Personal Emergency', CURRENT_DATE, '10:00:00', 'Vice Principal', 'APPROVED'),
            (rec.id, 'GP-2023-0104', 'Emily Myers', 'STAFF', 'Library', 'Inter-school Meeting', CURRENT_DATE, '09:45:00', 'Dr. Sarah Jenkins', 'APPROVED');
        END IF;

        -- Seed Parcel Receives
        IF NOT EXISTS (SELECT 1 FROM parcel_receives WHERE school_id = rec.id) THEN
            INSERT INTO parcel_receives (school_id, sender_name, contact_number, item_details, date_received, received_by, status)
            VALUES 
            (rec.id, 'FedEx Logistics', '+1 800 232 4444', 'Science Lab Equipment', CURRENT_DATE - INTERVAL '1 day', 'Sarah Wilson', 'RECEIVED'),
            (rec.id, 'Blue Dart', '+1 888 123 0000', 'Exam Answer Sheets', CURRENT_DATE - INTERVAL '2 days', 'John Doe', 'COLLECTED'),
            (rec.id, 'Amazon Business', '+1 555 019 9999', 'IT Spares & Peripherals', CURRENT_DATE - INTERVAL '3 days', 'Alex Rivers', 'RECEIVED');
        END IF;

        -- Seed Parcel Dispatches
        IF NOT EXISTS (SELECT 1 FROM parcel_dispatches WHERE school_id = rec.id) THEN
            INSERT INTO parcel_dispatches (school_id, receiver_name, receiver_institution, delivery_address, phone_number, dispatch_date, item_details, courier_name, tracking_number, status)
            VALUES 
            (rec.id, 'Jonathan Adams', 'University of Oxford', 'Wellington Square, Oxford OX1 2JD, UK', '+1 555 987 1111', CURRENT_DATE - INTERVAL '1 day', 'Research Thesis Pack', 'FedEx Express', '#TRK-9921', 'IN_TRANSIT'),
            (rec.id, 'Maria Lopez', 'Science Faculty Head', 'Faculty Building B, Room 402', '+1 555 987 2222', CURRENT_DATE - INTERVAL '2 days', 'Lab Supplies (Fragile)', 'Blue Dart', '#TRK-9854', 'DELIVERED'),
            (rec.id, 'Board of Kingston', 'Admin Council', 'Kingston City Center, Suite 100', '+1 555 987 3333', CURRENT_DATE - INTERVAL '3 days', 'Annual Financial Audit', 'DHL', '#TRK-9772', 'DELAYED');
        END IF;

    END LOOP;
END $$;
