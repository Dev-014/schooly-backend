-- Add discount and term support to fee_due
ALTER TABLE fee_due ADD COLUMN discount_amount NUMERIC(10,2) DEFAULT 0;
ALTER TABLE fee_due ADD COLUMN term_name VARCHAR(100);
