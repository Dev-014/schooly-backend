-- ==========================================
-- V4__schema_improvements.sql
-- Upgrades Schooly WebApp DB Schema
-- ==========================================

-- 1. Create Master Roles Table & Link to user_school_roles
CREATE TABLE IF NOT EXISTS roles (
    role user_role PRIMARY KEY,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

INSERT INTO roles (role, description) VALUES
('SUPER_ADMIN', 'System-wide administrator with full access'),
('ADMIN', 'School-level administrator'),
('TEACHER', 'Teaching staff member'),
('STUDENT', 'Enrolled student'),
('PARENT', 'Parent or guardian of a student')
ON CONFLICT (role) DO NOTHING;

-- 2. Drop and Recreate Existing Foreign Keys with Explicit Cascade / Deletion Behavior
-- Altering class table
ALTER TABLE class DROP CONSTRAINT IF EXISTS fk_class_school;
ALTER TABLE class ADD CONSTRAINT fk_class_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT;

-- Altering student table
ALTER TABLE student DROP CONSTRAINT IF EXISTS fk_student_class;
ALTER TABLE student DROP CONSTRAINT IF EXISTS fk_student_school;
ALTER TABLE student ADD CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class (id) ON DELETE RESTRICT;
ALTER TABLE student ADD CONSTRAINT fk_student_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT;

-- Altering staff table
ALTER TABLE staff DROP CONSTRAINT IF EXISTS fk_staff_school;
ALTER TABLE staff ADD CONSTRAINT fk_staff_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT;

-- Altering fee_invoice table
ALTER TABLE fee_invoice DROP CONSTRAINT IF EXISTS fk_fee_invoice_student;
ALTER TABLE fee_invoice DROP CONSTRAINT IF EXISTS fk_fee_invoice_school;
ALTER TABLE fee_invoice ADD CONSTRAINT fk_fee_invoice_student FOREIGN KEY (student_id) REFERENCES student (id) ON DELETE RESTRICT;
ALTER TABLE fee_invoice ADD CONSTRAINT fk_fee_invoice_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT;

-- Altering payment table
ALTER TABLE payment DROP CONSTRAINT IF EXISTS fk_payment_invoice;
ALTER TABLE payment DROP CONSTRAINT IF EXISTS fk_payment_school;
ALTER TABLE payment ADD CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES fee_invoice (id) ON DELETE RESTRICT;
ALTER TABLE payment ADD CONSTRAINT fk_payment_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT;

-- Altering student_attendance table
ALTER TABLE student_attendance DROP CONSTRAINT IF EXISTS fk_attendance_student;
ALTER TABLE student_attendance DROP CONSTRAINT IF EXISTS fk_attendance_school;
ALTER TABLE student_attendance ADD CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES student (id) ON DELETE CASCADE;
ALTER TABLE student_attendance ADD CONSTRAINT fk_attendance_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT;

-- Altering user_school_roles table
ALTER TABLE user_school_roles DROP CONSTRAINT IF EXISTS fk_user_school_roles_user;
ALTER TABLE user_school_roles DROP CONSTRAINT IF EXISTS fk_user_school_roles_school;
ALTER TABLE user_school_roles ADD CONSTRAINT fk_user_school_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE user_school_roles ADD CONSTRAINT fk_user_school_roles_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT;
ALTER TABLE user_school_roles DROP CONSTRAINT IF EXISTS fk_user_school_roles_role;
ALTER TABLE user_school_roles ADD CONSTRAINT fk_user_school_roles_role FOREIGN KEY (role) REFERENCES roles (role) ON DELETE RESTRICT;

-- Altering student_parents table
ALTER TABLE student_parents DROP CONSTRAINT IF EXISTS fk_student_parents_student;
ALTER TABLE student_parents DROP CONSTRAINT IF EXISTS fk_student_parents_parent;
ALTER TABLE student_parents ADD CONSTRAINT fk_student_parents_student FOREIGN KEY (student_id) REFERENCES student (id) ON DELETE CASCADE;
ALTER TABLE student_parents ADD CONSTRAINT fk_student_parents_parent FOREIGN KEY (parent_user_id) REFERENCES users (id) ON DELETE CASCADE;

-- Altering auth_sessions table
ALTER TABLE auth_sessions DROP CONSTRAINT IF EXISTS fk_auth_sessions_user;
ALTER TABLE auth_sessions DROP CONSTRAINT IF EXISTS fk_auth_sessions_school;
ALTER TABLE auth_sessions ADD CONSTRAINT fk_auth_sessions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE auth_sessions ADD CONSTRAINT fk_auth_sessions_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE;

-- 3. Create Sections, Academic Years, and Subjects tables
CREATE TABLE IF NOT EXISTS sections (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_sections_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_sections_school_id ON sections (school_id);

CREATE TABLE IF NOT EXISTS academic_years (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_academic_years_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_academic_years_school_id ON academic_years (school_id);

-- Apply missing FK constraints on student table
ALTER TABLE student DROP CONSTRAINT IF EXISTS fk_student_section;
ALTER TABLE student ADD CONSTRAINT fk_student_section FOREIGN KEY (section_id) REFERENCES sections (id) ON DELETE SET NULL;

ALTER TABLE student DROP CONSTRAINT IF EXISTS fk_student_academic_year;
ALTER TABLE student ADD CONSTRAINT fk_student_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_years(id) ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS subjects (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_subjects_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_subjects_school_id ON subjects (school_id);

-- 4. Separate Teacher Entity (Ensures only teachers are mapped to subjects and timetables)
CREATE TABLE IF NOT EXISTS teachers (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL UNIQUE,
    specialization VARCHAR(255),
    qualification VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_teachers_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_teachers_staff_id ON teachers (staff_id);

CREATE TABLE IF NOT EXISTS teacher_subjects (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    section_id BIGINT,
    subject_id BIGINT NOT NULL,
    academic_year_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_teacher_subjects_teacher FOREIGN KEY (teacher_id) REFERENCES teachers (id) ON DELETE RESTRICT,
    CONSTRAINT fk_teacher_subjects_class FOREIGN KEY (class_id) REFERENCES class (id) ON DELETE RESTRICT,
    CONSTRAINT fk_teacher_subjects_section FOREIGN KEY (section_id) REFERENCES sections (id) ON DELETE SET NULL,
    CONSTRAINT fk_teacher_subjects_subject FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE RESTRICT,
    CONSTRAINT fk_teacher_subjects_academic_year FOREIGN KEY (academic_year_id) REFERENCES academic_years (id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_teacher_subjects_teacher_id ON teacher_subjects (teacher_id);
CREATE INDEX IF NOT EXISTS idx_teacher_subjects_class_id ON teacher_subjects (class_id);

-- 5. Exams, Grade Scales & Exam Results (Replaces free-text grade)
CREATE TABLE IF NOT EXISTS exams (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    exam_type VARCHAR(100),
    class_id BIGINT NOT NULL,
    section_id BIGINT,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_exams_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT,
    CONSTRAINT fk_exams_class FOREIGN KEY (class_id) REFERENCES class (id) ON DELETE RESTRICT,
    CONSTRAINT fk_exams_section FOREIGN KEY (section_id) REFERENCES sections (id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_exams_school_id ON exams (school_id);
CREATE INDEX IF NOT EXISTS idx_exams_class_id ON exams (class_id);

CREATE TABLE IF NOT EXISTS grade_scales (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    grade VARCHAR(10) NOT NULL,
    min_mark NUMERIC(5,2) NOT NULL,
    max_mark NUMERIC(5,2) NOT NULL,
    grade_point NUMERIC(3,2),
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_grade_scales_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT,
    CONSTRAINT uq_school_grade UNIQUE (school_id, grade),
    CONSTRAINT chk_min_max_marks CHECK (min_mark <= max_mark)
);
CREATE INDEX IF NOT EXISTS idx_grade_scales_school_id ON grade_scales (school_id);

CREATE TABLE IF NOT EXISTS exam_results (
    id BIGSERIAL PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    subject_id BIGINT NOT NULL,
    marks_obtained NUMERIC(6,2),
    max_marks NUMERIC(6,2),
    grade_scale_id BIGINT,
    remarks TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_exam_results_exam FOREIGN KEY (exam_id) REFERENCES exams (id) ON DELETE RESTRICT,
    CONSTRAINT fk_exam_results_student FOREIGN KEY (student_id) REFERENCES student (id) ON DELETE RESTRICT,
    CONSTRAINT fk_exam_results_subject FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE RESTRICT,
    CONSTRAINT fk_exam_results_grade_scale FOREIGN KEY (grade_scale_id) REFERENCES grade_scales (id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_exam_results_exam_id ON exam_results (exam_id);
CREATE INDEX IF NOT EXISTS idx_exam_results_student_id ON exam_results (student_id);

-- 6. Class Schedules (Timetable)
CREATE TABLE IF NOT EXISTS timetable (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    section_id BIGINT,
    subject_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_timetable_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT,
    CONSTRAINT fk_timetable_class FOREIGN KEY (class_id) REFERENCES class (id) ON DELETE RESTRICT,
    CONSTRAINT fk_timetable_section FOREIGN KEY (section_id) REFERENCES sections (id) ON DELETE SET NULL,
    CONSTRAINT fk_timetable_subject FOREIGN KEY (subject_id) REFERENCES subjects (id) ON DELETE RESTRICT,
    CONSTRAINT fk_timetable_teacher FOREIGN KEY (teacher_id) REFERENCES teachers (id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_timetable_class_id ON timetable (class_id);
CREATE INDEX IF NOT EXISTS idx_timetable_teacher_id ON timetable (teacher_id);

-- 7. Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    sender_id BIGINT,
    recipient_id BIGINT,
    recipient_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'UNREAD',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_notifications_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT,
    CONSTRAINT fk_notifications_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_notifications_school_id ON notifications (school_id);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_id ON notifications (recipient_id);

-- 8. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT,
    user_id BIGINT,
    action VARCHAR(255) NOT NULL,
    entity_name VARCHAR(100),
    entity_id VARCHAR(100),
    details JSONB,
    ip_address VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_school_id ON audit_logs (school_id);

-- 9. Roles & Permissions Bridge Table
CREATE TABLE IF NOT EXISTS roles_permissions (
    id BIGSERIAL PRIMARY KEY,
    role user_role NOT NULL,
    permission VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_roles_permissions_role FOREIGN KEY (role) REFERENCES roles (role) ON DELETE CASCADE,
    CONSTRAINT uq_role_permission UNIQUE (role, permission)
);
CREATE INDEX IF NOT EXISTS idx_roles_permissions_role ON roles_permissions (role);

-- 10. Transport Table
CREATE TABLE IF NOT EXISTS transport (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    vehicle_number VARCHAR(100) NOT NULL,
    driver_name VARCHAR(255),
    driver_phone VARCHAR(20),
    route_name VARCHAR(255),
    capacity INT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_transport_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_transport_school_id ON transport (school_id);

-- 11. Hostel Table
CREATE TABLE IF NOT EXISTS hostel (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    block VARCHAR(100),
    total_rooms INT,
    capacity INT,
    warden_name VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_hostel_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_hostel_school_id ON hostel (school_id);

-- 12. Internal Messages Table
CREATE TABLE IF NOT EXISTS messages (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT,
    subject VARCHAR(255),
    body TEXT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SENT',
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_messages_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT fk_messages_recipient FOREIGN KEY (recipient_id) REFERENCES users (id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_messages_sender_id ON messages (sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_recipient_id ON messages (recipient_id);

-- 13. Leave Management Tables (Addressing Leave Management Missing feedback)
CREATE TABLE IF NOT EXISTS staff_leaves (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL,
    school_id BIGINT NOT NULL,
    leave_type VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_staff_leaves_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE RESTRICT,
    CONSTRAINT fk_staff_leaves_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT,
    CONSTRAINT fk_staff_leaves_approved_by FOREIGN KEY (approved_by) REFERENCES users (id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_staff_leaves_staff_id ON staff_leaves (staff_id);
CREATE INDEX IF NOT EXISTS idx_staff_leaves_school_id ON staff_leaves (school_id);

CREATE TABLE IF NOT EXISTS student_leaves (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    school_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT fk_student_leaves_student FOREIGN KEY (student_id) REFERENCES student (id) ON DELETE RESTRICT,
    CONSTRAINT fk_student_leaves_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE RESTRICT,
    CONSTRAINT fk_student_leaves_approved_by FOREIGN KEY (approved_by) REFERENCES users (id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_student_leaves_student_id ON student_leaves (student_id);
CREATE INDEX IF NOT EXISTS idx_student_leaves_school_id ON student_leaves (school_id);

-- 14. Add soft delete column (deleted_at) to all pre-existing V1/V2 tables
ALTER TABLE schools ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE user_school_roles ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE class ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE student ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE staff ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE fee_invoice ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE payment ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE student_attendance ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
