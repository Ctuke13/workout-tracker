package com.chidituke.workout_tracker.controller.workout;

import com.chidituke.workout_tracker.dto.request.workout_program.ProgramEnrollmentRequest;
import com.chidituke.workout_tracker.dto.request.workout_program.WorkoutProgramRequest;
import com.chidituke.workout_tracker.dto.response.workout_program.ProgramAnalyticsResponse;
import com.chidituke.workout_tracker.dto.response.workout_program.ProgramEnrollmentResponse;
import com.chidituke.workout_tracker.dto.response.workout_program.ProgramProgressResponse;
import com.chidituke.workout_tracker.dto.response.workout_program.WorkoutProgramResponse;
import com.chidituke.workout_tracker.exceptions.ErrorResponse;
import com.chidituke.workout_tracker.exceptions.user.ProfessionalVerificationException;
import com.chidituke.workout_tracker.exceptions.workout_program.WorkoutProgramNotFoundException;
import com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException;
import com.chidituke.workout_tracker.model.workout.ProgramPlan;
import com.chidituke.workout_tracker.model.workout.WorkoutProgram.DifficultyLevel;
import com.chidituke.workout_tracker.model.workout.WorkoutProgram.ProgramType;
import com.chidituke.workout_tracker.service.workout.WorkoutProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/workout-programs")
@RequiredArgsConstructor
@Tag(name = "Workout Programs", description = "API for managing multi-week workout programs")
public class WorkoutProgramController {

    private final WorkoutProgramService workoutProgramService;

    // =======================
    // PUBLIC DISCOVERY ENDPOINTS
    // =======================

