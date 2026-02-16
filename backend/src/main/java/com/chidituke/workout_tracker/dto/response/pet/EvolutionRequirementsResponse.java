package com.chidituke.workout_tracker.dto.response.pet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for pet evolution requirements endpoint
 * Shows current evolution status and what's needed for next stage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvolutionRequirementsResponse {

    // Current state
    private String currentStage;          // e.g., "BABY"
    private String currentStageDisplay;   // e.g., "Baby Wolf"
    private Integer currentLevel;         // e.g., 5

    // Next evolution info
    private String nextStage;             // e.g., "KID" or null if max
    private String nextStageDisplay;      // e.g., "Kid Wolf" or null
    private Integer levelRequired;        // e.g., 11
    private Integer levelsRemaining;      // e.g., 6 (11 - 5)

    // Eligibility
    private Boolean canEvolve;            // true if currentLevel >= levelRequired
    private String message;               // User-friendly message

    /**
     * Generate user-friendly message based on evolution status
     */
    public void generateMessage() {
        if (nextStage == null) {
            this.message = String.format("You've reached the maximum evolution stage: %s!", currentStageDisplay);
        } else if (canEvolve) {
            this.message = String.format("Your pet is ready to evolve from %s to %s!", currentStageDisplay, nextStageDisplay);
        } else {
            this.message = String.format("Reach level %d to evolve to %s (%d levels to go)",
                    levelRequired, nextStageDisplay, levelsRemaining);
        }
    }
}