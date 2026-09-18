-- Ticket Categories
CREATE TABLE ticket_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    department VARCHAR(100),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert Default Categories
INSERT INTO ticket_categories (name, department) VALUES 
('School Setup', 'Onboarding'),
('Student', 'Academic'),
('Teacher', 'Academic'),
('Parent', 'Support'),
('Academic', 'Academic'),
('Finance', 'Finance'),
('Scholarship', 'Finance'),
('Administration', 'Operations'),
('Communication', 'Support'),
('Transport', 'Operations'),
('Library', 'Academic'),
('Technical', 'Tech Support'),
('Other', 'Support');

-- Support Tickets
CREATE TABLE support_tickets (
    id BIGSERIAL PRIMARY KEY,
    ticket_code VARCHAR(50) NOT NULL UNIQUE,
    school_id BIGINT NOT NULL REFERENCES schools(id),
    creator_user_id BIGINT NOT NULL REFERENCES users(id),
    portal_source VARCHAR(50) NOT NULL,
    category_id BIGINT REFERENCES ticket_categories(id),
    priority VARCHAR(50) DEFAULT 'LOW',
    subject VARCHAR(255) NOT NULL,
    description TEXT,
    attachment_url VARCHAR(500),
    status VARCHAR(50) DEFAULT 'NEW',
    assigned_employee_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP
);

-- Indexes for support tickets
CREATE INDEX idx_tickets_school ON support_tickets(school_id);
CREATE INDEX idx_tickets_status ON support_tickets(status);

-- Ticket History
CREATE TABLE ticket_histories (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES support_tickets(id),
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    employee_id BIGINT REFERENCES users(id),
    remark TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_history_ticket ON ticket_histories(ticket_id);
