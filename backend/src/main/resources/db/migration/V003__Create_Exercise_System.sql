-- =============================================================================
-- V003__Create_Exercise_System.sql
-- Creates exercises, workout_plans, plan_exercise tables + FITNESS GOALS SYSTEM
-- EXACTLY MATCHES the JPA entity definitions with explicit column names
-- INCLUDES isIsometric support for complete workout tracking modalities
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
                           is_cardio BOOLEAN NOT NULL DEFAULT FALSE,
                           is_isometric BOOLEAN NOT NULL DEFAULT FALSE,
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

-- CRITICAL: Exercise modality consistency constraint
ALTER TABLE exercises ADD CONSTRAINT chk_exercise_modality_consistency
    CHECK (
        (is_cardio = TRUE AND is_isometric = FALSE) OR   -- Cardio exercises
        (is_cardio = FALSE AND is_isometric = TRUE) OR   -- Isometric exercises
        (is_cardio = FALSE AND is_isometric = FALSE)     -- Regular strength exercises
        );

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

-- =============================================================================
-- FITNESS GOALS SYSTEM - Multiple goals per exercise with relevance scoring
-- =============================================================================

-- =====================================================
-- FITNESS GOALS DEFINITION TABLE
-- =====================================================
CREATE TABLE fitness_goals (
                               goal_id SERIAL PRIMARY KEY,
                               goal_code VARCHAR(50) NOT NULL UNIQUE,        -- 'build-muscle', 'lose-weight'
                               goal_name VARCHAR(100) NOT NULL,              -- 'Build Muscle', 'Lose Weight'
                               goal_emoji VARCHAR(10),                       -- '💪', '🔥'
                               goal_description TEXT,                        -- Detailed description
                               display_order INTEGER NOT NULL DEFAULT 999,  -- UI ordering (lower = higher priority)
                               is_active BOOLEAN DEFAULT true,              -- Enable/disable goals
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- EXERCISE-GOAL MAPPING (Many-to-Many)
-- =====================================================
CREATE TABLE exercise_goal_mapping (
                                       exercise_id BIGINT NOT NULL,
                                       goal_id INTEGER NOT NULL,
                                       relevance_score INTEGER NOT NULL DEFAULT 3,   -- 1-5: How relevant (1=poor, 5=perfect)
                                       is_primary BOOLEAN DEFAULT false,             -- Mark the most relevant goal as primary
                                       notes TEXT,                                   -- Optional: why this exercise fits this goal
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                       PRIMARY KEY (exercise_id, goal_id),
                                       CONSTRAINT fk_exercise_goal_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(exercise_id) ON DELETE CASCADE,
                                       CONSTRAINT fk_exercise_goal_goal FOREIGN KEY (goal_id) REFERENCES fitness_goals(goal_id) ON DELETE CASCADE,
                                       CONSTRAINT chk_relevance_score CHECK (relevance_score >= 1 AND relevance_score <= 5)
);

-- =====================================================
-- INDEXES FOR PERFORMANCE (Based on Expected Queries)
-- =====================================================

-- Exercises table indexes (including workout tracking modality indexes)
CREATE INDEX idx_exercises_name ON exercises(exercise_name);
CREATE INDEX idx_exercises_type ON exercises(exercise_type);
CREATE INDEX idx_exercises_difficulty ON exercises(difficulty_level);

-- WORKOUT TRACKING MODALITY INDEXES (Critical for performance)
CREATE INDEX idx_exercises_is_cardio ON exercises(is_cardio);
CREATE INDEX idx_exercises_is_isometric ON exercises(is_isometric);
CREATE INDEX idx_exercises_modality_combined ON exercises(is_cardio, is_isometric);

-- Additional optimized indexes for workout filtering
CREATE INDEX idx_exercises_cardio_only ON exercises(is_cardio) WHERE is_cardio = TRUE;
CREATE INDEX idx_exercises_isometric_only ON exercises(is_isometric) WHERE is_isometric = TRUE;
CREATE INDEX idx_exercises_strength_only ON exercises(is_cardio, is_isometric) WHERE is_cardio = FALSE AND is_isometric = FALSE;

-- Standard exercise indexes
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

-- Fitness Goals indexes
CREATE INDEX idx_fitness_goals_code ON fitness_goals(goal_code);
CREATE INDEX idx_fitness_goals_active ON fitness_goals(is_active, display_order);

-- Exercise Goal Mapping indexes
CREATE INDEX idx_exercise_goal_mapping_exercise ON exercise_goal_mapping(exercise_id);
CREATE INDEX idx_exercise_goal_mapping_goal ON exercise_goal_mapping(goal_id);
CREATE INDEX idx_exercise_goal_mapping_relevance ON exercise_goal_mapping(goal_id, relevance_score DESC);
CREATE INDEX idx_exercise_goal_mapping_primary ON exercise_goal_mapping(goal_id, is_primary, relevance_score DESC);

-- Compound index for goal filtering with relevance ordering (performance optimization)
CREATE INDEX idx_exercise_goal_relevance_performance
    ON exercise_goal_mapping(goal_id, relevance_score DESC, exercise_id);

-- Composite indexes for complex queries (including modality-aware searches)
CREATE INDEX idx_exercises_search ON exercises(exercise_type, difficulty_level, published);
CREATE INDEX idx_exercises_search_cardio ON exercises(is_cardio, exercise_type, difficulty_level, published);
CREATE INDEX idx_exercises_search_isometric ON exercises(is_isometric, exercise_type, difficulty_level, published);
CREATE INDEX idx_exercises_search_strength ON exercises(is_cardio, is_isometric, exercise_type, difficulty_level, published);

CREATE INDEX idx_workout_plans_search ON workout_plans(workout_type, difficulty_level, is_public);
CREATE INDEX idx_exercises_popular ON exercises(usage_count, average_rating, published);
CREATE INDEX idx_workout_plans_popular ON workout_plans(times_used, average_rating, is_public);

-- Performance optimization for workout tracking interface queries
CREATE INDEX idx_exercises_tracking_performance ON exercises(is_cardio, is_isometric, published, average_rating DESC);

-- =====================================================
-- INITIAL GOALS DATA
-- =====================================================

-- Tier 1: Core Primary Goals (Most Common - Featured in UI)
INSERT INTO fitness_goals (goal_code, goal_name, goal_emoji, goal_description, display_order) VALUES
                                                                                                  ('build-muscle', 'Build Muscle', '💪', 'Increase muscle mass, size, and definition through resistance training', 1),
                                                                                                  ('lose-weight', 'Lose Weight', '🔥', 'Burn calories, reduce body fat, and achieve weight loss goals', 2),
                                                                                                  ('gain-strength', 'Gain Strength', '🏋️', 'Develop maximum strength, power, and lifting capacity', 3),
                                                                                                  ('improve-endurance', 'Improve Endurance', '⚡', 'Build cardiovascular fitness and muscular stamina', 4),
                                                                                                  ('increase-flexibility', 'Increase Flexibility', '🧘‍♀️', 'Improve mobility, range of motion, and joint health', 5),
                                                                                                  ('athletic-performance', 'Athletic Performance', '🎯', 'Sport-specific training, speed, and agility development', 6);

-- Tier 2: Specialized Goals (Advanced Users)
INSERT INTO fitness_goals (goal_code, goal_name, goal_emoji, goal_description, display_order) VALUES
                                                                                                  ('functional-fitness', 'Functional Fitness', '⚙️', 'Real-world movement patterns for daily activities', 7),
                                                                                                  ('hiit-conditioning', 'HIIT Training', '⚡', 'High-intensity interval training for conditioning', 8),
                                                                                                  ('powerlifting', 'Powerlifting', '🏋️‍♂️', 'Maximum strength in squat, bench press, and deadlift', 9),
                                                                                                  ('bodyweight-training', 'Bodyweight Training', '🤸', 'Calisthenics and exercises requiring no equipment', 10),
                                                                                                  ('rehabilitation', 'Recovery & Rehab', '🛡️', 'Injury recovery and movement restoration', 11);

-- Tier 3: Lifestyle Goals (Holistic Approach)
INSERT INTO fitness_goals (goal_code, goal_name, goal_emoji, goal_description, display_order) VALUES
                                                                                                  ('stress-relief', 'Stress Relief', '🧘', 'Mental health, stress management, and mindfulness', 12),
                                                                                                  ('general-health', 'General Health', '❤️', 'Overall wellness and chronic disease prevention', 13),
                                                                                                  ('event-preparation', 'Event Prep', '🏃', 'Training for marathons, competitions, or fitness challenges', 14);

-- =====================================================
-- UNIQUE CONSTRAINTS
-- =====================================================

-- Ensure unique exercise names
ALTER TABLE exercises ADD CONSTRAINT uk_exercise_name_unique UNIQUE (exercise_name);

-- Ensure exercises appear only once per workout in same order
ALTER TABLE plan_exercise ADD CONSTRAINT uk_plan_exercise_workout_order UNIQUE (workout_plan_id, order_in_workout);

-- =============================================================================
-- USER EXERCISE RATING AND HISTORY SYSTEM
-- =============================================================================

-- =====================================================
-- USER EXERCISE RATINGS TABLE
-- =====================================================

CREATE TABLE user_exercise_ratings (
                                       rating_id BIGSERIAL PRIMARY KEY,

    -- Relationships
                                       user_id BIGINT NOT NULL,
                                       exercise_id BIGINT NOT NULL,

    -- Rating data
                                       rating DOUBLE PRECISION NOT NULL,
                                       comment VARCHAR(500),

    -- Timestamps
                                       rated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
                                       CONSTRAINT fk_user_exercise_rating_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                       CONSTRAINT fk_user_exercise_rating_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(exercise_id) ON DELETE CASCADE,

    -- Prevent duplicate ratings
                                       CONSTRAINT uk_user_exercise_rating_unique UNIQUE (user_id, exercise_id)
);

-- Add constraints for ratings
ALTER TABLE user_exercise_ratings ADD CONSTRAINT chk_rating_range
    CHECK (rating >= 0.0 AND rating <= 5.0);

-- =====================================================
-- USER RATING TAGS TABLE (ElementCollection)
-- =====================================================

CREATE TABLE user_rating_tags (
                                  rating_id BIGINT NOT NULL,
                                  tag VARCHAR(50) NOT NULL,
                                  CONSTRAINT fk_rating_tags_rating FOREIGN KEY (rating_id) REFERENCES user_exercise_ratings(rating_id) ON DELETE CASCADE
);

-- =====================================================
-- USER EXERCISE HISTORY TABLE
-- =====================================================

CREATE TABLE user_exercise_history (
                                       history_id BIGSERIAL PRIMARY KEY,

    -- Relationships
                                       user_id BIGINT NOT NULL,
                                       exercise_id BIGINT NOT NULL,

    -- Usage data
                                       used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       duration_minutes INTEGER,
                                       context VARCHAR(20) NOT NULL DEFAULT 'view',
                                       notes VARCHAR(200),
                                       workout_plan_id BIGINT, -- For future workout plan integration

    -- Foreign key constraints
                                       CONSTRAINT fk_user_exercise_history_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                       CONSTRAINT fk_user_exercise_history_exercise FOREIGN KEY (exercise_id) REFERENCES exercises(exercise_id) ON DELETE CASCADE
);

-- Add constraints for history
ALTER TABLE user_exercise_history ADD CONSTRAINT chk_history_context
    CHECK (context IN ('view', 'workout', 'favorite', 'rate', 'share'));

ALTER TABLE user_exercise_history ADD CONSTRAINT chk_history_duration
    CHECK (duration_minutes IS NULL OR (duration_minutes >= 1 AND duration_minutes <= 480));

-- =====================================================
-- ADDITIONAL INDEXES FOR USER EXERCISE SYSTEM
-- =====================================================

-- User Exercise Ratings indexes
CREATE INDEX idx_user_exercise_ratings_user ON user_exercise_ratings(user_id);
CREATE INDEX idx_user_exercise_ratings_exercise ON user_exercise_ratings(exercise_id);
CREATE INDEX idx_user_exercise_ratings_rating ON user_exercise_ratings(rating);
CREATE INDEX idx_user_exercise_ratings_rated_at ON user_exercise_ratings(rated_at);

-- User Rating Tags indexes
CREATE INDEX idx_user_rating_tags_rating ON user_rating_tags(rating_id);
CREATE INDEX idx_user_rating_tags_tag ON user_rating_tags(tag);

-- User Exercise History indexes
CREATE INDEX idx_user_exercise_history_user ON user_exercise_history(user_id);
CREATE INDEX idx_user_exercise_history_exercise ON user_exercise_history(exercise_id);
CREATE INDEX idx_user_exercise_history_used_at ON user_exercise_history(used_at);
CREATE INDEX idx_user_exercise_history_context ON user_exercise_history(context);

-- Composite indexes for common queries
CREATE INDEX idx_user_exercise_history_user_date ON user_exercise_history(user_id, used_at DESC);
CREATE INDEX idx_user_exercise_history_user_context ON user_exercise_history(user_id, context);
CREATE INDEX idx_user_exercise_ratings_exercise_rating ON user_exercise_ratings(exercise_id, rating DESC);

-- =============================================================================
-- PERFORMANCE VERIFICATION AND LOGGING
-- =============================================================================

-- Log successful migration completion with modality tracking support
DO $$
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE 'V003 Migration completed successfully!';
    RAISE NOTICE 'Exercise system created with workout tracking modality support:';
    RAISE NOTICE '  - TIME_BASED tracking (is_cardio = TRUE)';
    RAISE NOTICE '  - HOLD_BASED tracking (is_isometric = TRUE)';
    RAISE NOTICE '  - REP_BASED tracking (both FALSE)';
    RAISE NOTICE 'Total indexes created: % (optimized for workout tracking)',
                 (SELECT count(*) FROM pg_indexes WHERE tablename LIKE 'exercise%' OR tablename LIKE 'workout%');
    RAISE NOTICE '========================================';
END $$;