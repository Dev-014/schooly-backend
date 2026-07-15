-- V4__onboarding_and_auth_metadata.sql
-- Adds JSONB metadata, multi-tenant enhancements, super_admin/parent seed data, and nullable school_id for global roles.

-- 1. Add metadata and updated_at to schools (tenants)
ALTER TABLE schools
    ADD COLUMN IF NOT EXISTS subdomain VARCHAR(255) UNIQUE,
    ADD COLUMN IF NOT EXISTS metadata JSONB DEFAULT '{}'::jsonb,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 2. Add metadata and updated_at to users
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS metadata JSONB DEFAULT '{}'::jsonb,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 3. Add metadata to student and staff
ALTER TABLE student
    ADD COLUMN IF NOT EXISTS metadata JSONB DEFAULT '{}'::jsonb;

ALTER TABLE staff
    ADD COLUMN IF NOT EXISTS metadata JSONB DEFAULT '{}'::jsonb;

-- 4. Allow nullable school_id on user_school_roles for global roles (e.g. SUPER_ADMIN)
ALTER TABLE user_school_roles
    ALTER COLUMN school_id DROP NOT NULL;

-- 5. Seed Super Admin User
INSERT INTO users (phone, name, email, password_hash, status, metadata)
VALUES
('0000000000', 'Global Super Admin', 'superadmin@schooly.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ACTIVE', '{"isGlobalOwner": true}'::jsonb)
ON CONFLICT (phone) DO NOTHING;

INSERT INTO user_school_roles (user_id, school_id, role, status)
VALUES
((SELECT id FROM users WHERE phone = '0000000000'), NULL, 'SUPER_ADMIN', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- 6. Seed Multi-School User (for testing conditional school selection /select-school)
INSERT INTO users (phone, name, email, password_hash, status, metadata)
VALUES
('5555555555', 'David MultiSchool', 'david@multischool.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ACTIVE', '{"notes": "Multi-tenant test user"}'::jsonb)
ON CONFLICT (phone) DO NOTHING;

INSERT INTO user_school_roles (user_id, school_id, role, status)
VALUES
((SELECT id FROM users WHERE phone = '5555555555'), (SELECT id FROM schools WHERE code = 'GA001'), 'TEACHER', 'ACTIVE'),
((SELECT id FROM users WHERE phone = '5555555555'), (SELECT id FROM schools WHERE code = 'CHS002'), 'ADMIN', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- 7. Seed Parent User with Multiple Children (for testing conditional student selection /selectstudent)
INSERT INTO users (phone, name, email, password_hash, status, metadata)
VALUES
('6666666666', 'Rajesh Malhotra (Parent)', 'rajesh@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'ACTIVE', '{"relation": "Father"}'::jsonb)
ON CONFLICT (phone) DO NOTHING;

INSERT INTO user_school_roles (user_id, school_id, role, status)
VALUES
((SELECT id FROM users WHERE phone = '6666666666'), (SELECT id FROM schools WHERE code = 'GA001'), 'PARENT', 'ACTIVE')
ON CONFLICT DO NOTHING;

-- Insert second child for parent testing
INSERT INTO student (user_id, name, admission_no, roll_number, class_id, school_id, status, admission_date)
VALUES
(
  NULL,
  'Arjun Malhotra', 'ADM-2024-101', '201',
  (SELECT id FROM class WHERE name = 'Grade 10-A' LIMIT 1),
  (SELECT id FROM schools WHERE code = 'GA001'),
  'ACTIVE', '2024-01-01'
),
(
  NULL,
  'Sanya Malhotra', 'ADM-2024-102', '202',
  (SELECT id FROM class WHERE name = 'Grade 10-A' LIMIT 1),
  (SELECT id FROM schools WHERE code = 'GA001'),
  'ACTIVE', '2024-01-01'
)
ON CONFLICT DO NOTHING;

-- Link both students to the parent
INSERT INTO student_parents (student_id, parent_user_id, relation, is_primary)
VALUES
((SELECT id FROM student WHERE admission_no = 'ADM-2024-101'), (SELECT id FROM users WHERE phone = '6666666666'), 'Father', TRUE),
((SELECT id FROM student WHERE admission_no = 'ADM-2024-102'), (SELECT id FROM users WHERE phone = '6666666666'), 'Father', TRUE)
ON CONFLICT DO NOTHING;
