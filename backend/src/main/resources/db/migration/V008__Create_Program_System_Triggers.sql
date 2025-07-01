-- =============================================================================
-- V008__Create_Program_System_Triggers.sql
-- Creates triggers for program system tables
-- Uses the update_updated_at_column() function created in V002
-- =============================================================================

-- Triggers for Program System tables
CREATE TRIGGER update_workout_programs_updated_at
    BEFORE UPDATE ON workout_programs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_program_plans_updated_at
    BEFORE UPDATE ON program_plans
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_scheduled_workouts_updated_at
    BEFORE UPDATE ON scheduled_workouts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();