-- V22: Timetable Entries
-- Stores weekly timetable grid (Class, Section, Day of Week, Period, Subject, Teacher, Room Number)
CREATE TABLE IF NOT EXISTS timetable_entries (
    id                BIGSERIAL PRIMARY KEY,
    school_id         BIGINT       NOT NULL REFERENCES schools(id),
    class_id          BIGINT       NOT NULL REFERENCES class(id),
    section_id        BIGINT       REFERENCES sections(id),
    academic_year_id  BIGINT       NOT NULL REFERENCES academic_years(id),
    day_of_week       VARCHAR(20)  NOT NULL, -- MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    period_id         BIGINT       NOT NULL REFERENCES timetable_periods(id),
    subject_id        BIGINT       REFERENCES subjects(id),
    teacher_id        BIGINT       REFERENCES staff(id),
    room_number       VARCHAR(50),
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_timetable_grid UNIQUE (school_id, class_id, section_id, academic_year_id, day_of_week, period_id)
);

CREATE INDEX IF NOT EXISTS idx_te_school_class ON timetable_entries(school_id, class_id);
CREATE INDEX IF NOT EXISTS idx_te_section ON timetable_entries(section_id);
CREATE INDEX IF NOT EXISTS idx_te_day ON timetable_entries(day_of_week);
CREATE INDEX IF NOT EXISTS idx_te_teacher ON timetable_entries(teacher_id);
