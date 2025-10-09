package com.chidituke.workout_tracker.dto.response.progress;

import com.chidituke.workout_tracker.model.progress.UserProgression;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO for user streak information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreakInfoDTO {

    private Integer currentStreakDays;
    private Integer longestStreakDays;
    private LocalDate lastWorkoutDate;
    private Boolean isStreakActive;
    private String streakStatus;
    private Integer daysUntilStreakBreaks;
    private String nextMilestone;
    private Integer daysToNextMilestone;

    /**
     * Convert entity to DTO
     */
    public static StreakInfoDTO fromProgression(UserProgression progression) {
        if (progression == null) return null;

        LocalDate today = LocalDate.now();
        LocalDate lastWorkout = progression.getLastWorkoutDate();
        boolean isActive = progression.isStreakActive();

        String status;
        Integer daysUntilBreak;

        if (lastWorkout == null) {
            status = "No workouts yet";
            daysUntilBreak = null;
        } else if (lastWorkout.equals(today)) {
            status = "Active - worked out today!";
            daysUntilBreak = 1;
        } else if (isActive) {
            status = "Active - keep it up!";
            daysUntilBreak = 1;
        } else {
            status = "Broken - start a new streak!";
            daysUntilBreak = null;
        }

        // Calculate next milestone
        int current = progression.getCurrentStreakDays();
        int[] milestones = {3, 7, 14, 30, 60, 100, 365};
        String nextMilestone = null;
        Integer daysToMilestone = null;

        for (int milestone : milestones) {
            if (current < milestone) {
                nextMilestone = milestone + " days";
                daysToMilestone = milestone - current;
                break;
            }
        }

        return StreakInfoDTO.builder()
                .currentStreakDays(progression.getCurrentStreakDays())
                .longestStreakDays(progression.getLongestStreakDays())
                .lastWorkoutDate(lastWorkout)
                .isStreakActive(isActive)
                .streakStatus(status)
                .daysUntilStreakBreaks(daysUntilBreak)
                .nextMilestone(nextMilestone)
                .daysToNextMilestone(daysToMilestone)
                .build();
    }
}