package com.chidituke.workout_tracker.dto.response.subscription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for subscription statistics (admin use)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatsDTO {

    private Long totalSubscriptions;
    private Long activeSubscriptions;
    private Long freeSubscriptions;
    private Long paidSubscriptions;

    private Map<String, Long> subscriptionsByTier;
    private Map<String, Long> subscriptionsByStatus;

    private Double totalMonthlyRevenue;
    private Double averageRevenuePerUser;

    // Growth metrics
    private Long newSubscriptionsThisMonth;
    private Long cancelledSubscriptionsThisMonth;
    private Double churnRate;
    private Double conversionRate; // free to paid
}