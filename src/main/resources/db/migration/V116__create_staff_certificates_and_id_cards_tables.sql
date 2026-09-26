-- V116__create_staff_certificates_and_id_cards_tables.sql
-- Creates tables for staff certificates and staff id card generation

CREATE TABLE IF NOT EXISTS staff_certificates (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    certificate_type VARCHAR(100) NOT NULL,
    issue_date DATE,
    status VARCHAR(50) DEFAULT 'ISSUED',
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_cert_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_cert_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_staff_certificates_school_id ON staff_certificates (school_id);
CREATE INDEX IF NOT EXISTS idx_staff_certificates_staff_id ON staff_certificates (staff_id);

CREATE TABLE IF NOT EXISTS staff_id_cards (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'GENERATED',
    template VARCHAR(100) DEFAULT 'DEFAULT',
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_idcard_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_idcard_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_staff_id_cards_school_id ON staff_id_cards (school_id);
CREATE INDEX IF NOT EXISTS idx_staff_id_cards_staff_id ON staff_id_cards (staff_id);
