-- V78__create_fee_reminder_tables.sql
CREATE TABLE fee_reminders (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    staff_id BIGINT,
    student_id BIGINT NOT NULL,
    fee_amount NUMERIC(10, 2),
    method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fee_reminders_school FOREIGN KEY (school_id) REFERENCES schools (id),
    CONSTRAINT fk_fee_reminders_staff FOREIGN KEY (staff_id) REFERENCES staff (id),
    CONSTRAINT fk_fee_reminders_student FOREIGN KEY (student_id) REFERENCES student (id)
);
