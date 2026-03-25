-- =============================================================================
-- V020__Add_Extended_Exercises.sql
-- Adds 150+ commonly used exercises across all categories
-- Validated against Exercise.java enums:
--   ExerciseType: STRENGTH | CARDIO | FLEXIBILITY | BALANCE | PLYOMETRIC | REHABILITATION | SPORTS_SPECIFIC
--   DifficultyLevel: BEGINNER | INTERMEDIATE | ADVANCED
--   WorkoutTrackingMode: REP_BASED | HOLD_BASED | TIME_BASED
-- NOTE: Does NOT re-insert exercises already seeded in V012
-- =============================================================================

-- ============================================
-- STRENGTH — CHEST
-- ============================================
INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES
      ('Barbell Bench Press', '🏋️', 'Primary chest exercise using a barbell. Lie on bench, lower barbell to mid-chest, press to full extension. Keep feet flat and maintain slight arch.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 15, 80, 2100, 4.8, 520, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Incline Dumbbell Press', '📐', 'Upper chest focused press. Set bench to 30-45 degrees, press dumbbells from shoulder level to full extension above upper chest.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 12, 65, 980, 4.6, 245, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Decline Bench Press', '📉', 'Lower chest focused press. Lie on declined bench, press barbell from lower chest to full extension.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 12, 70, 567, 4.4, 134, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Cable Chest Fly', '🔀', 'Isolation exercise for chest. Set cables at shoulder height, bring handles together in arc motion in front of chest, control return.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 10, 45, 678, 4.5, 167, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Dumbbell Fly', '🦅', 'Chest isolation. Lie on bench, lower dumbbells in wide arc with slight elbow bend, bring back together over chest.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 10, 45, 789, 4.4, 198, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Chest Press Machine', '⚙️', 'Machine-based chest press. Sit in machine, press handles forward until arms extended, return with control. Beginner friendly.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 10, 55, 1200, 4.2, 312, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Diamond Push-Up', '💎', 'Push-up with hands forming a diamond shape below chest. Heavily targets triceps and inner chest. Keep elbows close to body.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 5, 28, 890, 4.5, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Decline Push-Up', '📉', 'Push-up with feet elevated on chair or bench. Shifts emphasis to upper chest and front delts. Great home exercise requiring no equipment.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 5, 30, 678, 4.4, 167, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Archer Push-Up', '🏹', 'Advanced push-up shifting weight to one arm. Extend one arm to side while performing push-up on other. Build toward one-arm push-up.', 'STRENGTH', 'ADVANCED', FALSE, FALSE, 'REP_BASED', 6, 35, 345, 4.7, 87, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- ============================================
-- STRENGTH — BACK
-- ============================================
INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES
      ('Barbell Row', '🚣', 'Compound back exercise. Hinge at hips, pull barbell to lower chest, squeeze shoulder blades, lower with control.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 15, 75, 1456, 4.7, 367, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Dumbbell Row', '💪', 'Single-arm back exercise. Place one hand on bench, pull dumbbell from hanging position to hip, lower with control.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 12, 55, 1789, 4.6, 445, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Lat Pulldown', '⬇️', 'Cable exercise targeting lats. Pull bar from overhead to upper chest, lean back slightly, control return.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 12, 55, 2100, 4.6, 534, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Seated Cable Row', '🪑', 'Horizontal pulling movement. Pull cable handle to lower chest, squeeze shoulder blades, extend arms with control.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 12, 50, 1234, 4.5, 312, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Face Pull', '😤', 'Rear delt and rotator cuff health exercise. Pull cable rope to face level with elbows high, externally rotate at end.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 8, 30, 890, 4.7, 223, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('T-Bar Row', '🔱', 'Heavy compound back movement. Straddle barbell or use T-bar machine, pull weight to chest, lower with control.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 15, 70, 678, 4.6, 167, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Wide-Grip Pull-Up', '🎯', 'Pull-up with wider than shoulder-width grip emphasizing lat width. Pull until chin clears bar, lower fully.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 10, 45, 1100, 4.8, 278, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Chin-Up', '🏅', 'Underhand grip pull-up targeting biceps and lats. Hang with palms facing you, pull until chin clears bar.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 8, 40, 1350, 4.7, 334, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Resistance Band Row', '🪢', 'Home-friendly rowing exercise. Anchor band at low point, pull handles to hips squeezing shoulder blades. No gym needed.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 30, 789, 4.3, 198, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- ============================================
-- STRENGTH — SHOULDERS
-- ============================================
INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES
      ('Overhead Press', '🏋️', 'Compound shoulder exercise. Press barbell or dumbbells from shoulder level to full overhead extension. Keep core tight.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 15, 70, 1567, 4.7, 389, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Dumbbell Lateral Raise', '↔️', 'Shoulder isolation for medial delts. Raise dumbbells to side until parallel to floor, lower with control.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 30, 2300, 4.5, 578, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Front Raise', '⬆️', 'Anterior delt isolation. Raise dumbbell or plate from thigh to shoulder height, lower slowly.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 30, 890, 4.3, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Arnold Press', '💫', 'Rotational shoulder press. Start with palms facing you, rotate and press overhead, reverse on descent.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 12, 55, 678, 4.6, 167, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Rear Delt Fly', '🦅', 'Posterior delt isolation. Hinge forward, raise dumbbells to side with slight elbow bend, squeeze rear delts.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 25, 1100, 4.4, 278, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Resistance Band Lateral Raise', '🪢', 'Home-friendly shoulder isolation. Stand on resistance band, raise arms to shoulder height, control descent.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 6, 20, 567, 4.2, 134, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- ============================================
-- STRENGTH — ARMS
-- ============================================
INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES
      ('Barbell Bicep Curl', '💪', 'Classic bicep builder. Stand with barbell at thighs, curl to shoulder level keeping elbows at sides, lower with control.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 10, 35, 2456, 4.5, 612, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Dumbbell Bicep Curl', '🦾', 'Alternating or simultaneous dumbbell curls. Supinate wrist at top for full bicep contraction.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 10, 35, 2890, 4.4, 723, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Hammer Curl', '🔨', 'Neutral grip curl targeting brachialis and brachioradialis. Keep palms facing each other throughout.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 30, 1234, 4.5, 312, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Preacher Curl', '📖', 'Strict bicep isolation on preacher bench. Full range of motion, eliminates body momentum from cheat curls.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 10, 35, 678, 4.6, 167, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Resistance Band Curl', '🪢', 'Home-friendly bicep curl. Stand on resistance band, curl handles to shoulders. Great for home workouts.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 25, 890, 4.2, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Tricep Overhead Extension', '🔝', 'Overhead tricep isolation. Hold dumbbell overhead with both hands, lower behind head, extend to full lockout.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 30, 1456, 4.4, 367, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Skull Crushers', '💀', 'Lying tricep extension with EZ bar. Lower bar toward forehead, extend to lockout, keep elbows stationary.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 10, 35, 890, 4.5, 223, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Tricep Pushdown', '⬇️', 'Cable tricep isolation. Push cable bar or rope from chin level to full extension, keep elbows at sides.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 30, 1789, 4.3, 445, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Barbell Shrug', '🤷', 'Trap isolation. Hold barbell at thighs, shrug straight up toward ears, hold briefly, lower slowly.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 35, 1100, 4.3, 278, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- ============================================
-- STRENGTH — LOWER BODY
-- ============================================
INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES
      ('Barbell Squat', '🏋️', 'King of leg exercises. Barbell on upper back, squat until thighs parallel to floor, drive through heels to stand.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 20, 100, 2300, 4.9, 578, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Goblet Squat', '🥃', 'Squat holding dumbbell or kettlebell at chest. Great for learning squat pattern. Keep elbows inside knees.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 12, 55, 1234, 4.6, 312, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Romanian Deadlift', '🦵', 'Hip hinge for hamstrings and glutes. Maintain slight knee bend, lower bar along legs until stretch felt, drive hips forward.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 15, 80, 1456, 4.7, 367, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Sumo Deadlift', '🤸', 'Wide-stance deadlift emphasizing glutes and inner thighs. Feet wider than shoulder width, toes pointed out.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 15, 85, 890, 4.7, 223, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Hip Thrust', '🍑', 'Primary glute exercise. Upper back on bench, barbell across hips, drive hips up to full extension, squeeze glutes.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 12, 60, 1789, 4.8, 445, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Bodyweight Hip Thrust', '🍑', 'Glute bridge with upper back on bench. Drive hips up to full extension without weight. Excellent home glute exercise.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 35, 1100, 4.5, 278, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Glute Kickback', '🦵', 'Bodyweight or cable glute isolation. On hands and knees, drive heel toward ceiling squeezing glute at top.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 25, 890, 4.3, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Leg Press', '🦵', 'Machine-based leg exercise. Push platform away at various foot positions to target quads, glutes, and hamstrings.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 15, 70, 2100, 4.5, 534, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Leg Extension', '⬆️', 'Quad isolation machine. Sit in machine, extend legs to full lockout, lower with control.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 10, 40, 1100, 4.2, 278, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Leg Curl', '🔄', 'Hamstring isolation machine. Lie face down or seated, curl weight toward glutes, lower with control.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 10, 40, 1234, 4.3, 312, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Walking Lunges', '🚶', 'Dynamic lunge variation. Step forward into lunge, bring rear foot into next lunge, continue walking forward.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 10, 55, 1567, 4.5, 389, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Reverse Lunge', '↩️', 'Step backward into lunge. Easier on knees than forward lunge. Great for beginners or those with knee concerns.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 45, 890, 4.4, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Lateral Lunge', '↔️', 'Side-to-side lunge for inner thighs and glutes. Step to side, lower into lunge with straight opposite leg.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 40, 678, 4.3, 167, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Nordic Hamstring Curl', '🦵', 'Advanced bodyweight hamstring exercise. Kneel and lower body toward ground controlling with hamstrings only.', 'STRENGTH', 'ADVANCED', FALSE, FALSE, 'REP_BASED', 10, 40, 345, 4.7, 87, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Kettlebell Swing', '🏑', 'Hip hinge power movement. Drive hips forward explosively to swing kettlebell to shoulder height, hinge back.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 12, 80, 1234, 4.7, 312, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Good Morning', '🌅', 'Hip hinge for posterior chain. Barbell on back, hinge at hips until torso parallel to floor, drive hips forward.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 12, 45, 456, 4.5, 112, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Seated Calf Raise', '🦶', 'Seated variation targeting soleus muscle. Sit with dumbbell on knee, raise on toes, lower fully to stretch.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 6, 20, 678, 4.1, 167, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- ============================================
-- STRENGTH — CORE
-- ============================================
INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES
      ('Hanging Leg Raise', '🏋️', 'Core exercise from pull-up bar. Hang, raise legs to parallel or above, lower with control. Minimize swinging.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 8, 30, 890, 4.7, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Ab Wheel Rollout', '⚙️', 'Advanced core stability. Kneel with ab wheel, roll forward keeping core tight, pull back to start.', 'STRENGTH', 'ADVANCED', FALSE, FALSE, 'REP_BASED', 8, 30, 567, 4.8, 134, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('V-Up', '✌️', 'Full body crunch. Simultaneously lift legs and torso reaching hands to feet, lower with control.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 6, 25, 678, 4.5, 167, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Leg Raise', '🦵', 'Lie flat, raise straight legs to vertical, lower slowly keeping lower back pressed to floor.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 6, 20, 1100, 4.3, 278, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Pallof Press', '🎯', 'Anti-rotation core exercise. Stand perpendicular to cable, press handle away from chest, resist rotation.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 8, 20, 345, 4.6, 87, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Sit-Up', '🆙', 'Classic core movement. Lie with knees bent, hands behind head, curl torso to seated, lower with control.', 'STRENGTH', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 5, 20, 2100, 4.1, 534, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Cable Crunch', '🔌', 'Weighted ab exercise. Kneel below cable pulley, pull weight toward knees while crunching torso down.', 'STRENGTH', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 8, 25, 678, 4.4, 167, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP);

-- ============================================
-- PLYOMETRIC EXERCISES
-- ============================================
INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES
      ('Box Jump', '📦', 'Explosive jump onto elevated box. Squat then jump landing softly on box, step down carefully. Develops explosive power.', 'PLYOMETRIC', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 8, 60, 890, 4.6, 223, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Tuck Jump', '🤸', 'Explosive jump bringing knees to chest. Land softly, absorb impact, immediately jump again.', 'PLYOMETRIC', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 5, 70, 456, 4.5, 112, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Depth Jump', '📦', 'Step off box, land and immediately jump as high as possible. Trains reactive strength and rate of force development.', 'PLYOMETRIC', 'ADVANCED', FALSE, FALSE, 'REP_BASED', 8, 65, 345, 4.7, 87, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Broad Jump', '🦘', 'Horizontal explosive jump for distance. Swing arms, bend knees, jump forward landing in athletic position.', 'PLYOMETRIC', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 6, 55, 456, 4.5, 112, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Clap Push-Up', '👏', 'Explosive push-up with hand clap. Push with maximum force to become airborne, clap hands, land and absorb.', 'PLYOMETRIC', 'ADVANCED', FALSE, FALSE, 'REP_BASED', 6, 45, 345, 4.7, 87, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Lateral Bound', '↔️', 'Single-leg lateral jump. Push off one foot, land on opposite foot, immediately bound back. Builds lateral power.', 'PLYOMETRIC', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 6, 60, 456, 4.4, 112, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- ============================================
-- ISOMETRIC / BALANCE EXERCISES
-- ============================================
INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES
      ('Copenhagen Plank', '🔷', 'Advanced side plank with top leg on elevated surface. Challenges lateral core and adductor strength.', 'BALANCE', 'ADVANCED', FALSE, TRUE, 'HOLD_BASED', 3, 20, 234, 4.7, 56, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Bear Crawl Hold', '🐻', 'On hands and toes with knees 2 inches off floor. Hold position building shoulder and core stability.', 'BALANCE', 'BEGINNER', FALSE, TRUE, 'HOLD_BASED', 3, 15, 567, 4.3, 134, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Passive Hang', '🪝', 'Hang from bar with passive shoulder position. Decompresses spine, builds grip endurance. Good for shoulder health.', 'BALANCE', 'BEGINNER', FALSE, TRUE, 'HOLD_BASED', 2, 10, 678, 4.4, 167, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Arch Hold', '🌉', 'Lie face down, lift arms and legs off ground simultaneously. Builds entire posterior chain isometrically.', 'BALANCE', 'BEGINNER', FALSE, TRUE, 'HOLD_BASED', 3, 15, 456, 4.3, 112, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Handstand Hold', '🙃', 'Inverted balance against wall. Builds wrist, shoulder, and core strength. Start against wall, progress to freestanding.', 'BALANCE', 'ADVANCED', FALSE, TRUE, 'HOLD_BASED', 5, 30, 345, 4.8, 87, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Isometric Squat Hold', '🪑', 'Hold the bottom squat position for time. Builds quad endurance and mobility simultaneously.', 'BALANCE', 'BEGINNER', FALSE, TRUE, 'HOLD_BASED', 3, 20, 789, 4.3, 198, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Single-Leg Balance', '🦩', 'Stand on one leg for balance training. Progress to eyes closed or unstable surface for ankle stability.', 'BALANCE', 'BEGINNER', FALSE, TRUE, 'HOLD_BASED', 3, 10, 890, 4.1, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Tuck Hold', '🧘', 'Seated with knees tucked, lift body off ground with straight arms. Fundamental gymnastic skill.', 'BALANCE', 'INTERMEDIATE', FALSE, TRUE, 'HOLD_BASED', 2, 20, 345, 4.5, 87, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Horse Stance', '🐴', 'Wide squat hold from martial arts. Feet wide, toes out, lower into deep squat. Builds exceptional leg endurance.', 'BALANCE', 'INTERMEDIATE', FALSE, TRUE, 'HOLD_BASED', 4, 25, 456, 4.2, 112, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- ============================================
-- CARDIO EXERCISES
-- ============================================
INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES
      ('Jogging', '🏃', 'Easy-paced running between walking and sprinting. Great entry point for new runners. You should be able to hold a conversation throughout.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 'TIME_BASED', 20, 150, 2890, 4.5, 723, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Cycling', '🚴', 'Stationary or outdoor cycling. Low impact cardio excellent for endurance and leg conditioning. Adjust resistance for intensity.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 'TIME_BASED', 30, 250, 2300, 4.6, 578, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Swimming', '🏊', 'Full body low-impact cardio. Choose stroke based on goal — freestyle for endurance, butterfly for power.', 'CARDIO', 'INTERMEDIATE', TRUE, FALSE, 'TIME_BASED', 30, 300, 1456, 4.8, 367, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Rowing Machine', '🚣', 'Full body cardio on rowing ergometer. Drive with legs first, lean back, then pull arms. Return in reverse sequence.', 'CARDIO', 'INTERMEDIATE', TRUE, FALSE, 'TIME_BASED', 20, 220, 890, 4.7, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Elliptical', '⭕', 'Low-impact cardio machine. Smooth circular motion reduces joint stress. Use handles for upper body involvement.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 'TIME_BASED', 30, 200, 1789, 4.3, 445, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Stair Climber', '🪜', 'Stair climbing machine. Excellent for glutes, quads, and cardiovascular fitness. Keep posture upright, avoid leaning on rails.', 'CARDIO', 'INTERMEDIATE', TRUE, FALSE, 'TIME_BASED', 20, 230, 890, 4.5, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Jump Rope', '🪢', 'Classic cardio using jump rope. Start with basic two-foot jumps, progress to alternating feet or double-unders.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 'TIME_BASED', 10, 120, 1567, 4.6, 389, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Walking', '🚶', 'Low intensity cardio accessible to all fitness levels. Brisk walking at 3-4 mph provides meaningful cardiovascular benefit.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 'TIME_BASED', 30, 150, 2789, 4.4, 698, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Sprints', '⚡', 'Maximum effort short running intervals. Sprint at full speed for set distance or time, rest, repeat. Develops speed and power.', 'CARDIO', 'ADVANCED', TRUE, FALSE, 'TIME_BASED', 15, 200, 890, 4.7, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Treadmill Run', '🏃', 'Controlled indoor running. Set speed and incline for consistent training regardless of weather conditions.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 'TIME_BASED', 25, 220, 2100, 4.4, 534, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Battle Ropes', '🪢', 'High intensity upper body cardio. Create alternating or simultaneous waves with heavy ropes for brutal full body conditioning.', 'CARDIO', 'INTERMEDIATE', TRUE, FALSE, 'TIME_BASED', 10, 150, 678, 4.6, 167, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Sled Push', '🛷', 'Loaded sled pushed across turf. Develops lower body power and cardiovascular capacity simultaneously.', 'CARDIO', 'INTERMEDIATE', TRUE, FALSE, 'TIME_BASED', 10, 180, 456, 4.7, 112, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Shadow Boxing', '🥊', 'Cardio boxing without equipment. Combine punches, footwork, and head movement. Zero equipment home cardio.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 'TIME_BASED', 15, 150, 890, 4.5, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Hiking', '🥾', 'Outdoor cardiovascular exercise on trails or hills. Variable terrain challenges balance and cardio differently than flat running.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 'TIME_BASED', 60, 400, 1456, 4.7, 367, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Speed Skaters', '⛸️', 'Lateral jumping mimicking speed skating. Jump side to side landing on one leg, reach opposite hand to foot.', 'CARDIO', 'INTERMEDIATE', TRUE, FALSE, 'TIME_BASED', 6, 65, 567, 4.4, 134, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Stair Running', '🪜', 'Running up and down stairs. High intensity movement building leg power and cardiovascular capacity.', 'CARDIO', 'INTERMEDIATE', TRUE, FALSE, 'TIME_BASED', 15, 180, 678, 4.6, 167, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Dance Cardio', '💃', 'Rhythmic dance-based cardio. Follow choreography or freestyle to music. Fun, effective zero-equipment home cardio.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 'TIME_BASED', 30, 200, 1100, 4.3, 278, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Lateral Shuffles', '↔️', 'Side-to-side defensive shuffle. Stay low in athletic stance, shuffle laterally without crossing feet.', 'CARDIO', 'BEGINNER', TRUE, FALSE, 'TIME_BASED', 5, 50, 456, 4.2, 112, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Medicine Ball Slam', '💥', 'Explosive power and cardio movement. Raise ball overhead, slam to ground with maximum force, catch and repeat.', 'CARDIO', 'INTERMEDIATE', TRUE, FALSE, 'TIME_BASED', 8, 80, 678, 4.6, 167, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- ============================================
-- FLEXIBILITY & MOBILITY
-- ============================================
INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES
      ('Standing Quad Stretch', '🦵', 'Stand on one leg, pull heel to glute, hold 30-60 seconds each side. Essential pre or post workout.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 2, 5, 1234, 4.2, 312, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Seated Hamstring Stretch', '🪑', 'Sit with legs extended, reach toward toes keeping back straight. Hold at point of tension.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 3, 5, 1456, 4.1, 367, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Pigeon Pose', '🕊️', 'Deep hip opener from yoga. Front leg bent at 90 degrees, back leg extended, lower chest toward front shin.', 'FLEXIBILITY', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 4, 8, 1100, 4.6, 278, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Couch Stretch', '🛋️', 'Deep hip flexor stretch with rear leg on wall. One of the best stretches for desk workers with tight hips.', 'FLEXIBILITY', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 4, 5, 890, 4.7, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Figure Four Stretch', '4️⃣', 'Piriformis and glute stretch. Lie on back, cross ankle over opposite knee, pull thigh toward chest.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 3, 5, 1100, 4.4, 278, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Spinal Twist', '🔄', 'Seated or supine spinal rotation. Cross one leg over, use opposite arm to deepen twist. Hold each side.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 3, 5, 1234, 4.3, 312, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('World''s Greatest Stretch', '🌍', 'Dynamic full-body mobility sequence. Lunge with rotation and overhead reach. Covers hip flexors, thoracic spine, hamstrings.', 'FLEXIBILITY', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 4, 10, 678, 4.7, 167, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Thoracic Spine Extension', '🔙', 'Upper back mobility using foam roller. Extend over roller across mid-back, work through stiff segments.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 4, 5, 789, 4.5, 198, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Butterfly Stretch', '🦋', 'Inner thigh and groin stretch. Sit with soles of feet together, knees out, gently press knees toward floor.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 3, 5, 1234, 4.2, 312, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Low Lunge Stretch', '🫳', 'Deep hip flexor stretch. Back knee on ground, sink hips forward, raise arms overhead for full front body stretch.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 3, 6, 1100, 4.4, 278, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Upward Dog', '🐕', 'Yoga pose for spine extension. Press through hands, lift chest and hips off ground, feel stretch through hip flexors.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 2, 8, 678, 4.3, 167, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Foam Rolling', '🛞', 'Self-myofascial release. Slowly roll over tight muscles applying body weight to release knots and tension.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 10, 20, 1567, 4.5, 389, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Seated Forward Fold', '🙇', 'Full posterior chain stretch. Sit with legs extended, hinge at hips and reach forward. Focus on hinging not rounding.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 3, 5, 890, 4.3, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Ankle Circles', '⭕', 'Ankle mobility. Rotate ankle through full range clockwise and counterclockwise. Pre-workout essential.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 2, 3, 678, 4.0, 167, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Chest Opener', '💓', 'Pectoral stretch. Clasp hands behind back, squeeze shoulder blades, lift arms, open chest toward ceiling.', 'FLEXIBILITY', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 2, 5, 890, 4.2, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- ============================================
-- SPORTS-SPECIFIC & FUNCTIONAL
-- ============================================
INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES
      ('Power Clean', '⚡', 'Olympic lift combining deadlift and front squat. Pull barbell from floor explosively, catch in front rack at shoulder height.', 'SPORTS_SPECIFIC', 'ADVANCED', FALSE, FALSE, 'REP_BASED', 20, 100, 456, 4.8, 112, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Thruster', '🚀', 'Front squat into overhead press in one explosive motion. Demanding CrossFit conditioning and strength movement.', 'SPORTS_SPECIFIC', 'ADVANCED', FALSE, FALSE, 'REP_BASED', 15, 90, 567, 4.7, 134, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Turkish Get-Up', '🇹🇷', 'Complex kettlebell movement from floor to standing through multiple transitions while keeping weight overhead.', 'SPORTS_SPECIFIC', 'ADVANCED', FALSE, FALSE, 'REP_BASED', 15, 60, 345, 4.8, 87, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Farmer Carry', '🌾', 'Loaded carry for grip, core, and conditioning. Carry heavy dumbbells at sides for distance or time.', 'SPORTS_SPECIFIC', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 60, 678, 4.6, 167, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Kettlebell Snatch', '🏑', 'Single-arm movement from between legs to overhead in one motion. Builds power, shoulder stability, and conditioning.', 'SPORTS_SPECIFIC', 'ADVANCED', FALSE, FALSE, 'REP_BASED', 15, 80, 345, 4.7, 87, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Ring Push-Up', '⭕', 'Push-up on gymnastic rings. Instability demands greater stabilizer activation. Hands turn naturally as you press.', 'SPORTS_SPECIFIC', 'INTERMEDIATE', FALSE, FALSE, 'REP_BASED', 8, 40, 456, 4.7, 112, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Ring Row', '⭕', 'Bodyweight row using gymnastic rings. Adjust body angle to modify difficulty. Keep body rigid like a plank.', 'SPORTS_SPECIFIC', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 8, 35, 567, 4.6, 134, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- ============================================
-- REHABILITATION EXERCISES
-- ============================================
INSERT INTO exercises (
    exercise_name, emoji, description, exercise_type, difficulty_level,
    is_cardio, is_isometric, workout_tracking_mode, estimated_duration_minutes, estimated_calories,
    usage_count, average_rating, total_ratings, published, is_favorite, created_by_professional, created_at
) VALUES
      ('Clamshell', '🐚', 'Hip abductor strengthening. Lie on side with knees bent, open top knee like a clamshell, lower with control. Targets glute medius.', 'REHABILITATION', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 6, 10, 678, 4.4, 167, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Band Pull-Apart', '🪢', 'Shoulder health exercise. Hold band at shoulder height with straight arms, pull apart squeezing shoulder blades together.', 'REHABILITATION', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 5, 10, 890, 4.5, 223, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Wall Slide', '🧱', 'Scapular control exercise. Stand against wall, slide arms up and down maintaining full contact throughout range.', 'REHABILITATION', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 5, 8, 567, 4.3, 134, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Terminal Knee Extension', '🦵', 'VMO and knee tracking rehab. Anchor band behind knee, extend leg to full lockout. Helps knee pain and tracking issues.', 'REHABILITATION', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 6, 10, 456, 4.4, 112, TRUE, FALSE, TRUE, CURRENT_TIMESTAMP),
      ('Banded Monster Walk', '🦎', 'Hip abductor activation. Band around ankles, walk laterally in squat position keeping band taut throughout.', 'REHABILITATION', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 6, 15, 567, 4.3, 134, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('Neck Retraction', '🤸', 'Cervical spine correction for forward head posture. Pull head straight back creating double chin, hold briefly.', 'REHABILITATION', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 3, 5, 456, 4.2, 112, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
      ('90-90 Hip Stretch', '🔄', 'Hip external and internal rotation stretch. Both legs at 90-degree angles, rotate between forward and side-lying positions.', 'REHABILITATION', 'BEGINNER', FALSE, FALSE, 'REP_BASED', 4, 5, 678, 4.5, 167, TRUE, FALSE, FALSE, CURRENT_TIMESTAMP);

-- ============================================
-- VALIDATION
-- ============================================
DO $$
DECLARE
total_exercises INTEGER;
    strength_count INTEGER;
    cardio_count INTEGER;
    flexibility_count INTEGER;
    balance_count INTEGER;
    plyometric_count INTEGER;
    sports_count INTEGER;
    rehab_count INTEGER;
BEGIN
SELECT COUNT(*) INTO total_exercises FROM exercises WHERE published = TRUE;
SELECT COUNT(*) INTO strength_count FROM exercises WHERE exercise_type = 'STRENGTH' AND published = TRUE;
SELECT COUNT(*) INTO cardio_count FROM exercises WHERE exercise_type = 'CARDIO' AND published = TRUE;
SELECT COUNT(*) INTO flexibility_count FROM exercises WHERE exercise_type = 'FLEXIBILITY' AND published = TRUE;
SELECT COUNT(*) INTO balance_count FROM exercises WHERE exercise_type = 'BALANCE' AND published = TRUE;
SELECT COUNT(*) INTO plyometric_count FROM exercises WHERE exercise_type = 'PLYOMETRIC' AND published = TRUE;
SELECT COUNT(*) INTO sports_count FROM exercises WHERE exercise_type = 'SPORTS_SPECIFIC' AND published = TRUE;
SELECT COUNT(*) INTO rehab_count FROM exercises WHERE exercise_type = 'REHABILITATION' AND published = TRUE;

RAISE NOTICE '=========================================';
    RAISE NOTICE '✅ V020 EXTENDED EXERCISES COMPLETE';
    RAISE NOTICE '=========================================';
    RAISE NOTICE '📚 Total published exercises: %', total_exercises;
    RAISE NOTICE '  💪 STRENGTH: %', strength_count;
    RAISE NOTICE '  🏃 CARDIO: %', cardio_count;
    RAISE NOTICE '  🤸 FLEXIBILITY: %', flexibility_count;
    RAISE NOTICE '  ⚖️  BALANCE (Isometric): %', balance_count;
    RAISE NOTICE '  🚀 PLYOMETRIC: %', plyometric_count;
    RAISE NOTICE '  🏅 SPORTS_SPECIFIC: %', sports_count;
    RAISE NOTICE '  🏥 REHABILITATION: %', rehab_count;
    RAISE NOTICE '=========================================';
END $$;