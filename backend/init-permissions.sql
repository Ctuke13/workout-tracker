-- Grant all privileges on the public schema to workout_user
GRANT ALL ON SCHEMA public TO workout_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO workout_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO workout_user;

-- Set default privileges for future objects created by workout_user
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO workout_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO workout_user;

-- Ensure workout_user can create objects in public schema
GRANT CREATE ON SCHEMA public TO workout_user;

-- Make workout_user a database owner for full control
ALTER DATABASE workout_tracker OWNER TO workout_user;