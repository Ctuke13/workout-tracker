package com.chidituke.workout_tracker.controller.user;

import com.chidituke.workout_tracker.dto.request.user.UserPreferencesDTO;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user preferences
 * Base path: /api/users/preferences
 */
@RestController
@RequestMapping("/api/users/preferences")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Preferences", description = "Manage user preferences (units, settings)")
public class UserPreferencesController {

    private final UserRepository userRepository;

    /**
     * Get current user's preferences
     * <p>
     * GET /api/users/preferences
     */
    @GetMapping
    @Operation(summary = "Get user preferences",
            description = "Get current user's unit preferences and settings")
    public ResponseEntity<UserPreferencesDTO> getPreferences(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Getting preferences for user: {}", userDetails.getUsername());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserPreferencesDTO preferences = UserPreferencesDTO.fromUser(user);

        return ResponseEntity.ok(preferences);
    }

    /**
     * Update user preferences
     * <p>
     * PUT /api/users/preferences
     */
    @PutMapping
    @Operation(summary = "Update user preferences",
            description = "Update current user's unit preferences and settings")
    public ResponseEntity<UserPreferencesDTO> updatePreferences(
            @RequestBody UserPreferencesDTO preferencesDTO,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Updating preferences for user: {}", userDetails.getUsername());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update preferences
        if (preferencesDTO.getPreferredDistanceUnit() != null) {
            user.setPreferredDistanceUnit(preferencesDTO.getPreferredDistanceUnit());
        }

        if (preferencesDTO.getPreferredWeightUnit() != null) {
            user.setPreferredWeightUnit(preferencesDTO.getPreferredWeightUnit());
        }

        // Save to database
        user = userRepository.save(user);

        log.info("Updated preferences: distance={}, weight={}",
                user.getPreferredDistanceUnit(),
                user.getPreferredWeightUnit()
        );

        UserPreferencesDTO updatedPreferences = UserPreferencesDTO.fromUser(user);

        return ResponseEntity.ok(updatedPreferences);
    }

    /**
     * Update distance unit preference
     * <p>
     * PATCH /api/users/preferences/distance-unit
     */
    @PatchMapping("/distance-unit")
    @Operation(summary = "Update distance unit preference")
    public ResponseEntity<String> updateDistanceUnit(
            @RequestParam String unit,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!unit.equals("km") && !unit.equals("miles")) {
            return ResponseEntity.badRequest().body("Invalid unit. Must be 'km' or 'miles'");
        }

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPreferredDistanceUnit(unit);
        userRepository.save(user);

        log.info("Updated distance unit to {} for user: {}", unit, userDetails.getUsername());

        return ResponseEntity.ok("Distance unit updated to " + unit);
    }

    /**
     * Update weight unit preference
     * <p>
     * PATCH /api/users/preferences/weight-unit
     */
    @PatchMapping("/weight-unit")
    @Operation(summary = "Update weight unit preference")
    public ResponseEntity<String> updateWeightUnit(
            @RequestParam String unit,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!unit.equals("kg") && !unit.equals("lbs")) {
            return ResponseEntity.badRequest().body("Invalid unit. Must be 'kg' or 'lbs'");
        }

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPreferredWeightUnit(unit);
        userRepository.save(user);

        log.info("Updated weight unit to {} for user: {}", unit, userDetails.getUsername());

        return ResponseEntity.ok("Weight unit updated to " + unit);
    }
}