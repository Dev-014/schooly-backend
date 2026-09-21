CREATE TABLE online_admissions (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL REFERENCES schools(id),
    application_id VARCHAR(50) NOT NULL UNIQUE,
    student_name VARCHAR(255) NOT NULL,
    class_id BIGINT REFERENCES class(id),
    father_name VARCHAR(255),
    date_of_birth DATE,
    gender VARCHAR(20),
    category_id BIGINT REFERENCES student_categories(id),
    mobile_number VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    previous_school VARCHAR(255),
    transaction_status VARCHAR(50) DEFAULT 'UNPAID',
    status VARCHAR(50) DEFAULT 'PENDING',
    applied_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
