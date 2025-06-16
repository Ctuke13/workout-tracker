package com.chidituke.workout_tracker.model;

/**
 * Subscription tiers that unlock different features
 * Based on the refined 4-tier structure for fitness platform
 */
public enum SubscriptionTier {
    FREE("Free", 0.00, "Track your workouts and see your progress"),
    PLUS("Plus", 4.99, "Plan your workouts and stay consistent"),
    PRO("Pro", 12.99, "Intelligent training that adapts to your goals"),
    PRO_PROFESSIONAL("Pro Professional", 24.99, "Client management & lead generation for fitness professionals");

    private final String displayName;
    private final Double monthlyPrice;
    private final String description;

    SubscriptionTier(String displayName, Double monthlyPrice, String description) {
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public Double getMonthlyPrice() { return monthlyPrice; }
    public String getDescription() { return description; }

    // 🎯 PERSONAL FITNESS FEATURES
    public boolean canScheduleWorkouts() {
        return this.ordinal() >= PLUS.ordinal();
    }

    public boolean canReceiveNotifications() {
        return this.ordinal() >= PLUS.ordinal();
    }

    public boolean canCreateRoutines() {
        return this.ordinal() >= PLUS.ordinal();
    }

    public boolean canTrackConsistency() {
        return this.ordinal() >= PLUS.ordinal();
    }

    public boolean canCustomizeTemplates() {
        return this.ordinal() >= PLUS.ordinal();
    }

    public boolean canUseAIGeneration() {
        return this.ordinal() >= PRO.ordinal();
    }

    public boolean canAccessSportSpecificPrograms() {
        return this.ordinal() >= PRO.ordinal();
    }

    public boolean canUseProgressivePeriodization() {
        return this.ordinal() >= PRO.ordinal();
    }

    public boolean hasAdvancedAnalytics() {
        return this.ordinal() >= PRO.ordinal();
    }

    public boolean canAccessPremiumContent() {
        return this.ordinal() >= PRO.ordinal();
    }

    public boolean hasNutritionIntegration() {
        return this.ordinal() >= PRO.ordinal();
    }

    // 🎯 PROFESSIONAL BUSINESS FEATURES
    public boolean canGetProfessionalVerification() {
        return this == PRO_PROFESSIONAL;
    }

    public boolean canManageClients() {
        return this == PRO_PROFESSIONAL;
    }

    public boolean canAccessClientAnalytics() {
        return this == PRO_PROFESSIONAL;
    }

    public boolean canScheduleClientSessions() {
        return this == PRO_PROFESSIONAL;
    }

    public boolean canMessageClients() {
        return this == PRO_PROFESSIONAL;
    }

    public boolean canAssignPrograms() {
        return this == PRO_PROFESSIONAL;
    }

    public boolean canViewClientProgress() {
        return this == PRO_PROFESSIONAL;
    }

    public boolean hasPriorityDirectoryPlacement() {
        return this == PRO_PROFESSIONAL;
    }

    public boolean canCreateProfessionalProfile() {
        return this == PRO_PROFESSIONAL;
    }

    public boolean hasBusinessInsights() {
        return this == PRO_PROFESSIONAL;
    }

    // 🎯 LEGACY METHODS (for backward compatibility)
    public boolean canCreateBasicContent() {
        return canCustomizeTemplates();
    }

    public boolean canCreateProfessionalContent() {
        return canUseAIGeneration();
    }

    public boolean canRunProfessionalBusiness() {
        return canManageClients();
    }

    public boolean canVerifyExercises() {
        return canUseAIGeneration();
    }

    public boolean hasAnalytics() {
        return hasAdvancedAnalytics();
    }

    public boolean canAcceptClients() {
        return canManageClients();
    }
}