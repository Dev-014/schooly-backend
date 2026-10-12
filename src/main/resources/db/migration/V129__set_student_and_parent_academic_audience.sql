-- V129: Set proper audience for student and parent academic permissions

UPDATE permission_definitions 
SET audience = '["student", "parent"]'::jsonb
WHERE id IN ('perm_academic_student_timetable_view', 'perm_academic_subject_directory_view');

UPDATE permission_definitions 
SET audience = '["parent"]'::jsonb
WHERE id = 'perm_academic_parent_calendar_view';
