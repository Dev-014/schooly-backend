-- ============================================================================
-- V143__clean_legacy_exam_marks_permissions.sql
-- Remove legacy pre-catalog 'marks' permissions (perm_exam_view, perm_exam_edit)
-- and preserve canonical 'marks_entry' permissions as per auth-catalog.json SOT.
-- ============================================================================

-- 1. Remove legacy role_permissions references
DELETE FROM role_permissions
WHERE permission_id IN (
    'perm_exam_view',
    'perm_exam_edit'
);

-- 2. Remove from system_role_permissions if table exists
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'system_role_permissions') THEN
        DELETE FROM system_role_permissions
        WHERE permission_id IN (
            'perm_exam_view',
            'perm_exam_edit'
        ) OR permission_key IN (
            'exams.marks.view',
            'exams.marks.edit'
        );
    END IF;
END $$;

-- 3. Delete legacy entries from permission_definitions
DELETE FROM permission_definitions
WHERE id IN (
    'perm_exam_view',
    'perm_exam_edit'
) OR permission_key IN (
    'exams.marks.view',
    'exams.marks.edit'
);
