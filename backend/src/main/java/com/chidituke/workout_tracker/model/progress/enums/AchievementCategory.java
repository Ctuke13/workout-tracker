package com.chidituke.workout_tracker.model.progress.enums;

/**
 * Categories for organizing the 83 achievements.
 * <p>
 * Achievement Distribution:
 * - WORKOUT_MILESTONE: 10 achievements (First Workout → Icon)
 * - STREAK: 10 achievements (Hot Start → The Unbreakable)
 * - STRENGTH_VOLUME: 6 achievements (Lifter → Million Pound Club)
 * - WEEKLY_CHALLENGE: 5 achievements (Solid Week → Perfect Month)
 * - TIME_BASED: 5 achievements (Time Warrior → Eternal)
 * - SEASONAL_RANK: 6 achievements (Seasonal Devotee → Seasonal Legend)
 * - SPECIAL_HIDDEN: 22 achievements (New Year → Comeback Kid)
 * - CARDIO_DISTANCE: 6 achievements (First Mile → Around the World)
 * - ISOMETRIC_ENDURANCE: 6 achievements (Steady Holder → The Pillar)
 * - WORKOUT_DIVERSITY: 7 achievements (Explorer → Jack of All Trades)
 * <p>
 * Total: 83 achievements
 */
public enum AchievementCategory {
    WORKOUT_MILESTONE,       // Total workouts completed (singular!)
    STREAK,                  // Consecutive day streaks (no _ACHIEVEMENTS!)
    STRENGTH_VOLUME,         // Total weight lifted
    WEEKLY_CHALLENGE,        // Weekly workout consistency (singular!)
    TIME_BASED,              // Workout duration totals
    SEASONAL_RANK,           // Seasonal ranking achievements (NEW!)
    SPECIAL_HIDDEN,          // Hidden/special achievements
    CARDIO_DISTANCE,         // Distance covered
    ISOMETRIC_ENDURANCE,     // Isometric hold time
    WORKOUT_DIVERSITY;       // Variety of exercises

    /**
     * Get display name for category.
     *
     * @return Human-readable category name
     */
    public String getDisplayName() {
        return switch (this) {
            case WORKOUT_MILESTONE -> "Workout Milestones";
            case STREAK -> "Streak Master";
            case STRENGTH_VOLUME -> "Strength Volume";
            case TIME_BASED -> "Time Under Tension";
            case CARDIO_DISTANCE -> "Distance Runner";
            case ISOMETRIC_ENDURANCE -> "Isometric Endurance";
            case WORKOUT_DIVERSITY -> "Exercise Variety";
            case WEEKLY_CHALLENGE -> "Weekly Warrior";
            case SEASONAL_RANK -> "Seasonal Rankings";
            case SPECIAL_HIDDEN -> "Special Achievements";
        };
    }
}