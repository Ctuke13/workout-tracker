package com.chidituke.workout_tracker.repository.user;

import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.UserType;
import com.chidituke.workout_tracker.model.user.enums.SubscriptionTier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ==================== AUTHENTICATION & BASIC QUERIES ====================

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // ==================== LOCATION-BASED QUERIES (NEW FOR ALL USERS) ====================

    /**
     * Find users by zipcode
     */
    List<User> findByZipcodeAndAccountStatus(String zipcode, User.AccountStatus accountStatus);

    /**
     * Find users by city (case insensitive)
     */
    List<User> findByCityContainingIgnoreCaseAndAccountStatus(String city, User.AccountStatus accountStatus);

    /**
     * Find users by state (case insensitive)
     */
    List<User> findByStateIgnoreCaseAndAccountStatus(String state, User.AccountStatus accountStatus);

    /**
     * Find users within a specific area (zipcode OR city/state)
     */
    @Query("SELECT u FROM User u WHERE u.accountStatus = :accountStatus AND " +
            "(u.zipcode = :location OR LOWER(u.city) LIKE LOWER(CONCAT('%', :location, '%')) " +
            "OR LOWER(u.state) LIKE LOWER(CONCAT('%', :location, '%')))")
    List<User> findByLocationAndAccountStatus(@Param("location") String location,
                                              @Param("accountStatus") User.AccountStatus accountStatus);

    /**
     * Find users with location information
     */
    @Query("SELECT u FROM User u WHERE u.accountStatus = 'ACTIVE' AND " +
            "(u.zipcode IS NOT NULL OR (u.city IS NOT NULL AND u.state IS NOT NULL))")
    List<User> findUsersWithLocationInfo();

    // ==================== USER TYPE QUERIES ====================

    List<User> findByUserType(UserType userType);

    @Query("SELECT u FROM User u WHERE u.userType = :userType AND u.accountStatus = 'ACTIVE'")
    List<User> findActiveUsersByType(@Param("userType") UserType userType);

    // ==================== PROFESSIONAL USER QUERIES ====================

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL AND u.accountStatus = 'ACTIVE'")
    List<User> findActiveProfessionals();

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.accountStatus = 'ACTIVE' AND u.subscriptionTier = 'PRO_PROFESSIONAL'")
    List<User> findActiveProfessionalsWithSubscription();

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.professionalProfile.isProfilePublic = true AND u.accountStatus = 'ACTIVE'")
    List<User> findPublicProfessionals();

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.professionalProfile.acceptsNewClients = true AND u.accountStatus = 'ACTIVE'")
    List<User> findProfessionalsAcceptingClients();

    // ==================== DIRECTORY & DISCOVERY QUERIES ====================

    @Query("SELECT u FROM User u WHERE u.privacySettings = 'PUBLIC' AND u.accountStatus = 'ACTIVE'")
    Page<User> findPublicUsers(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.privacySettings = 'PUBLIC' AND u.professionalProfile.isProfilePublic = true AND u.accountStatus = 'ACTIVE'")
    Page<User> findPublicProfessionalsForDirectory(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.privacySettings = 'PUBLIC' AND u.professionalProfile.isProfilePublic = true AND u.accountStatus = 'ACTIVE' " +
            "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchPublicProfessionals(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ==================== SUBSCRIPTION-BASED QUERIES ====================

    @Query("SELECT u FROM User u WHERE u.subscriptionTier = :tier AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersBySubscriptionTier(@Param("tier") SubscriptionTier tier);

    @Query("SELECT u FROM User u WHERE u.subscriptionTier != 'FREE' AND u.accountStatus = 'ACTIVE'")
    List<User> findPaidSubscribers();

    @Query("SELECT u FROM User u WHERE u.subscriptionTier = 'FREE' AND u.accountStatus = 'ACTIVE'")
    List<User> findFreeUsers();

    @Query("SELECT u FROM User u WHERE u.subscriptionTier = 'PRO_PROFESSIONAL' AND u.userType = 'PROFESSIONAL' AND u.accountStatus = 'ACTIVE'")
    List<User> findProProfessionalUsers();

    @Query("SELECT u FROM User u WHERE u.subscriptionTier IN ('PLUS', 'PRO', 'PRO_PROFESSIONAL') AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersWithPaidFeatures();

    // ==================== VERIFICATION QUERIES ====================

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.professionalProfile.isVerified = true AND u.accountStatus = 'ACTIVE'")
    List<User> findVerifiedProfessionals();

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.professionalProfile.isVerified = false AND u.subscriptionTier = 'PRO_PROFESSIONAL' AND u.accountStatus = 'ACTIVE'")
    List<User> findUnverifiedProProfessionals();

    // ==================== ACTIVITY & ENGAGEMENT QUERIES ====================

    @Query("SELECT u FROM User u WHERE u.lastActive >= :since AND u.accountStatus = 'ACTIVE'")
    List<User> findActiveUsersSince(@Param("since") LocalDateTime since);

    @Query("SELECT u FROM User u WHERE u.currentStreak >= :minStreak AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersWithWorkoutStreak(@Param("minStreak") Integer minStreak);

    @Query("SELECT u FROM User u WHERE u.accountStatus = 'ACTIVE' ORDER BY u.totalWorkouts DESC")
    Page<User> findMostActiveUsers(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.accountStatus = 'ACTIVE' ORDER BY u.currentStreak DESC")
    Page<User> findUsersOrderByCurrentStreak(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.accountStatus = 'ACTIVE' ORDER BY u.longestStreak DESC")
    Page<User> findUsersOrderByLongestStreak(Pageable pageable);

    // ==================== NOTIFICATION QUERIES ====================

    @Query("SELECT u FROM User u WHERE u.notificationSettings != 'NONE' AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersWithNotificationsEnabled();

    @Query("SELECT u FROM User u WHERE u.notificationSettings = 'ALL' AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersWithAllNotifications();

    @Query("SELECT u FROM User u WHERE u.notificationSettings = 'WORKOUT_ONLY' AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersWithWorkoutNotifications();

    // ==================== PROFESSIONAL BUSINESS QUERIES ====================

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.professionalProfile.acceptsNewClients = true " +
            "AND (u.professionalProfile.maxClients IS NULL OR u.professionalProfile.activeClientsCount < u.professionalProfile.maxClients) " +
            "AND u.subscriptionTier = 'PRO_PROFESSIONAL' AND u.accountStatus = 'ACTIVE'")
    List<User> findAvailableProfessionals();

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.professionalProfile.baseZipcode = :zipcode AND u.accountStatus = 'ACTIVE'")
    List<User> findProfessionalsByBaseZipcode(@Param("zipcode") String zipcode);

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.professionalProfile.offersVirtualSessions = true AND u.accountStatus = 'ACTIVE'")
    List<User> findVirtualProfessionals();

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.professionalProfile.serviceType = :serviceType AND u.accountStatus = 'ACTIVE'")
    List<User> findProfessionalsByServiceType(@Param("serviceType") String serviceType);

    // ==================== ANALYTICS QUERIES ====================

    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.accountStatus = 'ACTIVE'")
    Long countActiveUsersByType(@Param("userType") UserType userType);

    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.accountStatus = :accountStatus")
    Long countUsersByUserTypeAndAccountStatus(@Param("userType") UserType userType, @Param("accountStatus") User.AccountStatus accountStatus);

    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL AND u.accountStatus = 'ACTIVE'")
    Long countActiveProfessionals();

    @Query("SELECT u.subscriptionTier, COUNT(u) FROM User u WHERE u.accountStatus = 'ACTIVE' GROUP BY u.subscriptionTier")
    List<Object[]> countUsersBySubscriptionTier();

    @Query("SELECT DATE(u.createdAt), COUNT(u) FROM User u WHERE u.createdAt >= :since GROUP BY DATE(u.createdAt) ORDER BY DATE(u.createdAt)")
    List<Object[]> getUserRegistrationStats(@Param("since") LocalDateTime since);

    @Query("SELECT u.userType, u.subscriptionTier, COUNT(u) FROM User u WHERE u.accountStatus = 'ACTIVE' GROUP BY u.userType, u.subscriptionTier")
    List<Object[]> getUserTypeSubscriptionBreakdown();

    // ==================== ADVANCED SEARCH QUERIES ====================

    @Query("SELECT u FROM User u WHERE u.accountStatus = 'ACTIVE' " +
            "AND (:userType IS NULL OR u.userType = :userType) " +
            "AND (:subscriptionTier IS NULL OR u.subscriptionTier = :subscriptionTier) " +
            "AND (:searchTerm IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> findUsersWithFilters(
            @Param("userType") UserType userType,
            @Param("subscriptionTier") SubscriptionTier subscriptionTier,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    // ==================== LOCATION-BASED PROFESSIONAL SEARCH ====================

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.professionalProfile.isProfilePublic = true AND u.accountStatus = 'ACTIVE' " +
            "AND (:baseZipcode IS NULL OR u.professionalProfile.baseZipcode = :baseZipcode) " +
            "AND (:serviceType IS NULL OR u.professionalProfile.serviceType = :serviceType) " +
            "AND (:acceptsNewClients IS NULL OR u.professionalProfile.acceptsNewClients = :acceptsNewClients) " +
            "AND (:offersVirtual IS NULL OR u.professionalProfile.offersVirtualSessions = :offersVirtual)")
    Page<User> findProfessionalsWithLocationFilters(
            @Param("baseZipcode") String baseZipcode,
            @Param("serviceType") String serviceType,
            @Param("acceptsNewClients") Boolean acceptsNewClients,
            @Param("offersVirtual") Boolean offersVirtual,
            Pageable pageable
    );

    // ==================== SOCIAL FEATURES QUERIES ====================

    @Query("SELECT u FROM User u WHERE u.privacySettings != 'PRIVATE' AND u.accountStatus = 'ACTIVE' " +
            "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchUsersForConnection(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ==================== RECENT ACTIVITY QUERIES ====================

    @Query("SELECT u FROM User u WHERE u.lastActive >= :since AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersWithRecentActivity(@Param("since") LocalDateTime since);

    @Query("SELECT u FROM User u WHERE u.lastActive >= :cutoffTime AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersActiveWithinMinutes(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.lastActive >= :cutoffTime AND u.accountStatus = 'ACTIVE' " +
            "ORDER BY u.lastActive DESC")
    List<User> findRecentlyActiveProfessionals(@Param("cutoffTime") LocalDateTime cutoffTime);

    // ==================== ADMIN & MAINTENANCE QUERIES ====================

    @Query("SELECT u FROM User u WHERE u.lastActive < :cutoffDate AND u.accountStatus = 'ACTIVE'")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NULL AND u.accountStatus = 'ACTIVE'")
    List<User> findProfessionalUsersWithoutProfile();

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.subscriptionTier != 'PRO_PROFESSIONAL' AND u.accountStatus = 'ACTIVE'")
    List<User> findProfessionalsWithoutProSubscription();

    @Query("SELECT u FROM User u WHERE u.subscriptionTier = 'FREE' AND u.createdAt <= :cutoffDate AND u.accountStatus = 'ACTIVE'")
    List<User> findLongTermFreeUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ==================== FEATURE ACCESS QUERIES ====================

    @Query("SELECT u FROM User u WHERE u.subscriptionTier IN ('PLUS', 'PRO', 'PRO_PROFESSIONAL') AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersWhoCanScheduleWorkouts();

    @Query("SELECT u FROM User u WHERE u.subscriptionTier IN ('PRO', 'PRO_PROFESSIONAL') AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersWhoCanUseAI();

    @Query("SELECT u FROM User u WHERE u.subscriptionTier = 'PRO_PROFESSIONAL' AND u.userType = 'PROFESSIONAL' AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersWhoCanManageClients();

    // ==================== LOCATION ANALYTICS QUERIES ====================

    @Query("SELECT u.state, COUNT(u) FROM User u WHERE u.state IS NOT NULL AND u.accountStatus = 'ACTIVE' GROUP BY u.state ORDER BY COUNT(u) DESC")
    List<Object[]> getUsersByStateStats();

    @Query("SELECT u.city, u.state, COUNT(u) FROM User u WHERE u.city IS NOT NULL AND u.state IS NOT NULL AND u.accountStatus = 'ACTIVE' GROUP BY u.city, u.state ORDER BY COUNT(u) DESC")
    List<Object[]> getUsersByCityStats();

    @Query("SELECT COUNT(u) FROM User u WHERE u.zipcode IS NOT NULL AND u.accountStatus = 'ACTIVE'")
    Long countUsersWithZipcode();

    @Query("SELECT COUNT(u) FROM User u WHERE (u.city IS NOT NULL AND u.state IS NOT NULL) AND u.accountStatus = 'ACTIVE'")
    Long countUsersWithCityState();
}