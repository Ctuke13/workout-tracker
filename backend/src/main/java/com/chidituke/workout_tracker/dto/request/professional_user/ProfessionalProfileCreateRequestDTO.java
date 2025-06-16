package com.chidituke.workout_tracker.dto;

import com.chidituke.workout_tracker.model.ProfessionalProfile;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalProfileCreateRequestDTO {

    @NotBlank(message = "Display name is required")
    @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
    private String displayName;

    @NotNull(message = "Service type is required")
    private ProfessionalProfile.ServiceType serviceType;

    @NotNull(message = "Experience level is required")
    private ProfessionalProfile.ExperienceLevel experienceLevel;

    @NotNull(message = "Experience years is required")
    @Min(value = 0, message = "Experience years must be 0 or greater")
    @Max(value = 50, message = "Experience years must be 50 or less")
    private Integer experienceYears;

    @NotBlank(message = "Bio is required")
    @Size(min = 50, max = 2000, message = "Bio must be between 50 and 2000 characters")
    private String bio;

    @Size(min = 1, max = 10, message = "Must have between 1 and 10 specializations")
    private List<String> specializations;

    @NotBlank(message = "Base zipcode is required")
    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid zipcode format")
    private String baseZipcode;

    @Size(max = 20, message = "Cannot have more than 20 service areas")
    private List<String> serviceAreas;

    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "0.01", message = "Hourly rate must be greater than 0")
    @DecimalMax(value = "10000.00", message = "Hourly rate must be less than $10,000")
    private Double hourlyRate;

    @Builder.Default
    private Boolean acceptsNewClients = true;

    @Builder.Default
    private Boolean offersVirtualSessions = false;

    @Builder.Default
    private Boolean offersInPerson = true;

    @Builder.Default
    private Boolean isPublicProfile = true;
}

// ==============================================
// PROFESSIONAL PROFILE UPDATE REQUEST DTO
// ==============================================

package com.chidituke.workout_tracker.dto;

import com.chidituke.workout_tracker.model.ProfessionalProfile;
import jakarta.validation.constraints.*;
        import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalProfileUpdateRequestDTO {

    @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
    private String displayName;

    private ProfessionalProfile.ServiceType serviceType;

    private ProfessionalProfile.ExperienceLevel experienceLevel;

    @Min(value = 0, message = "Experience years must be 0 or greater")
    @Max(value = 50, message = "Experience years must be 50 or less")
    private Integer experienceYears;

    @Size(min = 50, max = 2000, message = "Bio must be between 50 and 2000 characters")
    private String bio;

    @Size(max = 10, message = "Cannot have more than 10 specializations")
    private List<String> specializations;

    @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "Invalid zipcode format")
    private String baseZipcode;

    @Size(max = 20, message = "Cannot have more than 20 service areas")
    private List<String> serviceAreas;

    @DecimalMin(value = "0.01", message = "Hourly rate must be greater than 0")
    @DecimalMax(value = "10000.00", message = "Hourly rate must be less than $10,000")
    private Double hourlyRate;

    private Boolean acceptsNewClients;

    private Boolean offersVirtualSessions;

    private Boolean offersInPerson;

    private Boolean isPublicProfile;

    private Boolean isActive;
}