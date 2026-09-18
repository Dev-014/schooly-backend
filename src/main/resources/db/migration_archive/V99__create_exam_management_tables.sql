-- V99__create_exam_management_tables.sql
-- Migration for Exam & Result module: Term Setup, Exam Setup, Exam Schedule, and Admit Cards

-- 1. Exam Terms Table
CREATE TABLE IF NOT EXISTS exam_terms (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    academic_year_id BIGINT REFERENCES academic_years(id) ON DELETE SET NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    result_publish_date DATE,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_exam_terms_school_name UNIQUE (school_id, name)
);

CREATE INDEX IF NOT EXISTS idx_exam_terms_school_year ON exam_terms(school_id, academic_year_id);

-- 2. Exam Setups Table (Configure New Examination)
CREATE TABLE IF NOT EXISTS exam_setups (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    term_id BIGINT NOT NULL REFERENCES exam_terms(id) ON DELETE CASCADE,
    order_no INTEGER NOT NULL DEFAULT 1,
    name VARCHAR(150) NOT NULL,
    group_name VARCHAR(100),
    best_of_count INTEGER,
    weightage_active BOOLEAN DEFAULT FALSE,
    weightage_percent NUMERIC(5,2) DEFAULT 0.00,
    evaluation_type VARCHAR(50) DEFAULT 'STANDARD',
    internal_note TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_exam_setups_school_term_name UNIQUE (school_id, term_id, name)
);

CREATE INDEX IF NOT EXISTS idx_exam_setups_school_term ON exam_setups(school_id, term_id);

-- 3. Exam Schedules Table
CREATE TABLE IF NOT EXISTS exam_schedules (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    exam_setup_id BIGINT NOT NULL REFERENCES exam_setups(id) ON DELETE CASCADE,
    class_id BIGINT NOT NULL REFERENCES class(id) ON DELETE CASCADE,
    section_id BIGINT REFERENCES sections(id) ON DELETE SET NULL,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    exam_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room_number VARCHAR(100),
    full_marks NUMERIC(6,2) NOT NULL DEFAULT 100.00,
    passing_marks NUMERIC(6,2) NOT NULL DEFAULT 35.00,
    instructions TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_exam_schedule_entry UNIQUE (school_id, exam_setup_id, class_id, section_id, subject_id)
);

CREATE INDEX IF NOT EXISTS idx_exam_schedules_search ON exam_schedules(school_id, exam_setup_id, class_id, exam_date);
CREATE INDEX IF NOT EXISTS idx_exam_schedules_subject ON exam_schedules(school_id, subject_id);

