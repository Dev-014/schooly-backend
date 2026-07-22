-- V12__superadmin_sample_data.sql
-- Pre-populates rich sample schools, audit logs, and module access for Super Admin operational testing.

-- 0. Synchronize sequence to prevent primary key collisions if explicit IDs were inserted previously
SELECT setval('school_id_seq', COALESCE((SELECT MAX(id) FROM schools), 0) + 10, false);
SELECT setval('audit_logs_id_seq', COALESCE((SELECT MAX(id) FROM audit_logs), 0) + 10, false);

-- 1. Insert 5 rich sample schools if not present
INSERT INTO schools (name, code, contact_email, contact_phone, address, status, subdomain, metadata)
VALUES
('Greenwood International Academy', 'GREENWOOD', 'admin@greenwood.edu', '+1-555-0101', '1200 Campus Drive, New York, NY', 'Active', 'greenwood', '{"plan": "Enterprise", "onboardingStep": "Step 8: Activated", "healthStatus": "Healthy"}'),
('St. Xavier''s High School', 'STXAVIER', 'principal@stxaviers.org', '+1-555-0102', '450 Michigan Ave, Chicago, IL', 'Active', 'stxavier', '{"plan": "Professional", "onboardingStep": "Step 8: Activated", "healthStatus": "Healthy"}'),
('Delhi Public School Main Campus', 'DPSMAIN', 'info@dpsmain.edu', '+1-555-0103', '789 Silicon Blvd, Seattle, WA', 'Active', 'dpsmain', '{"plan": "Enterprise", "onboardingStep": "Step 8: Activated", "healthStatus": "Healthy"}'),
('Oakridge International School', 'OAKRIDGE', 'admissions@oakridge.edu', '+1-555-0104', '320 Austin Parkway, Austin, TX', 'Trial', 'oakridge', '{"plan": "Professional", "onboardingStep": "Step 5: Data Import", "healthStatus": "Warning"}'),
('Springfield Elementary Academy', 'SPRINGFIELD', 'contact@springfield.org', '+1-555-0105', '742 Evergreen Terrace, Springfield, OR', 'Active', 'springfield', '{"plan": "Essential Academy", "onboardingStep": "Step 8: Activated", "healthStatus": "Healthy"}')
ON CONFLICT (code) DO NOTHING;

-- 2. Insert sample audit logs for operational history
INSERT INTO audit_logs (actor_name, action, resource_type, target_school_name, changes_json, ip_address, status)
VALUES
('Dr. Alistair Finch (Super Admin)', 'PLAN_UPGRADE_EXECUTE', 'Subscription', 'Greenwood International Academy', '{"old_plan": "Professional", "new_plan": "Enterprise"}', '192.168.1.104', 'SUCCESS'),
('System Sync Daemon', 'DATA_IMPORT_COMPLETE', 'Student Register', 'St. Xavier''s High School', '{"imported_records": 450, "failed_records": 0}', '10.0.1.24', 'SUCCESS'),
('Elena Rostova (School Administrator)', 'AUTH_LOGIN_ATTEMPT', 'Authentication', 'Delhi Public School Main Campus', '{"reason": "Invalid OTP verification token"}', '172.16.4.88', 'FAILED'),
('Marcus Vance (Principal / HOD)', 'FEE_INVOICE_GENERATE', 'Finance', 'Oakridge International School', '{"invoices_created": 320, "total_amount": 145000}', '192.168.2.15', 'SUCCESS'),
('Dr. Alistair Finch (Super Admin)', 'MODULE_TOGGLE_APPLY', 'Module Access', 'Springfield Elementary Academy', '{"module": "ONLINE_CLASSES", "enabled": true}', '192.168.1.104', 'SUCCESS');

-- 3. Ensure module access exists for sample schools
INSERT INTO school_module_access (school_id, module_id, enabled)
SELECT s.id, m.id, true
FROM schools s, platform_modules m
WHERE s.code IN ('GREENWOOD', 'STXAVIER', 'DPSMAIN', 'OAKRIDGE', 'SPRINGFIELD')
  AND m.code IN ('ATTENDANCE', 'EXAMS', 'FINANCE', 'COMMUNICATION')
ON CONFLICT DO NOTHING;
