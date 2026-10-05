-- V118__create_school_entrance_exams_table.sql
-- Creates table for front office entrance exam registrations and evaluations

CREATE TABLE IF NOT EXISTS school_entrance_exams (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    candidate_name VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(50),
    parent_name VARCHAR(255),
    gender VARCHAR(20),
    class_name VARCHAR(100),
    exam_name VARCHAR(150),
    center_name VARCHAR(150),
    exam_date DATE,
    exam_time VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'Scheduled',
    score NUMERIC(5, 2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_entrance_exam_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_school_entrance_exams_school_id ON school_entrance_exams (school_id);
CREATE INDEX IF NOT EXISTS idx_school_entrance_exams_status ON school_entrance_exams (school_id, status);
CREATE INDEX IF NOT EXISTS idx_school_entrance_exams_date ON school_entrance_exams (school_id, exam_date);
