package com.chidituke.workout_tracker.controller.user;

import com.chidituke.workout_tracker.dto.request.user.UserUpdateRequest;
import com.chidituke.workout_tracker.dto.request.user.UserSearchRequest;
import com.chidituke.workout_tracker.dto.request.user.PasswordChangeRequest;
import com.chidituke.workout_tracker.dto.request.user.NicknameUpdateRequest;
import com.chidituke.workout_tracker.dto.request.user.PetNameUpdateRequest;
import com.chidituke.workout_tracker.dto.response.user.UserProfileResponse;
import com.chidituke.workout_tracker.dto.response.user.UserSearchResponse;
import com.chidituke.workout_tracker.dto.response.user.UserDataExportResponse;
import com.chidituke.workout_tracker.dto.response.workout_session.WorkoutSessionResponse;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.pet.PetStats;
import com.chidituke.workout_tracker.security.CurrentUser;
import com.chidituke.workout_tracker.security.UserPrincipal;
import com.chidituke.workout_tracker.service.user.UserService;
import com.chidituke.workout_tracker.service.workout.WorkoutSessionService;
import com.chidituke.workout_tracker.service.progress.AchievementService;
import com.chidituke.workout_tracker.service.pet.PetStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for user profile management, discovery, and social features")
public class UserController {

    private final UserService userService;
    private final WorkoutSessionService workoutSessionService;
    private final AchievementService achievementService;
    private final PetStatsService petStatsService;

    // ==================== PROFILE MANAGEMENT ====================

