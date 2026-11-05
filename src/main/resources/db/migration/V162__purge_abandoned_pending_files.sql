-- Migration V162: Purge abandoned pending files and remove orphaned attachments
-- Any file with status PENDING_UPLOAD created before this migration is marked as DELETED
UPDATE files
SET deleted_at = NOW(),
    status = 'DELETED'
WHERE status = 'PENDING_UPLOAD';

-- Remove attachment records linking to DELETED files
DELETE FROM file_attachments
WHERE file_id IN (
    SELECT id FROM files WHERE status = 'DELETED'
);

