package com.chidituke.workout_tracker.service.user;

import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.UserType;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for admin-only user operations and analytics.
 * Handles user management, system analytics, and administrative functions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminService {

    private final UserRepository userRepository;
    private final UserQueryService userQueryService;
    private final UserProfileService userProfileService;

    // ==================== ADMIN USER MANAGEMENT ====================

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void forceDeactivateUser(Long userId, String reason) {
        User user = userQueryService.getUserById(userId);
        user.setAccountStatus(User.AccountStatus.SUSPENDED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.warn("Admin force-deactivated user {} (ID: {}) - Reason: {}",
                user.getUsername(), userId, reason);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void forceReactivateUser(Long userId, String reason) {
        User user = userQueryService.getUserById(userId);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Admin force-reactivated user {} (ID: {}) - Reason: {}",
                user.getUsername(), userId, reason);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void promoteUserToProfessional(Long userId) {
        User user = userQueryService.getUserById(userId);
        user.setUserType(UserType.PROFESSIONAL);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("User {} (ID: {}) promoted to PROFESSIONAL", user.getUsername(), userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void demoteUserToRegular(Long userId) {
        User user = userQueryService.getUserById(userId);
        user.setUserType(UserType.REGULAR);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("User {} (ID: {}) demoted to REGULAR", user.getUsername(), userId);
    }

    // ==================== SYSTEM ANALYTICS ====================

    @Cacheable(value = "user-analytics", key = "'total-active'")
    public long getTotalActiveUsers() {
        try {
            return userRepository.countActiveUsersByType(UserType.REGULAR);
        } catch (Exception e) {
            log.error("Error getting total active users: {}", e.getMessage());
            return userRepository.count();
        }
    }

    @Cacheable(value = "user-analytics", key = "'total-professionals'")
    public long getTotalVerifiedProfessionals() {
        try {
            return userRepository.countActiveUsersByType(UserType.PROFESSIONAL);
        } catch (Exception e) {
            log.error("Error getting total professionals: {}", e.getMessage());
            return userRepository.findVerifiedProfessionals().size();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getTopRatedProfessionals(int limit) {
        try {
            return userRepository.findVerifiedProfessionals()
                    .stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting top rated professionals: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "user-analytics", key = "'user-growth-stats'")
    public Map<String, Object> getUserGrowthStats() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime weekAgo = now.minusWeeks(1);
            LocalDateTime monthAgo = now.minusMonths(1);

            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countByAccountStatus(User.AccountStatus.ACTIVE);
            long professionalUsers = userRepository.countActiveUsersByType(UserType.PROFESSIONAL);

            return Map.of(
                    "totalUsers", totalUsers,
                    "activeUsers", activeUsers,
                    "professionalUsers", professionalUsers,
                    "suspendedUsers", userRepository.countByAccountStatus(User.AccountStatus.SUSPENDED),
                    "growthRate", calculateGrowthRate(totalUsers),
                    "professionalPercentage", totalUsers > 0 ? (double) professionalUsers / totalUsers * 100 : 0.0
            );
        } catch (Exception e) {
            log.error("Error generating user growth stats: {}", e.getMessage());
            return Map.of("error", "Unable to generate stats");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getUserActivityStats() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime hourAgo = now.minusHours(1);
            LocalDateTime dayAgo = now.minusDays(1);
            LocalDateTime weekAgo = now.minusWeeks(1);

            List<User> allActiveUsers = userRepository.findByAccountStatus(User.AccountStatus.ACTIVE);

            long activeLastHour = allActiveUsers.stream()
                    .filter(user -> user.getLastActive() != null && user.getLastActive().isAfter(hourAgo))
                    .count();

            long activeLastDay = allActiveUsers.stream()
                    .filter(user -> user.getLastActive() != null && user.getLastActive().isAfter(dayAgo))
                    .count();

            long activeLastWeek = allActiveUsers.stream()
                    .filter(user -> user.getLastActive() != null && user.getLastActive().isAfter(weekAgo))
                    .count();

            return Map.of(
                    "activeLastHour", activeLastHour,
                    "activeLastDay", activeLastDay,
                    "activeLastWeek", activeLastWeek,
                    "totalActive", allActiveUsers.size(),
                    "engagementRate", allActiveUsers.size() > 0 ? (double) activeLastDay / allActiveUsers.size() * 100 : 0.0
            );
        } catch (Exception e) {
            log.error("Error generating user activity stats: {}", e.getMessage());
            return Map.of("error", "Unable to generate activity stats");
        }
    }

    // ==================== BULK OPERATIONS ====================

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void bulkUpdateUserStatus(List<Long> userIds, User.AccountStatus newStatus, String reason) {
        int successCount = 0;
        int failCount = 0;

        for (Long userId : userIds) {
            try {
                User user = userQueryService.getUserById(userId);
                user.setAccountStatus(newStatus);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to update status for user {}: {}", userId, e.getMessage());
                failCount++;
            }
        }

        log.info("Bulk status update completed - Success: {}, Failed: {}, Status: {}, Reason: {}",
                successCount, failCount, newStatus, reason);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void bulkDeleteInactiveUsers(int daysInactive) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
        List<User> inactiveUsers = userRepository.findByAccountStatus(User.AccountStatus.ACTIVE)
                .stream()
                .filter(user -> user.getLastActive() != null && user.getLastActive().isBefore(cutoffDate))
                .collect(Collectors.toList());

        int deletedCount = 0;
        for (User user : inactiveUsers) {
            try {
                userProfileService.deleteUser(user.getId());
                deletedCount++;
            } catch (Exception e) {
                log.error("Failed to delete inactive user {}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("Bulk deleted {} inactive users (inactive for {} days)", deletedCount, daysInactive);
    }

    // ==================== SYSTEM MAINTENANCE ====================

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void cleanupTestAccounts() {
        List<User> testUsers = userRepository.findByEmailContaining("test@");
        testUsers.addAll(userRepository.findByUsernameContaining("test_"));

        int cleanedCount = 0;
        for (User user : testUsers) {
            try {
                if (user.getAccountStatus() != User.AccountStatus.SUSPENDED) {
                    user.setAccountStatus(User.AccountStatus.SUSPENDED);
                    userRepository.save(user);
                    cleanedCount++;
                }
            } catch (Exception e) {
                log.error("Failed to cleanup test user {}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("Cleaned up {} test accounts", cleanedCount);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> generateSystemHealthReport() {
        try {
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countByAccountStatus(User.AccountStatus.ACTIVE);
            long suspendedUsers = userRepository.countByAccountStatus(User.AccountStatus.SUSPENDED);

            List<User> recentlyActive = userRepository.findActiveUsersSince(LocalDateTime.now().minusHours(24));

            return Map.of(
                    "timestamp", LocalDateTime.now().toString(),
                    "totalUsers", totalUsers,
                    "activeUsers", activeUsers,
                    "suspendedUsers", suspendedUsers,
                    "activeIn24Hours", recentlyActive.size(),
                    "systemHealth", activeUsers > 0 ? "HEALTHY" : "WARNING",
                    "recommendations", generateHealthRecommendations(activeUsers, suspendedUsers, totalUsers)
            );
        } catch (Exception e) {
            log.error("Error generating system health report: {}", e.getMessage());
            return Map.of(
                    "timestamp", LocalDateTime.now().toString(),
                    "status", "ERROR",
                    "error", e.getMessage()
            );
        }
    }

    // ==================== HELPER METHODS ====================

    private double calculateGrowthRate(long currentTotal) {
        // Placeholder calculation - would need historical data
        return 5.2; // Mock 5.2% growth rate
    }

    private List<String> generateHealthRecommendations(long activeUsers, long suspendedUsers, long totalUsers) {
        List<String> recommendations = new ArrayList<>();

        if (suspendedUsers > totalUsers * 0.1) {
            recommendations.add("High number of suspended users - review suspension policies");
        }

        if (activeUsers < totalUsers * 0.7) {
            recommendations.add("Low user engagement - consider re-engagement campaigns");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("System health is good - continue monitoring");
        }

        return recommendations;
    }

    // ==================== AUDIT LOGGING ====================

    @PreAuthorize("hasRole('ADMIN')")
    public void logAdminAction(String adminUsername, String action, Long targetUserId, String details) {
        log.info("ADMIN_ACTION: Admin {} performed '{}' on user {} - Details: {}",
                adminUsername, action, targetUserId, details);
        // In a production system, this would write to an audit log table
    }
}