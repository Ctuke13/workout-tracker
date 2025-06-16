package com.chidituke.workout_tracker.repository;

import com.chidituke.workout_tracker.model.Subscription;
import com.chidituke.workout_tracker.model.SubscriptionTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // ==================== USER SUBSCRIPTION QUERIES ====================

    /**
     * Find subscription by user ID (performance optimized)
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId")
    Optional<Subscription> findByUserId(@Param("userId") Long userId);

    /**
     * Find active subscription by user ID (most common query)
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId);

    /**
     * Check if user has specific tier and status (performance optimized boolean query)
     */
    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.user.id = :userId AND s.subscriptionTier = :tier AND s.status = :status")
    boolean existsByUserIdAndTierAndStatus(@Param("userId") Long userId,
                                           @Param("tier") SubscriptionTier tier,
                                           @Param("status") Subscription.SubscriptionStatus status);

    /**
     * Get user's subscription tier efficiently (for quick feature checks)
     */
    @Query("SELECT s.subscriptionTier FROM Subscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE'")
    Optional<SubscriptionTier> findActiveSubscriptionTierByUserId(@Param("userId") Long userId);

    /**
     * Check if user has active paid subscription (performance optimized)
     */
    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.user.id = :userId AND s.status = 'ACTIVE' AND s.subscriptionTier != 'FREE'")
    boolean hasActivePaidSubscription(@Param("userId") Long userId);

    // ==================== STRIPE INTEGRATION QUERIES ====================

    /**
     * Find by Stripe subscription ID
     */
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    /**
     * Find by Stripe customer ID
     */
    List<Subscription> findByStripeCustomerId(String stripeCustomerId);

    /**
     * Find active subscription by Stripe customer ID (for webhooks)
     */
    @Query("SELECT s FROM Subscription s WHERE s.stripeCustomerId = :customerId AND s.status = 'ACTIVE'")
    Optional<Subscription> findActiveByStripeCustomerId(@Param("customerId") String customerId);

    // ==================== ADMIN & MANAGEMENT QUERIES ====================

    /**
     * Find all active subscriptions (for admin dashboard)
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE'")
    List<Subscription> findAllActiveSubscriptions();

    /**
     * Find expired subscriptions that need cleanup
     */
    @Query("SELECT s FROM Subscription s WHERE s.endDate < :now AND s.status = 'ACTIVE'")
    List<Subscription> findExpiredActiveSubscriptions(@Param("now") LocalDateTime now);

    /**
     * Find subscriptions expiring soon (for notifications)
     */
    @Query("SELECT s FROM Subscription s WHERE s.endDate BETWEEN :start AND :end AND s.status = 'ACTIVE'")
    List<Subscription> findSubscriptionsExpiringSoon(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    // ==================== STATISTICS QUERIES (PERFORMANCE OPTIMIZED) ====================

    /**
     * Get subscription stats by tier (for admin dashboard)
     */
    @Query("SELECT s.subscriptionTier, COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE' GROUP BY s.subscriptionTier")
    List<Object[]> getActiveSubscriptionStatsByTier();

    /**
     * Get subscription stats by status
     */
    @Query("SELECT s.status, COUNT(s) FROM Subscription s GROUP BY s.status")
    List<Object[]> getSubscriptionStatsByStatus();

    /**
     * Count total active subscriptions (cached query)
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE'")
    Long countActiveSubscriptions();

    /**
     * Count active paid subscriptions
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = 'ACTIVE' AND s.subscriptionTier != 'FREE'")
    Long countActivePaidSubscriptions();

    /**
     * Calculate total monthly revenue from active subscriptions
     */
    @Query("SELECT SUM(CASE " +
            "WHEN s.subscriptionTier = 'PLUS' THEN 4.99 " +
            "WHEN s.subscriptionTier = 'PRO' THEN 12.99 " +
            "WHEN s.subscriptionTier = 'PRO_PROFESSIONAL' THEN 24.99 " +
            "ELSE 0 END) " +
            "FROM Subscription s WHERE s.status = 'ACTIVE'")
    Double calculateTotalMonthlyRevenue();

    /**
     * Get new subscriptions this month
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.createdAt >= :startOfMonth")
    Long countNewSubscriptionsThisMonth(@Param("startOfMonth") LocalDateTime startOfMonth);

    /**
     * Get cancelled subscriptions this month
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.cancelledAt >= :startOfMonth")
    Long countCancelledSubscriptionsThisMonth(@Param("startOfMonth") LocalDateTime startOfMonth);

    // ==================== FEATURE ACCESS QUERIES (HIGH PERFORMANCE) ====================

    /**
     * Users who can schedule workouts (PLUS+)
     */
    @Query("SELECT s.user.id FROM Subscription s WHERE s.status = 'ACTIVE' AND s.subscriptionTier IN ('PLUS', 'PRO', 'PRO_PROFESSIONAL')")
    List<Long> findUsersWhoCanScheduleWorkouts();

    /**
     * Users who can use AI (PRO+)
     */
    @Query("SELECT s.user.id FROM Subscription s WHERE s.status = 'ACTIVE' AND s.subscriptionTier IN ('PRO', 'PRO_PROFESSIONAL')")
    List<Long> findUsersWhoCanUseAI();

    /**
     * Users who can manage clients (PRO_PROFESSIONAL only)
     */
    @Query("SELECT s.user.id FROM Subscription s WHERE s.status = 'ACTIVE' AND s.subscriptionTier = 'PRO_PROFESSIONAL'")
    List<Long> findUsersWhoCanManageClients();

    // ==================== BULK OPERATIONS (FOR PERFORMANCE) ====================

    /**
     * Find subscriptions by multiple user IDs (for batch operations)
     */
    @Query("SELECT s FROM Subscription s WHERE s.user.id IN :userIds AND s.status = 'ACTIVE'")
    List<Subscription> findActiveSubscriptionsByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * Find subscriptions that need billing today
     */
    @Query("SELECT s FROM Subscription s WHERE DATE(s.nextBillingDate) = DATE(:today) AND s.status = 'ACTIVE'")
    List<Subscription> findSubscriptionsForBillingToday(@Param("today") LocalDateTime today);

    /**
     * Update multiple subscriptions to expired status (bulk operation)
     */
    @Query("UPDATE Subscription s SET s.status = 'EXPIRED' WHERE s.id IN :ids")
    void bulkExpireSubscriptions(@Param("ids") List<Long> ids);

    // ==================== CUSTOM QUERIES FOR SPECIFIC USE CASES ====================

    /**
     * Find free users who might be good candidates for upgrade
     */
    @Query("SELECT s FROM Subscription s WHERE s.subscriptionTier = 'FREE' AND s.createdAt <= :cutoffDate AND s.status = 'ACTIVE'")
    List<Subscription> findFreeUsersForUpgradeTargeting(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find professional users with active subscriptions (for directory)
     */
    @Query("SELECT s FROM Subscription s JOIN s.user u WHERE s.status = 'ACTIVE' AND s.subscriptionTier = 'PRO_PROFESSIONAL' AND u.userType = 'PROFESSIONAL'")
    List<Subscription> findActiveProfessionalSubscriptions();

    /**
     * Check if user has ever had a paid subscription (for trial eligibility)
     */
    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.user.id = :userId AND s.subscriptionTier != 'FREE'")
    boolean hasEverHadPaidSubscription(@Param("userId") Long userId);
}