-- Add unique constraint to ensure a user only has one staff profile per school
ALTER TABLE staff ADD CONSTRAINT uk_staff_school_user UNIQUE (school_id, user_id);
