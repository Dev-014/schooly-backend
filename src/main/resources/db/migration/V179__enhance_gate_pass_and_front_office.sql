-- Migration: Add expected return date, actual return tracking, and authorization override to gate_passes
ALTER TABLE gate_passes
    ADD COLUMN IF NOT EXISTS expected_return_date DATE,
    ADD COLUMN IF NOT EXISTS actual_return_date DATE,
    ADD COLUMN IF NOT EXISTS actual_return_time TIME,
    ADD COLUMN IF NOT EXISTS is_override BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS authorization_override_reason TEXT,
    ADD COLUMN IF NOT EXISTS authorized_by_role VARCHAR(50);

CREATE INDEX IF NOT EXISTS idx_gate_passes_expected_return ON gate_passes (expected_return_date, expected_return_time);
