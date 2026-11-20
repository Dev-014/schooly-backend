-- ============================================================================
-- V177__add_submodule_config_and_sync_mvp_modules.sql
-- 1. Adds per-school submodule enable/disable persistence on school_module_access
-- 2. Registers FRONT_CMS as a first-class MVP module in platform_modules
-- 3. Ensures unreleased post-MVP modules are marked is_default = FALSE
-- ============================================================================

ALTER TABLE school_module_access
    ADD COLUMN IF NOT EXISTS disabled_sub_modules TEXT;

INSERT INTO platform_modules (
    code, name, description, is_default, status, category, add_on_price, target_roles, sub_modules, created_at, updated_at
)
SELECT
    'FRONT_CMS',
    'Front CMS',
    'Public school website pages, banners, galleries, notices, events, and menus',
    TRUE,
    'ACTIVE',
    'CORE',
    0.00,
    'ADMIN',
    '["Events", "Gallery", "Notice", "Media Manager", "Pages", "Menus", "Banner Image", "Testimonials"]',
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM platform_modules WHERE code = 'FRONT_CMS'
);

UPDATE platform_modules
SET is_default = FALSE
WHERE code IN (
    'INVENTORY',
    'DISCIPLINE',
    'LESSON_MODULE',
    'CERTIFICATES',
    'DOWNLOADS',
    'ONLINE_CLASS',
    'ONLINE_EXAMS',
    'QUESTION_PAPER'
);
