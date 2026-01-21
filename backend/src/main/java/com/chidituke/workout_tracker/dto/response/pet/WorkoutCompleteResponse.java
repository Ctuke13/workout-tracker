package com.chidituke.workout_tracker.dto.response.pet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Internal response from PetStatsService.handleWorkoutCompletion()
 * Gets converted to PetStatsUpdateDTO in ProgressController
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutCompleteResponse {

    // Crystal info
    private Integer crystalsEarned;
    private Integer wastedCrystals;
    private Integer newCrystalBalance;

    // Fatigue info
    private Integer fatigueIncrease;
    private Integer newFatigue;
    private Boolean isSleeping;
    private Long sleepTimeRemainingMinutes;

    // Other stat changes
    private Integer motivationGain;
    private Integer newMotivation;
    private Integer cleanlinessDecrease;
    private Integer newCleanliness;

    // Message
    private String message;
}