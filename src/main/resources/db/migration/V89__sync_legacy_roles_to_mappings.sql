-- V89__sync_legacy_roles_to_mappings.sql
-- Ensure all legacy user_school_roles are ported over to user_role_mappings before we abandon the legacy table

ALTER TABLE user_role_mappings ALTER COLUMN school_id DROP NOT NULL;

INSERT INTO user_role_mappings (school_id, user_id, role_id, is_active)
SELECT 
    usr.school_id, 
    usr.user_id, 
    CASE 
        WHEN usr.role IN ('ADMIN', 'SUPER_ADMIN') THEN 'role_school_admin_global'
        WHEN usr.role = 'TEACHER' THEN 'role_teacher_global'
        WHEN usr.role = 'STUDENT' THEN 'role_student_global'
        WHEN usr.role = 'PARENT' THEN 'role_parent_global'
        WHEN usr.role = 'STAFF' THEN 'role_school_admin_global'
    END as mapped_role_id,
    CASE 
        WHEN usr.status = 'ACTIVE' THEN true 
        ELSE false 
    END as is_active
FROM user_school_roles usr
WHERE NOT EXISTS (
    SELECT 1 FROM user_role_mappings urm 
    WHERE urm.school_id = usr.school_id 
      AND urm.user_id = usr.user_id
      AND urm.role_id = CASE 
            WHEN usr.role IN ('ADMIN', 'SUPER_ADMIN') THEN 'role_school_admin_global'
            WHEN usr.role = 'TEACHER' THEN 'role_teacher_global'
            WHEN usr.role = 'STUDENT' THEN 'role_student_global'
            WHEN usr.role = 'PARENT' THEN 'role_parent_global'
            WHEN usr.role = 'STAFF' THEN 'role_school_admin_global'
        END
);
