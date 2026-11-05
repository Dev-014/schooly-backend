-- Migration V163: Add document attachment fields to student_leaves table
ALTER TABLE student_leaves
ADD COLUMN IF NOT EXISTS attachment_file_id UUID,
ADD COLUMN IF NOT EXISTS attachment_file_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS attachment_url TEXT;

-- Link existing pending leave #10 for Atul Yadav to his active uploaded document
UPDATE student_leaves
SET attachment_file_id = '67eef3f7-84d5-45c6-803d-b544f8f96874',
    attachment_file_name = 'Request for Approval of Tracking Sheet - Complete.pdf'
WHERE id = 10 AND student_id = 5;

