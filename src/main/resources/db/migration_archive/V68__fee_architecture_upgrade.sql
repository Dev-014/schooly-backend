-- Create School Account table
CREATE TABLE school_account (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id),
    account_name VARCHAR(255) NOT NULL,
    account_type VARCHAR(50) NOT NULL, -- BANK, UPI, CASH
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_school_account_school_id ON school_account(school_id);

-- Add account_id to fee_payment
ALTER TABLE fee_payment ADD COLUMN account_id BIGINT REFERENCES school_account(id);

-- Create Fee Installment table
CREATE TABLE fee_installment (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id),
    academic_year_id BIGINT REFERENCES academic_years(id),
    name VARCHAR(255) NOT NULL,
    start_date DATE,
    due_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fee_installment_school_id ON fee_installment(school_id);

-- Create Fee Installment Item table (ties installment to categories for the base structure)
CREATE TABLE fee_installment_item (
    id BIGSERIAL PRIMARY KEY,
    fee_installment_id BIGINT NOT NULL REFERENCES fee_installment(id) ON DELETE CASCADE,
    fee_category_id BIGINT NOT NULL REFERENCES fee_category(id),
    amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Student Fee Structure table (links student to a specific structure for an academic year)
CREATE TABLE student_fee_structure (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES student(id),
    fee_structure_id BIGINT NOT NULL REFERENCES fee_structure(id),
    academic_year_id BIGINT REFERENCES academic_years(id),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sfs_student_id ON student_fee_structure(student_id);

-- Modify FeeDue table to support Ad-Hoc fees and link to installments
ALTER TABLE fee_due ADD COLUMN fee_installment_id BIGINT REFERENCES fee_installment(id);
ALTER TABLE fee_due ADD COLUMN is_ad_hoc BOOLEAN DEFAULT false;

