-- V85__seed_missing_user_role_mappings.sql
-- Ensure all students in user_school_roles are properly mapped in user_role_mappings

INSERT INTO user_role_mappings (school_id, user_id, role_id, is_active)
SELECT usr.school_id, usr.user_id, 'role_student_global', true
FROM user_school_roles usr
WHERE usr.role = 'STUDENT'
  AND NOT EXISTS (
      SELECT 1 FROM user_role_mappings urm 
      WHERE urm.user_id = usr.user_id AND urm.role_id = 'role_student_global'
  );
