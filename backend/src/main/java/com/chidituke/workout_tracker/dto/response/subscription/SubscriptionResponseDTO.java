// ===== SubscriptionResponseDTO.java =====
package com.chidituke.workout_tracker.dto.response.subscription;

import com.chidituke.workout_tracker.model.SubscriptionTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Performance-optimized DTO for subscription responses
 * Avoids lazy loading issues by excluding User entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponseDTO {

    private Long id;
    private Long userId;
    private SubscriptionTier subscriptionTier;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime nextBillingDate;
    private Boolean autoRenew;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields for convenience
    private String tierDisplayName;
    private Double monthlyPrice;
    private String tierDescription;
    private Boolean isActive;
    private Boolean isPaidTier;
    private Long daysRemaining;

    // Feature flags (computed for performance)
    private Boolean canScheduleWorkouts;
    private Boolean canUseAI;
    private Boolean canManageClients;
    private Boolean hasAdvancedAnalytics;
}