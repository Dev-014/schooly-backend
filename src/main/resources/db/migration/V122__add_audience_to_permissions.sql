-- ============================================================================
-- V122__add_audience_to_permissions.sql
-- Adds the audience JSONB column to permission_definitions and backfills data.
-- ============================================================================

ALTER TABLE permission_definitions ADD COLUMN audience JSONB DEFAULT '["staff"]'::jsonb;

-- Explicitly tag Student/Parent specific views
UPDATE permission_definitions 
SET audience = '["student", "parent"]'::jsonb
WHERE permission_key IN (
    'student.exams.timetable.view',
    'student.exams.admit_card.view',
    'student.exams.results.view',
    'student.exams.syllabus.view'
);

-- Update universal views (staff, student, parent)
UPDATE permission_definitions 
SET audience = '["staff", "student", "parent"]'::jsonb
WHERE permission_key IN (
    'attendance.attendance_record.view',
    'student.student.view',
    'fees.fee_invoice.view',
    'family.family_profile.view',
    'certificates.certificate.view',
    'online_class.session.view',
    'online_exams.exam.view',
    'lesson_module.plan.view',
    'downloads.material.view',
    'library.book.view',
    'question_paper.paper.view',
    'discipline.record.view',
    'communication.message.view',
    'homework.assignment.view'
);
