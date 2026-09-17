-- V101__create_exam_attendance_and_reports_tables.sql
-- Migration for Exam Attendance (Attendance Tracking), Examination Reports, and Report Card Generation

-- 1. Exam Attendances Table
CREATE TABLE IF NOT EXISTS exam_attendances (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    term_id BIGINT NOT NULL REFERENCES exam_terms(id) ON DELETE CASCADE,
    exam_setup_id BIGINT REFERENCES exam_setups(id) ON DELETE SET NULL,
    exam_schedule_id BIGINT REFERENCES exam_schedules(id) ON DELETE SET NULL,
    class_id BIGINT NOT NULL REFERENCES class(id) ON DELETE CASCADE,
    section_id BIGINT REFERENCES sections(id) ON DELETE SET NULL,
    student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    room_number VARCHAR(50) DEFAULT 'Room 402B',
    seat_assignment VARCHAR(50) NOT NULL DEFAULT 'Row 1, Seat 1',
    attendance_status VARCHAR(20) NOT NULL DEFAULT 'PRESENT', -- PRESENT, ABSENT, LEAVE
    session_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    remarks TEXT,
    recorded_by BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_exam_attendance_entry UNIQUE (school_id, term_id, student_id)
);

CREATE INDEX IF NOT EXISTS idx_exam_attendance_filter ON exam_attendances(school_id, term_id, class_id, section_id);
CREATE INDEX IF NOT EXISTS idx_exam_attendance_room ON exam_attendances(school_id, room_number, session_status);

-- 2. Exam Report Cards Table
CREATE TABLE IF NOT EXISTS exam_report_cards (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    term_id BIGINT NOT NULL REFERENCES exam_terms(id) ON DELETE CASCADE,
    exam_setup_id BIGINT REFERENCES exam_setups(id) ON DELETE SET NULL,
    class_id BIGINT NOT NULL REFERENCES class(id) ON DELETE CASCADE,
    section_id BIGINT REFERENCES sections(id) ON DELETE SET NULL,
    student_id BIGINT NOT NULL REFERENCES student(id) ON DELETE CASCADE,
    generation_mode VARCHAR(20) NOT NULL DEFAULT 'TERM_WISE', -- TERM_WISE, EXAM_WISE
    template_name VARCHAR(100) DEFAULT 'Classic CBSE Standard',
    total_marks NUMERIC(6,2),
    max_marks NUMERIC(6,2) DEFAULT 100.00,
    percentage NUMERIC(5,2),
    grade VARCHAR(10),
    division VARCHAR(50),
    status VARCHAR(30) NOT NULL DEFAULT 'GENERATED', -- GENERATED, ISSUED, PENDING
    batch_id BIGINT,
    file_url TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_exam_report_card_entry UNIQUE (school_id, term_id, student_id, generation_mode)
);

CREATE INDEX IF NOT EXISTS idx_exam_report_cards_filter ON exam_report_cards(school_id, term_id, class_id, section_id);

-- 3. Exam Report Card Batches Table
CREATE TABLE IF NOT EXISTS exam_report_card_batches (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    batch_name VARCHAR(150) NOT NULL,
    term_id BIGINT REFERENCES exam_terms(id) ON DELETE SET NULL,
    exam_setup_id BIGINT REFERENCES exam_setups(id) ON DELETE SET NULL,
    class_id BIGINT REFERENCES class(id) ON DELETE SET NULL,
    section_id BIGINT REFERENCES sections(id) ON DELETE SET NULL,
    generation_mode VARCHAR(20) NOT NULL DEFAULT 'TERM_WISE',
    template_name VARCHAR(100) DEFAULT 'Classic CBSE Standard',
    total_count INTEGER NOT NULL DEFAULT 0,
    processed_count INTEGER NOT NULL DEFAULT 0,
    progress_percent NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'PROCESSING', -- PROCESSING, COMPLETED, FAILED
    completed_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_exam_report_card_batches_school ON exam_report_card_batches(school_id, status);

-- 4. Edit Permissions
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES 
    ('perm_exams_results_exam_attendance_edit', 'exams_results.exam_attendance.edit', 'EXAMS_RESULTS', 'exam_attendance', 'edit', 'Record Exam Attendance', 'Allows recording and updating examinee attendance and seating', '["school"]', true, true),
    ('perm_exams_results_report_card_edit', 'exams_results.report_card.edit', 'EXAMS_RESULTS', 'report_card', 'edit', 'Generate Report Cards', 'Allows configuring and issuing student academic report cards', '["school"]', true, true),
    ('perm_exams_results_examination_reports_edit', 'exams_results.examination_reports.edit', 'EXAMS_RESULTS', 'examination_reports', 'edit', 'Generate Examination Reports', 'Allows exporting and generating examination analytics and reports', '["school"]', true, true),
    ('perm_exams_results_report_generation_edit', 'exams_results.report_generation.edit', 'EXAMS_RESULTS', 'report_generation', 'edit', 'Manage Report Generation', 'Allows managing batch processing and templates for report generation', '["school"]', true, true)
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
    'exams_results.exam_attendance.view',
    'exams_results.exam_attendance.edit',
    'exams_results.examination_reports.view',
    'exams_results.report_card.view'
)
ON CONFLICT DO NOTHING;

