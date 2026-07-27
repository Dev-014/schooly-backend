-- V16__Remove_Section_From_Class.sql
-- Remove the redundant 'section' column from the 'class' table since 'sections' is a distinct entity and table in the system.

ALTER TABLE class DROP COLUMN IF EXISTS section;
