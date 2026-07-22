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
('ATTENDANCE', 'Attendance Management', 'Track daily student and staff attendance with instant notification capabilities', TRUE, 'ACTIVE', 'CORE', 0.00),
('EXAMS', 'Exams & Grading', 'Manage exams, marks, custom grading scales, evaluation criteria, and report cards', TRUE, 'ACTIVE', 'CORE', 0.00),
('FINANCE', 'Fee & Finance Management', 'Manage fee invoices, online payment gateway integration, collections, and expenses', TRUE, 'ACTIVE', 'FINANCE', 149.00),
('TRANSPORT', 'Transport & Bus Tracking', 'Manage routes, stops, buses, GPS bus tracking, and driver assignments', FALSE, 'ACTIVE', 'OPERATION', 99.00),
('LIBRARY', 'Library Management', 'Manage books, barcoding, issues, returns, and digital library cataloging', FALSE, 'ACTIVE', 'ACADEMIC', 49.00),
('LMS', 'Learning Management System (LMS)', 'Online courses, lesson planner, course curriculum, homework, and study materials', FALSE, 'ACTIVE', 'ACADEMIC', 199.00),
('BIOMETRIC', 'Biometric & RFID Attendance', 'Hardware integration for automated biometric and RFID student/staff check-ins', FALSE, 'ACTIVE', 'INTEGRATION', 129.00),
('HOSTEL', 'Hostel & Dormitory Management', 'Manage room allocations, dormitory warden logs, mess fees, and visitor passes', FALSE, 'ACTIVE', 'OPERATION', 79.00),
('COMMUNICATION', 'SMS & WhatsApp Gateway', 'Automated parent & staff notifications, circulars, and instant messaging alerts', TRUE, 'ACTIVE', 'INTEGRATION', 59.00),
('HR_PAYROLL', 'Staff HR & Payroll Processing', 'Staff leave management, salary computation, tax calculation, and pay slip generation', FALSE, 'ACTIVE', 'FINANCE', 119.00)
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
