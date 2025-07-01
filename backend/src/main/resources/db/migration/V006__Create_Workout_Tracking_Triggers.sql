-- =============================================================================
-- V006__Create_Workout_Tracking_Triggers.sql
-- Creates triggers for workout tracking system tables
-- Uses the update_updated_at_column() function created in V002
-- =============================================================================

-- Triggers for Workout Tracking System tables
-- UPDATED: Changed from 'workout_logs' to 'workout_sessions'
CREATE TRIGGER update_workout_sessions_updated_at
    BEFORE UPDATE ON workout_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_performance_records_updated_at
    BEFORE UPDATE ON performance_records
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();