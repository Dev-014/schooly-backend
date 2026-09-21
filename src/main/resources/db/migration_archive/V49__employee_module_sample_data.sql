-- V49__employee_module_sample_data.sql
-- Seeds 8 realistic employees with rich operational data across all employee module tables.
-- All inserts use ON CONFLICT DO NOTHING for idempotency (safe to re-run).

-- ─── STEP 0: Synchronize sequences ───────────────────────────────────────────
DO $$
BEGIN
    PERFORM setval('users_id_seq', GREATEST((SELECT MAX(id) FROM users), 100));
    PERFORM setval('super_admin_employees_id_seq', GREATEST((SELECT MAX(id) FROM super_admin_employees), 50));
END $$;

-- ─── STEP 1: Insert 8 new employee users ─────────────────────────────────────
-- Password hash = BCrypt for 'Welcome@123'
INSERT INTO users (name, email, phone, password_hash, status) VALUES
('Priya Sharma',   'priya.sharma@schooly.com',   '+91-9811001001', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ACTIVE'),
('Arjun Mehta',    'arjun.mehta@schooly.com',    '+91-9811001002', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ACTIVE'),
('Sunita Rajput',  'sunita.rajput@schooly.com',  '+91-9811001003', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ACTIVE'),
('Rajiv Nair',     'rajiv.nair@schooly.com',     '+91-9811001004', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ACTIVE'),
('Meera Kapoor',   'meera.kapoor@schooly.com',   '+91-9811001005', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ACTIVE'),
('Vikram Singh',   'vikram.singh@schooly.com',   '+91-9811001006', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ACTIVE'),
('Ananya Joshi',   'ananya.joshi@schooly.com',   '+91-9811001007', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ACTIVE'),
('Deepak Verma',   'deepak.verma@schooly.com',   '+91-9811001008', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ACTIVE')
ON CONFLICT (phone) DO NOTHING;

-- ─── STEP 2: Update existing Super Admin employee profile ─────────────────────
UPDATE super_admin_employees
SET department   = 'Administration',
    designation  = 'System Administrator',
    employee_code = 'EMP-2024-000',
    salary_band  = 'Band E',
    joined_at    = '2022-01-01',
    leave_balance = 18,
    updated_at   = NOW()
WHERE user_id = (SELECT id FROM users WHERE email = 'superadmin@schooly.com' LIMIT 1)
  AND employee_code IS NULL;

-- ─── STEP 3: Create super_admin_employees for the 8 new users ────────────────
INSERT INTO super_admin_employees (user_id, department, designation, employee_code, salary_band, joined_at, leave_balance)
SELECT u.id, emp.dept, emp.desig, emp.code, emp.band, emp.joined::date, emp.leave_bal
FROM users u
JOIN (VALUES
    ('priya.sharma@schooly.com',  'Human Resources',   'HR Director',         'EMP-2024-001', 'Band D', '2022-03-15', 21),
    ('arjun.mehta@schooly.com',   'Finance',           'Chief Finance Officer','EMP-2024-002', 'Band E', '2021-08-01', 24),
    ('sunita.rajput@schooly.com', 'Academic Affairs',  'Dean of Academics',   'EMP-2024-003', 'Band D', '2020-06-10', 20),
    ('rajiv.nair@schooly.com',    'IT & Technology',   'Head of IT',          'EMP-2024-004', 'Band C', '2023-01-20', 15),
    ('meera.kapoor@schooly.com',  'Administration',    'Operations Manager',  'EMP-2024-005', 'Band C', '2023-04-05', 12),
    ('vikram.singh@schooly.com',  'Finance',           'Senior Accountant',   'EMP-2024-006', 'Band B', '2023-07-01', 18),
    ('ananya.joshi@schooly.com',  'Human Resources',   'Recruitment Lead',    'EMP-2024-007', 'Band B', '2024-01-15', 20),
    ('deepak.verma@schooly.com',  'IT & Technology',   'DevOps Engineer',     'EMP-2024-008', 'Band B', '2024-02-01', 20)
) AS emp(email, dept, desig, code, band, joined, leave_bal) ON u.email = emp.email
WHERE NOT EXISTS (
    SELECT 1 FROM super_admin_employees sae WHERE sae.user_id = u.id
);

-- ─── STEP 4: Insert 5 Departments ────────────────────────────────────────────
INSERT INTO departments (name, description, head_employee_id)
VALUES
('Administration',    'Central operations and institutional governance',
    (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com' LIMIT 1)),
('Human Resources',   'Recruitment, payroll policy, and employee relations',
    (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com' LIMIT 1)),
('Finance',           'Budgeting, accounting, and financial compliance',
    (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com' LIMIT 1)),
('Academic Affairs',  'Curriculum development and academic governance',
    (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'sunita.rajput@schooly.com' LIMIT 1)),
('IT & Technology',   'Infrastructure, security, and software systems',
    (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com' LIMIT 1))
ON CONFLICT (name) DO NOTHING;

-- ─── STEP 5: Attendance — Last 7 working days for all 9 employees ─────────────
WITH emp_list AS (
    SELECT sae.id AS emp_id
    FROM super_admin_employees sae
    JOIN users u ON sae.user_id = u.id
    WHERE u.email IN (
        'superadmin@schooly.com', 'priya.sharma@schooly.com', 'arjun.mehta@schooly.com',
        'sunita.rajput@schooly.com', 'rajiv.nair@schooly.com', 'meera.kapoor@schooly.com',
        'vikram.singh@schooly.com', 'ananya.joshi@schooly.com', 'deepak.verma@schooly.com'
    )
),
all_days AS (
    SELECT d::date AS work_date
    FROM generate_series(
        CURRENT_DATE - INTERVAL '8 days',
        CURRENT_DATE - INTERVAL '1 day',
        INTERVAL '1 day'
    ) AS d
),
working_days AS (
    SELECT work_date FROM all_days
    WHERE EXTRACT(DOW FROM work_date) NOT IN (0, 6)
)
INSERT INTO employee_attendance (employee_id, date, status, check_in_time, check_out_time, notes)
SELECT
    e.emp_id,
    d.work_date,
    CASE
        WHEN (e.emp_id + EXTRACT(DOW FROM d.work_date)::int) % 9 = 0 THEN 'ABSENT'
        WHEN (e.emp_id + EXTRACT(DOW FROM d.work_date)::int) % 7 = 0 THEN 'HALF_DAY'
        ELSE 'PRESENT'
    END AS status,
    CASE
        WHEN (e.emp_id + EXTRACT(DOW FROM d.work_date)::int) % 9 = 0 THEN NULL
        WHEN (e.emp_id + EXTRACT(DOW FROM d.work_date)::int) % 5 = 0 THEN '09:30:00'::time
        ELSE '09:00:00'::time
    END AS check_in,
    CASE
        WHEN (e.emp_id + EXTRACT(DOW FROM d.work_date)::int) % 9 = 0 THEN NULL
        WHEN (e.emp_id + EXTRACT(DOW FROM d.work_date)::int) % 7 = 0 THEN '13:30:00'::time
        WHEN (e.emp_id + EXTRACT(DOW FROM d.work_date)::int) % 5 = 0 THEN '18:30:00'::time
        ELSE '17:00:00'::time
    END AS check_out,
    CASE
        WHEN (e.emp_id + EXTRACT(DOW FROM d.work_date)::int) % 9 = 0 THEN 'Personal leave'
        WHEN (e.emp_id + EXTRACT(DOW FROM d.work_date)::int) % 7 = 0 THEN 'Medical appointment'
        ELSE NULL
    END AS notes
FROM emp_list e
CROSS JOIN working_days d
ON CONFLICT DO NOTHING;

-- ─── STEP 6: Leave Requests ────────────────────────────────────────────────────
INSERT INTO employee_leaves (employee_id, leave_type, start_date, end_date, reason, status, approved_by)
VALUES
-- Priya Sharma — Approved sick leave
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'SICK_LEAVE', CURRENT_DATE - 10, CURRENT_DATE - 9, 'Seasonal flu and fever', 'APPROVED',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com')),

-- Arjun Mehta — Approved vacation
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'VACATION', CURRENT_DATE - 20, CURRENT_DATE - 16, 'Family trip to Goa for annual vacation', 'APPROVED',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com')),

-- Sunita Rajput — Pending leave for conference
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'sunita.rajput@schooly.com'),
 'PERSONAL', CURRENT_DATE + 5, CURRENT_DATE + 7, 'Attending national education summit in Delhi', 'PENDING', NULL),

-- Rajiv Nair — Rejected leave (short notice)
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'),
 'CASUAL_LEAVE', CURRENT_DATE - 2, CURRENT_DATE, 'Urgent personal work', 'REJECTED',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com')),

-- Meera Kapoor — Pending maternity
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'meera.kapoor@schooly.com'),
 'MATERNITY', CURRENT_DATE + 15, CURRENT_DATE + 105, 'Maternity leave as per company policy', 'PENDING', NULL),

-- Vikram Singh — Approved casual leave
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'vikram.singh@schooly.com'),
 'CASUAL_LEAVE', CURRENT_DATE - 5, CURRENT_DATE - 5, 'Personal errand — bank documentation', 'APPROVED',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com')),

-- Ananya Joshi — Pending sick leave
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'ananya.joshi@schooly.com'),
 'SICK_LEAVE', CURRENT_DATE + 1, CURRENT_DATE + 2, 'Wisdom tooth extraction recovery', 'PENDING', NULL),

-- Deepak Verma — Approved work-from-home (treated as leave)
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'deepak.verma@schooly.com'),
 'PERSONAL', CURRENT_DATE - 3, CURRENT_DATE - 3, 'Home internet and power maintenance', 'APPROVED',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'))

ON CONFLICT DO NOTHING;

-- ─── STEP 7: Payroll — August 2026 ────────────────────────────────────────────
INSERT INTO employee_payroll (employee_id, month, year, base_salary, allowances, deductions, net_salary, status, payment_date)
VALUES
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'August', 2026, 200000.00, 40000.00, 18000.00, 222000.00, 'PAID', CURRENT_DATE - 1),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'August', 2026, 120000.00, 22000.00, 11000.00, 131000.00, 'PAID', CURRENT_DATE - 1),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'August', 2026, 180000.00, 35000.00, 16000.00, 199000.00, 'PAID', CURRENT_DATE - 1),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'sunita.rajput@schooly.com'),
 'August', 2026, 125000.00, 20000.00, 11500.00, 133500.00, 'PENDING', NULL),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'),
 'August', 2026, 90000.00, 15000.00, 8000.00, 97000.00, 'PENDING', NULL),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'meera.kapoor@schooly.com'),
 'August', 2026, 85000.00, 12000.00, 7500.00, 89500.00, 'PENDING', NULL),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'vikram.singh@schooly.com'),
 'August', 2026, 70000.00, 10000.00, 6300.00, 73700.00, 'PAID', CURRENT_DATE - 1),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'ananya.joshi@schooly.com'),
 'August', 2026, 65000.00, 8000.00, 5800.00, 67200.00, 'PENDING', NULL),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'deepak.verma@schooly.com'),
 'August', 2026, 72000.00, 9000.00, 6500.00, 74500.00, 'PENDING', NULL)

