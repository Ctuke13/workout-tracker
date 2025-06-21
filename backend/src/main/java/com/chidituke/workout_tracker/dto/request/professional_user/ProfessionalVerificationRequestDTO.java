package com.chidituke.workout_tracker.dto.request.professional_user;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalVerificationRequestDTO {

    @NotBlank(message = "Certification type is required")
    @Size(max = 100, message = "Certification type must not exceed 100 characters")
    private String certificationType;

    @NotBlank(message = "Certification number is required")
    @Size(max = 50, message = "Certification number must not exceed 50 characters")
    private String certificationNumber;

    @NotBlank(message = "Issuing organization is required")
    @Size(max = 100, message = "Issuing organization must not exceed 100 characters")
    private String issuingOrganization;

    @NotNull(message = "Issue date is required")
    @PastOrPresent(message = "Issue date cannot be in the future")
    private LocalDate issueDate;

    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @Size(max = 500, message = "Additional notes must not exceed 500 characters")
    private String additionalNotes;

    @Size(max = 2000, message = "Documentation URL must not exceed 2000 characters")
    @Pattern(regexp = "^(https?://).*", message = "Documentation must be a valid HTTPS URL")
    private String documentationUrl;
}