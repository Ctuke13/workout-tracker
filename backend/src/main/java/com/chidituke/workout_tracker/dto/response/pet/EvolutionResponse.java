package com.chidituke.workout_tracker.dto.response.pet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for pet evolution endpoint
 * Returns result of evolution attempt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvolutionResponse {

    private Boolean success;              // true if evolution succeeded
    private String oldStage;              // Previous evolution stage
    private String newStage;              // New evolution stage (same as oldStage if failed)
    private String newStageDisplay;       // Display name of new stage
    private Integer currentLevel;         // Pet's current level
    private String message;               // User-friendly message

    /**
     * Create success response
     */
    public static EvolutionResponse success(String oldStage, String newStage, String newStageDisplay, Integer level) {
        return EvolutionResponse.builder()
                .success(true)
                .oldStage(oldStage)
                .newStage(newStage)
                .newStageDisplay(newStageDisplay)
                .currentLevel(level)
                .message(String.format("🎉 Evolution complete! Your pet evolved from %s to %s!",
                        formatStageName(oldStage), newStageDisplay))
                .build();
    }

    /**
     * Create failure response - already at max evolution
     */
    public static EvolutionResponse alreadyMaxEvolution(String currentStage, String currentStageDisplay, Integer level) {
        return EvolutionResponse.builder()
                .success(false)
                .oldStage(currentStage)
                .newStage(currentStage)
                .newStageDisplay(currentStageDisplay)
                .currentLevel(level)
                .message(String.format("Your pet is already at the maximum evolution stage: %s!", currentStageDisplay))
                .build();
    }

    /**
     * Create failure response - level requirement not met
     */
    public static EvolutionResponse levelTooLow(String currentStage, String currentStageDisplay, Integer currentLevel, Integer requiredLevel) {
        return EvolutionResponse.builder()
                .success(false)
                .oldStage(currentStage)
                .newStage(currentStage)
                .newStageDisplay(currentStageDisplay)
                .currentLevel(currentLevel)
                .message(String.format("Your pet needs to reach level %d to evolve (currently level %d)",
                        requiredLevel, currentLevel))
                .build();
    }

    /**
     * Format stage name for display
     */
    private static String formatStageName(String stage) {
        if (stage == null) return "Unknown";
        return stage.substring(0, 1).toUpperCase() + stage.substring(1).toLowerCase();
    }
}