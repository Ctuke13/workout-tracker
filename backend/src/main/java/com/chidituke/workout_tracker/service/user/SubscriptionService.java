package com.chidituke.workout_tracker.service.user;

import com.chidituke.workout_tracker.dto.request.subscription.SubscriptionCreateRequestDTO;
import com.chidituke.workout_tracker.dto.request.subscription.SubscriptionUpdateRequestDTO;
import com.chidituke.workout_tracker.dto.response.subscription.SubscriptionResponseDTO;
import com.chidituke.workout_tracker.dto.response.subscription.SubscriptionStatusDTO;
import com.chidituke.workout_tracker.dto.response.subscription.SubscriptionStatsDTO;
import com.chidituke.workout_tracker.exceptions.subscription.FeatureNotAvailableException;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.mapper.user.SubscriptionMapper;
import com.chidituke.workout_tracker.model.user.Subscription;
import com.chidituke.workout_tracker.model.user.enums.SubscriptionTier;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.user.SubscriptionRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;

    // ==================== CORE SUBSCRIPTION OPERATIONS ====================

    /**
     * Get user's current subscription (creates FREE if none exists)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "userSubscriptions", key = "#userId")
    public SubscriptionResponseDTO getUserSubscription(Long userId) {
        Optional<Subscription> subscription = subscriptionRepository.findByUserId(userId);

        if (subscription.isPresent()) {
            return subscriptionMapper.toResponseDTO(subscription.get());
        } else {
            // Create and return FREE subscription
            Subscription newSubscription = createFreeSubscription(userId);
            return subscriptionMapper.toResponseDTO(newSubscription);
        }
    }

    /**
     * Get subscription status (lightweight, high-performance)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "subscriptionStatus", key = "#userId")
    public SubscriptionStatusDTO getSubscriptionStatus(Long userId) {
        Optional<Subscription> subscription = subscriptionRepository.findActiveSubscriptionByUserId(userId);

        if (subscription.isPresent()) {
            return subscriptionMapper.toStatusDTO(subscription.get());
        } else {
            // Return default FREE status without creating entity (performance optimization)
            return subscriptionMapper.createQuickStatusDTO(userId, SubscriptionTier.FREE, true);
        }
    }

    /**
     * Create new subscription from DTO
     */
    @CacheEvict(value = {"userSubscriptions", "subscriptionStatus"}, key = "#dto.userId")
    public SubscriptionResponseDTO createSubscription(SubscriptionCreateRequestDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException(dto.getUserId()));

        Subscription subscription = subscriptionMapper.toEntity(dto, user);
        Subscription saved = subscriptionRepository.save(subscription);

        // Update User entity's subscription tier for consistency
        user.setSubscriptionTier(dto.getSubscriptionTier());
        userRepository.save(user);

        log.info("Created {} subscription for user: {}", dto.getSubscriptionTier(), dto.getUserId());
        return subscriptionMapper.toResponseDTO(saved);
    }

    /**
     * Update existing subscription
     */
    @CacheEvict(value = {"userSubscriptions", "subscriptionStatus"}, key = "#userId")
    public SubscriptionResponseDTO updateSubscription(Long userId, SubscriptionUpdateRequestDTO dto) {
        Subscription subscription = findSubscriptionEntity(userId);

        subscriptionMapper.updateEntityFromDTO(subscription, dto);
        Subscription saved = subscriptionRepository.save(subscription);

        // Sync with User entity if tier changed
        if (dto.getSubscriptionTier() != null) {
            User user = subscription.getUser();
            user.setSubscriptionTier(dto.getSubscriptionTier());
            userRepository.save(user);
        }

        log.info("Updated subscription for user: {}", userId);
        return subscriptionMapper.toResponseDTO(saved);
    }

    // ==================== TIER CHECKING METHODS (HIGH PERFORMANCE) ====================

    /**
     * Check if user has FREE tier (cached for performance)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tierChecks", key = "'free:' + #userId")
    public boolean isFreeTier(Long userId) {
        Optional<SubscriptionTier> tier = subscriptionRepository.findActiveSubscriptionTierByUserId(userId);
        return tier.map(t -> t == SubscriptionTier.FREE).orElse(true);
    }

    /**
     * Check if user has PLUS tier or higher (cached for performance)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tierChecks", key = "'plus:' + #userId")
    public boolean isPlusTier(Long userId) {
        Optional<SubscriptionTier> tier = subscriptionRepository.findActiveSubscriptionTierByUserId(userId);
        return tier.map(t -> t.ordinal() >= SubscriptionTier.PLUS.ordinal()).orElse(false);
    }

    /**
     * Check if user has PRO tier or higher (cached for performance)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tierChecks", key = "'pro:' + #userId")
    public boolean isProTier(Long userId) {
        Optional<SubscriptionTier> tier = subscriptionRepository.findActiveSubscriptionTierByUserId(userId);
        return tier.map(t -> t.ordinal() >= SubscriptionTier.PRO.ordinal()).orElse(false);
    }

    /**
     * Check if user has PRO_PROFESSIONAL tier (cached for performance)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tierChecks", key = "'pro_professional:' + #userId")
    public boolean isProProfessional(Long userId) {
        Optional<SubscriptionTier> tier = subscriptionRepository.findActiveSubscriptionTierByUserId(userId);
        return tier.map(t -> t == SubscriptionTier.PRO_PROFESSIONAL).orElse(false);
    }

    /**
     * Check if user has any paid tier (cached for performance)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tierChecks", key = "'paid:' + #userId")
    public boolean isPaidTier(Long userId) {
        return subscriptionRepository.hasActivePaidSubscription(userId);
    }

    // ==================== FEATURE ACCESS METHODS ====================

    /**
     * Check specific feature access with detailed error messaging
     */
    @Transactional(readOnly = true)
    public boolean canAccessFeature(Long userId, String featureName) {
        Optional<Subscription> subscription = subscriptionRepository.findActiveSubscriptionByUserId(userId);

        if (subscription.isEmpty()) {
            return false;
        }

        return subscriptionMapper.checkFeatureAccess(subscription.get(), featureName);
    }

    /**
     * Validate feature access and throw exception if not allowed
     */
    @Transactional(readOnly = true)
    public void validateFeatureAccess(Long userId, String featureName, SubscriptionTier requiredTier) {
        if (!canAccessFeature(userId, featureName)) {
            Optional<SubscriptionTier> currentTier = subscriptionRepository.findActiveSubscriptionTierByUserId(userId);
            String current = currentTier.map(SubscriptionTier::getDisplayName).orElse("Free");
            throw new FeatureNotAvailableException(featureName, requiredTier.getDisplayName() + " (currently: " + current + ")");
        }
    }

    // ==================== SUBSCRIPTION MANAGEMENT ====================

    /**
     * Create free subscription for user (performance optimized)
     */
    public Subscription createFreeSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Subscription subscription = Subscription.builder()
                .user(user)
                .subscriptionTier(SubscriptionTier.FREE)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .build();

        Subscription saved = subscriptionRepository.save(subscription);

        // Update User entity
        user.setSubscriptionTier(SubscriptionTier.FREE);
        userRepository.save(user);

        log.info("Created FREE subscription for user: {}", userId);
        return saved;
    }

    /**
     * Upgrade user subscription with validation
     */
    @CacheEvict(value = {"userSubscriptions", "subscriptionStatus", "tierChecks"}, key = "#userId")
    public SubscriptionResponseDTO upgradeSubscription(Long userId, SubscriptionTier newTier) {
        Subscription subscription = findSubscriptionEntity(userId);

        // Validate upgrade is possible
        if (newTier.ordinal() <= subscription.getSubscriptionTier().ordinal()) {
            throw new IllegalArgumentException("Cannot upgrade to a lower or same tier");
        }

        subscription.upgrade(newTier);
        Subscription saved = subscriptionRepository.save(subscription);

        log.info("Upgraded subscription for user {} to {}", userId, newTier);
        return subscriptionMapper.toResponseDTO(saved);
    }

    /**
     * Cancel user subscription with reason
     */
    @CacheEvict(value = {"userSubscriptions", "subscriptionStatus", "tierChecks"}, key = "#userId")
    public void cancelSubscription(Long userId, String reason) {
        Subscription subscription = findSubscriptionEntity(userId);
        subscription.cancel(reason);
        subscriptionRepository.save(subscription);
        log.info("Cancelled subscription for user: {} - Reason: {}", userId, reason);
    }

    /**
     * Reactivate cancelled subscription
     */
    @CacheEvict(value = {"userSubscriptions", "subscriptionStatus", "tierChecks"}, key = "#userId")
    public SubscriptionResponseDTO reactivateSubscription(Long userId) {
        Subscription subscription = findSubscriptionEntity(userId);
        subscription.reactivate();
        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Reactivated subscription for user: {}", userId);
        return subscriptionMapper.toResponseDTO(saved);
    }

    // ==================== STRIPE INTEGRATION ====================

    /**
     * Update Stripe subscription ID
     */
    @CacheEvict(value = {"userSubscriptions"}, key = "#userId")
    public void updateStripeSubscriptionId(Long userId, String stripeSubscriptionId) {
        Subscription subscription = findSubscriptionEntity(userId);
        subscription.setStripeSubscriptionId(stripeSubscriptionId);
        subscriptionRepository.save(subscription);
    }

    /**
     * Update Stripe customer ID
     */
    @CacheEvict(value = {"userSubscriptions"}, key = "#userId")
    public void updateStripeCustomerId(Long userId, String stripeCustomerId) {
        Subscription subscription = findSubscriptionEntity(userId);
        subscription.setStripeCustomerId(stripeCustomerId);
        subscriptionRepository.save(subscription);
    }

    /**
     * Get subscription by Stripe subscription ID
     */
    @Transactional(readOnly = true)
    public Optional<SubscriptionResponseDTO> getByStripeSubscriptionId(String stripeSubscriptionId) {
        return subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                .map(subscriptionMapper::toResponseDTO);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Check if subscription is active and not expired
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "subscriptionActive", key = "#userId")
    public boolean isSubscriptionActive(Long userId) {
        Optional<Subscription> subscription = subscriptionRepository.findActiveSubscriptionByUserId(userId);
        return subscription.map(Subscription::isActive).orElse(false);
    }

    /**
     * Get days remaining in subscription
     */
    @Transactional(readOnly = true)
    public long getDaysRemaining(Long userId) {
        Subscription subscription = findSubscriptionEntity(userId);
        return subscription.getDaysRemaining();
    }

    // ==================== ADMIN OPERATIONS ====================

    /**
     * Administrative method to expire old subscriptions
     */
    @Transactional
    public void expireOldSubscriptions() {
        List<Subscription> expiredSubs = subscriptionRepository.findExpiredActiveSubscriptions(LocalDateTime.now());

        for (Subscription sub : expiredSubs) {
            sub.setStatus(Subscription.SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);

            // Clear caches for affected users
            Long userId = subscriptionMapper.extractUserId(sub);
            if (userId != null) {
                // Cache eviction is handled by Spring's cache management
            }
        }

        if (!expiredSubs.isEmpty()) {
            log.info("Expired {} old subscriptions", expiredSubs.size());
        }
    }

    /**
     * Get comprehensive subscription statistics (performance optimized)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "subscriptionStats", unless = "#result == null")
    public SubscriptionStatsDTO getSubscriptionStats() {
        // Gather all statistics in efficient queries
        Long totalActive = subscriptionRepository.countActiveSubscriptions();
        Long totalPaid = subscriptionRepository.countActivePaidSubscriptions();
        Double totalRevenue = subscriptionRepository.calculateTotalMonthlyRevenue();

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        Long newThisMonth = subscriptionRepository.countNewSubscriptionsThisMonth(startOfMonth);
        Long cancelledThisMonth = subscriptionRepository.countCancelledSubscriptionsThisMonth(startOfMonth);

        // Get tier and status breakdowns
        Map<String, Long> tierStats = new HashMap<>();
        List<Object[]> tierData = subscriptionRepository.getActiveSubscriptionStatsByTier();
        for (Object[] row : tierData) {
            tierStats.put(row[0].toString(), (Long) row[1]);
        }

        Map<String, Long> statusStats = new HashMap<>();
        List<Object[]> statusData = subscriptionRepository.getSubscriptionStatsByStatus();
        for (Object[] row : statusData) {
            statusStats.put(row[0].toString(), (Long) row[1]);
        }

        return subscriptionMapper.toStatsDTO(
                totalActive, totalActive, tierStats, statusStats,
                totalRevenue != null ? totalRevenue : 0.0,
                newThisMonth, cancelledThisMonth);
    }

    // ==================== BULK OPERATIONS (FOR PERFORMANCE) ====================

    /**
     * Get subscriptions for multiple users (batch operation)
     */
    @Transactional(readOnly = true)
    public List<SubscriptionResponseDTO> getUserSubscriptions(List<Long> userIds) {
        List<Subscription> subscriptions = subscriptionRepository.findActiveSubscriptionsByUserIds(userIds);
        return subscriptionMapper.toResponseDTOList(subscriptions);
    }

    /**
     * Check if users can access specific feature (batch operation)
     */
    @Transactional(readOnly = true)
    public Map<Long, Boolean> checkFeatureAccessForUsers(List<Long> userIds, String featureName) {
        List<Subscription> subscriptions = subscriptionRepository.findActiveSubscriptionsByUserIds(userIds);

        Map<Long, Boolean> results = new HashMap<>();
        for (Subscription sub : subscriptions) {
            Long userId = subscriptionMapper.extractUserId(sub);
            if (userId != null) {
                results.put(userId, subscriptionMapper.checkFeatureAccess(sub, featureName));
            }
        }

        // Add false for users without subscriptions
        for (Long userId : userIds) {
            results.putIfAbsent(userId, false);
        }

        return results;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Find subscription entity or throw exception (internal use)
     */
    private Subscription findSubscriptionEntity(Long userId) {
        return subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("No subscription found for user: " + userId));
    }
}