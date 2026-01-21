package com.chidituke.workout_tracker.dto.response.pet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedPreviewResponse {

    // ==========================================
    // FEEDING CALCULATION
    // ==========================================

    private Integer baseFuel;        // 15, 40, or 60
    private Double efficiency;       // 0.40 to 1.00
    private Integer actualFuel;      // baseFuel × efficiency

    // ==========================================
    // COST INFO
    // ==========================================

    private Integer crystalCost;     // 1, 3, or 5
    private Integer currentCrystals;
    private Boolean canAfford;

    // ==========================================
    // CONTEXT INFO
    // ==========================================

    private Integer daysSinceLastWorkout;
    private String efficiencyMessage; // "100% efficiency - worked out today!"
}