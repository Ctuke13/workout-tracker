package com.chidituke.workout_tracker.config;

import com.chidituke.workout_tracker.model.Exercise;
import com.chidituke.workout_tracker.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExerciseDataLoader implements CommandLineRunner {

    private final ExerciseRepository exerciseRepository;

    @Override
    public void run(String... args) throws Exception {
        if (exerciseRepository.count() == 0) {
            log.info("Database is empty. Loading initial exercise data...");
            loadAllExercises();
            log.info("Successfully loaded {} exercises into database", exerciseRepository.count());
        } else {
            log.info("Database already contains {} exercises. Skipping data load.", exerciseRepository.count());
        }
    }

    private void loadAllExercises() {
        loadFatBurnExercises();
        loadMuscleExercises();
        loadEnduranceExercises();
        loadFlexibilityExercises();
        loadSportSpecificExercises();
        loadRecoveryExercises();
    }

    private void loadFatBurnExercises() {
        createExercise(
                "HIIT Circuit",
                Exercise.ExerciseType.CARDIO,  // Maps to "fat-burn" goal
                20,
                List.of(),  // No equipment required
                Exercise.DifficultyLevel.INTERMEDIATE,
                400,
                "🔥",
                "High-intensity interval training combining cardio and strength movements for maximum calorie burn.",
                List.of("Burns calories fast", "Boosts metabolism for hours"),
                List.of("Start with 30 sec work, 30 sec rest", "Focus on form over speed", "Stay hydrated throughout"),
                List.of("CARDIO", "FULL_BODY")
        );

        createExercise(
                "Tabata Training",
                Exercise.ExerciseType.CARDIO,
                15,
                List.of(),
                Exercise.DifficultyLevel.ADVANCED,
                350,
                "⚡",
                "Ultra-high intensity 4-minute protocols that push your anaerobic capacity to the limit.",
                List.of("Maximum intensity", "Time efficient"),
                List.of("Give 100% effort during work intervals", "Only 2-3 times per week maximum", "Warm up thoroughly before starting"),
                List.of("CARDIO", "FULL_BODY")
        );

        createExercise(
                "Jump Rope HIIT",
                Exercise.ExerciseType.CARDIO,
                15,
                List.of("jump_rope"),
                Exercise.DifficultyLevel.INTERMEDIATE,
                300,
                "🪢",
                "Fast-paced jump rope intervals for cardiovascular fitness and agility.",
                List.of("Great cardio workout", "Improves coordination"),
                List.of("Land softly on balls of feet", "Start with basic bounce", "Keep elbows close to body"),
                List.of("CARDIO", "CALVES")
        );

        createExercise(
                "Burpee Challenge",
                Exercise.ExerciseType.PLYOMETRIC,
                10,
                List.of(),
                Exercise.DifficultyLevel.ADVANCED,
                400,
                "💥",
                "The ultimate full-body exercise for rapid fat burning and conditioning.",
                List.of("Full body conditioning", "Mental toughness"),
                List.of("Modify by stepping back instead of jumping", "Keep core tight throughout", "Breathe steadily"),
                List.of("FULL_BODY", "CARDIO")
        );
    }

    private void loadMuscleExercises() {
        createExercise(
                "Push Day Routine",
                Exercise.ExerciseType.STRENGTH,
                45,
                List.of("dumbbells"),
                Exercise.DifficultyLevel.INTERMEDIATE,
                250,
                "🏋️‍♂️",
                "Comprehensive upper body push workout targeting chest, shoulders, and triceps.",
                List.of("Builds upper body strength", "Develops chest and shoulders"),
                List.of("Start with compound movements first", "Progressive overload each week", "Rest 2-3 minutes between sets"),
                List.of("CHEST", "SHOULDERS", "TRICEPS")
        );

        createExercise(
                "Squat & Deadlift",
                Exercise.ExerciseType.STRENGTH,
                50,
                List.of("barbell"),
                Exercise.DifficultyLevel.ADVANCED,
                300,
                "🦵",
                "Heavy compound movements for maximum lower body development.",
                List.of("Builds leg strength", "Increases overall muscle mass"),
                List.of("Master form before adding weight", "Activate glutes properly", "Use full range of motion"),
                List.of("QUADS", "HAMSTRINGS", "GLUTES")
        );

        createExercise(
                "Pull Day Focus",
                Exercise.ExerciseType.STRENGTH,
                40,
                List.of("pull_up_bar"),
                Exercise.DifficultyLevel.INTERMEDIATE,
                250,
                "💪",
                "Back and bicep focused workout using pulling movements.",
                List.of("Develops back width", "Improves posture"),
                List.of("Squeeze shoulder blades together", "Control the negative", "Don't use momentum"),
                List.of("BACK", "BICEPS")
        );

        createExercise(
                "Bodyweight Strength",
                Exercise.ExerciseType.STRENGTH,
                30,
                List.of(),
                Exercise.DifficultyLevel.BEGINNER,
                200,
                "💯",
                "Build muscle using just your bodyweight with progressive exercises.",
                List.of("No equipment needed", "Functional strength"),
                List.of("Focus on perfect form", "Increase reps gradually", "Add time under tension"),
                List.of("FULL_BODY", "CORE")
        );
    }

    private void loadEnduranceExercises() {
        createExercise(
                "Steady-State Run",
                Exercise.ExerciseType.CARDIO,
                45,  // 30-60 mins average
                List.of(),
                Exercise.DifficultyLevel.BEGINNER,
                300,
                "🏃‍♂️",
                "Consistent pace running to build cardiovascular endurance.",
                List.of("Improves heart health", "Builds aerobic base"),
                List.of("Maintain conversational pace", "Start with shorter durations", "Focus on nasal breathing"),
                List.of("CARDIO", "FULL_BODY")
        );

        createExercise(
                "Rowing Intervals",
                Exercise.ExerciseType.CARDIO,
                25,
                List.of("rowing_machine"),
                Exercise.DifficultyLevel.INTERMEDIATE,
                400,
                "🚣‍♂️",
                "Full-body cardiovascular workout using rowing machine intervals.",
                List.of("Full body workout", "Low impact"),
                List.of("Drive with legs first", "Keep core engaged", "Control the return phase"),
                List.of("CARDIO", "BACK", "LEGS")
        );

        createExercise(
                "Cycling Endurance",
                Exercise.ExerciseType.CARDIO,
                60,  // 45-90 mins average
                List.of("exercise_bike"),
                Exercise.DifficultyLevel.INTERMEDIATE,
                300,
                "🚴‍♂️",
                "Sustained cycling workout for leg endurance and cardiovascular capacity.",
                List.of("Joint-friendly", "Weather independent"),
                List.of("Maintain 80-100 RPM cadence", "Use proper bike setup", "Stay hydrated"),
                List.of("CARDIO", "QUADS", "CALVES")
        );
    }

    private void loadFlexibilityExercises() {
        createExercise(
                "Dynamic Yoga Flow",
                Exercise.ExerciseType.FLEXIBILITY,
                30,
                List.of("yoga_mat"),
                Exercise.DifficultyLevel.BEGINNER,
                150,
                "🧘‍♀️",
                "Flowing yoga sequences combining movement with breath.",
                List.of("Improves flexibility", "Reduces stress"),
                List.of("Focus on breath coordination", "Don't force poses", "Use modifications as needed"),
                List.of("FULL_BODY", "CORE")
        );

        createExercise(
                "Mobility Routine",
                Exercise.ExerciseType.FLEXIBILITY,
                15,
                List.of(),
                Exercise.DifficultyLevel.BEGINNER,
                80,
                "🤸‍♂️",
                "Dynamic movements to improve joint range of motion.",
                List.of("Better movement patterns", "Injury prevention"),
                List.of("Move through full range slowly", "Do before and after workouts", "Focus on problem areas"),
                List.of("FULL_BODY")
        );

        createExercise(
                "Recovery Stretching",
                Exercise.ExerciseType.FLEXIBILITY,
                20,
                List.of("foam_roller"),
                Exercise.DifficultyLevel.BEGINNER,
                50,
                "😌",
                "Gentle stretches for deep relaxation and recovery.",
                List.of("Aids muscle recovery", "Better sleep"),
                List.of("Hold stretches 2-5 minutes", "Use props for support", "Create calm environment"),
                List.of("FULL_BODY")
        );
    }

    private void loadSportSpecificExercises() {
        createExercise(
                "Agility Drills",
                Exercise.ExerciseType.SPORTS_SPECIFIC,
                20,
                List.of("cones"),
                Exercise.DifficultyLevel.INTERMEDIATE,
                250,
                "⚡",
                "Quick movement drills for athletic performance and coordination.",
                List.of("Improves reaction time", "Sport performance"),
                List.of("Focus on quick feet", "Keep center of gravity low", "Practice direction changes"),
                List.of("FULL_BODY", "CARDIO")
        );

        createExercise(
                "Plyometric Power",
                Exercise.ExerciseType.PLYOMETRIC,
                25,
                List.of(),
                Exercise.DifficultyLevel.ADVANCED,
                300,
                "💥",
                "Jump training for explosive power and athletic performance.",
                List.of("Explosive power", "Vertical jump"),
                List.of("Land softly", "Quality over quantity", "Full recovery between sets"),
                List.of("LEGS", "CARDIO")
        );

        createExercise(
                "Sprint Training",
                Exercise.ExerciseType.CARDIO,
                30,
                List.of(),
                Exercise.DifficultyLevel.INTERMEDIATE,
                400,
                "🏃‍♀️",
                "High-intensity sprint intervals for speed and power development.",
                List.of("Speed development", "Anaerobic power"),
                List.of("Warm up thoroughly", "Focus on proper form", "Allow full recovery"),
                List.of("CARDIO", "LEGS")
        );
    }

    private void loadRecoveryExercises() {
        createExercise(
                "Active Recovery Walk",
                Exercise.ExerciseType.REHABILITATION,
                35,  // 30-45 mins average
                List.of(),
                Exercise.DifficultyLevel.BEGINNER,
                150,
                "🚶‍♂️",
                "Gentle walking to promote recovery and reduce muscle stiffness.",
                List.of("Promotes recovery", "Mental clarity"),
                List.of("Keep it light and easy", "Enjoy nature", "Focus on breathing"),
                List.of("CARDIO")
        );

        createExercise(
                "Meditation & Breathwork",
                Exercise.ExerciseType.REHABILITATION,
                15,  // 10-20 mins average
                List.of(),
                Exercise.DifficultyLevel.BEGINNER,
                20,
                "🧠",
                "Mindfulness practice for mental recovery and stress management.",
                List.of("Stress reduction", "Mental recovery"),
                List.of("Find quiet space", "Start with guided sessions", "Be patient with yourself"),
                List.of()  // No physical muscle groups
        );

        createExercise(
                "Foam Rolling Session",
                Exercise.ExerciseType.REHABILITATION,
                15,
                List.of("foam_roller"),
                Exercise.DifficultyLevel.BEGINNER,
                50,
                "🔄",
                "Self-massage technique for muscle recovery and tension relief.",
                List.of("Reduces muscle tension", "Improves circulation"),
                List.of("Roll slowly", "Breathe through discomfort", "Focus on tight areas"),
                List.of("FULL_BODY")
        );
    }

    private void createExercise(String name, Exercise.ExerciseType exerciseType, Integer durationMinutes,
                                List<String> equipment, Exercise.DifficultyLevel difficulty,
                                Integer calories, String emoji, String description,
                                List<String> benefits, List<String> tips, List<String> muscleGroups) {
        Exercise exercise = new Exercise();

        // ✅ FIXED: Using correct setter names and types
        exercise.setExerciseName(name);  // Correct field name
        exercise.setExerciseType(exerciseType);  // Enum type
        exercise.setEstimatedDurationMinutes(durationMinutes);  // Integer type
        exercise.setEquipmentRequired(equipment);  // List<String> type
        exercise.setDifficultyLevel(difficulty);  // Enum type
        exercise.setEstimatedCalories(calories);  // Integer type
        exercise.setEmoji(emoji);
        exercise.setDescription(description);
        exercise.setBenefits(benefits);
        exercise.setTips(tips);
        exercise.setTargetMuscleGroups(muscleGroups);  // Correct field name
        exercise.setPublished(true);
        exercise.setUsageCount(0);  // Initialize usage count
        exercise.setAverageRating(0.0);  // Initialize rating
        exercise.setTotalRatings(0);  // Initialize rating count

        exerciseRepository.save(exercise);
        log.debug("Created exercise: {} (Type: {}, Difficulty: {})", name, exerciseType, difficulty);
    }
}