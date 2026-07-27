CREATE TABLE class_teacher_assignments (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    staff_id BIGINT NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    class_id BIGINT NOT NULL REFERENCES class(id) ON DELETE CASCADE,
    section_id BIGINT REFERENCES sections(id) ON DELETE CASCADE,
    academic_year_id BIGINT NOT NULL REFERENCES academic_years(id) ON DELETE CASCADE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ensure a section only has one active primary teacher per academic year
CREATE UNIQUE INDEX idx_unique_active_class_teacher
ON class_teacher_assignments(section_id, academic_year_id)
WHERE status = 'ACTIVE' AND section_id IS NOT NULL;

CREATE INDEX idx_class_teacher_assignments_school_id ON class_teacher_assignments(school_id);
CREATE INDEX idx_class_teacher_assignments_staff_id ON class_teacher_assignments(staff_id);
