-- V55__align_modules_with_frontend_sidebar.sql

-- 1. Insert or Update all exact frontend modules
INSERT INTO platform_modules (code, name, description, is_default, status, category, add_on_price)
VALUES
('FRONT_OFFICE', 'Front Office', 'Admission enquiries, visitor logs, and complaints', TRUE, 'ACTIVE', 'CORE', 0.00),
('STUDENTS', 'Students & Admission', 'Manage student admissions, reports, and categories', TRUE, 'ACTIVE', 'CORE', 0.00),
('CERTIFICATES', 'Certificates & ID Cards', 'Generate ID cards and certificates for students/staff', FALSE, 'ACTIVE', 'OPERATION', 49.00),
('ONLINE_CLASS', 'Online Class', 'Online classes, zoom integration, teacher timetable', FALSE, 'ACTIVE', 'ACADEMIC', 99.00),
('STAFF_HR', 'Teachers & Staff', 'Staff list, workloads, and payroll', TRUE, 'ACTIVE', 'HR', 0.00),
('ATTENDANCE', 'Attendance', 'Daily student attendance and leave management', TRUE, 'ACTIVE', 'CORE', 0.00),
('ONLINE_EXAMS', 'Online Exams', 'Create exams, question banks, and evaluate students online', FALSE, 'ACTIVE', 'ACADEMIC', 149.00),
('LESSON_MODULE', 'Lesson Module', 'Lesson planner, topics, and syllabus tracking', FALSE, 'ACTIVE', 'ACADEMIC', 49.00),
('EXAMS_RESULTS', 'Exams & Results', 'Offline exams, report cards, grade list, and marks entry', TRUE, 'ACTIVE', 'CORE', 0.00),
('HOMEWORK', 'Homework', 'Assign and evaluate daily classwork and homework', TRUE, 'ACTIVE', 'ACADEMIC', 0.00),
('ACADEMICS', 'Academic Module', 'Subjects, classes, sections, timetables, and promotion', TRUE, 'ACTIVE', 'CORE', 0.00),
('DOWNLOADS', 'Downloads Module', 'Study materials, syllabus, assignments, and videos', FALSE, 'ACTIVE', 'ACADEMIC', 29.00),
('FEES', 'Fees Management', 'Fee collection, installments, search due fees', TRUE, 'ACTIVE', 'FINANCE', 0.00),
('INCOME', 'Income Management', 'Track miscellaneous school incomes', TRUE, 'ACTIVE', 'FINANCE', 0.00),
('LIBRARY', 'Library', 'Books issue/return, and catalog management', FALSE, 'ACTIVE', 'OPERATION', 59.00),
('EXPENSE', 'Expense Management', 'Track operational expenses', TRUE, 'ACTIVE', 'FINANCE', 0.00),
('QUESTION_PAPER', 'Question Paper', 'Generate and repository question papers', FALSE, 'ACTIVE', 'ACADEMIC', 79.00),
('COMMUNICATION', 'Communication', 'Send SMS, email, WhatsApp, and manage logs', TRUE, 'ACTIVE', 'INTEGRATION', 49.00),
('DISCIPLINE', 'Discipline', 'Track disciplinary issues and assessments', FALSE, 'ACTIVE', 'OPERATION', 29.00),
('INVENTORY', 'Inventory', 'Manage stock, issues, and profit/loss', FALSE, 'ACTIVE', 'OPERATION', 89.00),
('SETTINGS', 'Settings', 'Roles, permissions, and academic setup', TRUE, 'ACTIVE', 'SYSTEM', 0.00)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    is_default = EXCLUDED.is_default,
    category = EXCLUDED.category,
    add_on_price = EXCLUDED.add_on_price;

-- 2. Clean up old mappings
DELETE FROM plan_modules;

-- 3. Remap subscription plans
INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM subscription_plans p, platform_modules m
WHERE p.code = 'FREE' AND m.code IN ('FRONT_OFFICE', 'STUDENTS', 'ATTENDANCE', 'ACADEMICS', 'SETTINGS')
ON CONFLICT DO NOTHING;

INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM subscription_plans p, platform_modules m
WHERE p.code = 'GROWTH' AND m.code IN ('FRONT_OFFICE', 'STUDENTS', 'ATTENDANCE', 'ACADEMICS', 'SETTINGS', 'EXAMS_RESULTS', 'HOMEWORK', 'FEES', 'COMMUNICATION')
ON CONFLICT DO NOTHING;

INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM subscription_plans p, platform_modules m
WHERE p.code = 'PREMIUM' AND m.code IN ('FRONT_OFFICE', 'STUDENTS', 'ATTENDANCE', 'ACADEMICS', 'SETTINGS', 'EXAMS_RESULTS', 'HOMEWORK', 'FEES', 'COMMUNICATION', 'STAFF_HR', 'INCOME', 'EXPENSE', 'LIBRARY', 'INVENTORY', 'DISCIPLINE')
ON CONFLICT DO NOTHING;

INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM subscription_plans p, platform_modules m
WHERE p.code = 'ENTERPRISE'
ON CONFLICT DO NOTHING;

-- 4. Update existing Permission Definitions to map to these new modules
DELETE FROM role_permissions;
DELETE FROM permission_definitions;

INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES
('perm_attendance_view', 'attendance.attendance_record.view', 'ATTENDANCE', 'attendance_record', 'view', 'View Attendance', 'Allows viewing of student attendance records', '["school", "assigned", "class", "section", "linked"]', false, true),
('perm_attendance_edit', 'attendance.attendance_record.edit', 'ATTENDANCE', 'attendance_record', 'edit', 'Edit Attendance', 'Allows modification of student attendance records', '["school", "assigned", "class", "section"]', true, true),

('perm_student_view', 'student.student.view', 'STUDENTS', 'student', 'view', 'View Student', 'Allows viewing student profiles', '["school", "assigned", "class", "section", "linked"]', false, true),
('perm_student_edit', 'student.student.edit', 'STUDENTS', 'student', 'edit', 'Edit Student', 'Allows editing student profiles', '["school", "assigned"]', false, true),
('perm_student_add', 'student.student.add', 'STUDENTS', 'student', 'add', 'Admit Student', 'Allows admitting new students', '["school"]', false, true),

('perm_fee_view', 'fees.fee_collection.view', 'FEES', 'fee_collection', 'view', 'View Fee Collections', 'Allows viewing fee transactions', '["school", "linked"]', false, true),
('perm_fee_edit', 'fees.fee_collection.edit', 'FEES', 'fee_collection', 'edit', 'Collect Fees', 'Allows collecting fees', '["school"]', true, true),
('perm_fee_refund', 'fees.fee_collection.refund', 'FEES', 'fee_collection', 'refund', 'Refund Fees', 'Allows processing fee refunds', '["school"]', true, true),

('perm_exam_view', 'exams.marks.view', 'EXAMS_RESULTS', 'marks', 'view', 'View Exam Marks', 'Allows viewing student exam marks', '["school", "class", "linked"]', false, true),
('perm_exam_edit', 'exams.marks.edit', 'EXAMS_RESULTS', 'marks', 'edit', 'Enter Exam Marks', 'Allows entering marks for exams', '["school", "class"]', true, true),

('perm_homework_view', 'homework.assignment.view', 'HOMEWORK', 'assignment', 'view', 'View Homework', 'Allows viewing homework assignments', '["school", "class", "linked"]', false, true),
('perm_homework_edit', 'homework.assignment.edit', 'HOMEWORK', 'assignment', 'edit', 'Assign Homework', 'Allows assigning homework', '["school", "class"]', false, true),

('perm_frontoffice_view', 'frontoffice.visitor.view', 'FRONT_OFFICE', 'visitor', 'view', 'View Visitors', 'Allows viewing visitor log', '["school"]', false, true),
('perm_frontoffice_edit', 'frontoffice.visitor.edit', 'FRONT_OFFICE', 'visitor', 'edit', 'Manage Front Office', 'Allows managing front office entries', '["school"]', false, true),

('perm_hr_view', 'hr.staff.view', 'STAFF_HR', 'staff', 'view', 'View Staff', 'Allows viewing staff list', '["school"]', false, true),
('perm_hr_edit', 'hr.staff.edit', 'STAFF_HR', 'staff', 'edit', 'Manage Staff', 'Allows managing staff HR and payroll', '["school"]', true, true),

('perm_communication_send', 'communication.message.send', 'COMMUNICATION', 'message', 'send', 'Send Communications', 'Allows sending SMS/Emails to parents', '["school", "class"]', true, true),

('perm_settings_edit', 'settings.academic.edit', 'SETTINGS', 'academic', 'edit', 'Manage Settings', 'Allows editing global school settings', '["school"]', true, true)
ON CONFLICT (permission_key) DO UPDATE SET
    module_key = EXCLUDED.module_key,
    name = EXCLUDED.name,
    description = EXCLUDED.description;