ON CONFLICT DO NOTHING;

-- ─── STEP 8: Performance Reviews — Q1 2026 ────────────────────────────────────
INSERT INTO employee_performance (employee_id, review_cycle, rating, reviewer_id, comments, goals)
VALUES
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'Q1 2026', 5,
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'Outstanding leadership. Successfully hired 12 new staff members and reduced attrition by 30%.',
 'Implement automated onboarding process. Launch Employee Wellness Program by Q3 2026.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'Q1 2026', 5,
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'Exceptional financial stewardship. Reduced operational costs by 15% while maintaining growth.',
 'Implement AI-based budget forecasting. Achieve zero-deficit operations for FY2027.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'sunita.rajput@schooly.com'),
 'Q1 2026', 4,
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'Excellent curriculum development. Successfully launched 3 new elective programs with high student uptake.',
 'Complete accreditation process for 2 new courses. Publish academic research paper by year-end.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'),
 'Q1 2026', 4,
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'Strong IT infrastructure improvements. Uptime maintained at 99.7%. Rolled out new school ERP successfully.',
 'Complete cloud migration to AWS by Q2 2026. Achieve ISO 27001 security certification.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'meera.kapoor@schooly.com'),
 'Q1 2026', 4,
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'Efficient operations management. Campus facilities rating improved from 3.8 to 4.4 stars.',
 'Implement paperless office initiative. Reduce operational overhead by 10%.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'vikram.singh@schooly.com'),
 'Q1 2026', 3,
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'Consistent and reliable accounting. Audits passed without major findings. Needs to improve reporting speed.',
 'Get certified in IFRS standards. Reduce monthly closing time from 5 days to 3 days.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'ananya.joshi@schooly.com'),
 'Q1 2026', 4,
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'Excellent recruitment pipeline management. Cut time-to-hire from 45 to 28 days. Great candidate experience.',
 'Build automated LinkedIn sourcing pipeline. Target 95% offer acceptance rate.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'deepak.verma@schooly.com'),
 'Q1 2026', 4,
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'),
 'Excellent CI/CD pipeline improvements. Deployment frequency doubled with zero downtime incidents.',
 'Complete Kubernetes cluster migration. Achieve 99.99% pipeline reliability.')