-- 5. Seed Initial Data for Demonstration & Screenshot Alignment
DO $$
DECLARE
    v_school_id BIGINT;
    v_class10_id BIGINT;
    v_sec_a_id BIGINT;
    v_sec_b_id BIGINT;
    v_term1_id BIGINT;
    v_term2_id BIGINT;
    v_term_final_id BIGINT;
    v_setup1_id BIGINT;
    v_s1 BIGINT;
    v_s2 BIGINT;
    v_s3 BIGINT;
    v_s4 BIGINT;
    v_s5 BIGINT;
    v_sa BIGINT;
    v_sb BIGINT;
    v_batch1_id BIGINT;
BEGIN
    SELECT id INTO v_school_id FROM schools ORDER BY id ASC LIMIT 1;

    IF v_school_id IS NOT NULL THEN
        SELECT id INTO v_class10_id FROM class WHERE school_id = v_school_id AND (name = 'Grade 10' OR name = 'Class 10') LIMIT 1;
        IF v_class10_id IS NOT NULL THEN
            SELECT id INTO v_sec_a_id FROM sections WHERE class_id = v_class10_id AND (name = 'A' OR name = 'Section A' OR name = 'Section A (Sci)') LIMIT 1;
            SELECT id INTO v_sec_b_id FROM sections WHERE class_id = v_class10_id AND (name = 'B' OR name = 'Section B') LIMIT 1;
        END IF;

        -- Ensure Terms (Mid-Term 2024, Final Examination, etc.)
        SELECT id INTO v_term1_id FROM exam_terms WHERE school_id = v_school_id AND name LIKE '%Mid-Term%' LIMIT 1;
        IF v_term1_id IS NULL THEN
            INSERT INTO exam_terms (school_id, name, description, start_date, end_date, status)
            VALUES (v_school_id, 'Mid-Term 2024', 'Mid-Term Examination Session 2024', '2024-09-01', '2024-09-20', 'ACTIVE')
            RETURNING id INTO v_term1_id;
        END IF;

        SELECT id INTO v_term_final_id FROM exam_terms WHERE school_id = v_school_id AND (name LIKE '%Final%' OR name LIKE '%Semester%') LIMIT 1;
        IF v_term_final_id IS NULL THEN
            INSERT INTO exam_terms (school_id, name, description, start_date, end_date, status)
            VALUES (v_school_id, 'Final Examination 2023-2024', 'Final Annual Examination', '2024-03-01', '2024-03-25', 'ACTIVE')
            RETURNING id INTO v_term_final_id;
        END IF;

        SELECT id INTO v_setup1_id FROM exam_setups WHERE school_id = v_school_id AND term_id = v_term1_id LIMIT 1;

        -- 5.1 Seed Students for Attendance Roster (Adrian Bennett, Clarissa Chen, Daniel Wright, Elena Kovic, Franklin Reed)
        SELECT id INTO v_s1 FROM student WHERE school_id = v_school_id AND (name = 'Adrian Bennett' OR admission_no = 'STU-2024-001') LIMIT 1;
        IF v_s1 IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'Adrian Bennett', 'Adrian', 'Bennett', 'STU-2024-001', '001', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-01')
            RETURNING id INTO v_s1;
        END IF;

        SELECT id INTO v_s2 FROM student WHERE school_id = v_school_id AND (name = 'Clarissa Chen' OR admission_no = 'STU-2024-042') LIMIT 1;
        IF v_s2 IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'Clarissa Chen', 'Clarissa', 'Chen', 'STU-2024-042', '002', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-02')
            RETURNING id INTO v_s2;
        END IF;

        SELECT id INTO v_s3 FROM student WHERE school_id = v_school_id AND (name = 'Daniel Wright' OR admission_no = 'STU-2024-106') LIMIT 1;
        IF v_s3 IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'Daniel Wright', 'Daniel', 'Wright', 'STU-2024-106', '003', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-03')
            RETURNING id INTO v_s3;
        END IF;

        SELECT id INTO v_s4 FROM student WHERE school_id = v_school_id AND (name = 'Elena Kovic' OR admission_no = 'STU-2024-088') LIMIT 1;
        IF v_s4 IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'Elena Kovic', 'Elena', 'Kovic', 'STU-2024-088', '004', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-04')
            RETURNING id INTO v_s4;
        END IF;

        SELECT id INTO v_s5 FROM student WHERE school_id = v_school_id AND (name = 'Franklin Reed' OR admission_no = 'STU-2024-115') LIMIT 1;
        IF v_s5 IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'Franklin Reed', 'Franklin', 'Reed', 'STU-2024-115', '005', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-05')
            RETURNING id INTO v_s5;
        END IF;

        -- 5.2 Seed Attendance Records
        INSERT INTO exam_attendances (school_id, term_id, exam_setup_id, class_id, section_id, student_id, room_number, seat_assignment, attendance_status, session_status)
        VALUES 
            (v_school_id, v_term1_id, v_setup1_id, v_class10_id, v_sec_a_id, v_s1, 'Room 402B', 'Row 1, Seat 4', 'PRESENT', 'ACTIVE'),
            (v_school_id, v_term1_id, v_setup1_id, v_class10_id, v_sec_a_id, v_s2, 'Room 402B', 'Row 1, Seat 5', 'ABSENT', 'ACTIVE'),
            (v_school_id, v_term1_id, v_setup1_id, v_class10_id, v_sec_a_id, v_s3, 'Room 402B', 'Row 2, Seat 1', 'PRESENT', 'ACTIVE'),
            (v_school_id, v_term1_id, v_setup1_id, v_class10_id, v_sec_a_id, v_s4, 'Room 402B', 'Row 2, Seat 2', 'LEAVE', 'ACTIVE'),
            (v_school_id, v_term1_id, v_setup1_id, v_class10_id, v_sec_a_id, v_s5, 'Room 402B', 'Row 2, Seat 3', 'PRESENT', 'ACTIVE')
        ON CONFLICT (school_id, term_id, student_id) DO UPDATE 
        SET seat_assignment = EXCLUDED.seat_assignment,
            attendance_status = EXCLUDED.attendance_status,
            room_number = EXCLUDED.room_number;

        -- 5.3 Seed Students for Detailed Result Preview (Alice Abbott, Benjamin Brooks)
        SELECT id INTO v_sa FROM student WHERE school_id = v_school_id AND (name = 'Alice Abbott' OR roll_number = '101') LIMIT 1;
        IF v_sa IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'Alice Abbott', 'Alice', 'Abbott', 'STU-2024-101', '101', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-10')
            RETURNING id INTO v_sa;
        END IF;

        SELECT id INTO v_sb FROM student WHERE school_id = v_school_id AND (name = 'Benjamin Brooks' OR roll_number = '102') LIMIT 1;
        IF v_sb IS NULL THEN
            INSERT INTO student (school_id, name, first_name, last_name, admission_no, roll_number, class_id, section_id, status, admission_date)
            VALUES (v_school_id, 'Benjamin Brooks', 'Benjamin', 'Brooks', 'STU-2024-102', '102', v_class10_id, v_sec_a_id, 'ACTIVE', '2024-01-11')
            RETURNING id INTO v_sb;
        END IF;

        -- 5.4 Seed Report Cards
        INSERT INTO exam_report_cards (school_id, term_id, exam_setup_id, class_id, section_id, student_id, generation_mode, template_name, total_marks, max_marks, percentage, grade, division, status)
        VALUES 
            (v_school_id, v_term_final_id, v_setup1_id, v_class10_id, v_sec_a_id, v_sa, 'TERM_WISE', 'Classic CBSE Standard', 93.00, 100.00, 93.00, 'A+', 'Distinction', 'GENERATED'),
            (v_school_id, v_term_final_id, v_setup1_id, v_class10_id, v_sec_a_id, v_sb, 'TERM_WISE', 'Classic CBSE Standard', 86.00, 100.00, 86.00, 'A', 'First Division', 'GENERATED')
        ON CONFLICT (school_id, term_id, student_id, generation_mode) DO NOTHING;

        -- 5.5 Seed Report Card Batches (matching 85% processing, 357 of 420, and recently generated list)
        INSERT INTO exam_report_card_batches (school_id, batch_name, term_id, class_id, section_id, generation_mode, total_count, processed_count, progress_percent, status, created_at)
        VALUES 
            (v_school_id, 'Final Summative Assessment - Grade 10', v_term_final_id, v_class10_id, v_sec_a_id, 'TERM_WISE', 420, 357, 85.00, 'PROCESSING', CURRENT_TIMESTAMP),
            (v_school_id, 'Grade 10 - Sec B', v_term_final_id, v_class10_id, v_sec_b_id, 'TERM_WISE', 40, 40, 100.00, 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '5 minutes'),
            (v_school_id, 'Grade 12 - Finals', v_term_final_id, v_class10_id, NULL, 'TERM_WISE', 60, 60, 100.00, 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '3 hours');

    END IF;
END $$;
