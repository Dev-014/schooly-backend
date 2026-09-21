-- V63__create_fee_module_tables.sql

-- 1. Fee Category
CREATE TABLE IF NOT EXISTS fee_category (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fee_category_school FOREIGN KEY (school_id) REFERENCES schools(id)
);
CREATE INDEX IF NOT EXISTS idx_fee_category_school_id ON fee_category(school_id);

-- 2. Fee Structure
CREATE TABLE IF NOT EXISTS fee_structure (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    academic_year_id BIGINT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fee_structure_school FOREIGN KEY (school_id) REFERENCES schools(id)
);
CREATE INDEX IF NOT EXISTS idx_fee_structure_school_id ON fee_structure(school_id);

-- 3. Fee Structure Items
CREATE TABLE IF NOT EXISTS fee_structure_item (
    id BIGSERIAL PRIMARY KEY,
    fee_structure_id BIGINT NOT NULL,
    fee_category_id BIGINT NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fsi_structure FOREIGN KEY (fee_structure_id) REFERENCES fee_structure(id) ON DELETE CASCADE,
    CONSTRAINT fk_fsi_category FOREIGN KEY (fee_category_id) REFERENCES fee_category(id)
);
CREATE INDEX IF NOT EXISTS idx_fsi_structure_id ON fee_structure_item(fee_structure_id);

-- 4. Fee Invoice Items
CREATE TABLE IF NOT EXISTS fee_invoice_item (
    id BIGSERIAL PRIMARY KEY,
    fee_invoice_id BIGINT NOT NULL,
    fee_category_id BIGINT NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    paid_amount NUMERIC(10,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fii_invoice FOREIGN KEY (fee_invoice_id) REFERENCES fee_invoice(id) ON DELETE CASCADE,
    CONSTRAINT fk_fii_category FOREIGN KEY (fee_category_id) REFERENCES fee_category(id)
);
CREATE INDEX IF NOT EXISTS idx_fii_invoice_id ON fee_invoice_item(fee_invoice_id);

-- 5. Update Fee Invoice
ALTER TABLE fee_invoice ADD COLUMN IF NOT EXISTS fee_structure_id BIGINT;
ALTER TABLE fee_invoice ADD CONSTRAINT fk_fee_invoice_structure FOREIGN KEY (fee_structure_id) REFERENCES fee_structure(id) ON DELETE SET NULL;

-- 6. Rename Payment to Fee Payment and Update
ALTER TABLE payment RENAME TO fee_payment;
ALTER TABLE fee_payment RENAME CONSTRAINT fk_payment_invoice TO fk_fee_payment_invoice;
ALTER TABLE fee_payment RENAME CONSTRAINT fk_payment_school TO fk_fee_payment_school;

-- Note: payment table has amount NUMERIC(19,2). We add new columns:
ALTER TABLE fee_payment 
    ADD COLUMN IF NOT EXISTS student_id BIGINT,
    ADD COLUMN IF NOT EXISTS receipt_number VARCHAR(255),
    ADD COLUMN IF NOT EXISTS payment_date DATE;

-- Try to backfill student_id for existing payments based on invoice
UPDATE fee_payment fp
SET student_id = fi.student_id
FROM fee_invoice fi
WHERE fp.invoice_id = fi.id AND fp.student_id IS NULL;

-- Now we can make student_id NOT NULL if we want, but let's keep it nullable temporarily just in case.
ALTER TABLE fee_payment ADD CONSTRAINT fk_fee_payment_student FOREIGN KEY (student_id) REFERENCES student(id);
CREATE INDEX IF NOT EXISTS idx_fee_payment_student_id ON fee_payment(student_id);

-- 7. Fee Payment Items (Mapping which invoice items were paid)
CREATE TABLE IF NOT EXISTS fee_payment_item (
    id BIGSERIAL PRIMARY KEY,
    fee_payment_id BIGINT NOT NULL,
    fee_invoice_item_id BIGINT NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fpi_payment FOREIGN KEY (fee_payment_id) REFERENCES fee_payment(id) ON DELETE CASCADE,
    CONSTRAINT fk_fpi_invoice_item FOREIGN KEY (fee_invoice_item_id) REFERENCES fee_invoice_item(id)
);
CREATE INDEX IF NOT EXISTS idx_fpi_payment_id ON fee_payment_item(fee_payment_id);