    @GetMapping
    @Operation(summary = "Get all published programs", description = "Retrieve all published workout programs ordered by popularity")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved workout programs")
    public ResponseEntity<List<WorkoutProgramResponse>> getAllPublishedPrograms() {
        List<WorkoutProgramResponse> programs = workoutProgramService.getAllPublishedPrograms();
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get program by ID", description = "Retrieve a specific workout program by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Program found"),
            @ApiResponse(responseCode = "404", description = "Program not found")
    })
    public ResponseEntity<WorkoutProgramResponse> getProgramById(
            @Parameter(description = "Program ID") @PathVariable Long id,
            Principal principal) {

        Optional<WorkoutProgramResponse> program;

        if (principal != null) {
            // User is authenticated, check both published and owned programs
            program = workoutProgramService.getProgramById(id, principal.getName());
        } else {
            // Not authenticated, only published programs
            program = workoutProgramService.getProgramById(id);
        }

        return program.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get programs by type", description = "Retrieve programs filtered by program type")
    @ApiResponse(responseCode = "200", description = "Programs retrieved successfully")
    public ResponseEntity<List<WorkoutProgramResponse>> getProgramsByType(
            @Parameter(description = "Program type") @PathVariable ProgramType type) {

        List<WorkoutProgramResponse> programs = workoutProgramService.getProgramsByType(type);
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/difficulty/{difficulty}")
    @Operation(summary = "Get programs by difficulty", description = "Retrieve programs filtered by difficulty level")
    @ApiResponse(responseCode = "200", description = "Programs retrieved successfully")
    public ResponseEntity<List<WorkoutProgramResponse>> getProgramsByDifficulty(
            @Parameter(description = "Difficulty level") @PathVariable DifficultyLevel difficulty) {

        List<WorkoutProgramResponse> programs = workoutProgramService.getProgramsByDifficulty(difficulty);
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/duration")
    @Operation(summary = "Get programs by duration", description = "Retrieve programs within a specific duration range")
    @ApiResponse(responseCode = "200", description = "Programs retrieved successfully")
    public ResponseEntity<List<WorkoutProgramResponse>> getProgramsByDuration(
            @Parameter(description = "Minimum weeks") @RequestParam @Min(1) @Max(52) Integer minWeeks,
            @Parameter(description = "Maximum weeks") @RequestParam @Min(1) @Max(52) Integer maxWeeks) {

        List<WorkoutProgramResponse> programs = workoutProgramService.getProgramsByDuration(minWeeks, maxWeeks);
        return ResponseEntity.ok(programs);
    }

    // =======================
    // SEARCH & ADVANCED FILTERING
    // =======================

    @GetMapping("/search")
    @Operation(summary = "Search workout programs", description = "Search programs by name with optional filters")
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    public ResponseEntity<Page<WorkoutProgramResponse>> searchPrograms(
            @Parameter(description = "Search term") @RequestParam(required = false) String q,
            @Parameter(description = "Program type filter") @RequestParam(required = false) ProgramType programType,
            @Parameter(description = "Difficulty filter") @RequestParam(required = false) DifficultyLevel difficulty,
            @Parameter(description = "Minimum weeks") @RequestParam(required = false) @Min(1) @Max(52) Integer minWeeks,
            @Parameter(description = "Maximum weeks") @RequestParam(required = false) @Min(1) @Max(52) Integer maxWeeks,
            @Parameter(description = "Minimum sessions per week") @RequestParam(required = false) @Min(1) @Max(7) Integer minSessions,
            @Parameter(description = "Maximum sessions per week") @RequestParam(required = false) @Min(1) @Max(7) Integer maxSessions,
            @PageableDefault(size = 20, sort = "enrollmentCount", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<WorkoutProgramResponse> results;

        if (hasAdvancedFilters(programType, difficulty, minWeeks, maxWeeks, minSessions, maxSessions)) {
            // Use advanced filtering
            results = workoutProgramService.searchProgramsWithFilters(
                    programType, difficulty, minWeeks, maxWeeks, minSessions, maxSessions, pageable);
        } else if (q != null && !q.trim().isEmpty()) {
            // Simple text search
            results = workoutProgramService.searchPrograms(q, pageable);
        } else {
            // No search term or filters, return all published programs with pagination
            results = Page.empty(pageable);
        }

        return ResponseEntity.ok(results);
    }

    // =======================
    // POPULAR & RECOMMENDED PROGRAMS
    // =======================

    @GetMapping("/popular")
    @Operation(summary = "Get most popular programs", description = "Retrieve the most popular workout programs")
    @ApiResponse(responseCode = "200", description = "Popular programs retrieved successfully")
    public ResponseEntity<List<WorkoutProgramResponse>> getMostPopularPrograms(
            @Parameter(description = "Number of programs to return") @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {

        List<WorkoutProgramResponse> programs = workoutProgramService.getMostPopularPrograms(limit);
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/highly-rated")
    @Operation(summary = "Get highly rated programs", description = "Retrieve programs with high ratings")
    @ApiResponse(responseCode = "200", description = "Highly rated programs retrieved successfully")
    public ResponseEntity<List<WorkoutProgramResponse>> getHighlyRatedPrograms(
            @Parameter(description = "Minimum rating") @RequestParam(defaultValue = "4.0") @Min(1) @Max(5) Double minRating,
            @Parameter(description = "Minimum number of reviews") @RequestParam(defaultValue = "5") @Min(1) Integer minReviews) {

        List<WorkoutProgramResponse> programs = workoutProgramService.getHighlyRatedPrograms(minRating, minReviews);
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending programs", description = "Retrieve currently trending workout programs")
    @ApiResponse(responseCode = "200", description = "Trending programs retrieved successfully")
    public ResponseEntity<List<WorkoutProgramResponse>> getTrendingPrograms(
            @Parameter(description = "Minimum enrollment count") @RequestParam(defaultValue = "10") @Min(1) Integer minEnrollment) {

        List<WorkoutProgramResponse> programs = workoutProgramService.getTrendingPrograms(minEnrollment);
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/high-completion")
    @Operation(summary = "Get programs with high completion rates", description = "Retrieve programs with high completion rates")
    @ApiResponse(responseCode = "200", description = "High completion rate programs retrieved successfully")
    public ResponseEntity<List<WorkoutProgramResponse>> getHighCompletionRatePrograms(
            @Parameter(description = "Minimum completion rate (0.0-1.0)") @RequestParam(defaultValue = "0.8") @Min(0) @Max(1) Double minCompletionRate) {

        List<WorkoutProgramResponse> programs = workoutProgramService.getHighCompletionRatePrograms(minCompletionRate);
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get program statistics", description = "Retrieve overall statistics about workout programs")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<Map<String, Object>> getOverallProgramStatistics() {
        Map<String, Object> statistics = workoutProgramService.getOverallProgramStatistics();
        return ResponseEntity.ok(statistics);
    }

    // =======================
    // PROFESSIONAL PROGRAM CREATION
    // =======================

    @PostMapping
    @Operation(summary = "Create workout program",
            description = "Create a new workout program (requires PROFESSIONAL or ADMIN role)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Program created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Professional role required")
    })
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<WorkoutProgramResponse> createProgram(
            @Valid @RequestBody WorkoutProgramRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        WorkoutProgramResponse response = workoutProgramService.createProgram(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update workout program", description = "Update an existing workout program")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Program updated successfully"),
            @ApiResponse(responseCode = "404", description = "Program not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this program")
    })
    public ResponseEntity<WorkoutProgramResponse> updateProgram(
            @Parameter(description = "Program ID") @PathVariable Long id,
            @Valid @RequestBody WorkoutProgramRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        WorkoutProgramResponse response = workoutProgramService.updateProgram(id, userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workout program", description = "Delete a workout program")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Program deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Program not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to delete this program"),
            @ApiResponse(responseCode = "409", description = "Cannot delete program with active enrollments")
    })
    public ResponseEntity<Void> deleteProgram(
            @Parameter(description = "Program ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        workoutProgramService.deleteProgram(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // =======================
    // PROGRAM ENROLLMENT & TRACKING
    // =======================

    @PostMapping("/{id}/enroll")
    @Operation(summary = "Enroll in program", description = "Enroll the authenticated user in a workout program")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Successfully enrolled in program"),
            @ApiResponse(responseCode = "404", description = "Program not found"),
            @ApiResponse(responseCode = "400", description = "Program not available for enrollment"),
            @ApiResponse(responseCode = "409", description = "Already enrolled in this program")
    })
    public ResponseEntity<ProgramEnrollmentResponse> enrollInProgram(
            @Parameter(description = "Program ID") @PathVariable Long id,
            @Valid @RequestBody ProgramEnrollmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProgramEnrollmentResponse response = workoutProgramService.enrollInProgram(
                userDetails.getUsername(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/progress")
    @Operation(summary = "Get program progress", description = "Get user's progress through a specific program")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Program not found")
    })
    public ResponseEntity<ProgramProgressResponse> getProgramProgress(
            @Parameter(description = "Program ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProgramProgressResponse progress = workoutProgramService.getProgramProgress(userDetails.getUsername(), id);
        return ResponseEntity.ok(progress);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark program completed", description = "Mark a program as completed by the user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Program marked as completed"),
            @ApiResponse(responseCode = "404", description = "Program not found"),
            @ApiResponse(responseCode = "400", description = "Program not yet completed")
    })
    public ResponseEntity<Map<String, Object>> markProgramCompleted(
            @Parameter(description = "Program ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        workoutProgramService.markProgramCompleted(userDetails.getUsername(), id);

        Map<String, Object> response = Map.of(
                "message", "Program marked as completed successfully",
                "programId", id,
                "completedAt", java.time.LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    // =======================
    // PROGRAM STRUCTURE
    // =======================

    @GetMapping("/{id}/structure")
    @Operation(summary = "Get program structure", description = "Get the complete structure/schedule of a program")
    @ApiResponse(responseCode = "200", description = "Program structure retrieved successfully")
    public ResponseEntity<List<ProgramPlan>> getProgramStructure(
            @Parameter(description = "Program ID") @PathVariable Long id) {

        List<ProgramPlan> structure = workoutProgramService.getProgramStructure(id);
        return ResponseEntity.ok(structure);
    }

    @GetMapping("/{id}/week/{weekNumber}")
    @Operation(summary = "Get program week structure", description = "Get the structure for a specific week of a program")
    @ApiResponse(responseCode = "200", description = "Week structure retrieved successfully")
    public ResponseEntity<List<ProgramPlan>> getProgramWeekStructure(
            @Parameter(description = "Program ID") @PathVariable Long id,
            @Parameter(description = "Week number") @PathVariable @Min(1) @Max(52) Integer weekNumber) {

        List<ProgramPlan> weekStructure = workoutProgramService.getProgramWeekStructure(id, weekNumber);
        return ResponseEntity.ok(weekStructure);
    }

    @GetMapping("/{id}/schedule-overview")
    @Operation(summary = "Get program schedule overview", description = "Get overview of program schedule and workout distribution")
    @ApiResponse(responseCode = "200", description = "Schedule overview retrieved successfully")
    public ResponseEntity<Map<String, Object>> getProgramScheduleOverview(
            @Parameter(description = "Program ID") @PathVariable Long id) {

        Map<String, Object> overview = workoutProgramService.getProgramScheduleOverview(id);
        return ResponseEntity.ok(overview);
    }

    // =======================
    // PROFESSIONAL FEATURES
    // =======================

    @GetMapping("/my")
    @Operation(summary = "Get professional's programs", description = "Get all programs created by the authenticated professional")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Professional programs retrieved successfully")
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<WorkoutProgramResponse>> getProfessionalPrograms(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutProgramResponse> programs = workoutProgramService.getProfessionalPrograms(userDetails.getUsername());
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/my/published")
    @Operation(summary = "Get professional's published programs", description = "Get published programs created by the authenticated professional")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Published programs retrieved successfully")
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<WorkoutProgramResponse>> getProfessionalPublishedPrograms(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkoutProgramResponse> programs = workoutProgramService.getProfessionalPublishedPrograms(userDetails.getUsername());
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/my/count")
    @Operation(summary = "Get professional program count", description = "Get total number of programs created by the professional")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Program count retrieved successfully")
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getProfessionalProgramCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        long count = workoutProgramService.getProfessionalProgramCount(userDetails.getUsername());

        Map<String, Object> response = Map.of(
                "totalPrograms", count
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/analytics")
    @Operation(summary = "Get program analytics", description = "Get analytics data for a program (creator or admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Program not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view analytics")
    })
    public ResponseEntity<ProgramAnalyticsResponse> getProgramAnalytics(
            @Parameter(description = "Program ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProgramAnalyticsResponse analytics = workoutProgramService.getProgramAnalytics(id, userDetails.getUsername());
        return ResponseEntity.ok(analytics);
    }

    // =======================
    // ADMIN FEATURES
    // =======================

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve program", description = "Approve a program for publication (admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Program approved successfully"),
            @ApiResponse(responseCode = "404", description = "Program not found"),
            @ApiResponse(responseCode = "403", description = "Admin role required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveProgram(
            @Parameter(description = "Program ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        workoutProgramService.approveProgram(id, userDetails.getUsername());

        Map<String, Object> response = Map.of(
                "message", "Program approved successfully",
                "programId", id,
                "approvedAt", java.time.LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/awaiting-review")
    @Operation(summary = "Get programs awaiting review", description = "Get programs waiting for admin approval")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponse(responseCode = "200", description = "Programs awaiting review retrieved successfully")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WorkoutProgramResponse>> getProgramsAwaitingReview() {
        List<WorkoutProgramResponse> programs = workoutProgramService.getProgramsAwaitingReview();
        return ResponseEntity.ok(programs);
    }

    // =======================
    // EXCEPTION HANDLERS
    // =======================

    @ExceptionHandler(WorkoutProgramNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProgramNotFound(
            WorkoutProgramNotFoundException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Workout Program Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ProfessionalVerificationException.class)
    public ResponseEntity<ErrorResponse> handleProfessionalVerification(
            ProfessionalVerificationException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Professional Role Required",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedOperation(
            UnauthorizedOperationException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Unauthorized Operation",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Operation Not Allowed",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Request",
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    // =======================
    // HELPER METHODS
    // =======================

    private boolean hasAdvancedFilters(ProgramType programType, DifficultyLevel difficulty,
                                       Integer minWeeks, Integer maxWeeks, Integer minSessions, Integer maxSessions) {
        return programType != null || difficulty != null || minWeeks != null ||
                maxWeeks != null || minSessions != null || maxSessions != null;
    }
}