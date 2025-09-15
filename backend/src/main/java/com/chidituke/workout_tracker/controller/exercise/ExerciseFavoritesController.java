package com.chidituke.workout_tracker.controller.exercise;

import com.chidituke.workout_tracker.controller.BaseApiController;
import com.chidituke.workout_tracker.dto.response.exercise.ExerciseResponseDTO;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.security.CurrentUser;
import com.chidituke.workout_tracker.security.UserPrincipal;
import com.chidituke.workout_tracker.service.exercise.ExerciseFavoritesService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/exercises/favorites")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@Tag(name = "Exercise Favorites", description = "Exercise favorites management endpoints")
public class ExerciseFavoritesController extends BaseApiController {

    private final ExerciseFavoritesService favoritesService;

    // ===================================================================
    // CORE FAVORITES ENDPOINTS
    // ===================================================================

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user favorites", description = "Get user's favorite exercises with pagination")
    public ResponseEntity<List<ExerciseResponseDTO>> getUserFavoriteExercises(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            // Support both paginated and non-paginated calls for backward compatibility
            if (page == 0 && size == 20) {
                // Default call - return all favorites (existing behavior)
                List<Exercise> favorites = favoritesService.getUserFavoriteExercises(userPrincipal.getId());
                List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(favorites);

                log.debug("Retrieved {} favorite exercises for user {}", response.size(), userPrincipal.getId());
                return okList(response);
            } else {
                // Paginated call - now that service is fixed, we can use proper pagination
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<Exercise> favoritesPage = favoritesService.getUserFavoriteExercises(userPrincipal.getId(), pageable);
                List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(favoritesPage.getContent());

                log.debug("Retrieved {} favorite exercises (page {}) for user {}", response.size(), page, userPrincipal.getId());
                return okList(response);
            }
        } catch (Exception e) {
            log.error("Failed to get favorite exercises for user {}", userPrincipal.getId(), e);
            return okList(Collections.emptyList());
        }
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user favorites count", description = "Get total number of user's favorite exercises")
    public ResponseEntity<Map<String, Object>> getUserFavoritesCount(@CurrentUser UserPrincipal userPrincipal) {

        try {
            long count = favoritesService.getUserFavoriteCount(userPrincipal.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("favoriteCount", count);
            response.put("hasAnyFavorites", count > 0);
            response.put("userId", userPrincipal.getId());

            log.debug("Retrieved favorite count: {} for user {}", count, userPrincipal.getId());
            return ok(response);

        } catch (Exception e) {
            log.error("Failed to get favorite count for user {}", userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("favoriteCount", 0L);
            errorResponse.put("hasAnyFavorites", false);
            return ok(errorResponse);
        }
    }

    @PostMapping("/{exerciseId}/toggle")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Toggle favorite", description = "Add or remove exercise from favorites")
    public ResponseEntity<Map<String, Object>> toggleExerciseFavorite(
            @PathVariable Long exerciseId,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            boolean isFavorite = favoritesService.toggleFavorite(userPrincipal.getId(), exerciseId);

            Map<String, Object> response = new HashMap<>();
            response.put("exerciseId", exerciseId);
            response.put("isFavorite", isFavorite);
            response.put("action", isFavorite ? "added" : "removed");
            response.put("message", isFavorite ? "Exercise added to favorites" : "Exercise removed from favorites");

            log.debug("Toggled favorite status for exercise {} (user {}): {}",
                    exerciseId, userPrincipal.getId(), isFavorite);

            return ok(response);

        } catch (Exception e) {
            log.error("Failed to toggle favorite for exercise {} (user {})", exerciseId, userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update favorite status");
            errorResponse.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/ids")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get favorite IDs", description = "Get lightweight list of user's favorite exercise IDs")
    public ResponseEntity<Set<Long>> getFavoriteExerciseIds(@CurrentUser UserPrincipal userPrincipal) {
        try {
            Set<Long> favoriteIds = favoritesService.getUserFavoriteExerciseIds(userPrincipal.getId());
            return ok(favoriteIds);
        } catch (Exception e) {
            log.error("Failed to get favorite IDs for user {}", userPrincipal.getId(), e);
            return ok(Collections.emptySet());
        }
    }

    @PostMapping("/check")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check multiple favorites", description = "Check favorite status for multiple exercises")
    public ResponseEntity<Map<Long, Boolean>> checkMultipleFavoriteStatus(
            @RequestBody List<Long> exerciseIds,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            Map<Long, Boolean> favoriteStatus = favoritesService.checkFavoriteStatus(userPrincipal.getId(), exerciseIds);
            return ok(favoriteStatus);
        } catch (Exception e) {
            log.error("Failed to check multiple favorites for user {}", userPrincipal.getId(), e);
            return ok(exerciseIds.stream()
                    .collect(Collectors.toMap(id -> id, id -> false)));
        }
    }

    // ===================================================================
    // BULK OPERATIONS
    // ===================================================================

    @PostMapping("/bulk/add")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add multiple favorites", description = "Add multiple exercises to favorites")
    public ResponseEntity<Map<String, Object>> addMultipleToFavorites(
            @RequestBody List<Long> exerciseIds,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            var favorites = favoritesService.addMultipleToFavorites(userPrincipal.getId(), exerciseIds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("addedCount", favorites.size());
            response.put("requestedCount", exerciseIds.size());
            response.put("message", String.format("Added %d exercises to favorites", favorites.size()));

            log.debug("Added {} exercises to favorites for user {}", favorites.size(), userPrincipal.getId());
            return ok(response);

        } catch (Exception e) {
            log.error("Failed to add multiple favorites for user {}", userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("addedCount", 0);
            errorResponse.put("error", "Failed to add exercises to favorites");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/bulk/remove")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove multiple favorites", description = "Remove multiple exercises from favorites")
    public ResponseEntity<Map<String, Object>> removeMultipleFromFavorites(
            @RequestBody List<Long> exerciseIds,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            favoritesService.removeMultipleFromFavorites(userPrincipal.getId(), exerciseIds);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("removedCount", exerciseIds.size());
            response.put("message", String.format("Removed %d exercises from favorites", exerciseIds.size()));

            log.debug("Removed {} exercises from favorites for user {}", exerciseIds.size(), userPrincipal.getId());
            return ok(response);

        } catch (Exception e) {
            log.error("Failed to remove multiple favorites for user {}", userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("removedCount", 0);
            errorResponse.put("error", "Failed to remove exercises from favorites");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/clear")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Clear all favorites", description = "Remove all exercises from user's favorites")
    public ResponseEntity<Map<String, Object>> clearAllFavorites(@CurrentUser UserPrincipal userPrincipal) {

        try {
            long count = favoritesService.getUserFavoriteCount(userPrincipal.getId());
            favoritesService.clearAllUserFavorites(userPrincipal.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("clearedCount", count);
            response.put("message", String.format("Cleared %d favorites", count));

            log.debug("Cleared {} favorites for user {}", count, userPrincipal.getId());
            return ok(response);

        } catch (Exception e) {
            log.error("Failed to clear favorites for user {}", userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("clearedCount", 0);
            errorResponse.put("error", "Failed to clear favorites");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ===================================================================
    // ANALYTICS ENDPOINTS
    // ===================================================================

    @GetMapping("/trending")
    @Operation(summary = "Get trending favorites", description = "Get exercises that are trending in favorites")
    public ResponseEntity<List<ExerciseResponseDTO>> getTrendingFavorites(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {

        List<Exercise> trending = favoritesService.getTrendingFavorites(days, limit);
        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(trending);
        return okList(response);
    }

    @GetMapping("/most-popular")
    @Operation(summary = "Get most favorited", description = "Get most favorited exercises overall")
    public ResponseEntity<List<ExerciseResponseDTO>> getMostFavoritedExercises(
            @RequestParam(defaultValue = "10") int limit) {

        List<Exercise> mostFavorited = favoritesService.getMostFavoritedExercises(limit);
        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(mostFavorited);
        return okList(response);
    }

    @GetMapping("/recommendations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get recommended based on favorites", description = "Get exercise recommendations based on favorites")
    public ResponseEntity<List<ExerciseResponseDTO>> getRecommendedBasedOnFavorites(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(defaultValue = "10") int limit) {

        List<Exercise> recommended = favoritesService.getRecommendedExercises(userPrincipal.getId(), limit);
        List<ExerciseResponseDTO> response = ExerciseResponseDTO.fromEntityList(recommended);
        return okList(response);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get favorite statistics", description = "Get comprehensive favorite system statistics")
    public ResponseEntity<ExerciseFavoritesService.FavoriteStatistics> getFavoriteStatistics() {
        ExerciseFavoritesService.FavoriteStatistics stats = favoritesService.getFavoriteStatistics();
        return ok(stats);
    }
}