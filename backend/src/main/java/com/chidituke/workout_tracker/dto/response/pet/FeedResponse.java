package com.chidituke.workout_tracker.dto.response.pet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedResponse {

    private PetStatsResponse petStats;

    // ==========================================
    // FEEDING DETAILS
    // ==========================================

    private String mealType;          // "SNACK", "MEAL", "FEAST"
    private Integer crystalsSpent;
    private Integer baseFuel;
    private Double efficiency;
    private Integer actualFuelGained;

    // ==========================================
    // MESSAGES
    // ==========================================

    private String message;           // "Fed your pet a Meal! +34 Fuel"
    private String efficiencyWarning; // "Efficiency reduced - workout to improve!"
}