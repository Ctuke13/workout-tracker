package com.chidituke.workout_tracker.controller.notifications;

import com.chidituke.workout_tracker.dto.request.notifications.DeviceTokenRequest;
import com.chidituke.workout_tracker.model.notifications.DeviceToken;
import com.chidituke.workout_tracker.security.CurrentUser;
import com.chidituke.workout_tracker.security.UserPrincipal;
import com.chidituke.workout_tracker.service.notifications.DeviceTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles FCM device token registration and deactivation.
 * <p>
 * React Native migration note:
 * These endpoints are platform-agnostic. When moving to React Native,
 * only the frontend token retrieval changes — these endpoints stay the same.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Push notification token management")
public class NotificationsController {

    private final DeviceTokenService deviceTokenService;

    /**
     * Register an FCM token for the current user.
     * Called by the frontend after the user grants notification permission.
     * <p>
     * POST /api/notifications/token
     */
    @PostMapping("/token")
    @Operation(summary = "Register FCM token", description = "Register a device token for push notifications")
    public ResponseEntity<Map<String, Object>> registerToken(
            @Valid @RequestBody DeviceTokenRequest request,
            @CurrentUser UserPrincipal currentUser) {

        DeviceToken saved = deviceTokenService.registerToken(
                currentUser.getId(),
                request.getToken(),
                request.getPlatform() != null ? request.getPlatform() : DeviceToken.Platform.WEB
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "tokenId", saved.getId(),
                "platform", saved.getPlatform(),
                "message", "Device registered for push notifications"
        ));
    }

    /**
     * Deactivate all tokens for the current user.
     * Called on logout so the user stops receiving notifications.
     * <p>
     * DELETE /api/notifications/token
     */
    @DeleteMapping("/token")
    @Operation(summary = "Deactivate tokens", description = "Deactivate all device tokens on logout")
    public ResponseEntity<Map<String, Object>> deactivateTokens(
            @CurrentUser UserPrincipal currentUser) {

        deviceTokenService.deactivateAllTokens(currentUser.getId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Device tokens deactivated"
        ));
    }
}