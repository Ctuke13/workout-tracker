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
                "fat-burn",
                "20 mins",
                "None",
                "Intermediate",
                "400-600/hr",
                "🔥",
                "High-intensity interval training combining cardio and strength movements for maximum calorie burn.",
                List.of("Burns calories fast", "Boosts metabolism for hours"),
                List.of("Start with 30 sec work, 30 sec rest", "Focus on form over speed", "Stay hydrated throughout")
        );

        createExercise(
                "Tabata Training",
                "fat-burn",
                "15 mins",
                "None",
                "Advanced",
                "350-500/hr",
                "⚡",
                "Ultra-high intensity 4-minute protocols that push your anaerobic capacity to the limit.",
                List.of("Maximum intensity", "Time efficient"),
                List.of("Give 100% effort during work intervals", "Only 2-3 times per week maximum", "Warm up thoroughly before starting")
        );

        createExercise(
                "Jump Rope HIIT",
                "fat-burn",
                "15 mins",
                "Jump rope",
                "Intermediate",
                "300-450/hr",
                "🪢",
                "Fast-paced jump rope intervals for cardiovascular fitness and agility.",
                List.of("Great cardio workout", "Improves coordination"),
                List.of("Land softly on balls of feet", "Start with basic bounce", "Keep elbows close to body")
        );

        createExercise(
                "Burpee Challenge",
                "fat-burn",
                "10 mins",
                "None",
                "Advanced",
                "400-600/hr",
                "💥",
                "The ultimate full-body exercise for rapid fat burning and conditioning.",
                List.of("Full body conditioning", "Mental toughness"),
                List.of("Modify by stepping back instead of jumping", "Keep core tight throughout", "Breathe steadily")
        );
    }

    private void loadMuscleExercises() {
        createExercise(
                "Push Day Routine",
                "muscle-building",
                "45 mins",
                "Dumbbells",
                "Intermediate",
                "250-400/hr",
                "🏋️‍♂️",
                "Comprehensive upper body push workout targeting chest, shoulders, and triceps.",
                List.of("Builds upper body strength", "Develops chest and shoulders"),
                List.of("Start with compound movements first", "Progressive overload each week", "Rest 2-3 minutes between sets")
        );

        createExercise(
                "Squat & Deadlift",
                "muscle-building",
                "50 mins",
                "Barbell",
                "Advanced",
                "300-450/hr",
                "🦵",
                "Heavy compound movements for maximum lower body development.",
                List.of("Builds leg strength", "Increases overall muscle mass"),
                List.of("Master form before adding weight", "Activate glutes properly", "Use full range of motion")
        );

        createExercise(
                "Pull Day Focus",
                "muscle-building",
                "40 mins",
                "Pull-up bar",
                "Intermediate",
                "250-380/hr",
                "💪",
                "Back and bicep focused workout using pulling movements.",
                List.of("Develops back width", "Improves posture"),
                List.of("Squeeze shoulder blades together", "Control the negative", "Don't use momentum")
        );

        createExercise(
                "Bodyweight Strength",
                "muscle-building",
                "30 mins",
                "None",
                "Beginner",
                "200-350/hr",
                "💯",
                "Build muscle using just your bodyweight with progressive exercises.",
                List.of("No equipment needed", "Functional strength"),
                List.of("Focus on perfect form", "Increase reps gradually", "Add time under tension")
        );
    }

    private void loadEnduranceExercises() {
        createExercise(
                "Steady-State Run",
                "endurance",
                "30-60 mins",
                "None",
                "Beginner",
                "300-500/hr",
                "🏃‍♂️",
                "Consistent pace running to build cardiovascular endurance.",
                List.of("Improves heart health", "Builds aerobic base"),
                List.of("Maintain conversational pace", "Start with shorter durations", "Focus on nasal breathing")
        );

        createExercise(
                "Rowing Intervals",
                "endurance",
                "25 mins",
                "Rowing machine",
                "Intermediate",
                "400-600/hr",
                "🚣‍♂️",
                "Full-body cardiovascular workout using rowing machine intervals.",
                List.of("Full body workout", "Low impact"),
                List.of("Drive with legs first", "Keep core engaged", "Control the return phase")
        );

        createExercise(
                "Cycling Endurance",
                "endurance",
                "45-90 mins",
                "Bike",
                "Intermediate",
                "300-600/hr",
                "🚴‍♂️",
                "Sustained cycling workout for leg endurance and cardiovascular capacity.",
                List.of("Joint-friendly", "Weather independent"),
                List.of("Maintain 80-100 RPM cadence", "Use proper bike setup", "Stay hydrated")
        );
    }

    private void loadFlexibilityExercises() {
        createExercise(
                "Dynamic Yoga Flow",
                "flexibility",
                "30 mins",
                "Yoga mat",
                "Beginner",
                "150-250/hr",
                "🧘‍♀️",
                "Flowing yoga sequences combining movement with breath.",
                List.of("Improves flexibility", "Reduces stress"),
                List.of("Focus on breath coordination", "Don't force poses", "Use modifications as needed")
        );

        createExercise(
                "Mobility Routine",
                "flexibility",
                "15 mins",
                "None",
                "Beginner",
                "80-150/hr",
                "🤸‍♂️",
                "Dynamic movements to improve joint range of motion.",
                List.of("Better movement patterns", "Injury prevention"),
                List.of("Move through full range slowly", "Do before and after workouts", "Focus on problem areas")
        );

        createExercise(
                "Recovery Stretching",
                "flexibility",
                "20 mins",
                "Foam roller",
                "Beginner",
                "50-120/hr",
                "😌",
                "Gentle stretches for deep relaxation and recovery.",
                List.of("Aids muscle recovery", "Better sleep"),
                List.of("Hold stretches 2-5 minutes", "Use props for support", "Create calm environment")
        );
    }

    private void loadSportSpecificExercises() {
        createExercise(
                "Agility Drills",
                "sport-specific",
                "20 mins",
                "Cones",
                "Intermediate",
                "250-400/hr",
                "⚡",
                "Quick movement drills for athletic performance and coordination.",
                List.of("Improves reaction time", "Sport performance"),
                List.of("Focus on quick feet", "Keep center of gravity low", "Practice direction changes")
        );

        createExercise(
                "Plyometric Power",
                "sport-specific",
                "25 mins",
                "None",
                "Advanced",
                "300-500/hr",
                "💥",
                "Jump training for explosive power and athletic performance.",
                List.of("Explosive power", "Vertical jump"),
                List.of("Land softly", "Quality over quantity", "Full recovery between sets")
        );

        createExercise(
                "Sprint Training",
                "sport-specific",
                "30 mins",
                "Track/field",
                "Intermediate",
                "400-700/hr",
                "🏃‍♀️",
                "High-intensity sprint intervals for speed and power development.",
                List.of("Speed development", "Anaerobic power"),
                List.of("Warm up thoroughly", "Focus on proper form", "Allow full recovery")
        );
    }

    private void loadRecoveryExercises() {
        createExercise(
                "Active Recovery Walk",
                "recovery",
                "30-45 mins",
                "None",
                "Beginner",
                "150-250/hr",
                "🚶‍♂️",
                "Gentle walking to promote recovery and reduce muscle stiffness.",
                List.of("Promotes recovery", "Mental clarity"),
                List.of("Keep it light and easy", "Enjoy nature", "Focus on breathing")
        );

        createExercise(
                "Meditation & Breathwork",
                "recovery",
                "10-20 mins",
                "None",
                "Beginner",
                "20-50/hr",
                "🧠",
                "Mindfulness practice for mental recovery and stress management.",
                List.of("Stress reduction", "Mental recovery"),
                List.of("Find quiet space", "Start with guided sessions", "Be patient with yourself")
        );

        createExercise(
                "Foam Rolling Session",
                "recovery",
                "15 mins",
                "Foam roller",
                "Beginner",
                "50-100/hr",
                "🔄",
                "Self-massage technique for muscle recovery and tension relief.",
                List.of("Reduces muscle tension", "Improves circulation"),
                List.of("Roll slowly", "Breathe through discomfort", "Focus on tight areas")
        );
    }

    private void createExercise(String name, String goal, String duration, String equipment,
                                String difficulty, String calories, String emoji, String description,
                                List<String> benefits, List<String> tips) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setGoal(goal);
        exercise.setDuration(duration);
        exercise.setEquipment(equipment);
        exercise.setDifficulty(difficulty);
        exercise.setCalories(calories);
        exercise.setEmoji(emoji);
        exercise.setDescription(description);
        exercise.setBenefits(benefits);
        exercise.setTips(tips);
        exercise.setPublished(true);

        exerciseRepository.save(exercise);
        log.debug("Created exercise: {}", name);
    }
}