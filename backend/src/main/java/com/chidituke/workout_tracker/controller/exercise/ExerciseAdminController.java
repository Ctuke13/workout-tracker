package com.chidituke.workout_tracker.controller.exercise;

import com.chidituke.workout_tracker.controller.BaseApiController;
import com.chidituke.workout_tracker.dto.request.exercise.*;
import com.chidituke.workout_tracker.dto.response.exercise.*;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.security.CurrentUser;
import com.chidituke.workout_tracker.security.UserPrincipal;
import com.chidituke.workout_tracker.service.exercise.ExerciseAdminService;
import com.chidituke.workout_tracker.service.user.UserService;
import com.chidituke.workout_tracker.service.exercise.ExerciseService;
import com.chidituke.workout_tracker.service.exercise.ExerciseFavoritesService;
import com.chidituke.workout_tracker.repository.workout.ExerciseRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/exercises/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@Tag(name = "Exercise Administration", description = "Administrative operations for exercise management")
public class ExerciseAdminController extends BaseApiController {

    private final ExerciseService exerciseService;
    private final UserService userService;
    private final ExerciseFavoritesService favoritesService;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseAdminService exerciseAdminService;

    // ===================================================================
    // CONTENT CREATION (PROFESSIONAL + ADMIN)
    // ===================================================================

