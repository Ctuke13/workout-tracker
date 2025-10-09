package com.chidituke.workout_tracker.model.progress.enums;

import lombok.Getter;

/**
 * Rarity levels for achievements.
 * Higher rarity = more difficult to achieve = more bonus XP.
 * <p>
 * Rarity Distribution (83 total):
 * - COMMON: 25 achievements (30%) - Bonus: 10 XP
 * - UNCOMMON: 20 achievements (24%) - Bonus: 25 XP
 * - RARE: 18 achievements (22%) - Bonus: 50 XP
 * - EPIC: 12 achievements (14%) - Bonus: 100 XP
 * - LEGENDARY: 8 achievements (10%) - Bonus: 250 XP
 */
@Getter
public enum Rarity {
    COMMON(10, "🥉", "#A8A8A8"),          // Bronze - Easy achievements
    UNCOMMON(25, "🥈", "#C0C0C0"),        // Silver - Moderate achievements
    RARE(50, "🥇", "#FFD700"),            // Gold - Challenging achievements
    EPIC(100, "💎", "#9F7AEA"),           // Diamond - Very difficult achievements
    LEGENDARY(250, "🌟", "#F59E0B");      // Star - Extremely rare achievements

    private final int bonusXp;
    private final String icon;
    private final String colorHex;

    Rarity(int bonusXp, String icon, String colorHex) {
        this.bonusXp = bonusXp;
        this.icon = icon;
        this.colorHex = colorHex;
    }

    /**
     * Get display name with icon.
     *
     * @return Formatted rarity name
     */
    public String getDisplayName() {
        return icon + " " + this.name();
    }
}