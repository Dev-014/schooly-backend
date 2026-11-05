-- ============================================================================
-- V164__fix_notifications_type_check_constraint.sql
-- Drop restrictive check constraint on notification_type in notifications table
-- to allow all domain NotificationType enum values (such as STUDENT_LEAVE_REQUESTED).
-- ============================================================================

ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_notification_type_check;