ON CONFLICT DO NOTHING;

-- ─── STEP 9: Employee Documents ────────────────────────────────────────────────
INSERT INTO employee_documents (employee_id, document_type, file_name, file_url, uploaded_by, status)
SELECT
    sae.id,
    doc.doc_type,
    doc.file_name,
    'https://storage.schooly.com/employees/' || sae.id || '/' || doc.file_name,
    (SELECT sae2.id FROM super_admin_employees sae2 JOIN users u2 ON sae2.user_id = u2.id WHERE u2.email = 'priya.sharma@schooly.com' LIMIT 1),
    doc.status
FROM super_admin_employees sae
JOIN users u ON sae.user_id = u.id
CROSS JOIN (VALUES
    ('RESUME',            'resume_latest.pdf',         'VERIFIED'),
    ('NATIONAL_ID',       'aadhaar_card.pdf',          'VERIFIED'),
    ('DEGREE_CERTIFICATE','degree_certificate.pdf',    'VERIFIED')
) AS doc(doc_type, file_name, status)
WHERE u.email IN (
    'priya.sharma@schooly.com', 'arjun.mehta@schooly.com', 'sunita.rajput@schooly.com',
    'rajiv.nair@schooly.com', 'meera.kapoor@schooly.com', 'vikram.singh@schooly.com',
    'ananya.joshi@schooly.com', 'deepak.verma@schooly.com'
)
ON CONFLICT DO NOTHING;

