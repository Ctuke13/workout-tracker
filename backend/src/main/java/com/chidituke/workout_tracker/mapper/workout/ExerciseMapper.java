package com.chidituke.workout_tracker.mapper.workout;

import com.chidituke.workout_tracker.dto.request.exercise.ExerciseCreateRequestDTO;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.model.workout.ScheduledWorkout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ExerciseMapper with Exercise Type Detection for ScheduledWorkout
 * <p>
 * Centralized exercise mapping logic that provides:
 * - Exercise entity creation and updates ✅
 * - Exercise type detection (cardio/isometric/strength) ✅
 * - Exercise data extraction for ScheduledWorkoutResponse ✅
 * - Workout tracking mode determination ✅
 * - Duration estimation and configuration helpers ✅
 */
@Service
@Component
public class ExerciseMapper {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseMapper.class);

    // =============================================================================
    //  CORE MAPPING METHODS
    // =============================================================================

    /**
     * Extract exercise information for ScheduledWorkoutResponse
     * This is the key method that provides exercise data to the frontend!
     */
    public ScheduledWorkoutResponse.ExerciseInfo mapExerciseToResponseInfo(Exercise exercise) {
        if (exercise == null) {
            logger.warn("⚠️ Attempted to map null exercise to response info");
            return null;
        }

        logger.debug("🔍 Mapping exercise {} to response info", exercise.getExerciseName());

        try {
            return ScheduledWorkoutResponse.ExerciseInfo.builder()
                    .id(exercise.getId())
                    .name(exercise.getExerciseName())
                    .emoji(exercise.getEmoji())
                    .description(exercise.getDescription())
                    .exerciseType(exercise.getExerciseType() != null ? exercise.getExerciseType().name() : null)
                    .difficultyLevel(exercise.getDifficultyLevel() != null ? exercise.getDifficultyLevel().name() : null)
                    .estimatedDurationMinutes(exercise.getEstimatedDurationMinutes())
                    .estimatedCalories(exercise.getEstimatedCalories())
                    .targetMuscleGroups(exercise.getTargetMuscleGroups())
                    .equipmentRequired(exercise.getEquipmentRequired())
                    .videoUrl(exercise.getVideoUrl())
                    .benefits(exercise.getBenefits())
                    .tips(exercise.getTips())

                    // These are the flags that fix the frontend exercise type detection!
                    .isCardio(exercise.getIsCardio())
                    .isIsometric(exercise.getIsIsometric())
                    .workoutTrackingMode(exercise.getWorkoutTrackingMode() != null ?
                            exercise.getWorkoutTrackingMode().name() : null)

                    // Additional metadata
                    .averageRating(exercise.getAverageRating())
                    .totalRatings(exercise.getTotalRatings())
                    .usageCount(exercise.getUsageCount())
                    .isFromVerifiedSource(exercise.isFromVerifiedSource())
                    .build();

        } catch (Exception e) {
            logger.error("❌ Failed to map exercise {} to response info: {}",
                    exercise.getId(), e.getMessage(), e);
            return createFallbackExerciseInfo(exercise);
        }
    }

    /**
     * Get exercise from ScheduledWorkout with proper resolution
     * Handles both direct exercise references and workout plan exercises via PlanExercise
     */
    public Exercise extractExerciseFromScheduledWorkout(ScheduledWorkout scheduledWorkout) {
        if (scheduledWorkout == null) {
            logger.warn("⚠️ Cannot extract exercise from null scheduled workout");
            return null;
        }

        try {
            // Use the getResolvedExercise() method we added to ScheduledWorkout entity
            Exercise exercise = scheduledWorkout.getResolvedExercise();

            if (exercise != null) {
                logger.debug("✅ Successfully resolved exercise: {} for scheduled workout: {}",
                        exercise.getExerciseName(), scheduledWorkout.getId());
                return exercise;
            } else {
                logger.warn("⚠️ No exercise found for scheduled workout: {}", scheduledWorkout.getId());
                return null;
            }

        } catch (Exception e) {
            logger.error("❌ Failed to extract exercise from scheduled workout {}: {}",
                    scheduledWorkout.getId(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Maps ExerciseCreateRequestDTO to Exercise entity
     */
    public void mapRequestToEntity(ExerciseCreateRequestDTO request, Exercise exercise) {
        try {
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

            // ✅ Auto-correct any inconsistencies
            autoCorrectExerciseModality(exercise);

        } catch (Exception e) {
            logger.error("❌ Failed to map request to exercise entity: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to map exercise request: " + e.getMessage(), e);
        }
    }

    // =============================================================================
    // ✅ VALIDATION METHODS
    // =============================================================================

    /**
     * Validate exercise type consistency with explicit strength validation
     * Ensures that exercises have consistent cardio/isometric/strength classification
     */
    public boolean validateExerciseTypeConsistency(Exercise exercise) {
        if (exercise == null) {
            logger.warn("❌ Cannot validate null exercise");
            return false;
        }

        boolean isCardio = exercise.getIsCardio() != null && exercise.getIsCardio();
        boolean isIsometric = exercise.getIsIsometric() != null && exercise.getIsIsometric();
        boolean isStrength = !isCardio && !isIsometric; // Derived strength flag

        // 1. Check for mutual exclusivity (can't be multiple types)
        if (isCardio && isIsometric) {
            logger.warn("❌ Exercise {} cannot be both cardio AND isometric", exercise.getId());
            return false;
        }

        // 2. Validate that exercise has exactly one type
        int typeCount = (isCardio ? 1 : 0) + (isIsometric ? 1 : 0) + (isStrength ? 1 : 0);
        if (typeCount != 1) {
            logger.warn("❌ Exercise {} must be exactly one type. Current: cardio={}, isometric={}, strength={}",
                    exercise.getId(), isCardio, isIsometric, isStrength);
            return false;
        }

        // 3. Validate tracking mode consistency
        Exercise.WorkoutTrackingMode expectedMode = determineOptimalTrackingMode(exercise);
        Exercise.WorkoutTrackingMode actualMode = exercise.getWorkoutTrackingMode();

        if (actualMode != null && !actualMode.equals(expectedMode)) {
            logger.warn("⚠️ Exercise {} has inconsistent tracking mode. Expected: {}, Actual: {} (cardio={}, isometric={}, strength={})",
                    exercise.getId(), expectedMode, actualMode, isCardio, isIsometric, isStrength);
            return false;
        }

        // 4. Validate ExerciseType enum consistency
        if (exercise.getExerciseType() != null) {
            boolean exerciseTypeConsistent = validateExerciseTypeEnumConsistency(exercise, isCardio, isIsometric, isStrength);
            if (!exerciseTypeConsistent) {
                return false;
            }
        }

        logger.debug("✅ Exercise {} type validation passed: cardio={}, isometric={}, strength={}",
                exercise.getId(), isCardio, isIsometric, isStrength);
        return true;
    }

    /**
     * Validate that ExerciseType enum matches boolean flags
     */
    private boolean validateExerciseTypeEnumConsistency(Exercise exercise, boolean isCardio, boolean isIsometric, boolean isStrength) {
        Exercise.ExerciseType exerciseType = exercise.getExerciseType();

        // Check cardio consistency
        boolean enumIndicatesCardio = (exerciseType == Exercise.ExerciseType.CARDIO);
        if (isCardio != enumIndicatesCardio) {
            logger.warn("⚠️ Exercise {} cardio flag inconsistency. isCardio={}, ExerciseType={}",
                    exercise.getId(), isCardio, exerciseType);
            return false;
        }

        // Check strength types (most exercise types are strength-based)
        boolean enumIndicatesStrength = (exerciseType == Exercise.ExerciseType.STRENGTH ||
                exerciseType == Exercise.ExerciseType.PLYOMETRIC ||
                exerciseType == Exercise.ExerciseType.SPORTS_SPECIFIC);

        if (isStrength && !enumIndicatesStrength && exerciseType != Exercise.ExerciseType.BALANCE) {
            logger.warn("⚠️ Exercise {} marked as strength but ExerciseType={} doesn't match",
                    exercise.getId(), exerciseType);
        }

        return true;
    }

    /**
     * Check if exercise data is complete and valid
     */
    public boolean isExerciseDataComplete(Exercise exercise) {
        if (exercise == null) {
            return false;
        }

        // Must have basic info
        if (exercise.getExerciseName() == null || exercise.getExerciseName().trim().isEmpty()) {
            return false;
        }

        // Must have valid exercise type
        if (exercise.getExerciseType() == null) {
            return false;
        }

        // Must have consistent modality settings
        return validateExerciseTypeConsistency(exercise);
    }

    // =============================================================================
    // CLASSIFICATION AND ANALYSIS METHODS
    // =============================================================================

    /**
     * Get explicit exercise type classification
     */
    public String getExerciseTypeClassification(Exercise exercise) {
        if (exercise == null) {
            return "unknown";
        }

        boolean isCardio = exercise.getIsCardio() != null && exercise.getIsCardio();
        boolean isIsometric = exercise.getIsIsometric() != null && exercise.getIsIsometric();

        if (isCardio) {
            return "cardio";
        } else if (isIsometric) {
            return "isometric";
        } else {
            return "strength"; // Explicit default
        }
    }

    /**
     * Get exercise type for frontend (string format)
     */
    public String getExerciseTypeForFrontend(Exercise exercise) {
        if (exercise == null) {
            logger.warn("⚠️ Attempted to get exercise type for null exercise");
            return "unknown";  // ✅ Honest about the situation
        }

        if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
            return "cardio";
        }

        if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
            return "isometric";
        }

        return "strength";  // ✅ Valid default for real exercises
    }

    /**
     * Comprehensive exercise type analysis for debugging
     */
    public ExerciseTypeAnalysis analyzeExerciseType(Exercise exercise) {
        if (exercise == null) {
            return new ExerciseTypeAnalysis("null", false, false, false, "invalid", "Exercise is null");
        }

        boolean isCardio = exercise.getIsCardio() != null && exercise.getIsCardio();
        boolean isIsometric = exercise.getIsIsometric() != null && exercise.getIsIsometric();
        boolean isStrength = !isCardio && !isIsometric;

        String classification = getExerciseTypeClassification(exercise);
        boolean isValid = validateExerciseTypeConsistency(exercise);

        StringBuilder issues = new StringBuilder();

        // Check for issues
        if (isCardio && isIsometric) {
            issues.append("Cannot be both cardio and isometric. ");
        }

        if (!isCardio && !isIsometric && !isStrength) {
            issues.append("Must be at least one type. ");
        }

        Exercise.WorkoutTrackingMode expectedMode = determineOptimalTrackingMode(exercise);
        Exercise.WorkoutTrackingMode actualMode = exercise.getWorkoutTrackingMode();
        if (actualMode != null && !actualMode.equals(expectedMode)) {
            issues.append(String.format("Tracking mode mismatch: expected %s, got %s. ", expectedMode, actualMode));
        }

        String issuesText = issues.length() > 0 ? issues.toString().trim() : "No issues found";

        return new ExerciseTypeAnalysis(classification, isCardio, isIsometric, isStrength,
                isValid ? "valid" : "invalid", issuesText);
    }

    /**
     * Get exercise summary for logging/debugging
     */
    public String getExerciseSummary(Exercise exercise) {
        if (exercise == null) {
            return "null exercise";
        }

        return String.format("Exercise[id=%d, name='%s', type=%s, cardio=%s, isometric=%s]",
                exercise.getId(),
                exercise.getExerciseName(),
                exercise.getExerciseType(),
                exercise.getIsCardio(),
                exercise.getIsIsometric());
    }

    // =============================================================================
    //  AUTO-CORRECTION METHODS
    // =============================================================================

    /**
     * Auto-correct exercise modality with strength validation
     */
    public void autoCorrectExerciseModality(Exercise exercise) {
        if (exercise == null) {
            return;
        }

        boolean isCardio = exercise.getIsCardio() != null && exercise.getIsCardio();
        boolean isIsometric = exercise.getIsIsometric() != null && exercise.getIsIsometric();

        // If both flags are true, prioritize based on ExerciseType
        if (isCardio && isIsometric) {
            logger.warn("🔧 Exercise {} has both cardio and isometric flags. Auto-correcting...", exercise.getId());

            if (exercise.getExerciseType() == Exercise.ExerciseType.CARDIO) {
                exercise.setIsIsometric(false); // Keep cardio, remove isometric
                logger.info("✅ Corrected to cardio exercise based on ExerciseType.CARDIO");
            } else {
                exercise.setIsCardio(false); // Keep isometric, remove cardio
                logger.info("✅ Corrected to isometric exercise");
            }
        }

        // Ensure null flags are set to false for consistency
        if (exercise.getIsCardio() == null) {
            exercise.setIsCardio(false);
        }
        if (exercise.getIsIsometric() == null) {
            exercise.setIsIsometric(false);
        }

        // Log final classification
        String finalType = getExerciseTypeClassification(exercise);
        logger.debug("🔧 Auto-corrected exercise {} to type: {}", exercise.getId(), finalType);
    }

    // =============================================================================
    //  UTILITY AND HELPER METHODS
    // =============================================================================

    /**
     * Determine optimal tracking mode for an exercise
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
     * Suggest exercise duration based on tracking mode
     */
    public Integer suggestExerciseDuration(Exercise exercise) {
        Exercise.WorkoutTrackingMode mode = exercise.getWorkoutTrackingMode();

        return switch (mode) {
            case TIME_BASED -> 20; // 20 minutes for cardio
            case HOLD_BASED -> 5;  // 5 minutes for isometric holds
            case REP_BASED -> 10;  // 10 minutes for strength exercises
        };
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
     * Helper method to auto-determine if exercise is isometric
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
                "wall push", "tree pose", "warrior pose", "mountain pose", "l-sit"
        );

        return isometricPatterns.stream()
                .anyMatch(pattern -> name.contains(pattern));
    }

    /**
     * Create fallback exercise info when mapping fails
     */
    private ScheduledWorkoutResponse.ExerciseInfo createFallbackExerciseInfo(Exercise exercise) {
        return ScheduledWorkoutResponse.ExerciseInfo.builder()
                .id(exercise.getId())
                .name(exercise.getExerciseName() != null ? exercise.getExerciseName() : "Unknown Exercise")
                .emoji("💪")
                .description("Exercise details unavailable")
                .exerciseType("STRENGTH") // Safe default
                .difficultyLevel("INTERMEDIATE") // Safe default
                .isCardio(false) // Safe default
                .isIsometric(false) // Safe default
                .workoutTrackingMode("REP_BASED") // Safe default
                .isFromVerifiedSource(false)
                .build();
    }

    // =============================================================================
    // DATA CLASSES
    // =============================================================================

    /**
     * Data class for exercise type analysis results
     */
    public static class ExerciseTypeAnalysis {
        public final String classification;  // "cardio", "isometric", "strength"
        public final boolean isCardio;
        public final boolean isIsometric;
        public final boolean isStrength;
        public final String validationStatus; // "valid", "invalid"
        public final String issues;

        public ExerciseTypeAnalysis(String classification, boolean isCardio, boolean isIsometric,
                                    boolean isStrength, String validationStatus, String issues) {
            this.classification = classification;
            this.isCardio = isCardio;
            this.isIsometric = isIsometric;
            this.isStrength = isStrength;
            this.validationStatus = validationStatus;
            this.issues = issues;
        }

        @Override
        public String toString() {
            return String.format("ExerciseType[%s: cardio=%s, isometric=%s, strength=%s, status=%s, issues=%s]",
                    classification, isCardio, isIsometric, isStrength, validationStatus, issues);
        }
    }
}