    /**
     * Get current user's profile
     */
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Get detailed profile information for the authenticated user")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(@CurrentUser UserPrincipal currentUser) {
        User user = userService.getUserById(currentUser.getId());
        UserProfileResponse profile = userService.convertToUserProfileResponse(user);
        return ResponseEntity.ok(profile);
    }

    /**
     * Get user profile by ID (public information only)
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user profile by ID", description = "Get public profile information for a specific user")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @CurrentUser UserPrincipal currentUser) {

        User user = userService.getUserById(userId);

        // Check privacy settings
        if (user.getPrivacySettings() == User.PrivacySettings.PRIVATE &&
                (currentUser == null || !currentUser.getId().equals(userId))) {
            return ResponseEntity.notFound().build();
        }

        UserProfileResponse profile = userService.convertToUserProfileResponse(user);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update current user's profile
     */
    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Update profile information for the authenticated user")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @Valid @RequestBody UserUpdateRequest request,
            @CurrentUser UserPrincipal currentUser) {

        UserProfileResponse updated = userService.updateUserProfile(currentUser.getId(), request);
        return ResponseEntity.ok(updated);
    }

    // ==================== USER DISCOVERY & SEARCH ====================

    /**
     * Search users with filters
     */
    @PostMapping("/search")
    @Operation(summary = "Search users", description = "Search users with various filters and criteria")
    public ResponseEntity<Page<UserSearchResponse>> searchUsers(
            @Valid @RequestBody UserSearchRequest searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "firstName") String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<UserSearchResponse> results = userService.searchUsers(searchRequest, pageable);
        return ResponseEntity.ok(results);
    }

    /**
     * Find users near a location
     */
    @GetMapping("/near")
    @Operation(summary = "Find users near location", description = "Find users within a specified radius of a location")
    public ResponseEntity<List<UserSearchResponse>> findUsersNearLocation(
            @RequestParam String location,
            @RequestParam(defaultValue = "25") int radiusMiles,
            @RequestParam(defaultValue = "20") int limit) {

        List<UserSearchResponse> users = userService.findUsersNearLocation(location, radiusMiles, limit);
        return ResponseEntity.ok(users);
    }

    /**
     * Find users by specific zipcode
     */
    @GetMapping("/zipcode/{zipcode}")
    @Operation(summary = "Find users by zipcode", description = "Find users in a specific zipcode area")
    public ResponseEntity<List<UserSearchResponse>> findUsersByZipcode(
            @PathVariable String zipcode,
            @RequestParam(defaultValue = "20") int limit) {

        List<UserSearchResponse> users = userService.findUsersByZipcode(zipcode, limit);
        return ResponseEntity.ok(users);
    }

    /**
     * Find users by city
     */
    @GetMapping("/city/{city}")
    @Operation(summary = "Find users by city", description = "Find users in a specific city")
    public ResponseEntity<List<UserSearchResponse>> findUsersByCity(
            @PathVariable String city,
            @RequestParam(defaultValue = "20") int limit) {

        List<UserSearchResponse> users = userService.findUsersByCity(city, limit);
        return ResponseEntity.ok(users);
    }

    /**
     * Find users by state
     */
    @GetMapping("/state/{state}")
    @Operation(summary = "Find users by state", description = "Find users in a specific state")
    public ResponseEntity<List<UserSearchResponse>> findUsersByState(
            @PathVariable String state,
            @RequestParam(defaultValue = "20") int limit) {

        List<UserSearchResponse> users = userService.findUsersByState(state, limit);
        return ResponseEntity.ok(users);
    }

    /**
     * Find users by fitness level
     */
    @GetMapping("/fitness-level/{fitnessLevel}")
    @Operation(summary = "Find users by fitness level", description = "Find users with a specific fitness level")
    public ResponseEntity<List<UserSearchResponse>> findUsersByFitnessLevel(
            @PathVariable User.FitnessLevel fitnessLevel,
            @RequestParam(defaultValue = "20") int limit) {

        List<UserSearchResponse> users = userService.findUsersByFitnessLevel(fitnessLevel, limit);
        return ResponseEntity.ok(users);
    }

    /**
     * Find recently active users
     */
    @GetMapping("/active")
    @Operation(summary = "Find recently active users", description = "Find users who have been active recently")
    public ResponseEntity<List<UserSearchResponse>> findRecentlyActiveUsers(
            @RequestParam(defaultValue = "20") int limit) {

        List<UserSearchResponse> users = userService.findRecentlyActiveUsers(limit);
        return ResponseEntity.ok(users);
    }

    // ==================== PROFESSIONAL USER FEATURES ====================

    /**
     * Find verified professionals
     */
    @GetMapping("/professionals")
    @Operation(summary = "Find professionals", description = "Find verified fitness professionals")
    public ResponseEntity<List<UserSearchResponse>> findVerifiedProfessionals(
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "20") int limit) {

        List<UserSearchResponse> professionals = userService.findVerifiedProfessionals(location, limit);
        return ResponseEntity.ok(professionals);
    }

    /**
     * Find available professionals
     */
    @GetMapping("/professionals/available")
    @Operation(summary = "Find available professionals", description = "Find professionals currently accepting new clients")
    public ResponseEntity<List<UserSearchResponse>> findAvailableProfessionals(
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "20") int limit) {

        List<UserSearchResponse> professionals = userService.findAvailableProfessionals(location, limit);
        return ResponseEntity.ok(professionals);
    }

    // ==================== ACTIVITY & STATUS ====================

    /**
     * Get user activity status
     */
    @GetMapping("/{userId}/activity-status")
    @Operation(summary = "Get user activity status", description = "Get the current activity status of a user")
    public ResponseEntity<String> getUserActivityStatus(@PathVariable Long userId) {
        String status = userService.getActivityStatus(userId);
        return ResponseEntity.ok(status);
    }

    /**
     * Update current user's last active timestamp
     */
    @PostMapping("/activity/ping")
    @Operation(summary = "Update activity", description = "Update the user's last active timestamp")
    public ResponseEntity<Void> updateActivity(@CurrentUser UserPrincipal currentUser) {
        userService.updateLastActive(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Get total active users (admin only)
     */
    @GetMapping("/admin/stats/total-active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get total active users", description = "Get the total number of active users (admin only)")
    public ResponseEntity<Long> getTotalActiveUsers() {
        long total = userService.getTotalActiveUsers();
        return ResponseEntity.ok(total);
    }

    /**
     * Get total verified professionals (admin only)
     */
    @GetMapping("/admin/stats/total-professionals")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get total professionals", description = "Get the total number of verified professionals (admin only)")
    public ResponseEntity<Long> getTotalVerifiedProfessionals() {
        long total = userService.getTotalVerifiedProfessionals();
        return ResponseEntity.ok(total);
    }

    /**
     * Deactivate user account (admin only)
     */
    @PostMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate user", description = "Deactivate a user account (admin only)")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Reactivate user account (admin only)
     */
    @PostMapping("/{userId}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reactivate user", description = "Reactivate a user account (admin only)")
    public ResponseEntity<Void> reactivateUser(@PathVariable Long userId) {
        userService.reactivateUser(userId);
        return ResponseEntity.ok().build();
    }

    // ==================== ACCOUNT MANAGEMENT ====================

    /**
     * Delete current user's account
     */
    @DeleteMapping("/profile")
    @Operation(summary = "Delete account", description = "Delete the authenticated user's account")
    public ResponseEntity<Void> deleteAccount(@CurrentUser UserPrincipal currentUser) {
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Change password
     */
    @PutMapping("/password")
    @Operation(summary = "Change password", description = "Change the authenticated user's password")
    public ResponseEntity<?> changePassword(
            @RequestBody @Valid PasswordChangeRequest request,
            @CurrentUser UserPrincipal currentUser) {

        try {
            userService.changePassword(
                    currentUser.getId(),
                    request.getCurrentPassword(),
                    request.getNewPassword()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Password changed successfully"
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Update nickname
     */
    @PutMapping("/nickname")
    @Operation(summary = "Update nickname", description = "Update the authenticated user's nickname")
    public ResponseEntity<UserProfileResponse> updateNickname(
            @RequestBody @Valid NicknameUpdateRequest request,
            @CurrentUser UserPrincipal currentUser) {

        User updated = userService.updateNickname(currentUser.getId(), request.getNickname());
        UserProfileResponse profile = userService.convertToUserProfileResponse(updated);

        return ResponseEntity.ok(profile);
    }

    /**
     * Update pet name
     */
    @PutMapping("/pet-name")
    @Operation(summary = "Update pet name", description = "Update the authenticated user's pet name")
    public ResponseEntity<UserProfileResponse> updatePetName(
            @RequestBody @Valid PetNameUpdateRequest request,
            @CurrentUser UserPrincipal currentUser) {

        User updated = userService.updatePetName(currentUser.getId(), request.getPetName());
        UserProfileResponse profile = userService.convertToUserProfileResponse(updated);

        return ResponseEntity.ok(profile);
    }

    /**
     * Export user data - COMPLETE VERSION with Workout History & Achievements
     */
    @GetMapping("/export")
    @Operation(summary = "Export user data", description = "Export all user data including workouts and achievements")
    public ResponseEntity<Map<String, Object>> exportUserData(@CurrentUser UserPrincipal currentUser) {

        User user = userService.getUserById(currentUser.getId());

        // Build comprehensive export
        Map<String, Object> exportData = new LinkedHashMap<>();

        // ==================== PROFILE INFORMATION ====================
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("userId", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("nickname", user.getNickname());
        profile.put("dateOfBirth", user.getDateOfBirth());
        profile.put("gender", user.getGender());
        profile.put("zipcode", user.getZipcode());
        profile.put("city", user.getCity());
        profile.put("state", user.getState());
        profile.put("country", user.getCountry());
        profile.put("phoneNumber", user.getPhoneNumber());
        profile.put("bio", user.getBio());
        exportData.put("profile", profile);

        // ==================== ACCOUNT INFORMATION ====================
        Map<String, Object> account = new LinkedHashMap<>();
        account.put("userType", user.getUserType());
        account.put("subscriptionTier", user.getSubscriptionTier());
        account.put("accountStatus", user.getAccountStatus());
        account.put("createdAt", user.getCreatedAt());
        account.put("lastActive", user.getLastActive());
        account.put("isProfessional", user.isProfessional());
        exportData.put("account", account);

        // ==================== PET INFORMATION ====================
        Map<String, Object> pet = new LinkedHashMap<>();

        try {
            PetStats petStats = petStatsService.getPetStats(user.getId());

            // Basic pet info
            pet.put("petName", petStats.getPetName());
            pet.put("petType", petStats.getPetType());
            pet.put("petColor", petStats.getPetColor());

            // Progression
            pet.put("xp", petStats.getXp());
            pet.put("level", petStats.getLevel());
            pet.put("evolutionStage", petStats.getEvolutionStage());
            pet.put("workoutsCompleted", petStats.getWorkoutsCompleted());

            // Economy
            pet.put("crystals", petStats.getCrystals());

            // Core stats
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("fuel", petStats.getFuel());
            stats.put("fuelStatus", petStats.getFuelStatus());
            stats.put("motivation", petStats.getMotivation());
            stats.put("motivationStatus", petStats.getMotivationStatus());
            stats.put("fatigue", petStats.getFatigue());
            stats.put("fatigueStatus", petStats.getFatigueStatus());
            stats.put("cleanliness", petStats.getCleanliness());
            stats.put("cleanlinessStatus", petStats.getCleanlinessStatus());
            pet.put("stats", stats);

            // Status
            Map<String, Object> status = new LinkedHashMap<>();
            status.put("isSleeping", petStats.getIsSleeping());
            status.put("isNeglected", petStats.getIsNeglected());
            status.put("canInteract", petStats.canInteract());
            status.put("canMotivate", petStats.canMotivate());
            status.put("canBathe", petStats.canBathe());
            status.put("canFeed", petStats.canFeed());
            pet.put("status", status);

            // Home
            pet.put("selectedHome", petStats.getSelectedHome());

            // Timestamps
            pet.put("lastWorkoutTime", petStats.getLastWorkoutTime());
            pet.put("lastFedTime", petStats.getLastFedTime());
            pet.put("createdAt", petStats.getCreatedAt());

        } catch (Exception e) {
            log.warn("Could not load pet stats for export: {}", e.getMessage());
            pet.put("petName", user.getPetName());
            pet.put("note", "Pet stats unavailable");
        }

        exportData.put("pet", pet);

        // ==================== WORKOUT STATISTICS ====================
        Map<String, Object> workoutStats = new LinkedHashMap<>();
        workoutStats.put("totalWorkouts", user.getTotalWorkouts());
        workoutStats.put("currentStreak", user.getCurrentStreak());
        workoutStats.put("longestStreak", user.getLongestStreak());
        workoutStats.put("weeklyWorkoutGoal", user.getWeeklyWorkoutGoal());
        workoutStats.put("goalType", user.getGoalType());
        exportData.put("workoutStatistics", workoutStats);

        // ==================== FITNESS INFORMATION ====================
        Map<String, Object> fitness = new LinkedHashMap<>();
        fitness.put("fitnessLevel", user.getFitnessLevel());
        fitness.put("fitnessGoals", user.getFitnessGoals());
        fitness.put("workoutFrequency", user.getWorkoutFrequency());
        fitness.put("activityLevel", user.getActivityLevel());
        fitness.put("heightCm", user.getHeightCm());
        fitness.put("weightKg", user.getWeightKg());
        exportData.put("fitnessProfile", fitness);

        // ==================== PREFERENCES ====================
        Map<String, Object> preferences = new LinkedHashMap<>();
        preferences.put("measurementSystem", user.getMeasurementSystem());
        preferences.put("preferredWeightUnit", user.getPreferredWeightUnit());
        preferences.put("preferredDistanceUnit", user.getPreferredDistanceUnit());
        preferences.put("notificationSettings", user.getNotificationSettings());
        preferences.put("privacySettings", user.getPrivacySettings());
        exportData.put("preferences", preferences);

        // ==================== WORKOUT HISTORY ====================
        try {
            List<WorkoutSessionResponse> workoutSessions = workoutSessionService.getUserWorkoutHistory(user.getUsername());

            List<Map<String, Object>> workouts = new ArrayList<>();
            for (WorkoutSessionResponse session : workoutSessions) {
                Map<String, Object> workout = new LinkedHashMap<>();
                workout.put("sessionId", session.getId());
                workout.put("date", session.getDate());
                workout.put("workoutName", session.getWorkoutPlanName());
                workout.put("duration", session.getTotalDurationMinutes());
                workout.put("totalSets", session.getTotalSetsCompleted());
                workout.put("totalExercises", session.getTotalExercisesCompleted());
                workout.put("notes", session.getNotes());
                workout.put("completed", session.getIsCompleted());
                workout.put("completionPercentage", session.getCompletionPercentage());
                workout.put("estimatedCalories", session.getEstimatedCalories());

                workouts.add(workout);
            }

            exportData.put("workoutHistory", workouts);
            exportData.put("totalWorkoutsInHistory", workouts.size());

        } catch (Exception e) {
            log.warn("Could not load workout history for export: {}", e.getMessage());
            exportData.put("workoutHistory", List.of());
            exportData.put("workoutHistoryNote", "Workout history unavailable");
        }

        // ==================== ACHIEVEMENTS ====================
        try {
            List<?> userAchievements = achievementService.getUserAchievements(user.getId());

            List<Map<String, Object>> achievements = new ArrayList<>();
            for (Object achievement : userAchievements) {
                Map<String, Object> ach = new LinkedHashMap<>();
                // Map achievement data - adjust based on your Achievement model
                ach.put("achievement", achievement.toString());
                achievements.add(ach);
            }

            exportData.put("achievements", achievements);
            exportData.put("totalAchievements", achievements.size());

        } catch (Exception e) {
            log.warn("Could not load achievements for export: {}", e.getMessage());
            exportData.put("achievements", List.of());
            exportData.put("achievementsNote", "Achievements unavailable");
        }

        // ==================== EXPORT METADATA ====================
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("exportedAt", LocalDateTime.now().toString());
        metadata.put("exportFormat", "JSON");
        metadata.put("exportVersion", "2.0");
        metadata.put("dataIncluded", List.of(
                "profile",
                "account",
                "pet",
                "workoutStatistics",
                "fitnessProfile",
                "preferences",
                "workoutHistory",
                "achievements"
        ));
        exportData.put("exportMetadata", metadata);

        return ResponseEntity.ok(exportData);
    }

    // ==================== TUTORIAL ENDPOINTS ====================

    /**
     * Mark pet tutorial as completed
     */
    @PutMapping("/tutorial/pet/complete")
    @Operation(summary = "Mark pet tutorial as completed",
            description = "Marks the pet tutorial as completed for the current user")
    public ResponseEntity<Map<String, Object>> completePetTutorial(@CurrentUser UserPrincipal currentUser) {
        User user = userService.getUserById(currentUser.getId());
        user.setPetTutorialCompleted(true);
        userService.updateUser(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Pet tutorial completed"
        ));
    }

    /**
     * Restart pet tutorial (set to false so it shows again)
     */
    @PutMapping("/tutorial/pet/restart")
    @Operation(summary = "Restart pet tutorial",
            description = "Allows user to replay the pet tutorial by resetting completion status")
    public ResponseEntity<Map<String, Object>> restartPetTutorial(@CurrentUser UserPrincipal currentUser) {
        User user = userService.getUserById(currentUser.getId());
        user.setPetTutorialCompleted(false);
        userService.updateUser(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Pet tutorial will show again"
        ));
    }

    /**
     * Mark calendar tutorial as completed
     */
    @PutMapping("/tutorial/calendar/complete")
    @Operation(summary = "Mark calendar tutorial as completed",
            description = "Marks the calendar tutorial as completed for the current user")
    public ResponseEntity<Map<String, Object>> completeCalendarTutorial(@CurrentUser UserPrincipal currentUser) {
        User user = userService.getUserById(currentUser.getId());
        user.setCalendarTutorialCompleted(true);
        userService.updateUser(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Calendar tutorial completed"
        ));
    }

    /**
     * Restart calendar tutorial (set to false so it shows again)
     */
    @PutMapping("/tutorial/calendar/restart")
    @Operation(summary = "Restart calendar tutorial",
            description = "Allows user to replay the calendar tutorial by resetting completion status")
    public ResponseEntity<Map<String, Object>> restartCalendarTutorial(@CurrentUser UserPrincipal currentUser) {
        User user = userService.getUserById(currentUser.getId());
        user.setCalendarTutorialCompleted(false);
        userService.updateUser(user);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Calendar tutorial will show again"
        ));
    }

    // ==================== TEST ENDPOINTS ====================

    /**
     * Test endpoint for user controller
     */
    @GetMapping("/test")
    @Operation(summary = "Test endpoint", description = "Test if the user controller is working")
    public ResponseEntity<String> testUserController() {
        return ResponseEntity.ok("User controller is working!");
    }
}