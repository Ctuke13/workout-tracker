package com.chidituke.workout_tracker.mapper.workout;

import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.service.workout.ExerciseService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
public class ExerciseMapper {

    /**
     * Maps ExerciseCreationRequest DTO to Exercise entity
     */
    public void mapRequestToEntity(ExerciseService.ExerciseCreationRequest request, Exercise exercise) {
        exercise.setExerciseName(request.getName());
        exercise.setDescription(request.getDescription());
        exercise.setExerciseType(request.getExerciseType());
        exercise.setDifficultyLevel(request.getDifficultyLevel());

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

        exercise.setVideoUrl(request.getVideoUrl());
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
}