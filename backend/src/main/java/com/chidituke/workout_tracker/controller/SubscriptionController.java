package com.chidituke.workout_tracker.controller;

import com.chidituke.workout_tracker.model.Subscription;
import com.chidituke.workout_tracker.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Get user's current subscription
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Subscription> getUserSubscription(@PathVariable Long userId) {
        try {
            Subscription subscription = subscriptionService.getUserSubscription(userId);
            return ResponseEntity.ok(subscription);
        } catch (Exception e) {
            log.error("Error getting subscription for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Check subscription status for user
     */
    @GetMapping("/user/{userId}/status")
    public ResponseEntity<Map<String, Object>> getSubscriptionStatus(@PathVariable Long userId) {
        try {
            Subscription subscription = subscriptionService.getUserSubscription(userId);

            Map<String, Object> status = Map.of(
                    "planType", subscription.getPlanType(),
                    "status", subscription.getStatus(),
                    "isActive", subscription.isActive(),
                    "isFreeTier", subscription.isFreeTier(),
                    "isPlusTier", subscription.isPlusTier(),
                    "isProTier", subscription.isProTier(),
                    "daysRemaining", subscription.getDaysRemaining(),
                    "startDate", subscription.getStartDate(),
                    "endDate", subscription.getEndDate()
            );

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting subscription status for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Upgrade user subscription
     */
    @PostMapping("/user/{userId}/upgrade")
    public ResponseEntity<Subscription> upgradeSubscription(
            @PathVariable Long userId,
            @RequestParam String planType) {
        try {
            Subscription.PlanType newPlanType = Subscription.PlanType.valueOf(planType.toUpperCase());
            Subscription updated = subscriptionService.upgradeSubscription(userId, newPlanType);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error upgrading subscription for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cancel user subscription
     */
    @PostMapping("/user/{userId}/cancel")
    public ResponseEntity<String> cancelSubscription(
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        try {
            subscriptionService.cancelSubscription(userId, reason != null ? reason : "User requested");
            return ResponseEntity.ok("Subscription cancelled successfully");
        } catch (Exception e) {
            log.error("Error cancelling subscription for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Reactivate cancelled subscription
     */
    @PostMapping("/user/{userId}/reactivate")
    public ResponseEntity<Subscription> reactivateSubscription(@PathVariable Long userId) {
        try {
            Subscription reactivated = subscriptionService.reactivateSubscription(userId);
            return ResponseEntity.ok(reactivated);
        } catch (Exception e) {
            log.error("Error reactivating subscription for user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== YOUR EXISTING TEST ENDPOINTS (KEPT) ====================

    /**
     * Test endpoint - check subscription status for user ID
     */
    @GetMapping("/test/{userId}")
    public ResponseEntity<String> testSubscription(@PathVariable Long userId) {
        try {
            boolean isFree = subscriptionService.isFreeTier(userId);
            boolean isPlus = subscriptionService.isPlusTier(userId);
            boolean isPro = subscriptionService.isProTier(userId);

            String result = String.format(
                    "User %d: FREE=%s, PLUS=%s, PRO=%s",
                    userId, isFree, isPlus, isPro
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error in subscription test for user {}: {}", userId, e.getMessage());
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    /**
     * Simple test endpoint - check if user is FREE tier
     */
    @GetMapping("/test/{userId}/is-free")
    public ResponseEntity<Boolean> testIsFreeTier(@PathVariable Long userId) {
        try {
            boolean isFree = subscriptionService.isFreeTier(userId);
            return ResponseEntity.ok(isFree);
        } catch (Exception e) {
            log.error("Error checking if user {} is free tier: {}", userId, e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    /**
     * Test endpoint - check if user has paid tier
     */
    @GetMapping("/test/{userId}/is-paid")
    public ResponseEntity<Boolean> testIsPaidTier(@PathVariable Long userId) {
        try {
            boolean isPaid = subscriptionService.isPaidTier(userId);
            return ResponseEntity.ok(isPaid);
        } catch (Exception e) {
            log.error("Error checking if user {} has paid tier: {}", userId, e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Admin endpoint - get subscription statistics
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<?> getSubscriptionStats() {
        try {
            return ResponseEntity.ok(subscriptionService.getSubscriptionStats());
        } catch (Exception e) {
            log.error("Error getting subscription stats: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Admin endpoint - expire old subscriptions
     */
    @PostMapping("/admin/expire-old")
    public ResponseEntity<String> expireOldSubscriptions() {
        try {
            subscriptionService.expireOldSubscriptions();
            return ResponseEntity.ok("Old subscriptions expired successfully");
        } catch (Exception e) {
            log.error("Error expiring old subscriptions: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}