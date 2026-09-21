-- V19: Add status column to subjects table
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
-- Backfill existing rows
UPDATE subjects SET status = 'ACTIVE' WHERE status IS NULL;
