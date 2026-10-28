-- V151__unify_teacher_assignments_sot.sql
-- Unify teacher assignments Single Source of Truth (SOT)
-- Migrates any legacy class_teacher records from user_assignments into class_teacher_assignments

INSERT INTO class_teacher_assignments (school_id, staff_id, class_id, section_id, academic_year_id, status, created_at, updated_at)
SELECT 
    ua.school_id,
    s.id AS staff_id,
    ua.class_id,
    ua.section_id,
    COALESCE(ua.academic_session_id, (SELECT id FROM academic_years WHERE school_id = ua.school_id AND status = 'ACTIVE' LIMIT 1)) AS academic_year_id,
    'ACTIVE' AS status,
    COALESCE(ua.effective_from, CURRENT_TIMESTAMP) AS created_at,
    CURRENT_TIMESTAMP AS updated_at
FROM user_assignments ua
JOIN staff s ON s.user_id = ua.user_id AND s.school_id = ua.school_id
WHERE ua.assignment_type = 'class_teacher' 
  AND ua.is_active = true
  AND ua.class_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM class_teacher_assignments cta 
      WHERE cta.school_id = ua.school_id 
        AND cta.class_id = ua.class_id 
        AND (cta.section_id = ua.section_id OR (cta.section_id IS NULL AND ua.section_id IS NULL))
        AND cta.status = 'ACTIVE'
  );

-- Deactivate migrated class_teacher records in user_assignments to prevent dual source of truth
UPDATE user_assignments 
SET is_active = false 
WHERE assignment_type = 'class_teacher';

-- Document SOT boundaries on user_assignments
COMMENT ON TABLE user_assignments IS 'Manages non-academic organizational scopes (e.g., department responsibilities). Academic teacher assignments are strictly managed in class_teacher_assignments and timetable_entries.';
