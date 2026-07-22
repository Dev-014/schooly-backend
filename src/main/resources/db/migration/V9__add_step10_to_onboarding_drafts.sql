-- V9__add_step10_to_onboarding_drafts.sql
-- Adds step10_data column to onboarding_drafts table to support the full 10-step school onboarding sequence including Agreement & Documents step.

ALTER TABLE onboarding_drafts ADD COLUMN IF NOT EXISTS step10_data TEXT;
