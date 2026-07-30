-- Add sample support tickets safely using subqueries to avoid hardcoded IDs
INSERT INTO support_tickets (ticket_code, school_id, creator_user_id, portal_source, category_id, priority, subject, description, status)
SELECT 
    'TICK-1001', 
    (SELECT id FROM schools LIMIT 1), 
    (SELECT id FROM users LIMIT 1), 
    'ADMIN', 
    (SELECT id FROM ticket_categories WHERE name = 'School Setup' LIMIT 1), 
    'HIGH', 
    'Missing teacher records after import', 
    'Half the teachers are missing after the CSV upload.', 
    'NEW'
WHERE EXISTS (SELECT 1 FROM schools LIMIT 1) AND EXISTS (SELECT 1 FROM users LIMIT 1);

INSERT INTO support_tickets (ticket_code, school_id, creator_user_id, portal_source, category_id, priority, subject, description, status)
SELECT 
    'TICK-1002', 
    (SELECT id FROM schools LIMIT 1), 
    (SELECT id FROM users LIMIT 1), 
    'TEACHER', 
    (SELECT id FROM ticket_categories WHERE name = 'Student' LIMIT 1), 
    'MEDIUM', 
    'Attendance sync failure', 
    'Biometric sync failed for Class 10. Can you please check the logs?', 
    'WORKING'
WHERE EXISTS (SELECT 1 FROM schools LIMIT 1) AND EXISTS (SELECT 1 FROM users LIMIT 1);

-- Add sample ticket history for TICK-1002
INSERT INTO ticket_histories (ticket_id, old_status, new_status, employee_id, remark)
SELECT 
    (SELECT id FROM support_tickets WHERE ticket_code = 'TICK-1002' LIMIT 1),
    'NEW',
    'WORKING',
    (SELECT user_id FROM user_school_roles WHERE role = 'SUPER_ADMIN' LIMIT 1),
    'Assigned to technical support team for review.'
WHERE EXISTS (SELECT 1 FROM support_tickets WHERE ticket_code = 'TICK-1002') AND EXISTS (SELECT 1 FROM user_school_roles WHERE role = 'SUPER_ADMIN' LIMIT 1);
