CREATE TABLE IF NOT EXISTS user_login_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    school_id BIGINT,
    device VARCHAR(255),
    browser VARCHAR(255),
    ip_address VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    login_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP,
    CONSTRAINT fk_ulh_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_ulh_school FOREIGN KEY (school_id) REFERENCES schools (id)
);

CREATE INDEX IF NOT EXISTS idx_ulh_user_id ON user_login_history (user_id);
CREATE INDEX IF NOT EXISTS idx_ulh_school_id ON user_login_history (school_id);

CREATE TABLE IF NOT EXISTS user_activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    school_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    module VARCHAR(100) NOT NULL,
    action VARCHAR(255) NOT NULL,
    ip_address VARCHAR(255),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ual_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_ual_school FOREIGN KEY (school_id) REFERENCES schools (id)
);

CREATE INDEX IF NOT EXISTS idx_ual_user_id ON user_activity_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_ual_school_id ON user_activity_logs (school_id);
CREATE INDEX IF NOT EXISTS idx_ual_module ON user_activity_logs (module);

CREATE TABLE IF NOT EXISTS account_requests (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    user_id BIGINT,
    request_type VARCHAR(100) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    resolution_notes TEXT,
    CONSTRAINT fk_ar_school FOREIGN KEY (school_id) REFERENCES schools (id),
    CONSTRAINT fk_ar_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_ar_school_id ON account_requests (school_id);
CREATE INDEX IF NOT EXISTS idx_ar_status ON account_requests (status);
