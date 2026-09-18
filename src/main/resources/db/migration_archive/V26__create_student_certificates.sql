CREATE TABLE student_certificates (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id),
    student_id BIGINT NOT NULL REFERENCES student(id),
    certificate_type VARCHAR(100) NOT NULL,
    issue_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    remarks TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_student_certificates_school_id ON student_certificates(school_id);
CREATE INDEX idx_student_certificates_student_id ON student_certificates(student_id);
