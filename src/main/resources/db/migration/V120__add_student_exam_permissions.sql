-- Add Student Exam Permissions with correct module key EXAMS_RESULTS
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES
('student_exams_timetable_view', 'student.exams.timetable.view', 'EXAMS_RESULTS', 'EXAM_TIMETABLE', 'VIEW', 'View Exam Timetable', 'Allows student to view their exam timetable', '["school", "assigned"]'::jsonb, false, true),
('student_exams_admit_card_view', 'student.exams.admit_card.view', 'EXAMS_RESULTS', 'EXAM_ADMIT_CARD', 'VIEW', 'View Exam Admit Card', 'Allows student to view their admit card', '["school", "assigned"]'::jsonb, false, true),
('student_exams_results_view', 'student.exams.results.view', 'EXAMS_RESULTS', 'EXAM_RESULTS', 'VIEW', 'View Exam Results', 'Allows student to view their exam results', '["school", "assigned"]'::jsonb, false, true),
('student_exams_syllabus_view', 'student.exams.syllabus.view', 'EXAMS_RESULTS', 'EXAM_SYLLABUS', 'VIEW', 'View Exam Syllabus', 'Allows student to view their exam syllabus', '["school", "assigned"]'::jsonb, false, true)
ON CONFLICT (id) DO UPDATE SET module_key = 'EXAMS_RESULTS';

-- Grant permissions to STUDENT role
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT r.id, pd.id, 'school', '{}'::jsonb
FROM roles r
CROSS JOIN permission_definitions pd
WHERE r.archetype = 'STUDENT'
  AND pd.permission_key IN (
    'student.exams.timetable.view',
    'student.exams.admit_card.view',
    'student.exams.results.view',
    'student.exams.syllabus.view'
  )
ON CONFLICT DO NOTHING;
