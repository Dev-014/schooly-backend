-- V80__add_department_id_to_user_assignments.sql
ALTER TABLE user_assignments
ADD COLUMN department_id BIGINT;
