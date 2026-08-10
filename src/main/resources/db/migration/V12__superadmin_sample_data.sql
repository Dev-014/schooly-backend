-- V12__superadmin_sample_data.sql
-- Pre-populates rich sample schools, audit logs, and module access for Super Admin operational testing.

-- 0. Synchronize sequence to prevent primary key collisions if explicit IDs were inserted previously
SELECT setval('school_id_seq', COALESCE((SELECT MAX(id) FROM schools), 0) + 10, false);
SELECT setval('audit_logs_id_seq', COALESCE((SELECT MAX(id) FROM audit_logs), 0) + 10, false);

-- 1. Insert 5 rich sample schools if not present
INSERT INTO schools (name, code, contact_email, contact_phone, address, status, subdomain, metadata)
VALUES
('Greenwood Academy', 'GREENWOOD', 'admin@greenwood.edu', '+1-555-0101', '1200 Campus Drive, New York, NY', 'Active', 'greenwood', '{"plan": "Enterprise", "onboardingStep": "Step 8: Activated", "healthStatus": "Healthy"}')
ON CONFLICT (code) DO NOTHING;

-- 2. Insert sample audit logs for operational history
INSERT INTO audit_logs (actor_name, action, resource_type, target_school_name, changes_json, ip_address, status)
VALUES
('Dr. Alistair Finch (Super Admin)', 'PLAN_UPGRADE_EXECUTE', 'Subscription', 'Greenwood Academy', '{"old_plan": "Professional", "new_plan": "Enterprise"}', '192.168.1.104', 'SUCCESS');

-- 3. Ensure module access exists for sample schools
INSERT INTO school_module_access (school_id, module_id, enabled)
SELECT s.id, m.id, true
FROM schools s, platform_modules m
WHERE s.code IN ('GREENWOOD')
  AND m.code IN ('ATTENDANCE', 'EXAMS', 'FINANCE', 'COMMUNICATION')
ON CONFLICT DO NOTHING;
