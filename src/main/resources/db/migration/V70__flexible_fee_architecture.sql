-- V70__flexible_fee_architecture.sql

-- 1. Create Collection Plan tables
CREATE TABLE collection_plan (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_collection_plan_school_id ON collection_plan(school_id);

CREATE TABLE collection_plan_item (
    id BIGSERIAL PRIMARY KEY,
    collection_plan_id BIGINT NOT NULL REFERENCES collection_plan(id) ON DELETE CASCADE,
    label VARCHAR(255) NOT NULL,
    due_date DATE,
    amount_type VARCHAR(50) NOT NULL, -- FIXED, PERCENTAGE, REMAINDER
    amount_value NUMERIC(10, 2), -- Nullable if type is REMAINDER
    sequence_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Update Fee Structure Item to support exclusion from collection plan calculation
ALTER TABLE fee_structure_item ADD COLUMN is_part_of_collection_plan BOOLEAN DEFAULT TRUE;

-- 3. Update Student Fee Structure to optionally link to a Collection Plan
ALTER TABLE student_fee_structure ADD COLUMN collection_plan_id BIGINT REFERENCES collection_plan(id) ON DELETE SET NULL;

-- 4. Update Fee Due to decouple from Installment and support generic dues
ALTER TABLE fee_due DROP COLUMN fee_installment_id CASCADE;
ALTER TABLE fee_due ADD COLUMN collection_plan_item_id BIGINT REFERENCES collection_plan_item(id) ON DELETE SET NULL;

-- Make fee_category_id nullable to support generic Collection dues (e.g. "Term 1")
ALTER TABLE fee_due ALTER COLUMN fee_category_id DROP NOT NULL;

-- 5. Drop old Installment tables safely
DROP TABLE IF EXISTS fee_installment_item CASCADE;
DROP TABLE IF EXISTS fee_installment CASCADE;
