-- V30__increase_color_code_length.sql

-- Increase color_code column size to accommodate gradient color codes
ALTER TABLE student_houses ALTER COLUMN color_code TYPE VARCHAR(100);
