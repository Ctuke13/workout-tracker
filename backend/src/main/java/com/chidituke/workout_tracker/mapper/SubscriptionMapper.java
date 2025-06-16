package com.chidituke.workout_tracker.mapper;

import com.chidituke.workout_tracker.dto.request.subscription.SubscriptionCreateRequestDTO;
import com.chidituke.workout_tracker.dto.request.subscription.SubscriptionUpdateRequestDTO;
import com.chidituke.workout_tracker.dto.response.subscription.SubscriptionResponseDTO;
import com.chidituke.workout_tracker.dto.response.subscription.SubscriptionStatusDTO;
import com.chidituke.workout_tracker.dto.response.subscription.SubscriptionStatsDTO;
import com.chidituke.workout_tracker.model.Subscription;
import com.chidituke.workout_tracker.model.SubscriptionTier;
import com.chidituke.workout_tracker.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * High-performance mapper for Subscription entities and DTOs
 * Avoids lazy loading issues and provides efficient conversions
 */
@Component
public class SubscriptionMapper {

    // ==================== ENTITY TO DTO MAPPINGS ====================

    /**
     * Convert Subscription entity to SubscriptionResponseDTO
     * Performance optimized - avoids lazy loading
     */
    public SubscriptionResponseDTO toResponseDTO(Subscription subscription) {
        if (subscription == null) return null;

        // Get user ID without triggering lazy load if possible
        Long userId = subscription.getUser() != null ? subscription.getUser().getId() : null;

        return SubscriptionResponseDTO.builder()
                .id(subscription.getId())
                .userId(userId)
                .subscriptionTier(subscription.getSubscriptionTier())
                .status(subscription.getStatus().getDisplayName())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .nextBillingDate(subscription.getNextBillingDate())
                .autoRenew(subscription.getAutoRenew())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())

                // Computed fields
                .tierDisplayName(subscription.getTierDisplayName())
                .monthlyPrice(subscription.getMonthlyPrice())
                .tierDescription(subscription.getSubscriptionTier().getDescription())
                .isActive(subscription.isActive())
                .isPaidTier(subscription.isPaidTier())
                .daysRemaining(subscription.getDaysRemaining())

