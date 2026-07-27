-- V21: Student Subject Enrollments
-- Stores individual student subject enrollments (core inherited + elective chosen)
CREATE TABLE IF NOT EXISTS student_subject_enrollments (
    id                BIGSERIAL PRIMARY KEY,
    school_id         BIGINT       NOT NULL REFERENCES schools(id),
    student_id        BIGINT       NOT NULL REFERENCES student(id),
    subject_id        BIGINT       NOT NULL REFERENCES subjects(id),
    academic_year_id  BIGINT       NOT NULL REFERENCES academic_years(id),
    enrollment_type   VARCHAR(20)  NOT NULL DEFAULT 'CORE',   -- CORE or ELECTIVE
    status            VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_student_subject UNIQUE (school_id, student_id, subject_id, academic_year_id)
);

CREATE INDEX IF NOT EXISTS idx_sse_school_student ON student_subject_enrollments(school_id, student_id);
CREATE INDEX IF NOT EXISTS idx_sse_year ON student_subject_enrollments(academic_year_id);
