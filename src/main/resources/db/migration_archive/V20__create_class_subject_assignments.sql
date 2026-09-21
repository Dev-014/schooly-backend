-- V20: Class Subject Assignments
-- Stores which subjects are assigned to a class-section for an academic year
CREATE TABLE IF NOT EXISTS class_subject_assignments (
    id                BIGSERIAL PRIMARY KEY,
    school_id         BIGINT       NOT NULL REFERENCES schools(id),
    class_id          BIGINT       NOT NULL REFERENCES class(id),
    section_id        BIGINT       REFERENCES sections(id),
    subject_id        BIGINT       NOT NULL REFERENCES subjects(id),
    academic_year_id  BIGINT       NOT NULL REFERENCES academic_years(id),
    subject_type      VARCHAR(20)  NOT NULL DEFAULT 'CORE',   -- CORE or ELECTIVE
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_class_subject UNIQUE (school_id, class_id, section_id, subject_id, academic_year_id)
);

CREATE INDEX IF NOT EXISTS idx_csa_school_class ON class_subject_assignments(school_id, class_id);
CREATE INDEX IF NOT EXISTS idx_csa_section ON class_subject_assignments(section_id);
