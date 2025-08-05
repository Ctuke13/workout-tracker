-- =============================================================================
-- V007__Create_Program_System.sql
-- Creates workout program management system tables with explicit column names
-- EXACTLY MATCHES WorkoutProgram.java, ProgramPlan.java, and ScheduledWorkout.java entity field mappings
-- ✅ UPDATED: Added exercise configuration fields to scheduled_workouts table
-- =============================================================================

-- =====================================================
-- WORKOUT PROGRAMS TABLE - FIXED TO MATCH WorkoutProgram.java EXACTLY
-- =====================================================
CREATE TABLE workout_programs (
                                  workout_program_id BIGSERIAL PRIMARY KEY,

    -- ✅ FIXED: Core fields (match entity field names exactly)
                                  name VARCHAR(100) NOT NULL,                        -- @Column(nullable = false, length = 100)
                                  description TEXT,                                  -- @Column(columnDefinition = "TEXT")
                                  program_type VARCHAR(50) NOT NULL,                 -- @Enumerated @Column(name = "program_type", nullable = false)
                                  difficulty_level VARCHAR(20) NOT NULL,             -- @Enumerated @Column(name = "difficulty_level", nullable = false)
                                  duration_weeks INTEGER NOT NULL,                   -- @Column(name = "duration_weeks", nullable = false)
                                  sessions_per_week INTEGER NOT NULL,                -- @Column(name = "sessions_per_week", nullable = false)
                                  target_goals VARCHAR(500),                         -- @Column(name = "target_goals", length = 500)
                                  equipment_needed VARCHAR(500),                     -- @Column(name = "equipment_needed", length = 500)

    -- ✅ ADDED: Creator information (was missing)
                                  created_by_user_id BIGINT,                         -- @Column(name = "created_by_user_id")
                                  created_by_professional BOOLEAN DEFAULT false,     -- @Column(name = "created_by_professional")

    -- ✅ FIXED: Status and visibility (fixed column names)
                                  is_published BOOLEAN DEFAULT true,                 -- @Column(name = "is_published")
                                  is_public BOOLEAN DEFAULT true,                    -- @Column(name = "is_public")

    -- ✅ FIXED: Usage tracking (fixed column names and added missing columns)
                                  enrollment_count INTEGER DEFAULT 0,               -- @Column(name = "enrollment_count") - was total_enrollments
                                  completion_count INTEGER DEFAULT 0,               -- @Column(name = "completion_count") - WAS MISSING!
                                  average_rating DOUBLE PRECISION DEFAULT 0.0,      -- @Column(name = "average_rating")
                                  total_ratings INTEGER DEFAULT 0,                  -- @Column(name = "total_ratings") - WAS MISSING!

    -- Timestamps
                                  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,  -- @Column(name = "created_at", updatable = false)
                                  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,  -- @Column(name = "updated_at")

    -- ✅ FIXED: Foreign key constraints (removed trainer_id as it's not in entity)
                                  CONSTRAINT fk_workout_programs_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES users(user_id),

    -- ✅ FIXED: Check constraints (match entity validation exactly)
                                  CHECK (duration_weeks >= 1 AND duration_weeks <= 52),           -- @Min(1) @Max(52)
                                  CHECK (sessions_per_week >= 1 AND sessions_per_week <= 7),       -- @Min(1) @Max(7)
                                  CHECK (average_rating IS NULL OR (average_rating >= 0.0 AND average_rating <= 5.0)),  -- @DecimalMin("0.0") @DecimalMax("5.0")
                                  CHECK (enrollment_count >= 0),                                   -- Implicit from business logic
                                  CHECK (completion_count >= 0),                                   -- Implicit from business logic
                                  CHECK (total_ratings >= 0),                                      -- @Min(0)
                                  CHECK (program_type IN ('STRENGTH', 'CARDIO', 'WEIGHT_LOSS', 'MUSCLE_GAIN', 'ENDURANCE', 'FLEXIBILITY', 'REHABILITATION', 'SPORTS_SPECIFIC')),
                                  CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);

-- =====================================================
-- PROGRAM PLANS TABLE - MATCHES ProgramPlan.java EXACTLY
-- =====================================================
CREATE TABLE program_plans (
                               program_plan_id BIGSERIAL PRIMARY KEY,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key relationships (match entity @JoinColumn names)
                               program_id BIGINT NOT NULL,  -- matches @JoinColumn(name = "program_id")
                               workout_plan_id BIGINT,      -- matches @JoinColumn(name = "workout_plan_id", nullable = true)

    -- Core fields (match @Column names exactly)
                               week_number INTEGER NOT NULL,              -- matches @Column(name = "week_number")
                               day_number INTEGER NOT NULL,               -- matches @Column(name = "day_number")
                               display_order INTEGER,                     -- matches @Column(name = "display_order")
                               is_rest_day BOOLEAN DEFAULT false,        -- matches @Column(name = "is_rest_day")
                               notes VARCHAR(500),                       -- matches @Column(name = "notes", length = 500)
                               phase_type VARCHAR(50),                   -- matches @Column(name = "phase_type")
                               target_intensity DECIMAL(5,2),            -- matches @Column(name = "target_intensity")
                               is_optional BOOLEAN DEFAULT false,        -- matches @Column(name = "is_optional")
                               created_by_user_id BIGINT,               -- matches @Column(name = "created_by_user_id")

    -- Foreign key constraints
                               CONSTRAINT fk_program_plans_program FOREIGN KEY (program_id) REFERENCES workout_programs(workout_program_id) ON DELETE CASCADE,
                               CONSTRAINT fk_program_plans_workout_plan FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(workout_plan_id),
                               CONSTRAINT fk_program_plans_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES users(user_id),

    -- Check constraints to match entity validation
                               CHECK (week_number >= 1 AND week_number <= 52),
                               CHECK (day_number >= 1 AND day_number <= 7),
                               CHECK (target_intensity IS NULL OR (target_intensity >= 0.0 AND target_intensity <= 100.0)),
                               CHECK (phase_type IS NULL OR phase_type IN ('PREPARATION', 'BASE_BUILDING', 'INTENSITY', 'RECOVERY', 'PEAK', 'DELOAD', 'SPECIALIZATION', 'TRANSITION', 'MAINTENANCE'))
);

-- =====================================================
-- SCHEDULED WORKOUTS TABLE - MATCHES ScheduledWorkout.java EXACTLY
-- ✅ UPDATED: Added exercise configuration fields for workout tracking
-- =====================================================
CREATE TABLE scheduled_workouts (
                                    scheduled_workout_id BIGSERIAL PRIMARY KEY,
                                    user_id BIGINT NOT NULL,
                                    program_id BIGINT,  -- matches @JoinColumn(name = "program_id")
                                    program_plan_id BIGINT,
                                    workout_plan_id BIGINT NOT NULL,
                                    scheduled_date DATE NOT NULL,
                                    scheduled_time TIME,
                                    status VARCHAR(30) DEFAULT 'SCHEDULED',
                                    notes TEXT,
                                    reminder_sent BOOLEAN DEFAULT false,
                                    completed_at TIMESTAMP WITH TIME ZONE,
                                    workout_session_id BIGINT,
                                    subscription_required BOOLEAN DEFAULT false,
                                    trainer_assigned_id BIGINT,
                                    is_makeup_workout BOOLEAN DEFAULT false,
                                    original_scheduled_workout_id BIGINT,
                                    reschedule_reason VARCHAR(255),
                                    weather_dependent BOOLEAN DEFAULT false,
                                    location_specific BOOLEAN DEFAULT false,
                                    group_workout BOOLEAN DEFAULT false,
                                    max_participants INTEGER,
                                    current_participants INTEGER DEFAULT 0,
                                    created_by_user_id BIGINT,
                                    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Columns from ScheduledWorkout.java entity
                                    week_number INTEGER,                                    -- @Column(name = "week_number")
                                    day_of_week INTEGER,                                   -- @Column(name = "day_of_week")
                                    custom_notes TEXT,                                     -- @Column(name = "custom_notes", columnDefinition = "TEXT")
                                    reminder_time TIMESTAMP WITH TIME ZONE,               -- @Column(name = "reminder_time")
                                    estimated_duration_minutes INTEGER,                   -- @Column(name = "estimated_duration_minutes")

    -- =============================================================================
    -- ✅ NEW: EXERCISE CONFIGURATION FIELDS (Step 6 of Solution)
    -- =============================================================================
    -- Strength exercise fields
                                    sets INTEGER,                                          -- @Column(name = "sets")
                                    reps VARCHAR(50),                                      -- @Column(name = "reps")
                                    weight DOUBLE PRECISION,                               -- @Column(name = "weight")
                                    rest_seconds INTEGER,                                  -- @Column(name = "rest_seconds")
                                    tempo VARCHAR(20),                                     -- @Column(name = "tempo")
                                    target_rpe INTEGER,                                    -- @Column(name = "target_rpe")

    -- Cardio exercise fields
                                    target_duration_minutes INTEGER,                      -- @Column(name = "target_duration_minutes")
                                    target_distance_km DOUBLE PRECISION,                  -- @Column(name = "target_distance_km")
                                    target_pace DOUBLE PRECISION,                         -- @Column(name = "target_pace")

    -- Isometric exercise fields
                                    hold_duration_seconds INTEGER,                        -- @Column(name = "hold_duration_seconds")

    -- Foreign key constraints
                                    CONSTRAINT fk_scheduled_workouts_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                    CONSTRAINT fk_scheduled_workouts_program FOREIGN KEY (program_id) REFERENCES workout_programs(workout_program_id),
                                    CONSTRAINT fk_scheduled_workouts_program_plan FOREIGN KEY (program_plan_id) REFERENCES program_plans(program_plan_id),
                                    CONSTRAINT fk_scheduled_workouts_workout_plan FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(workout_plan_id),
                                    CONSTRAINT fk_scheduled_workouts_workout_session FOREIGN KEY (workout_session_id) REFERENCES workout_sessions(workout_session_id),
                                    CONSTRAINT fk_scheduled_workouts_trainer FOREIGN KEY (trainer_assigned_id) REFERENCES users(user_id),
                                    CONSTRAINT fk_scheduled_workouts_original FOREIGN KEY (original_scheduled_workout_id) REFERENCES scheduled_workouts(scheduled_workout_id),
                                    CONSTRAINT fk_scheduled_workouts_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES users(user_id),

    -- Check constraints
                                    CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'POSTPONED', 'NO_SHOW')),
                                    CHECK (max_participants IS NULL OR max_participants >= 1),
                                    CHECK (current_participants >= 0),
                                    CHECK (current_participants IS NULL OR max_participants IS NULL OR current_participants <= max_participants),
                                    CHECK (day_of_week IS NULL OR (day_of_week >= 1 AND day_of_week <= 7)),
                                    CHECK (week_number IS NULL OR week_number >= 1),
                                    CHECK (estimated_duration_minutes IS NULL OR estimated_duration_minutes > 0),

    -- ✅ NEW: Check constraints for exercise configuration fields
                                    CHECK (sets IS NULL OR sets > 0),
                                    CHECK (weight IS NULL OR weight >= 0),
                                    CHECK (rest_seconds IS NULL OR rest_seconds >= 0),
                                    CHECK (target_rpe IS NULL OR (target_rpe >= 1 AND target_rpe <= 10)),
                                    CHECK (target_duration_minutes IS NULL OR target_duration_minutes > 0),
                                    CHECK (target_distance_km IS NULL OR target_distance_km > 0),
                                    CHECK (target_pace IS NULL OR target_pace > 0),
                                    CHECK (hold_duration_seconds IS NULL OR hold_duration_seconds > 0)
);

