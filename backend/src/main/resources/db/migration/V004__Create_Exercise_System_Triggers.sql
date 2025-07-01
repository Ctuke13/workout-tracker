-- =============================================================================
-- V004__Create_Exercise_System_Triggers.sql
-- Creates triggers for exercise system tables (exercises, workout_plans, plan_exercise)
-- Uses the update_updated_at_column() function created in V002
-- =============================================================================

-- Triggers for Exercise System tables
CREATE TRIGGER update_exercises_updated_at
    BEFORE UPDATE ON exercises
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- UPDATED: Changed from 'workouts' to 'workout_plans'
CREATE TRIGGER update_workout_plans_updated_at
    BEFORE UPDATE ON workout_plans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_plan_exercise_updated_at
    BEFORE UPDATE ON plan_exercise
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();