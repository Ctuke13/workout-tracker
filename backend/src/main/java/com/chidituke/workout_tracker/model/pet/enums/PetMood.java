package com.chidituke.workout_tracker.model.pet.enums;

/**
 * Represents the emotional state of the pet based on stats
 * Used to determine which animation state to display
 */
public enum PetMood {
    /**
     * Happy mood - All stats are healthy
     * Triggers: fuel > 60 AND motivation > 60 AND fatigue < 40 AND cleanliness > 60
     */
    HAPPY("happy", "Pet is happy and well-cared for!"),

    /**
     * Neutral mood - Normal state, default
     * Triggers: Stats are between happy and sad thresholds
     */
    NEUTRAL("neutral", "Pet is doing okay"),

    /**
     * Sad mood - At least one critical stat is bad
     * Triggers: fuel < 30 OR motivation < 30 OR fatigue > 70 OR cleanliness < 30
     */
    SAD("sad", "Pet needs attention!");

    private final String value;
    private final String description;

    PetMood(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Calculate pet mood based on current stats
     *
     * @param fuel        Current fuel level (0-100)
     * @param motivation  Current motivation level (0-100)
     * @param fatigue     Current fatigue level (0-100)
     * @param cleanliness Current cleanliness level (0-100)
     * @return The calculated mood
     */
    public static PetMood fromStats(int fuel, int motivation, int fatigue, int cleanliness) {
        // Check SAD first - any critical stat triggers sadness
        if (fuel < 30 || motivation < 30 || fatigue > 70 || cleanliness < 30) {
            return SAD;
        }

        // Check HAPPY - all stats must be healthy
        if (fuel > 60 && motivation > 60 && fatigue < 40 && cleanliness > 60) {
            return HAPPY;
        }

        // Everything else is NEUTRAL (default)
        return NEUTRAL;
    }
}