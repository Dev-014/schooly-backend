-- V117__create_school_complaints_table.sql
-- Creates table for front office institutional complaint management

CREATE TABLE IF NOT EXISTS school_complaints (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    complainant_name VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(50),
    complaint_type VARCHAR(100) NOT NULL,
    description TEXT,
    assigned_by VARCHAR(100),
    action_taken TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'Open',
    complaint_date DATE DEFAULT CURRENT_DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_complaint_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_school_complaints_school_id ON school_complaints (school_id);
CREATE INDEX IF NOT EXISTS idx_school_complaints_status ON school_complaints (school_id, status);
CREATE INDEX IF NOT EXISTS idx_school_complaints_date ON school_complaints (school_id, complaint_date);
