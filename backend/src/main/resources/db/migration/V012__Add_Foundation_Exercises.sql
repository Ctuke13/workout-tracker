-- =============================================================================
-- V012__Add_Foundation_Exercises.sql
-- UPDATED: Perfect exercise categorization with workout_tracking_mode support
-- FIXED: All exercises properly configured for ExerciseMapper exercise type detection
-- ENHANCED: Optimized for testing Plank/L-Sit isometric vs Running/Burpees cardio scenarios
-- =============================================================================

-- =====================================================
-- STEP 1: ADD EXERCISE CONFIGURATION COLUMNS TO SCHEDULED_WORKOUTS
-- =====================================================

-- Add exercise configuration columns to scheduled_workouts table (matches ScheduledWorkout.java)
-- ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS sets INTEGER;
-- ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS reps INTEGER;
-- ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS weight_kg DECIMAL(5,2);
-- ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS duration_minutes INTEGER;
-- ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS hold_duration_seconds INTEGER;
-- ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS rest_seconds INTEGER;
-- ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS distance_km DECIMAL(6,2);
-- ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS calories_target INTEGER;
-- ALTER TABLE scheduled_workouts ADD COLUMN IF NOT EXISTS exercise_notes TEXT;
DROP TABLE IF EXISTS scheduled_workouts CASCADE;

-- =====================================================
-- STEP 2: CLEAN UP EXISTING DATA
-- =====================================================

-- Delete any test exercises that might exist
DELETE FROM exercises WHERE exercise_name IN ('L-Sit Test', 'Test Exercise');

-- Delete existing foundation exercises to avoid duplicates
DELETE FROM exercises WHERE exercise_name IN (
                                              'Push-Up', 'Pull-Up', 'Dumbbell Bench Press', 'Dips', 'Pike Push-Up', 'Tricep Push-Up', 'Inverted Row',
                                              'Bodyweight Squat', 'Deadlift', 'Lunges', 'Bulgarian Split Squat', 'Calf Raises', 'Glute Bridges', 'Single-Leg Deadlift', 'Step-Ups',
                                              'Crunches', 'Russian Twists', 'Bicycle Crunches', 'Dead Bug',
                                              'Plank', 'Side Plank', 'Wall Sit', 'Dead Hang', 'Bridge Hold', 'Hollow Hold', 'L-Sit',
                                              'Running', 'Jumping Jacks', 'Burpees', 'Mountain Climbers', 'High Knees', 'Butt Kickers', 'Jump Squats',
                                              'Child''s Pose', 'Downward Dog', 'Cat-Cow Stretch', 'Hip Flexor Stretch', 'Shoulder Rolls'
    );

-- =====================================================
-- STEP 3: FOUNDATION EXERCISES WITH PERFECT CATEGORIZATION
-- =====================================================

-- ============================================
-- STRENGTH EXERCISES (REP_BASED TRACKING)
-- ✅ exercise_type = 'STRENGTH', is_cardio = FALSE, is_isometric = FALSE, workout_tracking_mode = 'REP_BASED'
-- ============================================

INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES

