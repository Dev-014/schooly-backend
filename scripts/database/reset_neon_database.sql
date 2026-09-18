-- ============================================================================
-- scripts/database/reset_neon_database.sql
-- Drops and recreates the public schema in PostgreSQL / Neon
-- WARNING: This will drop all tables and data.
-- After running this, Spring Boot + Flyway will execute V1__initial_schema.sql
-- and V2__seed_system_data.sql cleanly on startup.
-- ============================================================================

DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO neondb_owner;
GRANT ALL ON SCHEMA public TO public;

-- Optional: Confirm clean state
SELECT count(*) AS remaining_tables FROM information_schema.tables WHERE table_schema = 'public';
