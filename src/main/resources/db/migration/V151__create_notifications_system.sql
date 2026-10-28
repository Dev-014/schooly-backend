-- ============================================================================
-- V151__create_notifications_system.sql
-- Multi-Role Notification System: Schema, Indexes, Permissions, & Seed Data
-- ============================================================================

-- 1. Create notifications table if not already present
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    school_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    target_role VARCHAR(50) NOT NULL, -- 'ADMIN', 'TEACHER', 'PARENT', 'STAFF', 'STUDENT'
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(100) NOT NULL, -- e.g. 'ATTENDANCE_ABSENT', 'HOMEWORK_ASSIGNED', 'FEE_DUE_REMINDER'
    category VARCHAR(50) NOT NULL DEFAULT 'GENERAL', -- 'ACADEMIC', 'ATTENDANCE', 'FINANCE', 'HR', 'FRONTOFFICE', 'LIBRARY', 'COMMUNICATION', 'SYSTEM'
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL', -- 'LOW', 'NORMAL', 'HIGH', 'URGENT'
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    actor_id BIGINT, -- User who triggered or sent the notification
    actor_role VARCHAR(50), -- Role of trigger user: 'ADMIN', 'TEACHER', 'STAFF', 'PARENT', 'SYSTEM'
    actor_name VARCHAR(255), -- Cached display name of sender
    action_url VARCHAR(500), -- Deep link/path for navigation
    action_type VARCHAR(100) DEFAULT 'NAVIGATE', -- 'NAVIGATE', 'VIEW_RECEIPT', 'EXTERNAL'
    entity_type VARCHAR(100), -- Associated entity name: 'HOMEWORK', 'FEE_RECORD', 'LEAVE_REQUEST', etc.
    entity_id BIGINT, -- Associated entity identifier
    metadata JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Add foreign keys safely if not present
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_notification_school') THEN
        ALTER TABLE notifications ADD CONSTRAINT fk_notification_school FOREIGN KEY (school_id) REFERENCES schools (id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_notification_user') THEN
        ALTER TABLE notifications ADD CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_notification_actor') THEN
        ALTER TABLE notifications ADD CONSTRAINT fk_notification_actor FOREIGN KEY (actor_id) REFERENCES users (id) ON DELETE SET NULL;
    END IF;
END $$;

-- 3. Create performance indexes
CREATE INDEX IF NOT EXISTS idx_notifications_school_user_read ON notifications (school_id, user_id, is_read, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_school_role ON notifications (school_id, target_role, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_type ON notifications (school_id, notification_type);
CREATE INDEX IF NOT EXISTS idx_notifications_category ON notifications (school_id, category);
CREATE INDEX IF NOT EXISTS idx_notifications_actor ON notifications (school_id, actor_id);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications (school_id, created_at DESC);

-- 4. Register granular permissions
INSERT INTO permission_definitions (id, permission_key, module_key, resource_key, action_key, name, description, supported_scope_types, is_sensitive, is_system_permission)
VALUES 
    ('perm_notification_view', 'notification.view', 'COMMUNICATION', 'NOTIFICATION', 'VIEW', 'View Notifications', 'Allows viewing personal and role-based notifications', '["school", "user", "role"]'::jsonb, false, true),
    ('perm_notification_manage', 'notification.manage', 'COMMUNICATION', 'NOTIFICATION', 'MANAGE', 'Manage Notifications', 'Allows marking notifications read/unread and dismissing alerts', '["school", "user"]'::jsonb, false, true),
    ('perm_notification_create', 'notification.create', 'COMMUNICATION', 'NOTIFICATION', 'CREATE', 'Dispatch Notifications', 'Allows school admins and staff to send broadcast or direct notifications', '["school"]'::jsonb, false, true)
ON CONFLICT (id) DO NOTHING;

-- 5. Map permissions to roles
INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT r.id, pd.id, 'school', '{}'::jsonb
FROM roles r
CROSS JOIN permission_definitions pd
WHERE pd.permission_key IN ('notification.view', 'notification.manage')
  AND (r.archetype IN ('SUPER_ADMIN', 'SCHOOL_ADMIN', 'TEACHER', 'PARENT', 'STUDENT', 'STAFF')
       OR r.id IN ('role_super_admin_global', 'role_school_admin_global', 'role_teacher_global', 'role_parent_global', 'role_student_global', 'role_staff_global'))
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id, scope_type, scope_config)
SELECT r.id, pd.id, 'school', '{}'::jsonb
FROM roles r
CROSS JOIN permission_definitions pd
WHERE pd.permission_key = 'notification.create'
  AND (r.archetype IN ('SUPER_ADMIN', 'SCHOOL_ADMIN')
       OR r.id IN ('role_super_admin_global', 'role_school_admin_global'))
ON CONFLICT DO NOTHING;

-- 6. Seed initial notifications for existing users in School 1
-- Admin (User 2: John Admin)
INSERT INTO notifications (school_id, user_id, target_role, title, message, notification_type, category, priority, is_read, actor_id, actor_role, actor_name, action_url, entity_type, entity_id, metadata, created_at)
SELECT 1, 2, 'ADMIN', 'Staff Leave Request: Sarah Teacher', 'Sarah Teacher applied for 2 days Casual Leave starting next Monday.', 'LEAVE_REQUESTED', 'HR', 'HIGH', FALSE, 3, 'TEACHER', 'Sarah Teacher', '/hr/leaves', 'LEAVE_REQUEST', 101, '{"leaveType": "CASUAL", "days": 2}'::jsonb, NOW() - INTERVAL '25 minutes'
WHERE EXISTS (SELECT 1 FROM users WHERE id = 2) AND EXISTS (SELECT 1 FROM schools WHERE id = 1);

INSERT INTO notifications (school_id, user_id, target_role, title, message, notification_type, category, priority, is_read, actor_id, actor_role, actor_name, action_url, entity_type, entity_id, metadata, created_at)
SELECT 1, 2, 'ADMIN', 'New Complaint Filed: School Transport', 'Parent filed complaint regarding Bus Route 4 morning pickup delay.', 'COMPLAINT_FILED', 'FRONTOFFICE', 'NORMAL', FALSE, 7, 'PARENT', 'Rajesh Malhotra', '/frontoffice/complaints', 'COMPLAINT', 201, '{"priority": "MEDIUM", "category": "TRANSPORT"}'::jsonb, NOW() - INTERVAL '2 hours'
WHERE EXISTS (SELECT 1 FROM users WHERE id = 2) AND EXISTS (SELECT 1 FROM schools WHERE id = 1);

INSERT INTO notifications (school_id, user_id, target_role, title, message, notification_type, category, priority, is_read, actor_id, actor_role, actor_name, action_url, entity_type, entity_id, metadata, created_at)
SELECT 1, 2, 'ADMIN', 'Visitor Checked In: Mr. Anil Sharma', 'Visitor checked in to meet Principal regarding new admission inquiry.', 'VISITOR_CHECKED_IN', 'FRONTOFFICE', 'NORMAL', TRUE, NULL, 'STAFF', 'Front Desk Reception', '/frontoffice/visitors', 'VISITOR', 301, '{"badgeNo": "V-104", "purpose": "ADMISSION"}'::jsonb, NOW() - INTERVAL '1 day'
WHERE EXISTS (SELECT 1 FROM users WHERE id = 2) AND EXISTS (SELECT 1 FROM schools WHERE id = 1);

-- Teacher (User 3: Sarah Teacher)
INSERT INTO notifications (school_id, user_id, target_role, title, message, notification_type, category, priority, is_read, actor_id, actor_role, actor_name, action_url, entity_type, entity_id, metadata, created_at)
SELECT 1, 3, 'TEACHER', 'Homework Submitted: Linear Equations', 'Michael submitted assignment solution with attachment for Mathematics.', 'HOMEWORK_SUBMITTED', 'ACADEMIC', 'NORMAL', FALSE, 4, 'STUDENT', 'Michael Student', '/admin/homework/submissions', 'HOMEWORK_SUBMISSION', 401, '{"subject": "Mathematics", "class": "10-A"}'::jsonb, NOW() - INTERVAL '40 minutes'
WHERE EXISTS (SELECT 1 FROM users WHERE id = 3) AND EXISTS (SELECT 1 FROM schools WHERE id = 1);

INSERT INTO notifications (school_id, user_id, target_role, title, message, notification_type, category, priority, is_read, actor_id, actor_role, actor_name, action_url, entity_type, entity_id, metadata, created_at)
SELECT 1, 3, 'TEACHER', 'Monthly Salary Slip Available', 'Your salary slip for the month has been processed and is ready for download.', 'PAYROLL_GENERATED', 'HR', 'NORMAL', FALSE, 2, 'ADMIN', 'Accounts Department', '/hr/payroll', 'PAYROLL', 501, '{"netSalary": 48500}'::jsonb, NOW() - INTERVAL '1 day'
WHERE EXISTS (SELECT 1 FROM users WHERE id = 3) AND EXISTS (SELECT 1 FROM schools WHERE id = 1);

INSERT INTO notifications (school_id, user_id, target_role, title, message, notification_type, category, priority, is_read, actor_id, actor_role, actor_name, action_url, entity_type, entity_id, metadata, created_at)
SELECT 1, 3, 'TEACHER', 'Library Book Due in 2 Days', 'Book "Modern Teaching Methods" is due for return by Friday.', 'BOOK_DUE_REMINDER', 'LIBRARY', 'LOW', TRUE, NULL, 'SYSTEM', 'Central Library', '/library/my-books', 'BOOK_ISSUE', 601, '{"bookTitle": "Modern Teaching Methods"}'::jsonb, NOW() - INTERVAL '3 days'
WHERE EXISTS (SELECT 1 FROM users WHERE id = 3) AND EXISTS (SELECT 1 FROM schools WHERE id = 1);

-- Parent (User 7: Rajesh Malhotra)
INSERT INTO notifications (school_id, user_id, target_role, title, message, notification_type, category, priority, is_read, actor_id, actor_role, actor_name, action_url, entity_type, entity_id, metadata, created_at)
SELECT 1, 7, 'PARENT', 'New Homework Assigned: Mathematics', 'Sarah Teacher posted new homework: Exercise 4.2 due tomorrow by 5:00 PM.', 'HOMEWORK_ASSIGNED', 'ACADEMIC', 'NORMAL', FALSE, 3, 'TEACHER', 'Sarah Teacher', '/homework', 'HOMEWORK', 701, '{"subject": "Mathematics", "dueDate": "Tomorrow"}'::jsonb, NOW() - INTERVAL '15 minutes'
WHERE EXISTS (SELECT 1 FROM users WHERE id = 7) AND EXISTS (SELECT 1 FROM schools WHERE id = 1);

INSERT INTO notifications (school_id, user_id, target_role, title, message, notification_type, category, priority, is_read, actor_id, actor_role, actor_name, action_url, entity_type, entity_id, metadata, created_at)
SELECT 1, 7, 'PARENT', 'Upcoming Term Fee Due Reminder', 'Term 2 tuition fee of Rs. 14,500 is due on the 10th. Pay online to avoid late fee.', 'FEE_DUE_REMINDER', 'FINANCE', 'HIGH', FALSE, 2, 'ADMIN', 'School Finance Office', '/finance/fees', 'FEE_INVOICE', 801, '{"amount": 14500, "dueDate": "10th Nov"}'::jsonb, NOW() - INTERVAL '4 hours'
WHERE EXISTS (SELECT 1 FROM users WHERE id = 7) AND EXISTS (SELECT 1 FROM schools WHERE id = 1);

INSERT INTO notifications (school_id, user_id, target_role, title, message, notification_type, category, priority, is_read, actor_id, actor_role, actor_name, action_url, entity_type, entity_id, metadata, created_at)
SELECT 1, 7, 'PARENT', 'Mid-Term Exam Dates Published', 'Mid-Term examination timetable is released. Check schedules and prepare well.', 'EXAM_SCHEDULE_PUBLISHED', 'ACADEMIC', 'NORMAL', FALSE, 2, 'ADMIN', 'Exam Cell', '/exams/schedule', 'EXAM_TERM', 901, '{"examTerm": "Mid-Term 2026"}'::jsonb, NOW() - INTERVAL '1 day'
WHERE EXISTS (SELECT 1 FROM users WHERE id = 7) AND EXISTS (SELECT 1 FROM schools WHERE id = 1);

INSERT INTO notifications (school_id, user_id, target_role, title, message, notification_type, category, priority, is_read, actor_id, actor_role, actor_name, action_url, entity_type, entity_id, metadata, created_at)
SELECT 1, 7, 'PARENT', 'Attendance Alert: Absent Today', 'Your child was marked absent today. Please inform the class teacher if planned.', 'ATTENDANCE_ABSENT', 'ATTENDANCE', 'HIGH', TRUE, 3, 'TEACHER', 'Sarah Teacher', '/attendance', 'ATTENDANCE', 1001, '{"status": "ABSENT"}'::jsonb, NOW() - INTERVAL '2 days'
WHERE EXISTS (SELECT 1 FROM users WHERE id = 7) AND EXISTS (SELECT 1 FROM schools WHERE id = 1);

-- Student (User 4: Michael)
INSERT INTO notifications (school_id, user_id, target_role, title, message, notification_type, category, priority, is_read, actor_id, actor_role, actor_name, action_url, entity_type, entity_id, metadata, created_at)
SELECT 1, 4, 'STUDENT', 'Homework Evaluated: Mathematics', 'Your assignment was reviewed! Marks: 92/100. Remark: "Excellent work!"', 'HOMEWORK_GRADED', 'ACADEMIC', 'NORMAL', FALSE, 3, 'TEACHER', 'Sarah Teacher', '/homework', 'HOMEWORK_SUBMISSION', 401, '{"marks": 92, "maxMarks": 100}'::jsonb, NOW() - INTERVAL '1 hour'
WHERE EXISTS (SELECT 1 FROM users WHERE id = 4) AND EXISTS (SELECT 1 FROM schools WHERE id = 1);

INSERT INTO notifications (school_id, user_id, target_role, title, message, notification_type, category, priority, is_read, actor_id, actor_role, actor_name, action_url, entity_type, entity_id, metadata, created_at)
SELECT 1, 4, 'STUDENT', 'Annual Sports Day Circular', 'Registrations are open for track events and inter-house football tournament.', 'CIRCULAR_ANNOUNCEMENT', 'COMMUNICATION', 'NORMAL', FALSE, 2, 'ADMIN', 'Sports Department', '/communication/circulars', 'CIRCULAR', 1101, '{"event": "Sports Day"}'::jsonb, NOW() - INTERVAL '5 hours'
WHERE EXISTS (SELECT 1 FROM users WHERE id = 4) AND EXISTS (SELECT 1 FROM schools WHERE id = 1);
