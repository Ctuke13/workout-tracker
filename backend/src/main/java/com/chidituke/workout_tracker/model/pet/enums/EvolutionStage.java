package com.chidituke.workout_tracker.model.pet.enums;

import lombok.Getter;

/**
 * Evolution stages for pets based on level progression.
 * Each stage has distinct visual appearance and animations.
 */
@Getter
public enum EvolutionStage {
    /**
     * Baby stage: Levels 1-10
     * Starting stage with basic animations
     */
    BABY(1, 10, "Baby"),

    /**
     * Kid stage: Levels 11-25
     * First evolution with enhanced confidence
     */
    KID(11, 25, "Kid"),

    /**
     * Teen stage: Levels 26-50
     * Stands on hind legs, can wear accessories
     */
    TEEN(26, 50, "Teen"),

    /**
     * Adult stage: Levels 51-75
     * Fully mature with advanced abilities
     */
    ADULT(51, 75, "Adult"),

    /**
     * Champion stage: Levels 76-99
     * Elite appearance with legendary skills
     */
    CHAMPION(76, 99, "Champion"),

    /**
     * Legendary stage: Level 100+
     * Ultimate form, maximum prestige
     */
    LEGENDARY(100, Integer.MAX_VALUE, "Legendary");

    private final int minLevel;
    private final int maxLevel;
    private final String displayName;

    EvolutionStage(int minLevel, int maxLevel, String displayName) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.displayName = displayName;
    }

    /**
     * Get the appropriate evolution stage for a given level
     *
     * @param level The pet's current level
     * @return The evolution stage corresponding to that level
     */
    public static EvolutionStage getStageForLevel(int level) {
        if (level >= LEGENDARY.minLevel) return LEGENDARY;
        if (level >= CHAMPION.minLevel) return CHAMPION;
        if (level >= ADULT.minLevel) return ADULT;
        if (level >= TEEN.minLevel) return TEEN;
        if (level >= KID.minLevel) return KID;
        return BABY;
    }

    /**
     * Get the next evolution stage, or null if already at max
     *
     * @return The next evolution stage, or null if LEGENDARY
     */
    public EvolutionStage getNextStage() {
        return switch (this) {
            case BABY -> KID;
            case KID -> TEEN;
            case TEEN -> ADULT;
            case ADULT -> CHAMPION;
            case CHAMPION -> LEGENDARY;
            case LEGENDARY -> null; // Already at max
        };
    }

    /**
     * Check if this stage can evolve further
     *
     * @return true if there is a next stage
     */
    public boolean canEvolve() {
        return this != LEGENDARY;
    }

    /**
     * Get the level required to reach the next stage
     *
     * @return The minimum level for next stage, or -1 if already LEGENDARY
     */
    public int getLevelForNextStage() {
        EvolutionStage next = getNextStage();
        return next != null ? next.minLevel : -1;
    }
}