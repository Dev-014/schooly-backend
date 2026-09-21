-- V13__comprehensive_erp_modules.sql
-- Comprehensive ERP Module Standardization across all roles (Admin, Teacher, Student, Parent)
-- Adds target_roles and sub_modules to platform_modules and links subscription plans accurately.

ALTER TABLE platform_modules ADD COLUMN IF NOT EXISTS target_roles VARCHAR(255) DEFAULT 'ADMIN';
ALTER TABLE platform_modules ADD COLUMN IF NOT EXISTS sub_modules TEXT;

-- Seed and update 18 comprehensive Platform Modules
INSERT INTO platform_modules (code, name, description, is_default, status, category, add_on_price, target_roles, sub_modules)
VALUES
('ADMIN_SETUP', 'School Configuration & Administration', 'Manage roles & permissions, academic year & term setup, section structure, and ID/certificate configuration', TRUE, 'ACTIVE', 'CORE', 0.00, 'ADMIN,SUPER_ADMIN', '["Roles & Permissions", "Academic Year & Term Setup", "Section & Class Structure", "ID Cards & Certificates Configuration"]'),
('FRONT_OFFICE', 'Front Desk & Visitor Management', 'Manage admission enquiries, visitor book, gate passes, parcel logs, and postal complaint registers', TRUE, 'ACTIVE', 'CORE', 499.00, 'ADMIN', '["Admission Enquiry", "Visitor Book & Gate Pass", "Parcel Receive & Dispatch", "Complaints & Postal Records"]'),
('STUDENT_INFO', 'Student Information & Lifecycle', 'Manage student directory, admissions, house allocations, student categories, and sibling links', TRUE, 'ACTIVE', 'ACADEMIC', 0.00, 'ADMIN,TEACHER', '["Student Directory & Details", "Student Admission & Online Admission", "Student Promotion & House Allocation", "Student Categories & Referrals", "Link Siblings"]'),
('ACADEMIC_OPERATIONS', 'Timetable, Subjects & Workload', 'Configure subjects, class teacher assignments, daily schedules, and teacher workload allocations', TRUE, 'ACTIVE', 'ACADEMIC', 799.00, 'ADMIN,TEACHER,STUDENT', '["Subjects Management & Class Assignment", "Class Teacher Assignment", "Class Timetable & Daily Timetable", "Teacher Timetable & Workload", "Student Timetable"]'),
('LMS_CLASSWORK', 'LMS, Lesson Planner & Homework', 'Create lesson plans, course curriculums, topic progress tracking, homework assignments, and study resources', FALSE, 'ACTIVE', 'ACADEMIC', 1499.00, 'ADMIN,TEACHER,STUDENT', '["Lesson Planner & Course Curriculum", "Topic Management & Progress Reports", "Homework & Classwork Assignment", "Student Submissions & Portfolio", "Study Materials & Video Resources"]'),
('EXAMS_GRADING', 'Exams, Assessments & Report Cards', 'Manage exam schedules, admit cards, marks entry, co-curricular grades, teacher remarks, and report cards', TRUE, 'ACTIVE', 'ACADEMIC', 0.00, 'ADMIN,TEACHER,STUDENT,PARENT', '["Exam Setup & Schedules", "Admit Cards Generation", "Marks Entry & Co-Curricular Grades", "Teacher Remarks & Grade Lists", "Report Cards & Examination Reports"]'),
('ONLINE_EXAMS', 'Computer-Based Testing & Question Bank', 'Build comprehensive question repositories and administer live online computer-based tests with instant grading', FALSE, 'ACTIVE', 'ACADEMIC', 999.00, 'ADMIN,TEACHER,STUDENT', '["Question Bank & Repository", "Create & Manage Online Exams", "Online Exam Evaluation & Reports"]'),
('FEES_FINANCE', 'Student Fees & Collections', 'Manage fee structures, installment cycles, demand notices, online payment gateway integration, and receipts', TRUE, 'ACTIVE', 'FINANCE', 1499.00, 'ADMIN,STUDENT,PARENT', '["Fee Collection & POS", "Fee Structure & Installments", "Due Fee Search & Demand Notices", "Payment Receipts & Online Gateway"]'),
('ACCOUNTING', 'Income & Expense Accounting', 'Track institutional income heads, daily expenses, voucher creation, and comprehensive financial search/reports', FALSE, 'ACTIVE', 'FINANCE', 899.00, 'ADMIN', '["Income Heads & Add Income", "Expense Heads & Add Expense", "Financial Search & Reports"]'),
('HR_PAYROLL', 'Staff Management & Payroll', 'Maintain staff directory, profiles, staff leave registers, salary computations, and payslip generation', FALSE, 'ACTIVE', 'FINANCE', 1299.00, 'ADMIN,TEACHER', '["Staff Directory & HR Profiles", "Staff Attendance & Leave Register", "Payroll Computation & Payslips"]'),
('ATTENDANCE_LEAVE', 'Student & Staff Attendance', 'Register daily student/staff attendance, online class attendance tracking, leave requests, and parent portal views', TRUE, 'ACTIVE', 'OPERATION', 0.00, 'ADMIN,TEACHER,STUDENT,PARENT', '["Daily Student Attendance Register", "Online Class Attendance Logs", "Student & Staff Leave Applications", "Attendance Reports & Parent Portal View"]'),
('LIBRARY', 'Library & Circulation Management', 'Maintain book catalog, barcoding, member directories, issue & return dashboard, and overdue circulation fines', FALSE, 'ACTIVE', 'OPERATION', 599.00, 'ADMIN,STUDENT', '["Book Catalog & List", "Member Directory & Cards", "Issue & Return Dashboard", "Circulation & Fine Reports"]'),
('INVENTORY', 'Store & Asset Management', 'Catalog institutional stock items, manage stock issues, track purchases, and generate profit & loss stock reports', FALSE, 'ACTIVE', 'OPERATION', 699.00, 'ADMIN', '["Items Management & Catalog", "Stock Issue & Purchase", "Profit & Loss Stock Report"]'),
('TRANSPORT', 'Fleet & Bus Tracking', 'Configure bus routes, stops, vehicle & driver assignments, and enable live GPS bus tracking for parents and students', FALSE, 'ACTIVE', 'OPERATION', 1199.00, 'ADMIN,PARENT,STUDENT', '["Bus Routes & Stops Management", "Driver & Vehicle Assignment", "Live GPS Bus Tracking"]'),
('HOSTEL', 'Hostel & Dormitory Management', 'Manage residential quarters, room allocations, dormitory warden logs, mess fee mapping, and visitor passes', FALSE, 'ACTIVE', 'OPERATION', 999.00, 'ADMIN,STUDENT', '["Room & Bed Allocations", "Warden Logs & Visitor Passes", "Mess Routine & Fee Mapping"]'),
('COMMUNICATION', 'Messaging & Notifications Hub', 'Send SMS & WhatsApp circulars, automated alerts, circular history logs, and parent communications center', TRUE, 'ACTIVE', 'INTEGRATION', 599.00, 'ADMIN,TEACHER,PARENT', '["Send SMS / WhatsApp Circulars", "Message History & Delivery Logs", "Parent Communications Hub"]'),
('ONLINE_CLASSES', 'Live Virtual Classrooms', 'Integrate video meeting links, schedule live online classes, and synchronize teacher & student online timetables', FALSE, 'ACTIVE', 'INTEGRATION', 899.00, 'ADMIN,TEACHER,STUDENT', '["Online Classes Setup & Video Links", "Teacher & Student Online Timetable"]'),
('BIOMETRIC', 'Biometric & RFID Hardware', 'Hardware integration for automated biometric attendance check-ins and RFID smart gate passes', FALSE, 'ACTIVE', 'INTEGRATION', 1499.00, 'ADMIN', '["Hardware Device Configuration", "Automated RFID Gate Logs", "Biometric Attendance Sync"]')
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_default = EXCLUDED.is_default,
    category = EXCLUDED.category,
    add_on_price = EXCLUDED.add_on_price,
    target_roles = EXCLUDED.target_roles,
    sub_modules = EXCLUDED.sub_modules;

