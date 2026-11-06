-- ============================================================================
-- V171__disable_unreleased_modules_for_school_admin.sql
-- Revokes default permissions for unreleased/placeholder modules from the 
-- global School Admin role (role_school_admin_global).
-- Modules affected:
-- INVENTORY, DISCIPLINE, LESSON_MODULE, CERTIFICATES, DOWNLOADS,
-- ONLINE_CLASS, ONLINE_EXAMS, QUESTION_PAPER
-- ============================================================================

DELETE FROM role_permissions
WHERE role_id = 'role_school_admin_global'
  AND permission_id IN (
      SELECT id FROM permission_definitions
      WHERE module_key IN (
          'INVENTORY',
          'DISCIPLINE',
          'LESSON_MODULE',
          'CERTIFICATES',
          'DOWNLOADS',
          'ONLINE_CLASS',
          'ONLINE_EXAMS',
          'QUESTION_PAPER'
      )
  );

