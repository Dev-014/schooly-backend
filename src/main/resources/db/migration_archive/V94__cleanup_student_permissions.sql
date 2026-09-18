-- V94__cleanup_student_permissions.sql

-- 1. Move the granular screen permissions from DASHBOARD to STUDENTS module
UPDATE permission_definitions 
SET module_key = 'STUDENTS' 
WHERE permission_key LIKE 'dashboard.student_%' 
   OR permission_key = 'dashboard.online_admission.view'
   OR permission_key = 'dashboard.link_siblings.view';

-- 2. Rename the permission_key from dashboard.* to students.*
UPDATE permission_definitions
SET permission_key = REPLACE(permission_key, 'dashboard.', 'students.')
WHERE module_key = 'STUDENTS' AND permission_key LIKE 'dashboard.%';

-- 3. Delete the legacy generic student CRUD permissions 
-- to prevent duplicate/ambiguous blocks in the UI.
-- Must first remove FK references in role_permissions before deleting from permission_definitions.
DELETE FROM role_permissions
WHERE permission_id IN (
    SELECT id FROM permission_definitions
    WHERE permission_key IN (
        'student.student.view',
        'student.student.edit',
        'student.student.add'
    )
);

DELETE FROM permission_definitions
WHERE permission_key IN (
    'student.student.view',
    'student.student.edit',
    'student.student.add'
);

-- Note: The frontend app.routes.tsx has been updated simultaneously 
-- to use the new granular STUDENTS.STUDENT_DIRECTORY.VIEW instead of student.student.view.
