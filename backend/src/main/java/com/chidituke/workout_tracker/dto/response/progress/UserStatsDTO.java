package com.chidituke.workout_tracker.dto.response.progress;

import com.chidituke.workout_tracker.model.progress.UserProgression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for user workout statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsDTO {

    // Core stats
    private Integer totalWorkouts;
    private Integer totalSets;
    private BigDecimal totalVolume;
    private Integer totalMinutes;

    // Achievement tracking stats
    private BigDecimal totalDistance;
    private Integer totalHoldTime;
    private Integer uniqueExercises;

    // Workout type breakdown
    private Integer cardioWorkouts;
    private Integer strengthWorkouts;
    private Integer isometricWorkouts;

    // Special stats
    private Integer firstOfMonthWorkouts;
    private Integer weekendWorkouts;

    // Calculated stats
    private Double averageWorkoutDuration;
    private Double averageSetsPerWorkout;

    /**
     * Convert entity to DTO
     */
    public static UserStatsDTO fromProgression(UserProgression progression) {
        if (progression == null) return null;

        int totalWorkouts = progression.getTotalWorkoutsCompleted();
        double avgDuration = totalWorkouts > 0 ?
                (double) progression.getTotalWorkoutMinutes() / totalWorkouts : 0.0;
        double avgSets = totalWorkouts > 0 ?
                (double) progression.getTotalSetsCompleted() / totalWorkouts : 0.0;

        return UserStatsDTO.builder()
                .totalWorkouts(totalWorkouts)
                .totalSets(progression.getTotalSetsCompleted())
                .totalVolume(progression.getTotalVolumLifted())
                .totalMinutes(progression.getTotalWorkoutMinutes())
                .totalDistance(progression.getTotalDistanceKm())
                .totalHoldTime(progression.getTotalHoldSeconds())
                .uniqueExercises(progression.getUniqueExercisesTried())
                .cardioWorkouts(progression.getCardioWorkoutsCompleted())
                .strengthWorkouts(progression.getStrengthWorkoutsCompleted())
                .isometricWorkouts(progression.getIsometricWorkoutsCompleted())
                .firstOfMonthWorkouts(progression.getFirstOfMonthCount())
                .weekendWorkouts(progression.getWeekendWorkoutCount())
                .averageWorkoutDuration(Math.round(avgDuration * 100.0) / 100.0)
                .averageSetsPerWorkout(Math.round(avgSets * 100.0) / 100.0)
                .build();
    }
}