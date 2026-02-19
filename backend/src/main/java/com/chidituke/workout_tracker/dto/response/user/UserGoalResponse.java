package com.chidituke.workout_tracker.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user's goal settings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGoalResponse {

    private Integer weeklyWorkoutGoal; // null if not set
    private String goalType; // "workouts", "xp", etc.
    private Boolean hasGoalSet; // Convenience field

    // Preset suggestions for frontend
    private String goalLevel; // "beginner" (2-3), "regular" (3-4), "dedicated" (5-6)
}