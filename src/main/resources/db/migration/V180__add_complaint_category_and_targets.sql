ALTER TABLE school_complaints
    ADD COLUMN IF NOT EXISTS complaint_category VARCHAR(50) DEFAULT 'STUDENT',
    ADD COLUMN IF NOT EXISTS target_student_id BIGINT,
    ADD COLUMN IF NOT EXISTS target_staff_id BIGINT,
    ADD COLUMN IF NOT EXISTS location_or_asset VARCHAR(150);

CREATE INDEX IF NOT EXISTS idx_school_complaints_category ON school_complaints (school_id, complaint_category);
