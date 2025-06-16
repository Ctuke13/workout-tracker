package com.chidituke.workout_tracker.dto.response.subscription;

import com.chidituke.workout_tracker.model.SubscriptionTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lightweight DTO for subscription status checks
 * Optimized for frequent status queries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatusDTO {

    private Long userId;
    private SubscriptionTier subscriptionTier;
    private String status;
    private Boolean isActive;
    private Boolean isFreeTier;
    private Boolean isPlusTier;
    private Boolean isProTier;
    private Boolean isProProfessional;
    private Boolean isPaidTier;
    private Long daysRemaining;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Feature access flags (pre-computed for performance)
    private Boolean canScheduleWorkouts;
    private Boolean canReceiveNotifications;
    private Boolean canCreateRoutines;
    private Boolean canTrackConsistency;
    private Boolean canCustomizeTemplates;
    private Boolean canUseAIGeneration;
    private Boolean canAccessSportSpecificPrograms;
    private Boolean canUseProgressivePeriodization;
    private Boolean hasAdvancedAnalytics;
    private Boolean canAccessPremiumContent;
    private Boolean hasNutritionIntegration;
    private Boolean canManageClients;
    private Boolean canAccessClientAnalytics;
    private Boolean canScheduleClientSessions;
    private Boolean canMessageClients;
    private Boolean canAssignPrograms;
    private Boolean canViewClientProgress;
    private Boolean hasPriorityDirectoryPlacement;
    private Boolean canCreateProfessionalProfile;
    private Boolean hasBusinessInsights;
}