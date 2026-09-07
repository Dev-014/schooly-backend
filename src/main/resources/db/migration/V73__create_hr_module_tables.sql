-- V73__create_hr_module_tables.sql
-- Creates the foundational tables for the Admin Human Resource Module

-- 1. Departments and Designations
CREATE TABLE IF NOT EXISTS school_departments (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_school_dept_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE
);

CREATE INDEX idx_school_departments_school_id ON school_departments (school_id);

CREATE TABLE IF NOT EXISTS school_designations (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    department_id BIGINT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_school_desig_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_school_desig_dept FOREIGN KEY (department_id) REFERENCES school_departments (id) ON DELETE SET NULL
);

CREATE INDEX idx_school_designations_school_id ON school_designations (school_id);

-- 2. Staff Enhancements
-- staff table already has department_id and designation_id, but we need to add foreign keys 
-- and other missing fields.
ALTER TABLE staff ADD COLUMN IF NOT EXISTS biometric_id VARCHAR(100);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE staff ADD COLUMN IF NOT EXISTS gender VARCHAR(20);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS marital_status VARCHAR(50);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS father_name VARCHAR(255);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS mother_name VARCHAR(255);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS emergency_contact VARCHAR(100);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS current_address TEXT;
ALTER TABLE staff ADD COLUMN IF NOT EXISTS permanent_address TEXT;
ALTER TABLE staff ADD COLUMN IF NOT EXISTS qualification VARCHAR(255);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS work_experience TEXT;
ALTER TABLE staff ADD COLUMN IF NOT EXISTS contract_type VARCHAR(100);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS work_shift VARCHAR(100);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS location VARCHAR(255);
ALTER TABLE staff ADD COLUMN IF NOT EXISTS notes TEXT;

-- 3. Staff Bank Accounts
CREATE TABLE IF NOT EXISTS staff_bank_accounts (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL UNIQUE,
    account_holder_name VARCHAR(255),
    account_number VARCHAR(100),
    bank_name VARCHAR(255),
    ifsc_code VARCHAR(50),
    branch_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_bank_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE CASCADE
);

-- 4. Staff Payroll Details
CREATE TABLE IF NOT EXISTS staff_payroll_details (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL UNIQUE,
    basic_salary NUMERIC(19, 2) DEFAULT 0,
    hra NUMERIC(19, 2) DEFAULT 0,
    ta NUMERIC(19, 2) DEFAULT 0,
    da NUMERIC(19, 2) DEFAULT 0,
    special_allowance NUMERIC(19, 2) DEFAULT 0,
    pf NUMERIC(19, 2) DEFAULT 0,
    epf_number VARCHAR(100),
    tds NUMERIC(19, 2) DEFAULT 0,
    esic NUMERIC(19, 2) DEFAULT 0,
    other_deductions NUMERIC(19, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_payroll_details_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE CASCADE
);

-- 5. Staff Documents
CREATE TABLE IF NOT EXISTS staff_documents (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL,
    document_type VARCHAR(100) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_doc_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE CASCADE
);

CREATE INDEX idx_staff_documents_staff_id ON staff_documents (staff_id);

-- 6. Recruitment
CREATE TABLE IF NOT EXISTS recruitment_candidates (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255),
    date_of_birth DATE,
    applying_for VARCHAR(255),
    expected_salary NUMERIC(19, 2),
    marital_status VARCHAR(50),
    work_experience TEXT,
    interview_date TIMESTAMP,
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) DEFAULT 'NEW_APPLICATION',
    description TEXT,
    document_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_recruitment_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE
);

CREATE INDEX idx_recruitment_candidates_school_id ON recruitment_candidates (school_id);

-- 7. Leave Types
CREATE TABLE IF NOT EXISTS school_leave_types (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    days_allowed INT DEFAULT 0,
    is_paid BOOLEAN DEFAULT TRUE,
    applicable_roles VARCHAR(500), -- Comma separated roles if needed, or null for all
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_school_leave_type_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE
);

