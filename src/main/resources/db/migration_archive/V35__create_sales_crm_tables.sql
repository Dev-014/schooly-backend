-- Sales CRM Enums
CREATE TYPE crm_pipeline_stage AS ENUM (
    'NEW', 'CONTACTED', 'QUALIFIED', 'DEMO_SCHEDULED', 'DEMO_COMPLETED', 'QUOTATION_SENT', 'NEGOTIATION', 'WON', 'LOST'
);

CREATE TYPE crm_lead_source AS ENUM (
    'WEBSITE', 'FACEBOOK', 'GOOGLE', 'REFERENCE', 'WALKIN', 'COLD_CALL', 'CAMPAIGN', 'OTHER'
);

CREATE TYPE crm_follow_up_action AS ENUM (
    'CALL', 'WHATSAPP', 'EMAIL', 'MEETING', 'VISIT'
);

CREATE TYPE crm_follow_up_status AS ENUM (
    'PENDING', 'COMPLETED', 'MISSED'
);

CREATE TYPE crm_demo_mode AS ENUM (
    'ONLINE', 'OFFLINE'
);

CREATE TYPE crm_demo_status AS ENUM (
    'SCHEDULED', 'COMPLETED', 'CANCELED', 'NO_SHOW'
);

CREATE TYPE crm_quotation_status AS ENUM (
    'DRAFT', 'SENT', 'ACCEPTED', 'REJECTED', 'EXPIRED'
);

-- Core CRM Tables

CREATE TABLE crm_leads (
    id BIGSERIAL PRIMARY KEY,
    school_name VARCHAR(255) NOT NULL,
    principal_name VARCHAR(255),
    city VARCHAR(100),
    board VARCHAR(50),
    mobile VARCHAR(20) NOT NULL,
    alternative_mobile VARCHAR(20),
    email VARCHAR(255),
    address TEXT,
    state VARCHAR(100),
    pin_code VARCHAR(20),
    approx_student_strength INT,
    teachers INT,
    branches INT,
    current_erp VARCHAR(255),
    website VARCHAR(255),
    existing_problems TEXT,
    lead_source crm_lead_source DEFAULT 'OTHER',
    assigned_employee_id BIGINT REFERENCES users(id),
    priority VARCHAR(50) DEFAULT 'MEDIUM',
    expected_closing_date TIMESTAMP,
    lead_rating INT DEFAULT 1,
    notes TEXT,
    pipeline_stage crm_pipeline_stage DEFAULT 'NEW',
    status VARCHAR(50) DEFAULT 'ACTIVE',
    converted_school_id BIGINT REFERENCES schools(id),
    lost_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crm_leads_assigned_employee_id ON crm_leads(assigned_employee_id);
CREATE INDEX idx_crm_leads_pipeline_stage ON crm_leads(pipeline_stage);

CREATE TABLE crm_follow_ups (
    id BIGSERIAL PRIMARY KEY,
    lead_id BIGINT NOT NULL REFERENCES crm_leads(id) ON DELETE CASCADE,
    action_type crm_follow_up_action NOT NULL,
    scheduled_date TIMESTAMP NOT NULL,
    remarks TEXT,
    executive_id BIGINT REFERENCES users(id),
    status crm_follow_up_status DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crm_follow_ups_lead_id ON crm_follow_ups(lead_id);
CREATE INDEX idx_crm_follow_ups_scheduled_date ON crm_follow_ups(scheduled_date);

CREATE TABLE crm_demos (
    id BIGSERIAL PRIMARY KEY,
    lead_id BIGINT NOT NULL REFERENCES crm_leads(id) ON DELETE CASCADE,
    demo_date TIMESTAMP NOT NULL,
    mode crm_demo_mode NOT NULL,
    demo_by_id BIGINT REFERENCES users(id),
    status crm_demo_status DEFAULT 'SCHEDULED',
    feedback TEXT,
    recording_url VARCHAR(500),
    meeting_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crm_demos_lead_id ON crm_demos(lead_id);
CREATE INDEX idx_crm_demos_demo_date ON crm_demos(demo_date);

CREATE TABLE crm_quotations (
    id BIGSERIAL PRIMARY KEY,
    quotation_number VARCHAR(100) UNIQUE NOT NULL,
    lead_id BIGINT NOT NULL REFERENCES crm_leads(id) ON DELETE CASCADE,
    plan_name VARCHAR(255) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    discount DECIMAL(12,2) DEFAULT 0.00,
    gst DECIMAL(12,2) DEFAULT 0.00,
    total DECIMAL(12,2) NOT NULL,
    expiry_date TIMESTAMP,
    status crm_quotation_status DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crm_quotations_lead_id ON crm_quotations(lead_id);

CREATE TABLE crm_activity_logs (
    id BIGSERIAL PRIMARY KEY,
    lead_id BIGINT NOT NULL REFERENCES crm_leads(id) ON DELETE CASCADE,
    actor_id BIGINT REFERENCES users(id),
    activity_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_crm_activity_logs_lead_id ON crm_activity_logs(lead_id);
