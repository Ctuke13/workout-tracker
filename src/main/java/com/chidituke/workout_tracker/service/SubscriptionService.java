package com.chidituke.workout_tracker.service;

import com.chidituke.workout_tracker.model.Subscription;
import com.chidituke.workout_tracker.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    /**
     * Get user's current subscription (creates FREE if none exists)
     */
    public Subscription getUserSubscription(Long userId) {
        Optional<Subscription> subscription = subscriptionRepository.findByUserId(userId);

        if (subscription.isPresent()) {
            return subscription.get();
        } else {
            return createFreeSubscription(userId);
        }
    };

    /**
     * Check if user has FREE tier
     */
    public boolean isFreeTier (Long userId) {
        Subscription subscription = getUserSubscription(userId);
        return subscription.getPlanType() == Subscription.PlanType.FREE;
    }

    /**
     * Check if user has PLUS tier or higher
     */
    public boolean isPlusTier (Long userId) {
        Subscription subscription = getUserSubscription(userId);
        return subscription.getPlanType() == Subscription.PlanType.PLUS ||
                subscription.getPlanType() == Subscription.PlanType.PRO;
    }

    /**
     * Check if user has PRO tier
     */
    public boolean isProTier(Long userId) {
        Subscription sub = getUserSubscription(userId);
        return sub.getPlanType() == Subscription.PlanType.PRO;
    }

    public Subscription createFreeSubscription(Long userId) {
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setPlanType(Subscription.PlanType.FREE);
        subscription.setStatus(Subscription.Status.ACTIVE);

        return subscriptionRepository.save(subscription);
    };
}
