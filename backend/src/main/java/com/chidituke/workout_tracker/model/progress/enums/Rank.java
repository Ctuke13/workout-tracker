package com.chidituke.workout_tracker.model.progress.enums;

import lombok.Getter;

/**
 * Enum representing the 10 rank tiers in the progression system.
 * Each rank has 3 sub-tiers (III, II, I).
 * <p>
 * Data Flow:
 * - User earns XP from workouts
 * - When XP threshold reached, rank increases
 * - Rank determines user's status and unlocks features
 */
@Getter
public enum Rank {
    NOVICE(0, 99, "🥉"),           // 0-99 XP
    APPRENTICE(100, 299, "🥈"),    // 100-299 XP
    DEVOTEE(300, 599, "🥇"),       // 300-599 XP
    WARRIOR(600, 999, "⚔️"),       // 600-999 XP
    CHAMPION(1000, 1499, "🏆"),    // 1000-1499 XP
    ELITE(1500, 2099, "💎"),       // 1500-2099 XP
    MASTER(2100, 2799, "👑"),      // 2100-2799 XP
    LEGEND(2800, 3599, "⭐"),      // 2800-3599 XP
    ICON(3600, 4499, "🔥"),        // 3600-4499 XP
    IMMORTAL(4500, Integer.MAX_VALUE, "🌟"); // 4500+ XP

    private final int minXp;
    private final int maxXp;
    private final String icon;

    Rank(int minXp, int maxXp, String icon) {
        this.minXp = minXp;
        this.maxXp = maxXp;
        this.icon = icon;
    }

    /**
     * Calculate rank from XP amount.
     *
     * @param xp The XP amount
     * @return The rank for that XP level
     */
    public static Rank fromXp(int xp) {
        for (Rank rank : Rank.values()) {
            if (xp >= rank.minXp && xp <= rank.maxXp) {
                return rank;
            }
        }
        return IMMORTAL; // Fallback for very high XP
    }

    /**
     * Get the next rank in progression.
     *
     * @return The next rank, or null if already at IMMORTAL
     */
    public Rank getNextRank() {
        Rank[] ranks = Rank.values();
        int currentIndex = this.ordinal();
        if (currentIndex < ranks.length - 1) {
            return ranks[currentIndex + 1];
        }
        return null; // Already at max rank
    }

    /**
     * Calculate XP needed to reach next rank.
     *
     * @param currentXp Current XP amount
     * @return XP needed for next rank, or 0 if at max rank
     */
    public int getXpToNextRank(int currentXp) {
        if (this == IMMORTAL) {
            return 0; // Already at max
        }
        Rank nextRank = getNextRank();
        return nextRank != null ? nextRank.getMinXp() - currentXp : 0;
    }

    /**
     * Calculate progress percentage within current rank.
     *
     * @param currentXp Current XP amount
     * @return Progress percentage (0-100)
     */
    public double getProgressPercentage(int currentXp) {
        if (this == IMMORTAL) {
            return 100.0;
        }
        int rankXpRange = maxXp - minXp + 1;
        int xpIntoRank = currentXp - minXp;
        return (xpIntoRank / (double) rankXpRange) * 100.0;
    }
}