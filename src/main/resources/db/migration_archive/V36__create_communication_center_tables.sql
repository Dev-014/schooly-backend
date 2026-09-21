-- Communication Center Enums
CREATE TYPE communication_message_type AS ENUM (
    'ANNOUNCEMENT', 'SOFTWARE_UPDATE', 'MAINTENANCE_NOTICE', 'PAYMENT_REMINDER', 
    'SUBSCRIPTION_RENEWAL', 'TRAINING_INVITATION', 'NEW_FEATURE', 'HOLIDAY_NOTICE', 
    'EMERGENCY_ALERT', 'GENERAL_MESSAGE'
);

CREATE TYPE communication_audience_type AS ENUM (
    'ALL_SCHOOLS', 'SELECTED_SCHOOLS', 'SCHOOLS_BY_PLAN', 'SCHOOLS_BY_CITY', 
    'SCHOOLS_BY_BOARD', 'TRIAL_SCHOOLS', 'EXPIRED_SUBSCRIPTION', 'RENEWAL_DUE', 
    'INACTIVE_SCHOOLS'
);

CREATE TYPE communication_importance AS ENUM (
    'INFORMATION', 'ACTION_REQUIRED', 'IMPORTANT', 'CRITICAL'
);

CREATE TYPE communication_status AS ENUM (
    'DRAFT', 'SCHEDULED', 'SENT', 'CANCELLED'
);

CREATE TYPE communication_delivery_status AS ENUM (
    'PENDING', 'DELIVERED', 'READ', 'FAILED'
);

CREATE TYPE communication_channel AS ENUM (
    'PORTAL', 'MOBILE_APP', 'EMAIL', 'SMS', 'WHATSAPP'
);

-- Core Communication Tables

CREATE TABLE communication_announcements (
    id BIGSERIAL PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    message_type communication_message_type NOT NULL,
    importance communication_importance NOT NULL DEFAULT 'INFORMATION',
    status communication_status NOT NULL DEFAULT 'DRAFT',
    audience_type communication_audience_type NOT NULL,
    audience_criteria JSONB,
    scheduled_at TIMESTAMP,
    created_by_user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comm_announcements_status ON communication_announcements(status);
CREATE INDEX idx_comm_announcements_scheduled_at ON communication_announcements(scheduled_at);

CREATE TABLE communication_announcement_schools (
    announcement_id BIGINT NOT NULL REFERENCES communication_announcements(id) ON DELETE CASCADE,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    PRIMARY KEY (announcement_id, school_id)
);

CREATE TABLE communication_deliveries (
    id BIGSERIAL PRIMARY KEY,
    announcement_id BIGINT NOT NULL REFERENCES communication_announcements(id) ON DELETE CASCADE,
    school_id BIGINT NOT NULL REFERENCES schools(id) ON DELETE CASCADE,
    delivery_channel communication_channel NOT NULL DEFAULT 'PORTAL',
    status communication_delivery_status NOT NULL DEFAULT 'PENDING',
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comm_deliveries_announcement_id ON communication_deliveries(announcement_id);
CREATE INDEX idx_comm_deliveries_school_id ON communication_deliveries(school_id);
CREATE INDEX idx_comm_deliveries_status ON communication_deliveries(status);

CREATE TABLE communication_templates (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(255) NOT NULL,
    category communication_message_type NOT NULL,
    message TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
