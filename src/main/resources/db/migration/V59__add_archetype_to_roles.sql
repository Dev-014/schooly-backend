-- Add archetype column to roles table
ALTER TABLE roles ADD COLUMN archetype VARCHAR(50);

-- Set defaults for system roles
UPDATE roles SET archetype = 'SUPER_ADMIN' WHERE name = 'SUPER_ADMIN';
UPDATE roles SET archetype = 'STAFF' WHERE name = 'SCHOOL_ADMIN' OR name = 'TEACHER' OR name = 'ACADEMIC_COORDINATOR' OR name = 'ACCOUNTANT' OR name = 'TRANSPORT_MANAGER' OR name = 'LIBRARIAN' OR name = 'FRONT_DESK';

-- Set default for any other existing custom role to STAFF
UPDATE roles SET archetype = 'STAFF' WHERE archetype IS NULL;

-- Make archetype NOT NULL if needed (optional depending on future requirements)
ALTER TABLE roles ALTER COLUMN archetype SET NOT NULL;
