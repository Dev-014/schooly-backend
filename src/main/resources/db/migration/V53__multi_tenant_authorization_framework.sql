-- Platform Level (Layer A)
CREATE TABLE permission_definitions (
    id VARCHAR(100) PRIMARY KEY,
    permission_key VARCHAR(100) UNIQUE NOT NULL,
    module_key VARCHAR(50) NOT NULL,
    resource_key VARCHAR(50) NOT NULL,
    action_key VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    supported_scope_types JSONB, -- e.g., ["school", "assigned", "class", "linked"]
    requires_assignment BOOLEAN DEFAULT false,
    is_sensitive BOOLEAN DEFAULT false,
    is_system_permission BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- School Level (Layer B)
CREATE TABLE roles (
    id VARCHAR(50) PRIMARY KEY,
    school_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_system_role BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role_permissions (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    role_id VARCHAR(50) NOT NULL REFERENCES roles(id),
    permission_id VARCHAR(100) NOT NULL REFERENCES permission_definitions(id),
    scope_type VARCHAR(50) NOT NULL, -- e.g., "assigned", "school"
    scope_config JSONB
);

CREATE TABLE user_role_mappings (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id VARCHAR(50) NOT NULL REFERENCES roles(id),
    academic_session_id BIGINT,
    is_active BOOLEAN DEFAULT true
);

CREATE TABLE user_assignments (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    academic_session_id BIGINT NOT NULL,
    assignment_type VARCHAR(50) NOT NULL, -- e.g., 'class_teacher', 'subject_teacher', 'parent'
    class_id BIGINT,
    section_id BIGINT,
    subject_id BIGINT,
    student_id BIGINT, -- for parent link
    effective_from TIMESTAMP,
    effective_to TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- Seed System Platform Permissions
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES
('perm_attendance_view', 'attendance.attendance_record.view', 'attendance', 'attendance_record', 'view', 'View Attendance', 'Allows viewing of student attendance records', '["school", "assigned", "class", "section", "linked"]', false, true),
('perm_attendance_edit', 'attendance.attendance_record.edit', 'attendance', 'attendance_record', 'edit', 'Edit Attendance', 'Allows modification of student attendance records', '["school", "assigned", "class", "section"]', true, true),
('perm_student_view', 'student.student.view', 'student', 'student', 'view', 'View Student', 'Allows viewing student profiles', '["school", "assigned", "class", "section", "linked"]', false, true),
('perm_student_edit', 'student.student.edit', 'student', 'student', 'edit', 'Edit Student', 'Allows editing student profiles', '["school", "assigned"]', false, true),
('perm_fee_view', 'fees.fee_collection.view', 'fees', 'fee_collection', 'view', 'View Fee Collections', 'Allows viewing fee transactions', '["school", "linked"]', false, true),
('perm_fee_refund', 'fees.fee_collection.refund', 'fees', 'fee_collection', 'refund', 'Refund Fees', 'Allows processing fee refunds', '["school"]', true, true);
