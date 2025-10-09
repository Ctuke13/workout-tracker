package com.chidituke.workout_tracker.dto.response.progress;

import com.chidituke.workout_tracker.model.progress.UserProgression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for complete user progression information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgressionDTO {

    // User info
    private Long userId;

    // Seasonal progression
    private Integer seasonalXp;
    private String seasonalRank;
    private Integer seasonalTier;
    private Integer currentSeasonId;
    private LocalDate seasonStartDate;

    // Lifetime progression
    private Integer lifetimeXp;
    private String lifetimeRank;
    private Integer lifetimeTier;

    // Streaks
    private Integer currentStreakDays;
    private Integer longestStreakDays;
    private LocalDate lastWorkoutDate;
    private Boolean streakActive;

    // Core statistics
    private Integer totalWorkoutsCompleted;
    private Integer totalSetsCompleted;
    private BigDecimal totalVolumLifted;
    private Integer totalWorkoutMinutes;

    // Achievement tracking
    private BigDecimal totalDistanceKm;
    private Integer totalHoldSeconds;
    private Integer uniqueExercisesTried;
    private Integer cardioWorkoutsCompleted;
    private Integer strengthWorkoutsCompleted;
    private Integer isometricWorkoutsCompleted;
    private Integer firstOfMonthCount;
    private Integer weekendWorkoutCount;

    // Weekly tracking
    private Integer weeklyWorkoutCount;
    private LocalDate weekStartDate;

    // Calculated fields
    private Integer xpToNextSeasonalRank;
    private Integer xpToNextLifetimeRank;
    private Double seasonalRankProgress;
    private Double lifetimeRankProgress;

    /**
     * Convert entity to DTO
     */
    public static UserProgressionDTO fromEntity(UserProgression progression) {
        if (progression == null) return null;

        return UserProgressionDTO.builder()
                .userId(progression.getUserId())
                .seasonalXp(progression.getSeasonalXp())
                .seasonalRank(progression.getSeasonalRank().name())
                .seasonalTier(progression.getSeasonalTier())
                .currentSeasonId(progression.getCurrentSeasonId())
                .seasonStartDate(progression.getSeasonStartDate())
                .lifetimeXp(progression.getLifetimeXp())
                .lifetimeRank(progression.getLifetimeRank().name())
                .lifetimeTier(progression.getLifetimeTier())
                .currentStreakDays(progression.getCurrentStreakDays())
                .longestStreakDays(progression.getLongestStreakDays())
                .lastWorkoutDate(progression.getLastWorkoutDate())
                .streakActive(progression.isStreakActive())
                .totalWorkoutsCompleted(progression.getTotalWorkoutsCompleted())
                .totalSetsCompleted(progression.getTotalSetsCompleted())
                .totalVolumLifted(progression.getTotalVolumLifted())
                .totalWorkoutMinutes(progression.getTotalWorkoutMinutes())
                .totalDistanceKm(progression.getTotalDistanceKm())
                .totalHoldSeconds(progression.getTotalHoldSeconds())
                .uniqueExercisesTried(progression.getUniqueExercisesTried())
                .cardioWorkoutsCompleted(progression.getCardioWorkoutsCompleted())
                .strengthWorkoutsCompleted(progression.getStrengthWorkoutsCompleted())
                .isometricWorkoutsCompleted(progression.getIsometricWorkoutsCompleted())
                .firstOfMonthCount(progression.getFirstOfMonthCount())
                .weekendWorkoutCount(progression.getWeekendWorkoutCount())
                .weeklyWorkoutCount(progression.getWeeklyWorkoutCount())
                .weekStartDate(progression.getWeekStartDate())
                .xpToNextSeasonalRank(progression.getXpToNextSeasonalRank())
                .xpToNextLifetimeRank(progression.getXpToNextLifetimeRank())
                .seasonalRankProgress(progression.getSeasonalRank()
                        .getProgressPercentage(progression.getSeasonalXp()))
                .lifetimeRankProgress(progression.getLifetimeRank()
                        .getProgressPercentage(progression.getLifetimeXp()))
                .build();
    }
}