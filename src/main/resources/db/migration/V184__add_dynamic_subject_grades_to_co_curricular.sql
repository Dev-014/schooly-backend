-- Support dynamic subjectId-keyed grades from /app/co-curricular-subjects
ALTER TABLE exam_co_curricular_grades
    ADD COLUMN IF NOT EXISTS subject_grades_json TEXT;