-- =====================================================
-- ADD FOREIGN KEY CONSTRAINTS TO EXISTING TABLES
-- =====================================================

-- Add foreign key constraints to workout_sessions table (connecting to program system)
ALTER TABLE workout_sessions
    ADD CONSTRAINT fk_workout_sessions_program FOREIGN KEY (workout_program_id) REFERENCES workout_programs(workout_program_id);

ALTER TABLE workout_sessions
    ADD CONSTRAINT fk_workout_sessions_scheduled_workout FOREIGN KEY (scheduled_workout_id) REFERENCES scheduled_workouts(scheduled_workout_id);

-- =====================================================
-- PERFORMANCE INDEXES - FIXED TO MATCH CORRECTED SCHEMA
-- =====================================================

-- ✅ FIXED: Workout Programs indexes (updated column names)
CREATE INDEX idx_workout_programs_created_by_user ON workout_programs(created_by_user_id);
CREATE INDEX idx_workout_programs_type ON workout_programs(program_type);
CREATE INDEX idx_workout_programs_difficulty ON workout_programs(difficulty_level);
CREATE INDEX idx_workout_programs_published ON workout_programs(is_published);
CREATE INDEX idx_workout_programs_public ON workout_programs(is_public);
CREATE INDEX idx_workout_programs_professional ON workout_programs(created_by_professional);
CREATE INDEX idx_workout_programs_enrollment ON workout_programs(enrollment_count);
CREATE INDEX idx_workout_programs_completion ON workout_programs(completion_count);
CREATE INDEX idx_workout_programs_rating ON workout_programs(average_rating);
CREATE INDEX idx_workout_programs_total_ratings ON workout_programs(total_ratings);
CREATE INDEX idx_workout_programs_duration ON workout_programs(duration_weeks);
CREATE INDEX idx_workout_programs_sessions_per_week ON workout_programs(sessions_per_week);

