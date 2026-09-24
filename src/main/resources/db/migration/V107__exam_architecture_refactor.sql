-- Create new tables for the decoupled Exam Architecture

CREATE TABLE exam_subject_configs (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    exam_setup_id BIGINT NOT NULL REFERENCES exam_setups(id) ON DELETE CASCADE,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    max_marks NUMERIC(6, 2) NOT NULL DEFAULT 100.00,
    passing_marks NUMERIC(6, 2) NOT NULL DEFAULT 35.00,
    theory_marks NUMERIC(6, 2),
    practical_marks NUMERIC(6, 2),
    internal_marks NUMERIC(6, 2),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(school_id, exam_setup_id, subject_id)
);

CREATE TABLE exam_applicabilities (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    exam_setup_id BIGINT NOT NULL REFERENCES exam_setups(id) ON DELETE CASCADE,
    class_id BIGINT NOT NULL REFERENCES class(id) ON DELETE CASCADE,
    section_id BIGINT REFERENCES sections(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE exam_student_eligibility (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    exam_subject_config_id BIGINT NOT NULL REFERENCES exam_subject_configs(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL DEFAULT 'ELIGIBLE',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(exam_subject_config_id, student_id)
);

-- Backfill data to preserve existing configurations
INSERT INTO exam_subject_configs (school_id, exam_setup_id, subject_id, max_marks, passing_marks, created_at, updated_at)
SELECT DISTINCT school_id, exam_setup_id, subject_id, full_marks, passing_marks, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM exam_schedules
ON CONFLICT (school_id, exam_setup_id, subject_id) DO NOTHING;

INSERT INTO exam_subject_configs (school_id, exam_setup_id, subject_id, max_marks, passing_marks, created_at, updated_at)
SELECT DISTINCT em.school_id, em.exam_setup_id, em.subject_id, COALESCE(em.max_marks, 100.00), 35.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM exam_marks em
ON CONFLICT (school_id, exam_setup_id, subject_id) DO NOTHING;

INSERT INTO exam_applicabilities (school_id, exam_setup_id, class_id, section_id, created_at, updated_at)
SELECT DISTINCT school_id, exam_setup_id, class_id, section_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM exam_schedules;

-- Modify exam_schedules
ALTER TABLE exam_schedules ADD COLUMN exam_subject_config_id BIGINT;

UPDATE exam_schedules es
SET exam_subject_config_id = esc.id
FROM exam_subject_configs esc
WHERE es.exam_setup_id = esc.exam_setup_id
  AND es.subject_id = esc.subject_id
  AND es.school_id = esc.school_id;

-- Now make it NOT NULL and add foreign key
ALTER TABLE exam_schedules ALTER COLUMN exam_subject_config_id SET NOT NULL;
ALTER TABLE exam_schedules ADD CONSTRAINT fk_exam_schedules_config FOREIGN KEY (exam_subject_config_id) REFERENCES exam_subject_configs(id) ON DELETE CASCADE;

ALTER TABLE exam_schedules ADD COLUMN invigilator_id BIGINT REFERENCES staff(id) ON DELETE SET NULL;

ALTER TABLE exam_schedules DROP COLUMN full_marks;
ALTER TABLE exam_schedules DROP COLUMN passing_marks;

-- Modify exam_marks
ALTER TABLE exam_marks ADD COLUMN exam_subject_config_id BIGINT;

UPDATE exam_marks em
SET exam_subject_config_id = esc.id
FROM exam_subject_configs esc
WHERE em.exam_setup_id = esc.exam_setup_id
  AND em.subject_id = esc.subject_id
  AND em.school_id = esc.school_id;

ALTER TABLE exam_marks ALTER COLUMN exam_subject_config_id SET NOT NULL;
ALTER TABLE exam_marks ADD CONSTRAINT fk_exam_marks_config FOREIGN KEY (exam_subject_config_id) REFERENCES exam_subject_configs(id) ON DELETE CASCADE;

ALTER TABLE exam_marks DROP COLUMN max_marks;
