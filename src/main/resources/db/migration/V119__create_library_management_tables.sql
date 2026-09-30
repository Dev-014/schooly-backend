-- V119__create_library_management_tables.sql
-- Creates tables for library catalog, member cards, and issue/return circulation management

CREATE TABLE IF NOT EXISTS library_books (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    book_number VARCHAR(100),
    isbn VARCHAR(100),
    author VARCHAR(255) NOT NULL,
    publisher VARCHAR(255),
    category VARCHAR(100),
    rack_location VARCHAR(100),
    quantity INT NOT NULL DEFAULT 1,
    available_copies INT NOT NULL DEFAULT 1,
    price NUMERIC(10, 2),
    edition VARCHAR(100),
    description TEXT,
    cover_image_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'IN STOCK',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lib_book_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_lib_books_school ON library_books (school_id);
CREATE INDEX IF NOT EXISTS idx_lib_books_status ON library_books (school_id, status);
CREATE INDEX IF NOT EXISTS idx_lib_books_category ON library_books (school_id, category);

CREATE TABLE IF NOT EXISTS library_members (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    member_type VARCHAR(50) NOT NULL DEFAULT 'STUDENT',
    card_number VARCHAR(100) NOT NULL,
    student_id BIGINT,
    staff_id BIGINT,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    class_section VARCHAR(100),
    admission_number VARCHAR(100),
    max_books_allowed INT NOT NULL DEFAULT 3,
    active_issued_count INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lib_mem_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_lib_mem_student FOREIGN KEY (student_id) REFERENCES student (id) ON DELETE SET NULL,
    CONSTRAINT fk_lib_mem_staff FOREIGN KEY (staff_id) REFERENCES staff (id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_lib_members_school ON library_members (school_id);
CREATE INDEX IF NOT EXISTS idx_lib_members_card ON library_members (school_id, card_number);
CREATE INDEX IF NOT EXISTS idx_lib_members_status ON library_members (school_id, status);

CREATE TABLE IF NOT EXISTS library_circulations (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    issue_date DATE NOT NULL DEFAULT CURRENT_DATE,
    due_date DATE NOT NULL,
    return_date DATE,
    fine_amount NUMERIC(10, 2) DEFAULT 0.00,
    fine_paid BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(50) NOT NULL DEFAULT 'Issued',
    issued_by VARCHAR(100),
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_lib_circ_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE,
    CONSTRAINT fk_lib_circ_book FOREIGN KEY (book_id) REFERENCES library_books (id) ON DELETE CASCADE,
    CONSTRAINT fk_lib_circ_member FOREIGN KEY (member_id) REFERENCES library_members (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_lib_circ_school ON library_circulations (school_id);
CREATE INDEX IF NOT EXISTS idx_lib_circ_status ON library_circulations (school_id, status);
CREATE INDEX IF NOT EXISTS idx_lib_circ_book ON library_circulations (book_id);
CREATE INDEX IF NOT EXISTS idx_lib_circ_member ON library_circulations (member_id);
CREATE INDEX IF NOT EXISTS idx_lib_circ_due ON library_circulations (school_id, due_date);
