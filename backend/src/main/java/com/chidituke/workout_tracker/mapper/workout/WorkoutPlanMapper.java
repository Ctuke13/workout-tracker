package com.chidituke.workout_tracker.mapper.workout;

import com.chidituke.workout_tracker.dto.request.workout_plan.WorkoutPlanRequest;
import com.chidituke.workout_tracker.dto.response.workout_plan.WorkoutPlanResponse;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WorkoutPlanMapper {

    public WorkoutPlanResponse toResponse(WorkoutPlan entity) {
        if (entity == null) {
            return null;
        }

        return WorkoutPlanResponse.builder()
                .id(entity.getId())
                .workoutName(entity.getWorkoutName())
                .workoutDescription(entity.getWorkoutDescription())
                .workoutCategory(entity.getWorkoutCategory())
                .workoutImageUrl(entity.getWorkoutImageUrl())
                .isCardio(entity.isCardio())
                .workoutType(entity.getWorkoutType() != null ? entity.getWorkoutType().name() : null)
                .estimatedDurationMinutes(entity.getEstimatedDurationMinutes())
                .difficultyLevel(entity.getDifficultyLevel() != null ? entity.getDifficultyLevel().name() : null)
                .targetMuscleGroups(entity.getTargetMuscleGroups())
                .equipmentNeeded(entity.getEquipmentNeeded())
                .subscriptionTierRequired(entity.getSubscriptionTierRequired())
                .createdByUserId(entity.getCreatedByUserId())
                .isPublic(entity.getIsPublic())
                .timesUsed(entity.getTimesUsed())
                .averageRating(entity.getAverageRating())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<WorkoutPlanResponse> toResponseList(List<WorkoutPlan> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void mapRequestToEntity(WorkoutPlanRequest request, WorkoutPlan entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setWorkoutName(request.getWorkoutName());
        entity.setWorkoutDescription(request.getWorkoutDescription());
        entity.setWorkoutCategory(request.getWorkoutCategory());
        entity.setWorkoutImageUrl(request.getWorkoutImageUrl());
        entity.setCardio(request.getIsCardio() != null ? request.getIsCardio() : false);
        entity.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        entity.setTargetMuscleGroups(request.getTargetMuscleGroups());
        entity.setEquipmentNeeded(request.getEquipmentNeeded());
        entity.setSubscriptionTierRequired(request.getSubscriptionTierRequired());
        entity.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);

        // Convert workout type string to enum
        if (request.getWorkoutType() != null) {
            try {
                entity.setWorkoutType(WorkoutPlan.WorkoutType.valueOf(request.getWorkoutType().toUpperCase()));
            } catch (IllegalArgumentException e) {
                entity.setWorkoutType(WorkoutPlan.WorkoutType.STRENGTH);
            }
        }

        // Convert difficulty level string to enum
        if (request.getDifficultyLevel() != null) {
            try {
                entity.setDifficultyLevel(WorkoutPlan.DifficultyLevel.valueOf(request.getDifficultyLevel().toUpperCase()));
            } catch (IllegalArgumentException e) {
                entity.setDifficultyLevel(WorkoutPlan.DifficultyLevel.BEGINNER);
            }
        }
    }
}