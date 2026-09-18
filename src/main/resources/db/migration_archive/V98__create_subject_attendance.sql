-- V98__create_subject_attendance.sql
-- Implements period/subject-wise attendance tracking and seeds required permission definitions

CREATE TABLE IF NOT EXISTS subject_attendance (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    class_id BIGINT NOT NULL REFERENCES class(id) ON DELETE CASCADE,
    section_id BIGINT REFERENCES sections(id) ON DELETE SET NULL,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    timetable_entry_id BIGINT REFERENCES timetable_entries(id) ON DELETE SET NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    remarks TEXT,
    marked_by BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_subject_attendance_entry UNIQUE (school_id, student_id, subject_id, attendance_date, timetable_entry_id)
);

CREATE INDEX IF NOT EXISTS idx_subj_att_student ON subject_attendance(school_id, student_id);
CREATE INDEX IF NOT EXISTS idx_subj_att_class_date ON subject_attendance(school_id, class_id, subject_id, attendance_date);

-- Register permission definitions
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES 
    ('perm_attendance_subject_attendance_view', 'attendance.subject_attendance.view', 'ATTENDANCE', 'subject_attendance', 'view', 'View Subject Attendance', 'Allows viewing subject and period-wise attendance records', '["school"]', false, true),
    ('perm_attendance_subject_attendance_edit', 'attendance.subject_attendance.edit', 'ATTENDANCE', 'subject_attendance', 'edit', 'Mark Subject Attendance', 'Allows teachers to record subject and period-wise attendance', '["school"]', true, true)
ON CONFLICT (id) DO NOTHING;

-- Grant permissions to global teacher and admin roles
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_teacher_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key IN ('attendance.subject_attendance.view', 'attendance.subject_attendance.edit')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_school_admin_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key IN ('attendance.subject_attendance.view', 'attendance.subject_attendance.edit')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_student_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key = 'attendance.subject_attendance.view'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_parent_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key = 'attendance.subject_attendance.view'
ON CONFLICT DO NOTHING;
