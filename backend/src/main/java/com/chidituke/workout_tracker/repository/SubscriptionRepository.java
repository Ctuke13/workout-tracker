package com.chidituke.workout_tracker.repository;

import com.chidituke.workout_tracker.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // Find subscription by user ID
    Optional<Subscription> findByUserId(Long userId);

    // Check if user has active subscription
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.status = 'ACTIVE'")
            Optional<Subscription> findActiveSubscriptionByUserId(@Param("userId") Long userId);

    // Check if user has specific plan type
    boolean existsByUserIdAndPlanTypeAndStatus(Long userId, Subscription.PlanType planType, Subscription.Status status);
}