-- Program Plans indexes (using correct column names that match entity)
CREATE INDEX idx_program_plans_program ON program_plans(program_id);
CREATE INDEX idx_program_plans_workout_plan ON program_plans(workout_plan_id);
CREATE INDEX idx_program_plans_week ON program_plans(program_id, week_number);
CREATE INDEX idx_program_plans_day ON program_plans(program_id, day_number);
CREATE INDEX idx_program_plans_phase ON program_plans(program_id, phase_type);
CREATE INDEX idx_program_plans_created_by_user ON program_plans(created_by_user_id);

-- Scheduled Workouts indexes
CREATE INDEX idx_scheduled_workouts_user ON scheduled_workouts(user_id);
CREATE INDEX idx_scheduled_workouts_program ON scheduled_workouts(program_id);
CREATE INDEX idx_scheduled_workouts_workout_plan ON scheduled_workouts(workout_plan_id);
CREATE INDEX idx_scheduled_workouts_date ON scheduled_workouts(scheduled_date);
CREATE INDEX idx_scheduled_workouts_status ON scheduled_workouts(status);
CREATE INDEX idx_scheduled_workouts_user_date ON scheduled_workouts(user_id, scheduled_date);
CREATE INDEX idx_scheduled_workouts_trainer ON scheduled_workouts(trainer_assigned_id);
CREATE INDEX idx_scheduled_workouts_created_by_user ON scheduled_workouts(created_by_user_id);
CREATE INDEX idx_scheduled_workouts_week_day ON scheduled_workouts(week_number, day_of_week);
CREATE INDEX idx_scheduled_workouts_reminder_time ON scheduled_workouts(reminder_time);

