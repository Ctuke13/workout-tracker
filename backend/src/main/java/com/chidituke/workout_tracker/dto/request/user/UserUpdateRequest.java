package com.chidituke.workout_tracker.dto.request.user;

import com.chidituke.workout_tracker.model.user.User;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private User.Gender gender;

    @Pattern(regexp = "^\\d{5}$", message = "Zipcode must be 5 digits")
    private String zipcode;

    @Size(max = 100, message = "City name cannot exceed 100 characters")
    private String city;

    @Size(max = 50, message = "State name cannot exceed 50 characters")
    private String state;

    @Size(max = 50, message = "Country name cannot exceed 50 characters")
    private String country;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    private String profilePictureUrl;

    // User preferences
    private User.PrivacySettings privacySettings;
    private User.NotificationSettings notificationSettings;

    private User.MeasurementSystem preferredMeasurementSystem; // Maps to User.measurementSystem

    // Fitness information
    private User.FitnessLevel fitnessLevel;

    @Size(max = 10, message = "Maximum 10 fitness goals allowed")
    private List<@Size(max = 100, message = "Each goal cannot exceed 100 characters") String> fitnessGoals;

    private User.WorkoutFrequency workoutFrequency;
    private User.ActivityLevel activityLevel;

    // Physical measurements
    @Min(value = 50, message = "Height must be at least 50 cm")
    @Max(value = 300, message = "Height cannot exceed 300 cm")
    private Integer heightCm;

    @DecimalMin(value = "10.0", message = "Weight must be at least 10 kg")
    @DecimalMax(value = "500.0", message = "Weight cannot exceed 500 kg")
    private Double weightKg;

    // Workout preferences
    @Min(value = 10, message = "Workout duration must be at least 10 minutes")
    @Max(value = 300, message = "Workout duration cannot exceed 300 minutes")
    private Integer preferredWorkoutDuration;

    // Helper method to get formatted fitness goals string
    public String getFitnessGoalsAsString() {
        if (fitnessGoals == null || fitnessGoals.isEmpty()) {
            return null;
        }
        return String.join(", ", fitnessGoals);
    }

    // Helper method to check if location is provided
    public boolean hasLocationInfo() {
        return zipcode != null || (city != null && state != null);
    }

    // Helper method to get display location
    public String getLocationDisplay() {
        if (city != null && state != null) {
            return city + ", " + state;
        } else if (zipcode != null) {
            return zipcode;
        }
        return null;
    }
}