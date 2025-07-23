package com.chidituke.workout_tracker.mapper.workout;

import com.chidituke.workout_tracker.dto.request.exercise.ExerciseCreateRequestDTO;
import com.chidituke.workout_tracker.model.workout.Exercise;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExerciseMapper {

    /**
     * Maps ExerciseCreateRequestDTO to Exercise entity
     */
    public void mapRequestToEntity(ExerciseCreateRequestDTO request, Exercise exercise) {
        exercise.setExerciseName(request.getName());
        exercise.setEmoji(request.getEmoji());
        exercise.setDescription(request.getDescription());
        exercise.setExerciseType(request.getExerciseType());
        exercise.setDifficultyLevel(request.getDifficultyLevel());
        exercise.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        exercise.setEstimatedCalories(request.getEstimatedCalories());
        exercise.setVideoUrl(request.getVideoUrl());

        // Handle list fields
        if (request.getTargetMuscleGroups() != null) {
            exercise.setTargetMuscleGroups(new ArrayList<>(request.getTargetMuscleGroups()));
        }
        if (request.getEquipmentRequired() != null) {
            exercise.setEquipmentRequired(new ArrayList<>(request.getEquipmentRequired()));
        }
        if (request.getBenefits() != null) {
            exercise.setBenefits(new ArrayList<>(request.getBenefits()));
        }
        if (request.getTips() != null) {
            exercise.setTips(new ArrayList<>(request.getTips()));
        }

        // 🆕 Handle isIsometric field
        if (request.getIsIsometric() != null) {
            exercise.setIsIsometric(request.getIsIsometric());
        } else {
            // Auto-determine if not explicitly set
            exercise.setIsIsometric(determineIfIsometric(request.getName(), request.getExerciseType()));
        }
    }

    /**
     * 🆕 Helper method to auto-determine if exercise is isometric
     */
    private boolean determineIfIsometric(String exerciseName, Exercise.ExerciseType exerciseType) {
        if (exerciseName == null) {
            return false;
        }

        String name = exerciseName.toLowerCase();

        // List of isometric exercise patterns
        List<String> isometricPatterns = List.of(
                "plank", "wall sit", "dead hang", "bridge hold", "static hold",
                "isometric", "hold", "static", "wall squat", "glute bridge",
                "wall push", "tree pose", "warrior pose", "mountain pose"
        );

        return isometricPatterns.stream()
                .anyMatch(pattern -> name.contains(pattern));
    }

    /**
     * Helper method to calculate relevance score
     */
    public double calculateRelevanceScore(Exercise exercise) {
        double score = 0.0;

        // Rating weight (40%)
        if (exercise.getAverageRating() != null && exercise.getTotalRatings() > 0) {
            score += exercise.getAverageRating() * 0.4;
        }

        // Popularity weight (30%)
        if (exercise.getUsageCount() != null) {
            score += Math.min(exercise.getUsageCount() / 1000.0, 1.0) * 0.3;
        }

        // Professional content weight (20%)
        if (exercise.isFromVerifiedSource()) {
            score += 0.2;
        }

        // Recency weight (10%)
        if (exercise.getCreatedAt() != null) {
            long daysSinceCreation = java.time.Duration.between(
                    exercise.getCreatedAt(), LocalDateTime.now()).toDays();
            if (daysSinceCreation < 30) {
                score += 0.1 * (30 - daysSinceCreation) / 30.0;
            }
        }

        return score;
    }

    /**
     * 🆕 Helper method to determine optimal tracking mode for an exercise
     */
    public Exercise.WorkoutTrackingMode determineOptimalTrackingMode(Exercise exercise) {
        if (exercise.getIsCardio()) {
            return Exercise.WorkoutTrackingMode.TIME_BASED;
        }
        if (exercise.getIsIsometric()) {
            return Exercise.WorkoutTrackingMode.HOLD_BASED;
        }
        return Exercise.WorkoutTrackingMode.REP_BASED;
    }

    /**
     * 🆕 Helper method to suggest exercise duration based on tracking mode
     */
    public Integer suggestExerciseDuration(Exercise exercise) {
        Exercise.WorkoutTrackingMode mode = exercise.getWorkoutTrackingMode();

        return switch (mode) {
            case TIME_BASED -> 20; // 20 minutes for cardio
            case HOLD_BASED -> 5;  // 5 minutes for isometric holds
            case REP_BASED -> 10;  // 10 minutes for strength exercises
        };
    }
}