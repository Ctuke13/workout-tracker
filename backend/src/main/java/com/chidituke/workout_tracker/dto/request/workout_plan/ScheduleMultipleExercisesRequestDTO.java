package com.chidituke.workout_tracker.dto.request.workout_plan;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Request DTO for scheduling multiple exercises from a workout plan
 * Used when FREE users want to add workout plans (but hit 3-exercise limit)
 * Used when PLUS+ users want to add complete workout plans
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleMultipleExercisesRequestDTO {

    @NotNull(message = "Workout plan ID is required")
    private Long workoutPlanId;

    @NotNull(message = "Scheduled date is required")
    @FutureOrPresent(message = "Cannot schedule workouts in the past")
    private LocalDate scheduledDate;

    /**
     * Preferred start time for the first exercise
     * If null, will use default scheduling logic
     */
    private LocalTime preferredStartTime;

    /**
     * Whether to respect prescribed rest periods between exercises
     * Default: true (follow workout plan's rest recommendations)
     */
    @Builder.Default
    private Boolean respectRestPeriods = true;

    /**
     * Whether to automatically space workouts throughout the day
     * Default: true (spread exercises with smart timing)
     */
    @Builder.Default
    private Boolean autoSpaceWorkouts = true;

    /**
     * Custom notes to add to all scheduled exercises
     */
    private String notes;

    /**
     * Optional: Override specific exercises if user doesn't want the full plan
     * If null/empty, schedules all exercises from the workout plan
     * Useful for FREE users who want to see what's in a plan
     */
    private List<Long> exerciseIdsToSchedule;

    /**
     * Optional: Custom spacing between exercises (in minutes)
     * If null, uses smart default spacing (5-15 minutes depending on workout type)
     */
    @Min(value = 1, message = "Spacing must be at least 1 minute")
    @Max(value = 480, message = "Spacing cannot exceed 8 hours")
    private Integer spacingMinutes;

    /**
     * Whether to force scheduling even if it exceeds daily limits
     * Only used for showing upgrade prompts to FREE users
     * Default: false (respect subscription limits)
     */
    @Builder.Default
    private Boolean forceSchedule = false;

    /**
     * Priority level for the scheduled exercises
     * HIGH, MEDIUM, LOW - affects scheduling order if conflicts arise
     */
    private String priority;

    /**
     * Whether to send reminders for these scheduled exercises
     * Default: true (follow user's notification preferences)
     */
    @Builder.Default
    private Boolean enableReminders = true;
}