-- ==========================================
-- V5__student_audit_fields.sql
-- Adds audit columns for the student table.
-- ==========================================

ALTER TABLE student
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE student
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

UPDATE student
    SET created_at = CURRENT_TIMESTAMP
    WHERE created_at IS NULL;

UPDATE student
    SET updated_at = CURRENT_TIMESTAMP
    WHERE updated_at IS NULL;
