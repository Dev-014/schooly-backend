CREATE TABLE super_admin_employees (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    department VARCHAR(255),
    designation VARCHAR(255),
    employee_code VARCHAR(100) UNIQUE,
    joined_at DATE,
    salary_band VARCHAR(100),
    leave_balance INT DEFAULT 0,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Backfill existing super admins
INSERT INTO super_admin_employees (user_id, joined_at)
SELECT DISTINCT u.id, CURRENT_DATE
FROM users u
JOIN user_school_roles usr ON usr.user_id = u.id
WHERE usr.role = 'SUPER_ADMIN'
AND NOT EXISTS (SELECT 1 FROM super_admin_employees sae WHERE sae.user_id = u.id);
