package com.chidituke.workout_tracker.controller.user;

import com.chidituke.workout_tracker.dto.request.user.UserUpdateRequest;
import com.chidituke.workout_tracker.dto.request.user.UserSearchRequest;
import com.chidituke.workout_tracker.dto.response.user.UserProfileResponse;
import com.chidituke.workout_tracker.dto.response.user.UserSearchResponse;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.security.CurrentUser;
import com.chidituke.workout_tracker.security.UserPrincipal;
import com.chidituke.workout_tracker.service.user.UserService;
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

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for user profile management, discovery, and social features")
public class UserController {

    private final UserService userService;

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