-- Additional contract and bank statement documents
INSERT INTO employee_documents (employee_id, document_type, file_name, file_url, uploaded_by, status)
VALUES
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'EMPLOYMENT_CONTRACT', 'employment_contract_2022.pdf', 'https://storage.schooly.com/employees/contracts/priya_sharma_contract.pdf',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'VERIFIED'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'EMPLOYMENT_CONTRACT', 'employment_contract_2021.pdf', 'https://storage.schooly.com/employees/contracts/arjun_mehta_contract.pdf',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'VERIFIED'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'ananya.joshi@schooly.com'),
 'BANK_STATEMENT', 'bank_statement_q1_2026.pdf', 'https://storage.schooly.com/employees/bank/ananya_joshi_bank.pdf',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'ananya.joshi@schooly.com'),
 'PENDING'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'deepak.verma@schooly.com'),
 'BANK_STATEMENT', 'bank_statement_q1_2026.pdf', 'https://storage.schooly.com/employees/bank/deepak_verma_bank.pdf',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'deepak.verma@schooly.com'),
 'PENDING')

ON CONFLICT DO NOTHING;

-- ─── STEP 10: Employee Assets ──────────────────────────────────────────────────
INSERT INTO employee_assets (employee_id, asset_name, asset_type, serial_number, assigned_date, status, notes)
VALUES
-- Priya Sharma
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'Dell XPS 15 Laptop', 'Laptop', 'DXPS15-2024-PS01', NOW() - INTERVAL '2 years', 'ASSIGNED', 'Primary work laptop'),
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'LG 27" 4K Monitor', 'Monitor', 'LG27UK850-PS01', NOW() - INTERVAL '2 years', 'ASSIGNED', 'Office desk monitor'),

