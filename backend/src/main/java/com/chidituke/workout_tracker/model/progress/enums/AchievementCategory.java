package com.chidituke.workout_tracker.model.progress.enums;

/**
 * Categories for organizing the 83 achievements.
 * <p>
 * Achievement Distribution:
 * - WORKOUT_MILESTONES: 10 achievements (First Workout → 1000 Club)
 * - STREAK_ACHIEVEMENTS: 10 achievements (Starter → Year Legend)
 * - STRENGTH_VOLUME: 10 achievements (Iron Starter → Hercules)
 * - TIME_BASED: 10 achievements (Quick Session → Marathon Master)
 * - CARDIO_DISTANCE: 10 achievements (First Mile → Around the World)
 * - ISOMETRIC_ENDURANCE: 10 achievements (Steady Holder → The Pillar)
 * - WORKOUT_DIVERSITY: 10 achievements (Explorer → Jack of All Trades)
 * - WEEKLY_CHALLENGES: 10 achievements (Week Warrior → Perfect Year)
 * - SPECIAL_HIDDEN: 13 achievements (Early Bird → Leap Year Legend)
 * <p>
 * Total: 83 achievements
 */
public enum AchievementCategory {
    WORKOUT_MILESTONES,      // Total workouts completed
    STREAK_ACHIEVEMENTS,     // Consecutive day streaks
    STRENGTH_VOLUME,         // Total weight lifted
    TIME_BASED,              // Workout duration totals
    CARDIO_DISTANCE,         // Distance covered
    ISOMETRIC_ENDURANCE,     // Isometric hold time
    WORKOUT_DIVERSITY,       // Variety of exercises
    WEEKLY_CHALLENGES,       // Weekly workout consistency
    SPECIAL_HIDDEN;          // Hidden/special achievements

    /**
     * Get display name for category.
     *
     * @return Human-readable category name
     */
    public String getDisplayName() {
        return switch (this) {
            case WORKOUT_MILESTONES -> "Workout Milestones";
            case STREAK_ACHIEVEMENTS -> "Streak Master";
            case STRENGTH_VOLUME -> "Strength Volume";
            case TIME_BASED -> "Time Under Tension";
            case CARDIO_DISTANCE -> "Distance Runner";
            case ISOMETRIC_ENDURANCE -> "Isometric Endurance";
            case WORKOUT_DIVERSITY -> "Exercise Variety";
            case WEEKLY_CHALLENGES -> "Weekly Warrior";
            case SPECIAL_HIDDEN -> "Special Achievements";
        };
    }
}