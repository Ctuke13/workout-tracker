package com.chidituke.workout_tracker.service.user;

import com.chidituke.workout_tracker.dto.request.user.NotificationsPreferencesRequest;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.UserType;
import com.chidituke.workout_tracker.dto.request.auth.RegisterRequest;
import com.chidituke.workout_tracker.dto.request.user.UserUpdateRequest;
import com.chidituke.workout_tracker.dto.request.user.UserSearchRequest;
import com.chidituke.workout_tracker.dto.request.user.NotificationsPreferencesRequest;
import com.chidituke.workout_tracker.dto.response.user.UserProfileResponse;
import com.chidituke.workout_tracker.dto.response.user.UserSearchResponse;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.mapper.user.UserMapper;
import com.chidituke.workout_tracker.exceptions.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserProfileService userProfileService;
    private final UserActivityService userActivityService;
    private final UserAdminService userAdminService;
    private final UserQueryService userQueryService;

    // ==================== PROFANITY FILTER ====================
    private static final Set<String> BLOCKED_WORDS = Set.of(
            // Add offensive words here - keeping list minimal for example
            "fuck", "shit", "ass", "bitch", "damn", "cunt", "dick", "cock",
            "pussy", "whore", "slut", "fag", "nigger", "nigga", "retard",
            "nazi", "hitler", "penis", "vagina", "porn", "sex", "rape",
            "kill", "murder", "suicide", "terrorist", "bomb"
            // Add more as needed
    );

    // ═══════════════════════════════════════════════════════════════════
    // 🔍 BASIC USER OPERATIONS (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    public Optional<User> findByEmail(String email) {
        return userQueryService.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userQueryService.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userQueryService.findById(id);
    }

    public User getUserById(Long id) {
        return userQueryService.getUserById(id); // Use query service
    }

    public User getUserByEmail(String email) {
        return userQueryService.getUserByEmail(email); // Use query service
    }

    /**
     * Get user ID by username (ADDED FOR PERFORMANCE CONTROLLER)
     */
    public Long getUserIdByUsername(String username) {
        return userQueryService.getUserIdByUsername(username); // Use query service
    }

    @Transactional
    public User save(User user) {
        return userProfileService.save(user);
    }

    /**
     * Update user entity (ADDED FOR WEEKLY GOALS)
     */
    @Transactional
    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * Get user by username (ADDED FOR CONVENIENCE)
     */
    public User getUserByUsername(String username) {
        return userQueryService.getUserByUsername(username); // Use query service
    }

    public boolean existsByEmail(String email) {
        return userQueryService.existsByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userQueryService.existsByUsername(username);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 👤 USER REGISTRATION & PROFILE MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    @Transactional
    public User registerUser(RegisterRequest request) {
        return userProfileService.registerUser(request);
    }

    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserUpdateRequest request) {
        return userProfileService.updateUserProfile(userId, request);
    }

    @Transactional
    public void deleteUser(Long userId) {
        userProfileService.deleteUser(userId);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        userProfileService.deactivateUser(userId);
    }

    @Transactional
    public void reactivateUser(Long userId) {
        userProfileService.reactivateUser(userId);
    }

    // ==================== ONBOARDING METHODS ====================

    /**
     * Check if a nickname is available and valid
     */
    public NicknameCheckResult checkNicknameAvailability(String nickname, Long currentUserId) {
        // Null or empty check
        if (nickname == null || nickname.isBlank()) {
            return new NicknameCheckResult(false, "Nickname cannot be empty");
        }

        // Length check
        if (nickname.length() < 3) {
            return new NicknameCheckResult(false, "Nickname must be at least 3 characters");
        }
        if (nickname.length() > 20) {
            return new NicknameCheckResult(false, "Nickname cannot exceed 20 characters");
        }

        // Character validation (alphanumeric + underscore only)
        if (!nickname.matches("^[a-zA-Z0-9_]+$")) {
            return new NicknameCheckResult(false, "Nickname can only contain letters, numbers, and underscores");
        }

        // Profanity check
        String lowerNickname = nickname.toLowerCase();
        for (String blocked : BLOCKED_WORDS) {
            if (lowerNickname.contains(blocked)) {
                return new NicknameCheckResult(false, "Nickname contains inappropriate content");
            }
        }

        // Availability check
        boolean isTaken;
        if (currentUserId != null) {
            // User is updating their nickname - exclude their current one
            isTaken = userRepository.existsByNicknameAndIdNot(nickname, currentUserId);
        } else {
            isTaken = userRepository.existsByNickname(nickname);
        }

        if (isTaken) {
            return new NicknameCheckResult(false, "Nickname is already taken");
        }

        return new NicknameCheckResult(true, "Nickname is available");
    }

    /**
     * Check if a pet name is valid (no profanity)
     */
    public PetNameCheckResult checkPetName(String petName) {
        // Null is okay (optional field)
        if (petName == null || petName.isBlank()) {
            return new PetNameCheckResult(true, "");
        }

        // Length check
        if (petName.length() > 50) {
            return new PetNameCheckResult(false, "Pet name cannot exceed 50 characters");
        }

        // Profanity check
        String lowerPetName = petName.toLowerCase();
        for (String blocked : BLOCKED_WORDS) {
            if (lowerPetName.contains(blocked)) {
                return new PetNameCheckResult(false, "Pet name contains inappropriate content");
            }
        }

        return new PetNameCheckResult(true, "Pet name is valid");
    }

    /**
     * Complete the onboarding process for a user
     */
    @Transactional
    public User completeOnboarding(Long userId, String nickname, String petName) {
        User user = getUserById(userId);

        // Validate nickname if provided
        if (nickname != null && !nickname.isBlank()) {
            NicknameCheckResult nicknameCheck = checkNicknameAvailability(nickname, userId);
            if (!nicknameCheck.isAvailable()) {
                throw new IllegalArgumentException(nicknameCheck.getMessage());
            }
            user.setNickname(nickname);
        }

        // Validate pet name if provided
        if (petName != null && !petName.isBlank()) {
            PetNameCheckResult petNameCheck = checkPetName(petName);
            if (!petNameCheck.isValid()) {
                throw new IllegalArgumentException(petNameCheck.getMessage());
            }
            user.setPetName(petName);
        }

        user.setOnboardingCompleted(true);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Update user's nickname
     */
    @Transactional
    public User updateNickname(Long userId, String nickname) {
        User user = getUserById(userId);

        NicknameCheckResult check = checkNicknameAvailability(nickname, userId);
        if (!check.isAvailable()) {
            throw new IllegalArgumentException(check.getMessage());
        }

        user.setNickname(nickname);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Update user's pet name
     */
    @Transactional
    public User updatePetName(Long userId, String petName) {
        User user = getUserById(userId);

        PetNameCheckResult check = checkPetName(petName);
        if (!check.isValid()) {
            throw new IllegalArgumentException(check.getMessage());
        }

        user.setPetName(petName);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * Change user password
     * Verifies current password and updates to new password
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Validate new password
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("New password cannot be empty");
        }

        if (newPassword.length() < 8) {
            throw new RuntimeException("New password must be at least 8 characters");
        }

        // Don't allow same password
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        // Hash and update password
        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        log.info("Password changed successfully for user: {}", user.getUsername());
    }

// ==================== RESULT CLASSES ====================

    public record NicknameCheckResult(boolean available, String message) {
        public boolean isAvailable() {
            return available;
        }

        public String getMessage() {
            return message;
        }
    }

    public record PetNameCheckResult(boolean valid, String message) {
        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 🔍 USER SEARCH & DISCOVERY (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    public Page<UserSearchResponse> searchUsers(UserSearchRequest request, Pageable pageable) {
        return userQueryService.searchUsers(request, pageable);
    }


    public List<UserSearchResponse> findUsersNearLocation(String location, int radiusMiles, int limit) {
        return userQueryService.findUsersNearLocation(location, radiusMiles, limit);
    }

    public List<UserSearchResponse> findUsersByZipcode(String zipcode, int limit) {
        return userQueryService.findUsersByZipcode(zipcode, limit);
    }

    public List<UserSearchResponse> findUsersByCity(String city, int limit) {
        return userQueryService.findUsersByCity(city, limit);
    }

    public List<UserSearchResponse> findUsersByState(String state, int limit) {
        return userQueryService.findUsersByState(state, limit);
    }

    public List<UserSearchResponse> findUsersByFitnessLevel(User.FitnessLevel fitnessLevel, int limit) {
        return userQueryService.findUsersByFitnessLevel(fitnessLevel, limit);
    }

    public List<UserSearchResponse> findRecentlyActiveUsers(int limit) {
        return userQueryService.findRecentlyActiveUsers(limit);
    }


    // ═══════════════════════════════════════════════════════════════════
    // 🏋️ ACTIVITY & ENGAGEMENT (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    @Transactional
    public void updateLastActive(Long userId) {
        userActivityService.updateLastActive(userId);
    }

    public User.ActivityLevel getUserActivityLevel(Long userId) {
        return userQueryService.getUserActivityLevel(userId);
    }

    public String getActivityStatus(Long userId) {
        return userQueryService.getActivityStatus(userId);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 💼 PROFESSIONAL USER FEATURES (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    public List<UserSearchResponse> findVerifiedProfessionals(String location, int limit) {
        return userQueryService.findVerifiedProfessionals(location, limit);
    }

    public List<UserSearchResponse> findAvailableProfessionals(String location, int limit) {
        return userQueryService.findAvailableProfessionals(location, limit);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 📊 ANALYTICS & INSIGHTS (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    public long getTotalActiveUsers() {
        return userAdminService.getTotalActiveUsers();
    }

    public long getTotalVerifiedProfessionals() {
        return userAdminService.getTotalVerifiedProfessionals();
    }

    public List<User> getTopRatedProfessionals(int limit) {
        return userAdminService.getTopRatedProfessionals(limit);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 🔄 DTO CONVERSION METHODS (PERFORMANCE OPTIMIZED)
    // ═══════════════════════════════════════════════════════════════════

    public UserProfileResponse convertToUserProfileResponse(User user) {
        return userMapper.mapEntityToProfileResponse(user);
    }

    public UserSearchResponse convertToUserSearchResponse(User user) {
        return userMapper.mapEntityToSearchResponse(user);
    }

    // ═══════════════════════════════════════════════════════════════════
    // 🔔 NOTIFICATION PREFERENCES
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Update granular notification preferences for a user.
     * Only non-null fields in the request are applied.
     */
    @Transactional
    public void updateNotificationPreferences(Long userId, NotificationsPreferencesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (request.getNotifPetHealth() != null)
            user.setNotifPetHealth(request.getNotifPetHealth());
        if (request.getNotifStreakReminders() != null)
            user.setNotifStreakReminders(request.getNotifStreakReminders());
        if (request.getNotifAchievements() != null)
            user.setNotifAchievements(request.getNotifAchievements());
        if (request.getNotifRankSeason() != null)
            user.setNotifRankSeason(request.getNotifRankSeason());
        if (request.getNotifWeeklySummary() != null)
            user.setNotifWeeklySummary(request.getNotifWeeklySummary());
        if (request.getNotifSocialLeaderboard() != null)
            user.setNotifSocialLeaderboard(request.getNotifSocialLeaderboard());
        if (request.getNotifReengagement() != null)
            user.setNotifReengagement(request.getNotifReengagement());

        userRepository.save(user);
        log.info("✅ Notification preferences updated for user {}", userId);
    }
}