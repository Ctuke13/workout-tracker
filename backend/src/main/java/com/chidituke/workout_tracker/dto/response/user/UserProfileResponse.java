package com.chidituke.workout_tracker.dto.response.user;

import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.model.ProfessionalProfile;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileResponse {
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private User.Gender gender;
    private String zipcode;
    private String phoneNumber;
    private String bio;
    private String profilePictureUrl;
    private User.Role role;
    private User.AccountStatus accountStatus;
    private User.PrivacySettings privacySettings;
    private User.NotificationSettings notificationSettings;
    private User.MeasurementSystem preferredMeasurementSystem;
    private User.FitnessLevel fitnessLevel;
    private List<String> fitnessGoals;
    private Integer preferredWorkoutDuration;
    private User.WorkoutFrequency workoutFrequency;
    private LocalDateTime lastActive;
    private LocalDateTime createdAt;

    // Activity status
    private User.ActivityLevel activityLevel;
    private Boolean currentlyActive;
    private Boolean activeToday;

    // Professional information (if applicable)
    private Boolean isProfessional;
    private String professionalDisplayName;
    private ProfessionalProfile.ServiceType professionalServiceType;
    private Boolean professionalVerified;
    private Boolean professionalAcceptingClients;
    private Double professionalRating;
}