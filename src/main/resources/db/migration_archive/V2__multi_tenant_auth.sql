DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'school'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'schools'
    ) THEN
        ALTER TABLE school RENAME TO schools;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_type
        WHERE typname = 'user_role'
    ) THEN
        CREATE TYPE user_role AS ENUM ('SUPER_ADMIN', 'ADMIN', 'TEACHER', 'STUDENT', 'PARENT');
    END IF;
END $$;

ALTER TABLE student
    ADD COLUMN IF NOT EXISTS name VARCHAR(255);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(255),
    email VARCHAR(255),
    password_hash VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_phone ON users (phone);

CREATE TABLE IF NOT EXISTS user_school_roles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    school_id BIGINT NOT NULL,
    role user_role NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    left_at TIMESTAMP,
    CONSTRAINT fk_user_school_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_school_roles_school FOREIGN KEY (school_id) REFERENCES schools (id),
    CONSTRAINT uq_user_school_role UNIQUE (user_id, school_id, role)
);

CREATE INDEX IF NOT EXISTS idx_user_school_roles_user_id ON user_school_roles (user_id);
CREATE INDEX IF NOT EXISTS idx_user_school_roles_school_id ON user_school_roles (school_id);
CREATE INDEX IF NOT EXISTS idx_user_school_roles_school_role ON user_school_roles (school_id, role);

CREATE TABLE IF NOT EXISTS student_parents (
    student_id BIGINT NOT NULL,
    parent_user_id BIGINT NOT NULL,
    relation VARCHAR(100),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (student_id, parent_user_id),
    CONSTRAINT fk_student_parents_student FOREIGN KEY (student_id) REFERENCES student (id),
    CONSTRAINT fk_student_parents_parent FOREIGN KEY (parent_user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_student_parents_parent_user_id ON student_parents (parent_user_id);

CREATE TABLE IF NOT EXISTS auth_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    school_id BIGINT,
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL,
    device_info JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auth_sessions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_auth_sessions_school FOREIGN KEY (school_id) REFERENCES schools (id)
);

CREATE INDEX IF NOT EXISTS idx_student_school_id ON student (school_id);