CREATE INDEX idx_school_leave_types_school_id ON school_leave_types (school_id);

-- 8. Staff Leaves
CREATE TABLE IF NOT EXISTS staff_leaves (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    number_of_days NUMERIC(5, 2) NOT NULL,
    reason TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    approved_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_leave_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_leave_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_leave_type FOREIGN KEY (leave_type_id) REFERENCES school_leave_types (id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_leave_approver FOREIGN KEY (approved_by) REFERENCES staff (id) ON DELETE SET NULL
);

CREATE INDEX idx_staff_leaves_school_id ON staff_leaves (school_id);
CREATE INDEX idx_staff_leaves_staff_id ON staff_leaves (staff_id);

-- 9. Staff Leave Balances
CREATE TABLE IF NOT EXISTS staff_leave_balances (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    leave_type_id BIGINT NOT NULL,
    total_leaves NUMERIC(5, 2) DEFAULT 0,
    used_leaves NUMERIC(5, 2) DEFAULT 0,
    remaining_leaves NUMERIC(5, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_leave_bal_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_leave_bal_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_leave_bal_type FOREIGN KEY (leave_type_id) REFERENCES school_leave_types (id) ON DELETE CASCADE,
    CONSTRAINT uk_staff_leave_bal UNIQUE (staff_id, leave_type_id)
);

CREATE INDEX idx_staff_leave_balances_staff_id ON staff_leave_balances (staff_id);

-- 10. Staff Attendance
CREATE TABLE IF NOT EXISTS staff_attendance (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    working_hours NUMERIC(5, 2),
    notes TEXT,
    is_correction_requested BOOLEAN DEFAULT FALSE,
    correction_status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_att_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_att_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE CASCADE,
    CONSTRAINT uk_staff_att_date UNIQUE (staff_id, attendance_date)
);

CREATE INDEX idx_staff_attendance_school_id ON staff_attendance (school_id);
CREATE INDEX idx_staff_attendance_staff_id ON staff_attendance (staff_id);
CREATE INDEX idx_staff_attendance_date ON staff_attendance (attendance_date);

-- 11. Staff Tasks
CREATE TABLE IF NOT EXISTS staff_tasks (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    start_date DATE,
    due_date DATE,
    priority VARCHAR(50) DEFAULT 'MEDIUM',
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_task_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_task_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE CASCADE
);

CREATE INDEX idx_staff_tasks_staff_id ON staff_tasks (staff_id);

-- 12. Staff Advances
CREATE TABLE IF NOT EXISTS staff_advances (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    advance_date DATE NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    reason TEXT,
    repayment_method VARCHAR(100),
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, PARTIALLY_RECOVERED, FULLY_RECOVERED
    recovered_amount NUMERIC(19, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_adv_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_adv_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE CASCADE
);

CREATE INDEX idx_staff_advances_staff_id ON staff_advances (staff_id);

-- 13. Payroll
CREATE TABLE IF NOT EXISTS staff_payrolls (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    payroll_month VARCHAR(20) NOT NULL,
    payroll_year INT NOT NULL,
    basic_salary NUMERIC(19, 2) NOT NULL,
    total_earnings NUMERIC(19, 2) NOT NULL,
    total_deductions NUMERIC(19, 2) NOT NULL,
    net_payable_salary NUMERIC(19, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    payment_date DATE,
    details_json JSONB, -- stores breakdown of allowances, deductions, attendance summary for history
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_payroll_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_payroll_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE CASCADE,
    CONSTRAINT uk_staff_payroll_month UNIQUE (staff_id, payroll_month, payroll_year)
);

CREATE INDEX idx_staff_payrolls_school_id ON staff_payrolls (school_id);
CREATE INDEX idx_staff_payrolls_staff_id ON staff_payrolls (staff_id);
