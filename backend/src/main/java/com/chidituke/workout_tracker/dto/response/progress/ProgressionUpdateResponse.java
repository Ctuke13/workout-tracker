package com.chidituke.workout_tracker.dto.response.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for workout completion progression update.
 * Returns XP gained, rank changes, and newly unlocked achievements.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressionUpdateResponse {

    // XP changes
    private Integer xpGained;
    private Integer newSeasonalXp;
    private Integer newLifetimeXp;

    // Rank info
    private String seasonalRank;
    private String lifetimeRank;
    private Boolean rankedUp;
    private Boolean tieredUp;
    private String newRank;
    private String oldRank;
    private Integer oldTier;
    private Integer newSeasonalTier;

    // Streak info
    private Integer currentStreak;
    private Boolean streakMilestone;
    private String streakMessage;

    // Achievements
    private List<UserAchievementDTO> achievementsUnlocked;
    private Integer totalAchievementBonusXp;

    // Summary message
    private String summaryMessage;

    private PetStatsUpdateDTO petUpdate;

    /**
     * Whether a consistency bonus (15% XP boost) was applied to this workout.
     * Set when the frontend flags the workout as meeting honest-effort thresholds.
     */
    private Boolean consistencyBonusApplied;

    /**
     * Build a summary message based on the progression update
     */
    public String buildSummaryMessage() {
        StringBuilder message = new StringBuilder();

        message.append("Earned ").append(xpGained).append(" XP");

        if (Boolean.TRUE.equals(consistencyBonusApplied)) {
            message.append(" (including 15% consistency bonus)");
        }

        if (rankedUp) {
            message.append(" and ranked up to ").append(newRank).append("!");
        } else {
            message.append("!");
        }

        if (achievementsUnlocked != null && !achievementsUnlocked.isEmpty()) {
            message.append(" Unlocked ").append(achievementsUnlocked.size())
                    .append(" achievement").append(achievementsUnlocked.size() > 1 ? "s" : "").append("!");
        }

        if (streakMilestone) {
            message.append(" ").append(streakMessage);
        }

        return message.toString();
    }

    // ==========================================
    // NESTED DTO FOR PET STATS UPDATE
    // ==========================================

    /**
     * Pet stats update information returned with workout completion
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PetStatsUpdateDTO {
        // Crystal info
        private Integer crystalsEarned;
        private Integer wastedCrystals;
        private Integer newCrystalBalance;

        // Fatigue info
        private Integer fatigueIncrease;
        private Integer newFatigue;
        private Boolean isSleeping;

        // XP and Leveling info
        private Integer xpGained;
        private Integer newXp;
        private Integer newLevel;
        private Boolean leveledUp;

        // Message
        private String message;
    }
}