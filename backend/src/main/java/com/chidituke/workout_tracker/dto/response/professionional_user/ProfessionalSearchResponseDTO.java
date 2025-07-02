package com.chidituke.workout_tracker.dto.response.professionional_user;

import com.chidituke.workout_tracker.model.user.ProfessionalProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Search Response DTO for Professional Profile
 * Optimized for search results with fixed field names and missing enums
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalSearchResponseDTO {

    // ==============================================
    // BASIC IDENTIFICATION
    // ==============================================

    private Long id;
    private String displayName; // Added missing field

    // ==============================================
    // PROFESSIONAL DETAILS
    // ==============================================

    private ProfessionalProfile.ServiceType serviceType;
    private ProfessionalProfile.ExperienceLevel experienceLevel; // Added missing enum
    private Integer yearsExperience; // Fixed field name (was experienceYears)
    private String bio;
    private List<String> specializations;

    // ==============================================
    // LOCATION & DISTANCE
    // ==============================================

    private String baseZipcode;
    private Double distanceMiles; // For proximity search results

    // ==============================================
    // PRICING & AVAILABILITY
    // ==============================================

    private Double hourlyRate;
    private Boolean acceptsNewClients;
    private Boolean offersVirtualSessions;
    private Boolean offersInHomeService;
    private Boolean offersGymSessions;

    // ==============================================
    // VERIFICATION & RATINGS
    // ==============================================

    private Boolean isVerified;
    private Double averageRating;
    private Integer totalReviews;

    // ==============================================
    // PROFILE QUALITY METRICS
    // ==============================================

    private Double profileCompletionPercentage; // Fixed field name

    // ==============================================
    // HELPER METHODS FOR SEARCH DISPLAY
    // ==============================================

    /**
     * Get formatted display name
     */
    public String getDisplayNameOrFallback() {
        return displayName != null && !displayName.trim().isEmpty()
                ? displayName
                : "Professional User";
    }

    /**
     * Get service type display name
     */
    public String getServiceTypeDisplay() {
        return serviceType != null ? serviceType.getDisplayName() : "Professional";
    }

    /**
     * Get experience level display
     */
    public String getExperienceLevelDisplay() {
        return experienceLevel != null ? experienceLevel.getDisplayName() : "Not specified";
    }

    /**
     * Get formatted pricing for search results
     */
    public String getPricingDisplay() {
        if (hourlyRate == null) {
            return "Contact for pricing";
        }
        return String.format("$%.0f/hr", hourlyRate);
    }

    /**
     * Get compact rating display for search
     */
    public String getRatingDisplay() {
        if (averageRating == null || averageRating == 0.0) {
            return "No ratings";
        }

        if (totalReviews == null || totalReviews == 0) {
            return String.format("%.1f ⭐", averageRating);
        }

        return String.format("%.1f ⭐ (%d)", averageRating, totalReviews);
    }

    /**
     * Get experience display for search
     */
    public String getExperienceDisplay() {
        if (yearsExperience == null || yearsExperience == 0) {
            return "New";
        }

        if (yearsExperience == 1) {
            return "1 yr";
        }

        if (yearsExperience < 5) {
            return String.format("%d yrs", yearsExperience);
        }

        return String.format("%d+ yrs", yearsExperience);
    }

    /**
     * Get distance display for location-based search
     */
    public String getDistanceDisplay() {
        if (distanceMiles == null) {
            return "";
        }

        if (distanceMiles < 1.0) {
            return "< 1 mile";
        }

        if (distanceMiles < 10.0) {
            return String.format("%.1f miles", distanceMiles);
        }

        return String.format("%.0f miles", distanceMiles);
    }

    /**
     * Get location display for search results
     */
    public String getLocationDisplay() {
        if (baseZipcode != null) {
            String distance = getDistanceDisplay();
            if (!distance.isEmpty()) {
                return baseZipcode + " • " + distance;
            }
            return baseZipcode;
        }
        return "Location not specified";
    }

    /**
     * Get verification status badge
     */
    public String getVerificationBadge() {
        return Boolean.TRUE.equals(isVerified) ? "✅" : "";
    }

    /**
     * Get availability status for search
     */
    public String getAvailabilityStatus() {
        if (Boolean.TRUE.equals(acceptsNewClients)) {
            return "Available";
        } else {
            return "Unavailable";
        }
    }

    /**
     * Get session types as compact string
     */
    public String getSessionTypes() {
        List<String> types = new java.util.ArrayList<>();

        if (Boolean.TRUE.equals(offersVirtualSessions)) {
            types.add("Virtual");
        }
        if (Boolean.TRUE.equals(offersInHomeService)) {
            types.add("In-Home");
        }
        if (Boolean.TRUE.equals(offersGymSessions)) {
            types.add("Gym");
        }

        if (types.isEmpty()) {
            return "Contact for details";
        }

        return String.join(", ", types);
    }

    /**
     * Get primary specialization for display
     */
    public String getPrimarySpecialization() {
        if (specializations == null || specializations.isEmpty()) {
            return getServiceTypeDisplay();
        }
        return specializations.get(0);
    }

    /**
     * Get specializations count display
     */
    public String getSpecializationsDisplay() {
        if (specializations == null || specializations.isEmpty()) {
            return "";
        }

        if (specializations.size() == 1) {
            return specializations.get(0);
        }

        return specializations.get(0) + " +" + (specializations.size() - 1) + " more";
    }

    /**
     * Check if professional is highly rated
     */
    public boolean isHighlyRated() {
        return averageRating != null &&
                averageRating >= 4.5 &&
                totalReviews != null &&
                totalReviews >= 10;
    }

    /**
     * Check if professional is new (low review count)
     */
    public boolean isNewProfessional() {
        return totalReviews == null || totalReviews < 5;
    }

    /**
     * Check if professional offers remote services
     */
    public boolean offersRemoteServices() {
        return Boolean.TRUE.equals(offersVirtualSessions);
    }

    /**
     * Check if professional offers in-person services
     */
    public boolean offersInPersonServices() {
        return Boolean.TRUE.equals(offersInHomeService) || Boolean.TRUE.equals(offersGymSessions);
    }

    /**
     * Get CSS class for rating color coding
     */
    public String getRatingCssClass() {
        if (averageRating == null || averageRating == 0.0) {
            return "rating-none";
        }

        if (averageRating >= 4.5) {
            return "rating-excellent";
        } else if (averageRating >= 4.0) {
            return "rating-good";
        } else if (averageRating >= 3.5) {
            return "rating-fair";
        } else {
            return "rating-poor";
        }
    }

    /**
     * Get search result summary for quick view
     */
    public String getSearchSummary() {
        StringBuilder summary = new StringBuilder();

        summary.append(getDisplayNameOrFallback());
        summary.append(" • ");
        summary.append(getServiceTypeDisplay());

        if (yearsExperience != null && yearsExperience > 0) {
            summary.append(" • ");
            summary.append(getExperienceDisplay());
        }

        if (Boolean.TRUE.equals(isVerified)) {
            summary.append(" ✅");
        }

        return summary.toString();
    }

    /**
     * Check if this professional matches location criteria
     */
    public boolean isLocalMatch() {
        return distanceMiles != null && distanceMiles <= 10.0;
    }

    /**
     * Check if profile is premium quality (complete + verified + highly rated)
     */
    public boolean isPremiumQuality() {
        return Boolean.TRUE.equals(isVerified) &&
                profileCompletionPercentage != null && profileCompletionPercentage >= 90.0 &&
                isHighlyRated();
    }

    /**
     * Get quality score for ranking (0-100)
     */
    public double getQualityScore() {
        double score = 0.0;

        // Profile completion (0-30 points)
        if (profileCompletionPercentage != null) {
            score += (profileCompletionPercentage / 100.0) * 30.0;
        }

        // Verification (0-20 points)
        if (Boolean.TRUE.equals(isVerified)) {
            score += 20.0;
        }

        // Rating (0-30 points)
        if (averageRating != null && averageRating > 0) {
            score += (averageRating / 5.0) * 30.0;
        }

        // Review count (0-20 points)
        if (totalReviews != null) {
            score += Math.min(totalReviews / 50.0, 1.0) * 20.0;
        }

        return Math.min(score, 100.0);
    }
}