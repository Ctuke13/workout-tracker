package com.chidituke.workout_tracker.dto.response.auth;

import com.chidituke.workout_tracker.model.user.enums.UserType;
import com.chidituke.workout_tracker.model.user.enums.SubscriptionTier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private UserType userType;
    private Boolean isProfessional;
    private SubscriptionTier subscriptionTier;
    private String nickname;
    private String petName;
    private Boolean onboardingCompleted;


    // ═══════════════════════════════════════════════════════════════════
    // 🔧 CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Complete constructor used by AuthController
     */
    public JwtResponse(String accessToken, Long id, String username, String email,
                       String firstName, String lastName, UserType userType, Boolean isProfessional,
                       SubscriptionTier subscriptionTier) {
        this.token = accessToken;
        this.type = "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.isProfessional = isProfessional;
        this.subscriptionTier = subscriptionTier;
    }

    /**
     * Backward compatible constructor (without subscription tier)
     */
    public JwtResponse(String accessToken, Long id, String username, String email,
                       String firstName, String lastName, UserType userType, Boolean isProfessional) {
        this.token = accessToken;
        this.type = "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.isProfessional = isProfessional;
        this.subscriptionTier = SubscriptionTier.FREE; // Default to FREE
    }

    /**
     * Legacy constructor for backward compatibility (if needed)
     */
    public JwtResponse(String accessToken, String username, String email) {
        this.token = accessToken;
        this.type = "Bearer";
        this.username = username;
        this.email = email;
        this.subscriptionTier = SubscriptionTier.FREE; // Default to FREE
        // Other fields will be null - should be avoided in favor of complete constructor
    }

    /**
     * Complete constructor with onboarding fields
     */
    public JwtResponse(String accessToken, Long id, String username, String email,
                       String firstName, String lastName, UserType userType, Boolean isProfessional,
                       SubscriptionTier subscriptionTier, String nickname, String petName,
                       Boolean onboardingCompleted) {
        this.token = accessToken;
        this.type = "Bearer";
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.isProfessional = isProfessional;
        this.subscriptionTier = subscriptionTier;
        this.nickname = nickname;
        this.petName = petName;
        this.onboardingCompleted = onboardingCompleted;
    }

    // ═══════════════════════════════════════════════════════════════════
    // 🛠️ UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Get user's full name for display
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return username;
        }
    }

    /**
     * Check if user has professional features
     */
    public boolean hasProfileProfessionalFeatures() {
        return isProfessional != null && isProfessional;
    }

    /**
     * Get role display name
     */
    public String getRoleDisplayName() {
        if (userType == null) return "User";
        return switch (userType) {
            case REGULAR -> "User";
            case ADMIN -> "Administrator";
            case PROFESSIONAL -> "Professional";
        };
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    /**
     * Check if user has professional role
     */
    public boolean isProfessionalRole() {
        return userType == UserType.PROFESSIONAL;
    }

    /**
     * Get complete authentication header value
     */
    public String getAuthorizationHeader() {
        return type + " " + token;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public Boolean getOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(Boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }

    /**
     * Check if user needs to complete onboarding
     */
    public boolean needsOnboarding() {
        return !Boolean.TRUE.equals(onboardingCompleted);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 🔒 SUBSCRIPTION TIER UTILITY METHODS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Check if user can access paid workout plans
     */
    public boolean canAccessPaidPlans() {
        if (subscriptionTier == null) return false;
        return subscriptionTier == SubscriptionTier.PLUS ||
                subscriptionTier == SubscriptionTier.PRO ||
                subscriptionTier == SubscriptionTier.PRO_PROFESSIONAL;
    }

    /**
     * Check if user has PRO-level features
     */
    public boolean hasProFeatures() {
        if (subscriptionTier == null) return false;
        return subscriptionTier == SubscriptionTier.PRO ||
                subscriptionTier == SubscriptionTier.PRO_PROFESSIONAL;
    }

    /**
     * Get subscription display name
     */
    public String getSubscriptionDisplayName() {
        if (subscriptionTier == null) return "Free";
        return switch (subscriptionTier) {
            case FREE -> "Free";
            case PLUS -> "Plus";
            case PRO -> "Pro";
            case PRO_PROFESSIONAL -> "Pro Professional";
        };
    }

    /**
     * Check if user is on free tier
     */
    public boolean isFreeTier() {
        return subscriptionTier == null || subscriptionTier == SubscriptionTier.FREE;
    }
}