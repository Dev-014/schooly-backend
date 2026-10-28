-- Migration V138: Update unique constraint on exam_attendances to allow attendance per exam schedule
ALTER TABLE exam_attendances DROP CONSTRAINT IF EXISTS uq_exam_attendance_entry;

ALTER TABLE exam_attendances
    ADD CONSTRAINT uq_exam_attendance_entry UNIQUE (school_id, term_id, exam_schedule_id, student_id);
