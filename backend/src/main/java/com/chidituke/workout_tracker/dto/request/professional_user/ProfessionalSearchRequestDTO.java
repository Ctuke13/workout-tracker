package com.chidituke.workout_tracker.dto.request.professional_user;

import com.chidituke.workout_tracker.model.ProfessionalProfile;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for professional search with comprehensive filtering options
 * UPDATED to include all fields that the service expects
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalSearchRequestDTO {

    // ==============================================
    // LOCATION FILTERS
    // ==============================================

    @Size(max = 10, message = "Zipcode cannot exceed 10 characters")
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid zipcode format")
    private String zipcode;

    @Size(max = 100, message = "City name cannot exceed 100 characters")
    private String city;

    @Size(max = 50, message = "State cannot exceed 50 characters")
    private String state;

    @Min(value = 1, message = "Radius must be at least 1 mile")
    @Max(value = 100, message = "Radius cannot exceed 100 miles")
    private Integer radiusMiles;

    // ==============================================
    // SERVICE FILTERS
    // ==============================================

    private ProfessionalProfile.ServiceType serviceType;

    @Size(max = 10, message = "Maximum 10 specializations allowed")
    private List<String> specializations;

    private ProfessionalProfile.ExperienceLevel experienceLevel;

    // ==============================================
    // AVAILABILITY FILTERS - UPDATED with missing fields
    // ==============================================

    private Boolean acceptsNewClients;

    private Boolean offersVirtualSessions;

    // ✅ ADDED: Missing fields that the service expects
    private Boolean offersInHomeService;

    private Boolean offersGymSessions;

    // ✅ KEPT: Your existing field (can be used for general in-person filtering)
    private Boolean offersInPerson;

    private Boolean hasImmediateAvailability;

    // ==============================================
    // QUALITY FILTERS
    // ==============================================

    @DecimalMin(value = "0.0", message = "Minimum rating must be 0.0 or higher")
    @DecimalMax(value = "5.0", message = "Minimum rating must be 5.0 or lower")
    private Double minRating;

    @Min(value = 0, message = "Minimum reviews must be 0 or higher")
    private Integer minReviews;

    @Min(value = 0, message = "Minimum experience years must be 0 or higher")
    private Integer minExperienceYears;

    // ==============================================
    // PRICING FILTERS
    // ==============================================

    @DecimalMin(value = "10.0", message = "Minimum hourly rate must be at least $10")
    @DecimalMax(value = "1000.0", message = "Maximum hourly rate cannot exceed $1000")
    private Double maxHourlyRate;

    @DecimalMin(value = "10.0", message = "Minimum hourly rate must be at least $10")
    @DecimalMax(value = "1000.0", message = "Minimum hourly rate cannot exceed $1000")
    private Double minHourlyRate;

    // ==============================================
    // VERIFICATION STATUS
    // ==============================================

    private Boolean verifiedOnly;

    // ==============================================
    // SORTING OPTIONS
    // ==============================================

    @Builder.Default
    private SortBy sortBy = SortBy.RELEVANCE;

    @Builder.Default
    private SortDirection sortDirection = SortDirection.DESC;

    public enum SortBy {
        RELEVANCE, RATING, REVIEWS, EXPERIENCE, HOURLY_RATE, DISTANCE, RECENT_ACTIVITY
    }

    public enum SortDirection {
        ASC, DESC
    }

    // ==============================================
    // HELPER METHODS - UPDATED to include new fields
    // ==============================================

    /**
     * Check if location filter is specified
     */
    public boolean hasLocationFilter() {
        return zipcode != null || city != null || state != null;
    }

    /**
     * Check if this search requires proximity calculation
     */
    public boolean needsProximitySearch() {
        return hasLocationFilter() && radiusMiles != null && radiusMiles > 0;
    }

    /**
     * Check if service filter is specified
     */
    public boolean hasServiceFilter() {
        return serviceType != null ||
                (specializations != null && !specializations.isEmpty()) ||
                experienceLevel != null;
    }

    /**
     * Check if pricing filter is specified
     */
    public boolean hasPricingFilter() {
        return minHourlyRate != null || maxHourlyRate != null;
    }

    /**
     * Check if quality filter is specified
     */
    public boolean hasQualityFilter() {
        return minRating != null ||
                minReviews != null ||
                minExperienceYears != null ||
                (verifiedOnly != null && verifiedOnly);
    }

    /**
     * Check if availability filter is specified
     */
    public boolean hasAvailabilityFilter() {
        return Boolean.TRUE.equals(acceptsNewClients) ||
                Boolean.TRUE.equals(offersVirtualSessions) ||
                Boolean.TRUE.equals(offersInHomeService) ||
                Boolean.TRUE.equals(offersGymSessions) ||
                Boolean.TRUE.equals(offersInPerson) ||
                Boolean.TRUE.equals(hasImmediateAvailability);
    }

    /**
     * Get location display string
     */
    public String getLocationString() {
        if (city != null && state != null) {
            return city + ", " + state;
        } else if (zipcode != null) {
            return zipcode;
        } else if (city != null) {
            return city;
        } else if (state != null) {
            return state;
        }
        return null;
    }

    /**
     * Get price range display string
     */
    public String getPriceRangeString() {
        if (minHourlyRate != null && maxHourlyRate != null) {
            return String.format("$%.0f - $%.0f/hr", minHourlyRate, maxHourlyRate);
        } else if (minHourlyRate != null) {
            return String.format("$%.0f+/hr", minHourlyRate);
        } else if (maxHourlyRate != null) {
            return String.format("Up to $%.0f/hr", maxHourlyRate);
        }
        return "Any price";
    }

    /**
     * Get service types offered display string
     */
    public String getServiceTypesString() {
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
            return "Any service type";
        }

        return String.join(", ", services);
    }

    /**
     * Check if search has any filters applied
     */
    public boolean hasAnyFilters() {
        return hasLocationFilter() ||
                hasServiceFilter() ||
                hasPricingFilter() ||
                hasQualityFilter() ||
                hasAvailabilityFilter();
    }

    /**
     * Create a search summary string
     */
    public String getSearchSummary() {
        List<String> filters = new java.util.ArrayList<>();

        if (serviceType != null) {
            filters.add(serviceType.getDisplayName());
        }

        if (hasLocationFilter()) {
            filters.add(getLocationString());
        }

        if (minRating != null) {
            filters.add(String.format("%.1f+ stars", minRating));
        }

        if (Boolean.TRUE.equals(verifiedOnly)) {
            filters.add("Verified only");
        }

        if (Boolean.TRUE.equals(acceptsNewClients)) {
            filters.add("Accepting clients");
        }

        if (filters.isEmpty()) {
            return "All professionals";
        }

        return String.join(" • ", filters);
    }

    /**
     * Check if professional service type matches in-person criteria
     */
    public boolean matchesInPersonCriteria() {
        // If offersInPerson is true, match both in-home and gym services
        if (Boolean.TRUE.equals(offersInPerson)) {
            return true;
        }

        // Otherwise, check specific service types
        return Boolean.TRUE.equals(offersInHomeService) ||
                Boolean.TRUE.equals(offersGymSessions);
    }

    /**
     * Get experience filter display
     */
    public String getExperienceFilterString() {
        List<String> criteria = new java.util.ArrayList<>();

        if (experienceLevel != null) {
            criteria.add(experienceLevel.getDisplayName());
        }

        if (minExperienceYears != null) {
            criteria.add(minExperienceYears + "+ years");
        }

        if (criteria.isEmpty()) {
            return "Any experience";
        }

        return String.join(", ", criteria);
    }

    /**
     * Get quality criteria display
     */
    public String getQualityCriteriaString() {
        List<String> criteria = new java.util.ArrayList<>();

        if (minRating != null) {
            criteria.add(String.format("%.1f+ rating", minRating));
        }

        if (minReviews != null) {
            criteria.add(minReviews + "+ reviews");
        }

        if (Boolean.TRUE.equals(verifiedOnly)) {
            criteria.add("Verified");
        }

        if (criteria.isEmpty()) {
            return "Any quality";
        }

        return String.join(", ", criteria);
    }
}