package com.chidituke.workout_tracker.service.user;

import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for user activity tracking and engagement metrics.
 * Handles user activity updates, engagement tracking, and activity-based queries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserActivityService {

    private final UserRepository userRepository;
    private final UserQueryService userQueryService;

    // ==================== ACTIVITY TRACKING ====================

    public void updateLastActive(Long userId) {
        User user = userQueryService.getUserById(userId);
        user.setLastActive(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.debug("Updated last active for user: {} ({})", user.getUsername(), userId);
    }

    public void recordUserEngagement(Long userId, String engagementType) {
        User user = userQueryService.getUserById(userId);
        user.setLastActive(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.debug("Recorded {} engagement for user: {} ({})", engagementType, user.getUsername(), userId);
    }

    public void updateWorkoutStreak(Long userId, int newStreak) {
        User user = userQueryService.getUserById(userId);
        user.setCurrentStreak(newStreak);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Updated workout streak for user {} to {} days", user.getUsername(), newStreak);
    }

    public void incrementWorkoutStreak(Long userId) {
        User user = userQueryService.getUserById(userId);
        Integer currentStreak = user.getCurrentStreak() != null ? user.getCurrentStreak() : 0;
        user.setCurrentStreak(currentStreak + 1);
        user.setLastActive(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Incremented workout streak for user {} to {} days", user.getUsername(), currentStreak + 1);
    }

    public void resetWorkoutStreak(Long userId) {
        User user = userQueryService.getUserById(userId);
        user.setCurrentStreak(0);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("Reset workout streak for user {}", user.getUsername());
    }

    // ==================== ACTIVITY METRICS ====================

    public boolean isUserActive(Long userId) {
        User user = userQueryService.getUserById(userId);
        if (user.getLastActive() == null) return false;

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return user.getLastActive().isAfter(oneHourAgo);
    }

    public boolean isUserActiveToday(Long userId) {
        User user = userQueryService.getUserById(userId);
        if (user.getLastActive() == null) return false;

        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        return user.getLastActive().isAfter(todayStart);
    }

    public LocalDateTime getLastActiveTime(Long userId) {
        User user = userQueryService.getUserById(userId);
        return user.getLastActive();
    }

    public Integer getCurrentWorkoutStreak(Long userId) {
        User user = userQueryService.getUserById(userId);
        return user.getCurrentStreak() != null ? user.getCurrentStreak() : 0;
    }

    // ==================== ENGAGEMENT ANALYTICS ====================

    public void recordLogin(Long userId) {
        updateLastActive(userId);
        log.debug("Recorded login for user: {}", userId);
    }

    public void recordWorkoutCompletion(Long userId) {
        recordUserEngagement(userId, "WORKOUT_COMPLETION");
        incrementWorkoutStreak(userId);
    }

    public void recordExerciseCreation(Long userId) {
        recordUserEngagement(userId, "EXERCISE_CREATION");
    }

    public void recordSocialInteraction(Long userId, String interactionType) {
        recordUserEngagement(userId, "SOCIAL_" + interactionType);
    }

    // ==================== ACTIVITY STATUS ====================

    public String getDetailedActivityStatus(Long userId) {
        User user = userQueryService.getUserById(userId);

        if (user.getLastActive() == null) {
            return "Never Active";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastActive = user.getLastActive();

        if (lastActive.isAfter(now.minusMinutes(5))) {
            return "Online";
        } else if (lastActive.isAfter(now.minusMinutes(30))) {
            return "Recently Active";
        } else if (lastActive.isAfter(now.minusHours(1))) {
            return "Active in Last Hour";
        } else if (lastActive.isAfter(now.minusHours(24))) {
            return "Active Today";
        } else if (lastActive.isAfter(now.minusDays(7))) {
            return "Active This Week";
        } else {
            return "Inactive";
        }
    }

    // ==================== BATCH OPERATIONS ====================

    @Transactional
    public void updateMultipleUsersLastActive(List<Long> userIds) {
        LocalDateTime now = LocalDateTime.now();
        for (Long userId : userIds) {
            try {
                User user = userQueryService.getUserById(userId);
                user.setLastActive(now);
                user.setUpdatedAt(now);
                userRepository.save(user);
            } catch (Exception e) {
                log.warn("Failed to update last active for user {}: {}", userId, e.getMessage());
            }
        }
        log.info("Updated last active timestamp for {} users", userIds.size());
    }

    // ==================== CLEANUP OPERATIONS ====================

    @Transactional
    public void cleanupInactiveUsers(int daysInactive) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
        // Implementation would depend on your business rules
        // This is a placeholder for future inactive user cleanup logic
        log.info("Cleanup operation for users inactive since {}", cutoffDate);
    }
}