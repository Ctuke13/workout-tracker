package com.chidituke.workout_tracker.mapper.user;

import com.chidituke.workout_tracker.dto.response.professionional_user.ProfessionalVerificationResponseDTO;
import com.chidituke.workout_tracker.dto.request.professional_user.ProfessionalProfileCreateRequestDTO;
import com.chidituke.workout_tracker.dto.request.professional_user.ProfessionalProfileUpdateRequestDTO;
import com.chidituke.workout_tracker.dto.response.professionional_user.ProfessionalProfileResponseDTO;
import com.chidituke.workout_tracker.dto.response.professionional_user.ProfessionalSearchResponseDTO;
import com.chidituke.workout_tracker.dto.response.professionional_user.ProfessionalStatsResponseDTO;
import com.chidituke.workout_tracker.model.user.ProfessionalProfile;
import com.chidituke.workout_tracker.model.user.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper component for converting between ProfessionalProfile entities and DTOs
 * FIXED to match corrected DTO field names and entity structure
 */
@Component
public class ProfessionalProfileMapper {

    // ==============================================
    // CREATE REQUEST MAPPINGS - FIXED
    // ==============================================

    /**
     * Maps create request DTO to new ProfessionalProfile entity
     */
    public ProfessionalProfile mapCreateRequestToEntity(ProfessionalProfileCreateRequestDTO request, User user) {
        if (request == null || user == null) {
            return null;
        }

        return ProfessionalProfile.builder()
                .user(user)
                .displayName(request.getDisplayName())
                .serviceType(request.getServiceType())
                .experienceLevel(request.getExperienceLevel())
                .yearsExperience(request.getYearsExperience())
                .bio(request.getBio())
                .specializations(request.getSpecializations())
                .certifications(request.getCertifications())
                .baseZipcode(request.getBaseZipcode())
                .serviceAreas(request.getServiceAreas())
                .hourlyRate(request.getHourlyRate())
                .acceptsNewClients(request.getAcceptsNewClients() != null ? request.getAcceptsNewClients() : true)
                .offersVirtualSessions(request.getOffersVirtualSessions() != null ? request.getOffersVirtualSessions() : false)
                .offersInHomeService(request.getOffersInHomeService() != null ? request.getOffersInHomeService() :
                        (Boolean.TRUE.equals(request.getOffersInPerson()) ? true : false)) // ✅ ADDED: Backward compatibility
                .offersGymSessions(request.getOffersGymSessions() != null ? request.getOffersGymSessions() :
                        (Boolean.TRUE.equals(request.getOffersInPerson()) ? true : false)) // ✅ ADDED: Backward compatibility
                .isProfilePublic(request.getIsProfilePublic() != null ? request.getIsProfilePublic() : true)
                .isVerified(false)
                .verificationStatus(ProfessionalProfile.VerificationStatus.PENDING)
                .averageRating(0.0)
                .totalReviews(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==============================================
    // UPDATE REQUEST MAPPINGS - FIXED
    // ==============================================

    /**
     * Updates existing ProfessionalProfile entity with data from update request DTO
     */
    public void updateEntityFromRequest(ProfessionalProfile profile, ProfessionalProfileUpdateRequestDTO request) {
        if (request == null || profile == null) {
            return;
        }

        // Basic information updates
        if (request.getDisplayName() != null) {
            profile.setDisplayName(request.getDisplayName());
        }

        if (request.getServiceType() != null) {
            profile.setServiceType(request.getServiceType());
        }

        if (request.getExperienceLevel() != null) {
            profile.setExperienceLevel(request.getExperienceLevel());
        }

        if (request.getYearsExperience() != null) {
            profile.setYearsExperience(request.getYearsExperience());
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        if (request.getSpecializations() != null) {
            profile.setSpecializations(request.getSpecializations());
        }

        if (request.getCertifications() != null) {
            profile.setCertifications(request.getCertifications());
        }

        // Location updates
        if (request.getBaseZipcode() != null) {
            profile.setBaseZipcode(request.getBaseZipcode());
        }

        if (request.getServiceAreas() != null) {
            profile.setServiceAreas(request.getServiceAreas());
        }

        // Pricing updates
        if (request.getHourlyRate() != null) {
            profile.setHourlyRate(request.getHourlyRate());
        }

        // Availability updates
        if (request.getAcceptsNewClients() != null) {
            profile.setAcceptsNewClients(request.getAcceptsNewClients());
        }

        if (request.getOffersVirtualSessions() != null) {
            profile.setOffersVirtualSessions(request.getOffersVirtualSessions());
        }

        if (request.getOffersInHomeService() != null) {
            profile.setOffersInHomeService(request.getOffersInHomeService());
        }

        if (request.getOffersGymSessions() != null) {
            profile.setOffersGymSessions(request.getOffersGymSessions());
        }


        if (request.getOffersInPerson() != null) {
            // If offersInPerson is set, update both specific service types
            profile.setOffersInHomeService(request.getOffersInPerson());
            profile.setOffersGymSessions(request.getOffersInPerson());
        }

        // Professional settings updates
        if (request.getIsProfilePublic() != null) {
            profile.setIsProfilePublic(request.getIsProfilePublic());
        }

        // Always update timestamp
        profile.setUpdatedAt(LocalDateTime.now());
    }

    // ==============================================
    // RESPONSE MAPPINGS - FIXED
    // ==============================================

    /**
     * Maps ProfessionalProfile entity to full response DTO (for profile owner)
     */
    public ProfessionalProfileResponseDTO mapEntityToResponse(ProfessionalProfile profile) {
        if (profile == null) {
            return null;
        }

        User user = profile.getUser();

        return ProfessionalProfileResponseDTO.builder()
                .id(profile.getId())
                .userId(user != null ? user.getId() : null)
                .displayName(profile.getDisplayName())
                .firstName(user != null ? user.getFirstName() : null)
                .lastName(user != null ? user.getLastName() : null)
                .profileImageUrl(user != null ? user.getProfileImageUrl() : null)
                .serviceType(profile.getServiceType())
                .experienceLevel(profile.getExperienceLevel())
                .yearsExperience(profile.getYearsExperience())
                .bio(profile.getBio())
                .specializations(profile.getSpecializations())
                .certifications(profile.getCertifications())
                .baseZipcode(profile.getBaseZipcode())
                .serviceAreas(profile.getServiceAreas())
                .hourlyRate(profile.getHourlyRate())
                .averageRating(profile.getAverageRating())
                .totalReviews(profile.getTotalReviews())
                .acceptsNewClients(profile.getAcceptsNewClients())
                .offersVirtualSessions(profile.getOffersVirtualSessions())
                .offersInHomeService(profile.getOffersInHomeService())
                .offersGymSessions(profile.getOffersGymSessions())
                .isVerified(profile.getIsVerified())
                .verificationStatus(profile.getVerificationStatus())
                .verifiedAt(profile.getVerifiedAt())
                .verificationSubmittedAt(profile.getVerificationSubmittedAt())
                .verificationReviewedAt(profile.getVerificationReviewedAt())
                .isProfilePublic(profile.getIsProfilePublic())
                .profileCompletionPercentage(profile.calculateProfileCompletion())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .lastActiveDate(user != null ? user.getLastActive() : null)
                .isCurrentlyActive(user != null ? Boolean.TRUE.equals(user.getUserEnabled()) : false)
                .build();
    }

    /**
     * Maps ProfessionalProfile entity to public response DTO (for other users viewing)
     */
    public ProfessionalProfileResponseDTO mapEntityToPublicResponse(ProfessionalProfile profile, Long viewerId) {
        if (profile == null) {
            return null;
        }

        User user = profile.getUser();
        boolean isOwner = user != null && viewerId != null && user.getId().equals(viewerId);

        ProfessionalProfileResponseDTO.ProfessionalProfileResponseDTOBuilder builder =
                ProfessionalProfileResponseDTO.builder()
                        .id(profile.getId())
                        .userId(user != null ? user.getId() : null)
                        .displayName(profile.getDisplayName())
                        .firstName(user != null ? user.getFirstName() : null)
                        .lastName(user != null ? user.getLastName() : null)
                        .profileImageUrl(user != null ? user.getProfileImageUrl() : null)
                        .serviceType(profile.getServiceType())
                        .experienceLevel(profile.getExperienceLevel())
                        .yearsExperience(profile.getYearsExperience())
                        .bio(profile.getBio())
                        .specializations(profile.getSpecializations())
                        .hourlyRate(profile.getHourlyRate())
                        .averageRating(profile.getAverageRating())
                        .totalReviews(profile.getTotalReviews())
                        .acceptsNewClients(profile.getAcceptsNewClients())
                        .offersVirtualSessions(profile.getOffersVirtualSessions())
                        .offersInHomeService(profile.getOffersInHomeService())
                        .offersGymSessions(profile.getOffersGymSessions())
                        .isVerified(profile.getIsVerified())
                        .verificationStatus(profile.getVerificationStatus())
                        .lastActiveDate(user != null ? user.getLastActive() : null)
                        .isCurrentlyActive(user != null ? Boolean.TRUE.equals(user.getUserEnabled()) : false);

        // Only include private information if viewer is the owner
        if (isOwner) {
            builder.baseZipcode(profile.getBaseZipcode())
                    .serviceAreas(profile.getServiceAreas())
                    .certifications(profile.getCertifications())
                    .isProfilePublic(profile.getIsProfilePublic())
                    .profileCompletionPercentage(profile.calculateProfileCompletion())
                    .createdAt(profile.getCreatedAt())
                    .updatedAt(profile.getUpdatedAt())
                    .verifiedAt(profile.getVerifiedAt())
                    .verificationSubmittedAt(profile.getVerificationSubmittedAt())
                    .verificationReviewedAt(profile.getVerificationReviewedAt());
        } else {
            // For public viewing, only show general location area
            builder.baseZipcode(profile.getBaseZipcode() != null ?
                    profile.getBaseZipcode().substring(0, 3) + "XX" : null);
        }

        return builder.build();
    }

    /**
     * Maps ProfessionalProfile entity to search response DTO
     */
    public ProfessionalSearchResponseDTO mapEntityToSearchResponse(ProfessionalProfile profile) {
        if (profile == null) {
            return null;
        }

        User user = profile.getUser();

        return ProfessionalSearchResponseDTO.builder()
                .id(profile.getId())
                .displayName(profile.getDisplayName())
                .serviceType(profile.getServiceType())
                .experienceLevel(profile.getExperienceLevel())
                .yearsExperience(profile.getYearsExperience())
                .bio(truncateBio(profile.getBio(), 200))
                .specializations(profile.getSpecializations())
                .baseZipcode(profile.getBaseZipcode())
                .averageRating(profile.getAverageRating())
                .totalReviews(profile.getTotalReviews())
                .acceptsNewClients(profile.getAcceptsNewClients())
                .offersVirtualSessions(profile.getOffersVirtualSessions())
                .offersInHomeService(profile.getOffersInHomeService())
                .offersGymSessions(profile.getOffersGymSessions())
                .hourlyRate(profile.getHourlyRate())
                .isVerified(profile.getIsVerified())
                .profileCompletionPercentage(profile.calculateProfileCompletion())
                .distanceMiles(null) // Will be calculated separately if needed
                .build();
    }

    /**
     * Maps ProfessionalProfile entity to stats response DTO
     */
    public ProfessionalStatsResponseDTO mapEntityToStatsResponse(ProfessionalProfile profile) {
        if (profile == null) {
            return null;
        }

        return ProfessionalStatsResponseDTO.builder()
                // Profile metrics
                .profileViews(0) // Set to 0 or calculate if you have the data
                .profileViewsThisMonth(0)
                .profileCompletionPercentage(profile.calculateProfileCompletion())

                // Client metrics
                .totalClients(0) // Set to 0 or calculate if you have the data
                .activeClients(0)
                .newClientsThisMonth(0)
                .clientRetentionRate(0)

                // Rating metrics
                .averageRating(profile.getAverageRating())
                .totalReviews(profile.getTotalReviews())
                .fiveStarReviews(0) // Set to 0 or calculate if you have the data
                .reviewsThisMonth(0)

                // Activity metrics
                .sessionsCompleted(0) // Set to 0 or calculate if you have the data
                .sessionsThisMonth(0)
                .totalHoursLogged(0)
                .lastActiveDate(profile.getUpdatedAt())

                // Financial metrics
                .totalEarnings(0.0) // Set to 0 or calculate if you have the data
                .earningsThisMonth(0.0)
                .averageSessionRate(profile.getHourlyRate())

                // Growth metrics
                .profileViewGrowth(0.0)
                .clientGrowth(0.0)
                .ratingGrowth(0.0)

                // Rankings
                .localRanking(null)
                .categoryRanking(null)
                .build();
    }

    // ==============================================
    // VERIFICATION MAPPINGS - FIXED
    // ==============================================

    /**
     * Maps ProfessionalProfile entity to verification response DTO
     */
    public ProfessionalVerificationResponseDTO mapEntityToVerificationResponse(ProfessionalProfile profile) {
        if (profile == null) {
            return null;
        }

        return ProfessionalVerificationResponseDTO.builder()
                .verificationId(profile.getId())
                .status(profile.getVerificationStatus())
                .submittedAt(profile.getVerificationSubmittedAt())
                .reviewedAt(profile.getVerificationReviewedAt())
                .isValid(profile.getIsVerified())
                .build();
    }

    // ==============================================
    // LIST MAPPINGS
    // ==============================================

    /**
     * Maps list of ProfessionalProfile entities to search response DTOs
     */
    public List<ProfessionalSearchResponseDTO> mapEntitiesToSearchResponses(List<ProfessionalProfile> profiles) {
        if (profiles == null) {
            return null;
        }

        return profiles.stream()
                .map(this::mapEntityToSearchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps list of ProfessionalProfile entities to response DTOs
     */
    public List<ProfessionalProfileResponseDTO> mapEntitiesToResponses(List<ProfessionalProfile> profiles) {
        if (profiles == null) {
            return null;
        }

        return profiles.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    // ==============================================
    // HELPER METHODS
    // ==============================================

    /**
     * Truncates bio to specified length for search results
     */
    private String truncateBio(String bio, int maxLength) {
        if (bio == null || bio.length() <= maxLength) {
            return bio;
        }
        return bio.substring(0, maxLength - 3) + "...";
    }

    /**
     * Formats rating display string
     */
    private String formatRatingDisplay(Double averageRating, Integer totalReviews) {
        if (averageRating == null || totalReviews == null || totalReviews == 0) {
            return "No ratings yet";
        }
        return String.format("%.1f ⭐ (%d reviews)", averageRating, totalReviews);
    }

    /**
     * Formats pricing display string
     */
    private String formatPricingDisplay(Double hourlyRate) {
        if (hourlyRate == null) {
            return "Contact for pricing";
        }
        return String.format("$%.0f/hour", hourlyRate);
    }

    /**
     * Gets availability status string
     */
    private String getAvailabilityStatus(ProfessionalProfile profile) {
        if (!Boolean.TRUE.equals(profile.getAcceptsNewClients())) {
            return "Not accepting new clients";
        }
        if (!Boolean.TRUE.equals(profile.getIsProfilePublic())) {
            return "Profile private";
        }
        return "Available";
    }

    /**
     * Check if profile has complete required information
     */
    public boolean isProfileComplete(ProfessionalProfile profile) {
        if (profile == null) {
            return false;
        }

        return profile.getDisplayName() != null &&
                profile.getServiceType() != null &&
                profile.getExperienceLevel() != null &&
                profile.getYearsExperience() != null &&
                profile.getBio() != null &&
                profile.getSpecializations() != null && !profile.getSpecializations().isEmpty() &&
                profile.getBaseZipcode() != null &&
                profile.getHourlyRate() != null;
    }

    /**
     * Create a minimal profile response for listings
     */
    public ProfessionalSearchResponseDTO createMinimalSearchResponse(ProfessionalProfile profile) {
        if (profile == null) {
            return null;
        }

        return ProfessionalSearchResponseDTO.builder()
                .id(profile.getId())
                .displayName(profile.getDisplayName())
                .serviceType(profile.getServiceType())
                .experienceLevel(profile.getExperienceLevel())
                .baseZipcode(profile.getBaseZipcode())
                .hourlyRate(profile.getHourlyRate())
                .averageRating(profile.getAverageRating())
                .totalReviews(profile.getTotalReviews())
                .acceptsNewClients(profile.getAcceptsNewClients())
                .isVerified(profile.getIsVerified())
                .build();
    }
}