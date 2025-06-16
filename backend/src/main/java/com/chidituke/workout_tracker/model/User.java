package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    @Builder.Default
    private UserType userType = UserType.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_tier")
    @Builder.Default
    private SubscriptionTier subscriptionTier = SubscriptionTier.FREE;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level")
    private ActivityLevel activityLevel;

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
    private Boolean userEnabled = true;

    @Column(name = "account_non_expired")
    @Builder.Default
    private Boolean userAccountNonExpired = true;

    @Column(name = "account_non_locked")
    @Builder.Default
    private Boolean userAccountNonLocked = true;

    @Column(name = "credentials_non_expired")
    @Builder.Default
    private Boolean userCredentialsNonExpired = true;

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ==================== ROLE CHECKING METHODS ====================

    /**
     * Check if user has a specific role
     * @param roleName the role to check (e.g., "ADMIN", "PROFESSIONAL", "REGULAR")
     * @return true if user has the role
     */
    public boolean hasRole(String roleName) {
        if (this.userType == null) {
            return false;
        }
        return this.userType.name().equalsIgnoreCase(roleName);
    }

    /**
     * Check if user is a professional (has PROFESSIONAL user type)
     * @return true if user has professional user type
     */
    public boolean isProfessional() {
        return userType == UserType.PROFESSIONAL;
    }

    /**
     * Check if user is an admin (has ADMIN user type)
     * @return true if user has admin user type
     */
    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    /**
     * Check if user has pending verification request
     * @return true if verification is pending
     */
    public boolean hasPendingVerification() {
        return accountStatus == AccountStatus.PENDING_VERIFICATION;
    }

    // ==================== SUBSCRIPTION-BASED FEATURE ACCESS ====================

    /**
     * Personal fitness features (available to all user types based on subscription)
     */
    public boolean canScheduleWorkouts() {
        return subscriptionTier != null && subscriptionTier.canScheduleWorkouts();
    }

    public boolean canReceiveNotifications() {
        return subscriptionTier != null && subscriptionTier.canReceiveNotifications();
    }

    public boolean canCreateRoutines() {
        return subscriptionTier != null && subscriptionTier.canCreateRoutines();
    }

    public boolean canTrackConsistency() {
        return subscriptionTier != null && subscriptionTier.canTrackConsistency();
    }

    public boolean canCustomizeTemplates() {
        return subscriptionTier != null && subscriptionTier.canCustomizeTemplates();
    }

    public boolean canUseAIGeneration() {
        return subscriptionTier != null && subscriptionTier.canUseAIGeneration();
    }

    public boolean canAccessSportSpecificPrograms() {
        return subscriptionTier != null && subscriptionTier.canAccessSportSpecificPrograms();
    }

    public boolean canUseProgressivePeriodization() {
        return subscriptionTier != null && subscriptionTier.canUseProgressivePeriodization();
    }

    public boolean hasAdvancedAnalytics() {
        return subscriptionTier != null && subscriptionTier.hasAdvancedAnalytics();
    }

    public boolean canAccessPremiumContent() {
        return subscriptionTier != null && subscriptionTier.canAccessPremiumContent();
    }

    public boolean hasNutritionIntegration() {
        return subscriptionTier != null && subscriptionTier.hasNutritionIntegration();
    }

    // ==================== PROFESSIONAL FEATURES (require BOTH role AND subscription) ====================

    /**
     * Professional features require BOTH Professional user type AND Pro Professional subscription
     */
    public boolean canManageClients() {
        return isProfessional() &&
                subscriptionTier != null &&
                subscriptionTier.canManageClients();
    }

    public boolean canAccessClientAnalytics() {
        return isProfessional() &&
                subscriptionTier != null &&
                subscriptionTier.canAccessClientAnalytics();
    }

    public boolean canScheduleClientSessions() {
        return isProfessional() &&
                subscriptionTier != null &&
                subscriptionTier.canScheduleClientSessions();
    }

    public boolean canMessageClients() {
        return isProfessional() &&
                subscriptionTier != null &&
                subscriptionTier.canMessageClients();
    }

    public boolean canAssignPrograms() {
        return isProfessional() &&
                subscriptionTier != null &&
                subscriptionTier.canAssignPrograms();
    }

    public boolean canViewClientProgress() {
        return isProfessional() &&
                subscriptionTier != null &&
                subscriptionTier.canViewClientProgress();
    }

    public boolean hasPriorityDirectoryPlacement() {
        return isProfessional() &&
                subscriptionTier != null &&
                subscriptionTier.hasPriorityDirectoryPlacement();
    }

    public boolean canCreateProfessionalProfile() {
        return isProfessional() &&
                subscriptionTier != null &&
                subscriptionTier.canCreateProfessionalProfile();
    }

    public boolean hasBusinessInsights() {
        return isProfessional() &&
                subscriptionTier != null &&
                subscriptionTier.hasBusinessInsights();
    }

    /**
     * Can get professional verification (requires professional user type + Pro Professional subscription)
     */
    public boolean canGetProfessionalVerification() {
        return isProfessional() &&
                subscriptionTier != null &&
                subscriptionTier.canGetProfessionalVerification();
    }

    /**
     * Check if professional user is verified AND has required subscription
     */
    public boolean isProfessionalVerified() {
        if (!isProfessional()) return false;
        if (!canGetProfessionalVerification()) return false;
        return professionalProfile != null && Boolean.TRUE.equals(professionalProfile.getIsVerified());
    }

    // ==================== CONTENT CREATION (role + subscription based) ====================

    /**
     * Can create professional content (requires Professional/Admin role + Pro+ subscription)
     */
    public boolean canCreateProfessionalContent() {
        boolean hasRole = userType != null && userType.canCreateProfessionalContent();
        boolean hasSubscription = subscriptionTier != null && subscriptionTier.canUseAIGeneration();
        return hasRole && hasSubscription;
    }

    /**
     * Can verify exercises (requires Professional/Admin role + Pro+ subscription)
     */
    public boolean canVerifyExercises() {
        boolean hasRole = userType != null && userType.canVerifyExercises();
        boolean hasSubscription = subscriptionTier != null && subscriptionTier.canUseAIGeneration();
        return hasRole && hasSubscription;
    }

    /**
     * Check if user has admin access (uses your UserType logic)
     */
    public boolean hasAdminAccess() {
        return userType != null && userType.hasAdminAccess();
    }

    // ==================== LEGACY/COMPATIBILITY METHODS ====================

    /**
     * @deprecated Use specific feature methods instead
     */
    @Deprecated
    public boolean canCreatePrograms() {
        return canCreateProfessionalContent();
    }

    /**
     * @deprecated Use canManageClients() instead
     */
    @Deprecated
    public boolean canRunProfessionalBusiness() {
        return canManageClients();
    }

    /**
     * @deprecated Use canManageClients() instead
     */
    @Deprecated
    public boolean canAcceptClients() {
        return canManageClients();
    }

    // ==================== SUBSCRIPTION MANAGEMENT ====================

    /**
     * Check if user needs to upgrade subscription to access a specific tier
     */
    public boolean needsSubscriptionUpgrade(SubscriptionTier requiredTier) {
        if (subscriptionTier == null) return true;
        return subscriptionTier.ordinal() < requiredTier.ordinal();
    }

    /**
     * Get the subscription tier required for professional features
     */
    public SubscriptionTier getRequiredProfessionalTier() {
        return SubscriptionTier.PRO_PROFESSIONAL;
    }

    /**
     * Check if user can upgrade to a specific tier
     */
    public boolean canUpgradeTo(SubscriptionTier targetTier) {
        if (subscriptionTier == null) return true;
        return targetTier.ordinal() > subscriptionTier.ordinal();
    }

    /**
     * Get user's current subscription tier name for display
     */
    public String getSubscriptionDisplayName() {
        return subscriptionTier != null ? subscriptionTier.getDisplayName() : "Unknown";
    }

    /**
     * Check if user is on a paid tier
     */
    public boolean hasPaidSubscription() {
        return subscriptionTier != null && subscriptionTier != SubscriptionTier.FREE;
    }

    // ==================== SPRING SECURITY USERDETAILS IMPLEMENTATION ====================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return role-based authorities for Spring Security using UserType
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userType.name()));
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

    // ==================== OTHER BUSINESS LOGIC METHODS ====================

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

    // ==================== LIFECYCLE CALLBACKS ====================

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== ENUMS ====================

    public enum AccountStatus {
        ACTIVE("Account Active"),
        SUSPENDED("Account Suspended"),
        INACTIVE("Account Inactive"),
        PENDING_VERIFICATION("Pending Verification");

        private final String displayName;

        AccountStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Gender {
        MALE("Male"),
        FEMALE("Female"),
        OTHER("Other"),
        PREFER_NOT_TO_SAY("Prefer not to say");

        private final String displayName;

        Gender(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum FitnessLevel {
        BEGINNER("Beginner - New to fitness"),
        INTERMEDIATE("Intermediate - Some experience"),
        ADVANCED("Advanced - Experienced"),
        EXPERT("Expert - Professional level");

        private final String description;

        FitnessLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum PrivacySettings {
        PUBLIC("Public - Visible to everyone"),
        FRIENDS_ONLY("Friends Only - Visible to connections"),
        PRIVATE("Private - Only visible to you");

        private final String description;

        PrivacySettings(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum NotificationSettings {
        ALL("All Notifications"),
        WORKOUT_ONLY("Workout Notifications Only"),
        SOCIAL_ONLY("Social Notifications Only"),
        NONE("No Notifications");

        private final String description;

        NotificationSettings(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum MeasurementSystem {
        METRIC("Metric (kg, cm)"),
        IMPERIAL("Imperial (lbs, ft/in)");

        private final String description;

        MeasurementSystem(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
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

    public enum ActivityLevel {
        SEDENTARY("Sedentary - Little to no exercise"),
        LIGHTLY_ACTIVE("Lightly Active - Light exercise 1-3 days/week"),
        MODERATELY_ACTIVE("Moderately Active - Moderate exercise 3-5 days/week"),
        VERY_ACTIVE("Very Active - Hard exercise 6-7 days/week"),
        EXTREMELY_ACTIVE("Extremely Active - Very hard exercise, physical job");

        private final String description;

        ActivityLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}