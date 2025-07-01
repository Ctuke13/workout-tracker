-- =============================================================================
-- V005__Create_Workout_Tracking_System.sql
-- Creates workout_sessions and performance_records tables
-- EXACTLY MATCHES the WorkoutSession and PerformanceRecord entities
-- =============================================================================

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

    -- Session context
                                  mood VARCHAR(20),
                                  location VARCHAR(20),

    -- Program integration (nullable - WorkoutProgram/ScheduledWorkout tables don't exist yet)
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
    -- Note: workout_program_id and scheduled_workout_id will be added when those tables are created
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
                                     tempo VARCHAR(20),

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

-- =====================================================
-- INDEXES FOR PERFORMANCE (Based on Entity @Index annotations and Expected Queries)
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

-- Performance records table indexes (matching entity @Index annotations)
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

-- Composite indexes for complex queries
CREATE INDEX idx_workout_sessions_user_program ON workout_sessions(user_id, workout_program_id);
CREATE INDEX idx_workout_sessions_user_workout_plan ON workout_sessions(user_id, workout_plan_id);
CREATE INDEX idx_performance_records_exercise_date ON performance_records(exercise_id, created_at);
CREATE INDEX idx_performance_records_weight_reps ON performance_records(exercise_id, weight, reps);

-- =====================================================
-- UNIQUE CONSTRAINTS
-- =====================================================

-- Ensure unique workout session per user per scheduled workout (if scheduled)
CREATE UNIQUE INDEX idx_unique_scheduled_workout_session ON workout_sessions(scheduled_workout_id)
    WHERE scheduled_workout_id IS NOT NULL;

-- Ensure unique set numbers per exercise per workout session
CREATE UNIQUE INDEX idx_unique_performance_set ON performance_records(workout_session_id, exercise_id, set_number);