-- V111__create_accounting_tables.sql
-- Foundational tables for Accounting: Income and Expense

CREATE TABLE IF NOT EXISTS income_heads (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_income_head_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS incomes (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    income_head_id BIGINT NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    account_name VARCHAR(255),
    income_from VARCHAR(255),
    invoice_number VARCHAR(100),
    amount NUMERIC(10, 2) NOT NULL,
    date DATE NOT NULL,
    payment_mode VARCHAR(50),
    document_url VARCHAR(500),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_income_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_income_head FOREIGN KEY (income_head_id) REFERENCES income_heads (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS expense_heads (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expense_head_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    expense_head_id BIGINT NOT NULL,
    account_type VARCHAR(50) NOT NULL,
    account_name VARCHAR(255),
    vendor_name VARCHAR(255),
    invoice_number VARCHAR(100),
    amount NUMERIC(10, 2) NOT NULL,
    date DATE NOT NULL,
    payment_mode VARCHAR(50),
    document_url VARCHAR(500),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_expense_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_expense_head FOREIGN KEY (expense_head_id) REFERENCES expense_heads (id) ON DELETE CASCADE
);
