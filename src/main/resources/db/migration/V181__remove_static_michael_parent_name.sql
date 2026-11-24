-- Remove hardcoded/static 'Michael Parent' name from seeded parent users
UPDATE users 
SET name = 'Parent' 
WHERE name = 'Michael Parent' OR name ILIKE '%Michael Parent%';
