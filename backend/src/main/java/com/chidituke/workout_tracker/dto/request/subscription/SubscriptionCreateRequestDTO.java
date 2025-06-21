package com.chidituke.workout_tracker.dto.request.subscription;

import com.chidituke.workout_tracker.model.SubscriptionTier;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating new subscriptions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionCreateRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Subscription tier is required")
    private SubscriptionTier subscriptionTier;

    private LocalDateTime endDate;

    @Builder.Default
    private Boolean autoRenew = true;

    // Stripe integration fields
    private String stripeSubscriptionId;
    private String stripeCustomerId;
    private LocalDateTime nextBillingDate;
}