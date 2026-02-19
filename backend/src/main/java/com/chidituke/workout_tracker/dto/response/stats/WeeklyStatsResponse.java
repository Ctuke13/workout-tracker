package com.chidituke.workout_tracker.dto.response.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO for weekly workout statistics
 * Used by frontend to display "This Week" progress card
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyStatsResponse {

    // Core metrics
    private Integer workoutsThisWeek;
    private Integer xpThisWeek;
    private Integer currentStreak;

    // Goal tracking (optional - null if not set)
    private Integer weeklyGoal;
    private String goalType; // "workouts", "xp", etc.
    private Double goalProgress; // 0.0 - 1.0 (e.g., 0.75 = 75%)

    // Week context
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;

    // Additional context
    private Integer workoutsRemaining; // Workouts left to hit goal (null if no goal)
    private Boolean goalAchieved; // True if goal hit, null if no goal
    private String message; // Optional message like "1 more to hit your goal!"
}