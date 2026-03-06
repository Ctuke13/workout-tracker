package com.chidituke.workout_tracker.service.notifications;

import com.chidituke.workout_tracker.model.notifications.DeviceToken;
import com.chidituke.workout_tracker.repository.notifications.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Manages FCM device tokens for push notifications.
 * <p>
 * React Native migration note:
 * When moving to React Native, only the token registration source changes
 * (web FCM SDK → @react-native-firebase/messaging). This service is unchanged.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    /**
     * Register a new FCM token for a user.
     * If the token already exists for this user, reactivates it.
     * If it's a brand new token, saves it.
     *
     * @param userId   The authenticated user's ID
     * @param token    The FCM token from the browser/device
     * @param platform WEB, ANDROID, or IOS
     */
    @Transactional
    public DeviceToken registerToken(Long userId, String token, DeviceToken.Platform platform) {
        Optional<DeviceToken> existing = deviceTokenRepository.findByUserIdAndToken(userId, token);

        if (existing.isPresent()) {
            DeviceToken dt = existing.get();
            if (!dt.getActive()) {
                // Reactivate a previously deactivated token (e.g. user logged back in)
                dt.setActive(true);
                DeviceToken saved = deviceTokenRepository.save(dt);
                log.info("🔔 Reactivated device token for user {}: platform={}", userId, platform);
                return saved;
            }
            // Already active — nothing to do
            log.debug("🔔 Token already registered and active for user {}", userId);
            return dt;
        }

        // New token
        DeviceToken newToken = DeviceToken.builder()
                .userId(userId)
                .token(token)
                .platform(platform)
                .active(true)
                .build();

        DeviceToken saved = deviceTokenRepository.save(newToken);
        log.info("🔔 Registered new device token for user {}: platform={}", userId, platform);
        return saved;
    }

    /**
     * Get all active tokens for a user.
     * Used by NotificationService when sending a push notification.
     */
    public List<DeviceToken> getActiveTokens(Long userId) {
        return deviceTokenRepository.findByUserIdAndActiveTrue(userId);
    }

    /**
     * Deactivate all tokens for a user.
     * Called on logout so the user stops receiving notifications on that device.
     */
    @Transactional
    public void deactivateAllTokens(Long userId) {
        deviceTokenRepository.deactivateAllForUser(userId);
        log.info("🔕 Deactivated all device tokens for user {}", userId);
    }

    /**
     * Deactivate a specific token.
     * Called when FCM reports a token as invalid or expired.
     */
    @Transactional
    public void deactivateToken(String token) {
        deviceTokenRepository.deactivateByToken(token);
        log.info("🔕 Deactivated invalid FCM token: {}...", token.substring(0, Math.min(20, token.length())));
    }
}