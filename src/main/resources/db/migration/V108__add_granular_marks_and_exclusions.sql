-- Add granular marks to exam_marks
ALTER TABLE exam_marks 
ADD COLUMN theory_marks_obtained NUMERIC(6, 2),
ADD COLUMN practical_marks_obtained NUMERIC(6, 2),
ADD COLUMN internal_marks_obtained NUMERIC(6, 2);

-- Note: student exclusion is already supported by the status column in exam_student_eligibility
-- which has DEFAULT 'ELIGIBLE'. We will use 'EXCLUDED' as the other state.
