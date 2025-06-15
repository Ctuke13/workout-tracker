package com.chidituke.workout_tracker.model;

/**
 * Enum representing different user categories in the system
 * Note: Professional status requires PRO_PROFESSIONAL subscription ($25)
 */
public enum UserType {
    REGULAR("Regular User", "Consumer who uses workouts and programs"),
    PROFESSIONAL("Fitness Professional", "Can create programs and content - requires PRO_PROFESSIONAL subscription"),
    ADMIN("Administrator", "System administrator with full access");

    private final String displayName;
    private final String description;

    UserType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasAdminAccess() {
        return this == ADMIN;
    }

    public boolean canCreatePrograms() {
        // Professional features require PRO_PROFESSIONAL subscription
        // This is enforced in the service layer
        return this == PROFESSIONAL || this == ADMIN;
    }

    public boolean canVerifyExercises() {
        return this == PROFESSIONAL || this == ADMIN;
    }
}