-- Clean up plan_modules for re-linking
DELETE FROM plan_modules WHERE plan_id IN (SELECT id FROM subscription_plans WHERE code IN ('FREE', 'GROWTH', 'PREMIUM', 'ENTERPRISE'));

-- Link Free Starter Plan (FREE): ADMIN_SETUP, STUDENT_INFO, ATTENDANCE_LEAVE, EXAMS_GRADING
INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM subscription_plans p, platform_modules m
WHERE p.code = 'FREE' AND m.code IN ('ADMIN_SETUP', 'STUDENT_INFO', 'ATTENDANCE_LEAVE', 'EXAMS_GRADING')
ON CONFLICT DO NOTHING;

-- Link Pro Growth Plan (GROWTH): All Free Starter + FRONT_OFFICE, ACADEMIC_OPERATIONS, FEES_FINANCE, COMMUNICATION
INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM subscription_plans p, platform_modules m
WHERE p.code = 'GROWTH' AND m.code IN ('ADMIN_SETUP', 'STUDENT_INFO', 'ATTENDANCE_LEAVE', 'EXAMS_GRADING', 'FRONT_OFFICE', 'ACADEMIC_OPERATIONS', 'FEES_FINANCE', 'COMMUNICATION')
ON CONFLICT DO NOTHING;

-- Link Premium Partner Plan (PREMIUM): All Pro Growth + LMS_CLASSWORK, ONLINE_EXAMS, ACCOUNTING, HR_PAYROLL, LIBRARY, INVENTORY, ONLINE_CLASSES
INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM subscription_plans p, platform_modules m
WHERE p.code = 'PREMIUM' AND m.code IN ('ADMIN_SETUP', 'STUDENT_INFO', 'ATTENDANCE_LEAVE', 'EXAMS_GRADING', 'FRONT_OFFICE', 'ACADEMIC_OPERATIONS', 'FEES_FINANCE', 'COMMUNICATION', 'LMS_CLASSWORK', 'ONLINE_EXAMS', 'ACCOUNTING', 'HR_PAYROLL', 'LIBRARY', 'INVENTORY', 'ONLINE_CLASSES')
ON CONFLICT DO NOTHING;

-- Link Enterprise Custom Plan (ENTERPRISE): All 18 Modules
INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM subscription_plans p, platform_modules m
WHERE p.code = 'ENTERPRISE'
ON CONFLICT DO NOTHING;
