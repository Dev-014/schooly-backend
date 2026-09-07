-- Update existing student users with the default password 'schooly123'
UPDATE users
SET password_hash = '$2a$10$gyH2/euTFFXJ78OSpiR7wODF1guGsEiVfSg6mwet07Jp7ybQVbnpq'
WHERE id IN (
    SELECT DISTINCT u.id 
    FROM users u
    JOIN user_school_roles usr ON u.id = usr.user_id
    WHERE usr.role = 'STUDENT'
      AND (u.password_hash IS NULL OR u.password_hash = '')
);

-- Ensure admission numbers are unique globally
-- Note: In some systems admission_no might only be unique per school, but per the requirements we will treat it as globally unique for the login flow.
-- We can add a unique index if it doesn't exist, but first we should clean up any duplicates or just rely on application logic to enforce it moving forward.
-- Since it's an existing table, we'll let application logic handle collisions (e.g., throwing an error if multiple students are found).
