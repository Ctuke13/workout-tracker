package com.chidituke.workout_tracker.model.user;

import com.chidituke.workout_tracker.model.user.enums.SubscriptionTier;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_tier", nullable = false)
    @Builder.Default
    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "start_date", nullable = false)
    @Builder.Default
    private LocalDateTime startDate = LocalDateTime.now();

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Stripe Integration
    @Column(name = "stripe_subscription_id", unique = true)
    private String stripeSubscriptionId;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    // Additional fields
    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    @Column(name = "auto_renew")
    @Builder.Default
    private Boolean autoRenew = true;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ==================== SUBSCRIPTION STATUS METHODS ====================

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE &&
                (endDate == null || endDate.isAfter(LocalDateTime.now()));
    }

    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDateTime.now()) ||
                status == SubscriptionStatus.EXPIRED;
    }

    public boolean isCancelled() {
        return status == SubscriptionStatus.CANCELLED;
    }

    public boolean isPending() {
        return status == SubscriptionStatus.PENDING;
    }

    // ==================== TIER CHECKING METHODS ====================

    public boolean isFreeTier() {
        return subscriptionTier == SubscriptionTier.FREE;
    }

    public boolean isPlusTier() {
        return subscriptionTier == SubscriptionTier.PLUS;
    }

    public boolean isProTier() {
        return subscriptionTier == SubscriptionTier.PRO;
    }

    public boolean isProProfessional() {
        return subscriptionTier == SubscriptionTier.PRO_PROFESSIONAL;
    }

    public boolean isPaidTier() {
        return subscriptionTier != SubscriptionTier.FREE;
    }

    // ==================== FEATURE ACCESS METHODS ====================

    public boolean canScheduleWorkouts() {
        return isActive() && subscriptionTier.canScheduleWorkouts();
    }

    public boolean canUseAI() {
        return isActive() && subscriptionTier.canUseAIGeneration();
    }

    public boolean canManageClients() {
        return isActive() && subscriptionTier.canManageClients();
    }

    public boolean hasAdvancedAnalytics() {
        return isActive() && subscriptionTier.hasAdvancedAnalytics();
    }

    // ==================== SUBSCRIPTION MANAGEMENT ====================

    public long getDaysRemaining() {
        if (endDate == null) return Long.MAX_VALUE;
        return java.time.Duration.between(LocalDateTime.now(), endDate).toDays();
    }

    public void cancel(String reason) {
        this.status = SubscriptionStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = LocalDateTime.now();
        this.autoRenew = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void reactivate() {
        if (this.status == SubscriptionStatus.CANCELLED) {
            this.status = SubscriptionStatus.ACTIVE;
            this.cancellationReason = null;
            this.cancelledAt = null;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void upgrade(SubscriptionTier newTier) {
        if (newTier.ordinal() > this.subscriptionTier.ordinal()) {
            this.subscriptionTier = newTier;
            this.updatedAt = LocalDateTime.now();
            // Sync with User entity
            if (user != null) {
                user.setSubscriptionTier(newTier);
            }
        }
    }

    public void downgrade(SubscriptionTier newTier) {
        if (newTier.ordinal() < this.subscriptionTier.ordinal()) {
            this.subscriptionTier = newTier;
            this.updatedAt = LocalDateTime.now();
            // Sync with User entity
            if (user != null) {
                user.setSubscriptionTier(newTier);
            }
        }
    }

    public double getMonthlyPrice() {
        return subscriptionTier.getMonthlyPrice();
    }

    public String getTierDisplayName() {
        return subscriptionTier.getDisplayName();
    }

    // ==================== BUSINESS LOGIC ====================

    public boolean canUpgradeTo(SubscriptionTier targetTier) {
        return targetTier.ordinal() > this.subscriptionTier.ordinal();
    }

    public boolean canDowngradeTo(SubscriptionTier targetTier) {
        return targetTier.ordinal() < this.subscriptionTier.ordinal();
    }

    public boolean isEligibleForTrial(SubscriptionTier tier) {
        // Users who've never had a paid subscription can get trials
        return isFreeTier() && tier != SubscriptionTier.FREE;
    }

    // ==================== LEGACY METHODS (for backward compatibility) ====================

    /**
     * @deprecated Use canManageClients() instead
     */
    @Deprecated
    public boolean canCreatePrograms() {
        return canManageClients();
    }

    // ==================== LIFECYCLE METHODS ====================

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();

        // Sync subscription tier with User entity
        if (user != null && user.getSubscriptionTier() != this.subscriptionTier) {
            user.setSubscriptionTier(this.subscriptionTier);
        }
    }

    @PrePersist
    protected void onCreate() {
        // Sync subscription tier with User entity on creation
        if (user != null) {
            user.setSubscriptionTier(this.subscriptionTier);
        }
    }

    // ==================== ENUMS ====================

    public enum SubscriptionStatus {
        ACTIVE("Active"),
        CANCELLED("Cancelled"),
        EXPIRED("Expired"),
        PENDING("Pending");

        private final String displayName;

        SubscriptionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}