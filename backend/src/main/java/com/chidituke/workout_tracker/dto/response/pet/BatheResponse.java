package com.chidituke.workout_tracker.dto.response.pet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatheResponse {

    private PetStatsResponse petStats;

    // ==========================================
    // BATH DETAILS
    // ==========================================

    private Integer bathTier;      // 1, 2, or 3
    private Integer restoration;   // Amount of cleanliness restored
    private String tierName;       // "Deodorant Spray", "Sponge", "Full Shower"
    private String tierDescription; // Description of the bath tier

    // ==========================================
    // MESSAGES
    // ==========================================

    private String message;        // "Your pet feels fresh and clean!"
}