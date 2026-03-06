package com.chidituke.workout_tracker.repository.notifications;

import com.chidituke.workout_tracker.model.notifications.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    /**
     * All active tokens for a user — used when sending a notification.
     */
    List<DeviceToken> findByUserIdAndActiveTrue(Long userId);

    /**
     * Find a specific token regardless of active status — used for deduplication.
     */
    Optional<DeviceToken> findByUserIdAndToken(Long userId, String token);

    /**
     * Deactivate all tokens for a user — called on logout.
     */
    @Modifying
    @Query("UPDATE DeviceToken dt SET dt.active = false WHERE dt.userId = :userId")
    void deactivateAllForUser(@Param("userId") Long userId);

    /**
     * Deactivate a specific token — called when FCM reports a token is invalid.
     */
    @Modifying
    @Query("UPDATE DeviceToken dt SET dt.active = false WHERE dt.token = :token")
    void deactivateByToken(@Param("token") String token);

    /**
     * Check if a token is already registered and active for a user.
     */
    boolean existsByUserIdAndTokenAndActiveTrue(Long userId, String token);
}