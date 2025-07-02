package com.chidituke.workout_tracker.service.user;

import com.chidituke.workout_tracker.dto.response.professionional_user.ProfessionalVerificationResponseDTO;
import com.chidituke.workout_tracker.dto.request.professional_user.ProfessionalProfileCreateRequestDTO;
import com.chidituke.workout_tracker.dto.request.professional_user.ProfessionalProfileUpdateRequestDTO;
import com.chidituke.workout_tracker.dto.request.professional_user.ProfessionalSearchRequestDTO;
import com.chidituke.workout_tracker.dto.request.professional_user.ProfessionalVerificationRequestDTO;
import com.chidituke.workout_tracker.dto.response.professionional_user.ProfessionalProfileResponseDTO;
import com.chidituke.workout_tracker.dto.response.professionional_user.ProfessionalSearchResponseDTO;
import com.chidituke.workout_tracker.dto.response.professionional_user.ProfessionalStatsResponseDTO;
import com.chidituke.workout_tracker.exceptions.user.ProfessionalVerificationException;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.mapper.user.ProfessionalProfileMapper;
import com.chidituke.workout_tracker.model.user.ProfessionalProfile;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.user.ProfessionalProfileRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for Professional Profile management
 * Fixed to include proper JPA Criteria API imports and method corrections
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfessionalProfileService {

    private final ProfessionalProfileRepository professionalProfileRepository;
    private final UserRepository userRepository;
    private final ProfessionalProfileMapper professionalProfileMapper;

    // ✅ ADDED: EntityManager for advanced queries if needed
    @PersistenceContext
    private EntityManager entityManager;

    // ==============================================
    // PROFILE MANAGEMENT - FIXED METHOD SIGNATURES
    // ==============================================

    /**
     * Create a new professional profile for a user
     */
    @Transactional
    @CacheEvict(value = {"professionalProfiles", "professionalSearch"}, allEntries = true)
    public ProfessionalProfileResponseDTO createProfile(ProfessionalProfileCreateRequestDTO request, Long userId) {
        log.info("Creating professional profile for user: {}", userId);

        // Validate user exists and doesn't already have a professional profile
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        if (professionalProfileRepository.existsByUserId(userId)) {
            throw new ProfessionalVerificationException("User already has a professional profile");
        }

        // Create and save professional profile
        ProfessionalProfile profile = professionalProfileMapper.mapCreateRequestToEntity(request, user);
        ProfessionalProfile savedProfile = professionalProfileRepository.save(profile);

        log.info("Successfully created professional profile with ID: {}", savedProfile.getId());
        return professionalProfileMapper.mapEntityToResponse(savedProfile);
    }

    /**
     * Update an existing professional profile
     */
    @Transactional
    @CacheEvict(value = {"professionalProfiles", "professionalSearch"}, allEntries = true)
    public ProfessionalProfileResponseDTO updateProfile(ProfessionalProfileUpdateRequestDTO request, Long userId) {
        log.info("Updating professional profile for user: {}", userId);

        ProfessionalProfile profile = findProfessionalProfileByUserId(userId);

        // Apply updates
        professionalProfileMapper.updateEntityFromRequest(profile, request);

        ProfessionalProfile savedProfile = professionalProfileRepository.save(profile);

        log.info("Successfully updated professional profile ID: {}", savedProfile.getId());
        return professionalProfileMapper.mapEntityToResponse(savedProfile);
    }

    /**
     * Get professional profile by user ID (owner view)
     */
    @Cacheable(value = "professionalProfiles", key = "#userId")
    public ProfessionalProfileResponseDTO getProfileByUserId(Long userId) {
        log.debug("Retrieving professional profile for user: {}", userId);

        ProfessionalProfile profile = findProfessionalProfileByUserId(userId);
        return professionalProfileMapper.mapEntityToResponse(profile);
    }

    /**
     * Get public professional profile (limited data for non-owners)
     */
    @Cacheable(value = "publicProfessionalProfiles", key = "#userId")
    public ProfessionalProfileResponseDTO getPublicProfileByUserId(Long userId) {
        log.debug("Retrieving public professional profile for user: {}", userId);

        ProfessionalProfile profile = findProfessionalProfileByUserId(userId);

        // Check if profile is public
        if (!Boolean.TRUE.equals(profile.getIsProfilePublic())) {
            throw new ProfessionalVerificationException("Profile is private");
        }

        // ✅ FIXED: Added viewerId parameter (null for public access)
        return professionalProfileMapper.mapEntityToPublicResponse(profile, null);
    }

    /**
     * Delete professional profile
     */
    @Transactional
    @CacheEvict(value = {"professionalProfiles", "professionalSearch"}, allEntries = true)
    public void deleteProfile(Long userId) {
        log.info("Deleting professional profile for user: {}", userId);

        ProfessionalProfile profile = findProfessionalProfileByUserId(userId);
        professionalProfileRepository.delete(profile);

        log.info("Successfully deleted professional profile for user: {}", userId);
    }

    // ==============================================
    // VERIFICATION METHODS - FIXED
    // ==============================================

    /**
     * Submit verification request
     */
    @Transactional
    @CacheEvict(value = "professionalProfiles", key = "#userId")
    public ProfessionalVerificationResponseDTO submitVerification(ProfessionalVerificationRequestDTO request, Long userId) {
        log.info("Submitting verification request for user: {}", userId);

        ProfessionalProfile profile = findProfessionalProfileByUserId(userId);

        // Update verification status
        profile.setVerificationStatus(ProfessionalProfile.VerificationStatus.PENDING);
        profile.setVerificationSubmittedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        ProfessionalProfile savedProfile = professionalProfileRepository.save(profile);

        log.info("Verification request submitted for professional profile: {}", savedProfile.getId());

        return ProfessionalVerificationResponseDTO.builder()
                .verificationId(savedProfile.getId())
                .status(savedProfile.getVerificationStatus())
                .submittedAt(savedProfile.getVerificationSubmittedAt())
                .reviewedAt(savedProfile.getVerificationReviewedAt())
                .isValid(savedProfile.getIsVerified())
                .build();
    }

    /**
     * Admin method to approve/reject verification
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = {"professionalProfiles", "professionalSearch"}, allEntries = true)
    public ProfessionalVerificationResponseDTO processVerification(Long profileId,
                                                                   ProfessionalProfile.VerificationStatus status,
                                                                   String reviewerNotes,
                                                                   Long adminId) {
        log.info("Processing verification request for profile: {} with status: {}", profileId, status);

        ProfessionalProfile profile = professionalProfileRepository.findById(profileId)
                .orElseThrow(() -> new UserNotFoundException("Professional profile not found"));

        profile.setVerificationStatus(status);
        profile.setVerificationReviewedAt(LocalDateTime.now());
        profile.setUpdatedAt(LocalDateTime.now());

        if (status == ProfessionalProfile.VerificationStatus.VERIFIED) {
            profile.setIsVerified(true);
            profile.setVerifiedAt(LocalDateTime.now());
        } else if (status == ProfessionalProfile.VerificationStatus.REJECTED) {
            profile.setIsVerified(false);
        }

        ProfessionalProfile savedProfile = professionalProfileRepository.save(profile);

        log.info("Verification processed for profile: {} with final status: {}", profileId, status);

        return ProfessionalVerificationResponseDTO.builder()
                .verificationId(savedProfile.getId())
                .status(savedProfile.getVerificationStatus())
                .submittedAt(savedProfile.getVerificationSubmittedAt())
                .reviewedAt(savedProfile.getVerificationReviewedAt())
                .isValid(savedProfile.getIsVerified())
                .build();
    }

    // ==============================================
    // SEARCH & DISCOVERY - FIXED SPECIFICATIONS
    // ==============================================

    /**
     * Search for professional profiles with advanced filtering
     */
    @Cacheable(value = "professionalSearch", key = "#request.toString() + '_' + #pageable.toString()")
    public Page<ProfessionalSearchResponseDTO> searchProfessionals(ProfessionalSearchRequestDTO request, Pageable pageable) {
        log.debug("Searching professionals with criteria: {}", request);

        Specification<ProfessionalProfile> spec = buildSearchSpecification(request);

        // Execute search
        Page<ProfessionalProfile> profiles = professionalProfileRepository.findAll(spec, pageable);

        // Convert to DTOs
        List<ProfessionalSearchResponseDTO> results = profiles.getContent().stream()
                .map(professionalProfileMapper::mapEntityToSearchResponse)
                .collect(Collectors.toList());

        log.debug("Found {} professional profiles matching search criteria", results.size());
        return new PageImpl<>(results, pageable, profiles.getTotalElements());
    }

    /**
     * Get featured professionals
     */
    @Cacheable(value = "featuredProfessionals", key = "#limit + '_' + (#location != null ? #location : 'all')")
    public List<ProfessionalSearchResponseDTO> getFeaturedProfessionals(int limit, String location) {
        log.debug("Getting {} featured professionals for location: {}", limit, location);

        Pageable pageable = PageRequest.of(0, limit);
        List<ProfessionalProfile> profiles;

        if (location != null && !location.trim().isEmpty()) {
            profiles = professionalProfileRepository.findFeaturedByLocation(location, pageable);
        } else {
            profiles = professionalProfileRepository.findFeatured(pageable);
        }

        return profiles.stream()
                .map(professionalProfileMapper::mapEntityToSearchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get professionals by location
     */
    public List<ProfessionalSearchResponseDTO> getProfessionalsByLocation(String zipcode, Pageable pageable) {
        log.debug("Getting professionals in zipcode: {}", zipcode);

        List<ProfessionalProfile> profiles = professionalProfileRepository.findByLocation(zipcode, pageable);

        return profiles.stream()
                .map(professionalProfileMapper::mapEntityToSearchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get professionals by city
     */
    public List<ProfessionalSearchResponseDTO> getProfessionalsByCity(String city, Pageable pageable) {
        log.debug("Getting professionals in city: {}", city);

        List<ProfessionalProfile> profiles = professionalProfileRepository.findByCity(city, pageable);

        return profiles.stream()
                .map(professionalProfileMapper::mapEntityToSearchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get professionals by service type
     */
    public List<ProfessionalSearchResponseDTO> getProfessionalsByServiceType(ProfessionalProfile.ServiceType serviceType, Pageable pageable) {
        log.debug("Getting professionals by service type: {}", serviceType);

        List<ProfessionalProfile> profiles = professionalProfileRepository.findByServiceType(serviceType, pageable);

        return profiles.stream()
                .map(professionalProfileMapper::mapEntityToSearchResponse)
                .collect(Collectors.toList());
    }

    // ==============================================
    // STATISTICS & ANALYTICS - FIXED
    // ==============================================

    /**
     * Get comprehensive statistics for a professional
     */
    @Cacheable(value = "professionalStats", key = "#userId")
    public ProfessionalStatsResponseDTO getProfessionalStats(Long userId) {
        log.debug("Getting statistics for professional: {}", userId);

        ProfessionalProfile profile = findProfessionalProfileByUserId(userId);

        return professionalProfileMapper.mapEntityToStatsResponse(profile);
    }

    // ==============================================
    // HELPER METHODS - FIXED SPECIFICATION BUILDER
    // ==============================================

    private ProfessionalProfile findProfessionalProfileByUserId(Long userId) {
        return professionalProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("Professional profile not found for user: " + userId));
    }

    private Specification<ProfessionalProfile> buildSearchSpecification(ProfessionalSearchRequestDTO request) {
        return (Root<ProfessionalProfile> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Base filters - only include public, verified profiles
            predicates.add(criteriaBuilder.isTrue(root.get("acceptsNewClients")));
            predicates.add(criteriaBuilder.isTrue(root.get("isProfilePublic")));

            // Location filters
            if (request.getZipcode() != null && !request.getZipcode().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("baseZipcode"), request.getZipcode()));
            }

            if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
                Join<ProfessionalProfile, User> userJoin = root.join("user", JoinType.INNER);
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("city")),
                        "%" + request.getCity().toLowerCase() + "%"
                ));
            }

            if (request.getState() != null && !request.getState().trim().isEmpty()) {
                Join<ProfessionalProfile, User> userJoin = root.join("user", JoinType.INNER);
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(userJoin.get("state")),
                        "%" + request.getState().toLowerCase() + "%"
                ));
            }

            // Service filters
            if (request.getServiceType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("serviceType"), request.getServiceType()));
            }

            if (request.getExperienceLevel() != null) {
                predicates.add(criteriaBuilder.equal(root.get("experienceLevel"), request.getExperienceLevel()));
            }

            // Availability filters
            if (request.getAcceptsNewClients() != null && request.getAcceptsNewClients()) {
                predicates.add(criteriaBuilder.isTrue(root.get("acceptsNewClients")));
            }

            if (request.getOffersVirtualSessions() != null && request.getOffersVirtualSessions()) {
                predicates.add(criteriaBuilder.isTrue(root.get("offersVirtualSessions")));
            }

            if (request.getVerifiedOnly() != null && request.getVerifiedOnly()) {
                predicates.add(criteriaBuilder.isTrue(root.get("isVerified")));
            }

            // Rating filters
            if (request.getMinRating() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), request.getMinRating()));
            }

            if (request.getMinReviews() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalReviews"), request.getMinReviews()));
            }

            // Experience filters
            if (request.getMinExperienceYears() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("yearsExperience"), request.getMinExperienceYears()));
            }

            // Pricing filters
            if (request.getMinHourlyRate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("hourlyRate"), request.getMinHourlyRate()));
            }

            if (request.getMaxHourlyRate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("hourlyRate"), request.getMaxHourlyRate()));
            }

            // Service type filters
            if (request.getOffersInHomeService() != null && request.getOffersInHomeService()) {
                predicates.add(criteriaBuilder.isTrue(root.get("offersInHomeService")));
            }

            if (request.getOffersGymSessions() != null && request.getOffersGymSessions()) {
                predicates.add(criteriaBuilder.isTrue(root.get("offersGymSessions")));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

// ==============================================
// SPECIFICATION EXAMPLES FOR REFERENCE
// ==============================================

/**
 * Additional Specification examples you can use:
 */
class ProfessionalProfileSpecifications {

    /**
     * Find professionals by service type
     */
    public static Specification<ProfessionalProfile> hasServiceType(ProfessionalProfile.ServiceType serviceType) {
        return (root, query, criteriaBuilder) ->
                serviceType == null ? criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get("serviceType"), serviceType);
    }

    /**
     * Find professionals in location radius
     */
    public static Specification<ProfessionalProfile> withinRadius(String zipcode, double radiusMiles) {
        return (root, query, criteriaBuilder) -> {
            if (zipcode == null) return criteriaBuilder.conjunction();

            // For simple zipcode matching (you'd implement distance calculation)
            return criteriaBuilder.like(root.get("baseZipcode"), zipcode.substring(0, 3) + "%");
        };
    }

    /**
     * Find verified professionals only
     */
    public static Specification<ProfessionalProfile> isVerified() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("isVerified"));
    }

    /**
     * Find professionals with minimum rating
     */
    public static Specification<ProfessionalProfile> hasMinimumRating(Double minRating) {
        return (root, query, criteriaBuilder) ->
                minRating == null ? criteriaBuilder.conjunction() :
                        criteriaBuilder.greaterThanOrEqualTo(root.get("averageRating"), minRating);
    }

    /**
     * Find professionals accepting new clients
     */
    public static Specification<ProfessionalProfile> acceptingClients() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.isTrue(root.get("acceptsNewClients")),
                        criteriaBuilder.isTrue(root.get("isProfilePublic"))
                );
    }

    /**
     * Complex search combining multiple criteria
     */
    public static Specification<ProfessionalProfile> searchCriteria(
            ProfessionalProfile.ServiceType serviceType,
            String city,
            Double minRating,
            Boolean verifiedOnly) {

        return Specification
                .where(hasServiceType(serviceType))
                .and(verifiedOnly ? isVerified() : null)
                .and(hasMinimumRating(minRating))
                .and(acceptingClients())
                .and((root, query, criteriaBuilder) -> {
                    if (city == null) return criteriaBuilder.conjunction();

                    Join<ProfessionalProfile, User> userJoin = root.join("user", JoinType.INNER);
                    return criteriaBuilder.like(
                            criteriaBuilder.lower(userJoin.get("city")),
                            "%" + city.toLowerCase() + "%"
                    );
                });
    }
}