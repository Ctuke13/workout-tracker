-- =============================================================================
-- V012__Add_Foundation_Exercises.sql
-- Adds comprehensive core exercise library with proper isIsometric support
-- Covers all workout tracking modes: REP_BASED, HOLD_BASED, TIME_BASED
-- =============================================================================

-- =====================================================
-- STRENGTH EXERCISES (REP_BASED) - Traditional Sets/Reps
-- =====================================================

-- Upper Body Strength
INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, created_at
) VALUES
      ('Push-Up', '💪', 'Classic bodyweight exercise targeting chest, shoulders, and triceps. Start in plank position, lower body to ground, push back up.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 5, 25, 1250, 4.6, 312, TRUE, CURRENT_TIMESTAMP),

      ('Pull-Up', '🎯', 'Ultimate upper body exercise using pull-up bar. Hang from bar, pull body up until chin clears bar, lower with control.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 8, 40, 892, 4.8, 198, TRUE, CURRENT_TIMESTAMP),

      ('Dumbbell Bench Press', '🏋️', 'Fundamental chest exercise using dumbbells. Lie on bench, press weights from chest level to full arm extension.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 12, 60, 1567, 4.7, 423, TRUE, CURRENT_TIMESTAMP),

      ('Bodyweight Squat', '🦵', 'Essential lower body movement. Stand with feet shoulder-width apart, lower hips back and down, return to standing.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 6, 30, 2103, 4.5, 567, TRUE, CURRENT_TIMESTAMP),

      ('Deadlift', '⚡', 'King of compound movements. Lift barbell from ground to hip level using legs and back, emphasizing proper form.', 'STRENGTH', 'ADVANCED', FALSE, FALSE, 15, 80, 734, 4.9, 156, TRUE, CURRENT_TIMESTAMP),

-- Core Strength
      ('Crunches', '🔥', 'Targeted abdominal exercise. Lie on back, lift shoulders off ground by contracting abs, lower with control.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 4, 20, 1876, 4.2, 445, TRUE, CURRENT_TIMESTAMP),

-- =====================================================
-- ISOMETRIC EXERCISES (HOLD_BASED) - Time-Based Holds
-- =====================================================

      ('Plank', '🛡️', 'Core-strengthening isometric hold. Maintain straight body position on forearms and toes, engaging entire core.', 'STRENGTH', 'BEGINNER', FALSE, TRUE, 3, 15, 3456, 4.8, 892, TRUE, CURRENT_TIMESTAMP),

      ('Wall Sit', '🧱', 'Isometric leg strengthener. Sit against wall with thighs parallel to ground, hold position to build endurance.', 'STRENGTH', 'INTERMEDIATE', FALSE, TRUE, 5, 25, 1234, 4.4, 287, TRUE, CURRENT_TIMESTAMP),

      ('Dead Hang', '🤲', 'Grip and shoulder strengthening hold. Hang from pull-up bar with arms fully extended, focus on maintaining grip.', 'STRENGTH', 'INTERMEDIATE', FALSE, TRUE, 2, 10, 567, 4.6, 123, TRUE, CURRENT_TIMESTAMP),

      ('Bridge Hold', '🌉', 'Glute and posterior chain isometric. Lie on back, lift hips to create straight line from knees to shoulders.', 'STRENGTH', 'BEGINNER', FALSE, TRUE, 4, 20, 987, 4.3, 234, TRUE, CURRENT_TIMESTAMP),

      ('L-Sit', '🔺', 'Advanced core and arm isometric. Sit with legs extended, hands on ground, lift body and legs off ground.', 'STRENGTH', 'ADVANCED', FALSE, TRUE, 3, 25, 234, 4.9, 67, TRUE, CURRENT_TIMESTAMP),

-- =====================================================
-- CARDIO EXERCISES (TIME_BASED) - Duration/Distance Focus
-- =====================================================

      ('Running', '🏃', 'Classic cardiovascular exercise. Maintain steady pace for specified duration or distance, focus on breathing rhythm.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 20, 200, 4567, 4.7, 1234, TRUE, CURRENT_TIMESTAMP),

      ('Jumping Jacks', '⭐', 'Full-body cardio movement. Jump feet apart while raising arms overhead, return to starting position rapidly.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 8, 60, 2134, 4.4, 678, TRUE, CURRENT_TIMESTAMP),

      ('Burpees', '💥', 'High-intensity full-body exercise. Drop to squat, kick back to plank, return to squat, jump up with arms overhead.', 'PLYOMETRIC', 'INTERMEDIATE', TRUE, FALSE, 10, 120, 1456, 4.6, 389, TRUE, CURRENT_TIMESTAMP),

      ('Mountain Climbers', '⛰️', 'Dynamic core and cardio exercise. In plank position, rapidly alternate bringing knees toward chest.', 'CARDIO', 'INTERMEDIATE', TRUE, FALSE, 6, 50, 1789, 4.5, 456, TRUE, CURRENT_TIMESTAMP),

-- =====================================================
-- FLEXIBILITY EXERCISES (TIME_BASED) - Duration Focus
-- =====================================================

      ('Child''s Pose', '🧘', 'Restorative yoga pose for back and shoulders. Kneel and sit back on heels, extend arms forward on ground.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 3, 5, 891, 4.3, 234, TRUE, CURRENT_TIMESTAMP),

      ('Downward Dog', '🐕', 'Classic yoga pose for full-body stretch. Form inverted V-shape with hands and feet on ground, straighten legs and arms.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 2, 8, 1567, 4.5, 423, TRUE, CURRENT_TIMESTAMP);

-- =====================================================
-- EXERCISE MUSCLE GROUPS (Comprehensive Mapping)
-- =====================================================

-- Push-Up muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (1, 'Chest'), (1, 'Shoulders'), (1, 'Triceps'), (1, 'Core');

-- Pull-Up muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (2, 'Back'), (2, 'Biceps'), (2, 'Forearms'), (2, 'Core');

-- Dumbbell Bench Press muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (3, 'Chest'), (3, 'Shoulders'), (3, 'Triceps');

-- Bodyweight Squat muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (4, 'Quadriceps'), (4, 'Glutes'), (4, 'Hamstrings'), (4, 'Calves');

-- Deadlift muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (5, 'Hamstrings'), (5, 'Glutes'), (5, 'Back'), (5, 'Traps'), (5, 'Forearms');

-- Crunches muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (6, 'Core'), (6, 'Abdominals');

-- Plank muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (7, 'Core'), (7, 'Shoulders'), (7, 'Back'), (7, 'Glutes');

-- Wall Sit muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (8, 'Quadriceps'), (8, 'Glutes'), (8, 'Calves');

-- Dead Hang muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (9, 'Forearms'), (9, 'Shoulders'), (9, 'Back');

-- Bridge Hold muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (10, 'Glutes'), (10, 'Hamstrings'), (10, 'Core');

-- L-Sit muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (11, 'Core'), (11, 'Shoulders'), (11, 'Triceps'), (11, 'Hip Flexors');

-- Running muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (12, 'Quadriceps'), (12, 'Hamstrings'), (12, 'Calves'), (12, 'Glutes'), (12, 'Core');

-- Jumping Jacks muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (13, 'Quadriceps'), (13, 'Calves'), (13, 'Shoulders'), (13, 'Core');

-- Burpees muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (14, 'Chest'), (14, 'Shoulders'), (14, 'Core'), (14, 'Quadriceps'), (14, 'Glutes');

-- Mountain Climbers muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (15, 'Core'), (15, 'Shoulders'), (15, 'Quadriceps'), (15, 'Hip Flexors');

-- Child's Pose muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (16, 'Back'), (16, 'Shoulders'), (16, 'Hips');

-- Downward Dog muscle groups
INSERT INTO exercise_muscle_groups (exercise_id, muscle_group) VALUES
                                                                   (17, 'Shoulders'), (17, 'Back'), (17, 'Hamstrings'), (17, 'Calves');

-- =====================================================
-- EXERCISE EQUIPMENT REQUIREMENTS
-- =====================================================

-- Bodyweight exercises (no equipment)
INSERT INTO exercise_equipment (exercise_id, equipment) VALUES
                                                            (1, 'None'), (4, 'None'), (6, 'None'), (7, 'None'), (8, 'None'),
                                                            (10, 'None'), (12, 'None'), (13, 'None'), (14, 'None'), (15, 'None'),
                                                            (16, 'None'), (17, 'None');

-- Equipment-based exercises
INSERT INTO exercise_equipment (exercise_id, equipment) VALUES
                                                            (2, 'Pull-up Bar'), (9, 'Pull-up Bar'),
                                                            (3, 'Dumbbells'), (3, 'Bench'),
                                                            (5, 'Barbell'), (5, 'Weight Plates'),
                                                            (11, 'Parallel Bars');

-- =====================================================
-- EXERCISE BENEFITS
-- =====================================================

-- Push-Up benefits
INSERT INTO exercise_benefits (exercise_id, benefit) VALUES
                                                         (1, 'Builds upper body strength'), (1, 'Improves core stability'), (1, 'No equipment required'), (1, 'Multiple variations available');

-- Pull-Up benefits
INSERT INTO exercise_benefits (exercise_id, benefit) VALUES
                                                         (2, 'Develops back strength'), (2, 'Improves grip strength'), (2, 'Enhances posture'), (2, 'Functional movement pattern');

-- Plank benefits
INSERT INTO exercise_benefits (exercise_id, benefit) VALUES
                                                         (7, 'Strengthens entire core'), (7, 'Improves posture'), (7, 'Reduces back pain risk'), (7, 'Enhances stability');

-- Running benefits
INSERT INTO exercise_benefits (exercise_id, benefit) VALUES
                                                         (12, 'Improves cardiovascular health'), (12, 'Burns high calories'), (12, 'Strengthens legs'), (12, 'Boosts mental health');

-- Burpees benefits
INSERT INTO exercise_benefits (exercise_id, benefit) VALUES
                                                         (14, 'Full-body workout'), (14, 'High calorie burn'), (14, 'Improves conditioning'), (14, 'No equipment needed');

-- =====================================================
-- EXERCISE TIPS
-- =====================================================

-- Push-Up tips
INSERT INTO exercise_tips (exercise_id, tip) VALUES
                                                 (1, 'Keep your body in a straight line from head to heels'),
                                                 (1, 'Don''t let your hips sag or pike up'),
                                                 (1, 'Focus on controlled movement, not speed'),
                                                 (1, 'Modify on knees if needed');

-- Plank tips
INSERT INTO exercise_tips (exercise_id, tip) VALUES
                                                 (7, 'Engage your core throughout the hold'),
                                                 (7, 'Keep your shoulders directly over your elbows'),
                                                 (7, 'Breathe normally, don''t hold your breath'),
                                                 (7, 'Start with shorter holds and build up time');

-- Running tips
INSERT INTO exercise_tips (exercise_id, tip) VALUES
                                                 (12, 'Land on midfoot, not heels'),
                                                 (12, 'Keep your cadence around 180 steps per minute'),
                                                 (12, 'Start slowly and build distance gradually'),
                                                 (12, 'Focus on breathing rhythm');

-- Deadlift tips
INSERT INTO exercise_tips (exercise_id, tip) VALUES
                                                 (5, 'Keep the bar close to your body throughout'),
                                                 (5, 'Drive through your heels'),
                                                 (5, 'Keep your back neutral, chest up'),
                                                 (5, 'Hip hinge movement, not squat');

-- =====================================================
-- FITNESS GOAL MAPPINGS (Multi-Goal Support)
-- =====================================================

-- Build Muscle goal mappings
INSERT INTO exercise_goal_mapping (exercise_id, goal_id, relevance_score, is_primary) VALUES
                                                                                          (1, 1, 5, TRUE),   -- Push-Up
                                                                                          (2, 1, 5, TRUE),   -- Pull-Up
                                                                                          (3, 1, 5, TRUE),   -- Dumbbell Bench Press
                                                                                          (4, 1, 4, FALSE),  -- Bodyweight Squat
                                                                                          (5, 1, 5, TRUE),   -- Deadlift
                                                                                          (6, 1, 3, FALSE);  -- Crunches

-- Lose Weight goal mappings
INSERT INTO exercise_goal_mapping (exercise_id, goal_id, relevance_score, is_primary) VALUES
                                                                                          (12, 2, 5, TRUE),  -- Running
                                                                                          (13, 2, 4, FALSE), -- Jumping Jacks
                                                                                          (14, 2, 5, TRUE),  -- Burpees
                                                                                          (15, 2, 4, FALSE); -- Mountain Climbers

-- Gain Strength goal mappings
INSERT INTO exercise_goal_mapping (exercise_id, goal_id, relevance_score, is_primary) VALUES
                                                                                          (2, 3, 5, TRUE),   -- Pull-Up
                                                                                          (3, 3, 5, TRUE),   -- Dumbbell Bench Press
                                                                                          (5, 3, 5, TRUE),   -- Deadlift
                                                                                          (7, 3, 4, FALSE),  -- Plank
                                                                                          (11, 3, 5, TRUE);  -- L-Sit

-- Improve Endurance goal mappings
INSERT INTO exercise_goal_mapping (exercise_id, goal_id, relevance_score, is_primary) VALUES
                                                                                          (12, 4, 5, TRUE),  -- Running
                                                                                          (8, 4, 4, FALSE),  -- Wall Sit
                                                                                          (14, 4, 5, TRUE),  -- Burpees
                                                                                          (7, 4, 3, FALSE);  -- Plank

-- Increase Flexibility goal mappings
INSERT INTO exercise_goal_mapping (exercise_id, goal_id, relevance_score, is_primary) VALUES
                                                                                          (16, 5, 5, TRUE),  -- Child's Pose
                                                                                          (17, 5, 5, TRUE);  -- Downward Dog

-- Functional Fitness goal mappings
INSERT INTO exercise_goal_mapping (exercise_id, goal_id, relevance_score, is_primary) VALUES
                                                                                          (1, 7, 4, FALSE),  -- Push-Up
                                                                                          (4, 7, 5, TRUE),   -- Bodyweight Squat
                                                                                          (5, 7, 5, TRUE),   -- Deadlift
                                                                                          (14, 7, 4, FALSE); -- Burpees

-- =====================================================
-- VERIFICATION QUERIES AND STATS
-- =====================================================

-- Verify exercise distribution by workout tracking mode
DO $$
DECLARE
rep_based_count INTEGER;
    hold_based_count INTEGER;
    time_based_count INTEGER;
    total_count INTEGER;
BEGIN
    -- Count exercises by tracking mode
SELECT COUNT(*) INTO rep_based_count FROM exercises WHERE is_cardio = FALSE AND is_isometric = FALSE;
SELECT COUNT(*) INTO hold_based_count FROM exercises WHERE is_isometric = TRUE;
SELECT COUNT(*) INTO time_based_count FROM exercises WHERE is_cardio = TRUE;
SELECT COUNT(*) INTO total_count FROM exercises;

RAISE NOTICE '========================================';
    RAISE NOTICE 'V012 Core Exercises Migration Complete!';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Exercise Distribution by Workout Tracking Mode:';
    RAISE NOTICE '  REP_BASED (Strength): % exercises', rep_based_count;
    RAISE NOTICE '  HOLD_BASED (Isometric): % exercises', hold_based_count;
    RAISE NOTICE '  TIME_BASED (Cardio): % exercises', time_based_count;
    RAISE NOTICE '  Total Exercises: %', total_count;
    RAISE NOTICE '';
    RAISE NOTICE 'Exercise Categories:';
    RAISE NOTICE '  STRENGTH: % exercises', (SELECT COUNT(*) FROM exercises WHERE exercise_type = 'STRENGTH');
    RAISE NOTICE '  CARDIO: % exercises', (SELECT COUNT(*) FROM exercises WHERE exercise_type = 'CARDIO');
    RAISE NOTICE '  PLYOMETRIC: % exercises', (SELECT COUNT(*) FROM exercises WHERE exercise_type = 'PLYOMETRIC');
    RAISE NOTICE '  FLEXIBILITY: % exercises', (SELECT COUNT(*) FROM exercises WHERE exercise_type = 'FLEXIBILITY');
    RAISE NOTICE '';
    RAISE NOTICE 'Difficulty Distribution:';
    RAISE NOTICE '  BEGINNER: % exercises', (SELECT COUNT(*) FROM exercises WHERE difficulty_level = 'BEGINNER');
    RAISE NOTICE '  INTERMEDIATE: % exercises', (SELECT COUNT(*) FROM exercises WHERE difficulty_level = 'INTERMEDIATE');
    RAISE NOTICE '  ADVANCED: % exercises', (SELECT COUNT(*) FROM exercises WHERE difficulty_level = 'ADVANCED');
    RAISE NOTICE '';
    RAISE NOTICE 'Equipment Requirements:';
    RAISE NOTICE '  No Equipment: % exercises', (SELECT COUNT(DISTINCT e.exercise_id) FROM exercises e JOIN exercise_equipment eq ON e.exercise_id = eq.exercise_id WHERE eq.equipment = 'None');
    RAISE NOTICE '  Requires Equipment: % exercises', (SELECT COUNT(DISTINCT e.exercise_id) FROM exercises e JOIN exercise_equipment eq ON e.exercise_id = eq.exercise_id WHERE eq.equipment != 'None');
    RAISE NOTICE '';
    RAISE NOTICE 'Your workout tracking system is ready!';
    RAISE NOTICE '  Frontend can filter: isCardio, isIsometric, workoutTrackingMode';
    RAISE NOTICE '  Backend supports: REP_BASED, HOLD_BASED, TIME_BASED tracking';
    RAISE NOTICE '========================================';
END $$;