package com.chidituke.workout_tracker.security;

import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.UserType;
import com.chidituke.workout_tracker.model.user.enums.SubscriptionTier;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
public class UserPrincipal implements UserDetails {
    private Long id;
    private String username;
    private String email;
    private String password;
    private UserType userType;
    private SubscriptionTier subscriptionTier;

    public UserPrincipal(Long id, String username, String email, String password,
                         UserType userType, SubscriptionTier subscriptionTier) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.userType = userType;
        this.subscriptionTier = subscriptionTier;
    }

    public static UserPrincipal create(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getUserType(),
                user.getSubscriptionTier() // ✅ ADDED: Get subscription tier from user
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (userType != null) {
            return List.of(new SimpleGrantedAuthority("ROLE_" + userType.name()));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // ==================== USER TYPE METHODS ====================

    public boolean isProfessional() {
        return userType == UserType.PROFESSIONAL;
    }

    public boolean isAdmin() {
        return userType == UserType.ADMIN;
    }

    public boolean isRegular() {
        return userType == UserType.REGULAR;
    }

    // ==================== SUBSCRIPTION TIER METHODS ====================

    public boolean isFreeTier() {
        return subscriptionTier == SubscriptionTier.FREE;
    }

    public boolean isPlusTier() {
        return subscriptionTier == SubscriptionTier.PLUS;
    }

    public boolean isProTier() {
        return subscriptionTier == SubscriptionTier.PRO;
    }

    public boolean isProProfessionalTier() {
        return subscriptionTier == SubscriptionTier.PRO_PROFESSIONAL;
    }

    public boolean hasPaidSubscription() {
        return subscriptionTier != null && subscriptionTier != SubscriptionTier.FREE;
    }

    // ==================== FEATURE ACCESS METHODS ====================

    /**
     * Check if user can access paid workout plan features
     */
    public boolean canAccessPaidPlans() {
        return subscriptionTier != null &&
                (subscriptionTier == SubscriptionTier.PLUS ||
                        subscriptionTier == SubscriptionTier.PRO ||
                        subscriptionTier == SubscriptionTier.PRO_PROFESSIONAL);
    }

    /**
     * Check if user can schedule workouts
     */
    public boolean canScheduleWorkouts() {
        return subscriptionTier != null && subscriptionTier.canScheduleWorkouts();
    }

    /**
     * Check if user can use AI features
     */
    public boolean canUseAIFeatures() {
        return subscriptionTier != null && subscriptionTier.canUseAIGeneration();
    }

    /**
     * Check if user can manage clients (professional feature)
     */
    public boolean canManageClients() {
        return isProfessional() &&
                subscriptionTier != null &&
                subscriptionTier.canManageClients();
    }

    /**
     * Get subscription tier display name
     */
    public String getSubscriptionDisplayName() {
        return subscriptionTier != null ? subscriptionTier.getDisplayName() : "Unknown";
    }
}