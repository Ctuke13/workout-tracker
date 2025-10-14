package com.chidituke.workout_tracker.model.progress.enums;

import lombok.Getter;

/**
 * Enum representing the 10 rank tiers in the progression system.
 * Each rank has 3 sub-tiers (III, II, I).
 * <p>
 * Rank Structure (from spec v1.0):
 * - NOVICE:     0 - 1,500 XP    (Levels 1-10)
 * - APPRENTICE: 1,500 - 4,000   (Levels 11-20)
 * - DEVOTEE:    4,000 - 8,000   (Levels 21-30)
 * - WARRIOR:    8,000 - 14,000  (Levels 31-40)
 * - CHAMPION:   14,000 - 22,000 (Levels 41-50)
 * - ELITE:      22,000 - 32,000 (Levels 51-60)
 * - MASTER:     32,000 - 44,000 (Levels 61-70)
 * - LEGEND:     44,000 - 60,000 (Levels 71-80)
 * - ICON:       60,000 - 90,000 (Levels 81-90) - Lifetime only
 * - IMMORTAL:   90,000+         (Levels 91-100) - Lifetime only
 * <p>
 * Data Flow:
 * - User earns XP from workouts
 * - When XP threshold reached, rank increases
 * - Rank determines user's status and unlocks features
 */
@Getter
public enum Rank {
    // Seasonal Ranks (achievable in one 3-month season)
    NOVICE(0, 1500, "🌱"),           // 0-1,500 XP
    APPRENTICE(1500, 4000, "🟠"),    // 1,500-4,000 XP
    DEVOTEE(4000, 8000, "🟡"),       // 4,000-8,000 XP
    WARRIOR(8000, 14000, "🟢"),      // 8,000-14,000 XP
    CHAMPION(14000, 22000, "🔵"),    // 14,000-22,000 XP
    ELITE(22000, 32000, "💜"),       // 22,000-32,000 XP
    MASTER(32000, 44000, "🔴"),      // 32,000-44,000 XP
    LEGEND(44000, 60000, "⚪"),      // 44,000-60,000 XP

    // Lifetime Exclusive Ranks (not achievable in single season)
    ICON(60000, 90000, "🌟"),        // 60,000-90,000 XP
    IMMORTAL(90000, Integer.MAX_VALUE, "💎"); // 90,000+ XP

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
            if (xp >= rank.minXp && xp < rank.maxXp) {
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

        // For other ranks, calculate % through the rank
        int rankXpRange = maxXp - minXp;
        int xpIntoRank = currentXp - minXp;

        // Clamp between 0-100
        double percentage = (xpIntoRank / (double) rankXpRange) * 100.0;
        return Math.max(0.0, Math.min(100.0, percentage));
    }

    /**
     * Calculate tier (III, II, I) within current rank.
     *
     * @param currentXp Current XP amount
     * @return Tier number (3=III, 2=II, 1=I)
     */
    public int calculateTier(int currentXp) {
        if (this == IMMORTAL) {
            // Special handling for IMMORTAL
            if (currentXp < 120000) return 3; // III
            if (currentXp < 150000) return 2; // II
            return 1; // I
        }

        int rankXpRange = maxXp - minXp;
        int xpIntoRank = currentXp - minXp;
        int tierSize = rankXpRange / 3;

        if (xpIntoRank < tierSize) return 3;        // Tier III
        if (xpIntoRank < tierSize * 2) return 2;   // Tier II
        return 1;                                    // Tier I
    }
}