-- ✅ NEW: Indexes for exercise configuration fields
CREATE INDEX idx_scheduled_workouts_sets ON scheduled_workouts(sets) WHERE sets IS NOT NULL;
CREATE INDEX idx_scheduled_workouts_weight ON scheduled_workouts(weight) WHERE weight IS NOT NULL;
CREATE INDEX idx_scheduled_workouts_target_duration ON scheduled_workouts(target_duration_minutes) WHERE target_duration_minutes IS NOT NULL;

-- =====================================================
-- UNIQUE CONSTRAINTS - FIXED
-- =====================================================

-- ✅ FIXED: Ensure unique program names per creator (updated column name)
ALTER TABLE workout_programs ADD CONSTRAINT uk_program_name_creator UNIQUE (name, created_by_user_id);

-- Ensure unique program plan per program/week/day (using correct column names)
ALTER TABLE program_plans ADD CONSTRAINT uk_program_plan_week_day UNIQUE (program_id, week_number, day_number);

-- =====================================================
-- COMMENTS FOR DOCUMENTATION - FIXED
-- =====================================================

COMMENT ON TABLE workout_programs IS 'Workout program templates created by users and professionals';
COMMENT ON TABLE program_plans IS 'Links specific workout plans to specific days/weeks within programs';
COMMENT ON TABLE scheduled_workouts IS 'Individual scheduled workout instances for users with exercise configuration support';

