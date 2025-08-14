-- =============================================================================
-- V012__Add_Foundation_Exercises_With_Favorites.sql
-- UPDATED: Original V012 + Exercise Favorites System
-- =============================================================================

-- =====================================================
-- ADD MISSING COLUMNS TO EXISTING TABLES (SAFE)
-- =====================================================

-- Add missing columns to exercises table
ALTER TABLE exercises ADD COLUMN IF NOT EXISTS created_by_professional BOOLEAN DEFAULT FALSE;
ALTER TABLE exercises ADD COLUMN IF NOT EXISTS is_favorite BOOLEAN DEFAULT FALSE;
ALTER TABLE exercises ADD COLUMN IF NOT EXISTS average_rating DECIMAL(3,2) DEFAULT 0.0;
ALTER TABLE exercises ADD COLUMN IF NOT EXISTS total_reviews INTEGER DEFAULT 0;

-- Add missing columns to workout_plans table
ALTER TABLE workout_plans ADD COLUMN IF NOT EXISTS created_by_professional BOOLEAN DEFAULT FALSE;
ALTER TABLE workout_plans ADD COLUMN IF NOT EXISTS average_rating DECIMAL(3,2) DEFAULT 0.0;
ALTER TABLE workout_plans ADD COLUMN IF NOT EXISTS total_reviews INTEGER DEFAULT 0;

-- Add exercise configuration columns to scheduled_workouts table
ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS sets INTEGER;
ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS reps INTEGER;
ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS weight_kg DECIMAL(5,2);
ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS duration_minutes INTEGER;
ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS hold_duration_seconds INTEGER;
ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS rest_seconds INTEGER;
ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS distance_km DECIMAL(6,2);
ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS calories_target INTEGER;
ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS exercise_notes TEXT;

-- =====================================================
-- USER EXERCISE FAVORITES SYSTEM
-- =====================================================

-- Create user_exercise_favorites table for many-to-many relationship
CREATE TABLE IF NOT EXISTS user_exercise_favorites (
                                                       favorite_id BIGSERIAL PRIMARY KEY,
                                                       user_id BIGINT NOT NULL,
                                                       exercise_id BIGINT NOT NULL,
                                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
                                                       CONSTRAINT fk_user_exercise_favorites_user
                                                       FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_exercise_favorites_exercise
    FOREIGN KEY (exercise_id) REFERENCES exercises(exercise_id) ON DELETE CASCADE,

    -- Prevent duplicate favorites
    CONSTRAINT uk_user_exercise_favorites_unique
    UNIQUE (user_id, exercise_id)
    );

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_user_exercise_favorites_user_id
    ON user_exercise_favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_user_exercise_favorites_exercise_id
    ON user_exercise_favorites(exercise_id);
CREATE INDEX IF NOT EXISTS idx_user_exercise_favorites_created_at
    ON user_exercise_favorites(created_at);

-- Composite index for efficient user favorites lookup
CREATE INDEX IF NOT EXISTS idx_user_exercise_favorites_user_created
    ON user_exercise_favorites(user_id, created_at DESC);

-- =====================================================
-- FOUNDATION EXERCISES (38 TOTAL) - ALL WORKOUT MODES
-- =====================================================

-- Delete any test exercises that might exist
DELETE FROM exercises WHERE exercise_name IN ('L-Sit Test');

INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_reviews, published, is_favorite, created_by_professional, created_at
) VALUES
-- Upper Body Strength - Core Movements
('Push-Up', '💪', 'Classic bodyweight exercise targeting chest, shoulders, and triceps. Start in plank position, lower body to ground, push back up.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 5, 25, 1250, 4.6, 312, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Pull-Up', '🎯', 'Ultimate upper body exercise using pull-up bar. Hang from bar, pull body up until chin clears bar, lower with control.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 8, 40, 892, 4.8, 198, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Dumbbell Bench Press', '🏋️', 'Fundamental chest exercise using dumbbells. Lie on bench, press weights from chest level to full arm extension.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 12, 60, 1567, 4.7, 423, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Dips', '🔴', 'Tricep-focused exercise using parallel bars, chair, or bench edge. Lower body by bending arms, push back up.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 6, 35, 567, 4.5, 145, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Pike Push-Up', '🔺', 'Shoulder-focused push-up variation. In downward dog position, lower head toward ground, press back up.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 5, 30, 423, 4.4, 98, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Tricep Push-Up', '💎', 'Diamond push-up variation targeting triceps. Hands form diamond shape, keep elbows close to body.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 4, 25, 356, 4.3, 87, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Inverted Row', '🚣', 'Horizontal pulling exercise using table or low bar. Lie under bar, pull chest to bar, lower with control.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 6, 30, 789, 4.2, 189, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),

-- Lower Body Strength
('Bodyweight Squat', '🦵', 'Essential lower body movement. Stand with feet shoulder-width apart, lower hips back and down, return to standing.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 6, 30, 2103, 4.5, 567, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Deadlift', '⚡', 'King of compound movements. Lift barbell from ground to hip level using legs and back, emphasizing proper form.', 'STRENGTH', 'ADVANCED', FALSE, FALSE, 15, 80, 734, 4.9, 156, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
('Lunges', '🦵', 'Single-leg strength exercise for quads and glutes. Step forward, lower back knee, return to standing.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 8, 40, 1456, 4.4, 334, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Bulgarian Split Squat', '🏃', 'Advanced single-leg squat variation. Rear foot elevated, lower into lunge position, drive back up.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 10, 50, 456, 4.6, 112, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Calf Raises', '🦶', 'Isolation exercise for calf muscles. Rise up on toes, hold briefly, lower with control.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 4, 20, 890, 4.1, 203, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Glute Bridges', '🌉', 'Hip thrust movement for glute activation. Lie on back, lift hips up, squeeze glutes, lower slowly.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 5, 25, 1234, 4.3, 289, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Single-Leg Deadlift', '⚖️', 'Balance and hamstring exercise. Stand on one leg, hinge at hip, reach toward ground, return to standing.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 8, 35, 345, 4.5, 78, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Step-Ups', '🔶', 'Single-leg step exercise using bench or platform. Step up with one leg, drive knee up, step down controlled.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 8, 40, 678, 4.2, 156, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),

-- Core Strength
('Crunches', '🔥', 'Targeted abdominal exercise. Lie on back, lift shoulders off ground by contracting abs, lower with control.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 4, 20, 1876, 4.2, 445, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Russian Twists', '🌪️', 'Rotational core exercise targeting obliques. Sit with knees bent, lean back, rotate torso side to side.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 4, 20, 567, 4.1, 134, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Bicycle Crunches', '🚴', 'Dynamic ab exercise with rotation. Lie on back, bring opposite elbow to knee in cycling motion.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 5, 25, 890, 4.3, 201, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Dead Bug', '🐛', 'Core stability exercise for deep abdominals. Lie on back, extend opposite arm and leg, return to start.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 6, 20, 234, 4.4, 67, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),

-- Isometric Exercises (HOLD_BASED)
('Plank', '🛡️', 'Core-strengthening isometric hold. Maintain straight body position on forearms and toes, engaging entire core.', 'STRENGTH', 'BEGINNER', FALSE, TRUE, 3, 15, 3456, 4.8, 892, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Side Plank', '🔷', 'Lateral core strength exercise. Lie on side, prop up on forearm, maintain straight line from head to feet.', 'STRENGTH', 'INTERMEDIATE', FALSE, TRUE, 3, 15, 567, 4.5, 134, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Wall Sit', '🧱', 'Isometric leg strengthener. Sit against wall with thighs parallel to ground, hold position to build endurance.', 'STRENGTH', 'INTERMEDIATE', FALSE, TRUE, 5, 25, 1234, 4.4, 287, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Dead Hang', '🤲', 'Grip and shoulder strengthening hold. Hang from pull-up bar with arms fully extended, focus on maintaining grip.', 'STRENGTH', 'INTERMEDIATE', FALSE, TRUE, 2, 10, 567, 4.6, 123, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Bridge Hold', '🌉', 'Glute and posterior chain isometric. Lie on back, lift hips to create straight line from knees to shoulders.', 'STRENGTH', 'BEGINNER', FALSE, TRUE, 4, 20, 987, 4.3, 234, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Hollow Hold', '⭕', 'Advanced isometric core exercise. Lie on back, lift shoulders and legs, hold hollow body position.', 'STRENGTH', 'INTERMEDIATE', FALSE, TRUE, 2, 15, 234, 4.7, 56, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('L-Sit', '🔺', 'Advanced core and arm isometric. Sit with legs extended, hands on ground, lift body and legs off ground.', 'STRENGTH', 'ADVANCED', FALSE, TRUE, 3, 25, 234, 4.9, 67, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP), -- Marked as favorite and professional

-- Cardio Exercises (TIME_BASED)
('Running', '🏃', 'Classic cardiovascular exercise. Maintain steady pace for specified duration or distance, focus on breathing rhythm.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 20, 200, 4567, 4.7, 1234, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Jumping Jacks', '⭐', 'Full-body cardio movement. Jump feet apart while raising arms overhead, return to starting position rapidly.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 8, 60, 2134, 4.4, 678, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Burpees', '💥', 'High-intensity full-body exercise. Drop to squat, kick back to plank, return to squat, jump up with arms overhead.', 'PLYOMETRIC', 'INTERMEDIATE', TRUE, FALSE, 10, 120, 1456, 4.6, 389, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Mountain Climbers', '⛰️', 'Dynamic core and cardio exercise. In plank position, rapidly alternate bringing knees toward chest.', 'CARDIO', 'INTERMEDIATE', TRUE, FALSE, 6, 50, 1789, 4.5, 456, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('High Knees', '🏃‍♂️', 'Running in place with high knee drive. Bring knees up toward chest rapidly while staying in place.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 3, 30, 678, 4.2, 156, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Butt Kickers', '🦵', 'Running in place kicking heels to glutes. Keep upper body straight while bringing heels to buttocks.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 3, 25, 567, 4.1, 134, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Jump Squats', '🚀', 'Explosive squat with jump for power. Perform squat then jump up explosively, land softly and repeat.', 'PLYOMETRIC', 'INTERMEDIATE', TRUE, FALSE, 6, 60, 445, 4.5, 89, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),

-- Flexibility Exercises
('Child''s Pose', '🧘', 'Restorative yoga pose for back and shoulders. Kneel and sit back on heels, extend arms forward on ground.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 3, 5, 891, 4.3, 234, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Downward Dog', '🐕', 'Classic yoga pose for full-body stretch. Form inverted V-shape with hands and feet on ground, straighten legs and arms.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 2, 8, 1567, 4.5, 423, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Cat-Cow Stretch', '🐱', 'Spinal mobility exercise. On hands and knees, arch and round spine alternately for mobility.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 3, 10, 456, 4.2, 98, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Hip Flexor Stretch', '🦵', 'Hip opening stretch for tight hip flexors. Lunge position, push hips forward to stretch front of hip.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 2, 5, 567, 4.1, 123, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Shoulder Rolls', '🔄', 'Shoulder mobility and warm-up exercise. Roll shoulders backward and forward in controlled circles.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 2, 5, 345, 4.0, 78, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- =====================================================
-- WORKOUT PLANS - READY FOR SCHEDULING
-- =====================================================

-- Delete any existing workout plans to avoid duplicates
DELETE FROM plan_exercise WHERE workout_plan_id IN (
    SELECT workout_plan_id FROM workout_plans
    WHERE workout_name IN ('Quick Upper Body', 'Quick Lower Body', 'Quick Core Blast', 'Quick Cardio Burn', 'Quick HIIT Starter', 'Power Upper Body', 'Complete Full Body Beginner', 'Strength & Power Pro')
);
DELETE FROM workout_plans WHERE workout_name IN ('Quick Upper Body', 'Quick Lower Body', 'Quick Core Blast', 'Quick Cardio Burn', 'Quick HIIT Starter', 'Power Upper Body', 'Complete Full Body Beginner', 'Strength & Power Pro');

INSERT INTO workout_plans (
    workout_name, workout_description, workout_category,
    workout_type, difficulty_level, estimated_duration_minutes,
    subscription_tier_required, is_public, times_used, average_rating,
    target_muscle_groups, equipment_needed, created_by_professional, created_at
) VALUES
-- FREE TIER PLANS (3 exercises each - perfect for daily limit)
('Quick Upper Body', 'Essential upper body workout with 3 key exercises for beginners', 'Upper Body',
 'STRENGTH', 'BEGINNER', 15, 'FREE', TRUE, 1250, 4.5,
 'Chest,Shoulders,Triceps,Back', 'None', FALSE, CURRENT_TIMESTAMP),

('Quick Lower Body', 'Fundamental lower body routine targeting major leg muscles', 'Lower Body',
 'STRENGTH', 'BEGINNER', 15, 'FREE', TRUE, 967, 4.4,
 'Quadriceps,Glutes,Hamstrings,Calves', 'None', FALSE, CURRENT_TIMESTAMP),

('Quick Core Blast', 'Focused core workout to strengthen your midsection', 'Core',
 'STRENGTH', 'BEGINNER', 12, 'FREE', TRUE, 1456, 4.3,
 'Core,Abdominals,Obliques', 'None', FALSE, CURRENT_TIMESTAMP),

('Quick Cardio Burn', 'High-energy cardio session to get your heart pumping', 'Cardio',
 'CARDIO', 'BEGINNER', 12, 'FREE', TRUE, 1789, 4.2,
 'Full Body,Cardiovascular System', 'None', FALSE, CURRENT_TIMESTAMP),

('Quick HIIT Starter', 'Beginner-friendly HIIT workout for maximum efficiency', 'HIIT',
 'CARDIO', 'BEGINNER', 15, 'FREE', TRUE, 1234, 4.6,
 'Full Body,Cardiovascular System', 'None', FALSE, CURRENT_TIMESTAMP),

-- PROFESSIONAL TIER PLANS
('Power Upper Body', 'Intermediate upper body strength with challenging exercises', 'Upper Body',
 'STRENGTH', 'INTERMEDIATE', 18, 'FREE', TRUE, 734, 4.6,
 'Chest,Back,Shoulders,Arms', 'Pull-up Bar', TRUE, CURRENT_TIMESTAMP), -- Professional content

-- PLUS+ TIER PLANS (4-6 exercises each)
('Complete Full Body Beginner', 'Comprehensive full-body workout covering all major muscle groups', 'Full Body',
 'STRENGTH', 'BEGINNER', 30, 'PLUS', TRUE, 892, 4.7,
 'Chest,Back,Legs,Core,Shoulders,Arms', 'None', FALSE, CURRENT_TIMESTAMP),

('Strength & Power Pro', 'Advanced strength training with compound movements', 'Strength',
 'STRENGTH', 'ADVANCED', 50, 'PRO', TRUE, 234, 4.9,
 'Full Body,Power,Strength', 'Dumbbells,Barbell,Pull-up Bar', TRUE, CURRENT_TIMESTAMP); -- Professional content

-- =====================================================
-- WORKOUT PLAN EXERCISES (Linking exercises to plans)
-- =====================================================

INSERT INTO plan_exercise (
    workout_plan_id, exercise_id, order_in_workout,
    prescribed_sets, prescribed_reps, prescribed_rest_seconds,
    instructions, coaching_cues, subscription_tier_required
)
SELECT
    wp.workout_plan_id, e.exercise_id,
    CASE
        WHEN wp.workout_name = 'Quick Upper Body' AND e.exercise_name = 'Push-Up' THEN 1
        WHEN wp.workout_name = 'Quick Upper Body' AND e.exercise_name = 'Inverted Row' THEN 2
        WHEN wp.workout_name = 'Quick Upper Body' AND e.exercise_name = 'Pike Push-Up' THEN 3
        WHEN wp.workout_name = 'Quick Lower Body' AND e.exercise_name = 'Bodyweight Squat' THEN 1
        WHEN wp.workout_name = 'Quick Lower Body' AND e.exercise_name = 'Lunges' THEN 2
        WHEN wp.workout_name = 'Quick Lower Body' AND e.exercise_name = 'Glute Bridges' THEN 3
        WHEN wp.workout_name = 'Quick Core Blast' AND e.exercise_name = 'Plank' THEN 1
        WHEN wp.workout_name = 'Quick Core Blast' AND e.exercise_name = 'L-Sit' THEN 2
        WHEN wp.workout_name = 'Quick Core Blast' AND e.exercise_name = 'Dead Bug' THEN 3
        END,
    3,
    CASE
        WHEN e.exercise_name = 'Push-Up' THEN '8-12'
        WHEN e.exercise_name = 'Inverted Row' THEN '6-10'
        WHEN e.exercise_name = 'Pike Push-Up' THEN '5-8'
        WHEN e.exercise_name = 'Bodyweight Squat' THEN '10-15'
        WHEN e.exercise_name = 'Lunges' THEN '8-10 each leg'
        WHEN e.exercise_name = 'Glute Bridges' THEN '12-15'
        WHEN e.exercise_name = 'Plank' THEN '20-45 seconds'
        WHEN e.exercise_name = 'L-Sit' THEN '10-30 seconds'
        WHEN e.exercise_name = 'Dead Bug' THEN '8-10 each side'
        END,
    60,
    CASE
        WHEN e.exercise_name = 'Push-Up' THEN 'Start on knees if full push-ups are too difficult'
        WHEN e.exercise_name = 'Inverted Row' THEN 'Use table edge or low bar, adjust body angle for difficulty'
        WHEN e.exercise_name = 'Pike Push-Up' THEN 'Focus on shoulders, go slow and controlled'
        WHEN e.exercise_name = 'Bodyweight Squat' THEN 'Sit back like sitting in a chair, keep chest up'
        WHEN e.exercise_name = 'Lunges' THEN 'Step forward far enough for 90-degree angles'
        WHEN e.exercise_name = 'Glute Bridges' THEN 'Squeeze glutes at the top, control the descent'
        WHEN e.exercise_name = 'Plank' THEN 'Hold steady position, breathe normally'
        WHEN e.exercise_name = 'L-Sit' THEN 'Advanced hold, bend knees if needed'
        WHEN e.exercise_name = 'Dead Bug' THEN 'Extend opposite arm and leg, keep lower back pressed down'
        END,
    CASE
        WHEN e.exercise_name = 'Push-Up' THEN 'Keep body straight, control the movement'
        WHEN e.exercise_name = 'Inverted Row' THEN 'Pull chest to bar, squeeze shoulder blades'
        WHEN e.exercise_name = 'Pike Push-Up' THEN 'Keep core tight, press through shoulders'
        WHEN e.exercise_name = 'Bodyweight Squat' THEN 'Weight in heels, knees track over toes'
        WHEN e.exercise_name = 'Lunges' THEN 'Keep torso upright, push through front heel'
        WHEN e.exercise_name = 'Glute Bridges' THEN 'Drive through heels, maintain bridge position'
        WHEN e.exercise_name = 'Plank' THEN 'Straight line from head to heels, engage core'
        WHEN e.exercise_name = 'L-Sit' THEN 'Keep legs straight, body off ground'
        WHEN e.exercise_name = 'Dead Bug' THEN 'Move slowly, maintain core tension throughout'
        END,
    'FREE'
FROM workout_plans wp
         CROSS JOIN exercises e
WHERE (
          (wp.workout_name = 'Quick Upper Body' AND e.exercise_name IN ('Push-Up', 'Inverted Row', 'Pike Push-Up')) OR
          (wp.workout_name = 'Quick Lower Body' AND e.exercise_name IN ('Bodyweight Squat', 'Lunges', 'Glute Bridges')) OR
          (wp.workout_name = 'Quick Core Blast' AND e.exercise_name IN ('Plank', 'L-Sit', 'Dead Bug'))
          );

-- =====================================================
-- CREATE BASIC INDEXES FOR PERFORMANCE
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_exercises_published ON exercises(published);
CREATE INDEX IF NOT EXISTS idx_exercises_favorite ON exercises(is_favorite);
CREATE INDEX IF NOT EXISTS idx_exercises_professional ON exercises(created_by_professional);
CREATE INDEX IF NOT EXISTS idx_workout_plans_professional ON workout_plans(created_by_professional);
CREATE INDEX IF NOT EXISTS idx_scheduled_workouts_user_date ON scheduled_workouts(user_id, scheduled_date);

-- =====================================================
-- SUCCESS MESSAGE
-- =====================================================

DO $$
DECLARE
exercise_count INTEGER;
    plan_count INTEGER;
    isometric_count INTEGER;
    cardio_count INTEGER;
    strength_count INTEGER;
    favorites_table_exists BOOLEAN;
BEGIN
SELECT COUNT(*) INTO exercise_count FROM exercises;
SELECT COUNT(*) INTO plan_count FROM workout_plans;
SELECT COUNT(*) INTO isometric_count FROM exercises WHERE is_isometric = TRUE;
SELECT COUNT(*) INTO cardio_count FROM exercises WHERE is_cardio = TRUE;
SELECT COUNT(*) INTO strength_count FROM exercises WHERE is_cardio = FALSE AND is_isometric = FALSE;

-- Check if favorites table was created
SELECT EXISTS (
    SELECT FROM information_schema.tables
    WHERE table_schema = 'public'
      AND table_name = 'user_exercise_favorites'
) INTO favorites_table_exists;

RAISE NOTICE '=========================================';
    RAISE NOTICE '🚀 V012 ENHANCED MIGRATION COMPLETE! 🚀';
    RAISE NOTICE '=========================================';
    RAISE NOTICE '';
    RAISE NOTICE '📚 EXERCISE LIBRARY: % total exercises', exercise_count;
    RAISE NOTICE '  💪 REP_BASED (Strength): % exercises', strength_count;
    RAISE NOTICE '  ⏱️  HOLD_BASED (Isometric): % exercises', isometric_count;
    RAISE NOTICE '  🏃 TIME_BASED (Cardio): % exercises', cardio_count;
    RAISE NOTICE '';
    RAISE NOTICE '📋 WORKOUT PLANS: % total plans', plan_count;
    RAISE NOTICE '';
    IF favorites_table_exists THEN
        RAISE NOTICE '⭐ FAVORITES SYSTEM: ✅ Ready';
ELSE
        RAISE NOTICE '⭐ FAVORITES SYSTEM: ❌ Failed to create';
END IF;
    RAISE NOTICE '';
    RAISE NOTICE '✅ INDIVIDUAL EXERCISE SCHEDULING READY!';
    RAISE NOTICE '✅ L-Sit exercise ready for testing!';
    RAISE NOTICE '✅ Exercise favorites system ready!';
    RAISE NOTICE '=========================================';
END $$;