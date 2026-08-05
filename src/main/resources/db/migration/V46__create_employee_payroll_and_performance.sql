CREATE TABLE employee_payroll (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    month VARCHAR(20) NOT NULL,
    year INT NOT NULL,
    base_salary NUMERIC(10, 2) NOT NULL,
    allowances NUMERIC(10, 2) DEFAULT 0,
    deductions NUMERIC(10, 2) DEFAULT 0,
    net_salary NUMERIC(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payroll_employee FOREIGN KEY (employee_id) REFERENCES super_admin_employees (id) ON DELETE CASCADE
);

CREATE INDEX idx_employee_payroll_emp_id ON employee_payroll(employee_id);
CREATE INDEX idx_employee_payroll_status ON employee_payroll(status);

CREATE TABLE employee_performance (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    review_cycle VARCHAR(100) NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    reviewer_id BIGINT,
    comments TEXT,
    goals TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_performance_employee FOREIGN KEY (employee_id) REFERENCES super_admin_employees (id) ON DELETE CASCADE,
    CONSTRAINT fk_performance_reviewer FOREIGN KEY (reviewer_id) REFERENCES super_admin_employees (id) ON DELETE SET NULL
);

CREATE INDEX idx_employee_performance_emp_id ON employee_performance(employee_id);
