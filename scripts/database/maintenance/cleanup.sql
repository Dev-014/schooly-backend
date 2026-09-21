DELETE FROM account_requests WHERE user_id IN (SELECT id FROM users WHERE name IS NULL OR name = '');
DELETE FROM users WHERE name IS NULL OR name = '';
