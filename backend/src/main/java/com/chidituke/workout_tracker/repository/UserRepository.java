package com.chidituke.workout_tracker.repository;

import com.chidituke.workout_tracker.model.User;
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

    // 🔐 AUTHENTICATION & BASIC QUERIES
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // 👤 USER TYPE QUERIES
    List<User> findByUserType(User.UserType userType);

    @Query("SELECT u FROM User u WHERE u.userType = :userType AND u.accountStatus = 'ACTIVE'")
    List<User> findActiveUsersByType(@Param("userType") User.UserType userType);

    // 🌍 LOCATION-BASED QUERIES
    List<User> findByZipcodeAndUserType(String zipcode, User.UserType userType);

    List<User> findByLocationCityAndLocationStateAndUserType(
            String city, String state, User.UserType userType);

    @Query("SELECT u FROM User u WHERE u.zipcode IN :zipcodes AND u.userType = :userType AND u.accountStatus = 'ACTIVE'")
    List<User> findByZipcodesAndUserType(@Param("zipcodes") List<String> zipcodes, @Param("userType") User.UserType userType);

    // 💼 PROFESSIONAL USER QUERIES
    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL AND u.accountStatus = 'ACTIVE'")
    List<User> findActiveProfessionals();

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.zipcode = :zipcode AND u.accountStatus = 'ACTIVE'")
    List<User> findProfessionalsByZipcode(@Param("zipcode") String zipcode);

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.zipcode IN :zipcodes AND u.accountStatus = 'ACTIVE'")
    List<User> findProfessionalsByZipcodes(@Param("zipcodes") List<String> zipcodes);

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.allowProfessionalRequests = true AND u.accountStatus = 'ACTIVE'")
    List<User> findProfessionalsAcceptingRequests();

    // 🎯 DIRECTORY & DISCOVERY QUERIES
    @Query("SELECT u FROM User u WHERE u.profileVisibility = 'PUBLIC' AND u.showInDirectory = true AND u.accountStatus = 'ACTIVE'")
    Page<User> findPublicUsers(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.profileVisibility = 'PUBLIC' AND u.showInDirectory = true AND u.accountStatus = 'ACTIVE'")
    Page<User> findPublicProfessionals(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.profileVisibility = 'PUBLIC' AND u.showInDirectory = true AND u.accountStatus = 'ACTIVE' " +
            "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchPublicProfessionals(@Param("searchTerm") String searchTerm, Pageable pageable);

    // 📊 SUBSCRIPTION-BASED QUERIES
    @Query("SELECT u FROM User u JOIN u.subscriptions s WHERE s.status = 'ACTIVE' AND s.planType = :planType")
    List<User> findUsersByActivePlan(@Param("planType") String planType);

    @Query("SELECT u FROM User u WHERE EXISTS (SELECT s FROM u.subscriptions s WHERE s.status = 'ACTIVE' AND s.planType IN ('PLUS', 'PRO'))")
    List<User> findPaidSubscribers();

    @Query("SELECT u FROM User u WHERE NOT EXISTS (SELECT s FROM u.subscriptions s WHERE s.status = 'ACTIVE' AND s.planType != 'FREE')")
    List<User> findFreeUsers();

    // ✅ VERIFICATION QUERIES
    List<User> findByIsVerifiedAndUserType(Boolean isVerified, User.UserType userType);

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.professionalProfile.isVerified = true AND u.accountStatus = 'ACTIVE'")
    List<User> findVerifiedProfessionals();

    // 🏃‍♂️ ACTIVITY & ENGAGEMENT QUERIES
    @Query("SELECT u FROM User u WHERE u.lastActive >= :since AND u.accountStatus = 'ACTIVE'")
    List<User> findActiveUsersSince(@Param("since") LocalDateTime since);

    @Query("SELECT u FROM User u WHERE u.lastWorkoutDate >= :since AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersWithRecentWorkouts(@Param("since") LocalDateTime since);

    @Query("SELECT u FROM User u WHERE u.currentStreakDays >= :minStreak AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersWithWorkoutStreak(@Param("minStreak") Integer minStreak);

    @Query("SELECT u FROM User u ORDER BY u.totalWorkoutsCompleted DESC")
    Page<User> findMostActiveUsers(Pageable pageable);

    // 🔔 NOTIFICATION & PRIVACY QUERIES
    List<User> findByEmailNotificationsAndAccountStatus(Boolean emailNotifications, String accountStatus);

    List<User> findByAllowProfessionalRequestsAndAccountStatus(Boolean allowRequests, String accountStatus);

    @Query("SELECT u FROM User u WHERE u.allowWorkoutInvitations = true AND u.accountStatus = 'ACTIVE' AND u.zipcode = :zipcode")
    List<User> findUsersAcceptingInvitationsByZipcode(@Param("zipcode") String zipcode);

    // 📈 ANALYTICS QUERIES
    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = :userType AND u.accountStatus = 'ACTIVE'")
    Long countActiveUsersByType(@Param("userType") User.UserType userType);

    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL AND u.accountStatus = 'ACTIVE'")
    Long countActiveProfessionals();

    @Query("SELECT u.locationState, COUNT(u) FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.accountStatus = 'ACTIVE' GROUP BY u.locationState")
    List<Object[]> countProfessionalsByState();

    @Query("SELECT DATE(u.createdAt), COUNT(u) FROM User u WHERE u.createdAt >= :since GROUP BY DATE(u.createdAt) ORDER BY DATE(u.createdAt)")
    List<Object[]> getUserRegistrationStats(@Param("since") LocalDateTime since);

    // 🔍 ADVANCED SEARCH QUERIES
    @Query("SELECT u FROM User u WHERE u.accountStatus = 'ACTIVE' " +
            "AND (:userType IS NULL OR u.userType = :userType) " +
            "AND (:zipcode IS NULL OR u.zipcode = :zipcode) " +
            "AND (:city IS NULL OR LOWER(u.locationCity) = LOWER(:city)) " +
            "AND (:state IS NULL OR LOWER(u.locationState) = LOWER(:state)) " +
            "AND (:isVerified IS NULL OR u.isVerified = :isVerified)")
    Page<User> findUsersWithFilters(
            @Param("userType") User.UserType userType,
            @Param("zipcode") String zipcode,
            @Param("city") String city,
            @Param("state") String state,
            @Param("isVerified") Boolean isVerified,
            Pageable pageable
    );

    // 👥 SOCIAL FEATURES QUERIES
    @Query("SELECT u FROM User u WHERE u.profileVisibility != 'PRIVATE' AND u.accountStatus = 'ACTIVE' " +
            "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchUsersForFriendConnection(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.allowFriendRequests = true AND u.accountStatus = 'ACTIVE' " +
            "AND u.zipcode = :zipcode AND u.id != :excludeUserId")
    List<User> findPotentialLocalConnections(@Param("zipcode") String zipcode, @Param("excludeUserId") Long excludeUserId);

    // 🏅 LEADERBOARD QUERIES
    @Query("SELECT u FROM User u WHERE u.accountStatus = 'ACTIVE' ORDER BY u.currentStreakDays DESC")
    Page<User> findUsersOrderByCurrentStreak(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.accountStatus = 'ACTIVE' ORDER BY u.longestStreakDays DESC")
    Page<User> findUsersOrderByLongestStreak(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.accountStatus = 'ACTIVE' AND u.totalWorkoutsCompleted > 0 ORDER BY u.totalWorkoutsCompleted DESC")
    Page<User> findUsersOrderByTotalWorkouts(Pageable pageable);

    // 🔧 ADMIN & MAINTENANCE QUERIES
    @Query("SELECT u FROM User u WHERE u.lastActive < :cutoffDate AND u.accountStatus = 'ACTIVE'")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NULL")
    List<User> findProfessionalUsersWithoutProfile();

    @Query("SELECT u FROM User u WHERE u.zipcode IS NULL AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersWithoutLocation();

    // 🕐 ACTIVITY STATUS QUERIES (New)
    @Query("SELECT u FROM User u WHERE u.showActivityStatus = true AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersWithPublicActivityStatus();

    @Query("SELECT u FROM User u WHERE u.showActivityStatus = false AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersWithHiddenActivityStatus();

    // Users active within specific time windows
    @Query("SELECT u FROM User u WHERE u.lastActive >= :cutoffTime AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersActiveWithinMinutes(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT u FROM User u WHERE u.lastActive >= :yesterday AND u.lastActive < :today AND u.accountStatus = 'ACTIVE'")
    List<User> findUsersActiveYesterday(@Param("yesterday") LocalDateTime yesterday, @Param("today") LocalDateTime today);

    // Professional activity queries for ranking
    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.lastActive >= :cutoffTime AND u.accountStatus = 'ACTIVE' " +
            "ORDER BY u.lastActive DESC")
    List<User> findRecentlyActiveProfessionals(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.allowProfessionalRequests = true AND u.lastActive >= :cutoffTime " +
            "AND u.zipcode = :zipcode AND u.accountStatus = 'ACTIVE' " +
            "ORDER BY u.lastActive DESC")
    List<User> findRecentlyActiveProfessionalsByLocation(@Param("zipcode") String zipcode, @Param("cutoffTime") LocalDateTime cutoffTime);

    // Find professionals who are both accepting clients AND recently active
    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.allowProfessionalRequests = true AND u.accountStatus = 'ACTIVE' " +
            "AND u.professionalProfile.acceptsNewClients = true " +
            "AND (u.professionalProfile.maxClients IS NULL OR u.professionalProfile.activeClientsCount < u.professionalProfile.maxClients) " +
            "AND u.lastActive >= :cutoffTime " +
            "ORDER BY u.lastActive DESC")
    List<User> findActivelyAcceptingProfessionals(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find professionals available RIGHT NOW (within 5 minutes)
    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.allowProfessionalRequests = true AND u.accountStatus = 'ACTIVE' " +
            "AND u.professionalProfile.acceptsNewClients = true " +
            "AND u.lastActive >= :immediateTime " +
            "AND (:zipcode IS NULL OR u.zipcode = :zipcode OR u.professionalProfile.offersVirtualSessions = true) " +
            "ORDER BY u.lastActive DESC")
    List<User> findImmediatelyAvailableProfessionals(@Param("zipcode") String zipcode, @Param("immediateTime") LocalDateTime immediateTime);

    // Professionals ranked by activity and rating combined
    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.profileVisibility = 'PUBLIC' AND u.showInDirectory = true AND u.accountStatus = 'ACTIVE' " +
            "AND (:zipcode IS NULL OR u.zipcode = :zipcode) " +
            "ORDER BY " +
            "CASE WHEN u.lastActive >= :recentCutoff THEN 1 ELSE 0 END DESC, " +
            "u.professionalProfile.averageRating DESC, " +
            "u.professionalProfile.totalReviews DESC, " +
            "u.lastActive DESC")
    Page<User> findTopRankedProfessionalsByActivity(@Param("zipcode") String zipcode,
                                                    @Param("recentCutoff") LocalDateTime recentCutoff,
                                                    Pageable pageable);

    // Location-based version
    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.allowProfessionalRequests = true AND u.accountStatus = 'ACTIVE' " +
            "AND u.professionalProfile.acceptsNewClients = true " +
            "AND (u.professionalProfile.maxClients IS NULL OR u.professionalProfile.activeClientsCount < u.professionalProfile.maxClients) " +
            "AND u.lastActive >= :cutoffTime AND u.zipcode = :zipcode " +
            "ORDER BY u.lastActive DESC")
    List<User> findActivelyAcceptingProfessionalsByLocation(@Param("zipcode") String zipcode, @Param("cutoffTime") LocalDateTime cutoffTime);

    // Activity-based user recommendations
    @Query("SELECT u FROM User u WHERE u.allowFriendRequests = true AND u.accountStatus = 'ACTIVE' " +
            "AND u.zipcode = :zipcode AND u.id != :excludeUserId " +
            "AND u.lastActive >= :cutoffTime " +
            "ORDER BY u.lastActive DESC")
    List<User> findActiveLocalConnectionsByActivity(@Param("zipcode") String zipcode,
                                                    @Param("excludeUserId") Long excludeUserId,
                                                    @Param("cutoffTime") LocalDateTime cutoffTime);

    // For business intelligence - which professionals are most responsive
    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.accountStatus = 'ACTIVE' " +
            "AND u.lastActive >= :cutoffTime " +
            "ORDER BY u.professionalProfile.responseTimeHours ASC, u.lastActive DESC")
    List<User> findMostResponsiveProfessionals(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find professionals similar to user's previous choices + activity
    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.accountStatus = 'ACTIVE' AND u.profileVisibility = 'PUBLIC' " +
            "AND u.lastActive >= :cutoffTime " +
            "AND u.professionalProfile.serviceType = :preferredServiceType " +
            "AND u.zipcode IN :preferredZipcodes " +
            "ORDER BY u.lastActive DESC, u.professionalProfile.averageRating DESC")
    List<User> findRecommendedActiveProfessionals(@Param("preferredServiceType") String serviceType,
                                                  @Param("preferredZipcodes") List<String> zipcodes,
                                                  @Param("cutoffTime") LocalDateTime cutoffTime);

    // Count professionals by response tier
    @Query("SELECT " +
            "CASE " +
            "  WHEN u.lastActive >= :onlineTime THEN 'ONLINE_NOW' " +
            "  WHEN u.lastActive >= :activeToday THEN 'ACTIVE_TODAY' " +
            "  WHEN u.lastActive >= :activeWeek THEN 'ACTIVE_WEEK' " +
            "  ELSE 'INACTIVE' " +
            "END as activityTier, " +
            "COUNT(u) " +
            "FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.accountStatus = 'ACTIVE' " +
            "GROUP BY " +
            "CASE " +
            "  WHEN u.lastActive >= :onlineTime THEN 'ONLINE_NOW' " +
            "  WHEN u.lastActive >= :activeToday THEN 'ACTIVE_TODAY' " +
            "  WHEN u.lastActive >= :activeWeek THEN 'ACTIVE_WEEK' " +
            "  ELSE 'INACTIVE' " +
            "END")
    List<Object[]> getProfessionalActivityDistribution(@Param("onlineTime") LocalDateTime onlineTime,
                                                       @Param("activeToday") LocalDateTime activeToday,
                                                       @Param("activeWeek") LocalDateTime activeWeek);

    // Admin queries for activity monitoring
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastActive >= :since AND u.accountStatus = 'ACTIVE'")
    Long countActiveUsersSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(u) FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.lastActive >= :since AND u.accountStatus = 'ACTIVE'")
    Long countActiveProfessionalsSince(@Param("since") LocalDateTime since);

    // Privacy-aware professional discovery
    @Query("SELECT u FROM User u WHERE u.userType = 'PROFESSIONAL' AND u.professionalProfile IS NOT NULL " +
            "AND u.profileVisibility = 'PUBLIC' AND u.showInDirectory = true " +
            "AND u.allowProfessionalRequests = true AND u.accountStatus = 'ACTIVE' " +
            "AND (:showActivityStatus IS NULL OR u.showActivityStatus = :showActivityStatus)")
    Page<User> findPublicProfessionalsWithActivityPreference(@Param("showActivityStatus") Boolean showActivityStatus, Pageable pageable);
}