package com.chidituke.workout_tracker.controller.workout;

import com.chidituke.workout_tracker.dto.request.program_plan.BulkAddRequest;
import com.chidituke.workout_tracker.dto.request.program_plan.UpdateProgramPlanRequest;
import com.chidituke.workout_tracker.dto.request.program_plan.WorkoutScheduleRequest;
import com.chidituke.workout_tracker.dto.response.program_plan.ProgramPlanResponse;
import com.chidituke.workout_tracker.dto.response.program_plan.ProgramStructureAnalyticsResponse;
import com.chidituke.workout_tracker.service.social.ProgramPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/program-plans")
@RequiredArgsConstructor
@Tag(name = "Program Structure", description = "APIs for managing workout program structure and scheduling")
public class ProgramPlanController {

    private final ProgramPlanService programPlanService;

    // ==================== PROGRAM STRUCTURE MANAGEMENT ====================

    /**
     * Get complete program structure
     */
    @GetMapping("/programs/{programId}")
    @Operation(summary = "Get program structure", description = "Get the complete week/day structure of a workout program")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Program structure retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Program not found")
    })
    public ResponseEntity<List<ProgramPlanResponse>> getProgramStructure(
            @Parameter(description = "Program ID") @PathVariable Long programId) {

        List<ProgramPlanResponse> structure = programPlanService.getProgramStructure(programId);
        return ResponseEntity.ok(structure);
    }

    /**
     * Get specific week structure
     */
    @GetMapping("/programs/{programId}/weeks/{weekNumber}")
    @Operation(summary = "Get week structure", description = "Get workout structure for a specific week")
    @ApiResponse(responseCode = "200", description = "Week structure retrieved successfully")
    public ResponseEntity<List<ProgramPlanResponse>> getWeekStructure(
            @Parameter(description = "Program ID") @PathVariable Long programId,
            @Parameter(description = "Week number") @PathVariable Integer weekNumber) {

        List<ProgramPlanResponse> weekStructure = programPlanService.getWeekStructure(programId, weekNumber);
        return ResponseEntity.ok(weekStructure);
    }

    /**
     * Get program structure analytics
     */
    @GetMapping("/programs/{programId}/analytics")
    @Operation(summary = "Get program analytics", description = "Get analytics about program structure and intensity")
    public ResponseEntity<ProgramStructureAnalyticsResponse> getStructureAnalytics(
            @Parameter(description = "Program ID") @PathVariable Long programId) {

        ProgramStructureAnalyticsResponse analytics = programPlanService.getStructureAnalytics(programId);
        return ResponseEntity.ok(analytics);
    }

    // ==================== ADDING WORKOUTS TO PROGRAMS ====================

    /**
     * Add single workout to program
     */
    @PostMapping("/programs/{programId}/workouts")
    @Operation(summary = "Add workout to program", description = "Schedule a workout plan for a specific week and day")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Workout added to program successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid scheduling request"),
            @ApiResponse(responseCode = "404", description = "Program or workout plan not found"),
            @ApiResponse(responseCode = "409", description = "Scheduling conflict")
    })
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<ProgramPlanResponse> addWorkoutToProgram(
            @Parameter(description = "Program ID") @PathVariable Long programId,
            @Valid @RequestBody WorkoutScheduleRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        ProgramPlanResponse response = programPlanService.addWorkoutToProgram(programId, request, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Bulk add workouts to program
     */
    @PostMapping("/programs/{programId}/workouts/bulk")
    @Operation(summary = "Bulk add workouts", description = "Add multiple workouts to a program at once")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Workouts added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid bulk request"),
            @ApiResponse(responseCode = "409", description = "Some scheduling conflicts")
    })
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ProgramPlanResponse>> bulkAddWorkouts(
            @Parameter(description = "Program ID") @PathVariable Long programId,
            @Valid @RequestBody BulkAddRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        List<ProgramPlanResponse> responses = programPlanService.bulkAddWorkouts(programId, request, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    // ==================== UPDATING PROGRAM STRUCTURE ====================

    /**
     * Update specific workout in program
     */
    @PutMapping("/{programPlanId}")
    @Operation(summary = "Update program workout", description = "Update a specific workout within a program")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Program workout updated successfully"),
            @ApiResponse(responseCode = "404", description = "Program workout not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this program")
    })
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<ProgramPlanResponse> updateProgramWorkout(
            @Parameter(description = "Program plan ID") @PathVariable Long programPlanId,
            @Valid @RequestBody UpdateProgramPlanRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        ProgramPlanResponse response = programPlanService.updateProgramWorkout(programPlanId, request, username);

        return ResponseEntity.ok(response);
    }

    /**
     * Remove workout from program
     */
    @DeleteMapping("/{programPlanId}")
    @Operation(summary = "Remove workout from program", description = "Remove a specific workout from a program")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Workout removed successfully"),
            @ApiResponse(responseCode = "404", description = "Program workout not found"),
            @ApiResponse(responseCode = "403", description = "Not authorized to modify this program")
    })
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<Void> removeWorkoutFromProgram(
            @Parameter(description = "Program plan ID") @PathVariable Long programPlanId,
            Authentication authentication) {

        String username = authentication.getName();
        programPlanService.removeWorkoutFromProgram(programPlanId, username);

        return ResponseEntity.noContent().build();
    }

    // ==================== REORDERING AND RESTRUCTURING ====================

    /**
     * Reorder workouts within a week
     */
    @PutMapping("/programs/{programId}/weeks/{weekNumber}/reorder")
    @Operation(summary = "Reorder week workouts", description = "Reorder workouts within a specific week")
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ProgramPlanResponse>> reorderWeekWorkouts(
            @Parameter(description = "Program ID") @PathVariable Long programId,
            @Parameter(description = "Week number") @PathVariable Integer weekNumber,
            @Parameter(description = "New order of program plan IDs") @RequestBody List<Long> programPlanIds,
            Authentication authentication) {

        String username = authentication.getName();
        List<ProgramPlanResponse> reorderedWorkouts = programPlanService.reorderWeekWorkouts(
                programId, weekNumber, programPlanIds, username);

        return ResponseEntity.ok(reorderedWorkouts);
    }

    /**
     * Move workout to different week/day
     */
    @PutMapping("/{programPlanId}/move")
    @Operation(summary = "Move workout", description = "Move a workout to a different week and/or day")
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<ProgramPlanResponse> moveWorkout(
            @Parameter(description = "Program plan ID") @PathVariable Long programPlanId,
            @Parameter(description = "New week number") @RequestParam Integer newWeekNumber,
            @Parameter(description = "New day number") @RequestParam Integer newDayNumber,
            Authentication authentication) {

        String username = authentication.getName();
        ProgramPlanResponse response = programPlanService.moveWorkout(
                programPlanId, newWeekNumber, newDayNumber, username);

        return ResponseEntity.ok(response);
    }

    // ==================== PROGRAM TEMPLATES AND COPYING ====================

    /**
     * Copy week structure to another week
     */
    @PostMapping("/programs/{programId}/weeks/{sourceWeek}/copy-to/{targetWeek}")
    @Operation(summary = "Copy week structure", description = "Copy workout structure from one week to another")
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ProgramPlanResponse>> copyWeekStructure(
            @Parameter(description = "Program ID") @PathVariable Long programId,
            @Parameter(description = "Source week number") @PathVariable Integer sourceWeek,
            @Parameter(description = "Target week number") @PathVariable Integer targetWeek,
            Authentication authentication) {

        String username = authentication.getName();
        List<ProgramPlanResponse> copiedStructure = programPlanService.copyWeekStructure(
                programId, sourceWeek, targetWeek, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(copiedStructure);
    }

    /**
     * Apply template to program
     */
    @PostMapping("/programs/{programId}/apply-template")
    @Operation(summary = "Apply program template", description = "Apply a predefined template structure to a program")
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ProgramPlanResponse>> applyProgramTemplate(
            @Parameter(description = "Program ID") @PathVariable Long programId,
            @Parameter(description = "Template name") @RequestParam String templateName,
            Authentication authentication) {

        String username = authentication.getName();
        List<ProgramPlanResponse> appliedStructure = programPlanService.applyProgramTemplate(
                programId, templateName, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(appliedStructure);
    }

    // ==================== PHASE MANAGEMENT ====================

    /**
     * Set phase for week range
     */
    @PutMapping("/programs/{programId}/phases")
    @Operation(summary = "Set program phases", description = "Set training phases for week ranges")
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ProgramPlanResponse>> setProgramPhases(
            @Parameter(description = "Program ID") @PathVariable Long programId,
            @RequestBody Map<String, Object> phaseConfiguration,
            Authentication authentication) {

        String username = authentication.getName();
        List<ProgramPlanResponse> updatedStructure = programPlanService.setProgramPhases(
                programId, phaseConfiguration, username);

        return ResponseEntity.ok(updatedStructure);
    }

    // ==================== VALIDATION AND CONFLICTS ====================

    /**
     * Validate program structure
     */
    @PostMapping("/programs/{programId}/validate")
    @Operation(summary = "Validate program structure", description = "Check program structure for conflicts and issues")
    public ResponseEntity<Map<String, Object>> validateProgramStructure(
            @Parameter(description = "Program ID") @PathVariable Long programId) {

        Map<String, Object> validationResults = programPlanService.validateProgramStructure(programId);
        return ResponseEntity.ok(validationResults);
    }

    /**
     * Check for scheduling conflicts
     */
    @PostMapping("/programs/{programId}/check-conflicts")
    @Operation(summary = "Check scheduling conflicts", description = "Check for potential scheduling conflicts before adding workouts")
    public ResponseEntity<Map<String, Object>> checkSchedulingConflicts(
            @Parameter(description = "Program ID") @PathVariable Long programId,
            @Valid @RequestBody WorkoutScheduleRequest request) {

        Map<String, Object> conflictResults = programPlanService.checkSchedulingConflicts(programId, request);
        return ResponseEntity.ok(conflictResults);
    }

    // ==================== PROGRAM INSIGHTS ====================

    /**
     * Get program recommendations
     */
    @GetMapping("/programs/{programId}/recommendations")
    @Operation(summary = "Get program recommendations", description = "Get AI-powered recommendations for program improvement")
    public ResponseEntity<Map<String, Object>> getProgramRecommendations(
            @Parameter(description = "Program ID") @PathVariable Long programId) {

        Map<String, Object> recommendations = programPlanService.getProgramRecommendations(programId);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Get intensity analysis
     */
    @GetMapping("/programs/{programId}/intensity-analysis")
    @Operation(summary = "Get intensity analysis", description = "Analyze training intensity distribution across the program")
    public ResponseEntity<Map<String, Object>> getIntensityAnalysis(
            @Parameter(description = "Program ID") @PathVariable Long programId) {

        Map<String, Object> analysis = programPlanService.getIntensityAnalysis(programId);
        return ResponseEntity.ok(analysis);
    }

    // ==================== BULK OPERATIONS ====================

    /**
     * Clear program structure
     */
    @DeleteMapping("/programs/{programId}/clear")
    @Operation(summary = "Clear program structure", description = "Remove all workouts from a program")
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<Void> clearProgramStructure(
            @Parameter(description = "Program ID") @PathVariable Long programId,
            Authentication authentication) {

        String username = authentication.getName();
        programPlanService.clearProgramStructure(programId, username);

        return ResponseEntity.noContent().build();
    }

    /**
     * Duplicate program structure
     */
    @PostMapping("/programs/{sourceId}/duplicate-to/{targetId}")
    @Operation(summary = "Duplicate program structure", description = "Copy structure from one program to another")
    @PreAuthorize("hasRole('PROFESSIONAL') or hasRole('ADMIN')")
    public ResponseEntity<List<ProgramPlanResponse>> duplicateProgramStructure(
            @Parameter(description = "Source program ID") @PathVariable Long sourceId,
            @Parameter(description = "Target program ID") @PathVariable Long targetId,
            Authentication authentication) {

        String username = authentication.getName();
        List<ProgramPlanResponse> duplicatedStructure = programPlanService.duplicateProgramStructure(
                sourceId, targetId, username);

        return ResponseEntity.status(HttpStatus.CREATED).body(duplicatedStructure);
    }

    // ==================== UTILITY ENDPOINTS ====================

    /**
     * Get available program templates
     */
    @GetMapping("/templates")
    @Operation(summary = "Get program templates", description = "Get list of available program templates")
    public ResponseEntity<List<Map<String, Object>>> getAvailableTemplates() {
        List<Map<String, Object>> templates = programPlanService.getAvailableTemplates();
        return ResponseEntity.ok(templates);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the program structure service is healthy")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Program Plan Controller is healthy! 🏗️");
    }
}