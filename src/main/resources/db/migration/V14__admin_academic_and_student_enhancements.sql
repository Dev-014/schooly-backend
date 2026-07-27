-- V14__admin_academic_and_student_enhancements.sql
-- Creates tables and sample data for Admin Academic and Student supporting operations

CREATE TABLE IF NOT EXISTS academic_years (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    start_date DATE,
    end_date DATE,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ay_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_ay_school_id ON academic_years(school_id);

CREATE TABLE IF NOT EXISTS sections (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL,
    school_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    room_number VARCHAR(50),
    capacity INT DEFAULT 40,
    CONSTRAINT fk_section_class FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE,
    CONSTRAINT fk_section_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_section_class_id ON sections(class_id);
CREATE INDEX IF NOT EXISTS idx_section_school_id ON sections(school_id);

CREATE TABLE IF NOT EXISTS subjects (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    code VARCHAR(50),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) DEFAULT 'THEORY',
    credits INT DEFAULT 3,
    grade_level VARCHAR(50),
    CONSTRAINT fk_subject_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_subject_school_id ON subjects(school_id);

CREATE TABLE IF NOT EXISTS timetable_periods (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    period_number INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    start_time VARCHAR(20),
    end_time VARCHAR(20),
    is_break BOOLEAN DEFAULT false,
    CONSTRAINT fk_period_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_period_school_id ON timetable_periods(school_id);

CREATE TABLE IF NOT EXISTS student_documents (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    school_id BIGINT NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    document_type VARCHAR(100),
    file_url VARCHAR(500),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_doc_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_doc_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_doc_student_id ON student_documents(student_id);

-- Seed Academic Years for School 1
INSERT INTO academic_years (id, school_id, name, start_date, end_date, status) VALUES
(1, 1, 'Academic Year 2025-2026', '2025-04-01', '2026-03-31', 'ACTIVE'),
(2, 1, 'Academic Year 2026-2027 (Upcoming)', '2026-04-01', '2027-03-31', 'UPCOMING')
ON CONFLICT DO NOTHING;

-- Seed Classes if missing for School 1
INSERT INTO class (id, name, school_id) VALUES
(1, 'Grade 10 - Standard', 1),
(2, 'Grade 11 - Science & Commerce', 1),
(3, 'Grade 12 - Advanced', 1)
ON CONFLICT DO NOTHING;

-- Seed Sections for Class 1, 2, 3 in School 1
INSERT INTO sections (id, class_id, school_id, name, room_number, capacity) VALUES
(1, 1, 1, 'Section A (Science Stream)', 'Room 101', 35),
(2, 1, 1, 'Section B (Commerce Stream)', 'Room 102', 40),
(3, 2, 1, 'Section A (General)', 'Room 201', 38),
(4, 3, 1, 'Section A (General)', 'Room 301', 36)
ON CONFLICT DO NOTHING;

-- Seed Subjects for School 1
INSERT INTO subjects (id, school_id, code, name, type, credits, grade_level) VALUES
(1, 1, 'PHY-101', 'Advanced Physics & Laboratory', 'PRACTICAL', 4, 'Grade 10'),
(2, 1, 'MAT-201', 'Calculus & Linear Algebra', 'THEORY', 4, 'Grade 10'),
(3, 1, 'ENG-101', 'English Literature & Composition', 'THEORY', 3, 'Grade 10'),
(4, 1, 'CHE-101', 'Organic & Inorganic Chemistry', 'PRACTICAL', 4, 'Grade 10'),
(5, 1, 'CSC-101', 'Computer Science & Python Coding', 'PRACTICAL', 4, 'Grade 10'),
(6, 1, 'ECO-101', 'Micro & Macro Economics', 'THEORY', 3, 'Grade 10')
ON CONFLICT DO NOTHING;

-- Seed Timetable Periods for School 1
INSERT INTO timetable_periods (id, school_id, period_number, name, start_time, end_time, is_break) VALUES
(1, 1, 1, 'Period 1', '08:30 AM', '09:15 AM', false),
(2, 1, 2, 'Period 2', '09:15 AM', '10:00 AM', false),
(3, 1, 3, 'Morning Break', '10:00 AM', '10:15 AM', true),
(4, 1, 4, 'Period 3', '10:15 AM', '11:00 AM', false),
(5, 1, 5, 'Period 4', '11:00 AM', '11:45 AM', false),
(6, 1, 6, 'Lunch Break', '11:45 AM', '12:30 PM', true),
(7, 1, 7, 'Period 5', '12:30 PM', '01:15 PM', false),
(8, 1, 8, 'Period 6', '01:15 PM', '02:00 PM', false)
ON CONFLICT DO NOTHING;

-- Seed Students if missing for School 1
INSERT INTO student (id, name, admission_no, class_id, school_id, status) VALUES
(1, 'Aarav Sharma', 'ADM-2025-001', 1, 1, 'ACTIVE'),
(2, 'Vihaan Patel', 'ADM-2025-002', 1, 1, 'ACTIVE'),
(3, 'Ananya Gupta', 'ADM-2025-003', 2, 1, 'ACTIVE')
ON CONFLICT DO NOTHING;

-- Seed Student Documents for existing Students (id 1, 2, 3)
INSERT INTO student_documents (id, student_id, school_id, document_name, document_type, file_url) VALUES
(1, 1, 1, 'Previous Academic Transcript 2024.pdf', 'TRANSCRIPT', 'https://storage.schooly.erp/docs/s1_transcript_2024.pdf'),
(2, 1, 1, 'Birth Certificate & ID Proof.pdf', 'IDENTITY_PROOF', 'https://storage.schooly.erp/docs/s1_birth_cert.pdf'),
(3, 2, 1, 'Transfer Certificate (TC).pdf', 'TRANSFER_CERTIFICATE', 'https://storage.schooly.erp/docs/s2_tc.pdf'),
(4, 3, 1, 'Medical Health Record.pdf', 'MEDICAL', 'https://storage.schooly.erp/docs/s3_medical.pdf')
ON CONFLICT DO NOTHING;

-- Reset sequences
SELECT setval('academic_years_id_seq', (SELECT MAX(id) FROM academic_years));
SELECT setval('sections_id_seq', (SELECT MAX(id) FROM sections));
SELECT setval('subjects_id_seq', (SELECT MAX(id) FROM subjects));
SELECT setval('timetable_periods_id_seq', (SELECT MAX(id) FROM timetable_periods));
SELECT setval('student_documents_id_seq', (SELECT MAX(id) FROM student_documents));
