package com.chidituke.workout_tracker.dto.request.subscription;

import com.chidituke.workout_tracker.model.SubscriptionTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for updating existing subscriptions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionUpdateRequestDTO {

    private SubscriptionTier subscriptionTier;
    private LocalDateTime endDate;
    private Boolean autoRenew;
    private LocalDateTime nextBillingDate;

    // Stripe integration fields
    private String stripeSubscriptionId;
    private String stripeCustomerId;
}