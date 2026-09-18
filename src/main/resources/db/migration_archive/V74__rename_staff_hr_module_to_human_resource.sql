-- V74__rename_staff_hr_module_to_human_resource.sql

UPDATE platform_modules 
SET name = 'Human Resource' 
WHERE code = 'STAFF_HR';
