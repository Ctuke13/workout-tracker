package com.chidituke.workout_tracker.dto.request.user;

import com.chidituke.workout_tracker.model.User;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.net.URL;
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

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 500, message = "Bio cannot exceed 500 characters")
    private String bio;

    @URL(message = "Profile picture URL must be valid")
    private String profilePictureUrl;

    private User.PrivacySettings privacySettings;
    private User.NotificationSettings notificationSettings;
    private User.MeasurementSystem preferredMeasurementSystem;
    private User.FitnessLevel fitnessLevel;
    private List<String> fitnessGoals;
    private Integer preferredWorkoutDuration;
    private User.WorkoutFrequency workoutFrequency;
}