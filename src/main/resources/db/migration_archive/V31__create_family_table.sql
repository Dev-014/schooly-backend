-- V31__create_family_table.sql

-- Create Family table
CREATE TABLE IF NOT EXISTS families (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    family_code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_family_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_family_school_id ON families(school_id);
CREATE INDEX IF NOT EXISTS idx_family_code ON families(family_code);

-- Add family_id to student table
ALTER TABLE student ADD COLUMN IF NOT EXISTS family_id BIGINT;
ALTER TABLE student ADD CONSTRAINT fk_student_family FOREIGN KEY (family_id) REFERENCES families(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_student_family_id ON student(family_id);

-- Function to generate family code
CREATE OR REPLACE FUNCTION generate_family_code() RETURNS TEXT AS $$
BEGIN
    RETURN 'FAM-' || TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDD') || '-' || LPAD(nextval('family_code_seq')::TEXT, 4, '0');
END;
$$ LANGUAGE plpgsql;

-- Create sequence for family code generation if not exists
CREATE SEQUENCE IF NOT EXISTS family_code_seq START 1;
