package com.chidituke.workout_tracker.controller.user;

import com.chidituke.workout_tracker.dto.request.subscription.SubscriptionCreateRequestDTO;
import com.chidituke.workout_tracker.dto.request.subscription.SubscriptionUpdateRequestDTO;
import com.chidituke.workout_tracker.dto.response.subscription.SubscriptionResponseDTO;
import com.chidituke.workout_tracker.dto.response.subscription.SubscriptionStatusDTO;
import com.chidituke.workout_tracker.dto.response.subscription.SubscriptionStatsDTO;
import com.chidituke.workout_tracker.model.user.enums.SubscriptionTier;
import com.chidituke.workout_tracker.service.user.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription Management", description = "APIs for managing user subscriptions and feature access")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    // ==================== CORE SUBSCRIPTION ENDPOINTS ====================

    /**
     * Get user's current subscription details
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user subscription", description = "Retrieve detailed subscription information for a user")
    public ResponseEntity<SubscriptionResponseDTO> getUserSubscription(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        SubscriptionResponseDTO subscription = subscriptionService.getUserSubscription(userId);
        return ResponseEntity.ok(subscription);
    }

    /**
     * Get subscription status (lightweight, high-performance endpoint)
     */
    @GetMapping("/user/{userId}/status")
    @Operation(summary = "Get subscription status", description = "Get lightweight subscription status with feature flags")
    public ResponseEntity<SubscriptionStatusDTO> getSubscriptionStatus(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        SubscriptionStatusDTO status = subscriptionService.getSubscriptionStatus(userId);
        return ResponseEntity.ok(status);
    }

    /**
     * Create new subscription
     */
    @PostMapping
    @Operation(summary = "Create subscription", description = "Create a new subscription for a user")
    public ResponseEntity<SubscriptionResponseDTO> createSubscription(
            @Valid @RequestBody SubscriptionCreateRequestDTO request) {

        SubscriptionResponseDTO created = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update existing subscription
     */
    @PutMapping("/user/{userId}")
    @Operation(summary = "Update subscription", description = "Update an existing user subscription")
    public ResponseEntity<SubscriptionResponseDTO> updateSubscription(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody SubscriptionUpdateRequestDTO request) {

        SubscriptionResponseDTO updated = subscriptionService.updateSubscription(userId, request);
        return ResponseEntity.ok(updated);
    }

    // ==================== SUBSCRIPTION MANAGEMENT ====================

    /**
     * Upgrade user subscription
     */
    @PostMapping("/user/{userId}/upgrade")
    @Operation(summary = "Upgrade subscription", description = "Upgrade user to a higher subscription tier")
    public ResponseEntity<SubscriptionResponseDTO> upgradeSubscription(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "New subscription tier") @RequestParam SubscriptionTier tier) {

        SubscriptionResponseDTO upgraded = subscriptionService.upgradeSubscription(userId, tier);
        return ResponseEntity.ok(upgraded);
    }

    /**
     * Cancel user subscription
     */
    @PostMapping("/user/{userId}/cancel")
    @Operation(summary = "Cancel subscription", description = "Cancel a user's subscription")
    public ResponseEntity<String> cancelSubscription(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Cancellation reason") @RequestParam(required = false) String reason) {

        subscriptionService.cancelSubscription(userId, reason != null ? reason : "User requested");
        return ResponseEntity.ok("Subscription cancelled successfully");
    }

    /**
     * Reactivate cancelled subscription
     */
    @PostMapping("/user/{userId}/reactivate")
    @Operation(summary = "Reactivate subscription", description = "Reactivate a cancelled subscription")
    public ResponseEntity<SubscriptionResponseDTO> reactivateSubscription(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        SubscriptionResponseDTO reactivated = subscriptionService.reactivateSubscription(userId);
        return ResponseEntity.ok(reactivated);
    }

    // ==================== FEATURE ACCESS ENDPOINTS ====================

    /**
     * Check if user can access specific feature
     */
    @GetMapping("/user/{userId}/feature/{featureName}")
    @Operation(summary = "Check feature access", description = "Check if user can access a specific feature")
    public ResponseEntity<Boolean> checkFeatureAccess(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Feature name") @PathVariable String featureName) {

        boolean canAccess = subscriptionService.canAccessFeature(userId, featureName);
        return ResponseEntity.ok(canAccess);
    }

    /**
     * Validate feature access (throws exception if not allowed)
     */
    @PostMapping("/user/{userId}/validate/{featureName}")
    @Operation(summary = "Validate feature access", description = "Validate feature access and return error if not allowed")
    public ResponseEntity<String> validateFeatureAccess(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Feature name") @PathVariable String featureName,
            @Parameter(description = "Required tier") @RequestParam SubscriptionTier requiredTier) {

        subscriptionService.validateFeatureAccess(userId, featureName, requiredTier);
        return ResponseEntity.ok("Feature access validated successfully");
    }

    /**
     * Batch check feature access for multiple users
     */
    @PostMapping("/users/feature/{featureName}")
    @Operation(summary = "Batch check feature access", description = "Check feature access for multiple users")
    public ResponseEntity<Map<Long, Boolean>> checkFeatureAccessBatch(
            @Parameter(description = "Feature name") @PathVariable String featureName,
            @Parameter(description = "List of user IDs") @RequestBody List<Long> userIds) {

        Map<Long, Boolean> results = subscriptionService.checkFeatureAccessForUsers(userIds, featureName);
        return ResponseEntity.ok(results);
    }

    // ==================== TIER CHECKING ENDPOINTS (HIGH PERFORMANCE) ====================

    /**
     * Check if user has FREE tier
     */
    @GetMapping("/user/{userId}/tier/free")
    @Operation(summary = "Check FREE tier", description = "Check if user has FREE tier")
    public ResponseEntity<Boolean> isFreeTier(@PathVariable Long userId) {
        boolean isFree = subscriptionService.isFreeTier(userId);
        return ResponseEntity.ok(isFree);
    }

    /**
     * Check if user has PLUS tier or higher
     */
    @GetMapping("/user/{userId}/tier/plus")
    @Operation(summary = "Check PLUS tier", description = "Check if user has PLUS tier or higher")
    public ResponseEntity<Boolean> isPlusTier(@PathVariable Long userId) {
        boolean isPlus = subscriptionService.isPlusTier(userId);
        return ResponseEntity.ok(isPlus);
    }

    /**
     * Check if user has PRO tier or higher
     */
    @GetMapping("/user/{userId}/tier/pro")
    @Operation(summary = "Check PRO tier", description = "Check if user has PRO tier or higher")
    public ResponseEntity<Boolean> isProTier(@PathVariable Long userId) {
        boolean isPro = subscriptionService.isProTier(userId);
        return ResponseEntity.ok(isPro);
    }

    /**
     * Check if user has PRO_PROFESSIONAL tier
     */
    @GetMapping("/user/{userId}/tier/pro-professional")
    @Operation(summary = "Check PRO_PROFESSIONAL tier", description = "Check if user has PRO_PROFESSIONAL tier")
    public ResponseEntity<Boolean> isProProfessional(@PathVariable Long userId) {
        boolean isProProfessional = subscriptionService.isProProfessional(userId);
        return ResponseEntity.ok(isProProfessional);
    }

    /**
     * Check if user has any paid tier
     */
    @GetMapping("/user/{userId}/tier/paid")
    @Operation(summary = "Check paid tier", description = "Check if user has any paid subscription tier")
    public ResponseEntity<Boolean> isPaidTier(@PathVariable Long userId) {
        boolean isPaid = subscriptionService.isPaidTier(userId);
        return ResponseEntity.ok(isPaid);
    }

    // ==================== BATCH OPERATIONS ====================

    /**
     * Get subscriptions for multiple users
     */
    @PostMapping("/users/bulk")
    @Operation(summary = "Get bulk subscriptions", description = "Get subscription details for multiple users")
    public ResponseEntity<List<SubscriptionResponseDTO>> getUserSubscriptionsBulk(
            @Parameter(description = "List of user IDs") @RequestBody List<Long> userIds) {

        List<SubscriptionResponseDTO> subscriptions = subscriptionService.getUserSubscriptions(userIds);
        return ResponseEntity.ok(subscriptions);
    }

    // ==================== STRIPE INTEGRATION ENDPOINTS ====================

    /**
     * Update Stripe subscription ID
     */
    @PutMapping("/user/{userId}/stripe/subscription")
    @Operation(summary = "Update Stripe subscription ID", description = "Update the Stripe subscription ID for a user")
    public ResponseEntity<String> updateStripeSubscriptionId(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Stripe subscription ID") @RequestParam String stripeSubscriptionId) {

        subscriptionService.updateStripeSubscriptionId(userId, stripeSubscriptionId);
        return ResponseEntity.ok("Stripe subscription ID updated successfully");
    }

    /**
     * Update Stripe customer ID
     */
    @PutMapping("/user/{userId}/stripe/customer")
    @Operation(summary = "Update Stripe customer ID", description = "Update the Stripe customer ID for a user")
    public ResponseEntity<String> updateStripeCustomerId(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Stripe customer ID") @RequestParam String stripeCustomerId) {

        subscriptionService.updateStripeCustomerId(userId, stripeCustomerId);
        return ResponseEntity.ok("Stripe customer ID updated successfully");
    }

    /**
     * Get subscription by Stripe subscription ID
     */
    @GetMapping("/stripe/{stripeSubscriptionId}")
    @Operation(summary = "Get subscription by Stripe ID", description = "Get subscription details using Stripe subscription ID")
    public ResponseEntity<SubscriptionResponseDTO> getByStripeSubscriptionId(
            @Parameter(description = "Stripe subscription ID") @PathVariable String stripeSubscriptionId) {

        return subscriptionService.getByStripeSubscriptionId(stripeSubscriptionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Get subscription statistics (admin only)
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get subscription statistics", description = "Get comprehensive subscription statistics (admin only)")
    public ResponseEntity<SubscriptionStatsDTO> getSubscriptionStats() {
        SubscriptionStatsDTO stats = subscriptionService.getSubscriptionStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Expire old subscriptions (admin only)
     */
    @PostMapping("/admin/expire-old")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Expire old subscriptions", description = "Manually expire old subscriptions (admin only)")
    public ResponseEntity<String> expireOldSubscriptions() {
        subscriptionService.expireOldSubscriptions();
        return ResponseEntity.ok("Old subscriptions expired successfully");
    }

    // ==================== LEGACY TEST ENDPOINTS (KEPT FOR COMPATIBILITY) ====================

    /**
     * Test endpoint - check subscription status for user ID
     */
    @GetMapping("/test/{userId}")
    @Operation(summary = "Test subscription status", description = "Test endpoint for checking subscription status")
    public ResponseEntity<String> testSubscription(@PathVariable Long userId) {
        boolean isFree = subscriptionService.isFreeTier(userId);
        boolean isPlus = subscriptionService.isPlusTier(userId);
        boolean isPro = subscriptionService.isProTier(userId);
        boolean isProProfessional = subscriptionService.isProProfessional(userId);

        String result = String.format(
                "User %d: FREE=%s, PLUS=%s, PRO=%s, PRO_PROFESSIONAL=%s",
                userId, isFree, isPlus, isPro, isProProfessional
        );

        return ResponseEntity.ok(result);
    }

    /**
     * Simple test endpoint - check if user is FREE tier
     */
    @GetMapping("/test/{userId}/is-free")
    @Operation(summary = "Test FREE tier", description = "Test endpoint for checking FREE tier")
    public ResponseEntity<Boolean> testIsFreeTier(@PathVariable Long userId) {
        boolean isFree = subscriptionService.isFreeTier(userId);
        return ResponseEntity.ok(isFree);
    }

    /**
     * Test endpoint - check if user has paid tier
     */
    @GetMapping("/test/{userId}/is-paid")
    @Operation(summary = "Test paid tier", description = "Test endpoint for checking paid tier")
    public ResponseEntity<Boolean> testIsPaidTier(@PathVariable Long userId) {
        boolean isPaid = subscriptionService.isPaidTier(userId);
        return ResponseEntity.ok(isPaid);
    }
}