-- Arjun Mehta
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'MacBook Pro 16"', 'Laptop', 'MBPM2-2024-AM01', NOW() - INTERVAL '1 year', 'ASSIGNED', 'High-performance laptop'),
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'iPhone 15 Pro', 'Mobile', 'IPHONE15P-AM01', NOW() - INTERVAL '6 months', 'ASSIGNED', 'Company mobile device'),

-- Rajiv Nair
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'),
 'ThinkPad X1 Carbon', 'Laptop', 'TPKX1C-2024-RN01', NOW() - INTERVAL '1 year', 'ASSIGNED', 'IT administration laptop'),
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'),
 'Cisco Network Switch', 'Hardware', 'CISCO-SF350-RN01', NOW() - INTERVAL '18 months', 'ASSIGNED', 'Office floor network switch'),

-- Deepak Verma
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'deepak.verma@schooly.com'),
 'MacBook Air M3', 'Laptop', 'MBAM3-2024-DV01', NOW() - INTERVAL '3 months', 'ASSIGNED', 'Development machine'),
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'deepak.verma@schooly.com'),
 'Samsung 32" Curved Monitor', 'Monitor', 'SAMCF791-DV01', NOW() - INTERVAL '3 months', 'ASSIGNED', 'Dev environment monitor'),

-- Meera Kapoor
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'meera.kapoor@schooly.com'),
 'HP EliteBook 840', 'Laptop', 'HPEB840-2024-MK01', NOW() - INTERVAL '8 months', 'ASSIGNED', 'Operations laptop'),

-- Vikram Singh
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'vikram.singh@schooly.com'),
 'Dell Inspiron 15', 'Laptop', 'DINSP15-2024-VS01', NOW() - INTERVAL '1 year', 'ASSIGNED', 'Finance team laptop'),

-- Sunita Rajput
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'sunita.rajput@schooly.com'),
 'Surface Pro 9', 'Laptop', 'SURFPRO9-2024-SR01', NOW() - INTERVAL '6 months', 'ASSIGNED', 'Academic productivity device'),

-- Ananya Joshi
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'ananya.joshi@schooly.com'),
 'HP Spectre x360', 'Laptop', 'HPSPX360-2024-AJ01', NOW() - INTERVAL '4 months', 'ASSIGNED', 'Recruitment workstation'),

-- Super Admin
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'MacBook Pro M3 Max', 'Laptop', 'MBPM3MAX-2024-SA01', NOW() - INTERVAL '3 months', 'ASSIGNED', 'Primary admin machine')

ON CONFLICT DO NOTHING;

-- ─── STEP 11: Employee Lifecycle Events ────────────────────────────────────────
INSERT INTO employee_lifecycle (employee_id, event_type, event_date, description, created_by)
VALUES
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'JOINED', '2022-03-15 09:00:00',
 'Joined as HR Manager. Completed 3-day induction and system training.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'PROMOTION', '2023-06-01 09:00:00',
 'Promoted from HR Manager to HR Director following exceptional performance review and 18 months of outstanding contribution.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'JOINED', '2021-08-01 09:00:00',
 'Joined as Senior Finance Manager with focus on restructuring financial operations.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'PROMOTION', '2022-12-01 09:00:00',
 'Promoted to Chief Finance Officer. Led cost-reduction initiative saving ₹45 lakhs annually.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'),
 'JOINED', '2023-01-20 09:00:00',
 'Joined as Head of IT. Immediately began infrastructure audit and ERP migration planning.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'),
 'TRAINING', '2024-03-10 09:00:00',
 'Completed AWS Solutions Architect Professional certification training (5-day program).',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'sunita.rajput@schooly.com'),
 'JOINED', '2020-06-10 09:00:00',
 'Joined as Head of Academic Affairs. Brought 15 years of curriculum development experience.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'sunita.rajput@schooly.com'),
 'AWARD', '2023-12-15 12:00:00',
 'Received "Best Educator Award" at the National Academic Excellence Conclave 2023.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'deepak.verma@schooly.com'),
 'JOINED', '2024-02-01 09:00:00',
 'Joined as DevOps Engineer. Background in cloud infrastructure and CI/CD automation.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'meera.kapoor@schooly.com'),
 'JOINED', '2023-04-05 09:00:00',
 'Joined as Operations Manager. Responsible for all campus facility and operational workflows.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'))

