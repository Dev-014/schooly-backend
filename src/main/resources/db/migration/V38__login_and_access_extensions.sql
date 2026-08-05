CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    request_ip VARCHAR(255),
    device_info VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP,
    CONSTRAINT fk_prt_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_prt_token ON password_reset_tokens (token);

-- Update account_requests for users without an account yet
ALTER TABLE account_requests 
ADD COLUMN IF NOT EXISTS requester_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS requester_email VARCHAR(255),
ADD COLUMN IF NOT EXISTS requester_phone VARCHAR(50),
ADD COLUMN IF NOT EXISTS requested_role VARCHAR(50),
ADD COLUMN IF NOT EXISTS reject_reason TEXT;

-- Update user_activity_logs with new auditing fields
ALTER TABLE user_activity_logs
ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'SUCCESS',
ADD COLUMN IF NOT EXISTS browser VARCHAR(255),
ADD COLUMN IF NOT EXISTS device VARCHAR(255);