-- 4. Exam Admit Cards Table
CREATE TABLE IF NOT EXISTS exam_admit_cards (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    exam_setup_id BIGINT NOT NULL REFERENCES exam_setups(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    class_id BIGINT NOT NULL REFERENCES class(id) ON DELETE CASCADE,
    section_id BIGINT REFERENCES sections(id) ON DELETE SET NULL,
    roll_number VARCHAR(50),
    card_number VARCHAR(100) UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    generated_at TIMESTAMP WITHOUT TIME ZONE,
    released_at TIMESTAMP WITHOUT TIME ZONE,
    template_name VARCHAR(100) DEFAULT 'STANDARD',
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_admit_card_entry UNIQUE (school_id, exam_setup_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_admit_cards_lookup ON exam_admit_cards(school_id, exam_setup_id, class_id, status);
CREATE INDEX IF NOT EXISTS idx_admit_cards_student ON exam_admit_cards(school_id, student_id);

-- 5. Permission Definitions for Edit Operations
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES 
    ('perm_exams_results_term_setup_edit', 'exams_results.term_setup.edit', 'EXAMS_RESULTS', 'term_setup', 'edit', 'Manage Term Setup', 'Allows configuring and modifying exam terms', '["school"]', true, true),
    ('perm_exams_results_exam_setup_student_edit', 'exams_results.exam_setup_student.edit', 'EXAMS_RESULTS', 'exam_setup_student', 'edit', 'Manage Exam Setup', 'Allows configuring examination structures, grouping and weightage', '["school"]', true, true),
    ('perm_exams_results_exam_schedule_student_edit', 'exams_results.exam_schedule_student.edit', 'EXAMS_RESULTS', 'exam_schedule_student', 'edit', 'Manage Exam Schedule', 'Allows creating and editing examination timetable schedules', '["school"]', true, true),
    ('perm_exams_results_admit_card_students_edit', 'exams_results.admit_card_students.edit', 'EXAMS_RESULTS', 'admit_card_students', 'edit', 'Issue Admit Cards', 'Allows generating, printing and releasing admit cards', '["school"]', true, true)
ON CONFLICT (id) DO NOTHING;

-- Grant permissions to Super Admin & School Admin
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
    'exams_results.term_setup.view',
    'exams_results.exam_setup_student.view',
    'exams_results.exam_schedule_student.view',
    'exams_results.admit_card_students.view'
)
ON CONFLICT DO NOTHING;

-- 6. Seed Sample Data matching the Mockup Screens for School 1
DO $$
DECLARE
    v_school_id BIGINT;
    v_acad_year_id BIGINT;
    v_term1_id BIGINT;
    v_term2_id BIGINT;
    v_setup1_id BIGINT;
    v_setup2_id BIGINT;
    v_setup3_id BIGINT;
    v_class10_id BIGINT;
    v_sec_a_id BIGINT;
    v_subj_math_id BIGINT;
    v_subj_phy_id BIGINT;
    v_subj_lit_id BIGINT;
    v_subj_bio_id BIGINT;
    v_std1_id BIGINT;
    v_std2_id BIGINT;
    v_std3_id BIGINT;
    v_std4_id BIGINT;
BEGIN
    SELECT id INTO v_school_id FROM schools WHERE id = 1;
    IF v_school_id IS NULL THEN
        SELECT id INTO v_school_id FROM schools LIMIT 1;
    END IF;

    IF v_school_id IS NOT NULL THEN
        -- Get or create Academic Year
        SELECT id INTO v_acad_year_id FROM academic_years WHERE school_id = v_school_id LIMIT 1;
        IF v_acad_year_id IS NULL THEN
            INSERT INTO academic_years (school_id, name, display_name, start_date, end_date, status)
            VALUES (v_school_id, '2024-2025', 'Academic Session 2024-25', '2024-04-01', '2025-03-31', 'ACTIVE')
            RETURNING id INTO v_acad_year_id;
        END IF;

        -- 1. Terms
        INSERT INTO exam_terms (school_id, academic_year_id, name, description, start_date, end_date, result_publish_date, status)
        VALUES (v_school_id, v_acad_year_id, 'First Term Examination', 'Main Academic Session', '2024-04-01', '2024-09-30', '2024-10-15', 'ACTIVE')
        ON CONFLICT (school_id, name) DO UPDATE SET description = EXCLUDED.description
        RETURNING id INTO v_term1_id;

        INSERT INTO exam_terms (school_id, academic_year_id, name, description, start_date, end_date, result_publish_date, status)
        VALUES (v_school_id, v_acad_year_id, 'Second Term Examination', 'Main Academic Session', '2024-10-01', '2025-03-31', '2025-04-10', 'SCHEDULED')
        ON CONFLICT (school_id, name) DO UPDATE SET description = EXCLUDED.description
        RETURNING id INTO v_term2_id;

        -- 2. Exam Setups
        INSERT INTO exam_setups (school_id, term_id, order_no, name, group_name, best_of_count, weightage_active, weightage_percent, evaluation_type, internal_note, status)
        VALUES (v_school_id, v_term1_id, 1, 'Unit Test I', 'Scholastic', 1, TRUE, 20.00, 'BEST_OF_N', 'Covers chapters 1 to 4', 'ACTIVE')
        ON CONFLICT (school_id, term_id, name) DO NOTHING
        RETURNING id INTO v_setup1_id;

        IF v_setup1_id IS NULL THEN
            SELECT id INTO v_setup1_id FROM exam_setups WHERE school_id = v_school_id AND term_id = v_term1_id AND name = 'Unit Test I';
        END IF;

        INSERT INTO exam_setups (school_id, term_id, order_no, name, group_name, best_of_count, weightage_active, weightage_percent, evaluation_type, internal_note, status)
        VALUES (v_school_id, v_term1_id, 2, 'Mid-Sessionals', 'Core Science', NULL, TRUE, 40.00, 'STANDARD', 'Mid-year evaluation syllabus', 'ACTIVE')
        ON CONFLICT (school_id, term_id, name) DO NOTHING
        RETURNING id INTO v_setup2_id;

        IF v_setup2_id IS NULL THEN
            SELECT id INTO v_setup2_id FROM exam_setups WHERE school_id = v_school_id AND term_id = v_term1_id AND name = 'Mid-Sessionals';
        END IF;

        INSERT INTO exam_setups (school_id, term_id, order_no, name, group_name, best_of_count, weightage_active, weightage_percent, evaluation_type, internal_note, status)
        VALUES (v_school_id, v_term2_id, 3, 'Final Practical', 'Laboratories', NULL, FALSE, 0.00, 'PERFORMANCE_BASED', 'Practical laboratory assessments', 'ACTIVE')
        ON CONFLICT (school_id, term_id, name) DO NOTHING
        RETURNING id INTO v_setup3_id;

        -- 3. Class & Section
        SELECT id INTO v_class10_id FROM class WHERE school_id = v_school_id AND name ILIKE '%Grade 10%' LIMIT 1;
        IF v_class10_id IS NULL THEN
            SELECT id INTO v_class10_id FROM class WHERE school_id = v_school_id LIMIT 1;
        END IF;

        IF v_class10_id IS NULL THEN
            INSERT INTO class (school_id, name, grade, level) VALUES (v_school_id, 'Grade 10', 10, 'SECONDARY') RETURNING id INTO v_class10_id;
        END IF;

        SELECT id INTO v_sec_a_id FROM sections WHERE school_id = v_school_id AND class_id = v_class10_id LIMIT 1;
        IF v_sec_a_id IS NULL THEN
            INSERT INTO sections (school_id, class_id, name, room_number, capacity) VALUES (v_school_id, v_class10_id, 'A', '101', 40) RETURNING id INTO v_sec_a_id;
        END IF;

        -- 4. Subjects
        SELECT id INTO v_subj_math_id FROM subjects WHERE school_id = v_school_id AND (code = 'MATH101' OR name ILIKE 'Math%') LIMIT 1;
        IF v_subj_math_id IS NULL THEN
            INSERT INTO subjects (school_id, code, name, type, credits, status) VALUES (v_school_id, 'MATH101', 'Mathematics', 'THEORY', 4, 'ACTIVE') RETURNING id INTO v_subj_math_id;
        END IF;

        SELECT id INTO v_subj_phy_id FROM subjects WHERE school_id = v_school_id AND (code = 'PHY101' OR name ILIKE 'Phys%') LIMIT 1;
        IF v_subj_phy_id IS NULL THEN
            INSERT INTO subjects (school_id, code, name, type, credits, status) VALUES (v_school_id, 'PHY101', 'Physics', 'THEORY', 4, 'ACTIVE') RETURNING id INTO v_subj_phy_id;
        END IF;

        SELECT id INTO v_subj_lit_id FROM subjects WHERE school_id = v_school_id AND (code = 'LIT101' OR name ILIKE 'Lit%' OR name ILIKE 'Eng%') LIMIT 1;
        IF v_subj_lit_id IS NULL THEN
            INSERT INTO subjects (school_id, code, name, type, credits, status) VALUES (v_school_id, 'LIT101', 'Literature', 'THEORY', 3, 'ACTIVE') RETURNING id INTO v_subj_lit_id;
        END IF;

        SELECT id INTO v_subj_bio_id FROM subjects WHERE school_id = v_school_id AND (code = 'BIO101' OR name ILIKE 'Bio%') LIMIT 1;
        IF v_subj_bio_id IS NULL THEN
            INSERT INTO subjects (school_id, code, name, type, credits, status) VALUES (v_school_id, 'BIO101', 'Biology', 'THEORY', 3, 'ACTIVE') RETURNING id INTO v_subj_bio_id;
        END IF;

        -- 5. Schedules (matching image: Math, Physics, Literature, Biology)
        IF v_setup1_id IS NOT NULL AND v_class10_id IS NOT NULL THEN
            INSERT INTO exam_schedules (school_id, exam_setup_id, class_id, section_id, subject_id, exam_date, start_time, end_time, room_number, full_marks, passing_marks, instructions, status)
            VALUES (v_school_id, v_setup1_id, v_class10_id, v_sec_a_id, v_subj_math_id, '2024-05-15', '09:00:00', '12:00:00', 'Hall A-102', 100.00, 35.00, 'Calculators not allowed', 'SCHEDULED')
            ON CONFLICT (school_id, exam_setup_id, class_id, section_id, subject_id) DO NOTHING;

            INSERT INTO exam_schedules (school_id, exam_setup_id, class_id, section_id, subject_id, exam_date, start_time, end_time, room_number, full_marks, passing_marks, instructions, status)
            VALUES (v_school_id, v_setup1_id, v_class10_id, v_sec_a_id, v_subj_phy_id, '2024-05-17', '14:00:00', '17:00:00', 'Lab 3', 75.00, 25.00, 'Bring lab equipment kit', 'SCHEDULED')
            ON CONFLICT (school_id, exam_setup_id, class_id, section_id, subject_id) DO NOTHING;

            INSERT INTO exam_schedules (school_id, exam_setup_id, class_id, section_id, subject_id, exam_date, start_time, end_time, room_number, full_marks, passing_marks, instructions, status)
            VALUES (v_school_id, v_setup1_id, v_class10_id, v_sec_a_id, v_subj_lit_id, '2024-05-18', '09:00:00', '12:00:00', 'Room 402', 100.00, 35.00, 'Standard writing materials', 'SCHEDULED')
            ON CONFLICT (school_id, exam_setup_id, class_id, section_id, subject_id) DO NOTHING;

            INSERT INTO exam_schedules (school_id, exam_setup_id, class_id, section_id, subject_id, exam_date, start_time, end_time, room_number, full_marks, passing_marks, instructions, status)
            VALUES (v_school_id, v_setup1_id, v_class10_id, v_sec_a_id, v_subj_bio_id, '2024-05-20', '11:00:00', '14:00:00', 'Main Auditorium', 100.00, 35.00, 'Specimen analysis included', 'SCHEDULED')
            ON CONFLICT (school_id, exam_setup_id, class_id, section_id, subject_id) DO NOTHING;
        END IF;

        -- 6. Ensure Students exist (matching image: Adrian Agreste, Diane Castillo, Ethan Knight, Fiona Lane)
        SELECT id INTO v_std1_id FROM student WHERE school_id = v_school_id AND (name = 'Adrian Agreste' OR roll_number = '102401') LIMIT 1;
        IF v_std1_id IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'Adrian Agreste', 'Adrian', 'Agreste', 'ADM-102401', '102401', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-10')
            RETURNING id INTO v_std1_id;
        END IF;

        SELECT id INTO v_std2_id FROM student WHERE school_id = v_school_id AND (name = 'Diane Castillo' OR roll_number = '102402') LIMIT 1;
        IF v_std2_id IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'Diane Castillo', 'Diane', 'Castillo', 'ADM-102402', '102402', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-11')
            RETURNING id INTO v_std2_id;
        END IF;

        SELECT id INTO v_std3_id FROM student WHERE school_id = v_school_id AND (name = 'Ethan Knight' OR roll_number = '102403') LIMIT 1;
        IF v_std3_id IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'Ethan Knight', 'Ethan', 'Knight', 'ADM-102403', '102403', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-12')
            RETURNING id INTO v_std3_id;
        END IF;

        SELECT id INTO v_std4_id FROM student WHERE school_id = v_school_id AND (name = 'Fiona Lane' OR roll_number = '102404') LIMIT 1;
        IF v_std4_id IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'Fiona Lane', 'Fiona', 'Lane', 'ADM-102404', '102404', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-13')
            RETURNING id INTO v_std4_id;
        END IF;

        -- 7. Admit Cards
        IF v_setup1_id IS NOT NULL THEN
            INSERT INTO exam_admit_cards (school_id, exam_setup_id, student_id, class_id, section_id, roll_number, card_number, status, generated_at)
            VALUES (v_school_id, v_setup1_id, v_std1_id, v_class10_id, v_sec_a_id, '102401', 'AC-2024-102401', 'GENERATED', CURRENT_TIMESTAMP)
            ON CONFLICT (school_id, exam_setup_id, student_id) DO NOTHING;

            INSERT INTO exam_admit_cards (school_id, exam_setup_id, student_id, class_id, section_id, roll_number, card_number, status)
            VALUES (v_school_id, v_setup1_id, v_std2_id, v_class10_id, v_sec_a_id, '102402', 'AC-2024-102402', 'PENDING')
            ON CONFLICT (school_id, exam_setup_id, student_id) DO NOTHING;

            INSERT INTO exam_admit_cards (school_id, exam_setup_id, student_id, class_id, section_id, roll_number, card_number, status, generated_at)
            VALUES (v_school_id, v_setup1_id, v_std3_id, v_class10_id, v_sec_a_id, '102403', 'AC-2024-102403', 'GENERATED', CURRENT_TIMESTAMP)
            ON CONFLICT (school_id, exam_setup_id, student_id) DO NOTHING;

            INSERT INTO exam_admit_cards (school_id, exam_setup_id, student_id, class_id, section_id, roll_number, card_number, status, generated_at)
            VALUES (v_school_id, v_setup1_id, v_std4_id, v_class10_id, v_sec_a_id, '102404', 'AC-2024-102404', 'GENERATED', CURRENT_TIMESTAMP)
            ON CONFLICT (school_id, exam_setup_id, student_id) DO NOTHING;
        END IF;

    END IF;
END $$;
