package com.chidituke.workout_tracker.controller;

import com.chidituke.workout_tracker.dto.response.professionional_user.ProfessionalVerificationResponseDTO;
import com.chidituke.workout_tracker.dto.request.professional_user.ProfessionalProfileCreateRequestDTO;
import com.chidituke.workout_tracker.dto.request.professional_user.ProfessionalProfileUpdateRequestDTO;
import com.chidituke.workout_tracker.dto.request.professional_user.ProfessionalSearchRequestDTO;
import com.chidituke.workout_tracker.dto.request.professional_user.ProfessionalVerificationRequestDTO;
import com.chidituke.workout_tracker.dto.response.professionional_user.ProfessionalProfileResponseDTO;
import com.chidituke.workout_tracker.dto.response.professionional_user.ProfessionalSearchResponseDTO;
import com.chidituke.workout_tracker.dto.response.professionional_user.ProfessionalStatsResponseDTO;
import com.chidituke.workout_tracker.model.ProfessionalProfile;
import com.chidituke.workout_tracker.security.UserPrincipal;
import com.chidituke.workout_tracker.service.ProfessionalProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Professional Profile operations
 * Fixed imports and references to use correct DTOs and field names
 */
@RestController
@RequestMapping("/api/professionals")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Professional Profiles", description = "Professional profile management endpoints")
public class ProfessionalProfileController {

    private final ProfessionalProfileService professionalProfileService;

    // ==============================================
    // PROFILE MANAGEMENT ENDPOINTS
    // ==============================================

