-- V58__add_display_name_to_academic_years.sql

ALTER TABLE academic_years ADD COLUMN display_name VARCHAR(100);

-- Populate existing rows with their name
UPDATE academic_years SET display_name = name WHERE display_name IS NULL;

-- Set NOT NULL
ALTER TABLE academic_years ALTER COLUMN display_name SET NOT NULL;
