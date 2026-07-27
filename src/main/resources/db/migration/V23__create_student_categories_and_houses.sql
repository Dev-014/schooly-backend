-- V23__create_student_categories_and_houses.sql

CREATE TABLE IF NOT EXISTS student_categories (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    CONSTRAINT fk_category_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_cat_school_id ON student_categories(school_id);

CREATE TABLE IF NOT EXISTS student_houses (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    color_code VARCHAR(20),
    description VARCHAR(255),
    CONSTRAINT fk_house_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_house_school_id ON student_houses(school_id);

ALTER TABLE student ADD COLUMN IF NOT EXISTS category_id BIGINT;
ALTER TABLE student ADD CONSTRAINT fk_student_category FOREIGN KEY (category_id) REFERENCES student_categories(id) ON DELETE SET NULL;

ALTER TABLE student ADD COLUMN IF NOT EXISTS house_id BIGINT;
ALTER TABLE student ADD CONSTRAINT fk_student_house FOREIGN KEY (house_id) REFERENCES student_houses(id) ON DELETE SET NULL;

-- Seed initial categories for school 1
INSERT INTO student_categories (id, school_id, name) VALUES
(1, 1, 'General'),
(2, 1, 'OBC'),
(3, 1, 'SC/ST')
ON CONFLICT DO NOTHING;

-- Seed initial houses for school 1
INSERT INTO student_houses (id, school_id, name, color_code) VALUES
(1, 1, 'Red House', 'bg-[#E53935]'),
(2, 1, 'Blue House', 'bg-[#1E88E5]'),
(3, 1, 'Green House', 'bg-[#43A047]'),
(4, 1, 'Yellow House', 'bg-[#FFB300]')
ON CONFLICT DO NOTHING;

SELECT setval('student_categories_id_seq', (SELECT MAX(id) FROM student_categories));
SELECT setval('student_houses_id_seq', (SELECT MAX(id) FROM student_houses));
