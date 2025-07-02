package com.chidituke.workout_tracker.repository.user;

import com.chidituke.workout_tracker.model.user.ProfessionalProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProfessionalProfile entity
 * Provides comprehensive data access methods for professional profile operations
 * including advanced search, filtering, and analytics queries
 */
@Repository
public interface ProfessionalProfileRepository extends JpaRepository<ProfessionalProfile, Long>,
        JpaSpecificationExecutor<ProfessionalProfile> {

    // ==============================================
    // BASIC PROFILE QUERIES
    // ==============================================

    /**
     * Find professional profile by user ID
     */
    @Query("SELECT pp FROM ProfessionalProfile pp WHERE pp.user.id = :userId")
    Optional<ProfessionalProfile> findByUserId(@Param("userId") Long userId);

    /**
     * Find all active professional profiles (accepting clients and public)
     */
    @Query("SELECT pp FROM ProfessionalProfile pp WHERE pp.acceptsNewClients = true AND pp.isProfilePublic = true")
    List<ProfessionalProfile> findAllActive();

    /**
     * Find all verified professional profiles
     */
    @Query("SELECT pp FROM ProfessionalProfile pp WHERE pp.isVerified = true AND pp.acceptsNewClients = true AND pp.isProfilePublic = true")
    List<ProfessionalProfile> findAllVerified();

    /**
     * Check if user has professional profile
     */
    @Query("SELECT COUNT(pp) > 0 FROM ProfessionalProfile pp WHERE pp.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);

    // ==============================================
    // LOCATION-BASED QUERIES
    // ==============================================

    /**
     * Find professionals by exact zipcode
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.baseZipcode = :zipcode " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC")
    List<ProfessionalProfile> findByLocation(@Param("zipcode") String zipcode, Pageable pageable);

    /**
     * Find professionals in zipcode area (first 3 digits)
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.baseZipcode LIKE CONCAT(:zipcodePrefix, '%') " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC")
    List<ProfessionalProfile> findByZipcodeArea(@Param("zipcodePrefix") String zipcodePrefix, Pageable pageable);

    /**
     * Find professionals by city (through user relationship)
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "JOIN pp.user u " +
            "WHERE LOWER(u.city) = LOWER(:city) " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC")
    List<ProfessionalProfile> findByCity(@Param("city") String city, Pageable pageable);

    /**
     * Find professionals by state (through user relationship)
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "JOIN pp.user u " +
            "WHERE LOWER(u.state) = LOWER(:state) " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC")
    List<ProfessionalProfile> findByState(@Param("state") String state, Pageable pageable);

    /**
     * Find professionals in service areas (zipcode included in serviceAreas)
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE :zipcode MEMBER OF pp.serviceAreas " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC")
    List<ProfessionalProfile> findByServiceArea(@Param("zipcode") String zipcode, Pageable pageable);

    // ==============================================
    // SERVICE TYPE & SPECIALIZATION QUERIES
    // ==============================================

    /**
     * Find professionals by service type
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.serviceType = :serviceType " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC")
    List<ProfessionalProfile> findByServiceType(@Param("serviceType") ProfessionalProfile.ServiceType serviceType,
                                                Pageable pageable);

    /**
     * Find professionals by specialization
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE :specialization MEMBER OF pp.specializations " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC")
    List<ProfessionalProfile> findBySpecialization(@Param("specialization") String specialization,
                                                   Pageable pageable);

    /**
     * Find professionals by experience level - Note: using yearsExperience field from entity
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.yearsExperience >= :minYears " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.yearsExperience DESC, pp.averageRating DESC")
    List<ProfessionalProfile> findByExperienceLevel(@Param("minYears") Integer minYears, Pageable pageable);

    /**
     * Find professionals with minimum experience years
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.yearsExperience >= :minYears " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.yearsExperience DESC, pp.averageRating DESC")
    List<ProfessionalProfile> findByMinimumExperience(@Param("minYears") Integer minYears,
                                                      Pageable pageable);

    // ==============================================
    // RATING & REVIEW QUERIES
    // ==============================================

    /**
     * Find top-rated professionals
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.averageRating >= :minRating " +
            "AND pp.totalReviews >= :minReviews " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC")
    List<ProfessionalProfile> findTopRated(@Param("minRating") Double minRating,
                                           @Param("minReviews") Integer minReviews,
                                           Pageable pageable);

    /**
     * Find professionals with rating above threshold
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.averageRating >= :rating " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC")
    List<ProfessionalProfile> findByMinimumRating(@Param("rating") Double rating, Pageable pageable);

    /**
     * Find professionals with minimum number of reviews
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.totalReviews >= :minReviews " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.totalReviews DESC, pp.averageRating DESC")
    List<ProfessionalProfile> findByMinimumReviews(@Param("minReviews") Integer minReviews,
                                                   Pageable pageable);

    // ==============================================
    // AVAILABILITY & PRICING QUERIES
    // ==============================================

    /**
     * Find professionals accepting new clients
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC")
    List<ProfessionalProfile> findAcceptingNewClients(Pageable pageable);

    /**
     * Find professionals offering virtual sessions
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.offersVirtualSessions = true " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC")
    List<ProfessionalProfile> findOfferingVirtualSessions(Pageable pageable);

    /**
     * Find professionals offering gym sessions
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.offersGymSessions = true " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC")
    List<ProfessionalProfile> findOfferingGymSessions(Pageable pageable);

    /**
     * Find professionals offering in-home sessions
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.offersInHomeService = true " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC")
    List<ProfessionalProfile> findOfferingInHomeSessions(Pageable pageable);

    /**
     * Find professionals within price range
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.hourlyRate BETWEEN :minRate AND :maxRate " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.hourlyRate ASC, pp.averageRating DESC")
    List<ProfessionalProfile> findByPriceRange(@Param("minRate") Double minRate,
                                               @Param("maxRate") Double maxRate,
                                               Pageable pageable);

    /**
     * Find professionals below maximum price
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.hourlyRate <= :maxRate " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "ORDER BY pp.hourlyRate ASC, pp.averageRating DESC")
    List<ProfessionalProfile> findByMaxPrice(@Param("maxRate") Double maxRate, Pageable pageable);

    // ==============================================
    // FEATURED & DISCOVERY QUERIES
    // ==============================================

    /**
     * Find featured professionals (top-rated, verified, active)
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.isVerified = true " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.averageRating >= 4.0 " +
            "AND pp.totalReviews >= 10 " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC, pp.createdAt ASC")
    List<ProfessionalProfile> findFeatured(Pageable pageable);

    /**
     * Find featured professionals by location
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.isVerified = true " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.averageRating >= 4.0 " +
            "AND pp.totalReviews >= 5 " +
            "AND (pp.baseZipcode = :location OR pp.baseZipcode LIKE CONCAT(:location, '%')) " +
            "ORDER BY pp.averageRating DESC, pp.totalReviews DESC")
    List<ProfessionalProfile> findFeaturedByLocation(@Param("location") String location, Pageable pageable);

    /**
     * Find newly verified professionals
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.isVerified = true " +
            "AND pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.verifiedAt >= :since " +
            "ORDER BY pp.verifiedAt DESC, pp.averageRating DESC")
    List<ProfessionalProfile> findNewlyVerified(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Find recently active professionals
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "JOIN pp.user u " +
            "WHERE pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "AND u.lastActive >= :since " +
            "ORDER BY u.lastActive DESC, pp.averageRating DESC")
    List<ProfessionalProfile> findRecentlyActive(@Param("since") LocalDateTime since, Pageable pageable);

    // ==============================================
    // VERIFICATION & STATUS QUERIES
    // ==============================================

    /**
     * Find pending verification requests
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.isVerified = false " +
            "AND pp.verificationSubmittedAt IS NOT NULL " +
            "ORDER BY pp.verificationSubmittedAt ASC")
    List<ProfessionalProfile> findPendingVerification(Pageable pageable);

    /**
     * Find profiles with expired verification
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.isVerified = true " +
            "AND pp.verifiedAt < :expiryThreshold " +
            "ORDER BY pp.verifiedAt ASC")
    List<ProfessionalProfile> findExpiredVerification(@Param("expiryThreshold") LocalDateTime expiryThreshold,
                                                      Pageable pageable);

    /**
     * Find verified professionals
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.isVerified = true " +
            "ORDER BY pp.verifiedAt DESC")
    List<ProfessionalProfile> findVerifiedProfessionals(Pageable pageable);

    // ==============================================
    // ANALYTICS & STATISTICS QUERIES
    // ==============================================

    /**
     * Count professionals by service type
     */
    @Query("SELECT pp.serviceType, COUNT(pp) FROM ProfessionalProfile pp " +
            "WHERE pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "GROUP BY pp.serviceType " +
            "ORDER BY COUNT(pp) DESC")
    List<Object[]> countByServiceType();

    /**
     * Count professionals by location (first 3 digits of zipcode)
     */
    @Query("SELECT SUBSTRING(pp.baseZipcode, 1, 3), COUNT(pp) FROM ProfessionalProfile pp " +
            "WHERE pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "AND pp.baseZipcode IS NOT NULL " +
            "GROUP BY SUBSTRING(pp.baseZipcode, 1, 3) " +
            "ORDER BY COUNT(pp) DESC")
    List<Object[]> countByLocationArea();

    /**
     * Get average rating by service type
     */
    @Query("SELECT pp.serviceType, AVG(pp.averageRating), COUNT(pp) FROM ProfessionalProfile pp " +
            "WHERE pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "AND pp.averageRating IS NOT NULL " +
            "GROUP BY pp.serviceType " +
            "ORDER BY AVG(pp.averageRating) DESC")
    List<Object[]> getAverageRatingByServiceType();

    /**
     * Get average hourly rate by service type
     */
    @Query("SELECT pp.serviceType, AVG(pp.hourlyRate), COUNT(pp) FROM ProfessionalProfile pp " +
            "WHERE pp.acceptsNewClients = true " +
            "AND pp.isProfilePublic = true " +
            "AND pp.isVerified = true " +
            "AND pp.hourlyRate IS NOT NULL " +
            "GROUP BY pp.serviceType " +
            "ORDER BY AVG(pp.hourlyRate) DESC")
    List<Object[]> getAverageHourlyRateByServiceType();

    /**
     * Find professionals created in date range
     */
    @Query("SELECT pp FROM ProfessionalProfile pp " +
            "WHERE pp.createdAt BETWEEN :startDate AND :endDate " +
            "ORDER BY pp.createdAt DESC")
    List<ProfessionalProfile> findCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate,
                                                 Pageable pageable);

    /**
     * Count total active professionals
     */
    @Query("SELECT COUNT(pp) FROM ProfessionalProfile pp WHERE pp.acceptsNewClients = true AND pp.isProfilePublic = true")
    Long countActiveProfessionals();

    /**
     * Count total verified professionals
     */
    @Query("SELECT COUNT(pp) FROM ProfessionalProfile pp WHERE pp.isVerified = true AND pp.acceptsNewClients = true AND pp.isProfilePublic = true")
    Long countVerifiedProfessionals();

    /**
     * Get completion rate statistics
     */
    @Query("SELECT AVG(pp.profileCompletionPercentage) FROM ProfessionalProfile pp WHERE pp.acceptsNewClients = true AND pp.isProfilePublic = true")
    Double getAverageProfileCompletion();

    // ==============================================
    // COMPLEX SEARCH QUERIES
    // ==============================================

    /**
     * Advanced search with multiple criteria
     * Note: This uses the JpaSpecificationExecutor for complex dynamic queries
     */
    Page<ProfessionalProfile> findAll(Specification<ProfessionalProfile> spec, Pageable pageable);

    /**
     * Find professionals with similar specializations
     */
    @Query(value = "SELECT DISTINCT pp.* FROM professional_profiles pp " +
            "WHERE pp.id != :excludeId " +
            "AND pp.accepts_new_clients = true " +
            "AND pp.is_profile_public = true " +
            "AND pp.is_verified = true " +
            "AND EXISTS (" +
            "  SELECT 1 FROM professional_specializations ps1 " +
            "  JOIN professional_specializations ps2 ON ps1.specialization = ps2.specialization " +
            "  WHERE ps1.professional_id = pp.id " +
            "  AND ps2.professional_id = :excludeId" +
            ") " +
            "ORDER BY pp.average_rating DESC, pp.total_reviews DESC",
            nativeQuery = true)
    List<ProfessionalProfile> findSimilarProfessionals(@Param("excludeId") Long excludeId, Pageable pageable);

    /**
     * Search professionals by multiple criteria (backup native query approach)
     */
    @Query(value = "SELECT DISTINCT pp.* FROM professional_profiles pp " +
            "LEFT JOIN users u ON pp.user_id = u.id " +
            "WHERE pp.accepts_new_clients = true " +
            "AND pp.is_profile_public = true " +
            "AND pp.is_verified = true " +
            "AND (:serviceType IS NULL OR pp.service_type = :serviceType) " +
            "AND (:zipcode IS NULL OR pp.base_zipcode = :zipcode) " +
            "AND (:city IS NULL OR LOWER(u.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
            "AND (:state IS NULL OR LOWER(u.state) LIKE LOWER(CONCAT('%', :state, '%'))) " +
            "AND (:minRating IS NULL OR pp.average_rating >= :minRating) " +
            "AND (:minReviews IS NULL OR pp.total_reviews >= :minReviews) " +
            "AND (:maxRate IS NULL OR pp.hourly_rate <= :maxRate) " +
            "AND (:minRate IS NULL OR pp.hourly_rate >= :minRate) " +
            "AND (:offersVirtual IS NULL OR pp.offers_virtual_sessions = :offersVirtual) " +
            "ORDER BY pp.average_rating DESC, pp.total_reviews DESC",
            nativeQuery = true)
    List<ProfessionalProfile> searchProfessionalsNative(
            @Param("serviceType") String serviceType,
            @Param("zipcode") String zipcode,
            @Param("city") String city,
            @Param("state") String state,
            @Param("minRating") Double minRating,
            @Param("minReviews") Integer minReviews,
            @Param("maxRate") Double maxRate,
            @Param("minRate") Double minRate,
            @Param("offersVirtual") Boolean offersVirtual,
            Pageable pageable);
}