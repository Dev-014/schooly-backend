-- 1. Insert Schools
INSERT INTO schools (name, code, contact_email, contact_phone, address, status)
VALUES
('Global Academy', 'GA001', 'admin@globalacademy.com', '1234567890', '123 Education Lane', 'ACTIVE'),
('City High School', 'CHS002', 'info@cityhigh.com', '9876543210', '456 Scholar Way', 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

-- 2. Insert Users (Password is 'password')
INSERT INTO users (phone, name, email, password_hash, status)
VALUES
('9999999999', 'John Admin', 'admin@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ACTIVE'),
('8888888888', 'Sarah Teacher', 'sarah@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ACTIVE'),
('7777777777', 'Michael Student', 'michael@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ACTIVE')
ON CONFLICT (phone) DO NOTHING;

-- 3. Assign Roles (Using Subqueries to find IDs)
INSERT INTO user_school_roles (user_id, school_id, role, status)
VALUES
((SELECT id FROM users WHERE phone = '9999999999'), (SELECT id FROM schools WHERE code = 'GA001'), 'ADMIN', 'ACTIVE'),
((SELECT id FROM users WHERE phone = '8888888888'), (SELECT id FROM schools WHERE code = 'GA001'), 'TEACHER', 'ACTIVE'),
((SELECT id FROM users WHERE phone = '7777777777'), (SELECT id FROM schools WHERE code = 'GA001'), 'STUDENT', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- 4. Insert Classes
INSERT INTO class (name, school_id)
VALUES ('Grade 10-A', (SELECT id FROM schools WHERE code = 'GA001'))
ON CONFLICT DO NOTHING;

-- 5. Insert Staff
INSERT INTO staff (user_id, school_id, joining_date, salary, status)
VALUES
((SELECT id FROM users WHERE phone = '8888888888'), (SELECT id FROM schools WHERE code = 'GA001'), '2023-01-15', 50000.00, 'ACTIVE')
ON CONFLICT DO NOTHING;

-- 6. Insert Students
INSERT INTO student (user_id, name, admission_no, roll_number, class_id, school_id, status, admission_date)
VALUES
(
  (SELECT id FROM users WHERE phone = '7777777777'),
  'Michael Student', 'ADM-2024-001', '101',
  (SELECT id FROM class WHERE name = 'Grade 10-A' LIMIT 1),
  (SELECT id FROM schools WHERE code = 'GA001'),
  'ACTIVE', '2024-01-01'
)
ON CONFLICT DO NOTHING;

-- 7. Insert Sample Attendance (FIXED: Using subquery for student_id)
INSERT INTO student_attendance (student_id, school_id, attendance_date, status)
VALUES
(
  (SELECT id FROM student WHERE admission_no = 'ADM-2024-001'),
  (SELECT id FROM schools WHERE code = 'GA001'),
  CURRENT_DATE, 'PRESENT'
);

-- 8. Insert Sample Fee Invoice (FIXED: Using subquery for student_id)
INSERT INTO fee_invoice (student_id, school_id, due_date, total_amount, paid_amount, status)
VALUES
(
  (SELECT id FROM student WHERE admission_no = 'ADM-2024-001'),
  (SELECT id FROM schools WHERE code = 'GA001'),
  CURRENT_DATE + INTERVAL '30 days', 1500.00, 0.00, 'UNPAID'
);