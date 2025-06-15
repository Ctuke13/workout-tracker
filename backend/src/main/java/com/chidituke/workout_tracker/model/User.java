package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password"})
@EqualsAndHashCode(exclude = {"password"})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Column(name = "first_name")
    @NotBlank(message = "First name is required")
    private String firstName;

    @Column(name = "last_name")
    @NotBlank(message = "Last name is required")
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "bio", length = 500)
    private String bio;

    // User Settings
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    @Builder.Default
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy_settings")
    @Builder.Default
    private PrivacySettings privacySettings = PrivacySettings.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_settings")
    @Builder.Default
    private NotificationSettings notificationSettings = NotificationSettings.ALL;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_system")
    @Builder.Default
    private MeasurementSystem measurementSystem = MeasurementSystem.METRIC;

    // ADDED: Missing fields referenced in compilation errors
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level")
    private ActivityLevel activityLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    @Builder.Default
    private UserType userType = UserType.REGULAR;

    // Fitness Information
    @Enumerated(EnumType.STRING)
    @Column(name = "fitness_level")
    private FitnessLevel fitnessLevel;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "workout_frequency")
    private WorkoutFrequency workoutFrequency;

    @Column(name = "fitness_goals", length = 1000)
    private String fitnessGoals;

    // Professional Information
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProfessionalProfile professionalProfile;

    // Subscription Information
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Subscription subscription;

    // Activity Tracking
    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @Column(name = "total_workouts")
    @Builder.Default
    private Integer totalWorkouts = 0;

    @Column(name = "current_streak")
    @Builder.Default
    private Integer currentStreak = 0;

    @Column(name = "longest_streak")
    @Builder.Default
    private Integer longestStreak = 0;

    // Account Management - RENAMED TO AVOID USERDETAILS CONFLICTS
    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "enabled")
    @Builder.Default
    private Boolean userEnabled = true;  // RENAMED: was 'enabled'

    @Column(name = "account_non_expired")
    @Builder.Default
    private Boolean userAccountNonExpired = true;  // RENAMED: was 'accountNonExpired'

    @Column(name = "account_non_locked")
    @Builder.Default
    private Boolean userAccountNonLocked = true;  // RENAMED: was 'accountNonLocked'

    @Column(name = "credentials_non_expired")
    @Builder.Default
    private Boolean userCredentialsNonExpired = true;  // RENAMED: was 'credentialsNonExpired'

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // UserDetails Implementation - USE RENAMED FIELDS
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return userAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return userAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return userCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return userEnabled;
    }

    // Business Logic Methods
    public boolean isProfessional() {
        return professionalProfile != null && professionalProfile.isVerified();
    }

    public boolean hasSubscription() {
        return subscription != null && subscription.isActive();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public Integer getAge() {
        if (dateOfBirth == null) return null;
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }

    public void incrementWorkoutCount() {
        this.totalWorkouts++;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateActivity() {
        this.lastActive = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Lifecycle Callbacks
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== ENUMS ====================

    public enum Role {
        USER, ADMIN, PROFESSIONAL
    }

    public enum AccountStatus {
        ACTIVE, SUSPENDED, INACTIVE, PENDING_VERIFICATION
    }

    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    public enum FitnessLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    public enum PrivacySettings {
        PUBLIC, FRIENDS_ONLY, PRIVATE
    }

    public enum NotificationSettings {
        ALL, WORKOUT_ONLY, SOCIAL_ONLY, NONE
    }

    public enum MeasurementSystem {
        METRIC, IMPERIAL
    }

    public enum WorkoutFrequency {
        RARELY(0, "Rarely (Less than once a week)"),
        ONCE_WEEK(1, "Once a week"),
        TWICE_WEEK(2, "2-3 times a week"),
        REGULARLY(4, "4-5 times a week"),
        DAILY(6, "6+ times a week"),
        MULTIPLE_DAILY(10, "Multiple times daily");

        private final int sessionsPerWeek;
        private final String description;

        WorkoutFrequency(int sessionsPerWeek, String description) {
            this.sessionsPerWeek = sessionsPerWeek;
            this.description = description;
        }

        public int getSessionsPerWeek() {
            return sessionsPerWeek;
        }

        public String getDescription() {
            return description;
        }
    }
}