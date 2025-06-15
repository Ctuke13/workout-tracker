package com.chidituke.workout_tracker.model;

/**
 * Enum representing different activity levels for users
 * Used to determine calorie needs and workout recommendations
 */
public enum ActivityLevel {
    SEDENTARY(1.2, "Sedentary", "Little to no exercise, desk job"),
    LIGHTLY_ACTIVE(1.375, "Lightly Active", "Light exercise 1-3 days per week"),
    MODERATELY_ACTIVE(1.55, "Moderately Active", "Moderate exercise 3-5 days per week"),
    VERY_ACTIVE(1.725, "Very Active", "Hard exercise 6-7 days per week"),
    EXTREMELY_ACTIVE(1.9, "Extremely Active", "Very hard exercise, physical job or training twice per day");

    private final double multiplier;
    private final String displayName;
    private final String description;

    ActivityLevel(double multiplier, String displayName, String description) {
        this.multiplier = multiplier;
        this.displayName = displayName;
        this.description = description;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Calculate Total Daily Energy Expenditure (TDEE)
     * @param bmr Basal Metabolic Rate
     * @return TDEE in calories
     */
    public double calculateTDEE(double bmr) {
        return bmr * multiplier;
    }
}