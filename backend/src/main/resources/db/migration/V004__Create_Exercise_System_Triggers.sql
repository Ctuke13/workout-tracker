-- =============================================================================
-- V004__Create_Exercise_System_Triggers.sql
-- Creates triggers for exercise system tables (exercises, workout_plans, plan_exercise)
-- Uses the update_updated_at_column() function created in V002
-- =============================================================================

-- Triggers for Exercise System tables that exist by V003
CREATE TRIGGER update_exercises_updated_at
    BEFORE UPDATE ON exercises
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workout_plans_updated_at
    BEFORE UPDATE ON workout_plans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_plan_exercise_updated_at
    BEFORE UPDATE ON plan_exercise
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Trigger for fitness_goals table (created in V003)
CREATE TRIGGER update_fitness_goals_updated_at
    BEFORE UPDATE ON fitness_goals
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- SECTION 5: Future Tables (To Be Created in Later Migrations)
-- =============================================================================

-- NOTE: The following tables will have their triggers created in future migrations:
-- - user_exercise_ratings (will be created in a later migration)
-- - workout_sessions (if planned)
-- - exercise_logs (if planned)
-- - Any other tables that don't exist yet

-- When you create the user_exercise_ratings table in a future migration,
-- add this trigger in that migration file:
--
-- CREATE TRIGGER update_user_exercise_ratings_updated_at
--     BEFORE UPDATE ON user_exercise_ratings
--     FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- SECTION 6: Verification
-- =============================================================================

-- Verify that all expected triggers were created
DO $$
DECLARE
trigger_count INTEGER;
    expected_triggers TEXT[] := ARRAY[
        'update_exercises_updated_at',
        'update_workout_plans_updated_at',
        'update_plan_exercise_updated_at'
    ];
    trigger_name TEXT;
BEGIN
    -- Count actual triggers created
SELECT COUNT(*) INTO trigger_count
FROM pg_trigger t
         JOIN pg_class c ON t.tgrelid = c.oid
         JOIN pg_namespace n ON c.relnamespace = n.oid
WHERE n.nspname = 'public'
  AND t.tgname = ANY(expected_triggers);

-- Log trigger creation results
RAISE NOTICE 'V004 Migration: Created % core exercise system triggers', trigger_count;

    -- List all triggers created in this migration
FOR trigger_name IN
SELECT t.tgname
FROM pg_trigger t
         JOIN pg_class c ON t.tgrelid = c.oid
         JOIN pg_namespace n ON c.relnamespace = n.oid
WHERE n.nspname = 'public'
  AND t.tgname LIKE '%updated_at%'
ORDER BY t.tgname
    LOOP
        RAISE NOTICE 'Created trigger: %', trigger_name;
END LOOP;
END $$;