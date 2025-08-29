package com.chidituke.workout_tracker.mapper.workout;

import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.model.workout.ScheduledWorkout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ScheduledWorkoutMapper with Exercise Type Detection Support
 * <p>
 * Maps ScheduledWorkout entities to ScheduledWorkoutResponse DTOs with:
 * - Complete exercise information with proper type flags ✅
 * - Exercise configuration fields for all workout types ✅
 * - Robust error handling and fallback mechanisms ✅
 * - Comprehensive logging for debugging ✅
 */
@Component
public class ScheduledWorkoutMapper {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledWorkoutMapper.class);

    // Inject ExerciseMapper for exercise data resolution
    @Autowired
    private ExerciseMapper exerciseMapper;

    /**
     * Map ScheduledWorkout entity to ScheduledWorkoutResponse DTO
     */
    public ScheduledWorkoutResponse toResponse(ScheduledWorkout entity) {
        if (entity == null) {
            return null;
        }

        logger.debug("🔄 Mapping ScheduledWorkout {} to response", entity.getId());

        try {
            ScheduledWorkoutResponse.ScheduledWorkoutResponseBuilder builder = ScheduledWorkoutResponse.builder()
                    .id(entity.getId())
                    .scheduledDate(entity.getScheduledDate())
                    .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                    .weekNumber(entity.getWeekNumber())
                    .dayOfWeek(entity.getDayOfWeek())
                    .customNotes(entity.getCustomNotes())
                    .reminderTime(entity.getReminderTime())
                    .estimatedDurationMinutes(entity.getEstimatedDurationMinutes())
                    .completedAt(entity.getCompletedAt())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .createdByUserId(entity.getCreatedByUserId())

                    //  Exercise configuration fields (all workout types)
                    .targetSets(entity.getTargetSets())
                    .targetReps(entity.getTargetReps())
                    .targetWeight(entity.getTargetWeight())
                    .targetWeightUnit(entity.getTargetWeightUnit())
                    .restSeconds(entity.getRestSeconds())
                    .tempo(entity.getTempo())
                    .targetRpe(entity.getTargetRpe())
                    .targetDurationMinutes(entity.getTargetDurationMinutes())
                    .targetDistanceKm(entity.getTargetDistanceKm())
                    .targetPace(entity.getTargetPace())
                    .holdDurationSeconds(entity.getHoldDurationSeconds())

                    //  Map related entities
                    .workoutPlan(mapWorkoutPlanInfo(entity))
                    .user(mapUserInfo(entity))
                    .program(mapWorkoutProgramInfo(entity))
                    .completedSession(mapWorkoutSessionInfo(entity));

            // Map exercise information with proper error handling
            try {
                ScheduledWorkoutResponse.ExerciseInfo exerciseInfo = mapExerciseInfo(entity);
                builder.exercise(exerciseInfo);

                if (exerciseInfo != null) {
                    logger.debug("✅ Successfully mapped exercise info: {} (cardio: {}, isometric: {})",
                            exerciseInfo.getName(),
                            exerciseInfo.getIsCardio(),
                            exerciseInfo.getIsIsometric());
                } else {
                    logger.warn("⚠️ No exercise info mapped for scheduled workout {}", entity.getId());
                }

            } catch (Exception e) {
                logger.error("❌ Failed to map exercise info for scheduled workout {}: {}",
                        entity.getId(), e.getMessage(), e);
                // Continue mapping without exercise info rather than failing completely
                builder.exercise(createFallbackExerciseInfo(entity));
            }

            ScheduledWorkoutResponse response = builder.build();

            logger.debug("✅ Successfully mapped ScheduledWorkout {} to response", entity.getId());
            return response;

        } catch (Exception e) {
            logger.error("❌ Failed to map ScheduledWorkout {} to response: {}",
                    entity.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to map scheduled workout: " + e.getMessage(), e);
        }
    }

    /**
     * Map list of entities to list of responses
     */
    public List<ScheduledWorkoutResponse> toResponseList(List<ScheduledWorkout> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // =============================================================================
    //  EXERCISE MAPPING METHODS
    // =============================================================================

    /**
     * CORE: Map exercise information using ExerciseMapper
     * This is where the magic happens - exercise type detection!
     */
    private ScheduledWorkoutResponse.ExerciseInfo mapExerciseInfo(ScheduledWorkout entity) {
        try {
            // Use ExerciseMapper to extract and map exercise data
            Exercise exercise = exerciseMapper.extractExerciseFromScheduledWorkout(entity);

            if (exercise != null) {
                // Validate and auto-correct exercise data
                if (!exerciseMapper.validateExerciseTypeConsistency(exercise)) {
                    logger.warn("⚠️ Auto-correcting inconsistent exercise type for exercise {}", exercise.getId());
                    exerciseMapper.autoCorrectExerciseModality(exercise);
                }

                // Map to response format
                ScheduledWorkoutResponse.ExerciseInfo exerciseInfo = exerciseMapper.mapExerciseToResponseInfo(exercise);

                logger.debug("🔍 Mapped exercise {} with flags: cardio={}, isometric={}, trackingMode={}",
                        exercise.getExerciseName(),
                        exerciseInfo != null ? exerciseInfo.getIsCardio() : "null",
                        exerciseInfo != null ? exerciseInfo.getIsIsometric() : "null",
                        exerciseInfo != null ? exerciseInfo.getWorkoutTrackingMode() : "null");

                return exerciseInfo;
            } else {
                logger.debug("⚠️ No exercise found for scheduled workout {}, creating fallback", entity.getId());
                return createFallbackExerciseInfo(entity);
            }

        } catch (Exception e) {
            logger.error("❌ Error mapping exercise info for scheduled workout {}: {}",
                    entity.getId(), e.getMessage(), e);
            return createFallbackExerciseInfo(entity);
        }
    }

    /**
     * Create fallback exercise info when no exercise is found
     */
    private ScheduledWorkoutResponse.ExerciseInfo createFallbackExerciseInfo(ScheduledWorkout entity) {
        String exerciseName = "Unknown Exercise";
        String exerciseType = "STRENGTH";
        boolean isCardio = false;
        boolean isIsometric = false;
        String trackingMode = "REP_BASED";

        // Try to get info from workout plan
        if (entity.getWorkoutPlan() != null) {
            exerciseName = entity.getWorkoutPlan().getWorkoutName();

            // Determine type from scheduled workout's resolved methods
            if (entity.isCardioWorkout()) {
                exerciseType = "CARDIO";
                isCardio = true;
                trackingMode = "TIME_BASED";
            } else if (entity.isIsometricWorkout()) {
                exerciseType = "BALANCE";
                isIsometric = true;
                trackingMode = "HOLD_BASED";
            }
        }

        logger.debug("🔧 Created fallback exercise info: name='{}', cardio={}, isometric={}",
                exerciseName, isCardio, isIsometric);

        return ScheduledWorkoutResponse.ExerciseInfo.builder()
                .id(null)
                .name(exerciseName)
                .emoji("💪")
                .description("Exercise from workout plan")
                .exerciseType(exerciseType)
                .difficultyLevel("INTERMEDIATE")
                .isCardio(isCardio)
                .isIsometric(isIsometric)
                .workoutTrackingMode(trackingMode)
                .isFromVerifiedSource(false)
                .build();
    }

    // =============================================================================
    //  RELATED ENTITY MAPPING METHODS (Existing - Keep as-is)
    // =============================================================================

    /**
     * Map WorkoutPlan information
     */
    private ScheduledWorkoutResponse.WorkoutPlanInfo mapWorkoutPlanInfo(ScheduledWorkout entity) {
        if (entity.getWorkoutPlan() == null) {
            return null;
        }

        return ScheduledWorkoutResponse.WorkoutPlanInfo.builder()
                .id(entity.getWorkoutPlan().getId())
                .name(entity.getWorkoutPlan().getWorkoutName())
                .description(entity.getWorkoutPlan().getWorkoutDescription())
                .difficulty(entity.getWorkoutPlan().getDifficultyLevel() != null ?
                        entity.getWorkoutPlan().getDifficultyLevel().name() : null)
                .estimatedDurationMinutes(entity.getWorkoutPlan().getEstimatedDurationMinutes())
                .category(entity.getWorkoutPlan().getWorkoutCategory())
                .imageUrl(entity.getWorkoutPlan().getWorkoutImageUrl())
                .isPublic(entity.getWorkoutPlan().getIsPublic())
                .build();
    }

    /**
     * Map User information
     */
    private ScheduledWorkoutResponse.UserInfo mapUserInfo(ScheduledWorkout entity) {
        if (entity.getUser() == null) {
            return null;
        }

        return ScheduledWorkoutResponse.UserInfo.builder()
                .id(entity.getUser().getId())
                .username(entity.getUser().getUsername())
                .email(entity.getUser().getEmail())
                .firstName(entity.getUser().getFirstName())
                .lastName(entity.getUser().getLastName())
                .subscriptionTier(entity.getUser().getSubscriptionTier() != null ?
                        entity.getUser().getSubscriptionTier().name() : null)
                .build();
    }

    /**
     * Map WorkoutProgram information
     */
    private ScheduledWorkoutResponse.WorkoutProgramInfo mapWorkoutProgramInfo(ScheduledWorkout entity) {
        if (entity.getProgram() == null) {
            return null;
        }

        return ScheduledWorkoutResponse.WorkoutProgramInfo.builder()
                .id(entity.getProgram().getId())
                .name(entity.getProgram().getName())
                .description(entity.getProgram().getDescription())
                .totalWeeks(entity.getProgram().getDurationWeeks())
                .difficulty(entity.getProgram().getDifficultyLevel() != null ?
                        entity.getProgram().getDifficultyLevel().name() : null)
                .category(entity.getProgram().getProgramType() != null ?
                        entity.getProgram().getProgramType().getDisplayName() : null)
                .imageUrl(null) // WorkoutProgram doesn't have imageUrl field
                .isActive(entity.getProgram().isActive()) // Use isActive() method, not getIsActive()
                .build();
    }

    /**
     * Map WorkoutSession information
     */
    private ScheduledWorkoutResponse.WorkoutSessionInfo mapWorkoutSessionInfo(ScheduledWorkout entity) {
        if (entity.getCompletedSession() == null) {
            return null;
        }

        return ScheduledWorkoutResponse.WorkoutSessionInfo.builder()
                .id(entity.getCompletedSession().getId())
                .startTime(entity.getCompletedSession().getCreatedAt()) // Assuming this is start time
                .endTime(entity.getCompletedSession().getUpdatedAt()) // Assuming this is end time
                .actualDurationMinutes(entity.getCompletedSession().getTotalDurationMinutes())
                .notes(entity.getCompletedSession().getNotes())
                .completed(true) // If it exists, it's completed
                .build();
    }

    // =============================================================================
    // ✅ UTILITY METHODS
    // =============================================================================

    /**
     * Convert day of week integer to name
     */
    private String dayOfWeekToName(Integer dayOfWeek) {
        if (dayOfWeek == null) {
            return null;
        }
        try {
            return DayOfWeek.of(dayOfWeek).name();
        } catch (Exception e) {
            return null;
        }
    }
}