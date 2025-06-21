package com.chidituke.workout_tracker.mapper;

import com.chidituke.workout_tracker.dto.request.plan_exercise.PlanExerciseRequest;
import com.chidituke.workout_tracker.dto.response.plan_exercise.PlanExerciseResponse;
import com.chidituke.workout_tracker.model.PlanExercise;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlanExerciseMapper {

    public PlanExerciseResponse toResponse(PlanExercise entity) {
        if (entity == null) {
            return null;
        }

        return PlanExerciseResponse.builder()
                .id(entity.getId())
                .orderInWorkout(entity.getOrderInWorkout())
                .exerciseId(entity.getExercise() != null ? entity.getExercise().getId() : null)
                .exerciseName(entity.getExercise() != null ? entity.getExercise().getExerciseName() : null)
                .exerciseDescription(entity.getExercise() != null ? entity.getExercise().getDescription() : null)
                .exerciseType(entity.getExercise() != null && entity.getExercise().getExerciseType() != null ?
                        entity.getExercise().getExerciseType().name() : null)
                .exerciseDifficulty(entity.getExercise() != null && entity.getExercise().getDifficultyLevel() != null ?
                        entity.getExercise().getDifficultyLevel().name() : null)
                .exerciseImageUrl(entity.getExercise() != null ? entity.getExercise().getVideoUrl() : null)
                .prescribedSets(entity.getPrescribedSets())
                .prescribedReps(entity.getPrescribedReps())
                .prescribedWeightPercent(entity.getPrescribedWeightPercent())
                .prescribedRestSeconds(entity.getPrescribedRestSeconds())
                .prescribedTempo(entity.getPrescribedTempo())
                .prescribedRpe(entity.getPrescribedRpe())
                .formattedPrescription(entity.getFormattedPrescription())
                .instructions(entity.getInstructions())
                .coachingCues(entity.getCoachingCues())
                .modificationNotes(entity.getModificationNotes())
                .alternativeExerciseId(entity.getAlternativeExercise() != null ? entity.getAlternativeExercise().getId() : null)
                .alternativeExerciseName(entity.getAlternativeExercise() != null ? entity.getAlternativeExercise().getExerciseName() : null)
                .hasAlternative(entity.hasAlternative())
                .isProgressionExercise(entity.getIsProgressionExercise())
                .progressionGoal(entity.getProgressionGoal())
                .isOptional(entity.getIsOptional())
                .isSuperset(entity.getIsSuperset())
                .supersetGroup(entity.getSupersetGroup())
                .equipmentAlternatives(entity.getEquipmentAlternatives())
                .subscriptionTierRequired(entity.getSubscriptionTierRequired())
                .createdByUserId(entity.getCreatedByUserId())
                .isUserCustomization(entity.getIsUserCustomization())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .displayOrder(entity.getDisplayOrder())
                .isAccessibleToCurrentUser(true) // Will be set by service based on user context
                .build();
    }

    public List<PlanExerciseResponse> toResponseList(List<PlanExercise> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void mapRequestToEntity(PlanExerciseRequest request, PlanExercise entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setOrderInWorkout(request.getOrderInWorkout());
        entity.setPrescribedSets(request.getPrescribedSets());
        entity.setPrescribedReps(request.getPrescribedReps());
        entity.setPrescribedWeightPercent(request.getPrescribedWeightPercent());
        entity.setPrescribedRestSeconds(request.getPrescribedRestSeconds());
        entity.setPrescribedTempo(request.getPrescribedTempo());
        entity.setPrescribedRpe(request.getPrescribedRpe());
        entity.setInstructions(request.getInstructions());
        entity.setCoachingCues(request.getCoachingCues());
        entity.setModificationNotes(request.getModificationNotes());
        entity.setIsProgressionExercise(request.getIsProgressionExercise() != null ? request.getIsProgressionExercise() : false);
        entity.setProgressionGoal(request.getProgressionGoal());
        entity.setIsOptional(request.getIsOptional() != null ? request.getIsOptional() : false);
        entity.setIsSuperset(request.getIsSuperset() != null ? request.getIsSuperset() : false);
        entity.setSupersetGroup(request.getSupersetGroup());
        entity.setEquipmentAlternatives(request.getEquipmentAlternatives());
        entity.setSubscriptionTierRequired(request.getSubscriptionTierRequired() != null ? request.getSubscriptionTierRequired() : "FREE");

        // Note: Exercise, AlternativeExercise, WorkoutPlan, CreatedByUserId, and IsUserCustomization
        // should be set by the service layer, not by the mapper
    }

    /**
     * Update accessibility flag based on user's subscription tier
     */
    public void setAccessibilityForUser(PlanExerciseResponse response, String userSubscriptionTier) {
        if (response == null) {
            return;
        }

        boolean isAccessible = true;
        String requiredTier = response.getSubscriptionTierRequired();

        if ("PLUS".equals(requiredTier) && "FREE".equals(userSubscriptionTier)) {
            isAccessible = false;
        } else if ("PRO".equals(requiredTier) && ("FREE".equals(userSubscriptionTier) || "PLUS".equals(userSubscriptionTier))) {
            isAccessible = false;
        }

        response.setIsAccessibleToCurrentUser(isAccessible);
    }

    /**
     * Update accessibility for a list of responses
     */
    public void setAccessibilityForUser(List<PlanExerciseResponse> responses, String userSubscriptionTier) {
        if (responses == null) {
            return;
        }

        responses.forEach(response -> setAccessibilityForUser(response, userSubscriptionTier));
    }
}