    @PostMapping
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    @Operation(summary = "Create exercise", description = "Create a new exercise (professionals/admins only)")
    public ResponseEntity<ExerciseResponseDTO> createExercise(
            @Valid @RequestBody ExerciseCreateRequestDTO createRequest,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            User currentUser = userService.getUserById(userPrincipal.getId());
            Exercise exercise = exerciseAdminService.createProfessionalExercise(currentUser, createRequest);
            ExerciseResponseDTO response = ExerciseResponseDTO.fromEntity(exercise);

            log.info("Created new exercise {} by user {}", exercise.getId(), userPrincipal.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Failed to create exercise for user {}", userPrincipal.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('PROFESSIONAL') and @exerciseService.isExerciseCreatedByUser(#id, authentication.principal.id))")
    @Operation(summary = "Update exercise", description = "Update an existing exercise")
    public ResponseEntity<ExerciseResponseDTO> updateExercise(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseUpdateRequestDTO updateRequest,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            // TODO: Implement exercise update functionality in your service
            log.warn("Exercise update requested for ID {} by user {} - not implemented yet", id, userPrincipal.getId());
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .header("X-Reason", "Exercise update functionality coming soon")
                    .build();

        } catch (Exception e) {
            log.error("Failed to update exercise {} for user {}", id, userPrincipal.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===================================================================
    // ADMIN-ONLY OPERATIONS
    // ===================================================================

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve exercise", description = "Approve a pending exercise for publication")
    public ResponseEntity<Map<String, Object>> approveExercise(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            User admin = userService.getUserById(userPrincipal.getId());
            exerciseAdminService.approveExercise(id, admin);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Exercise approved successfully");
            response.put("exerciseId", id);
            response.put("approvedBy", admin.getUsername());

            log.info("Exercise {} approved by admin {}", id, userPrincipal.getId());
            return ok(response);

        } catch (Exception e) {
            log.error("Failed to approve exercise {} by admin {}", id, userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to approve exercise");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete exercise", description = "Delete an exercise (admin only)")
    public ResponseEntity<Map<String, Object>> deleteExercise(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            User admin = userService.getUserById(userPrincipal.getId());
            exerciseAdminService.deleteExercise(id, admin);

            // Clean up associated favorites
            favoritesService.cleanupDeletedExerciseFavorites(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Exercise deleted successfully");
            response.put("exerciseId", id);
            response.put("deletedBy", admin.getUsername());

            log.info("Exercise {} deleted by admin {}", id, userPrincipal.getId());
            return ok(response);

        } catch (Exception e) {
            log.error("Failed to delete exercise {} by admin {}", id, userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to delete exercise");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/bulk-action")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk exercise actions", description = "Perform bulk actions on multiple exercises")
    public ResponseEntity<Map<String, Object>> performBulkAction(
            @Valid @RequestBody BulkExerciseActionRequestDTO bulkRequest,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            User admin = userService.getUserById(userPrincipal.getId());
            exerciseAdminService.performBulkAction(
                    bulkRequest.getExerciseIds(),
                    bulkRequest.getAction(),
                    bulkRequest.getReason(),
                    admin
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("Bulk action '%s' completed on %d exercises",
                    bulkRequest.getAction(), bulkRequest.getExerciseIds().size()));
            response.put("action", bulkRequest.getAction());
            response.put("exerciseCount", bulkRequest.getExerciseIds().size());
            response.put("performedBy", admin.getUsername());

            log.info("Bulk action '{}' completed on {} exercises by admin {}",
                    bulkRequest.getAction(), bulkRequest.getExerciseIds().size(), userPrincipal.getId());

            return ok(response);

        } catch (Exception e) {
            log.error("Failed to perform bulk action by admin {}", userPrincipal.getId(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to perform bulk action");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ===================================================================
    // ANALYTICS & REPORTING
    // ===================================================================

    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSIONAL')")
    @Operation(summary = "Get exercise analytics", description = "Get detailed analytics for an exercise")
    public ResponseEntity<ExerciseAnalyticsResponseDTO> getExerciseAnalytics(
            @PathVariable Long id,
            @CurrentUser UserPrincipal userPrincipal) {

        try {
            ExerciseAdminService.ExerciseAnalytics analytics = exerciseAdminService.getExerciseAnalytics(id);
            ExerciseAnalyticsResponseDTO response = ExerciseAnalyticsResponseDTO.fromServiceAnalytics(analytics);

            log.debug("Retrieved analytics for exercise {} by user {}", id, userPrincipal.getId());
            return ok(response);

        } catch (Exception e) {
            log.error("Failed to get analytics for exercise {} by user {}", id, userPrincipal.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/system-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROFESSIONAL')")
    @Operation(summary = "Get system status", description = "Get exercise system health and statistics")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        try {
            Map<String, Object> systemStatus = new HashMap<>();

            systemStatus.put("totalExercises", getTotalExerciseCount());
            systemStatus.put("publishedExercises", getPublishedExerciseCount());
            systemStatus.put("professionalContent", exerciseRepository.countProfessionalContent());
            systemStatus.put("unpublishedExercises", exerciseRepository.findUnpublishedExercises().size());
            systemStatus.put("averageRating", exerciseRepository.getAverageRatingAcrossExercises());

            systemStatus.put("systemHealthy", true);
            systemStatus.put("lastUpdated", java.time.LocalDateTime.now());

            log.debug("Retrieved enhanced system status");
            return ok(systemStatus);

        } catch (Exception e) {
            log.error("Failed to get system status", e);
            Map<String, Object> errorStatus = new HashMap<>();
            errorStatus.put("systemHealthy", false);
            errorStatus.put("error", "Failed to retrieve system status");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorStatus);
        }
    }

    @GetMapping("/recent-activity")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get recent activity", description = "Get recent activity for admin monitoring")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "50") int limit) {

        try {
            var recentActivity = favoritesService.getRecentFavoriteActivity(days, limit);

            List<Map<String, Object>> response = recentActivity.stream()
                    .map(favorite -> {
                        Map<String, Object> activityMap = new HashMap<>();
                        activityMap.put("userId", favorite.getUserId());
                        activityMap.put("exerciseId", favorite.getExerciseId());
                        activityMap.put("createdAt", favorite.getCreatedAt());
                        activityMap.put("action", "favorited");
                        return activityMap;
                    })
                    .toList();

            log.debug("Retrieved {} recent activities", response.size());
            return okList(response);

        } catch (Exception e) {
            log.error("Failed to get recent activity", e);
            return okList(List.of());
        }
    }

    // ===================================================================
    // HELPER METHODS
    // ===================================================================

    private long getTotalExerciseCount() {
        try {
            // Use the standard JpaRepository count() method
            return exerciseRepository.count();
        } catch (Exception e) {
            log.warn("Could not get total exercise count", e);
            return 0;
        }
    }

    private long getPublishedExerciseCount() {
        try {
            // Use the existing query method from your repository
            return exerciseRepository.countPublishedExercises();
        } catch (Exception e) {
            log.warn("Could not get published exercise count", e);
            return 0;
        }
    }
}