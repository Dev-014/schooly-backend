-- V56__cleanup_legacy_modules.sql

-- Remove all legacy platform modules that are not part of the 21 active frontend modules
-- This ensures that dynamic API responses (like Roles & Permissions) do not serve phantom modules

DELETE FROM platform_modules 
WHERE code NOT IN (
    'FRONT_OFFICE', 
    'STUDENTS', 
    'CERTIFICATES', 
    'ONLINE_CLASS', 
    'STAFF_HR', 
    'ATTENDANCE', 
    'ONLINE_EXAMS', 
    'LESSON_MODULE', 
    'EXAMS_RESULTS', 
    'HOMEWORK', 
    'ACADEMICS', 
    'DOWNLOADS', 
    'FEES', 
    'INCOME', 
    'LIBRARY', 
    'EXPENSE', 
    'QUESTION_PAPER', 
    'COMMUNICATION', 
    'DISCIPLINE', 
    'INVENTORY', 
    'SETTINGS'
);
