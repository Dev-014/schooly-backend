-- V7__create_missing_tables_and_seed_superadmin.sql
-- Creates all missing database tables required by JPA entities for Hibernate schema validation:
-- audit_logs, subscription_plans, platform_modules, plan_modules, school_module_access, school_config_overrides

-- 1. Create audit_logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_id BIGINT,
    actor_name VARCHAR(255),
    action VARCHAR(255) NOT NULL,
    resource_type VARCHAR(255),
    resource_id BIGINT,
    target_school_id BIGINT,
    target_school_name VARCHAR(255),
    changes_json TEXT,
    ip_address VARCHAR(255),
    user_agent VARCHAR(255),
    status VARCHAR(50) DEFAULT 'SUCCESS',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_actor_id ON audit_logs (actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_action ON audit_logs (action);
CREATE INDEX IF NOT EXISTS idx_audit_school_id ON audit_logs (target_school_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON audit_logs (timestamp);

-- 2. Create subscription_plans table
CREATE TABLE IF NOT EXISTS subscription_plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    monthly_price NUMERIC(10, 2),
    max_students INTEGER,
    storage_gb INTEGER,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Create platform_modules table
CREATE TABLE IF NOT EXISTS platform_modules (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_default BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Create plan_modules join table (ManyToMany relation between SubscriptionPlan and PlatformModule)
CREATE TABLE IF NOT EXISTS plan_modules (
    plan_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    PRIMARY KEY (plan_id, module_id),
    CONSTRAINT fk_plan_modules_plan FOREIGN KEY (plan_id) REFERENCES subscription_plans (id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_modules_module FOREIGN KEY (module_id) REFERENCES platform_modules (id) ON DELETE CASCADE
);

-- 5. Create school_module_access table
CREATE TABLE IF NOT EXISTS school_module_access (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    enabled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_school_module_access_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_school_module_access_module FOREIGN KEY (module_id) REFERENCES platform_modules (id) ON DELETE CASCADE
);

-- 6. Create school_config_overrides table
CREATE TABLE IF NOT EXISTS school_config_overrides (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL UNIQUE,
    student_limit INTEGER,
    enable_beta_features BOOLEAN DEFAULT FALSE,
    custom_config TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_school_config_overrides_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE
);

-- 7. Seed initial Platform Modules
INSERT INTO platform_modules (code, name, description, is_default, status)
VALUES
('ATTENDANCE', 'Attendance Management', 'Track daily student and staff attendance', TRUE, 'ACTIVE'),
('EXAMS', 'Exams & Grading', 'Manage exams, marks, and report cards', TRUE, 'ACTIVE'),
('TRANSPORT', 'Transport Management', 'Manage routes, buses, and driver assignments', FALSE, 'ACTIVE'),
('FINANCE', 'Fee & Finance Management', 'Manage fee invoices, collections, and expenses', TRUE, 'ACTIVE')
ON CONFLICT (code) DO NOTHING;

-- 8. Seed initial Subscription Plans
INSERT INTO subscription_plans (name, monthly_price, max_students, storage_gb, status)
VALUES
('Basic Plan', 49.99, 200, 10, 'ACTIVE'),
('Standard Plan', 99.99, 1000, 50, 'ACTIVE'),
('Premium Enterprise Plan', 249.99, 5000, 250, 'ACTIVE')
ON CONFLICT DO NOTHING;
