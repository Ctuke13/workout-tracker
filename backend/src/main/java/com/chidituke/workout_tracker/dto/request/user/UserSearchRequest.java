package com.chidituke.workout_tracker.dto.request.user;

import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.model.UserType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserSearchRequest {
    // Text search
    @Size(max = 100, message = "Search query cannot exceed 100 characters")
    private String query; // Search by name, username, email

    // ✅ ENHANCED: Complete location-based search options
    @Pattern(regexp = "^\\d{5}$", message = "Zipcode must be 5 digits")
    private String zipcode; // Exact zipcode search

    @Size(max = 100, message = "City name cannot exceed 100 characters")
    private String city; // City-based search

    @Size(max = 50, message = "State name cannot exceed 50 characters")
    private String state; // State-based search

    @Size(max = 50, message = "Country name cannot exceed 50 characters")
    private String country; // Country-based search

    @Min(value = 1, message = "Search radius must be at least 1 mile")
    @Max(value = 500, message = "Search radius cannot exceed 500 miles")
    private Integer radiusMiles; // Search radius for location-based searches

    // User characteristics
    private User.FitnessLevel fitnessLevel;
    private User.ActivityLevel activityLevel;
    private UserType userType;
    private Boolean isProfessional;

    // Professional-specific filters
    private Boolean isVerified; // For professionals
    private Boolean isAcceptingClients; // For professionals
    private Boolean offersVirtual; // For professionals offering virtual sessions

    @Size(max = 100, message = "Specialization cannot exceed 100 characters")
    private String specialization; // For professional search

    // Activity filters
    private Boolean isCurrentlyActive; // Users active in last 15 minutes
    private Boolean isActiveToday; // Users active today
    private Integer minWorkoutStreak; // Minimum current workout streak

    // Subscription filters
    private Boolean hasPaidSubscription; // Users with paid tiers
    private Boolean canUseAIFeatures; // Users with Pro+ subscriptions

    // ✅ ADDED: Helper methods for validation and processing
    public boolean hasLocationFilter() {
        return zipcode != null || city != null || state != null || country != null;
    }

    public String getLocationString() {
        if (zipcode != null) return zipcode;

        StringBuilder location = new StringBuilder();
        if (city != null) location.append(city);
        if (state != null) {
            if (location.length() > 0) location.append(", ");
            location.append(state);
        }
        if (country != null && !country.equals("US")) {
            if (location.length() > 0) location.append(", ");
            location.append(country);
        }

        return location.length() > 0 ? location.toString() : null;
    }

    public boolean isLocationSpecific() {
        return zipcode != null || (city != null && state != null);
    }

    public boolean needsProximitySearch() {
        return hasLocationFilter() && radiusMiles != null && radiusMiles > 0;
    }
}