package com.chidituke.workout_tracker.dto.response.progress;

import com.chidituke.workout_tracker.service.progress.AchievementService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user's achievement statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementStatsDTO {

    private Long totalUnlocked;
    private Long totalAchievements;
    private Long totalBonusXpEarned;
    private Double unlockPercentage;

    // Calculated fields
    private Long remainingAchievements;
    private String completionStatus;

    /**
     * Convert from service stats
     */
    public static AchievementStatsDTO fromStats(AchievementService.AchievementStats stats) {
        if (stats == null) return null;

        long remaining = stats.totalAchievements() - stats.totalUnlocked();
        String status = getCompletionStatus(stats.unlockPercentage());

        return AchievementStatsDTO.builder()
                .totalUnlocked(stats.totalUnlocked())
                .totalAchievements(stats.totalAchievements())
                .totalBonusXpEarned(stats.totalBonusXpEarned())
                .unlockPercentage(Math.round(stats.unlockPercentage() * 100.0) / 100.0)
                .remainingAchievements(remaining)
                .completionStatus(status)
                .build();
    }

    /**
     * Get completion status message
     */
    private static String getCompletionStatus(Double percentage) {
        if (percentage >= 100.0) return "All achievements unlocked! 🎉";
        if (percentage >= 75.0) return "Almost there!";
        if (percentage >= 50.0) return "Halfway there!";
        if (percentage >= 25.0) return "Making progress!";
        if (percentage > 0.0) return "Just getting started!";
        return "No achievements yet - start working out!";
    }
}