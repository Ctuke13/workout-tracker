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

    /**
     * Build a summary message based on the progression update
     */
    public String buildSummaryMessage() {
        StringBuilder message = new StringBuilder();

        message.append("Earned ").append(xpGained).append(" XP");

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
}