ON CONFLICT DO NOTHING;

-- ─── STEP 12: Employee Notes ───────────────────────────────────────────────────
INSERT INTO employee_notes (employee_id, note_content, author_id)
VALUES
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'Priya consistently exceeds expectations. Has built a strong HR team from ground up. Excellent communication and conflict resolution skills. Ready for expanded executive responsibilities.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'Arjun is a strategic finance leader. His cost optimization initiative in Q4 2025 saved ₹38 lakhs. Strong stakeholder management. Consider for Board of Directors advisory role.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'sunita.rajput@schooly.com'),
 'Sunita brings exceptional academic vision. The 3 new elective programs she launched have a 94% enrollment rate. Her peer-reviewed research adds institutional credibility.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'),
 'Rajiv has successfully modernized our entire IT stack. Zero critical security incidents under his watch. His cloud migration proposal has been approved by leadership.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'meera.kapoor@schooly.com'),
 'Meera runs a very tight operations ship. All facility audits passed with commendation. She has reduced vendor costs by renegotiating 4 key contracts this year.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'vikram.singh@schooly.com'),
 'Vikram is reliable and diligent. Areas for growth: proactive communication with department heads about budget variances. Enrolled in advanced Excel and Power BI training.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'ananya.joshi@schooly.com'),
 'Ananya transformed our recruitment process. Candidate NPS improved from 42 to 78 under her management. Strong builder of inclusive hiring pipelines.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com')),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'deepak.verma@schooly.com'),
 'Deepak is a strong technical contributor. His CI/CD pipeline improvements cut deployment time from 45min to 8min. Still learning system architecture — showing great progress.',
 (SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'))

ON CONFLICT DO NOTHING;

-- ─── STEP 13: Employee Timeline ────────────────────────────────────────────────
INSERT INTO employee_timeline (employee_id, title, description, date)
VALUES
-- Priya Sharma
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'Joined Schooly', 'Completed onboarding. Started as HR Manager with team of 2.', '2022-03-15 09:00:00'),
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'Completed HR Certification', 'Obtained SHRM-CP certification — sponsored by the company.', '2022-09-20 09:00:00'),
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'Promoted to HR Director', 'Recognized for exceptional performance. Team grown to 6 members.', '2023-06-01 09:00:00'),

-- Arjun Mehta
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'Joined Schooly', 'Joined as Senior Finance Manager. Led Q4 budget planning immediately.', '2021-08-01 09:00:00'),
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'Promoted to CFO', 'Cost reduction initiative recognized. Appointed Chief Finance Officer.', '2022-12-01 09:00:00'),
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'Led ERP Financial Module Launch', 'Successfully launched automated payroll processing for all staff.', '2024-01-15 09:00:00'),

-- Sunita Rajput
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'sunita.rajput@schooly.com'),
 'Joined Schooly', 'Began as Dean of Academics. First action: curriculum quality audit.', '2020-06-10 09:00:00'),
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'sunita.rajput@schooly.com'),
 'Best Educator Award', 'National Academic Excellence Conclave 2023 — Best Educator recognition.', '2023-12-15 12:00:00'),

-- Rajiv Nair
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'),
 'Joined Schooly', 'Joined as Head of IT. Conducted full infrastructure vulnerability assessment.', '2023-01-20 09:00:00'),
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'),
 'AWS Certification Completed', 'AWS Solutions Architect Professional — completed 5-day immersive program.', '2024-03-10 09:00:00'),

-- Deepak Verma
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'deepak.verma@schooly.com'),
 'Joined Schooly', 'Onboarded as DevOps Engineer. Background in Kubernetes and GitOps.', '2024-02-01 09:00:00'),

-- Ananya Joshi
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'ananya.joshi@schooly.com'),
 'Joined Schooly', 'Joined as Recruitment Lead. Built ATS from scratch in first 60 days.', '2024-01-15 09:00:00')

ON CONFLICT DO NOTHING;

