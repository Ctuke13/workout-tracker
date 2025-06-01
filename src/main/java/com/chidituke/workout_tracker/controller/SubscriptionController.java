package com.chidituke.workout_tracker.controller;

import com.chidituke.workout_tracker.model.Subscription;
import com.chidituke.workout_tracker.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

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
            System.out.println("Error in subscription test:  " + e.getMessage());
            e.printStackTrace();
            return  ResponseEntity.ok("Error: " + e.getMessage());
        }
    };

    /**
     * Simple test endpoint - check if user is FREE tier
     */
    @GetMapping("/test/{userId}/is-free")
    public ResponseEntity<Boolean> testIsFreeTier(@PathVariable Long userId) {
        boolean isFree = subscriptionService.isFreeTier(userId);
        return ResponseEntity.ok(isFree);
    }
}
