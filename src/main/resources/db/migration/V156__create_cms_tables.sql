-- V149: Create Front CMS Tables

CREATE TABLE IF NOT EXISTS cms_events (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT,
    title VARCHAR(255) NOT NULL,
    venue VARCHAR(255),
    start_date VARCHAR(100),
    end_date VARCHAR(100),
    publish_date VARCHAR(100),
    featured_image TEXT,
    description TEXT,
    seo_title VARCHAR(255),
    seo_keyword VARCHAR(255),
    seo_description TEXT,
    sidebar BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cms_galleries (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    featured_image TEXT,
    gallery_images TEXT,
    url VARCHAR(500),
    is_internal_only BOOLEAN DEFAULT FALSE,
    seo_title VARCHAR(255),
    seo_keyword VARCHAR(255),
    seo_description TEXT,
    sidebar BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cms_notices (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT,
    title VARCHAR(255) NOT NULL,
    notice_date VARCHAR(100),
    featured_image TEXT,
    description TEXT,
    url VARCHAR(500),
    seo_title VARCHAR(255),
    seo_keyword VARCHAR(255),
    seo_description TEXT,
    sidebar BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cms_pages (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT,
    title VARCHAR(255) NOT NULL,
    page_type VARCHAR(100) DEFAULT 'Standard',
    url VARCHAR(500),
    featured_image TEXT,
    description TEXT,
    seo_title VARCHAR(255),
    seo_keyword VARCHAR(255),
    seo_description TEXT,
    sidebar BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cms_menu_groups (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    open_in_new_tab BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cms_menu_items (
    id BIGSERIAL PRIMARY KEY,
    menu_group_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    menu_type VARCHAR(50) DEFAULT 'external',
    url VARCHAR(500),
    page_id BIGINT,
    open_in_new_tab BOOLEAN DEFAULT FALSE,
    sort_order INT DEFAULT 0,
    parent_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cms_banners (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT,
    file_name VARCHAR(255) NOT NULL,
    url TEXT NOT NULL,
    file_type VARCHAR(50),
    dimensions VARCHAR(100),
    size VARCHAR(100),
    upload_date VARCHAR(100),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cms_media (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50),
    url TEXT NOT NULL,
    size VARCHAR(100),
    dimensions VARCHAR(100),
    upload_date VARCHAR(100),
    youtube_url TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cms_testimonials (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT,
    name VARCHAR(255) NOT NULL,
    designation VARCHAR(255),
    testimonial TEXT,
    full_testimonial TEXT,
    image TEXT,
    rating INT DEFAULT 5,
    testimonial_date VARCHAR(100),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cms_events_school ON cms_events(school_id);
CREATE INDEX IF NOT EXISTS idx_cms_galleries_school ON cms_galleries(school_id);
CREATE INDEX IF NOT EXISTS idx_cms_notices_school ON cms_notices(school_id);
CREATE INDEX IF NOT EXISTS idx_cms_pages_school ON cms_pages(school_id);
CREATE INDEX IF NOT EXISTS idx_cms_menu_items_group ON cms_menu_items(menu_group_id);
CREATE INDEX IF NOT EXISTS idx_cms_banners_school ON cms_banners(school_id);
CREATE INDEX IF NOT EXISTS idx_cms_media_school ON cms_media(school_id);
CREATE INDEX IF NOT EXISTS idx_cms_testimonials_school ON cms_testimonials(school_id);

