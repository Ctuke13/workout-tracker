package com.chidituke.workout_tracker.dto.request.professional_user;

import com.chidituke.workout_tracker.model.user.ProfessionalProfile;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for updating professional profiles
 * FIXED to match mapper field expectations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalProfileUpdateRequestDTO {

    // ==============================================
    // BASIC INFORMATION
    // ==============================================

    @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
    private String displayName;

    @Size(max = 100, message = "Business name cannot exceed 100 characters")
    private String businessName;

    // ==============================================
    // PROFESSIONAL DETAILS
    // ==============================================

    private ProfessionalProfile.ServiceType serviceType;

    private ProfessionalProfile.ExperienceLevel experienceLevel;

    @Min(value = 0, message = "Years of experience must be 0 or greater")
    @Max(value = 50, message = "Years of experience must be 50 or less")
    private Integer yearsExperience; // ✅ FIXED: Match mapper expectation

    @Size(min = 50, max = 2000, message = "Bio must be between 50 and 2000 characters")
    private String bio;

    @Size(max = 10, message = "Cannot have more than 10 specializations")
    private List<String> specializations;

    @Size(max = 10, message = "Cannot have more than 10 certifications") // ✅ ADDED: Missing field
    private List<String> certifications;

    // ==============================================
    // LOCATION & SERVICE AREAS
    // ==============================================

    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid zipcode format")
    private String baseZipcode;

    @Size(max = 20, message = "Cannot have more than 20 service areas")
    private List<String> serviceAreas;

    // ==============================================
    // PRICING
    // ==============================================

    @DecimalMin(value = "0.01", message = "Hourly rate must be greater than 0")
    @DecimalMax(value = "10000.00", message = "Hourly rate must be less than $10,000")
    private Double hourlyRate;

    // ==============================================
    // AVAILABILITY - FIXED field names
    // ==============================================

    private Boolean acceptsNewClients;

    private Boolean offersVirtualSessions;

    // ✅ ADDED: Missing field that mapper expects
    private Boolean offersInHomeService;

    private Boolean offersGymSessions; // ✅ FIXED: Already exists

    // ✅ KEPT: For backward compatibility (can represent general in-person)
    private Boolean offersInPerson;

    // ==============================================
    // PROFILE SETTINGS - FIXED
    // ==============================================

    // ✅ REMOVED: isPublicProfile (duplicate)
    // ✅ KEPT: Only isProfilePublic to match mapper expectation
    private Boolean isProfilePublic;

    // ==============================================
    // HELPER METHODS
    // ==============================================

    /**
     * Check if any service availability is being updated
     */
    public boolean hasServiceAvailabilityChanges() {
        return acceptsNewClients != null ||
                offersVirtualSessions != null ||
                offersInHomeService != null ||
                offersGymSessions != null ||
                offersInPerson != null;
    }

    /**
     * Check if location information is being updated
     */
    public boolean hasLocationChanges() {
        return baseZipcode != null ||
                (serviceAreas != null && !serviceAreas.isEmpty());
    }

    /**
     * Check if professional details are being updated
     */
    public boolean hasProfessionalDetailsChanges() {
        return serviceType != null ||
                experienceLevel != null ||
                yearsExperience != null ||
                (specializations != null && !specializations.isEmpty()) ||
                (certifications != null && !certifications.isEmpty());
    }

    /**
     * Check if pricing information is being updated
     */
    public boolean hasPricingChanges() {
        return hourlyRate != null;
    }

    /**
     * Check if basic profile information is being updated
     */
    public boolean hasBasicInfoChanges() {
        return displayName != null ||
                businessName != null ||
                bio != null;
    }

    /**
     * Check if any updates are being made
     */
    public boolean hasAnyChanges() {
        return hasBasicInfoChanges() ||
                hasProfessionalDetailsChanges() ||
                hasLocationChanges() ||
                hasPricingChanges() ||
                hasServiceAvailabilityChanges() ||
                isProfilePublic != null;
    }

    /**
     * Get summary of changes being made
     */
    public String getChangesSummary() {
        List<String> changes = new java.util.ArrayList<>();

        if (hasBasicInfoChanges()) {
            changes.add("Basic Information");
        }
        if (hasProfessionalDetailsChanges()) {
            changes.add("Professional Details");
        }
        if (hasLocationChanges()) {
            changes.add("Location");
        }
        if (hasPricingChanges()) {
            changes.add("Pricing");
        }
        if (hasServiceAvailabilityChanges()) {
            changes.add("Service Availability");
        }
        if (isProfilePublic != null) {
            changes.add("Privacy Settings");
        }

        if (changes.isEmpty()) {
            return "No changes";
        }

        return String.join(", ", changes);
    }
}