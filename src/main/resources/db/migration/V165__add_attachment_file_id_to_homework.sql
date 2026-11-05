-- ============================================================================
-- V165__add_attachment_file_id_to_homework.sql
-- Add attachment_file_id UUID to homework_assignments and homework_submissions
-- to seamlessly link with centralized AWS S3 File Infrastructure.
-- ============================================================================

ALTER TABLE homework_assignments
    ADD COLUMN IF NOT EXISTS attachment_file_id UUID;

ALTER TABLE homework_submissions
    ADD COLUMN IF NOT EXISTS attachment_file_id UUID;

-- Link existing sample homework assignments to test files if present
UPDATE homework_assignments
SET attachment_file_id = '67eef3f7-84d5-45c6-803d-b544f8f96874',
    attachment_name = 'Science_Worksheet_Unit4.pdf'
WHERE id = 3 AND attachment_file_id IS NULL;

-- Link existing sample homework submission to test files if present
UPDATE homework_submissions
SET attachment_file_id = '67eef3f7-84d5-45c6-803d-b544f8f96874',
    attachment_name = 'Atul_Homework_Submission.pdf'
WHERE id = 1 AND attachment_file_id IS NULL;

