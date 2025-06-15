package com.chidituke.workout_tracker.service;

import com.chidituke.workout_tracker.model.Subscription;
import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.repository.SubscriptionRepository;
import com.chidituke.workout_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    /**
     * Get user's current subscription (creates FREE if none exists)
     */
    @Transactional(readOnly = true)
    public Subscription getUserSubscription(Long userId) {
        Optional<Subscription> subscription = subscriptionRepository.findByUserId(userId);

        if (subscription.isPresent()) {
            return subscription.get();
        } else {
            return createFreeSubscription(userId);
        }
    }

    /**
     * Check if user has FREE tier
     */
    @Transactional(readOnly = true)
    public boolean isFreeTier(Long userId) {
        Subscription subscription = getUserSubscription(userId);
        return subscription.isFreeTier();
    }

    /**
     * Check if user has PLUS tier or higher
     */
    @Transactional(readOnly = true)
    public boolean isPlusTier(Long userId) {
        Subscription subscription = getUserSubscription(userId);
        return subscription.isPlusTier() || subscription.isProTier();
    }

    /**
     * Check if user has PRO tier
     */
    @Transactional(readOnly = true)
    public boolean isProTier(Long userId) {
        Subscription subscription = getUserSubscription(userId);
        return subscription.isProTier();
    }

    /**
     * Check if user has any paid tier (PLUS or PRO)
     */
    @Transactional(readOnly = true)
    public boolean isPaidTier(Long userId) {
        Subscription subscription = getUserSubscription(userId);
        return subscription.isPaidTier();
    }

    /**
     * Create free subscription for user (FIXED: properly sets User object)
     */
    public Subscription createFreeSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Subscription subscription = Subscription.builder()
                .user(user)
                .planType(Subscription.PlanType.FREE)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .build();

        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Created FREE subscription for user: {}", userId);
        return saved;
    }

    /**
     * Upgrade user subscription
     */
    public Subscription upgradeSubscription(Long userId, Subscription.PlanType newPlanType) {
        Subscription subscription = getUserSubscription(userId);

        subscription.setPlanType(newPlanType);
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscription.setUpdatedAt(LocalDateTime.now());

        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Upgraded subscription for user {} to {}", userId, newPlanType);
        return saved;
    }

    /**
     * Cancel user subscription
     */
    public void cancelSubscription(Long userId, String reason) {
        Subscription subscription = getUserSubscription(userId);
        subscription.cancel(reason);
        subscriptionRepository.save(subscription);
        log.info("Cancelled subscription for user: {} - Reason: {}", userId, reason);
    }

    /**
     * Reactivate cancelled subscription
     */
    public Subscription reactivateSubscription(Long userId) {
        Subscription subscription = getUserSubscription(userId);
        subscription.reactivate();
        Subscription saved = subscriptionRepository.save(subscription);
        log.info("Reactivated subscription for user: {}", userId);
        return saved;
    }

    /**
     * Update Stripe subscription ID
     */
    public void updateStripeSubscriptionId(Long userId, String stripeSubscriptionId) {
        Subscription subscription = getUserSubscription(userId);
        subscription.setStripeSubscriptionId(stripeSubscriptionId);
        subscriptionRepository.save(subscription);
    }

    /**
     * Update Stripe customer ID
     */
    public void updateStripeCustomerId(Long userId, String stripeCustomerId) {
        Subscription subscription = getUserSubscription(userId);
        subscription.setStripeCustomerId(stripeCustomerId);
        subscriptionRepository.save(subscription);
    }

    /**
     * Get subscription by Stripe subscription ID
     */
    @Transactional(readOnly = true)
    public Optional<Subscription> getByStripeSubscriptionId(String stripeSubscriptionId) {
        return subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId);
    }

    /**
     * Check if subscription is active and not expired
     */
    @Transactional(readOnly = true)
    public boolean isSubscriptionActive(Long userId) {
        Subscription subscription = getUserSubscription(userId);
        return subscription.isActive();
    }

    /**
     * Get days remaining in subscription
     */
    @Transactional(readOnly = true)
    public long getDaysRemaining(Long userId) {
        Subscription subscription = getUserSubscription(userId);
        return subscription.getDaysRemaining();
    }

    /**
     * Administrative method to expire old subscriptions
     */
    @Transactional
    public void expireOldSubscriptions() {
        List<Subscription> expiredSubs = subscriptionRepository.findExpiredActiveSubscriptions(LocalDateTime.now());

        for (Subscription sub : expiredSubs) {
            sub.setStatus(Subscription.SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(sub);
        }

        if (!expiredSubs.isEmpty()) {
            log.info("Expired {} old subscriptions", expiredSubs.size());
        }
    }

    /**
     * Get subscription statistics
     */
    @Transactional(readOnly = true)
    public List<Object[]> getSubscriptionStats() {
        return subscriptionRepository.getActiveSubscriptionStats();
    }
}