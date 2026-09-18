-- Overwrite any temporary or fake hashes for students with the proper bcrypt hash for 'schooly123'
UPDATE users
SET password_hash = '$2a$10$gyH2/euTFFXJ78OSpiR7wODF1guGsEiVfSg6mwet07Jp7ybQVbnpq'
WHERE id IN (
    SELECT DISTINCT u.id 
    FROM users u
    JOIN user_school_roles usr ON u.id = usr.user_id
    WHERE usr.role = 'STUDENT'
      AND (
          u.password_hash IS NULL 
          OR u.password_hash = '' 
          OR u.password_hash LIKE '$2a$10$temporaryHashFor%'
      )
);
