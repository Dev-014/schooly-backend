-- V100__create_marks_grades_and_remarks_tables.sql
-- Migration for Marks Entry, Co-Curricular Assessment, Teacher Remarks, Grade Scales, and Divisions

-- 1. Exam Marks Table
CREATE TABLE IF NOT EXISTS exam_marks (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    exam_setup_id BIGINT NOT NULL REFERENCES exam_setups(id) ON DELETE CASCADE,
    class_id BIGINT NOT NULL REFERENCES class(id) ON DELETE CASCADE,
    section_id BIGINT REFERENCES sections(id) ON DELETE SET NULL,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    marks_obtained NUMERIC(6,2),
    max_marks NUMERIC(6,2) NOT NULL DEFAULT 100.00,
    attendance_status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    remarks TEXT,
    entered_by BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_exam_marks_entry UNIQUE (school_id, exam_setup_id, subject_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_exam_marks_filter ON exam_marks(school_id, exam_setup_id, class_id, section_id, subject_id);
CREATE INDEX IF NOT EXISTS idx_exam_marks_student ON exam_marks(school_id, student_id);

-- 2. Exam Co-Curricular Grades Table
CREATE TABLE IF NOT EXISTS exam_co_curricular_grades (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    term_id BIGINT NOT NULL REFERENCES exam_terms(id) ON DELETE CASCADE,
    class_id BIGINT NOT NULL REFERENCES class(id) ON DELETE CASCADE,
    section_id BIGINT REFERENCES sections(id) ON DELETE SET NULL,
    student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    physical_education_grade VARCHAR(10) DEFAULT 'PENDING',
    visual_arts_grade VARCHAR(10) DEFAULT 'PENDING',
    performing_arts_grade VARCHAR(10) DEFAULT 'PENDING',
    health_wellness_grade VARCHAR(10) DEFAULT 'PENDING',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    entered_by BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_co_curricular_entry UNIQUE (school_id, term_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_co_curricular_filter ON exam_co_curricular_grades(school_id, term_id, class_id, section_id);

-- 3. Exam Teacher Remarks Table (Student Qualitative Assessment)
CREATE TABLE IF NOT EXISTS exam_teacher_remarks (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    term_id BIGINT NOT NULL REFERENCES exam_terms(id) ON DELETE CASCADE,
    class_id BIGINT NOT NULL REFERENCES class(id) ON DELETE CASCADE,
    section_id BIGINT REFERENCES sections(id) ON DELETE SET NULL,
    student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    previous_grade VARCHAR(10) DEFAULT 'B',
    teacher_remarks TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    entered_by BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_teacher_remark_entry UNIQUE (school_id, term_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_teacher_remarks_filter ON exam_teacher_remarks(school_id, term_id, class_id, section_id);

-- 4. Exam Grade Scales Table
CREATE TABLE IF NOT EXISTS exam_grade_scales (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    grade_point NUMERIC(4,2) DEFAULT 0.0,
    target_class VARCHAR(100) DEFAULT 'All Classes',
    percent_from NUMERIC(5,2) NOT NULL,
    percent_to NUMERIC(5,2) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    order_no INTEGER DEFAULT 1,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_grade_scales_lookup ON exam_grade_scales(school_id, target_class, status);

-- 5. Exam Divisions Table
CREATE TABLE IF NOT EXISTS exam_divisions (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    percent_from NUMERIC(5,2) NOT NULL,
    percent_to NUMERIC(5,2) NOT NULL,
    description TEXT,
    color_tag VARCHAR(20) DEFAULT 'BLUE',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    order_no INTEGER DEFAULT 1,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_exam_divisions_name UNIQUE (school_id, name)
);

CREATE INDEX IF NOT EXISTS idx_exam_divisions_lookup ON exam_divisions(school_id, status);

-- 6. Permissions for Edit Operations
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES 
    ('perm_exams_results_marks_entry_edit', 'exams_results.marks_entry.edit', 'EXAMS_RESULTS', 'marks_entry', 'edit', 'Enter Exam Marks', 'Allows entering and updating student exam marks', '["school"]', true, true),
    ('perm_exams_results_co_curricular_grades_edit', 'exams_results.co_curricular_grades.edit', 'EXAMS_RESULTS', 'co_curricular_grades', 'edit', 'Assess Co-Curricular Grades', 'Allows recording and updating co-curricular performance', '["school"]', true, true),
    ('perm_exams_results_teacher_remark_edit', 'exams_results.teacher_remark.edit', 'EXAMS_RESULTS', 'teacher_remark', 'edit', 'Enter Teacher Remarks', 'Allows submitting qualitative feedback and remarks for students', '["school"]', true, true),
    ('perm_exams_results_grade_list_edit', 'exams_results.grade_list.edit', 'EXAMS_RESULTS', 'grade_list', 'edit', 'Manage Grade Scales', 'Allows creating and configuring academic grade thresholds', '["school"]', true, true),
    ('perm_exams_results_division_edit', 'exams_results.division.edit', 'EXAMS_RESULTS', 'division', 'edit', 'Manage Divisions', 'Allows creating and configuring academic divisions', '["school"]', true, true)
ON CONFLICT (id) DO NOTHING;

-- Grant permissions to Super Admin, School Admin and Teacher
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_super_admin_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key LIKE 'exams_results.%'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_school_admin_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key LIKE 'exams_results.%'
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT 'role_teacher_global', pd.id, 'school', '{}'::jsonb
FROM permission_definitions pd
WHERE pd.permission_key IN (
    'exams_results.marks_entry.view',
    'exams_results.marks_entry.edit',
    'exams_results.co_curricular_grades.view',
    'exams_results.co_curricular_grades.edit',
    'exams_results.teacher_remark.view',
    'exams_results.teacher_remark.edit',
    'exams_results.grade_list.view',
    'exams_results.division.view'
)
ON CONFLICT DO NOTHING;

-- 7. Seed Sample Data matching the UI Mockups for School 1
DO $$
DECLARE
    v_school_id BIGINT;
    v_term1_id BIGINT;
    v_term2_id BIGINT;
    v_setup1_id BIGINT;
    v_class10_id BIGINT;
    v_sec_a_id BIGINT;
    v_subj_math_id BIGINT;
    v_s1 BIGINT;
    v_s2 BIGINT;
    v_s3 BIGINT;
    v_s4 BIGINT;
BEGIN
    SELECT id INTO v_school_id FROM schools WHERE id = 1;
    IF v_school_id IS NULL THEN
        SELECT id INTO v_school_id FROM schools LIMIT 1;
    END IF;

    IF v_school_id IS NOT NULL THEN
        SELECT id INTO v_term1_id FROM exam_terms WHERE school_id = v_school_id ORDER BY id ASC LIMIT 1;
        SELECT id INTO v_term2_id FROM exam_terms WHERE school_id = v_school_id ORDER BY id DESC LIMIT 1;
        SELECT id INTO v_setup1_id FROM exam_setups WHERE school_id = v_school_id ORDER BY id ASC LIMIT 1;
        SELECT id INTO v_class10_id FROM class WHERE school_id = v_school_id LIMIT 1;
        SELECT id INTO v_sec_a_id FROM sections WHERE school_id = v_school_id AND class_id = v_class10_id LIMIT 1;
        SELECT id INTO v_subj_math_id FROM subjects WHERE school_id = v_school_id AND (code = 'MATH101' OR name ILIKE 'Math%') LIMIT 1;

        -- 7.1 Seed Grade Scales
        INSERT INTO exam_grade_scales (school_id, name, grade_point, target_class, percent_from, percent_to, description, status, order_no)
        VALUES 
            (v_school_id, 'Distinction (A+)', 4.0, 'Senior High', 90.00, 100.00, 'Exemplary academic achievement', 'ACTIVE', 1),
            (v_school_id, 'Credit (B)', 3.0, 'Senior High', 75.00, 89.00, 'Above institutional average', 'ACTIVE', 2),
            (v_school_id, 'Pass (C)', 2.0, 'Junior High', 50.00, 74.00, 'Meets core requirements', 'ACTIVE', 3)
        ON CONFLICT DO NOTHING;

        -- 7.2 Seed Divisions
        INSERT INTO exam_divisions (school_id, name, percent_from, percent_to, description, color_tag, status, order_no)
        VALUES
            (v_school_id, 'Distinction', 80.00, 100.00, 'Exceptional performance...', 'GREEN', 'ACTIVE', 1),
            (v_school_id, 'First Division', 69.00, 79.00, 'Consistently high standard...', 'BLUE', 'ACTIVE', 2),
            (v_school_id, 'Second Division', 45.00, 58.00, 'Satisfactory academic level...', 'ORANGE', 'ACTIVE', 3),
            (v_school_id, 'Pass', 33.00, 44.00, 'Minimum passing criteria met...', 'RED', 'ACTIVE', 4)
        ON CONFLICT (school_id, name) DO NOTHING;

        -- 7.3 Seed Students for Marks & Remarks (Aditi Agarwal, Benjamin Joshua, Catherine Miller, David Watson)
        SELECT id INTO v_s1 FROM student WHERE school_id = v_school_id AND (name = 'Aditi Agarwal' OR roll_number = '1001') LIMIT 1;
        IF v_s1 IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'Aditi Agarwal', 'Aditi', 'Agarwal', 'SCH2024-001', '1001', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-01')
            RETURNING id INTO v_s1;
        END IF;

        SELECT id INTO v_s2 FROM student WHERE school_id = v_school_id AND (name = 'Benjamin Joshua' OR roll_number = '1002') LIMIT 1;
        IF v_s2 IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'Benjamin Joshua', 'Benjamin', 'Joshua', 'SCH2024-042', '1002', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-02')
            RETURNING id INTO v_s2;
        END IF;

        SELECT id INTO v_s3 FROM student WHERE school_id = v_school_id AND (name = 'Catherine Miller' OR roll_number = '1003') LIMIT 1;
        IF v_s3 IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'Catherine Miller', 'Catherine', 'Miller', 'SCH2024-718', '1003', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-03')
            RETURNING id INTO v_s3;
        END IF;

        SELECT id INTO v_s4 FROM student WHERE school_id = v_school_id AND (name = 'David Watson' OR roll_number = '1004') LIMIT 1;
        IF v_s4 IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'David Watson', 'David', 'Watson', 'SCH2024-056', '1004', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-04')
            RETURNING id INTO v_s4;
        END IF;

        -- 7.4 Seed Marks (matching screenshot: 00, 88, Medical Leave, 62)
        IF v_setup1_id IS NOT NULL AND v_subj_math_id IS NOT NULL THEN
            INSERT INTO exam_marks (school_id, exam_setup_id, class_id, section_id, subject_id, student_id, marks_obtained, max_marks, attendance_status, remarks)
            VALUES (v_school_id, v_setup1_id, v_class10_id, v_sec_a_id, v_subj_math_id, v_s1, 0.00, 100.00, 'PRESENT', 'Add feedback...')
            ON CONFLICT (school_id, exam_setup_id, subject_id, student_id) DO NOTHING;

            INSERT INTO exam_marks (school_id, exam_setup_id, class_id, section_id, subject_id, student_id, marks_obtained, max_marks, attendance_status, remarks)
            VALUES (v_school_id, v_setup1_id, v_class10_id, v_sec_a_id, v_subj_math_id, v_s2, 88.00, 100.00, 'PRESENT', 'Excellent logical')
            ON CONFLICT (school_id, exam_setup_id, subject_id, student_id) DO NOTHING;

            INSERT INTO exam_marks (school_id, exam_setup_id, class_id, section_id, subject_id, student_id, marks_obtained, max_marks, attendance_status, remarks)
            VALUES (v_school_id, v_setup1_id, v_class10_id, v_sec_a_id, v_subj_math_id, v_s3, NULL, 100.00, 'ABSENT', 'Medical Leave')
            ON CONFLICT (school_id, exam_setup_id, subject_id, student_id) DO NOTHING;

            INSERT INTO exam_marks (school_id, exam_setup_id, class_id, section_id, subject_id, student_id, marks_obtained, max_marks, attendance_status, remarks)
            VALUES (v_school_id, v_setup1_id, v_class10_id, v_sec_a_id, v_subj_math_id, v_s4, 62.00, 100.00, 'PRESENT', 'Add feedback...')
            ON CONFLICT (school_id, exam_setup_id, subject_id, student_id) DO NOTHING;
        END IF;

        -- 7.5 Seed Co-Curricular Grades (Adrian Jenkins, Beatrix Thorne, Caleb Wright, Daniel Choi)
        IF v_term1_id IS NOT NULL THEN
            INSERT INTO exam_co_curricular_grades (school_id, term_id, class_id, section_id, student_id, physical_education_grade, visual_arts_grade, performing_arts_grade, health_wellness_grade, status)
            VALUES (v_school_id, v_term1_id, v_class10_id, v_sec_a_id, v_s1, 'A+', 'A', 'B+', 'A+', 'COMPLETED')
            ON CONFLICT (school_id, term_id, student_id) DO NOTHING;

            INSERT INTO exam_co_curricular_grades (school_id, term_id, class_id, section_id, student_id, physical_education_grade, visual_arts_grade, performing_arts_grade, health_wellness_grade, status)
            VALUES (v_school_id, v_term1_id, v_class10_id, v_sec_a_id, v_s2, 'PENDING', 'B', 'A', 'PENDING', 'PENDING')
            ON CONFLICT (school_id, term_id, student_id) DO NOTHING;

            INSERT INTO exam_co_curricular_grades (school_id, term_id, class_id, section_id, student_id, physical_education_grade, visual_arts_grade, performing_arts_grade, health_wellness_grade, status)
            VALUES (v_school_id, v_term1_id, v_class10_id, v_sec_a_id, v_s3, 'A', 'A+', 'C+', 'B+', 'COMPLETED')
            ON CONFLICT (school_id, term_id, student_id) DO NOTHING;

            INSERT INTO exam_co_curricular_grades (school_id, term_id, class_id, section_id, student_id, physical_education_grade, visual_arts_grade, performing_arts_grade, health_wellness_grade, status)
            VALUES (v_school_id, v_term1_id, v_class10_id, v_sec_a_id, v_s4, 'B-', 'B+', 'PENDING', 'A', 'PENDING')
            ON CONFLICT (school_id, term_id, student_id) DO NOTHING;
        END IF;

        -- 7.6 Seed Teacher Remarks (matching screenshot: Aiden, Elena, Marcus, Zara)
        IF v_term1_id IS NOT NULL THEN
            INSERT INTO exam_teacher_remarks (school_id, term_id, class_id, section_id, student_id, previous_grade, teacher_remarks, status)
            VALUES (v_school_id, v_term1_id, v_class10_id, v_sec_a_id, v_s1, 'A+', 'Aiden has shown exceptional analytical skills this term. His participation in class discussions has been consistently strong.', 'COMPLETED')
            ON CONFLICT (school_id, term_id, student_id) DO NOTHING;

            INSERT INTO exam_teacher_remarks (school_id, term_id, class_id, section_id, student_id, previous_grade, teacher_remarks, status)
            VALUES (v_school_id, v_term1_id, v_class10_id, v_sec_a_id, v_s2, 'B', '', 'PENDING')
            ON CONFLICT (school_id, term_id, student_id) DO NOTHING;

            INSERT INTO exam_teacher_remarks (school_id, term_id, class_id, section_id, student_id, previous_grade, teacher_remarks, status)
            VALUES (v_school_id, v_term1_id, v_class10_id, v_sec_a_id, v_s3, 'A-', 'Consistently high marks in mathematics. Needs to focus more on humanities subjects to achieve a well-rounded academic profile.', 'COMPLETED')
            ON CONFLICT (school_id, term_id, student_id) DO NOTHING;

            INSERT INTO exam_teacher_remarks (school_id, term_id, class_id, section_id, student_id, previous_grade, teacher_remarks, status)
            VALUES (v_school_id, v_term1_id, v_class10_id, v_sec_a_id, v_s4, 'B+', '', 'PENDING')
            ON CONFLICT (school_id, term_id, student_id) DO NOTHING;
        END IF;

    END IF;
END $$;