                // Feature flags (pre-computed for performance)
                .canScheduleWorkouts(subscription.canScheduleWorkouts())
                .canUseAI(subscription.canUseAI())
                .canManageClients(subscription.canManageClients())
                .hasAdvancedAnalytics(subscription.hasAdvancedAnalytics())
                .build();
    }

    /**
     * Convert Subscription entity to lightweight SubscriptionStatusDTO
     * Optimized for frequent status checks
     */
    public SubscriptionStatusDTO toStatusDTO(Subscription subscription) {
        if (subscription == null) return null;

        Long userId = subscription.getUser() != null ? subscription.getUser().getId() : null;
        SubscriptionTier tier = subscription.getSubscriptionTier();

        return SubscriptionStatusDTO.builder()
                .userId(userId)
                .subscriptionTier(tier)
                .status(subscription.getStatus().getDisplayName())
                .isActive(subscription.isActive())
                .isFreeTier(subscription.isFreeTier())
                .isPlusTier(subscription.isPlusTier())
                .isProTier(subscription.isProTier())
                .isProProfessional(subscription.isProProfessional())
                .isPaidTier(subscription.isPaidTier())
                .daysRemaining(subscription.getDaysRemaining())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())

                // Pre-compute all feature flags for performance
                .canScheduleWorkouts(subscription.isActive() && tier.canScheduleWorkouts())
                .canReceiveNotifications(subscription.isActive() && tier.canReceiveNotifications())
                .canCreateRoutines(subscription.isActive() && tier.canCreateRoutines())
                .canTrackConsistency(subscription.isActive() && tier.canTrackConsistency())
                .canCustomizeTemplates(subscription.isActive() && tier.canCustomizeTemplates())
                .canUseAIGeneration(subscription.isActive() && tier.canUseAIGeneration())
                .canAccessSportSpecificPrograms(subscription.isActive() && tier.canAccessSportSpecificPrograms())
                .canUseProgressivePeriodization(subscription.isActive() && tier.canUseProgressivePeriodization())
                .hasAdvancedAnalytics(subscription.isActive() && tier.hasAdvancedAnalytics())
                .canAccessPremiumContent(subscription.isActive() && tier.canAccessPremiumContent())
                .hasNutritionIntegration(subscription.isActive() && tier.hasNutritionIntegration())
                .canManageClients(subscription.isActive() && tier.canManageClients())
                .canAccessClientAnalytics(subscription.isActive() && tier.canAccessClientAnalytics())
                .canScheduleClientSessions(subscription.isActive() && tier.canScheduleClientSessions())
                .canMessageClients(subscription.isActive() && tier.canMessageClients())
                .canAssignPrograms(subscription.isActive() && tier.canAssignPrograms())
                .canViewClientProgress(subscription.isActive() && tier.canViewClientProgress())
                .hasPriorityDirectoryPlacement(subscription.isActive() && tier.hasPriorityDirectoryPlacement())
                .canCreateProfessionalProfile(subscription.isActive() && tier.canCreateProfessionalProfile())
                .hasBusinessInsights(subscription.isActive() && tier.hasBusinessInsights())
                .build();
    }

    /**
     * Convert list of subscriptions to response DTOs
     * Batch operation for performance
     */
    public List<SubscriptionResponseDTO> toResponseDTOList(List<Subscription> subscriptions) {
        if (subscriptions == null) return null;

        return subscriptions.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== DTO TO ENTITY MAPPINGS ====================

    /**
     * Convert SubscriptionCreateRequestDTO to Subscription entity
     * Requires User entity to be provided separately for performance
     */
    public Subscription toEntity(SubscriptionCreateRequestDTO dto, User user) {
        if (dto == null) return null;

        return Subscription.builder()
                .user(user)
                .subscriptionTier(dto.getSubscriptionTier())
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .endDate(dto.getEndDate())
                .autoRenew(dto.getAutoRenew())
                .stripeSubscriptionId(dto.getStripeSubscriptionId())
                .stripeCustomerId(dto.getStripeCustomerId())
                .nextBillingDate(dto.getNextBillingDate())
                .build();
    }

    /**
     * Update existing Subscription entity from SubscriptionUpdateRequestDTO
     * Performance optimized - only updates changed fields
     */
    public void updateEntityFromDTO(Subscription subscription, SubscriptionUpdateRequestDTO dto) {
        if (subscription == null || dto == null) return;

        if (dto.getSubscriptionTier() != null) {
            subscription.setSubscriptionTier(dto.getSubscriptionTier());
        }
        if (dto.getEndDate() != null) {
            subscription.setEndDate(dto.getEndDate());
        }
        if (dto.getAutoRenew() != null) {
            subscription.setAutoRenew(dto.getAutoRenew());
        }
        if (dto.getNextBillingDate() != null) {
            subscription.setNextBillingDate(dto.getNextBillingDate());
        }
        if (dto.getStripeSubscriptionId() != null) {
            subscription.setStripeSubscriptionId(dto.getStripeSubscriptionId());
        }
        if (dto.getStripeCustomerId() != null) {
            subscription.setStripeCustomerId(dto.getStripeCustomerId());
        }
    }

    // ==================== STATISTICS MAPPING ====================

    /**
     * Convert raw statistics data to SubscriptionStatsDTO
     * Optimized for admin dashboard performance
     */
    public SubscriptionStatsDTO toStatsDTO(
            Long totalSubscriptions,
            Long activeSubscriptions,
            Map<String, Long> tierStats,
            Map<String, Long> statusStats,
            Double totalRevenue,
            Long newThisMonth,
            Long cancelledThisMonth) {

        Long freeCount = tierStats.getOrDefault("FREE", 0L);
        Long paidCount = totalSubscriptions - freeCount;

        Double avgRevenue = activeSubscriptions > 0 ? totalRevenue / activeSubscriptions : 0.0;
        Double churnRate = activeSubscriptions > 0 ? (cancelledThisMonth.doubleValue() / activeSubscriptions) * 100 : 0.0;
        Double conversionRate = freeCount > 0 ? (paidCount.doubleValue() / totalSubscriptions) * 100 : 0.0;

        return SubscriptionStatsDTO.builder()
                .totalSubscriptions(totalSubscriptions)
                .activeSubscriptions(activeSubscriptions)
                .freeSubscriptions(freeCount)
                .paidSubscriptions(paidCount)
                .subscriptionsByTier(tierStats)
                .subscriptionsByStatus(statusStats)
                .totalMonthlyRevenue(totalRevenue)
                .averageRevenuePerUser(avgRevenue)
                .newSubscriptionsThisMonth(newThisMonth)
                .cancelledSubscriptionsThisMonth(cancelledThisMonth)
                .churnRate(churnRate)
                .conversionRate(conversionRate)
                .build();
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Create a minimal SubscriptionStatusDTO for quick feature checks
     * Ultra-lightweight for high-frequency operations
     */
    public SubscriptionStatusDTO createQuickStatusDTO(Long userId, SubscriptionTier tier, boolean isActive) {
        return SubscriptionStatusDTO.builder()
                .userId(userId)
                .subscriptionTier(tier)
                .isActive(isActive)
                .isFreeTier(tier == SubscriptionTier.FREE)
                .isPlusTier(tier == SubscriptionTier.PLUS)
                .isProTier(tier == SubscriptionTier.PRO)
                .isProProfessional(tier == SubscriptionTier.PRO_PROFESSIONAL)
                .isPaidTier(tier != SubscriptionTier.FREE)

                // Only compute essential feature flags for quick checks
                .canScheduleWorkouts(isActive && tier.canScheduleWorkouts())
                .canUseAIGeneration(isActive && tier.canUseAIGeneration())
                .canManageClients(isActive && tier.canManageClients())
                .hasAdvancedAnalytics(isActive && tier.hasAdvancedAnalytics())
                .build();
    }

    /**
     * Extract user ID safely without triggering lazy loading
     */
    public Long extractUserId(Subscription subscription) {
        if (subscription == null || subscription.getUser() == null) {
            return null;
        }
        return subscription.getUser().getId();
    }

    /**
     * Check if subscription allows specific feature (performance optimized)
     */
    public boolean checkFeatureAccess(Subscription subscription, String featureName) {
        if (subscription == null || !subscription.isActive()) {
            return false;
        }

        SubscriptionTier tier = subscription.getSubscriptionTier();
        return switch (featureName.toLowerCase()) {
            case "schedule_workouts" -> tier.canScheduleWorkouts();
            case "ai_generation" -> tier.canUseAIGeneration();
            case "manage_clients" -> tier.canManageClients();
            case "advanced_analytics" -> tier.hasAdvancedAnalytics();
            case "premium_content" -> tier.canAccessPremiumContent();
            case "nutrition_integration" -> tier.hasNutritionIntegration();
            default -> false;
        };
    }
}