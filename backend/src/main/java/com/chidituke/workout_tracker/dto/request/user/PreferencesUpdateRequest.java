package com.chidituke.workout_tracker.dto.request.user;

import com.chidituke.workout_tracker.model.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreferencesUpdateRequest {

    private User.NotificationSettings notificationSettings;
    private User.PrivacySettings privacySettings;
    private User.MeasurementSystem measurementSystem;
    private String preferredDistanceUnit; // "km" or "miles"
    private String preferredWeightUnit;   // "kg" or "lbs"
}