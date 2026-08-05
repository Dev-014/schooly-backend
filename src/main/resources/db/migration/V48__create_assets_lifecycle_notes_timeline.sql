CREATE TABLE employee_assets (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    asset_name VARCHAR(100) NOT NULL,
    asset_type VARCHAR(50) NOT NULL,
    serial_number VARCHAR(100),
    assigned_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    return_date TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ASSIGNED',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES super_admin_employees(id) ON DELETE CASCADE
);

CREATE TABLE employee_lifecycle (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES super_admin_employees(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES super_admin_employees(id) ON DELETE SET NULL
);

CREATE TABLE employee_notes (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    note_content TEXT NOT NULL,
    author_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES super_admin_employees(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES super_admin_employees(id) ON DELETE SET NULL
);

CREATE TABLE employee_timeline (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES super_admin_employees(id) ON DELETE CASCADE
);
