-- Preserve photo captions and hero destination links across mobile/web edits.
-- gallery_images remains available to older clients as a list of URLs.
ALTER TABLE cms_galleries ADD COLUMN IF NOT EXISTS photo_metadata TEXT;
ALTER TABLE cms_banners ADD COLUMN IF NOT EXISTS target_link TEXT;
