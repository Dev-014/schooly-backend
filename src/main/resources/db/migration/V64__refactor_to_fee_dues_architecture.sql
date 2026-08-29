-- 1. Create the fundamental Fee Due ledger
CREATE TABLE fee_due (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES student(id),
    school_id BIGINT NOT NULL REFERENCES schools(id),
    fee_category_id BIGINT NOT NULL REFERENCES fee_category(id),
    fee_structure_id BIGINT REFERENCES fee_structure(id),
    title VARCHAR(255) NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    paid_amount NUMERIC(10,2) DEFAULT 0,
    due_date DATE NOT NULL,
    status VARCHAR(50) DEFAULT 'UNPAID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Drop legacy item tables
-- They are referenced by fee_payment_item, so drop that first.
DROP TABLE fee_payment_item;
DROP TABLE fee_invoice_item;

-- 3. Refactor Payments (Support Unallocated Advances)
-- Remove hard link to Invoice
ALTER TABLE fee_payment DROP COLUMN invoice_id;
-- Add tracking for advances/credits
ALTER TABLE fee_payment ADD COLUMN unallocated_amount NUMERIC(10,2) DEFAULT 0;

-- 4. Create the Allocation Bridge
CREATE TABLE fee_payment_allocation (
    id BIGSERIAL PRIMARY KEY,
    fee_payment_id BIGINT NOT NULL REFERENCES fee_payment(id),
    fee_due_id BIGINT NOT NULL REFERENCES fee_due(id),
    allocated_amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
