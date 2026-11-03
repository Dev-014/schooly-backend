-- V159__enhance_school_complaints.sql
-- Adds complaint code, created_by_user tracking, source scope and priority for teacher/own checks

ALTER TABLE school_complaints
    ADD COLUMN IF NOT EXISTS complaint_code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS created_by_user_id BIGINT,
    ADD COLUMN IF NOT EXISTS created_by_role VARCHAR(50),
    ADD COLUMN IF NOT EXISTS source_scope VARCHAR(50) DEFAULT 'SCHOOL',
    ADD COLUMN IF NOT EXISTS priority VARCHAR(20) DEFAULT 'NORMAL';

-- Backfill complaint_code for any existing complaints
UPDATE school_complaints
SET complaint_code = CONCAT('CMP-', LPAD(id::text, 5, '0'))
WHERE complaint_code IS NULL;

CREATE INDEX IF NOT EXISTS idx_school_complaints_code ON school_complaints (school_id, complaint_code);
CREATE INDEX IF NOT EXISTS idx_school_complaints_user ON school_complaints (school_id, created_by_user_id);

