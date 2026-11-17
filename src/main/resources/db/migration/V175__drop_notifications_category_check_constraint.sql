-- ============================================================================
-- V175__drop_notifications_category_check_constraint.sql
-- Drop restrictive check constraint on category in notifications table
-- to allow all domain NotificationCategory enum values (including LEAVES).
-- ============================================================================

ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_category_check;
