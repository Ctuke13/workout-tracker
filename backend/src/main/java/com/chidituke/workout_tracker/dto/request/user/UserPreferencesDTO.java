package com.chidituke.workout_tracker.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user preferences (units, settings, etc.)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesDTO {

    private String preferredDistanceUnit; // "km" or "miles"
    private String preferredWeightUnit;   // "kg" or "lbs"

    /**
     * Create from User entity
     */
    public static UserPreferencesDTO fromUser(com.chidituke.workout_tracker.model.user.User user) {
        return new UserPreferencesDTO(
                user.getPreferredDistanceUnit(),
                user.getPreferredWeightUnit()
        );
    }
}