-- Add direct branding columns (logo_url and banner_url) to schools table
-- so WebApp and Mobile App can display school branding directly.

ALTER TABLE schools ADD COLUMN IF NOT EXISTS logo_url TEXT;
ALTER TABLE schools ADD COLUMN IF NOT EXISTS banner_url TEXT;