    @PostMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create professional profile",
            description = "Create a new professional profile for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Profile created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Profile already exists")
    })
    public ResponseEntity<ProfessionalProfileResponseDTO> createProfile(
            @Valid @RequestBody ProfessionalProfileCreateRequestDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Creating professional profile for user: {}", userPrincipal.getId());

        ProfessionalProfileResponseDTO response = professionalProfileService.createProfile(request, userPrincipal.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update professional profile",
            description = "Update the authenticated user's professional profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<ProfessionalProfileResponseDTO> updateProfile(
            @Valid @RequestBody ProfessionalProfileUpdateRequestDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Updating professional profile for user: {}", userPrincipal.getId());

        ProfessionalProfileResponseDTO response = professionalProfileService.updateProfile(request, userPrincipal.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get my professional profile",
            description = "Get the authenticated user's professional profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<ProfessionalProfileResponseDTO> getMyProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Getting professional profile for user: {}", userPrincipal.getId());

        ProfessionalProfileResponseDTO response = professionalProfileService.getProfileByUserId(userPrincipal.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get professional profile by user ID",
            description = "Get a professional profile by user ID (public view)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<ProfessionalProfileResponseDTO> getProfileByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        log.info("Getting public professional profile for user: {}", userId);

        ProfessionalProfileResponseDTO response = professionalProfileService.getPublicProfileByUserId(userId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete professional profile",
            description = "Delete the authenticated user's professional profile")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Profile deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Profile not found")
    })
    public ResponseEntity<Void> deleteProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Deleting professional profile for user: {}", userPrincipal.getId());

        professionalProfileService.deleteProfile(userPrincipal.getId());

        return ResponseEntity.noContent().build();
    }

    // ==============================================
    // SEARCH & DISCOVERY ENDPOINTS
    // ==============================================

    @PostMapping("/search")
    @Operation(summary = "Advanced professional search",
            description = "Search professionals with multiple criteria")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
    })
    public ResponseEntity<Page<ProfessionalSearchResponseDTO>> searchProfessionals(
            @Valid @RequestBody ProfessionalSearchRequestDTO request,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "averageRating") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("Searching professionals with criteria: {}", request);

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ProfessionalSearchResponseDTO> results = professionalProfileService.searchProfessionals(request, pageable);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured professionals",
            description = "Get top-rated verified professionals")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Featured professionals retrieved successfully")
    })
    public ResponseEntity<List<ProfessionalSearchResponseDTO>> getFeaturedProfessionals(
            @Parameter(description = "Maximum number of results") @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Location filter (zipcode)") @RequestParam(required = false) String location) {

        log.info("Getting featured professionals, limit: {}, location: {}", limit, location);

        List<ProfessionalSearchResponseDTO> featured = professionalProfileService.getFeaturedProfessionals(limit, location);

        return ResponseEntity.ok(featured);
    }

    @GetMapping("/zipcode/{zipcode}")
    @Operation(summary = "Get professionals by zipcode",
            description = "Find professionals in a specific zipcode area")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Professionals retrieved successfully")
    })
    public ResponseEntity<List<ProfessionalSearchResponseDTO>> getProfessionalsByZipcode(
            @Parameter(description = "Zipcode") @PathVariable String zipcode,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        log.info("Getting professionals by zipcode: {}", zipcode);

        Pageable pageable = PageRequest.of(page, size);
        List<ProfessionalSearchResponseDTO> professionals = professionalProfileService.getProfessionalsByLocation(zipcode, pageable);

        return ResponseEntity.ok(professionals);
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Get professionals by city",
            description = "Find professionals in a specific city")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Professionals retrieved successfully")
    })
    public ResponseEntity<List<ProfessionalSearchResponseDTO>> getProfessionalsByCity(
            @Parameter(description = "City name") @PathVariable String city,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        log.info("Getting professionals by city: {}", city);

        Pageable pageable = PageRequest.of(page, size);
        List<ProfessionalSearchResponseDTO> professionals = professionalProfileService.getProfessionalsByCity(city, pageable);

        return ResponseEntity.ok(professionals);
    }

    @GetMapping("/service/{serviceType}")
    @Operation(summary = "Get professionals by service type",
            description = "Find professionals by their service type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Professionals retrieved successfully")
    })
    public ResponseEntity<List<ProfessionalSearchResponseDTO>> getProfessionalsByServiceType(
            @Parameter(description = "Service type") @PathVariable ProfessionalProfile.ServiceType serviceType,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        log.info("Getting professionals by service type: {}", serviceType);

        Pageable pageable = PageRequest.of(page, size);
        List<ProfessionalSearchResponseDTO> professionals = professionalProfileService.getProfessionalsByServiceType(serviceType, pageable);

        return ResponseEntity.ok(professionals);
    }

    // ==============================================
    // VERIFICATION ENDPOINTS
    // ==============================================

    @PostMapping("/verification")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Submit verification request",
            description = "Submit professional verification documentation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verification request submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid verification data"),
            @ApiResponse(responseCode = "404", description = "Professional profile not found")
    })
    public ResponseEntity<ProfessionalVerificationResponseDTO> submitVerification(
            @Valid @RequestBody ProfessionalVerificationRequestDTO request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Submitting verification for user: {}", userPrincipal.getId());

        ProfessionalVerificationResponseDTO response = professionalProfileService.submitVerification(request, userPrincipal.getId());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{professionalId}/verification")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Process verification request",
            description = "Admin endpoint to approve or reject verification")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verification processed successfully"),
            @ApiResponse(responseCode = "403", description = "Admin access required"),
            @ApiResponse(responseCode = "404", description = "Professional profile not found")
    })
    public ResponseEntity<ProfessionalVerificationResponseDTO> processVerification(
            @Parameter(description = "Professional profile ID") @PathVariable Long professionalId,
            @Parameter(description = "Verification status") @RequestParam ProfessionalProfile.VerificationStatus status,
            @Parameter(description = "Review notes") @RequestParam(required = false) String notes,
            @AuthenticationPrincipal UserPrincipal adminPrincipal) {

        log.info("Processing verification for professional: {}, status: {}, admin: {}",
                professionalId, status, adminPrincipal.getId());

        ProfessionalVerificationResponseDTO response = professionalProfileService.processVerification(
                professionalId, status, notes, adminPrincipal.getId());

        return ResponseEntity.ok(response);
    }

    // ==============================================
    // STATISTICS & ANALYTICS ENDPOINTS
    // ==============================================

    @GetMapping("/stats")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get my professional statistics",
            description = "Get statistics for the authenticated user's professional profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Professional profile not found")
    })
    public ResponseEntity<ProfessionalStatsResponseDTO> getMyStats(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("Getting professional statistics for user: {}", userPrincipal.getId());

        ProfessionalStatsResponseDTO stats = professionalProfileService.getProfessionalStats(userPrincipal.getId());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users/{userId}/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get professional statistics by user ID",
            description = "Admin endpoint to get any user's professional statistics")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Admin access required"),
            @ApiResponse(responseCode = "404", description = "Professional profile not found")
    })
    public ResponseEntity<ProfessionalStatsResponseDTO> getStatsByUserId(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal adminPrincipal) {

        log.info("Getting professional statistics for user: {}, requested by admin: {}",
                userId, adminPrincipal.getId());

        ProfessionalStatsResponseDTO stats = professionalProfileService.getProfessionalStats(userId);

        return ResponseEntity.ok(stats);
    }

    // ==============================================
    // UTILITY ENDPOINTS
    // ==============================================

    @GetMapping("/service-types")
    @Operation(summary = "Get available service types",
            description = "Get list of available professional service types")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service types retrieved successfully")
    })
    public ResponseEntity<List<ProfessionalProfile.ServiceType>> getServiceTypes() {

        log.info("Getting available service types");

        List<ProfessionalProfile.ServiceType> serviceTypes = List.of(ProfessionalProfile.ServiceType.values());

        return ResponseEntity.ok(serviceTypes);
    }

    @GetMapping("/experience-levels")
    @Operation(summary = "Get available experience levels",
            description = "Get list of available experience levels")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Experience levels retrieved successfully")
    })
    public ResponseEntity<List<ProfessionalProfile.ExperienceLevel>> getExperienceLevels() {

        log.info("Getting available experience levels");

        List<ProfessionalProfile.ExperienceLevel> experienceLevels = List.of(ProfessionalProfile.ExperienceLevel.values());

        return ResponseEntity.ok(experienceLevels);
    }

    @GetMapping("/verification-statuses")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get verification statuses",
            description = "Admin endpoint to get available verification statuses")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verification statuses retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    public ResponseEntity<List<ProfessionalProfile.VerificationStatus>> getVerificationStatuses() {

        log.info("Getting available verification statuses");

        List<ProfessionalProfile.VerificationStatus> statuses = List.of(ProfessionalProfile.VerificationStatus.values());

        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check",
            description = "Check if the professional profiles service is healthy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    public ResponseEntity<String> healthCheck() {

        log.debug("Professional profiles health check");

        return ResponseEntity.ok("Professional Profiles Service is healthy");
    }

    @GetMapping("/info")
    @Operation(summary = "Get API information",
            description = "Get information about the Professional Profiles API")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "API information retrieved successfully")
    })
    public ResponseEntity<Object> getApiInfo() {

        log.debug("Getting professional profiles API info");

        return ResponseEntity.ok(java.util.Map.of(
                "service", "Professional Profiles API",
                "version", "1.0",
                "description", "Professional profile management and search capabilities",
                "endpoints", java.util.Map.of(
                        "profiles", "/api/professionals/profile",
                        "search", "/api/professionals/search",
                        "verification", "/api/professionals/verification",
                        "stats", "/api/professionals/stats"
                )
        ));
    }
}