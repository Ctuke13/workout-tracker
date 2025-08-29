package com.chidituke.workout_tracker.mapper.workout;

import com.chidituke.workout_tracker.dto.request.performance.PerformanceRequest;
import com.chidituke.workout_tracker.dto.response.performance.PerformanceResponse;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.model.workout.PerformanceRecord;
import com.chidituke.workout_tracker.repository.workout.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Enhanced Performance Mapper with comprehensive field mapping
 * Handles all performance metrics and calculated fields
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PerformanceMapper {

    private final ExerciseRepository exerciseRepository; // ✅ ADDED: To resolve exercise relationship

    // ==============================================
    // REQUEST TO ENTITY MAPPING
    // ==============================================

    /**
     * Maps PerformanceRequest DTO to PerformanceRecord entity
     * Note: WorkoutLog relationship is set separately in the service
     */
    public void mapRequestToEntity(PerformanceRequest request, PerformanceRecord performanceRecord) {
        if (request == null || performanceRecord == null) {
            return;
        }

        // ✅ ADDED: Map exercise relationship
        if (request.getExerciseId() != null) {
            Exercise exercise = exerciseRepository.findById(request.getExerciseId())
                    .orElseThrow(() -> new RuntimeException("Exercise not found: " + request.getExerciseId()));
            performanceRecord.setExercise(exercise);
        }

        // Basic performance metrics
        performanceRecord.setSetNumber(request.getSetNumber());
        performanceRecord.setReps(request.getReps());
        performanceRecord.setWeight(request.getWeight());

        // Cardio metrics
        performanceRecord.setDurationMinutes(request.getDurationMinutes());
        performanceRecord.setDurationSeconds(request.getDurationSeconds());
        performanceRecord.setDistanceKm(request.getDistanceKm());
        performanceRecord.setCaloriesBurned(request.getCaloriesBurned());

        // ✅ ADDED: Advanced performance metrics
        performanceRecord.setPerceivedExertion(request.getPerceivedExertion());
        performanceRecord.setFormRating(request.getFormRating());
        performanceRecord.setRestSeconds(request.getRestSeconds());
        performanceRecord.setTempo(request.getTempo());

        // ✅ ADDED: Specialized exercise metrics
        performanceRecord.setHoldDurationSeconds(request.getHoldDurationSeconds());
        performanceRecord.setBalanceScore(request.getBalanceScore());
        performanceRecord.setJumpHeightCm(request.getJumpHeightCm());
        performanceRecord.setPowerOutputWatts(request.getPowerOutputWatts());

        // ✅ ADDED: Professional training metrics
        performanceRecord.setAssignedByTrainerId(request.getAssignedByTrainerId());
        performanceRecord.setTargetReps(request.getTargetReps());
        performanceRecord.setTargetWeight(request.getTargetWeight());
        performanceRecord.setAchievementStatus(request.getAchievementStatus());

        // ✅ ADDED: Notes and context
        performanceRecord.setNotes(request.getNotes());
        performanceRecord.setEquipmentUsed(request.getEquipmentUsed());
        performanceRecord.setWorkoutEnvironment(request.getWorkoutEnvironment());

        // Audit fields (timestamps handled by JPA lifecycle)
        performanceRecord.setUpdatedAt(LocalDateTime.now());
    }

    // ==============================================
    // ENTITY TO RESPONSE MAPPING
    // ==============================================

    /**
     * Converts PerformanceRecord entity to PerformanceResponse DTO with calculated fields
     */
    public PerformanceResponse mapEntityToResponse(PerformanceRecord performanceRecord) {
        if (performanceRecord == null) {
            return null;
        }

        PerformanceResponse.PerformanceResponseBuilder builder = PerformanceResponse.builder()
                // Core identification
                .id(performanceRecord.getId())
                .workoutSessionId(performanceRecord.getWorkoutSession().getId())
                .setNumber(performanceRecord.getSetNumber())

                // Basic performance metrics
                .reps(performanceRecord.getReps())
                .weight(performanceRecord.getWeight())

                // Cardio metrics
                .durationMinutes(performanceRecord.getDurationMinutes())
                .durationSeconds(performanceRecord.getDurationSeconds())
                .distanceKm(performanceRecord.getDistanceKm())
                .caloriesBurned(performanceRecord.getCaloriesBurned())

                // ✅ ADDED: Advanced performance metrics
                .perceivedExertion(performanceRecord.getPerceivedExertion())
                .formRating(performanceRecord.getFormRating())
                .restSeconds(performanceRecord.getRestSeconds())
                .tempo(performanceRecord.getTempo())

                // ✅ ADDED: Specialized exercise metrics
                .holdDurationSeconds(performanceRecord.getHoldDurationSeconds())
                .balanceScore(performanceRecord.getBalanceScore())
                .jumpHeightCm(performanceRecord.getJumpHeightCm())
                .powerOutputWatts(performanceRecord.getPowerOutputWatts())

                // ✅ ADDED: Professional training context
                .assignedByTrainerId(performanceRecord.getAssignedByTrainerId())
                .targetReps(performanceRecord.getTargetReps())
                .targetWeight(performanceRecord.getTargetWeight())
                .achievementStatus(performanceRecord.getAchievementStatus())

                // ✅ ADDED: Context and notes
                .notes(performanceRecord.getNotes())
                .equipmentUsed(performanceRecord.getEquipmentUsed())
                .workoutEnvironment(performanceRecord.getWorkoutEnvironment())

                // Audit information
                .createdAt(performanceRecord.getCreatedAt())
                .updatedAt(performanceRecord.getUpdatedAt());

        // ✅ FIXED: Map exercise information using correct field names
        if (performanceRecord.getExercise() != null) {
            Exercise exercise = performanceRecord.getExercise();
            builder.exerciseId(exercise.getId())
                    .exerciseName(exercise.getExerciseName()) // : Use getName() not getExerciseName()
                    .exerciseCategory(exercise.getExerciseType() != null ? exercise.getExerciseType().name() : null)
                    .isCardioExercise(exercise.getIsCardio() != null ? exercise.getIsCardio() : false)
                    .isIsometricExercise(exercise.getIsIsometric() != null ? exercise.getIsIsometric() : false);
        }

        // ✅ ADDED: Map workout information
        if (performanceRecord.getWorkoutSession() != null) {
            builder.workoutDate(performanceRecord.getWorkoutSession().getDate());

            if (performanceRecord.getWorkoutSession().getWorkoutPlan() != null) {
                builder.workoutName(performanceRecord.getWorkoutSession().getWorkoutPlan().getWorkoutName())
                        .workoutCategory(performanceRecord.getWorkoutSession().getWorkoutPlan().getWorkoutCategory());
            }
        }

        // ✅ ADDED: Map trainer information if available
        if (performanceRecord.getAssignedByTrainerId() != null) {
            // Note: This would require a UserRepository injection to get trainer name
            // For now, we'll just set the ID and let the frontend handle name resolution
            builder.trainerName("Trainer #" + performanceRecord.getAssignedByTrainerId());
        }

        PerformanceResponse response = builder.build();

        // ✅ ADDED: Calculate derived fields
        calculateDerivedFields(response, performanceRecord);

        return response;
    }

    // ==============================================
    // DERIVED FIELD CALCULATIONS
    // ==============================================

    /**
     * Calculate and set derived fields in the response
     */
    private void calculateDerivedFields(PerformanceResponse response, PerformanceRecord performanceRecord) {

        // Calculate volume (weight × reps)
        if (response.getWeight() != null && response.getReps() != null) {
            response.setVolume(response.getWeight() * response.getReps());
        }

        // Calculate total duration in seconds
        Double totalSeconds = calculateTotalDurationSeconds(response.getDurationMinutes(), response.getDurationSeconds());
        response.setTotalDurationSeconds(totalSeconds);

        // Calculate pace and speed for cardio
        if (response.getDistanceKm() != null && totalSeconds != null && totalSeconds > 0) {
            // Pace in minutes per km
            double paceMinutesPerKm = (totalSeconds / 60.0) / response.getDistanceKm();
            response.setPace(paceMinutesPerKm);

            // Speed in km per hour
            double speedKmPerHour = (response.getDistanceKm() / totalSeconds) * 3600;
            response.setSpeed(speedKmPerHour);
        }

        // Calculate target volume
        if (response.getTargetReps() != null && response.getTargetWeight() != null) {
            response.setTargetVolume(response.getTargetReps() * response.getTargetWeight());
        }

        // Calculate efficiency percentage
        Double efficiencyPercentage = performanceRecord.calculateEfficiency();
        response.setEfficiencyPercentage(efficiencyPercentage);

        // Set intensity level based on perceived exertion
        if (response.getPerceivedExertion() != null) {
            PerformanceRecord.WorkoutIntensity intensity = performanceRecord.getWorkoutIntensity();
            response.setIntensityLevel(intensity.getDisplayName());
        }

        // Calculate performance score
        Double performanceScore = performanceRecord.getPerformanceScore();
        response.setPerformanceScore(performanceScore);

        // Set performance grade based on score
        if (performanceScore != null) {
            response.setPerformanceGrade(calculatePerformanceGrade(performanceScore));
        }

        // Check if targets were exceeded
        response.setExceededTargets(performanceRecord.exceededTargets());

        // Set personal record status (would need historical data comparison)
        response.setIsPersonalRecord(performanceRecord.isPotentialPersonalRecord());
    }

    /**
     * Calculate total duration in seconds from minutes and seconds
     */
    private Double calculateTotalDurationSeconds(Integer durationMinutes, Double durationSeconds) {
        double total = 0.0;
        if (durationMinutes != null) {
            total += durationMinutes * 60.0;
        }
        if (durationSeconds != null) {
            total += durationSeconds;
        }
        return total > 0 ? total : null;
    }

    /**
     * Calculate performance grade based on score
     */
    private String calculatePerformanceGrade(Double score) {
        if (score == null) return "N/A";

        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    // ==============================================
    // BULK MAPPING OPERATIONS
    // ==============================================

    /**
     * Create a minimal response for list operations (performance optimization)
     */
    public PerformanceResponse mapEntityToMinimalResponse(PerformanceRecord performanceRecord) {
        if (performanceRecord == null) {
            return null;
        }

        PerformanceResponse.PerformanceResponseBuilder builder = PerformanceResponse.builder()
                .id(performanceRecord.getId())
                .workoutSessionId(performanceRecord.getWorkoutSession().getId())
                .setNumber(performanceRecord.getSetNumber())
                .reps(performanceRecord.getReps())
                .weight(performanceRecord.getWeight())
                .durationMinutes(performanceRecord.getDurationMinutes())
                .distanceKm(performanceRecord.getDistanceKm())
                .perceivedExertion(performanceRecord.getPerceivedExertion())
                .createdAt(performanceRecord.getCreatedAt());

        // Include exercise name for context
        if (performanceRecord.getExercise() != null) {
            builder.exerciseId(performanceRecord.getExercise().getId())
                    .exerciseName(performanceRecord.getExercise().getExerciseName());
        }

        // Include workout date for context
        if (performanceRecord.getWorkoutSession() != null) {
            builder.workoutDate(performanceRecord.getWorkoutSession().getDate());
        }

        PerformanceResponse response = builder.build();

        // Calculate only essential derived fields for minimal response
        if (response.getWeight() != null && response.getReps() != null) {
            response.setVolume(response.getWeight() * response.getReps());
        }

        return response;
    }

    /**
     * Create a summary response for analytics (even more minimal)
     */
    public PerformanceResponse mapEntityToSummaryResponse(PerformanceRecord performanceRecord) {
        if (performanceRecord == null) {
            return null;
        }

        PerformanceResponse response = PerformanceResponse.builder()
                .id(performanceRecord.getId())
                .exerciseId(performanceRecord.getExercise() != null ? performanceRecord.getExercise().getId() : null)
                .exerciseName(performanceRecord.getExercise() != null ? performanceRecord.getExercise().getExerciseName() : null) // ✅ FIXED
                .workoutDate(performanceRecord.getWorkoutSession() != null ? performanceRecord.getWorkoutSession().getDate() : null)
                .volume(performanceRecord.calculateVolume())
                .perceivedExertion(performanceRecord.getPerceivedExertion())
                .achievementStatus(performanceRecord.getAchievementStatus())
                .build();

        return response;
    }

    // ==============================================
    // VALIDATION HELPERS
    // ==============================================

    /**
     * Validate that the request has consistent data
     */
    public boolean isValidRequest(PerformanceRequest request) {
        if (request == null) {
            return false;
        }

        // Must have either strength or cardio data
        boolean hasStrengthData = request.getReps() != null && request.getWeight() != null;
        boolean hasCardioData = request.getDurationMinutes() != null ||
                request.getDurationSeconds() != null ||
                request.getDistanceKm() != null;
        boolean hasOtherData = request.getHoldDurationSeconds() != null ||
                request.getBalanceScore() != null ||
                request.getJumpHeightCm() != null ||
                request.getPowerOutputWatts() != null;

        return hasStrengthData || hasCardioData || hasOtherData;
    }

    /**
     * Check if the request is complete enough for meaningful analytics
     */
    public boolean isAnalyticsReady(PerformanceRequest request) {
        if (!isValidRequest(request)) {
            return false;
        }

        // For strength training, should have RPE or form rating
        if (request.getReps() != null && request.getWeight() != null) {
            return request.getPerceivedExertion() != null || request.getFormRating() != null;
        }

        // For cardio, should have duration and either distance or calories
        if (request.getDurationMinutes() != null || request.getDurationSeconds() != null) {
            return request.getDistanceKm() != null || request.getCaloriesBurned() != null;
        }

        return true; // Other exercise types are considered analytics-ready if they have basic data
    }

    /**
     * Get mapping completeness score (0-1)
     */
    public double getMappingCompleteness(PerformanceRecord performanceRecord) {
        if (performanceRecord == null) {
            return 0.0;
        }

        int totalFields = 20; // Core fields we care about for analytics
        int mappedFields = 0;

        // Count mapped fields
        if (performanceRecord.getReps() != null) mappedFields++;
        if (performanceRecord.getWeight() != null) mappedFields++;
        if (performanceRecord.getDurationMinutes() != null) mappedFields++;
        if (performanceRecord.getDurationSeconds() != null) mappedFields++;
        if (performanceRecord.getDistanceKm() != null) mappedFields++;
        if (performanceRecord.getCaloriesBurned() != null) mappedFields++;
        if (performanceRecord.getPerceivedExertion() != null) mappedFields++;
        if (performanceRecord.getFormRating() != null) mappedFields++;
        if (performanceRecord.getRestSeconds() != null) mappedFields++;
        if (performanceRecord.getTempo() != null) mappedFields++;
        if (performanceRecord.getHoldDurationSeconds() != null) mappedFields++;
        if (performanceRecord.getBalanceScore() != null) mappedFields++;
        if (performanceRecord.getJumpHeightCm() != null) mappedFields++;
        if (performanceRecord.getPowerOutputWatts() != null) mappedFields++;
        if (performanceRecord.getTargetReps() != null) mappedFields++;
        if (performanceRecord.getTargetWeight() != null) mappedFields++;
        if (performanceRecord.getAchievementStatus() != null) mappedFields++;
        if (performanceRecord.getNotes() != null && !performanceRecord.getNotes().isEmpty()) mappedFields++;
        if (performanceRecord.getEquipmentUsed() != null) mappedFields++;
        if (performanceRecord.getWorkoutEnvironment() != null) mappedFields++;

        return (double) mappedFields / totalFields;
    }
}