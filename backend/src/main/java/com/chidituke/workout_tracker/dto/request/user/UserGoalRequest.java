package com.chidituke.workout_tracker.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Request DTO for updating user's weekly workout goal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGoalRequest {

    @Min(value = 0, message = "Weekly goal must be at least 0")
    @Max(value = 7, message = "Weekly goal cannot exceed 7 workouts")
    private Integer weeklyWorkoutGoal; // null = remove goal

    private String goalType; // Currently only "workouts" supported
}