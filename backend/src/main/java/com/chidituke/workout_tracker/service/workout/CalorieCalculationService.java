package com.chidituke.workout_tracker.service.workout;

import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.model.workout.PerformanceRecord;
import com.chidituke.workout_tracker.model.workout.WorkoutSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Service for calculating calories burned during workouts using MET-based formulas
 * <p>
 * MET Formula: Calories = MET × weight(kg) × duration(hours) × adjustment_factor
 * <p>
 * Intensity Mapping (based on perceived exertion 1-10):
 * - Light: 1-3 (conversational pace)
 * - Moderate: 4-6 (challenging but sustainable)
 * - Vigorous: 7-10 (hard effort)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalorieCalculationService {

    /**
     * Calculate calories for a performance record
     *
     * @param record The performance record to calculate calories for
     * @param user   The user who performed the exercise
     * @return Calculated calories (rounded to nearest integer)
     */
    public Integer calculateCalories(PerformanceRecord record, User user) {
        if (record == null || user == null) {
            log.warn("Cannot calculate calories: record or user is null");
            return 0;
        }

        Exercise exercise = record.getExercise();
        if (exercise == null) {
            log.warn("Cannot calculate calories: exercise is null for record {}", record.getId());
            return 0;
        }

        // Check if user has calorie tracking enabled
        if (Boolean.FALSE.equals(user.getCalorieTrackingEnabled())) {
            log.debug("Calorie tracking disabled for user {}", user.getId());
            return 0;
        }

        // Get user weight in kg (required for MET calculation)
        Double weightKg = user.getWeightKg();
        if (weightKg == null || weightKg <= 0) {
            log.warn("Cannot calculate calories: user {} has no valid weight", user.getId());
            return 0;
        }

        // Determine intensity level based on perceived exertion
        PerformanceRecord.IntensityLevel intensity = determineIntensity(record.getPerceivedExertion());

        // Get appropriate MET value for the intensity
        Double metValue = getMetValueForIntensity(exercise, intensity);
        if (metValue == null || metValue <= 0) {
            log.warn("Cannot calculate calories: no valid MET value for exercise {} at intensity {}",
                    exercise.getId(), intensity);
            return 0;
        }

        // Calculate duration in hours
        Double durationHours = calculateDurationInHours(record);
        if (durationHours == null || durationHours <= 0) {
            log.warn("Cannot calculate calories: no valid duration for record {}", record.getId());
            return 0;
        }

        // Get user's calorie adjustment factor (default 1.0)
        Double adjustmentFactor = user.getCalorieAdjustmentFactor() != null ?
                user.getCalorieAdjustmentFactor() : 1.0;

        // Calculate calories using MET formula
        double calories = metValue * weightKg * durationHours * adjustmentFactor;

        // Store metadata in the record
        record.setMetValueUsed(metValue);
        record.setIntensityLevel(intensity);
        record.setCalorieCalculationMethod("MET_BASED");
        record.setCaloriesBurned((int) Math.round(calories));

        log.debug("Calculated {} calories for exercise {} (MET: {}, duration: {}h, weight: {}kg, adjustment: {})",
                (int) Math.round(calories), exercise.getExerciseName(), metValue, durationHours, weightKg, adjustmentFactor);

        return (int) Math.round(calories);
    }

    /**
     * Calculate total calories for an entire workout session
     *
     * @param session The workout session
     * @param user    The user who performed the workout
     * @return Total calculated calories
     */
    @Transactional
    public Integer calculateSessionCalories(WorkoutSession session, User user) {
        if (session == null || session.getPerformanceRecords() == null || session.getPerformanceRecords().isEmpty()) {
            log.warn("Cannot calculate session calories: session or performance records are null/empty");
            return 0;
        }

        int totalCalories = 0;
        int recordsCalculated = 0;

        for (PerformanceRecord record : session.getPerformanceRecords()) {
            Integer calories = calculateCalories(record, user);
            if (calories != null && calories > 0) {
                totalCalories += calories;
                recordsCalculated++;
            }
        }

        // Update session with calculated calories
        session.setTotalCaloriesCalculated(totalCalories);
        session.setCalorieCalculationStatus(recordsCalculated > 0 ? "CALCULATED" : "NOT_CALCULATED");

        log.info("Calculated {} total calories for workout session {} ({} records processed)",
                totalCalories, session.getId(), recordsCalculated);

        return totalCalories;
    }

    /**
     * Predict calories for a scheduled workout based on targets
     *
     * @param exercise              The exercise
     * @param targetDurationMinutes Target duration in minutes
     * @param targetSets            Target number of sets
     * @param targetRpe             Target RPE (1-10)
     * @param user                  The user
     * @return Predicted calories
     */
    public Integer predictCalories(Exercise exercise, Integer targetDurationMinutes,
                                   Integer targetSets, Integer targetRpe, User user) {
        if (exercise == null || user == null) {
            return 0;
        }

        Double weightKg = user.getWeightKg();
        if (weightKg == null || weightKg <= 0) {
            return 0;
        }

        // Determine predicted intensity
        PerformanceRecord.IntensityLevel intensity = determineIntensity(targetRpe);

        // Get MET value
        Double metValue = getMetValueForIntensity(exercise, intensity);
        if (metValue == null || metValue <= 0) {
            // Fallback to base calories per minute if available
            if (exercise.getBaseCaloriesPerMinute() != null && targetDurationMinutes != null) {
                return exercise.getBaseCaloriesPerMinute() * targetDurationMinutes;
            }
            return 0;
        }

        // Calculate predicted duration
        Double durationHours = null;
        if (targetDurationMinutes != null && targetDurationMinutes > 0) {
            durationHours = targetDurationMinutes / 60.0;
        } else if (targetSets != null && targetSets > 0) {
            // Estimate duration for strength exercises (average 3 minutes per set)
            durationHours = (targetSets * 3) / 60.0;
        }

        if (durationHours == null || durationHours <= 0) {
            return 0;
        }

        Double adjustmentFactor = user.getCalorieAdjustmentFactor() != null ?
                user.getCalorieAdjustmentFactor() : 1.0;

        double predictedCalories = metValue * weightKg * durationHours * adjustmentFactor;

        log.debug("Predicted {} calories for exercise {} (MET: {}, duration: {}h, weight: {}kg)",
                (int) Math.round(predictedCalories), exercise.getExerciseName(), metValue, durationHours, weightKg);

        return (int) Math.round(predictedCalories);
    }

    /**
     * Determine intensity level based on perceived exertion (RPE scale 1-10)
     *
     * @param perceivedExertion RPE value (1-10)
     * @return Intensity level
     */
    private PerformanceRecord.IntensityLevel determineIntensity(Integer perceivedExertion) {
        if (perceivedExertion == null) {
            return PerformanceRecord.IntensityLevel.MODERATE; // Default to moderate
        }

        if (perceivedExertion >= 1 && perceivedExertion <= 3) {
            return PerformanceRecord.IntensityLevel.LIGHT;
        } else if (perceivedExertion >= 4 && perceivedExertion <= 6) {
            return PerformanceRecord.IntensityLevel.MODERATE;
        } else if (perceivedExertion >= 7 && perceivedExertion <= 10) {
            return PerformanceRecord.IntensityLevel.VIGOROUS;
        }

        return PerformanceRecord.IntensityLevel.MODERATE;
    }

    /**
     * Get the appropriate MET value for an exercise at a given intensity
     *
     * @param exercise  The exercise
     * @param intensity The intensity level
     * @return MET value, or null if not available
     */
    private Double getMetValueForIntensity(Exercise exercise, PerformanceRecord.IntensityLevel intensity) {
        switch (intensity) {
            case LIGHT:
                return exercise.getMetValueLight();
            case MODERATE:
                return exercise.getMetValueModerate();
            case VIGOROUS:
                return exercise.getMetValueVigorous();
            case CUSTOM:
                // For custom intensity, use moderate as fallback
                return exercise.getMetValueModerate();
            default:
                return exercise.getMetValueModerate();
        }
    }

    /**
     * Calculate duration in hours from a performance record
     * Handles different exercise types (cardio, isometric, strength)
     *
     * @param record The performance record
     * @return Duration in hours, or null if cannot be determined
     */
    private Double calculateDurationInHours(PerformanceRecord record) {
        // Priority 1: Actual timing if available
        if (record.getSetStartTime() != null && record.getSetEndTime() != null) {
            Duration duration = Duration.between(record.getSetStartTime(), record.getSetEndTime());
            return duration.toMinutes() / 60.0;
        }

        // Priority 2: Actual set duration in seconds
        if (record.getActualSetDurationSeconds() != null && record.getActualSetDurationSeconds() > 0) {
            return record.getActualSetDurationSeconds() / 3600.0;
        }

        // Priority 3: Duration in minutes (for cardio)
        if (record.getDurationMinutes() != null && record.getDurationMinutes() > 0) {
            return record.getDurationMinutes() / 60.0;
        }

        // Priority 4: Duration in seconds (for cardio)
        if (record.getDurationSeconds() != null && record.getDurationSeconds() > 0) {
            return record.getDurationSeconds() / 3600.0;
        }

        // Priority 5: Hold duration (for isometric exercises)
        if (record.getHoldDurationSeconds() != null && record.getHoldDurationSeconds() > 0) {
            return record.getHoldDurationSeconds() / 3600.0;
        }

        // Priority 6: Estimate for strength exercises based on reps
        // Average tempo: 2 seconds per rep
        if (record.getReps() != null && record.getReps() > 0) {
            double estimatedSeconds = record.getReps() * 2.0;
            return estimatedSeconds / 3600.0;
        }

        // Cannot determine duration
        return null;
    }

    /**
     * Calculate calorie burn rate per minute for an exercise
     *
     * @param exercise  The exercise
     * @param intensity The intensity level
     * @param user      The user
     * @return Calories per minute
     */
    public Double calculateCaloriesPerMinute(Exercise exercise, PerformanceRecord.IntensityLevel intensity, User user) {
        if (exercise == null || user == null || user.getWeightKg() == null) {
            return 0.0;
        }

        Double metValue = getMetValueForIntensity(exercise, intensity);
        if (metValue == null || metValue <= 0) {
            return 0.0;
        }

        Double adjustmentFactor = user.getCalorieAdjustmentFactor() != null ?
                user.getCalorieAdjustmentFactor() : 1.0;

        // Calories per minute = (MET × weight(kg) × adjustment) / 60
        return (metValue * user.getWeightKg() * adjustmentFactor) / 60.0;
    }

    /**
     * Convert calories to kilojoules
     *
     * @param calories Calories
     * @return Kilojoules
     */
    public Double convertToKilojoules(Integer calories) {
        if (calories == null) return 0.0;
        return calories * 4.184;
    }

    /**
     * Get calories in user's preferred unit
     *
     * @param calories Calories
     * @param user     The user
     * @return Formatted string with value and unit
     */
    public String getCaloriesInPreferredUnit(Integer calories, User user) {
        if (calories == null) return "0 cal";

        String preferredUnit = user.getPreferredCalorieUnit() != null ?
                user.getPreferredCalorieUnit() : "CALORIES";

        if ("KILOJOULES".equals(preferredUnit)) {
            return String.format("%.1f kJ", convertToKilojoules(calories));
        }

        return calories + " cal";
    }
}