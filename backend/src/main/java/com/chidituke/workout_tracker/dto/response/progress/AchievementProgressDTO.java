package com.chidituke.workout_tracker.dto.response.progress;

import com.chidituke.workout_tracker.service.progress.AchievementService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user's progress toward a specific achievement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementProgressDTO {

    // Achievement info
    private Integer achievementId;
    private String name;
    private String description;
    private String category;
    private String rarity;
    private Integer bonusXp;
    private String icon;

    // Progress info
    private Boolean isUnlocked;
    private Double progressPercentage;
    private Integer currentValue;
    private Integer targetValue;
    private Integer remainingValue;

    // Display helpers
    private String progressText;
    private String statusMessage;

    /**
     * Convert from service progress
     */
    public static AchievementProgressDTO fromProgress(
            AchievementService.AchievementProgress progress) {

        if (progress == null || progress.achievement() == null) return null;

        var achievement = progress.achievement();
        int remaining = Math.max(0, progress.targetValue() - progress.currentValue());

        return AchievementProgressDTO.builder()
                .achievementId(achievement.getAchievementId())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .category(achievement.getCategory().name())
                .rarity(achievement.getRarity().name())
                .bonusXp(achievement.getBonusXp())
                .icon(achievement.getIcon())
                .isUnlocked(progress.isUnlocked())
                .progressPercentage(Math.round(progress.progressPercentage() * 100.0) / 100.0)
                .currentValue(progress.currentValue())
                .targetValue(progress.targetValue())
                .remainingValue(remaining)
                .progressText(buildProgressText(progress))
                .statusMessage(buildStatusMessage(progress))
                .build();
    }

    /**
     * Build progress text (e.g., "25/100")
     */
    private static String buildProgressText(AchievementService.AchievementProgress progress) {
        return progress.currentValue() + "/" + progress.targetValue();
    }

    /**
     * Build status message
     */
    private static String buildStatusMessage(AchievementService.AchievementProgress progress) {
        if (progress.isUnlocked()) {
            return "Unlocked! ✅";
        }

        int remaining = progress.targetValue() - progress.currentValue();
        if (remaining <= 1) {
            return "Almost there! Just " + remaining + " more!";
        }

        double percentage = progress.progressPercentage();
        if (percentage >= 75.0) {
            return "So close! " + remaining + " to go!";
        } else if (percentage >= 50.0) {
            return "Halfway there! " + remaining + " remaining.";
        } else if (percentage >= 25.0) {
            return "Making progress! " + remaining + " to go.";
        } else if (percentage > 0.0) {
            return "Just started! " + remaining + " remaining.";
        }

        return "Not started yet.";
    }
}