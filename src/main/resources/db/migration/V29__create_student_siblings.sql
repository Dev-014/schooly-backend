CREATE TABLE student_siblings (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id),
    primary_student_id BIGINT NOT NULL REFERENCES student(id),
    sibling_student_id BIGINT NOT NULL REFERENCES student(id),
    relationship VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sibling_pair UNIQUE (primary_student_id, sibling_student_id)
);

CREATE INDEX idx_student_siblings_school ON student_siblings(school_id);
CREATE INDEX idx_student_siblings_primary ON student_siblings(primary_student_id);
CREATE INDEX idx_student_siblings_sibling ON student_siblings(sibling_student_id);
