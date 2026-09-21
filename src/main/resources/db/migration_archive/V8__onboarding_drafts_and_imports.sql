-- V8__onboarding_drafts_and_imports.sql
-- Creates tables for storing multi-step school onboarding drafts, data import jobs, field mappings, and inline validation errors.

CREATE TABLE IF NOT EXISTS onboarding_drafts (
    school_id BIGSERIAL PRIMARY KEY,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    current_step INTEGER NOT NULL DEFAULT 1,
    step1_data TEXT,
    step2_data TEXT,
    step3_data TEXT,
    step4_data TEXT,
    step5_data TEXT,
    step6_data TEXT,
    step7_data TEXT,
    step8_data TEXT,
    step9_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_onboarding_drafts_status ON onboarding_drafts (status);

CREATE TABLE IF NOT EXISTS data_import_jobs (
    job_id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    category VARCHAR(100) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    total_records INTEGER DEFAULT 0,
    successful_records INTEGER DEFAULT 0,
    failed_records INTEGER DEFAULT 0,
    field_mappings TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_data_import_jobs_school_id ON data_import_jobs (school_id);
CREATE INDEX IF NOT EXISTS idx_data_import_jobs_category ON data_import_jobs (category);

CREATE TABLE IF NOT EXISTS data_import_errors (
    error_id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL REFERENCES data_import_jobs(job_id) ON DELETE CASCADE,
    row_index VARCHAR(50) NOT NULL,
    category VARCHAR(100) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    error_message VARCHAR(500) NOT NULL,
    current_value VARCHAR(500),
    resolved BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_data_import_errors_job_id ON data_import_errors (job_id);
