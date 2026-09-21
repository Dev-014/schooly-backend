CREATE TABLE employee_attendance (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES super_admin_employees (id) ON DELETE CASCADE
);

CREATE INDEX idx_employee_attendance_date ON employee_attendance(date);
CREATE INDEX idx_employee_attendance_emp_id ON employee_attendance(employee_id);

CREATE TABLE employee_leaves (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    approved_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_leaves_employee FOREIGN KEY (employee_id) REFERENCES super_admin_employees (id) ON DELETE CASCADE,
    CONSTRAINT fk_leaves_approver FOREIGN KEY (approved_by) REFERENCES super_admin_employees (id) ON DELETE SET NULL
);

CREATE INDEX idx_employee_leaves_emp_id ON employee_leaves(employee_id);
CREATE INDEX idx_employee_leaves_status ON employee_leaves(status);