-- ✅ FIXED: Column comments to match entity
COMMENT ON COLUMN workout_programs.name IS 'Program name (2-100 characters)';
COMMENT ON COLUMN workout_programs.description IS 'Detailed program description';
COMMENT ON COLUMN workout_programs.program_type IS 'Type of workout program (STRENGTH, CARDIO, WEIGHT_LOSS, etc.)';
COMMENT ON COLUMN workout_programs.difficulty_level IS 'Program difficulty (BEGINNER, INTERMEDIATE, ADVANCED)';
COMMENT ON COLUMN workout_programs.duration_weeks IS 'Program duration in weeks (1-52)';
COMMENT ON COLUMN workout_programs.sessions_per_week IS 'Sessions per week (1-7)';
COMMENT ON COLUMN workout_programs.target_goals IS 'Program goals and objectives';
COMMENT ON COLUMN workout_programs.equipment_needed IS 'Required equipment for the program';
COMMENT ON COLUMN workout_programs.created_by_user_id IS 'ID of user who created this program';
COMMENT ON COLUMN workout_programs.created_by_professional IS 'Whether created by a professional trainer';
COMMENT ON COLUMN workout_programs.is_published IS 'Whether program is published and active';
COMMENT ON COLUMN workout_programs.is_public IS 'Whether program is publicly visible';
COMMENT ON COLUMN workout_programs.enrollment_count IS 'Number of users enrolled in program';
COMMENT ON COLUMN workout_programs.completion_count IS 'Number of users who completed program';
COMMENT ON COLUMN workout_programs.average_rating IS 'Average user rating (0.0-5.0)';
COMMENT ON COLUMN workout_programs.total_ratings IS 'Total number of ratings received';

COMMENT ON COLUMN program_plans.week_number IS 'Week number within the program (1-52)';
COMMENT ON COLUMN program_plans.day_number IS 'Day number within the week (1=Monday, 7=Sunday)';
COMMENT ON COLUMN program_plans.phase_type IS 'Training phase type (enum: PREPARATION, BASE_BUILDING, etc.)';
COMMENT ON COLUMN program_plans.target_intensity IS 'Target intensity percentage (0.0-100.0)';
COMMENT ON COLUMN program_plans.created_by_user_id IS 'User who created this program plan entry';

COMMENT ON COLUMN scheduled_workouts.status IS 'Current status of scheduled workout';
COMMENT ON COLUMN scheduled_workouts.subscription_required IS 'Whether user needs active subscription';
COMMENT ON COLUMN scheduled_workouts.created_by_user_id IS 'User who created/assigned this scheduled workout (for coach assignments)';
COMMENT ON COLUMN scheduled_workouts.custom_notes IS 'User-specific notes for this scheduled workout';
COMMENT ON COLUMN scheduled_workouts.day_of_week IS 'Day of week (1=Monday, 7=Sunday)';
COMMENT ON COLUMN scheduled_workouts.reminder_time IS 'When to send reminder for this workout';
COMMENT ON COLUMN scheduled_workouts.estimated_duration_minutes IS 'Estimated duration for this specific workout instance';

-- ✅ NEW: Comments for exercise configuration fields
COMMENT ON COLUMN scheduled_workouts.sets IS 'Number of sets for strength exercises';
COMMENT ON COLUMN scheduled_workouts.reps IS 'Number of reps per set (e.g., "8-12", "15", "AMRAP")';
COMMENT ON COLUMN scheduled_workouts.weight IS 'Weight to use for exercise (in kg or lbs)';
COMMENT ON COLUMN scheduled_workouts.rest_seconds IS 'Rest time between sets in seconds';
COMMENT ON COLUMN scheduled_workouts.tempo IS 'Exercise tempo (e.g., "3-1-2-1")';
COMMENT ON COLUMN scheduled_workouts.target_rpe IS 'Target Rate of Perceived Exertion (1-10)';
COMMENT ON COLUMN scheduled_workouts.target_duration_minutes IS 'Target duration for cardio exercises';
COMMENT ON COLUMN scheduled_workouts.target_distance_km IS 'Target distance for cardio exercises';
COMMENT ON COLUMN scheduled_workouts.target_pace IS 'Target pace for cardio exercises (min/km)';
COMMENT ON COLUMN scheduled_workouts.hold_duration_seconds IS 'Hold duration for isometric exercises';