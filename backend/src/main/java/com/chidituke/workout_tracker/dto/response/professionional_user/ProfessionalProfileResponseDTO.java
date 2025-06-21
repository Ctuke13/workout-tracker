package com.chidituke.workout_tracker.dto.response.professionional_user;

import com.chidituke.workout_tracker.model.ProfessionalProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for Professional Profile
 * FIXED to match mapper field expectations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalProfileResponseDTO {

    // ==============================================
    // BASIC PROFILE INFORMATION
    // ==============================================

    private Long id;
    private Long userId;
    private String displayName;
    private String firstName;
    private String lastName;
    private String profileImageUrl;

    // ==============================================
    // PROFESSIONAL DETAILS - FIXED field names
    // ==============================================

    private ProfessionalProfile.ServiceType serviceType;
    private ProfessionalProfile.ExperienceLevel experienceLevel;
    private Integer yearsExperience; // ✅ FIXED: Changed from experienceYears
    private String bio;
    private List<String> specializations;
    private List<String> certifications; // ✅ ADDED: Missing field

    // ==============================================
    // LOCATION INFORMATION
    // ==============================================

    private String baseZipcode;
    private List<String> serviceAreas;
    private String businessLocationDisplay;

    // ==============================================
    // PRICING AND AVAILABILITY - FIXED field names
    // ==============================================

    private Double hourlyRate;
    private Boolean acceptsNewClients;
    private Boolean offersVirtualSessions;

    // ✅ FIXED: Added specific service types that mapper expects
    private Boolean offersInHomeService;
    private Boolean offersGymSessions;

    // ✅ KEPT: For backward compatibility (general in-person)
    private Boolean offersInPerson;

    // ==============================================
    // RATINGS AND REVIEWS
    // ==============================================

    private Double averageRating;
    private Integer totalReviews;
    private String ratingDisplay;

    // ==============================================
    // VERIFICATION AND STATUS - FIXED field names
    // ==============================================

    private Boolean isVerified;
    private ProfessionalProfile.VerificationStatus verificationStatus;
    private Boolean isActive;
    private Boolean isProfilePublic; // ✅ FIXED: Changed from isPublicProfile

    // ==============================================
    // PROFILE COMPLETION AND ACTIVITY - FIXED field names
    // ==============================================

    private Double profileCompletionPercentage; // ✅ FIXED: Changed from profileCompletion
    private LocalDateTime lastActiveDate;
    private Boolean isCurrentlyActive;
    private String activityStatus;

    // ==============================================
    // TIMESTAMPS - ADDED missing fields
    // ==============================================

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime verificationSubmittedAt;
    private LocalDateTime verificationReviewedAt; // ✅ ADDED: Missing field

    // ==============================================
    // HELPER METHODS FOR DISPLAY - UPDATED
    // ==============================================

    /**
     * Get full name or display name
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return displayName;
        }
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    /**
     * Get experience display - FIXED to use yearsExperience
     */
    public String getExperienceDisplay() {
        if (yearsExperience == null) {
            return "Experience not specified";
        }
        return yearsExperience + (yearsExperience == 1 ? " year" : " years") + " experience";
    }

    /**
     * Get pricing display
     */
    public String getPricingDisplay() {
        if (hourlyRate == null) {
            return "Contact for pricing";
        }
        return String.format("$%.0f/hour", hourlyRate);
    }

    /**
     * Get profile completion display - FIXED to use profileCompletionPercentage
     */
    public String getProfileCompletionDisplay() {
        if (profileCompletionPercentage == null) {
            return "0% complete";
        }
        return String.format("%.1f%% complete", profileCompletionPercentage);
    }

    /**
     * Check if profile is complete - FIXED to use profileCompletionPercentage
     */
    public boolean hasCompleteProfile() {
        return profileCompletionPercentage != null && profileCompletionPercentage >= 80.0;
    }

    /**
     * Get verification status display
     */
    public String getVerificationStatusDisplay() {
        if (verificationStatus == null) {
            return "Not submitted";
        }
        return switch (verificationStatus) {
            case PENDING -> "Verification pending";
            case UNDER_REVIEW -> "Under review";
            case VERIFIED -> "Verified ✓";
            case REJECTED -> "Verification rejected";
            case EXPIRED -> "Verification expired";
            default -> "UnknownStatus";
        };
    }

    /**
     * Get service types offered display
     */
    public String getServiceTypesOffered() {
        List<String> services = new java.util.ArrayList<>();

        if (Boolean.TRUE.equals(offersVirtualSessions)) {
            services.add("Virtual");
        }
        if (Boolean.TRUE.equals(offersInHomeService)) {
            services.add("In-Home");
        }
        if (Boolean.TRUE.equals(offersGymSessions)) {
            services.add("Gym");
        }
        if (Boolean.TRUE.equals(offersInPerson) &&
                !Boolean.TRUE.equals(offersInHomeService) &&
                !Boolean.TRUE.equals(offersGymSessions)) {
            services.add("In-Person");
        }

        if (services.isEmpty()) {
            return "Contact for details";
        }

        return String.join(", ", services);
    }

    /**
     * Get availability status
     */
    public String getAvailabilityStatus() {
        if (!Boolean.TRUE.equals(isProfilePublic)) {
            return "Profile Private";
        }

        if (!Boolean.TRUE.equals(isVerified)) {
            return "Pending Verification";
        }

        if (Boolean.TRUE.equals(acceptsNewClients)) {
            return "Accepting New Clients";
        } else {
            return "Not Accepting New Clients";
        }
    }

    /**
     * Get rating display
     */
    public String getRatingDisplay() {
        if (averageRating == null || averageRating == 0.0) {
            return "No ratings yet";
        }

        if (totalReviews == null || totalReviews == 0) {
            return String.format("%.1f ⭐", averageRating);
        }

        return String.format("%.1f ⭐ (%d reviews)", averageRating, totalReviews);
    }

    /**
     * Check if professional is available for new clients
     */
    public boolean isAvailableForClients() {
        return Boolean.TRUE.equals(acceptsNewClients) &&
                Boolean.TRUE.equals(isProfilePublic) &&
                Boolean.TRUE.equals(isVerified);
    }

    /**
     * Get location display
     */
    public String getLocationDisplay() {
        if (baseZipcode != null) {
            return "Area: " + baseZipcode;
        }
        return "Location not specified";
    }

    /**
     * Get service areas display
     */
    public String getServiceAreasDisplay() {
        if (serviceAreas == null || serviceAreas.isEmpty()) {
            return getLocationDisplay();
        }

        if (serviceAreas.size() == 1) {
            return "Serves: " + serviceAreas.get(0);
        }

        return String.format("Serves %d areas", serviceAreas.size());
    }

    /**
     * Get verification badge display
     */
    public String getVerificationBadge() {
        if (Boolean.TRUE.equals(isVerified)) {
            return "✅ Verified Professional";
        }

        if (verificationStatus != null) {
            switch (verificationStatus) {
                case PENDING:
                case UNDER_REVIEW:
                    return "⏳ Verification Pending";
                case REJECTED:
                    return "❌ Verification Rejected";
                case EXPIRED:
                    return "⚠️ Verification Expired";
                default:
                    return "🔄 Not Verified";
            }
        }

        return "🔄 Not Verified";
    }

    /**
     * Check if professional profile needs attention
     */
    public boolean needsAttention() {
        return !hasCompleteProfile() ||
                !Boolean.TRUE.equals(isVerified) ||
                verificationStatus == ProfessionalProfile.VerificationStatus.REJECTED ||
                verificationStatus == ProfessionalProfile.VerificationStatus.EXPIRED;
    }

    /**
     * Get specializations display
     */
    public String getSpecializationsDisplay() {
        if (specializations == null || specializations.isEmpty()) {
            return serviceType != null ? serviceType.getDisplayName() : "General Fitness";
        }

        if (specializations.size() == 1) {
            return specializations.get(0);
        }

        return specializations.get(0) + " +" + (specializations.size() - 1) + " more";
    }

    /**
     * Get certifications display
     */
    public String getCertificationsDisplay() {
        if (certifications == null || certifications.isEmpty()) {
            return "No certifications listed";
        }

        if (certifications.size() == 1) {
            return certifications.get(0);
        }

        return certifications.get(0) + " +" + (certifications.size() - 1) + " more";
    }
}