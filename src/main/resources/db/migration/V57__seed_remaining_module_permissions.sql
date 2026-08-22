-- V57__seed_remaining_module_permissions.sql
-- Adds baseline view/edit permissions for the remaining 12 modules so they appear in the Roles & Permissions UI

INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES
-- CERTIFICATES
('perm_certificates_view', 'certificates.certificate.view', 'CERTIFICATES', 'certificate', 'view', 'View Certificates', 'Allows viewing certificates and ID cards', '["school", "class", "linked"]', false, true),
('perm_certificates_edit', 'certificates.certificate.edit', 'CERTIFICATES', 'certificate', 'edit', 'Manage Certificates', 'Allows generating and managing certificates and ID cards', '["school"]', true, true),

-- ONLINE_CLASS
('perm_onlineclass_view', 'online_class.session.view', 'ONLINE_CLASS', 'session', 'view', 'View Online Classes', 'Allows viewing scheduled online classes', '["school", "class", "section", "linked"]', false, true),
('perm_onlineclass_edit', 'online_class.session.edit', 'ONLINE_CLASS', 'session', 'edit', 'Manage Online Classes', 'Allows creating and starting online classes', '["school", "class", "section"]', false, true),

-- ONLINE_EXAMS
('perm_onlineexams_view', 'online_exams.exam.view', 'ONLINE_EXAMS', 'exam', 'view', 'View Online Exams', 'Allows viewing online exams and results', '["school", "class", "linked"]', false, true),
('perm_onlineexams_edit', 'online_exams.exam.edit', 'ONLINE_EXAMS', 'exam', 'edit', 'Manage Online Exams', 'Allows creating online exams and question banks', '["school", "class"]', true, true),

-- LESSON_MODULE
('perm_lesson_view', 'lesson.plan.view', 'LESSON_MODULE', 'plan', 'view', 'View Lesson Plans', 'Allows viewing lesson plans and syllabus', '["school", "class", "linked"]', false, true),
('perm_lesson_edit', 'lesson.plan.edit', 'LESSON_MODULE', 'plan', 'edit', 'Manage Lesson Plans', 'Allows creating and managing lesson plans', '["school", "class"]', false, true),

-- ACADEMICS
('perm_academics_view', 'academics.timetable.view', 'ACADEMICS', 'timetable', 'view', 'View Academics', 'Allows viewing classes, subjects, and timetables', '["school", "class", "linked"]', false, true),
('perm_academics_edit', 'academics.timetable.edit', 'ACADEMICS', 'timetable', 'edit', 'Manage Academics', 'Allows configuring classes, subjects, and timetables', '["school"]', true, true),

-- DOWNLOADS
('perm_downloads_view', 'downloads.material.view', 'DOWNLOADS', 'material', 'view', 'View Downloads', 'Allows viewing study materials and assignments', '["school", "class", "linked"]', false, true),
('perm_downloads_edit', 'downloads.material.edit', 'DOWNLOADS', 'material', 'edit', 'Manage Downloads', 'Allows uploading study materials', '["school", "class"]', false, true),

-- INCOME
('perm_income_view', 'income.transaction.view', 'INCOME', 'transaction', 'view', 'View Income', 'Allows viewing school income records', '["school"]', false, true),
('perm_income_edit', 'income.transaction.edit', 'INCOME', 'transaction', 'edit', 'Manage Income', 'Allows adding and managing income records', '["school"]', true, true),

-- LIBRARY
('perm_library_view', 'library.book.view', 'LIBRARY', 'book', 'view', 'View Library', 'Allows viewing library catalog and issued books', '["school", "linked"]', false, true),
('perm_library_edit', 'library.book.edit', 'LIBRARY', 'book', 'edit', 'Manage Library', 'Allows adding books and issuing/returning them', '["school"]', false, true),

-- EXPENSE
('perm_expense_view', 'expense.transaction.view', 'EXPENSE', 'transaction', 'view', 'View Expenses', 'Allows viewing school expenses', '["school"]', false, true),
('perm_expense_edit', 'expense.transaction.edit', 'EXPENSE', 'transaction', 'edit', 'Manage Expenses', 'Allows adding and managing expense records', '["school"]', true, true),

-- QUESTION_PAPER
('perm_questionpaper_view', 'question_paper.paper.view', 'QUESTION_PAPER', 'paper', 'view', 'View Question Papers', 'Allows viewing generated question papers', '["school", "class"]', false, true),
('perm_questionpaper_edit', 'question_paper.paper.edit', 'QUESTION_PAPER', 'paper', 'edit', 'Manage Question Papers', 'Allows generating question papers', '["school", "class"]', true, true),

-- DISCIPLINE
('perm_discipline_view', 'discipline.record.view', 'DISCIPLINE', 'record', 'view', 'View Discipline Records', 'Allows viewing student disciplinary records', '["school", "class", "linked"]', false, true),
('perm_discipline_edit', 'discipline.record.edit', 'DISCIPLINE', 'record', 'edit', 'Manage Discipline', 'Allows creating and managing disciplinary records', '["school", "class"]', true, true),

-- INVENTORY
('perm_inventory_view', 'inventory.stock.view', 'INVENTORY', 'stock', 'view', 'View Inventory', 'Allows viewing inventory and stock', '["school"]', false, true),
('perm_inventory_edit', 'inventory.stock.edit', 'INVENTORY', 'stock', 'edit', 'Manage Inventory', 'Allows managing stock, items, and vendors', '["school"]', true, true)

ON CONFLICT (permission_key) DO UPDATE SET
    module_key = EXCLUDED.module_key,
    name = EXCLUDED.name,
    description = EXCLUDED.description;
