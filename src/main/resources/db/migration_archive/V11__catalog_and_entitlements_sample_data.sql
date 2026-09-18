-- V11__catalog_and_entitlements_sample_data.sql
-- Supports dynamic Catalog loading, Three-Tier Entitlement Engine (Overrides -> Plans -> Fallback), and sample base data.

-- 1. Add new columns to platform_modules if they do not exist
ALTER TABLE platform_modules ADD COLUMN IF NOT EXISTS category VARCHAR(100) DEFAULT 'CORE';
ALTER TABLE platform_modules ADD COLUMN IF NOT EXISTS add_on_price NUMERIC(10, 2) DEFAULT 0.00;

-- 2. Add new columns to subscription_plans if they do not exist
ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS code VARCHAR(100);
ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS annual_price NUMERIC(10, 2);
ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS features TEXT;

-- 3. Create tenant_entitlement_overrides table for 14-day trials and add-on module purchases
CREATE TABLE IF NOT EXISTS tenant_entitlement_overrides (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    module_code VARCHAR(100) NOT NULL,
    override_type VARCHAR(50) NOT NULL, -- 'TRIAL', 'ADD_ON', 'CUSTOM'
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tenant_entitlement_overrides_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_tenant_entitlement_overrides_school ON tenant_entitlement_overrides (school_id);
CREATE INDEX IF NOT EXISTS idx_tenant_entitlement_overrides_module ON tenant_entitlement_overrides (module_code);

-- 4. Seed / update comprehensive Platform Modules
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
    category = EXCLUDED.category,
    add_on_price = EXCLUDED.add_on_price;

-- 5. Seed standard Subscription Plans
UPDATE subscription_plans SET code = 'BASIC_LEGACY' WHERE name = 'Basic Plan' AND code IS NULL;
UPDATE subscription_plans SET code = 'STANDARD_LEGACY' WHERE name = 'Standard Plan' AND code IS NULL;
UPDATE subscription_plans SET code = 'PREMIUM_LEGACY' WHERE name = 'Premium Enterprise Plan' AND code IS NULL;

INSERT INTO subscription_plans (name, code, monthly_price, annual_price, max_students, storage_gb, status, description, features)
VALUES
('Free Starter', 'FREE', 0.00, 0.00, 100, 5, 'ACTIVE', 'Basic entry-level package ideal for small elementary schools or trial setups.', '["Student & Staff Directory", "Basic Attendance Tracking", "Standard Report Cards", "Up to 100 Students"]'),
('Pro Growth', 'GROWTH', 1499.00, 14999.00, 500, 50, 'ACTIVE', 'Comprehensive ERP suite for growing schools requiring advanced finance and communication.', '["All Starter Features", "Fee & Invoice Management", "SMS/WhatsApp Alerts", "Parent Portal & App", "Up to 500 Students"]'),
('Premium Partner', 'PREMIUM', 3099.00, 30999.00, 1500, 200, 'ACTIVE', 'Advanced multi-module platform with bus tracking, library, LMS, and white-labeling.', '["All Pro Growth Features", "Transport & Bus Tracking", "Library & LMS Modules", "HR & Payroll Processing", "Dedicated Account Manager", "Up to 1500 Students"]'),
('Enterprise Custom', 'ENTERPRISE', 8999.00, 89999.00, 99999, 1000, 'ACTIVE', 'Unlimited custom institutional suite with biometric integration and multi-branch support.', '["Unlimited Students & Storage", "Biometric & RFID Attendance", "Custom Form Fields (JSONB)", "Priority 24/7 SLA & Support", "Multi-branch Consolidation"]')
ON CONFLICT DO NOTHING;

-- 6. Link Plans to Modules in plan_modules
INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM subscription_plans p, platform_modules m
WHERE p.code = 'FREE' AND m.code IN ('ATTENDANCE', 'EXAMS')
ON CONFLICT DO NOTHING;

INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM subscription_plans p, platform_modules m
WHERE p.code = 'GROWTH' AND m.code IN ('ATTENDANCE', 'EXAMS', 'FINANCE', 'COMMUNICATION')
ON CONFLICT DO NOTHING;

INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM subscription_plans p, platform_modules m
WHERE p.code = 'PREMIUM' AND m.code IN ('ATTENDANCE', 'EXAMS', 'FINANCE', 'COMMUNICATION', 'TRANSPORT', 'LIBRARY', 'LMS', 'HR_PAYROLL')
ON CONFLICT DO NOTHING;

INSERT INTO plan_modules (plan_id, module_id)
SELECT p.id, m.id FROM subscription_plans p, platform_modules m
WHERE p.code = 'ENTERPRISE'
ON CONFLICT DO NOTHING;
