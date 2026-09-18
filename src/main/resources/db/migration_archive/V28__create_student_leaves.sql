CREATE TABLE student_leaves (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id),
    student_id BIGINT NOT NULL REFERENCES student(id),
    apply_date DATE NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    days INT,
    reason TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    reply TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_student_leaves_school_id ON student_leaves(school_id);
CREATE INDEX idx_student_leaves_student_id ON student_leaves(student_id);
