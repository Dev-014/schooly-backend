CREATE TABLE IF NOT EXISTS school (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    contact_email VARCHAR(255),


    contact_phone VARCHAR(255),
    address VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS class (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    school_id BIGINT NOT NULL,
    CONSTRAINT fk_class_school FOREIGN KEY (school_id) REFERENCES school (id)
);

CREATE INDEX IF NOT EXISTS idx_class_school_id ON class (school_id);

CREATE TABLE IF NOT EXISTS student (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    admission_no VARCHAR(255) NOT NULL,
    roll_number VARCHAR(255),
    class_id BIGINT NOT NULL,
    section_id BIGINT,
    academic_year_id BIGINT,
    school_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL,
    admission_date DATE,
    CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class (id),
    CONSTRAINT fk_student_school FOREIGN KEY (school_id) REFERENCES school (id)
);

CREATE INDEX IF NOT EXISTS idx_student_class_id ON student (class_id);
CREATE INDEX IF NOT EXISTS idx_student_school_id ON student (school_id);

CREATE TABLE IF NOT EXISTS staff (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    school_id BIGINT NOT NULL,
    department_id BIGINT,
    designation_id BIGINT,
    joining_date DATE,
    salary NUMERIC(19, 2),
    status VARCHAR(255),
    CONSTRAINT fk_staff_school FOREIGN KEY (school_id) REFERENCES school (id)
);

CREATE INDEX IF NOT EXISTS idx_staff_school_id ON staff (school_id);

CREATE TABLE IF NOT EXISTS fee_invoice (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    school_id BIGINT NOT NULL,
    academic_year_id BIGINT,
    due_date DATE NOT NULL,
    total_amount NUMERIC(19, 2) NOT NULL,
    paid_amount NUMERIC(19, 2),
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fee_invoice_student FOREIGN KEY (student_id) REFERENCES student (id),
    CONSTRAINT fk_fee_invoice_school FOREIGN KEY (school_id) REFERENCES school (id)
);

CREATE INDEX IF NOT EXISTS idx_fee_invoice_student_id ON fee_invoice (student_id);
CREATE INDEX IF NOT EXISTS idx_fee_invoice_school_id ON fee_invoice (school_id);

CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    school_id BIGINT NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    payment_mode VARCHAR(255) NOT NULL,
    transaction_id VARCHAR(255),
    status VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES fee_invoice (id),
    CONSTRAINT fk_payment_school FOREIGN KEY (school_id) REFERENCES school (id)
);

CREATE INDEX IF NOT EXISTS idx_payment_invoice_id ON payment (invoice_id);
CREATE INDEX IF NOT EXISTS idx_payment_school_id ON payment (school_id);

CREATE TABLE IF NOT EXISTS student_attendance (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    school_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES student (id),
    CONSTRAINT fk_attendance_school FOREIGN KEY (school_id) REFERENCES school (id)
);

CREATE INDEX IF NOT EXISTS idx_attendance_student_id ON student_attendance (student_id);
CREATE INDEX IF NOT EXISTS idx_attendance_school_id ON student_attendance (school_id);
CREATE INDEX IF NOT EXISTS idx_attendance_date ON student_attendance (attendance_date);
