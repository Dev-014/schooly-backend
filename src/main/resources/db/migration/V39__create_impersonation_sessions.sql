CREATE TABLE impersonation_sessions (
    id BIGSERIAL PRIMARY KEY,
    original_user_id BIGINT NOT NULL REFERENCES users(id),
    impersonated_user_id BIGINT NOT NULL REFERENCES users(id),
    session_id VARCHAR(255) NOT NULL,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    ip_address VARCHAR(255),
    device_info JSONB
);

CREATE INDEX idx_impersonation_original_user ON impersonation_sessions(original_user_id);
CREATE INDEX idx_impersonation_status ON impersonation_sessions(status);