-- ─── STEP 14: Audit Logs ───────────────────────────────────────────────────────
INSERT INTO employee_audit_logs (employee_id, action, entity_type, entity_id, details)
VALUES
((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'CREATE_EMPLOYEE', 'SUPER_ADMIN_EMPLOYEE', 1,
 'Onboarded Priya Sharma as HR Manager. System access granted.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'CREATE_EMPLOYEE', 'SUPER_ADMIN_EMPLOYEE', 2,
 'Onboarded Arjun Mehta as Senior Finance Manager. Granted Finance module access.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'CREATE_EMPLOYEE', 'SUPER_ADMIN_EMPLOYEE', 5,
 'Onboarded Meera Kapoor as Operations Manager. Completed documentation check.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'UPDATE_EMPLOYEE_ROLE', 'SUPER_ADMIN_EMPLOYEE', 1,
 'Promoted Priya Sharma from HR Manager to HR Director. Band D salary upgrade applied.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'UPDATE_EMPLOYEE_ROLE', 'SUPER_ADMIN_EMPLOYEE', 2,
 'Promoted Arjun Mehta to Chief Finance Officer. Executive compensation package activated.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'LEAVE_APPROVED', 'EMPLOYEE_LEAVE', 1,
 'Approved sick leave for Priya Sharma (2 days). Medical certificate verified.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'LEAVE_APPROVED', 'EMPLOYEE_LEAVE', 2,
 'Approved vacation for Arjun Mehta (5 days). Business continuity plan reviewed.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'LEAVE_REJECTED', 'EMPLOYEE_LEAVE', 3,
 'Rejected Rajiv Nair leave request — insufficient notice period (less than 48 hours).'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'PAYROLL_RUN', 'EMPLOYEE_PAYROLL', 1,
 'Processed August 2026 payroll for 4 employees. Total payout: ₹6,25,700.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'DOCUMENT_VERIFIED', 'EMPLOYEE_DOCUMENT', 1,
 'Verified Priya Sharma employment documents: Resume, Aadhaar, Degree Certificate.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'ASSET_ASSIGNED', 'EMPLOYEE_ASSET', 1,
 'Assigned MacBook Pro M3 Max (SN: MBPM3MAX-2024-SA01) to System Administrator.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'),
 'ASSET_ASSIGNED', 'EMPLOYEE_ASSET', 6,
 'Assigned Cisco Network Switch to IT Infrastructure team under Rajiv Nair.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'PERFORMANCE_REVIEW_ADDED', 'EMPLOYEE_PERFORMANCE', 1,
 'Added Q1 2026 performance review for Priya Sharma. Rating: 5/5 — Outstanding.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'superadmin@schooly.com'),
 'DEPARTMENT_CREATED', 'DEPARTMENT', 1,
 'Created 5 departments: Administration, HR, Finance, Academic Affairs, IT & Technology.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'arjun.mehta@schooly.com'),
 'PAYROLL_STATUS_UPDATE', 'EMPLOYEE_PAYROLL', 2,
 'Updated Vikram Singh payroll status from PENDING to PAID. Payment date: today.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'NOTE_ADDED', 'EMPLOYEE_NOTE', 1,
 'Added manager note for Deepak Verma regarding CI/CD pipeline performance improvements.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'rajiv.nair@schooly.com'),
 'LIFECYCLE_EVENT_ADDED', 'EMPLOYEE_LIFECYCLE', 1,
 'Recorded AWS Certification training completion for Rajiv Nair.'),

((SELECT sae.id FROM super_admin_employees sae JOIN users u ON sae.user_id = u.id WHERE u.email = 'priya.sharma@schooly.com'),
 'LEAVE_APPROVED', 'EMPLOYEE_LEAVE', 6,
 'Approved 1-day casual leave for Vikram Singh. Administrative tasks completed.')

ON CONFLICT DO NOTHING;

-- ─── DONE ──────────────────────────────────────────────────────────────────────
-- Summary: 8 users, 9 employees, 5 departments, ~63 attendance records, 8 leaves,
-- 9 payrolls, 8 performance reviews, 28 documents, 13 assets, 10 lifecycle events,
-- 8 notes, 12 timeline entries, 18 audit log entries.
