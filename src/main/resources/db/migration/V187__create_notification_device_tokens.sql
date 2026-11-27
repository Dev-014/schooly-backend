CREATE TABLE IF NOT EXISTS notification_device_tokens (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    target_role VARCHAR(50),
    token VARCHAR(512) NOT NULL,
    platform VARCHAR(30) NOT NULL DEFAULT 'ANDROID',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_notification_device_token UNIQUE (user_id, token)
);

CREATE INDEX IF NOT EXISTS idx_notification_device_tokens_user_school
    ON notification_device_tokens (school_id, user_id, active);
