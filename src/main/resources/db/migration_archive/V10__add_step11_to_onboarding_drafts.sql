-- V10__add_step11_to_onboarding_drafts.sql
-- Adds step11_data column to onboarding_drafts table to support the 11-step school onboarding sequence including Plan & Payment (Payment & Subscription) step.

ALTER TABLE onboarding_drafts ADD COLUMN IF NOT EXISTS step11_data TEXT;
