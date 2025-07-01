-- =============================================================================
-- V003__Create_Exercise_System.sql
-- Creates exercises, workout_plans, and plan_exercise tables
-- EXACTLY MATCHES the JPA entity definitions with explicit column names
-- =============================================================================

-- =====================================================
-- EXERCISES TABLE (matches Exercise.java exactly)
-- =====================================================

CREATE TABLE exercises (
                           exercise_id BIGSERIAL PRIMARY KEY,

    -- Core exercise info
                           exercise_name VARCHAR(100) NOT NULL,
                           emoji VARCHAR(10),
                           description TEXT,
                           exercise_type VARCHAR(30) NOT NULL DEFAULT 'STRENGTH',
                           difficulty_level VARCHAR(20) NOT NULL DEFAULT 'BEGINNER',

    -- Duration and calories
                           estimated_duration_minutes INTEGER,
                           estimated_calories INTEGER,

    -- Media
                           video_url VARCHAR(500),

    -- Creator tracking
                           created_by_user_id BIGINT,
                           created_by_professional BOOLEAN DEFAULT false,

    -- Popularity and ratings
                           usage_count INTEGER DEFAULT 0,
                           average_rating DOUBLE PRECISION DEFAULT 0.0,
                           total_ratings INTEGER DEFAULT 0,

    -- Publication status
                           published BOOLEAN DEFAULT true,

    -- Timestamps
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add constraints for Exercise enum values
ALTER TABLE exercises ADD CONSTRAINT chk_exercise_type
    CHECK (exercise_type IN ('STRENGTH', 'CARDIO', 'FLEXIBILITY', 'BALANCE', 'PLYOMETRIC', 'REHABILITATION', 'SPORTS_SPECIFIC'));

ALTER TABLE exercises ADD CONSTRAINT chk_exercise_difficulty_level
    CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'));

ALTER TABLE exercises ADD CONSTRAINT chk_exercise_duration
    CHECK (estimated_duration_minutes IS NULL OR (estimated_duration_minutes >= 1 AND estimated_duration_minutes <= 480));

ALTER TABLE exercises ADD CONSTRAINT chk_exercise_calories
    CHECK (estimated_calories IS NULL OR (estimated_calories >= 0 AND estimated_calories <= 2000));

ALTER TABLE exercises ADD CONSTRAINT chk_exercise_usage_count
    CHECK (usage_count >= 0);

ALTER TABLE exercises ADD CONSTRAINT chk_exercise_rating
    CHECK (average_rating >= 0.0 AND average_rating <= 5.0);

ALTER TABLE exercises ADD CONSTRAINT chk_exercise_total_ratings
    CHECK (total_ratings >= 0);

-- =====================================================
-- WORKOUT_PLANS TABLE (matches WorkoutPlan.java exactly)
-- =====================================================

CREATE TABLE workout_plans (
                               workout_plan_id BIGSERIAL PRIMARY KEY,

    -- Core workout info
                               workout_name VARCHAR(255) NOT NULL,
                               workout_description TEXT,
                               workout_category VARCHAR(255) NOT NULL,
                               image_url VARCHAR(500),

    -- Workout characteristics
                               is_cardio BOOLEAN DEFAULT false,
                               workout_type VARCHAR(30) DEFAULT 'STRENGTH',
                               estimated_duration_minutes INTEGER,
                               difficulty_level VARCHAR(20) DEFAULT 'BEGINNER',

    -- Target info (stored as comma-separated strings)
                               target_muscle_groups VARCHAR(500),
                               equipment_needed VARCHAR(500),

    -- Access control
                               subscription_tier_required VARCHAR(20) DEFAULT 'FREE',

    -- Creator and visibility
                               created_by_user_id BIGINT,
                               is_public BOOLEAN DEFAULT true,

    -- Popularity tracking
                               times_used INTEGER DEFAULT 0,
                               average_rating DOUBLE PRECISION DEFAULT 0.0,

    -- Timestamps
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add constraints for WorkoutPlan enum values
ALTER TABLE workout_plans ADD CONSTRAINT chk_workout_type
    CHECK (workout_type IN ('STRENGTH', 'CARDIO', 'FLEXIBILITY', 'MIXED', 'HIIT', 'POWERLIFTING'));

ALTER TABLE workout_plans ADD CONSTRAINT chk_workout_difficulty_level
    CHECK (difficulty_level IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'));

ALTER TABLE workout_plans ADD CONSTRAINT chk_workout_subscription_tier
    CHECK (subscription_tier_required IN ('FREE', 'PLUS', 'PRO', 'PRO_PROFESSIONAL'));

ALTER TABLE workout_plans ADD CONSTRAINT chk_workout_times_used
    CHECK (times_used >= 0);

ALTER TABLE workout_plans ADD CONSTRAINT chk_workout_rating
    CHECK (average_rating >= 0.0 AND average_rating <= 5.0);

-- =====================================================
-- PLAN_EXERCISE TABLE (matches PlanExercise.java exactly)
-- =====================================================

CREATE TABLE plan_exercise (
                               plan_exercise_id BIGSERIAL PRIMARY KEY,

    -- Relationships
                               workout_plan_id BIGINT NOT NULL,
                               exercise_id BIGINT NOT NULL,
                               order_in_workout INTEGER NOT NULL,

    -- Exercise prescription
                               prescribed_sets INTEGER,
                               prescribed_reps VARCHAR(100),
                               prescribed_weight_percent DOUBLE PRECISION,
                               prescribed_rest_seconds INTEGER,
                               prescribed_tempo VARCHAR(50),
                               prescribed_rpe INTEGER,

    -- Instructions and coaching
                               instructions TEXT,
                               coaching_cues TEXT,
                               modification_notes TEXT,
                               alternative_exercise_id BIGINT,

    -- Progression tracking
                               is_progression_exercise BOOLEAN DEFAULT false,
                               progression_goal VARCHAR(500),

    -- Access control
                               subscription_tier_required VARCHAR(20) DEFAULT 'FREE',

    -- Workout structure
                               is_optional BOOLEAN DEFAULT false,
                               is_superset BOOLEAN DEFAULT false,
                               superset_group VARCHAR(10),
                               equipment_alternatives TEXT,

    -- Creator tracking
                               created_by_user_id BIGINT,
                               is_user_customization BOOLEAN DEFAULT false,

    -- Timestamps
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
                               CONSTRAINT fk_plan_exercise_workout_plan FOREIGN KEY (workout_plan_id) REFERENCES workout_plans(workout_plan_id) ON DELETE CASCADE,
                               CONSTRAINT fk_plan_exercise_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(exercise_id) ON DELETE CASCADE,
                               CONSTRAINT fk_plan_exercise_alternative FOREIGN KEY (alternative_exercise_id) REFERENCES exercises(exercise_id) ON DELETE SET NULL
);

-- Add constraints for PlanExercise values
ALTER TABLE plan_exercise ADD CONSTRAINT chk_plan_exercise_order
    CHECK (order_in_workout >= 1);

ALTER TABLE plan_exercise ADD CONSTRAINT chk_plan_exercise_rpe
    CHECK (prescribed_rpe IS NULL OR (prescribed_rpe >= 1 AND prescribed_rpe <= 10));

ALTER TABLE plan_exercise ADD CONSTRAINT chk_plan_exercise_subscription_tier
    CHECK (subscription_tier_required IN ('FREE', 'PLUS', 'PRO', 'PRO_PROFESSIONAL'));

-- =====================================================
-- COLLECTION TABLES FOR EXERCISES (@ElementCollection)
-- =====================================================

-- Exercise muscle groups
CREATE TABLE exercise_muscle_groups (
                                        exercise_id BIGINT NOT NULL,
                                        muscle_group VARCHAR(50) NOT NULL,
                                        CONSTRAINT fk_muscle_groups_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(exercise_id) ON DELETE CASCADE
);

-- Exercise equipment
CREATE TABLE exercise_equipment (
                                    exercise_id BIGINT NOT NULL,
                                    equipment VARCHAR(50) NOT NULL,
                                    CONSTRAINT fk_equipment_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(exercise_id) ON DELETE CASCADE
);

-- Exercise benefits
CREATE TABLE exercise_benefits (
                                   exercise_id BIGINT NOT NULL,
                                   benefit VARCHAR(100) NOT NULL,
                                   CONSTRAINT fk_benefits_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(exercise_id) ON DELETE CASCADE
);

-- Exercise tips
CREATE TABLE exercise_tips (
                               exercise_id BIGINT NOT NULL,
                               tip VARCHAR(200) NOT NULL,
                               CONSTRAINT fk_tips_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(exercise_id) ON DELETE CASCADE
);

-- =====================================================
-- INDEXES FOR PERFORMANCE (Based on Expected Queries)
-- =====================================================

-- Exercises table indexes
CREATE INDEX idx_exercises_name ON exercises(exercise_name);
CREATE INDEX idx_exercises_type ON exercises(exercise_type);
CREATE INDEX idx_exercises_difficulty ON exercises(difficulty_level);
CREATE INDEX idx_exercises_published ON exercises(published);
CREATE INDEX idx_exercises_creator ON exercises(created_by_user_id);
CREATE INDEX idx_exercises_professional ON exercises(created_by_professional);
CREATE INDEX idx_exercises_usage ON exercises(usage_count);
CREATE INDEX idx_exercises_rating ON exercises(average_rating);
CREATE INDEX idx_exercises_created_at ON exercises(created_at);
CREATE INDEX idx_exercises_duration ON exercises(estimated_duration_minutes);

-- Workout plans table indexes
CREATE INDEX idx_workout_plans_name ON workout_plans(workout_name);
CREATE INDEX idx_workout_plans_category ON workout_plans(workout_category);
CREATE INDEX idx_workout_plans_type ON workout_plans(workout_type);
CREATE INDEX idx_workout_plans_difficulty ON workout_plans(difficulty_level);
CREATE INDEX idx_workout_plans_public ON workout_plans(is_public);
CREATE INDEX idx_workout_plans_creator ON workout_plans(created_by_user_id);
CREATE INDEX idx_workout_plans_tier ON workout_plans(subscription_tier_required);
CREATE INDEX idx_workout_plans_times_used ON workout_plans(times_used);
CREATE INDEX idx_workout_plans_rating ON workout_plans(average_rating);
CREATE INDEX idx_workout_plans_created_at ON workout_plans(created_at);

-- Plan exercise table indexes
CREATE INDEX idx_plan_exercise_workout_plan ON plan_exercise(workout_plan_id);
CREATE INDEX idx_plan_exercise_exercise ON plan_exercise(exercise_id);
CREATE INDEX idx_plan_exercise_order ON plan_exercise(workout_plan_id, order_in_workout);
CREATE INDEX idx_plan_exercise_alternative ON plan_exercise(alternative_exercise_id);
CREATE INDEX idx_plan_exercise_creator ON plan_exercise(created_by_user_id);
CREATE INDEX idx_plan_exercise_progression ON plan_exercise(is_progression_exercise);
CREATE INDEX idx_plan_exercise_superset ON plan_exercise(workout_plan_id, superset_group);

-- Collection table indexes
CREATE INDEX idx_muscle_groups_exercise ON exercise_muscle_groups(exercise_id);
CREATE INDEX idx_muscle_groups_group ON exercise_muscle_groups(muscle_group);
CREATE INDEX idx_equipment_exercise ON exercise_equipment(exercise_id);
CREATE INDEX idx_equipment_equipment ON exercise_equipment(equipment);
CREATE INDEX idx_benefits_exercise ON exercise_benefits(exercise_id);
CREATE INDEX idx_tips_exercise ON exercise_tips(exercise_id);

-- Composite indexes for complex queries
CREATE INDEX idx_exercises_search ON exercises(exercise_type, difficulty_level, published);
CREATE INDEX idx_workout_plans_search ON workout_plans(workout_type, difficulty_level, is_public);
CREATE INDEX idx_exercises_popular ON exercises(usage_count, average_rating, published);
CREATE INDEX idx_workout_plans_popular ON workout_plans(times_used, average_rating, is_public);

-- =====================================================
-- UNIQUE CONSTRAINTS
-- =====================================================

-- Ensure unique exercise names
ALTER TABLE exercises ADD CONSTRAINT uk_exercise_name_unique UNIQUE (exercise_name);

-- Ensure exercises appear only once per workout in same order
ALTER TABLE plan_exercise ADD CONSTRAINT uk_plan_exercise_workout_order UNIQUE (workout_plan_id, order_in_workout);