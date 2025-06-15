package com.chidituke.workout_tracker.model;

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
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    @Builder.Default
    private PlanType planType = PlanType.FREE;

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

    // Business Logic Methods
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

    public boolean isFreeTier() {
        return planType == PlanType.FREE;
    }

    public boolean isPlusTier() {
        return planType == PlanType.PLUS;
    }

    public boolean isProTier() {
        return planType == PlanType.PRO;
    }

    public boolean isPaidTier() {
        return planType == PlanType.PLUS || planType == PlanType.PRO || planType == PlanType.PRO_PROFESSIONAL;
    }

    public boolean isProProfessional() {
        return planType == PlanType.PRO_PROFESSIONAL;
    }

    public boolean canCreatePrograms() {
        return planType == PlanType.PRO_PROFESSIONAL;
    }

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

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum PlanType {
        FREE("Free Plan", 0.0),
        PLUS("Plus Plan", 5.0),
        PRO("Pro Plan", 13.0),
        PRO_PROFESSIONAL("Pro Professional", 25.0);

        private final String displayName;
        private final double price;

        PlanType(String displayName, double price) {
            this.displayName = displayName;
            this.price = price;
        }

        public String getDisplayName() {
            return displayName;
        }

        public double getPrice() {
            return price;
        }
    }

    public enum SubscriptionStatus {
        ACTIVE("Active"),
        CANCELLED("Cancelled"),
        EXPIRED("Expired");

        private final String displayName;

        SubscriptionStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}