-- =============================================================================
-- V005__Create_Workout_Tracking_System.sql
-- Creates workout_sessions and performance_records tables
-- EXACTLY MATCHES the WorkoutSession and PerformanceRecord entities
-- =============================================================================

-- =====================================================
-- ADD SOFT DELETE COLUMNS TO SCHEDULED_WORKOUTS TABLE
-- =====================================================

-- Add soft delete columns to existing scheduled_workouts table
-- ALTER TABLE scheduled_workouts
--     ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
-- ADD COLUMN deleted_at TIMESTAMP,
-- ADD COLUMN deleted_by VARCHAR(255);
--
-- -- Add indexes for soft delete queries
-- CREATE INDEX idx_scheduled_workouts_deleted ON scheduled_workouts(deleted);
-- CREATE INDEX idx_scheduled_workouts_user_status_deleted ON scheduled_workouts(user_id, status, deleted);

-- =====================================================
-- WORKOUT_SESSIONS TABLE (matches WorkoutSession.java exactly)
-- =====================================================

CREATE TABLE workout_sessions (
                                  workout_session_id BIGSERIAL PRIMARY KEY,

    -- Core relationships
                                  user_id BIGINT NOT NULL,
                                  workout_plan_id BIGINT NOT NULL,

    -- Session metrics
                                  total_duration_minutes INTEGER,
                                  estimated_calories INTEGER,
                                  difficulty_rating INTEGER,
                                  overall_effort DOUBLE PRECISION,

    -- Session status and completion tracking
                                  session_status VARCHAR(20) DEFAULT 'PLANNED',
                                  total_exercises_planned INTEGER DEFAULT 0,
                                  total_exercises_completed INTEGER DEFAULT 0,
                                  completion_percentage DECIMAL(5,2) DEFAULT 0.0,
                                  workout_feedback TEXT,
                                  performance_summary TEXT,

    -- Session context
                                  mood VARCHAR(20),
                                  location VARCHAR(20),

    -- Program integration (nullable - WorkoutProgram table may not exist yet)
                                  workout_program_id BIGINT,
                                  week_number INTEGER,
                                  scheduled_workout_id BIGINT,

    -- Social features
                                  is_shared BOOLEAN DEFAULT false,

    -- Core data
                                  date DATE NOT NULL,
                                  notes TEXT,

    -- Timestamps
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

-- Foreign key constraints
                                  CONSTRAINT fk_workout_sessions_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                  CONSTRAINT fk_workout_sessions_workout_plan FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(workout_plan_id) ON DELETE CASCADE
-- CONSTRAINT fk_workout_sessions_scheduled_workout FOREIGN KEY (scheduled_workout_id) REFERENCES scheduled_workouts(scheduled_workout_id) ON DELETE CASCADE
);

-- Add constraints for WorkoutSession enum values and validation
ALTER TABLE workout_sessions ADD CONSTRAINT chk_workout_sessions_difficulty_rating
    CHECK (difficulty_rating IS NULL OR (difficulty_rating >= 1 AND difficulty_rating <= 10));

ALTER TABLE workout_sessions ADD CONSTRAINT chk_workout_sessions_overall_effort
    CHECK (overall_effort IS NULL OR (overall_effort >= 1.0 AND overall_effort <= 10.0));

ALTER TABLE workout_sessions ADD CONSTRAINT chk_workout_sessions_mood
    CHECK (mood IS NULL OR mood IN ('ENERGETIC', 'TIRED', 'MOTIVATED', 'FOCUSED', 'STRESSED', 'RELAXED', 'PUMPED', 'SLUGGISH'));

ALTER TABLE workout_sessions ADD CONSTRAINT chk_workout_sessions_location
    CHECK (location IS NULL OR location IN ('HOME', 'GYM', 'PARK', 'OFFICE', 'HOTEL', 'BEACH', 'TRAIL', 'STUDIO', 'OTHER'));

ALTER TABLE workout_sessions ADD CONSTRAINT chk_workout_sessions_week_number
    CHECK (week_number IS NULL OR week_number >= 1);

