package com.chidituke.workout_tracker.controller.pet;

import com.chidituke.workout_tracker.dto.request.pet.HomeSelectionRequest;
import com.chidituke.workout_tracker.dto.response.pet.*;
import com.chidituke.workout_tracker.mapper.pet.PetStatsMapper;
import com.chidituke.workout_tracker.model.pet.PetStats;
import com.chidituke.workout_tracker.security.CurrentUser;
import com.chidituke.workout_tracker.security.UserPrincipal;
import com.chidituke.workout_tracker.service.pet.PetStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for Pet Stats operations
 * Handles pet interactions, stat queries, and admin operations
 */
@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pet Stats", description = "Virtual pet companion statistics and interactions")
@SecurityRequirement(name = "bearer-jwt")
public class PetStatsController {

    private final PetStatsService petStatsService;
    private final PetStatsMapper petStatsMapper;

    // ============================================
    // GET PET STATS
    // ============================================

    @GetMapping("/stats")
    @Operation(summary = "Get current pet stats",
            description = "Retrieves the current statistics for the user's pet. Creates a new pet if one doesn't exist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet stats retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<PetStatsResponse> getPetStats(@CurrentUser UserPrincipal userPrincipal) {
        PetStats petStats = petStatsService.getPetStats(userPrincipal.getId());
        return ResponseEntity.ok(petStatsMapper.toResponse(petStats));
    }

    // ============================================
    // FEEDING ENDPOINTS (WITH CRYSTAL COST & EFFICIENCY)
    // ============================================

    @PostMapping("/feed/snack")
    @Operation(summary = "Feed pet a snack",
            description = "Feed pet a snack (costs 1 crystal, restores +15 fuel × efficiency)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet fed successfully"),
            @ApiResponse(responseCode = "400", description = "Not enough crystals or pet is sleeping"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<?> feedSnack(@CurrentUser UserPrincipal userPrincipal) {
        try {
            FeedResponse response = petStatsService.feedSnack(userPrincipal.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/feed/meal")
    @Operation(summary = "Feed pet a meal",
            description = "Feed pet a meal (costs 3 crystals, restores +40 fuel × efficiency) - BEST VALUE")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet fed successfully"),
            @ApiResponse(responseCode = "400", description = "Not enough crystals or pet is sleeping"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<?> feedMeal(@CurrentUser UserPrincipal userPrincipal) {
        try {
            FeedResponse response = petStatsService.feedMeal(userPrincipal.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/feed/feast")
    @Operation(summary = "Feed pet a feast",
            description = "Feed pet a feast (costs 5 crystals, restores +60 fuel × efficiency)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet fed successfully"),
            @ApiResponse(responseCode = "400", description = "Not enough crystals or pet is sleeping"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<?> feedFeast(@CurrentUser UserPrincipal userPrincipal) {
        try {
            FeedResponse response = petStatsService.feedFeast(userPrincipal.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/feed-preview")
    @Operation(summary = "Preview feeding results",
            description = "Preview how much fuel would be restored based on current efficiency")
    public ResponseEntity<FeedPreviewResponse> getFeedPreview(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam String mealType) {
        FeedPreviewResponse preview = petStatsService.getFeedPreview(
                userPrincipal.getId(),
                mealType
        );
        return ResponseEntity.ok(preview);
    }

    // ============================================
    // INTERACTION ENDPOINTS
    // ============================================

    @PostMapping("/motivate")
    @Operation(summary = "Motivate the pet",
            description = "Give pet a pep talk (restores +10 motivation, 12-hour cooldown). Requires fuel >= 40%")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet motivated successfully"),
            @ApiResponse(responseCode = "400", description = "Interaction on cooldown or low fuel"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<?> motivate(@CurrentUser UserPrincipal userPrincipal) {
        try {
            PetStats petStats = petStatsService.motivate(userPrincipal.getId());
            return ResponseEntity.ok(petStatsMapper.toResponse(petStats));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bathe")
    @Operation(summary = "Bathe the pet",
            description = "Clean the pet with tier-based animation. Tier 1: deodorant spray (+30), Tier 2: sponge (+50), Tier 3: sponge + showerhead (+60). Requires motivation >= 40% AND fuel >= 20%. No cooldown.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pet bathed successfully"),
            @ApiResponse(responseCode = "400", description = "Pet not motivated enough or low fuel"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<?> bathe(@CurrentUser UserPrincipal userPrincipal) {
        try {
            BatheResponse response = petStatsService.bathe(userPrincipal.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================
    // HOME SELECTION ENDPOINTS
    // ============================================

    @PostMapping("/home/select")
    @Operation(summary = "Select pet home",
            description = "Choose which home environment the pet lives in (GYM, NATURE, COZY)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Home selected successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid home type or not unlocked"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<?> selectHome(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody HomeSelectionRequest request) {
        try {
            PetStats petStats = petStatsService.selectHome(
                    userPrincipal.getId(),
                    request.getHomeType()
            );
            return ResponseEntity.ok(petStatsMapper.toResponse(petStats));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/home")
    @Operation(summary = "Get home information",
            description = "Get current selected home and list of unlocked homes")
    public ResponseEntity<HomeInfoResponse> getHomeInfo(@CurrentUser UserPrincipal userPrincipal) {
        HomeInfoResponse response = petStatsService.getHomeInfo(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    // ============================================
    // ADMIN ENDPOINTS
    // ============================================

    @PostMapping("/admin/trigger-decay")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Manually trigger daily decay (Admin only)",
            description = "Manually run the daily decay job for testing purposes")
    public ResponseEntity<Map<String, Object>> triggerDecay(@CurrentUser UserPrincipal userPrincipal) {
        int updatedCount = petStatsService.triggerDailyDecay();
        return ResponseEntity.ok(Map.of(
                "message", "Daily decay triggered successfully",
                "petsUpdated", updatedCount
        ));
    }

    @PostMapping("/admin/wake-pet/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Force wake sleeping pet (Admin only)",
            description = "Force a sleeping pet to wake up for testing")
    public ResponseEntity<Map<String, String>> wakePet(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long userId) {
        petStatsService.forceWakePet(userId);
        return ResponseEntity.ok(Map.of("message", "Pet woken successfully for user " + userId));
    }

    @PostMapping("/admin/clear-neglect/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clear neglect state (Admin only)",
            description = "Clear neglect state for testing")
    public ResponseEntity<Map<String, String>> clearNeglect(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long userId) {
        petStatsService.clearNeglect(userId);
        return ResponseEntity.ok(Map.of("message", "Neglect cleared for user " + userId));
    }

    @GetMapping("/admin/system-stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get system-wide pet statistics (Admin only)",
            description = "Retrieve aggregated statistics across all pets")
    public ResponseEntity<PetStatsService.SystemStats> getSystemStats(@CurrentUser UserPrincipal userPrincipal) {
        PetStatsService.SystemStats stats = petStatsService.getSystemStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/admin/pets-needing-attention")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get pets needing attention (Admin only)",
            description = "Retrieve lists of pets with low stats or unable to bathe")
    public ResponseEntity<PetStatsService.NeedAttentionStats> getPetsNeedingAttention(
            @CurrentUser UserPrincipal userPrincipal) {
        PetStatsService.NeedAttentionStats stats = petStatsService.getPetsNeedingAttention();
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/admin/delete/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a pet (Admin only)",
            description = "Delete a pet for a specific user")
    public ResponseEntity<Map<String, String>> deletePet(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long userId) {
        petStatsService.deletePet(userId);
        return ResponseEntity.ok(Map.of("message", "Pet deleted successfully for user " + userId));
    }
}