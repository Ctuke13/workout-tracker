package com.chidituke.workout_tracker.repository;

import com.chidituke.workout_tracker.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // Find subscription by user ID (FIXED: was s.userId, now s.user.id)
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId")
    Optional<Subscription> findByUserId(@Param("userId") Long userId);

    // Check if user has active subscription (FIXED: query syntax)
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId);

    // Check if user has specific plan type and status (FIXED: enum reference)
    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.user.id = :userId AND s.planType = :planType AND s.status = :status")
    boolean existsByUserIdAndPlanTypeAndStatus(@Param("userId") Long userId,
                                               @Param("planType") Subscription.PlanType planType,
                                               @Param("status") Subscription.SubscriptionStatus status);

    // Find by Stripe subscription ID
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    // Find by Stripe customer ID
    List<Subscription> findByStripeCustomerId(String stripeCustomerId);

    // Find all active subscriptions
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE'")
    List<Subscription> findAllActiveSubscriptions();

    // Find expired subscriptions that need cleanup
    @Query("SELECT s FROM Subscription s WHERE s.endDate < :now AND s.status = 'ACTIVE'")
    List<Subscription> findExpiredActiveSubscriptions(@Param("now") LocalDateTime now);

    // Get subscription stats by plan type
    @Query("SELECT s.planType, COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE' GROUP BY s.planType")
    List<Object[]> getActiveSubscriptionStats();
}