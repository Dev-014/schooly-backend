-- V110__restore_exam_marks_and_schedule_compatibility.sql
-- Ensure backward compatibility with JPA entities for exam marks and schedules

ALTER TABLE exam_marks ADD COLUMN IF NOT EXISTS max_marks NUMERIC(6,2) NOT NULL DEFAULT 100.00;
ALTER TABLE exam_schedules ADD COLUMN IF NOT EXISTS full_marks NUMERIC(6,2) NOT NULL DEFAULT 100.00;
ALTER TABLE exam_schedules ADD COLUMN IF NOT EXISTS passing_marks NUMERIC(6,2) NOT NULL DEFAULT 35.00;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'exam_marks' AND column_name = 'exam_subject_config_id'
    ) THEN
        ALTER TABLE exam_marks ALTER COLUMN exam_subject_config_id DROP NOT NULL;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'exam_schedules' AND column_name = 'exam_subject_config_id'
    ) THEN
        ALTER TABLE exam_schedules ALTER COLUMN exam_subject_config_id DROP NOT NULL;
    END IF;
END $$;
