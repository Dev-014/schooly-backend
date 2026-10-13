-- ============================================================================
-- V139__remove_examination_reports_permission.sql
-- Remove legacy examination_reports permissions and consolidate under report_card
-- ============================================================================

-- 1. Remove role_permissions references
DELETE FROM role_permissions
WHERE permission_id IN (
    'perm_exams_results_examination_reports_view',
    'perm_exams_results_examination_reports_edit'
);

-- 2. Remove from system_role_permissions if table exists
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'system_role_permissions') THEN
        DELETE FROM system_role_permissions
        WHERE permission_id IN (
            'perm_exams_results_examination_reports_view',
            'perm_exams_results_examination_reports_edit'
        ) OR permission_key IN (
            'exams_results.examination_reports.view',
            'exams_results.examination_reports.edit'
        );
    END IF;
END $$;

-- 3. Delete from permission_definitions
DELETE FROM permission_definitions
WHERE id IN (
    'perm_exams_results_examination_reports_view',
    'perm_exams_results_examination_reports_edit'
) OR permission_key IN (
    'exams_results.examination_reports.view',
    'exams_results.examination_reports.edit'
);
