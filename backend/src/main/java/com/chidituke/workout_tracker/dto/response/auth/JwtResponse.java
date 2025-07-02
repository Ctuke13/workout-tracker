package com.chidituke.workout_tracker.dto.response.auth;

import com.chidituke.workout_tracker.model.user.enums.UserType;
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

    // ═══════════════════════════════════════════════════════════════════
    // 🔧 CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Complete constructor used by AuthController
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
    }

    /**
     * Legacy constructor for backward compatibility (if needed)
     */
    public JwtResponse(String accessToken, String username, String email) {
        this.token = accessToken;
        this.type = "Bearer";
        this.username = username;
        this.email = email;
        // Other fields will be null - should be avoided in favor of complete constructor
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
}