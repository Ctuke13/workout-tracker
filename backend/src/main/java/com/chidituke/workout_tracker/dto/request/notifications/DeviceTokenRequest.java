package com.chidituke.workout_tracker.dto.request.notifications;

import com.chidituke.workout_tracker.model.notifications.DeviceToken;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceTokenRequest {

    @NotBlank(message = "FCM token is required")
    private String token;

    private DeviceToken.Platform platform = DeviceToken.Platform.WEB;
}