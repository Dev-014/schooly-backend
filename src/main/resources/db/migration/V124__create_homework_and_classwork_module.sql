-- V124__create_homework_and_classwork_module.sql
-- Create Homework & Classwork assignments, student submissions, and permissions

CREATE TABLE IF NOT EXISTS homework_assignments (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    assignment_type VARCHAR(20) NOT NULL DEFAULT 'HOMEWORK', -- 'HOMEWORK' or 'CLASSWORK'
    class_id BIGINT NOT NULL,
    section_id BIGINT,
    subject_id BIGINT NOT NULL,
    teacher_id BIGINT,
    academic_year_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    instructions TEXT,
    assigned_date DATE NOT NULL,
    due_date DATE NOT NULL,
    max_marks NUMERIC(6, 2) DEFAULT 100.00,
    attachment_url VARCHAR(500),
    attachment_name VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'PUBLISHED', -- 'PUBLISHED', 'DRAFT', 'ARCHIVED'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_hw_assign_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_hw_assign_class FOREIGN KEY (class_id) REFERENCES class (id) ON DELETE CASCADE,
    CONSTRAINT fk_hw_assign_section FOREIGN KEY (section_id) REFERENCES sections (id) ON DELETE SET NULL,
    CONSTRAINT fk_hw_assign_subject FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE CASCADE,
    CONSTRAINT fk_hw_assign_teacher FOREIGN KEY (teacher_id) REFERENCES staff (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_hw_assign_school_type ON homework_assignments (school_id, assignment_type);
CREATE INDEX IF NOT EXISTS idx_hw_assign_class_sec ON homework_assignments (school_id, class_id, section_id);
CREATE INDEX IF NOT EXISTS idx_hw_assign_due_date ON homework_assignments (school_id, due_date);
CREATE INDEX IF NOT EXISTS idx_hw_assign_status ON homework_assignments (school_id, status);

CREATE TABLE IF NOT EXISTS homework_submissions (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    assignment_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    submission_text TEXT,
    attachment_url VARCHAR(500),
    attachment_name VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'SUBMITTED', -- 'PENDING', 'SUBMITTED', 'LATE', 'EVALUATED', 'COMPLETED', 'INCOMPLETE', 'RESUBMIT'
    marks_obtained NUMERIC(6, 2),
    remarks TEXT,
    evaluated_by BIGINT,
    evaluated_at TIMESTAMP,
    rubric_scores TEXT, -- JSON string representation of rubric scores
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_hw_submission UNIQUE (assignment_id, student_id),
    CONSTRAINT fk_hw_sub_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_hw_sub_assignment FOREIGN KEY (assignment_id) REFERENCES homework_assignments (id) ON DELETE CASCADE,
    CONSTRAINT fk_hw_sub_student FOREIGN KEY (student_id) REFERENCES student (id) ON DELETE CASCADE,
    CONSTRAINT fk_hw_sub_evaluator FOREIGN KEY (evaluated_by) REFERENCES staff (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_hw_sub_school_student ON homework_submissions (school_id, student_id);
CREATE INDEX IF NOT EXISTS idx_hw_sub_assignment_status ON homework_submissions (assignment_id, status);

-- Seed Granular Permissions for Homework and Classwork
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES 
    ('perm_homework_submission_view', 'homework.submission.view', 'HOMEWORK', 'SUBMISSION', 'VIEW', 'View Homework Submissions', 'Allows viewing homework and classwork student submissions', '["school", "class", "linked"]'::jsonb, false, true),
    ('perm_homework_submission_edit', 'homework.submission.edit', 'HOMEWORK', 'SUBMISSION', 'EDIT', 'Submit Homework Answers', 'Allows students to submit homework/classwork answers and files', '["school", "class", "linked"]'::jsonb, false, true),
    ('perm_homework_submission_grade', 'homework.submission.grade', 'HOMEWORK', 'SUBMISSION', 'GRADE', 'Grade Submissions', 'Allows teachers and staff to evaluate and grade student submissions', '["school", "class"]'::jsonb, false, true)
ON CONFLICT (id) DO NOTHING;

-- Map permissions to System Roles
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT r.id, pd.id, 'school', '{}'::jsonb
FROM roles r
CROSS JOIN permission_definitions pd
WHERE (r.archetype IN ('SUPER_ADMIN', 'SCHOOL_ADMIN', 'TEACHER') OR r.id IN ('role_super_admin_global', 'role_school_admin_global', 'role_teacher_global'))
  AND pd.permission_key IN ('homework.submission.view', 'homework.submission.grade')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT r.id, pd.id, 'school', '{}'::jsonb
FROM roles r
CROSS JOIN permission_definitions pd
WHERE (r.archetype IN ('STUDENT', 'PARENT') OR r.id IN ('role_student_global', 'role_parent_global'))
  AND pd.permission_key IN ('homework.submission.view', 'homework.submission.edit')
ON CONFLICT DO NOTHING;