ALTER TABLE workout_sessions ADD CONSTRAINT chk_workout_sessions_session_status
    CHECK (session_status IN ('PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'PAUSED'));

ALTER TABLE workout_sessions ADD CONSTRAINT chk_workout_sessions_total_exercises_planned
    CHECK (total_exercises_planned >= 0);

ALTER TABLE workout_sessions ADD CONSTRAINT chk_workout_sessions_total_exercises_completed
    CHECK (total_exercises_completed >= 0);

ALTER TABLE workout_sessions ADD CONSTRAINT chk_workout_sessions_completion_percentage
    CHECK (completion_percentage >= 0.0 AND completion_percentage <= 100.0);

-- =====================================================
-- PERFORMANCE_RECORDS TABLE (matches PerformanceRecord.java exactly)
-- =====================================================

CREATE TABLE performance_records (
                                     performance_record_id BIGSERIAL PRIMARY KEY,

    -- Core relationships
                                     exercise_id BIGINT NOT NULL,
                                     workout_session_id BIGINT NOT NULL,

    -- Basic performance metrics
                                     set_number INTEGER NOT NULL DEFAULT 1,
                                     reps INTEGER,
                                     weight DOUBLE PRECISION,

    -- Cardio metrics
                                     duration_minutes INTEGER,
                                     duration_seconds DOUBLE PRECISION,
                                     distance_km DOUBLE PRECISION,
                                     calories_burned INTEGER,

    -- Advanced performance metrics
                                     perceived_exertion INTEGER,
                                     form_rating INTEGER,
                                     rest_seconds INTEGER,
                                     actual_rest_seconds INTEGER,
                                     tempo VARCHAR(20),

    -- Rest time tracking and set timing
                                     rest_time_before_set_seconds INTEGER,
                                     set_start_time TIMESTAMP,
                                     set_end_time TIMESTAMP,
                                     actual_set_duration_seconds INTEGER,

    -- Exercise completion tracking
                                     is_exercise_completed BOOLEAN DEFAULT FALSE,
                                     exercise_completion_notes TEXT,

    -- Target comparison fields
                                     target_reps_planned INTEGER,
                                     target_weight_planned DECIMAL(5,2),
                                     performance_vs_target VARCHAR(20) DEFAULT 'NOT_SET',

    -- Specialized exercise metrics
                                     hold_duration_seconds INTEGER,
                                     balance_score INTEGER,
                                     jump_height_cm DOUBLE PRECISION,
                                     power_output_watts DOUBLE PRECISION,

    -- Professional training metrics
                                     assigned_by_trainer_id BIGINT,
                                     target_reps INTEGER,
                                     target_weight DOUBLE PRECISION,
                                     achievement_status VARCHAR(20) DEFAULT 'NOT_SET',

    -- Notes and metadata
                                     notes VARCHAR(1000),
                                     equipment_used VARCHAR(200),
                                     workout_environment VARCHAR(100),

    -- Audit fields
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
                                     CONSTRAINT fk_performance_records_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(exercise_id) ON DELETE CASCADE,
                                     CONSTRAINT fk_performance_records_workout_session FOREIGN KEY (workout_session_id) REFERENCES workout_sessions(workout_session_id) ON DELETE CASCADE
);

-- Add constraints for PerformanceRecord validation
ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_set_number
    CHECK (set_number > 0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_reps
    CHECK (reps IS NULL OR reps >= 0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_weight
    CHECK (weight IS NULL OR weight >= 0.0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_duration_minutes
    CHECK (duration_minutes IS NULL OR duration_minutes >= 0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_duration_seconds
    CHECK (duration_seconds IS NULL OR duration_seconds >= 0.0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_distance_km
    CHECK (distance_km IS NULL OR distance_km >= 0.0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_calories_burned
    CHECK (calories_burned IS NULL OR calories_burned >= 0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_perceived_exertion
    CHECK (perceived_exertion IS NULL OR (perceived_exertion >= 1 AND perceived_exertion <= 10));

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_form_rating
    CHECK (form_rating IS NULL OR (form_rating >= 1 AND form_rating <= 10));

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_rest_seconds
    CHECK (rest_seconds IS NULL OR rest_seconds >= 0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_actual_rest_seconds
    CHECK (actual_rest_seconds IS NULL OR actual_rest_seconds >= 0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_hold_duration_seconds
    CHECK (hold_duration_seconds IS NULL OR hold_duration_seconds >= 0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_balance_score
    CHECK (balance_score IS NULL OR (balance_score >= 1 AND balance_score <= 10));

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_jump_height_cm
    CHECK (jump_height_cm IS NULL OR jump_height_cm >= 0.0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_power_output_watts
    CHECK (power_output_watts IS NULL OR power_output_watts >= 0.0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_target_reps
    CHECK (target_reps IS NULL OR target_reps >= 0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_target_weight
    CHECK (target_weight IS NULL OR target_weight >= 0.0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_achievement_status
    CHECK (achievement_status IN ('NOT_SET', 'EXCEEDED', 'MET', 'BELOW_TARGET', 'PARTIAL'));

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_tempo
    CHECK (tempo IS NULL OR tempo ~ '^\\d{1,2}-\\d{1,2}-\\d{1,2}-\\d{1,2}$' OR tempo = '');

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_rest_time_before_set
    CHECK (rest_time_before_set_seconds IS NULL OR rest_time_before_set_seconds >= 0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_target_reps_planned
    CHECK (target_reps_planned IS NULL OR target_reps_planned >= 0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_target_weight_planned
    CHECK (target_weight_planned IS NULL OR target_weight_planned >= 0.0);

ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_performance_vs_target
    CHECK (performance_vs_target IN ('NOT_SET', 'EXCEEDED', 'MET', 'BELOW', 'STRUGGLED'));

-- Add constraint for set timing logic
ALTER TABLE performance_records ADD CONSTRAINT chk_performance_records_set_timing
    CHECK (
        (set_start_time IS NULL AND set_end_time IS NULL) OR
        (set_start_time IS NOT NULL AND set_end_time IS NULL) OR
        (set_start_time IS NOT NULL AND set_end_time IS NOT NULL AND set_end_time >= set_start_time)
        );

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Workout sessions table indexes
CREATE INDEX idx_workout_sessions_user_id ON workout_sessions(user_id);
CREATE INDEX idx_workout_sessions_workout_plan_id ON workout_sessions(workout_plan_id);
CREATE INDEX idx_workout_sessions_workout_program_id ON workout_sessions(workout_program_id);
CREATE INDEX idx_workout_sessions_scheduled_workout_id ON workout_sessions(scheduled_workout_id);
CREATE INDEX idx_workout_sessions_date ON workout_sessions(date);
CREATE INDEX idx_workout_sessions_created_at ON workout_sessions(created_at);
CREATE INDEX idx_workout_sessions_user_date ON workout_sessions(user_id, date);
CREATE INDEX idx_workout_sessions_mood ON workout_sessions(mood);
CREATE INDEX idx_workout_sessions_location ON workout_sessions(location);
CREATE INDEX idx_workout_sessions_is_shared ON workout_sessions(is_shared);

-- Indexes for enhanced session tracking
CREATE INDEX idx_workout_sessions_session_status ON workout_sessions(session_status);
CREATE INDEX idx_workout_sessions_completion_percentage ON workout_sessions(completion_percentage);
CREATE INDEX idx_workout_sessions_user_status ON workout_sessions(user_id, session_status);
CREATE INDEX idx_workout_sessions_completion_analysis ON workout_sessions(user_id, session_status, completion_percentage, created_at);

-- Performance records table indexes
CREATE INDEX idx_performance_workout_session ON performance_records(workout_session_id);
CREATE INDEX idx_performance_exercise ON performance_records(exercise_id);
CREATE INDEX idx_performance_user_date ON performance_records(workout_session_id, created_at);
CREATE INDEX idx_performance_user_exercise ON performance_records(workout_session_id, exercise_id);
CREATE INDEX idx_performance_set_number ON performance_records(workout_session_id, set_number);

-- Additional performance indexes for analytics
CREATE INDEX idx_performance_records_achievement_status ON performance_records(achievement_status);
CREATE INDEX idx_performance_records_perceived_exertion ON performance_records(perceived_exertion);
CREATE INDEX idx_performance_records_form_rating ON performance_records(form_rating);
CREATE INDEX idx_performance_records_weight ON performance_records(weight);
CREATE INDEX idx_performance_records_created_at ON performance_records(created_at);
CREATE INDEX idx_performance_records_assigned_by_trainer ON performance_records(assigned_by_trainer_id);

-- Indexes for enhanced performance tracking
CREATE INDEX idx_performance_records_exercise_completed ON performance_records(is_exercise_completed);
CREATE INDEX idx_performance_records_performance_vs_target ON performance_records(performance_vs_target);
CREATE INDEX idx_performance_records_set_timing ON performance_records(set_start_time, set_end_time);
CREATE INDEX idx_performance_records_rest_time ON performance_records(rest_time_before_set_seconds);
CREATE INDEX idx_performance_records_actual_rest_seconds ON performance_records(actual_rest_seconds);
CREATE INDEX idx_performance_records_target_analysis ON performance_records(exercise_id, target_reps_planned, target_weight_planned, performance_vs_target);

-- Composite indexes for complex queries
CREATE INDEX idx_workout_sessions_user_program ON workout_sessions(user_id, workout_program_id);
CREATE INDEX idx_workout_sessions_user_workout_plan ON workout_sessions(user_id, workout_plan_id);
CREATE INDEX idx_performance_records_exercise_date ON performance_records(exercise_id, created_at);
CREATE INDEX idx_performance_records_weight_reps ON performance_records(exercise_id, weight, reps);

-- =====================================================
-- UTILITY FUNCTIONS
-- =====================================================

-- Function to calculate workout completion percentage
CREATE OR REPLACE FUNCTION calculate_workout_completion_percentage(
    p_workout_session_id BIGINT
) RETURNS DECIMAL(5,2) AS $$
DECLARE
total_planned INTEGER;
    total_completed INTEGER;
    completion_pct DECIMAL(5,2);
BEGIN
    -- Get totals from workout session
SELECT total_exercises_planned, total_exercises_completed
INTO total_planned, total_completed
FROM workout_sessions
WHERE workout_session_id = p_workout_session_id;

-- Calculate percentage
IF total_planned IS NULL OR total_planned = 0 THEN
        RETURN 0.0;
END IF;

    completion_pct := (total_completed::DECIMAL / total_planned::DECIMAL) * 100.0;

    -- Ensure it's within bounds
    IF completion_pct > 100.0 THEN
        completion_pct := 100.0;
END IF;

RETURN completion_pct;
END;
$$ LANGUAGE plpgsql;

-- Function to calculate rest time between sets
CREATE OR REPLACE FUNCTION calculate_rest_time_seconds(
    p_previous_set_end TIMESTAMP,
    p_current_set_start TIMESTAMP
) RETURNS INTEGER AS $$
BEGIN
    IF p_previous_set_end IS NULL OR p_current_set_start IS NULL THEN
        RETURN NULL;
END IF;

    IF p_current_set_start <= p_previous_set_end THEN
        RETURN 0;
END IF;

RETURN EXTRACT(EPOCH FROM (p_current_set_start - p_previous_set_end))::INTEGER;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- COMPLETION TRACKING TRIGGER
-- =====================================================

-- Trigger to automatically update completion percentage when exercises are completed
CREATE OR REPLACE FUNCTION update_workout_completion()
RETURNS TRIGGER AS $$
BEGIN
    -- Update the workout session completion when performance record changes
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
UPDATE workout_sessions
SET
    total_exercises_completed = (
        SELECT COUNT(DISTINCT exercise_id)
        FROM performance_records
        WHERE workout_session_id = NEW.workout_session_id
          AND is_exercise_completed = TRUE
    ),
    completion_percentage = calculate_workout_completion_percentage(NEW.workout_session_id),
    updated_at = CURRENT_TIMESTAMP
WHERE workout_session_id = NEW.workout_session_id;
END IF;

RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

-- Create trigger for automatic completion tracking
CREATE TRIGGER trg_update_workout_completion
    AFTER INSERT OR UPDATE OF is_exercise_completed ON performance_records
    FOR EACH ROW
    EXECUTE FUNCTION update_workout_completion();

-- =====================================================
-- UNIQUE CONSTRAINTS
-- =====================================================

-- Ensure unique workout session per user per scheduled workout (if scheduled)
CREATE UNIQUE INDEX idx_unique_scheduled_workout_session ON workout_sessions(scheduled_workout_id)
    WHERE scheduled_workout_id IS NOT NULL;

-- Ensure unique set numbers per exercise per workout session
CREATE UNIQUE INDEX idx_unique_performance_set ON performance_records(workout_session_id, exercise_id, set_number);