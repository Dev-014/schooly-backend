-- V166: Add document attachment and approver reply support to staff_leaves
ALTER TABLE public.staff_leaves
    ADD COLUMN IF NOT EXISTS attachment_file_id UUID REFERENCES public.files(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS attachment_file_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS attachment_url VARCHAR(1000),
    ADD COLUMN IF NOT EXISTS reply VARCHAR(1000);

CREATE INDEX IF NOT EXISTS idx_staff_leaves_attachment_file_id ON public.staff_leaves(attachment_file_id);

-- Attach sample verified S3 document to recent teacher leave for review verification
UPDATE public.staff_leaves
SET attachment_file_id = '67eef3f7-84d5-45c6-803d-b544f8f96874',
    attachment_file_name = 'Medical_Certificate_Teacher.pdf',
    attachment_url = '/api/v1/files/67eef3f7-84d5-45c6-803d-b544f8f96874/download'
WHERE id = (SELECT MAX(id) FROM public.staff_leaves);