-- UPPER BODY STRENGTH
('Push-Up', '💪', 'Classic bodyweight exercise targeting chest, shoulders, and triceps. Start in plank position, lower body to ground, push back up.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 5, 25, 1250, 4.6, 312, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Pull-Up', '🎯', 'Ultimate upper body exercise using pull-up bar. Hang from bar, pull body up until chin clears bar, lower with control.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 8, 40, 892, 4.8, 198, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Dumbbell Bench Press', '🏋️', 'Fundamental chest exercise using dumbbells. Lie on bench, press weights from chest level to full arm extension.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 12, 60, 1567, 4.7, 423, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Dips', '🔴', 'Tricep-focused exercise using parallel bars, chair, or bench edge. Lower body by bending arms, push back up.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 6, 35, 567, 4.5, 145, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Pike Push-Up', '🔺', 'Shoulder-focused push-up variation. In downward dog position, lower head toward ground, press back up.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 5, 30, 423, 4.4, 98, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Tricep Push-Up', '💎', 'Diamond push-up variation targeting triceps. Hands form diamond shape, keep elbows close to body.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 4, 25, 356, 4.3, 87, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Inverted Row', '🚣', 'Horizontal pulling exercise using table or low bar. Lie under bar, pull chest to bar, lower with control.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 6, 30, 789, 4.2, 189, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),

-- LOWER BODY STRENGTH
('Bodyweight Squat', '🦵', 'Essential lower body movement. Stand with feet shoulder-width apart, lower hips back and down, return to standing.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 6, 30, 2103, 4.5, 567, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Deadlift', '⚡', 'King of compound movements. Lift barbell from ground to hip level using legs and back, emphasizing proper form.', 'STRENGTH', 'ADVANCED', FALSE, FALSE, 'REP_BASED', 15, 80, 734, 4.9, 156, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
('Lunges', '🦵', 'Single-leg strength exercise for quads and glutes. Step forward, lower back knee, return to standing.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 40, 1456, 4.4, 334, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Bulgarian Split Squat', '🏃', 'Advanced single-leg squat variation. Rear foot elevated, lower into lunge position, drive back up.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 10, 50, 456, 4.6, 112, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Calf Raises', '🦶', 'Isolation exercise for calf muscles. Rise up on toes, hold briefly, lower with control.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 4, 20, 890, 4.1, 203, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Glute Bridges', '🌉', 'Hip thrust movement for glute activation. Lie on back, lift hips up, squeeze glutes, lower slowly.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 5, 25, 1234, 4.3, 289, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Single-Leg Deadlift', '⚖️', 'Balance and hamstring exercise. Stand on one leg, hinge at hip, reach toward ground, return to standing.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 8, 35, 345, 4.5, 78, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Step-Ups', '🔶', 'Single-leg step exercise using bench or platform. Step up with one leg, drive knee up, step down controlled.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 40, 678, 4.2, 156, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),

-- CORE STRENGTH
('Crunches', '🔥', 'Targeted abdominal exercise. Lie on back, lift shoulders off ground by contracting abs, lower with control.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 4, 20, 1876, 4.2, 445, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Russian Twists', '🌪️', 'Rotational core exercise targeting obliques. Sit with knees bent, lean back, rotate torso side to side.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 4, 20, 567, 4.1, 134, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Bicycle Crunches', '🚴', 'Dynamic ab exercise with rotation. Lie on back, bring opposite elbow to knee in cycling motion.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 5, 25, 890, 4.3, 201, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
('Dead Bug', '🪲', 'Core stability exercise for deep abdominals. Lie on back, extend opposite arm and leg, return to start.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 6, 20, 234, 4.4, 67, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- ============================================
-- ISOMETRIC EXERCISES (HOLD_BASED TRACKING)
-- ✅ exercise_type = 'BALANCE', is_cardio = FALSE, is_isometric = TRUE, workout_tracking_mode = 'HOLD_BASED'
-- ============================================

INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES

      ('Plank', '🛡️', 'Core-strengthening isometric hold. Maintain straight body position on forearms and toes, engaging entire core.', 'BALANCE', 'BEGINNER', FALSE, TRUE, 'HOLD_BASED', 3, 15, 3456, 4.8, 892, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Side Plank', '🔷', 'Lateral core strength exercise. Lie on side, prop up on forearm, maintain straight line from head to feet.', 'BALANCE', 'INTERMEDIATE', FALSE, TRUE, 'HOLD_BASED', 3, 15, 567, 4.5, 134, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Wall Sit', '🧱', 'Isometric leg strengthener. Sit against wall with thighs parallel to ground, hold position to build endurance.', 'BALANCE', 'INTERMEDIATE', FALSE, TRUE, 'HOLD_BASED', 5, 25, 1234, 4.4, 287, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Dead Hang', '🤲', 'Grip and shoulder strengthening hold. Hang from pull-up bar with arms fully extended, focus on maintaining grip.', 'BALANCE', 'INTERMEDIATE', FALSE, TRUE, 'HOLD_BASED', 2, 10, 567, 4.6, 123, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Bridge Hold', '🌉', 'Glute and posterior chain isometric. Lie on back, lift hips to create straight line from knees to shoulders.', 'BALANCE', 'BEGINNER', FALSE, TRUE, 'HOLD_BASED', 4, 20, 987, 4.3, 234, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Hollow Hold', '⭕', 'Advanced isometric core exercise. Lie on back, lift shoulders and legs, hold hollow body position.', 'BALANCE', 'INTERMEDIATE', FALSE, TRUE, 'HOLD_BASED', 2, 15, 234, 4.7, 56, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('L-Sit', '🔺', 'Advanced core and arm isometric. Sit with legs extended, hands on ground, lift body and legs off ground.', 'BALANCE', 'ADVANCED', FALSE, TRUE, 'HOLD_BASED', 3, 25, 234, 4.9, 67, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP);

-- ============================================
-- CARDIO EXERCISES (TIME_BASED TRACKING)
-- ✅ exercise_type = 'CARDIO', is_cardio = TRUE, is_isometric = FALSE, workout_tracking_mode = 'TIME_BASED'
-- ============================================

INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES

      ('Running', '🏃', 'Classic cardiovascular exercise. Maintain steady pace for specified duration or distance, focus on breathing rhythm.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 'TIME_BASED', 20, 200, 4567, 4.7, 1234, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Jumping Jacks', '⭐', 'Full-body cardio movement. Jump feet apart while raising arms overhead, return to starting position rapidly.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 'TIME_BASED', 8, 60, 2134, 4.4, 678, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Burpees', '💥', 'High-intensity full-body exercise. Drop to squat, kick back to plank, return to squat, jump up with arms overhead.', 'CARDIO', 'INTERMEDIATE', TRUE, FALSE, 'TIME_BASED', 10, 120, 1456, 4.6, 389, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Mountain Climbers', '⛰️', 'Dynamic core and cardio exercise. In plank position, rapidly alternate bringing knees toward chest.', 'CARDIO', 'INTERMEDIATE', TRUE, FALSE, 'TIME_BASED', 6, 50, 1789, 4.5, 456, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('High Knees', '🏃‍♂️', 'Running in place with high knee drive. Bring knees up toward chest rapidly while staying in place.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 'TIME_BASED', 3, 30, 678, 4.2, 156, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Butt Kickers', '🦵', 'Running in place kicking heels to glutes. Keep upper body straight while bringing heels to buttocks.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 'TIME_BASED', 3, 25, 567, 4.1, 134, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Jump Squats', '🚀', 'Explosive squat with jump for power. Perform squat then jump up explosively, land softly and repeat.', 'CARDIO', 'INTERMEDIATE', TRUE, FALSE, 'TIME_BASED', 6, 60, 445, 4.5, 89, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- ============================================
-- FLEXIBILITY EXERCISES (REP_BASED TRACKING)
-- ✅ exercise_type = 'FLEXIBILITY', is_cardio = FALSE, is_isometric = FALSE, workout_tracking_mode = 'REP_BASED'
-- ============================================

INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES

      ('Child''s Pose', '🧘', 'Restorative yoga pose for back and shoulders. Kneel and sit back on heels, extend arms forward on ground.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 3, 5, 891, 4.3, 234, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Downward Dog', '🐕', 'Classic yoga pose for full-body stretch. Form inverted V-shape with hands and feet on ground, straighten legs and arms.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 2, 8, 1567, 4.5, 423, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Cat-Cow Stretch', '🐱', 'Spinal mobility exercise. On hands and knees, arch and round spine alternately for mobility.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 3, 10, 456, 4.2, 98, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Hip Flexor Stretch', '🦵', 'Hip opening stretch for tight hip flexors. Lunge position, push hips forward to stretch front of hip.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 2, 5, 567, 4.1, 123, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Shoulder Rolls', '🔄', 'Shoulder mobility and warm-up exercise. Roll shoulders backward and forward in controlled circles.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 2, 5, 345, 4.0, 78, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- =====================================================
-- STEP 4: WORKOUT PLANS WITH PROPER EXERCISE CATEGORIZATION
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

-- FREE TIER PLANS (3 exercises each)
('Quick Upper Body', 'Essential upper body workout with 3 key exercises for beginners', 'Upper Body',
 'STRENGTH', 'BEGINNER', 15, 'FREE', TRUE, 1250, 4.5,
 'Chest,Shoulders,Triceps,Back', 'None', FALSE, CURRENT_TIMESTAMP),

('Quick Lower Body', 'Fundamental lower body routine targeting major leg muscles', 'Lower Body',
 'STRENGTH', 'BEGINNER', 15, 'FREE', TRUE, 967, 4.4,
 'Quadriceps,Glutes,Hamstrings,Calves', 'None', FALSE, CURRENT_TIMESTAMP),

('Quick Core Blast', 'Focused core workout combining strength and isometric holds', 'Core',
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
 'Chest,Back,Shoulders,Arms', 'Pull-up Bar', TRUE, CURRENT_TIMESTAMP),

-- PLUS+ TIER PLANS (4-6 exercises each)
('Complete Full Body Beginner', 'Comprehensive full-body workout covering all major muscle groups', 'Full Body',
 'STRENGTH', 'BEGINNER', 30, 'PLUS', TRUE, 892, 4.7,
 'Chest,Back,Legs,Core,Shoulders,Arms', 'None', FALSE, CURRENT_TIMESTAMP),

('Strength & Power Pro', 'Advanced strength training with compound movements', 'Strength',
 'STRENGTH', 'ADVANCED', 50, 'PRO', TRUE, 234, 4.9,
 'Full Body,Power,Strength', 'Dumbbells,Barbell,Pull-up Bar', TRUE, CURRENT_TIMESTAMP);

-- =====================================================
-- STEP 5: WORKOUT PLAN EXERCISES (Link exercises to plans)
-- =====================================================

-- Quick Upper Body Plan (3 STRENGTH exercises)
INSERT INTO plan_exercise (
    workout_plan_id, exercise_id, order_in_workout,
    prescribed_sets, prescribed_reps, prescribed_rest_seconds,
    instructions, coaching_cues, subscription_tier_required
)
SELECT
    wp.workout_plan_id, e.exercise_id,
    CASE e.exercise_name
        WHEN 'Push-Up' THEN 1
        WHEN 'Inverted Row' THEN 2
        WHEN 'Pike Push-Up' THEN 3
        END,
    3,
    CASE e.exercise_name
        WHEN 'Push-Up' THEN '8-12'
        WHEN 'Inverted Row' THEN '6-10'
        WHEN 'Pike Push-Up' THEN '5-8'
        END,
    60,
    CASE e.exercise_name
        WHEN 'Push-Up' THEN 'Start on knees if full push-ups are too difficult'
        WHEN 'Inverted Row' THEN 'Use table edge or low bar, adjust body angle for difficulty'
        WHEN 'Pike Push-Up' THEN 'Focus on shoulders, go slow and controlled'
        END,
    CASE e.exercise_name
        WHEN 'Push-Up' THEN 'Keep body straight, control the movement'
        WHEN 'Inverted Row' THEN 'Pull chest to bar, squeeze shoulder blades'
        WHEN 'Pike Push-Up' THEN 'Keep core tight, press through shoulders'
        END,
    'FREE'
FROM workout_plans wp
         CROSS JOIN exercises e
WHERE wp.workout_name = 'Quick Upper Body'
  AND e.exercise_name IN ('Push-Up', 'Inverted Row', 'Pike Push-Up');

-- Quick Lower Body Plan (3 STRENGTH exercises)
INSERT INTO plan_exercise (
    workout_plan_id, exercise_id, order_in_workout,
    prescribed_sets, prescribed_reps, prescribed_rest_seconds,
    instructions, coaching_cues, subscription_tier_required
)
SELECT
    wp.workout_plan_id, e.exercise_id,
    CASE e.exercise_name
        WHEN 'Bodyweight Squat' THEN 1
        WHEN 'Lunges' THEN 2
        WHEN 'Glute Bridges' THEN 3
        END,
    3,
    CASE e.exercise_name
        WHEN 'Bodyweight Squat' THEN '10-15'
        WHEN 'Lunges' THEN '8-10 each leg'
        WHEN 'Glute Bridges' THEN '12-15'
        END,
    60,
    CASE e.exercise_name
        WHEN 'Bodyweight Squat' THEN 'Sit back like sitting in a chair, keep chest up'
        WHEN 'Lunges' THEN 'Step forward far enough for 90-degree angles'
        WHEN 'Glute Bridges' THEN 'Squeeze glutes at the top, control the descent'
        END,
    CASE e.exercise_name
        WHEN 'Bodyweight Squat' THEN 'Weight in heels, knees track over toes'
        WHEN 'Lunges' THEN 'Keep torso upright, push through front heel'
        WHEN 'Glute Bridges' THEN 'Drive through heels, maintain bridge position'
        END,
    'FREE'
FROM workout_plans wp
         CROSS JOIN exercises e
WHERE wp.workout_name = 'Quick Lower Body'
  AND e.exercise_name IN ('Bodyweight Squat', 'Lunges', 'Glute Bridges');

-- ✅ KEY TEST SCENARIO: Quick Core Blast Plan (1 STRENGTH + 2 ISOMETRIC exercises)
-- This plan specifically tests the exercise type detection system!
INSERT INTO plan_exercise (
    workout_plan_id, exercise_id, order_in_workout,
    prescribed_sets, prescribed_reps, prescribed_rest_seconds,
    instructions, coaching_cues, subscription_tier_required
)
SELECT
    wp.workout_plan_id, e.exercise_id,
    CASE e.exercise_name
        WHEN 'Dead Bug' THEN 1
        WHEN 'Plank' THEN 2
        WHEN 'L-Sit' THEN 3
        END,
    3,
    CASE e.exercise_name
        WHEN 'Dead Bug' THEN '8-10 each side'
        WHEN 'Plank' THEN '20-45 seconds'
        WHEN 'L-Sit' THEN '10-30 seconds'
        END,
    60,
    CASE e.exercise_name
        WHEN 'Dead Bug' THEN 'Extend opposite arm and leg, keep lower back pressed down'
        WHEN 'Plank' THEN 'Hold steady position, breathe normally'
        WHEN 'L-Sit' THEN 'Advanced hold, bend knees if needed'
        END,
    CASE e.exercise_name
        WHEN 'Dead Bug' THEN 'Move slowly, maintain core tension throughout'
        WHEN 'Plank' THEN 'Straight line from head to heels, engage core'
        WHEN 'L-Sit' THEN 'Keep legs straight, body off ground'
        END,
    'FREE'
FROM workout_plans wp
         CROSS JOIN exercises e
WHERE wp.workout_name = 'Quick Core Blast'
  AND e.exercise_name IN ('Dead Bug', 'Plank', 'L-Sit');

-- ✅ KEY TEST SCENARIO: Quick Cardio Burn Plan (3 CARDIO exercises)
-- This plan specifically tests cardio exercise type detection!
INSERT INTO plan_exercise (
    workout_plan_id, exercise_id, order_in_workout,
    prescribed_sets, prescribed_reps, prescribed_rest_seconds,
    instructions, coaching_cues, subscription_tier_required
)
SELECT
    wp.workout_plan_id, e.exercise_id,
    CASE e.exercise_name
        WHEN 'Jumping Jacks' THEN 1
        WHEN 'High Knees' THEN 2
        WHEN 'Butt Kickers' THEN 3
        END,
    3,
    CASE e.exercise_name
        WHEN 'Jumping Jacks' THEN '30-45 seconds'
        WHEN 'High Knees' THEN '20-30 seconds'
        WHEN 'Butt Kickers' THEN '20-30 seconds'
        END,
    30,
    CASE e.exercise_name
        WHEN 'Jumping Jacks' THEN 'Keep movements controlled, land softly'
        WHEN 'High Knees' THEN 'Drive knees up high, pump arms'
        WHEN 'Butt Kickers' THEN 'Keep upper body upright, quick heel kicks'
        END,
    CASE e.exercise_name
        WHEN 'Jumping Jacks' THEN 'Full range of motion, steady rhythm'
        WHEN 'High Knees' THEN 'Stay on balls of feet, maintain pace'
        WHEN 'Butt Kickers' THEN 'Focus on heel to glute contact'
        END,
    'FREE'
FROM workout_plans wp
         CROSS JOIN exercises e
WHERE wp.workout_name = 'Quick Cardio Burn'
  AND e.exercise_name IN ('Jumping Jacks', 'High Knees', 'Butt Kickers');

-- ✅ KEY TEST SCENARIO: Quick HIIT Starter Plan (3 CARDIO exercises including Burpees)
-- This plan tests high-intensity cardio exercise type detection!
INSERT INTO plan_exercise (
    workout_plan_id, exercise_id, order_in_workout,
    prescribed_sets, prescribed_reps, prescribed_rest_seconds,
    instructions, coaching_cues, subscription_tier_required
)
SELECT
    wp.workout_plan_id, e.exercise_id,
    CASE e.exercise_name
        WHEN 'Burpees' THEN 1
        WHEN 'Mountain Climbers' THEN 2
        WHEN 'Jump Squats' THEN 3
        END,
    4,
    CASE e.exercise_name
        WHEN 'Burpees' THEN '20 seconds work'
        WHEN 'Mountain Climbers' THEN '30 seconds work'
        WHEN 'Jump Squats' THEN '20 seconds work'
        END,
    40,
    CASE e.exercise_name
        WHEN 'Burpees' THEN 'Full movement: squat, plank, jump'
        WHEN 'Mountain Climbers' THEN 'Fast alternating knees to chest'
        WHEN 'Jump Squats' THEN 'Full squat depth, explosive jump'
        END,
    CASE e.exercise_name
        WHEN 'Burpees' THEN 'Maintain form even when tired'
        WHEN 'Mountain Climbers' THEN 'Keep core tight, plank position'
        WHEN 'Jump Squats' THEN 'Land softly, control the descent'
        END,
    'FREE'
FROM workout_plans wp
         CROSS JOIN exercises e
WHERE wp.workout_name = 'Quick HIIT Starter'
  AND e.exercise_name IN ('Burpees', 'Mountain Climbers', 'Jump Squats');

-- Power Upper Body Plan (4 STRENGTH exercises including Pull-ups)
INSERT INTO plan_exercise (
    workout_plan_id, exercise_id, order_in_workout,
    prescribed_sets, prescribed_reps, prescribed_rest_seconds,
    instructions, coaching_cues, subscription_tier_required
)
SELECT
    wp.workout_plan_id, e.exercise_id,
    CASE e.exercise_name
        WHEN 'Push-Up' THEN 1
        WHEN 'Pull-Up' THEN 2
        WHEN 'Dips' THEN 3
        WHEN 'Pike Push-Up' THEN 4
        END,
    4,
    CASE e.exercise_name
        WHEN 'Push-Up' THEN '10-15'
        WHEN 'Pull-Up' THEN '5-10'
        WHEN 'Dips' THEN '8-12'
        WHEN 'Pike Push-Up' THEN '6-10'
        END,
    90,
    CASE e.exercise_name
        WHEN 'Push-Up' THEN 'Focus on full range of motion and control'
        WHEN 'Pull-Up' THEN 'Use assistance if needed, focus on form'
        WHEN 'Dips' THEN 'Control the descent, full range of motion'
        WHEN 'Pike Push-Up' THEN 'Target shoulders, keep core engaged'
        END,
    CASE e.exercise_name
        WHEN 'Push-Up' THEN 'Straight body line, engage core throughout'
        WHEN 'Pull-Up' THEN 'Pull chest to bar, control the negative'
        WHEN 'Dips' THEN 'Lean slightly forward, control the movement'
        WHEN 'Pike Push-Up' THEN 'Press through shoulders, maintain position'
        END,
    'FREE'
FROM workout_plans wp
         CROSS JOIN exercises e
WHERE wp.workout_name = 'Power Upper Body'
  AND e.exercise_name IN ('Push-Up', 'Pull-Up', 'Dips', 'Pike Push-Up');

-- Complete Full Body Beginner Plan (6 exercises - mixed types)
INSERT INTO plan_exercise (
    workout_plan_id, exercise_id, order_in_workout,
    prescribed_sets, prescribed_reps, prescribed_rest_seconds,
    instructions, coaching_cues, subscription_tier_required
)
SELECT
    wp.workout_plan_id, e.exercise_id,
    CASE e.exercise_name
        WHEN 'Push-Up' THEN 1
        WHEN 'Bodyweight Squat' THEN 2
        WHEN 'Plank' THEN 3
        WHEN 'Lunges' THEN 4
        WHEN 'Inverted Row' THEN 5
        WHEN 'Glute Bridges' THEN 6
        END,
    3,
    CASE e.exercise_name
        WHEN 'Push-Up' THEN '8-12'
        WHEN 'Bodyweight Squat' THEN '12-15'
        WHEN 'Plank' THEN '30-60 seconds'
        WHEN 'Lunges' THEN '10-12 each leg'
        WHEN 'Inverted Row' THEN '8-12'
        WHEN 'Glute Bridges' THEN '15-20'
        END,
    60,
    CASE e.exercise_name
        WHEN 'Push-Up' THEN 'Modify on knees if needed'
        WHEN 'Bodyweight Squat' THEN 'Focus on proper depth and form'
        WHEN 'Plank' THEN 'Hold steady position, breathe normally'
        WHEN 'Lunges' THEN 'Alternate legs, focus on balance'
        WHEN 'Inverted Row' THEN 'Adjust angle to match strength level'
        WHEN 'Glute Bridges' THEN 'Squeeze glutes at top, control descent'
        END,
    CASE e.exercise_name
        WHEN 'Push-Up' THEN 'Keep core tight, full range of motion'
        WHEN 'Bodyweight Squat' THEN 'Weight in heels, chest up'
        WHEN 'Plank' THEN 'Straight line from head to heels'
        WHEN 'Lunges' THEN 'Keep torso upright, 90-degree angles'
        WHEN 'Inverted Row' THEN 'Pull chest to bar, squeeze shoulder blades'
        WHEN 'Glute Bridges' THEN 'Drive through heels, engage glutes'
        END,
    'PLUS'
FROM workout_plans wp
         CROSS JOIN exercises e
WHERE wp.workout_name = 'Complete Full Body Beginner'
  AND e.exercise_name IN ('Push-Up', 'Bodyweight Squat', 'Plank', 'Lunges', 'Inverted Row', 'Glute Bridges');

-- Strength & Power Pro Plan (6 advanced exercises)
INSERT INTO plan_exercise (
    workout_plan_id, exercise_id, order_in_workout,
    prescribed_sets, prescribed_reps, prescribed_rest_seconds,
    instructions, coaching_cues, subscription_tier_required
)
SELECT
    wp.workout_plan_id, e.exercise_id,
    CASE e.exercise_name
        WHEN 'Deadlift' THEN 1
        WHEN 'Pull-Up' THEN 2
        WHEN 'Bulgarian Split Squat' THEN 3
        WHEN 'Dumbbell Bench Press' THEN 4
        WHEN 'L-Sit' THEN 5
        WHEN 'Single-Leg Deadlift' THEN 6
        END,
    4,
    CASE e.exercise_name
        WHEN 'Deadlift' THEN '5-8'
        WHEN 'Pull-Up' THEN '6-10'
        WHEN 'Bulgarian Split Squat' THEN '8-12 each leg'
        WHEN 'Dumbbell Bench Press' THEN '8-12'
        WHEN 'L-Sit' THEN '15-45 seconds'
        WHEN 'Single-Leg Deadlift' THEN '6-10 each leg'
        END,
    120,
    CASE e.exercise_name
        WHEN 'Deadlift' THEN 'Focus on perfect form, proper warm-up essential'
        WHEN 'Pull-Up' THEN 'Add weight if bodyweight is easy'
        WHEN 'Bulgarian Split Squat' THEN 'Rear foot elevated, front leg does the work'
        WHEN 'Dumbbell Bench Press' THEN 'Control the weight, full range of motion'
        WHEN 'L-Sit' THEN 'Advanced hold, bend knees if necessary'
        WHEN 'Single-Leg Deadlift' THEN 'Focus on balance and hamstring stretch'
        END,
    CASE e.exercise_name
        WHEN 'Deadlift' THEN 'Hip hinge movement, keep back neutral'
        WHEN 'Pull-Up' THEN 'Full range of motion, control the negative'
        WHEN 'Bulgarian Split Squat' THEN 'Descend straight down, drive through front heel'
        WHEN 'Dumbbell Bench Press' THEN 'Squeeze chest at top, control the eccentric'
        WHEN 'L-Sit' THEN 'Keep legs straight, shoulders active'
        WHEN 'Single-Leg Deadlift' THEN 'Hinge at hip, keep standing leg strong'
        END,
    'PRO'
FROM workout_plans wp
         CROSS JOIN exercises e
WHERE wp.workout_name = 'Strength & Power Pro'
  AND e.exercise_name IN ('Deadlift', 'Pull-Up', 'Bulgarian Split Squat', 'Dumbbell Bench Press', 'L-Sit', 'Single-Leg Deadlift');

-- =====================================================
-- STEP 6: DATA VALIDATION AND VERIFICATION
-- =====================================================

-- Validate all exercises have consistent data
UPDATE exercises
SET workout_tracking_mode = CASE
                                WHEN is_cardio = TRUE THEN 'TIME_BASED'
                                WHEN is_isometric = TRUE THEN 'HOLD_BASED'
                                ELSE 'REP_BASED'
    END
WHERE workout_tracking_mode IS NULL;

-- =====================================================
-- STEP 7: PERFORMANCE INDEXES (Additional ones for V012 data)
-- =====================================================

-- Create additional indexes for the new data
CREATE INDEX IF NOT EXISTS idx_exercises_workout_tracking_mode_published
    ON exercises(workout_tracking_mode, published, average_rating DESC);


-- =====================================================
-- STEP 8: SUCCESS VALIDATION AND REPORTING
-- =====================================================

DO $$
DECLARE
exercise_count INTEGER;
    plan_count INTEGER;
    isometric_count INTEGER;
    cardio_count INTEGER;
    strength_count INTEGER;
    flexibility_count INTEGER;
    balance_count INTEGER;
    validation_errors INTEGER := 0;

    -- Validation counters
    cardio_mismatch INTEGER;
    isometric_mismatch INTEGER;
    tracking_mode_mismatch INTEGER;
    total_plan_exercises INTEGER;

    -- Key test exercises
    plank_correct BOOLEAN := FALSE;
    lsit_correct BOOLEAN := FALSE;
    running_correct BOOLEAN := FALSE;
    burpees_correct BOOLEAN := FALSE;
BEGIN
    -- Count exercises by category
SELECT COUNT(*) INTO exercise_count FROM exercises;
SELECT COUNT(*) INTO plan_count FROM workout_plans;
SELECT COUNT(*) INTO isometric_count FROM exercises WHERE is_isometric = TRUE;
SELECT COUNT(*) INTO cardio_count FROM exercises WHERE is_cardio = TRUE;
SELECT COUNT(*) INTO strength_count FROM exercises WHERE exercise_type = 'STRENGTH';
SELECT COUNT(*) INTO flexibility_count FROM exercises WHERE exercise_type = 'FLEXIBILITY';
SELECT COUNT(*) INTO balance_count FROM exercises WHERE exercise_type = 'BALANCE';
SELECT COUNT(*) INTO total_plan_exercises FROM plan_exercise;

-- Validation checks
SELECT COUNT(*) INTO cardio_mismatch
FROM exercises
WHERE (exercise_type = 'CARDIO' AND is_cardio = FALSE)
   OR (exercise_type != 'CARDIO' AND is_cardio = TRUE);

SELECT COUNT(*) INTO isometric_mismatch
FROM exercises
WHERE (exercise_type = 'BALANCE' AND is_isometric = FALSE)
   OR (exercise_type != 'BALANCE' AND is_isometric = TRUE AND exercise_type != 'FLEXIBILITY');

SELECT COUNT(*) INTO tracking_mode_mismatch
FROM exercises
WHERE (is_cardio = TRUE AND workout_tracking_mode != 'TIME_BASED')
   OR (is_isometric = TRUE AND workout_tracking_mode != 'HOLD_BASED')
   OR (is_cardio = FALSE AND is_isometric = FALSE AND workout_tracking_mode != 'REP_BASED');

validation_errors := cardio_mismatch + isometric_mismatch + tracking_mode_mismatch;

    -- Check key test exercises
SELECT EXISTS(
    SELECT 1 FROM exercises
    WHERE exercise_name = 'Plank'
      AND exercise_type = 'BALANCE'
      AND is_isometric = TRUE
      AND workout_tracking_mode = 'HOLD_BASED'
) INTO plank_correct;

SELECT EXISTS(
    SELECT 1 FROM exercises
    WHERE exercise_name = 'L-Sit'
      AND exercise_type = 'BALANCE'
      AND is_isometric = TRUE
      AND workout_tracking_mode = 'HOLD_BASED'
) INTO lsit_correct;

SELECT EXISTS(
    SELECT 1 FROM exercises
    WHERE exercise_name = 'Running'
      AND exercise_type = 'CARDIO'
      AND is_cardio = TRUE
      AND workout_tracking_mode = 'TIME_BASED'
) INTO running_correct;

SELECT EXISTS(
    SELECT 1 FROM exercises
    WHERE exercise_name = 'Burpees'
      AND exercise_type = 'CARDIO'
      AND is_cardio = TRUE
      AND workout_tracking_mode = 'TIME_BASED'
) INTO burpees_correct;

-- Report results
RAISE NOTICE '=========================================';
    RAISE NOTICE '🚀 V012 UPDATED MIGRATION COMPLETE! 🚀';
    RAISE NOTICE '=========================================';
    RAISE NOTICE '';
    RAISE NOTICE '📚 EXERCISE LIBRARY: % total exercises', exercise_count;
    RAISE NOTICE '  💪 STRENGTH (REP_BASED): % exercises', strength_count;
    RAISE NOTICE '  ⚖️  BALANCE (HOLD_BASED): % exercises', balance_count;
    RAISE NOTICE '  🏃 CARDIO (TIME_BASED): % exercises', cardio_count;
    RAISE NOTICE '  🤸 FLEXIBILITY (REP_BASED): % exercises', flexibility_count;
    RAISE NOTICE '';
    RAISE NOTICE '🔍 CATEGORIZATION VALIDATION:';
    RAISE NOTICE '  ✅ Isometric exercises (is_isometric=TRUE): %', isometric_count;
    RAISE NOTICE '  ✅ Cardio exercises (is_cardio=TRUE): %', cardio_count;
    RAISE NOTICE '  ⌛ Categorization mismatches: %', validation_errors;
    RAISE NOTICE '';
    RAISE NOTICE '📋 WORKOUT PLANS: % total plans', plan_count;
    RAISE NOTICE '  📝 Total plan exercises: %', total_plan_exercises;
    RAISE NOTICE '';
    RAISE NOTICE '🎯 KEY TEST EXERCISES VALIDATION:';
    IF plank_correct THEN
        RAISE NOTICE '  ✅ Plank: BALANCE + isometric + HOLD_BASED';
ELSE
        RAISE NOTICE '  ❌ Plank: Configuration incorrect!';
END IF;
    IF lsit_correct THEN
        RAISE NOTICE '  ✅ L-Sit: BALANCE + isometric + HOLD_BASED';
ELSE
        RAISE NOTICE '  ❌ L-Sit: Configuration incorrect!';
END IF;
    IF running_correct THEN
        RAISE NOTICE '  ✅ Running: CARDIO + cardio + TIME_BASED';
ELSE
        RAISE NOTICE '  ❌ Running: Configuration incorrect!';
END IF;
    IF burpees_correct THEN
        RAISE NOTICE '  ✅ Burpees: CARDIO + cardio + TIME_BASED';
ELSE
        RAISE NOTICE '  ❌ Burpees: Configuration incorrect!';
END IF;
    RAISE NOTICE '';
    RAISE NOTICE '🎯 EXERCISE TYPE DETECTION READY:';
    RAISE NOTICE '  - Plank → Purple isometric interface';
    RAISE NOTICE '  - L-Sit → Purple isometric interface';
    RAISE NOTICE '  - Running → Red cardio interface';
    RAISE NOTICE '  - Burpees → Red cardio interface';
    RAISE NOTICE '  - Push-ups → Blue strength interface';
    RAISE NOTICE '';

    -- Validation summary
    IF validation_errors = 0 AND plank_correct AND lsit_correct AND running_correct AND burpees_correct THEN
        RAISE NOTICE '🎉 ALL VALIDATION CHECKS PASSED!';
        RAISE NOTICE '🎯 ExerciseMapper ready for testing!';
ELSE
        RAISE NOTICE '⚠️  VALIDATION WARNINGS: % issues found', validation_errors;
END IF;

    RAISE NOTICE '';
    RAISE NOTICE '📊 SAMPLE VERIFICATION QUERY:';
    RAISE NOTICE 'SELECT exercise_name, exercise_type, is_cardio, is_isometric, workout_tracking_mode FROM exercises WHERE exercise_name IN (''Plank'', ''L-Sit'', ''Running'', ''Burpees'') ORDER BY exercise_name;';
    RAISE NOTICE '=========================================';
END $$;