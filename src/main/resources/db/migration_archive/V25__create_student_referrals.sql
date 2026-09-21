CREATE TABLE student_referrals (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id),
    referral_by VARCHAR(255) NOT NULL,
    student_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    mobile VARCHAR(50) NOT NULL,
    note TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_student_referrals_school_id ON student_referrals(school_id);
