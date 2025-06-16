package com.chidituke.workout_tracker.model;

/**
 * Enum representing different user roles/types in the system
 * This determines WHAT TYPE of user someone is, not what features they can access
 * Features are controlled by SubscriptionTier
 */
public enum UserType {
    REGULAR("Regular User", "Consumer who uses workouts and programs"),
    PROFESSIONAL("Fitness Professional", "Fitness professional who can manage clients (requires Pro Professional subscription)"),
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

    // 🎯 ROLE-BASED PERMISSIONS (not feature-based)

    /**
     * Can this user type potentially manage clients?
     * Note: Still requires Pro Professional subscription to actually do it
     */
    public boolean canBeProfessional() {
        return this == PROFESSIONAL || this == ADMIN;
    }

    /**
     * Can this user type create professional content?
     * Note: Still requires appropriate subscription tier
     */
    public boolean canCreateProfessionalContent() {
        return this == PROFESSIONAL || this == ADMIN;
    }

    /**
     * Can this user type verify exercises?
     * Note: Still requires appropriate subscription tier
     */
    public boolean canVerifyExercises() {
        return this == PROFESSIONAL || this == ADMIN;
    }

    /**
     * Does this user type have admin access?
     */
    public boolean hasAdminAccess() {
        return this == ADMIN;
    }

    /**
     * Can this user type appear in professional directory?
     */
    public boolean canBeListedAsProfessional() {
        return this == PROFESSIONAL || this == ADMIN;
    }

    // 🎯 DEPRECATED METHODS (for backward compatibility)
    /**
     * @deprecated Use canCreateProfessionalContent() instead
     */
    @Deprecated
    public boolean canCreatePrograms() {
        return canCreateProfessionalContent();
    }
}