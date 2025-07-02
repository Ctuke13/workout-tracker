package com.chidituke.workout_tracker.dto.response.user;

import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.UserType;
import com.chidituke.workout_tracker.model.user.ProfessionalProfile;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private User.Gender gender;

    // ✅ FIXED: Complete location information for all users
    private String zipcode;
    private String city;
    private String state;
    private String country;

    private String phoneNumber;
    private String bio;
    private String profileImageUrl;

    private UserType userType;
    private User.AccountStatus accountStatus;
    private User.PrivacySettings privacySettings;
    private User.NotificationSettings notificationSettings;

    private User.MeasurementSystem measurementSystem;
    private User.FitnessLevel fitnessLevel;
    private String fitnessGoals;
    private User.WorkoutFrequency workoutFrequency;
    private LocalDateTime lastActive;
    private LocalDateTime createdAt;

    // Activity status
    private User.ActivityLevel activityLevel;
    private Boolean currentlyActive;
    private Boolean activeToday;

    // ✅ ADDED: Physical measurements
    private Integer heightCm;
    private Double weightKg;

    // Professional information (if applicable)
    private Boolean isProfessional;
    private String professionalDisplayName;
    private ProfessionalProfile.ServiceType professionalServiceType;
    private Boolean professionalVerified;
    private Boolean professionalAcceptingClients;
    private Double professionalRating;

    // ✅ ADDED: Helper methods for location display
    public String getLocationDisplay() {
        if (city != null && state != null) {
            return city + ", " + state;
        } else if (zipcode != null) {
            return zipcode;
        }
        return "Location not set";
    }

    public String getFullLocation() {
        StringBuilder location = new StringBuilder();
        if (city != null) location.append(city);
        if (state != null) {
            if (location.length() > 0) location.append(", ");
            location.append(state);
        }
        if (zipcode != null) {
            if (location.length() > 0) location.append(" ");
            location.append(zipcode);
        }
        if (country != null && !country.equals("US")) {
            if (location.length() > 0) location.append(", ");
            location.append(country);
        }
        return location.length() > 0 ? location.toString() : "Location not set";
    }

    public boolean hasLocationInfo() {
        return zipcode != null || (city != null && state != null);
    }
}