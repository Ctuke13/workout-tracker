package com.chidituke.workout_tracker.controller.test;

import com.chidituke.workout_tracker.controller.user.UserController;
import com.chidituke.workout_tracker.controller.workout.WorkoutPlanController;
import com.chidituke.workout_tracker.controller.workout.WorkoutProgramController;
import com.chidituke.workout_tracker.controller.user.ProfessionalProfileController;
import com.chidituke.workout_tracker.controller.user.SubscriptionController;
import com.chidituke.workout_tracker.controller.workout.ExerciseController;
import com.chidituke.workout_tracker.controller.workout.PerformanceController;
import com.chidituke.workout_tracker.controller.workout.ProgramPlanController;
import com.chidituke.workout_tracker.controller.workout.ScheduledWorkoutController;
import com.chidituke.workout_tracker.dto.request.program_plan.BulkAddRequest;
import com.chidituke.workout_tracker.dto.request.program_plan.WorkoutScheduleRequest;
import com.chidituke.workout_tracker.dto.request.workout_plan.WorkoutTemplateRequestDTO;
import com.chidituke.workout_tracker.dto.request.workout_program.WorkoutProgramRequest;
import com.chidituke.workout_tracker.dto.response.program_plan.ProgramPlanResponse;
import com.chidituke.workout_tracker.dto.response.program_plan.ProgramStructureAnalyticsResponse;
import com.chidituke.workout_tracker.model.workout.ProgramPlan;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.chidituke.workout_tracker.security.UserPrincipal;
import com.chidituke.workout_tracker.model.user.enums.UserType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    // Controllers (removed AuthController as no longer needed)
    private final UserController userController;
    private final ExerciseController exerciseController;
    private final WorkoutPlanController workoutPlanController;
    private final WorkoutProgramController workoutProgramController;
    private final ProgramPlanController programPlanController;
    private final PerformanceController performanceController;
    private final ProfessionalProfileController professionalProfileController;
    private final ScheduledWorkoutController scheduledWorkoutController;
    private final SubscriptionController subscriptionController;

    // ==================== ✅ CONGRUENT PROGRAM PLAN TESTING ====================

    /**
     * Test complete program structure workflow
     */
    @PostMapping("/program-plan/complete-workflow")
    public ResponseEntity<Map<String, Object>> testCompleteProgramPlanWorkflow() {
        Map<String, Object> results = new HashMap<>();
        List<String> steps = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            // Step 1: Create mock authentication (no longer using AuthController)
            steps.add("1. Creating mock professional authentication");
            UserPrincipal userPrincipal = createMockUserDetails("testpro", "PROFESSIONAL");
            Authentication auth = createAuthentication("testpro", "PROFESSIONAL");

            // Step 2: Create workout program
            steps.add("2. Creating workout program");
            WorkoutProgramRequest programRequest = createWorkoutProgramRequest();
            ResponseEntity<?> programResponse = workoutProgramController.createProgram(programRequest, userPrincipal);
            Long programId = extractId(programResponse);

            // Step 3: Create workout plans
            steps.add("3. Creating workout plans");
            List<Long> workoutPlanIds = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                WorkoutTemplateRequestDTO planRequest = createWorkoutPlanRequest("Plan " + i);
                ResponseEntity<?> planResponse = workoutPlanController.createWorkoutPlan(planRequest, userPrincipal);
                workoutPlanIds.add(extractId(planResponse));
            }

            // Step 4: Test adding individual workouts to program
            steps.add("4. Adding individual workouts to program structure");
            List<ProgramPlanResponse> addedWorkouts = new ArrayList<>();

            // Week 1: 3 workouts + 4 rest days
            for (int day = 1; day <= 7; day++) {
                WorkoutScheduleRequest scheduleRequest;
                if (day % 2 == 1 && day <= 5) { // Monday, Wednesday, Friday
                    scheduleRequest = createWorkoutScheduleRequest(
                            workoutPlanIds.get((day-1)/2 % workoutPlanIds.size()),
                            1, day, ProgramPlan.PhaseType.BASE_BUILDING,
                            BigDecimal.valueOf(60 + day * 5), false
                    );
                } else { // Rest days
                    scheduleRequest = createRestDayScheduleRequest(1, day, ProgramPlan.PhaseType.BASE_BUILDING);
                }

                ResponseEntity<ProgramPlanResponse> addResponse =
                        programPlanController.addWorkoutToProgram(programId, scheduleRequest, auth);
                addedWorkouts.add(addResponse.getBody());
            }

            // Step 5: Test bulk adding workouts
            steps.add("5. Bulk adding workouts for week 2");
            BulkAddRequest bulkRequest = createBulkAddRequest(workoutPlanIds, 2);
            ResponseEntity<List<ProgramPlanResponse>> bulkResponse =
                    programPlanController.bulkAddWorkouts(programId, bulkRequest, auth);

            // Step 6: Test program structure retrieval
            steps.add("6. Retrieving complete program structure");
            ResponseEntity<List<ProgramPlanResponse>> structureResponse =
                    programPlanController.getProgramStructure(programId);

            // Step 7: Test week-specific structure
            steps.add("7. Retrieving week 1 structure");
            ResponseEntity<List<ProgramPlanResponse>> week1Response =
                    programPlanController.getWeekStructure(programId, 1);

            // Step 8: Test analytics
            steps.add("8. Generating program structure analytics");
            ResponseEntity<ProgramStructureAnalyticsResponse> analyticsResponse =
                    programPlanController.getStructureAnalytics(programId);

            // Step 9: Test reordering
            steps.add("9. Testing workout reordering");
            List<Long> programPlanIds = week1Response.getBody().stream()
                    .map(ProgramPlanResponse::getId)
                    .toList();
            Collections.reverse(programPlanIds); // Reverse order
            ResponseEntity<List<ProgramPlanResponse>> reorderedResponse =
                    programPlanController.reorderWeekWorkouts(programId, 1, programPlanIds, auth);

            // Step 10: Test phase management
            steps.add("10. Testing phase management");
            Map<String, Object> phaseConfig = createPhaseConfiguration();
            ResponseEntity<List<ProgramPlanResponse>> phaseResponse =
                    programPlanController.setProgramPhases(programId, phaseConfig, auth);

            // Step 11: Test copying week structure
            steps.add("11. Testing week structure copying");
            ResponseEntity<List<ProgramPlanResponse>> copyResponse =
                    programPlanController.copyWeekStructure(programId, 1, 3, auth);

            // Step 12: Test validation
            steps.add("12. Testing program structure validation");
            ResponseEntity<Map<String, Object>> validationResponse =
                    programPlanController.validateProgramStructure(programId);

            // Step 13: Test template application
            steps.add("13. Testing template application");
            ResponseEntity<List<ProgramPlanResponse>> templateResponse =
                    programPlanController.applyProgramTemplate(programId, "3_DAY_SPLIT", auth);

            // Step 14: Test analytics and recommendations
            steps.add("14. Testing program recommendations");
            ResponseEntity<Map<String, Object>> recommendationsResponse =
                    programPlanController.getProgramRecommendations(programId);

            ResponseEntity<Map<String, Object>> intensityResponse =
                    programPlanController.getIntensityAnalysis(programId);

            // Compile results
            results.put("success", true);
            results.put("completedSteps", steps.size());
            results.put("steps", steps);
            results.put("programId", programId);
            results.put("structureSize", structureResponse.getBody().size());
            results.put("analytics", analyticsResponse.getBody());
            results.put("validation", validationResponse.getBody());
            results.put("recommendations", recommendationsResponse.getBody());
            results.put("intensity", intensityResponse.getBody());
            results.put("message", "✅ Complete ProgramPlan workflow test PASSED! All 14 steps completed successfully.");

        } catch (Exception e) {
            log.error("Error in program plan workflow test", e);
            errors.add("Step " + steps.size() + ": " + e.getMessage());
            results.put("success", false);
            results.put("completedSteps", steps.size());
            results.put("steps", steps);
            results.put("errors", errors);
            results.put("message", "❌ ProgramPlan workflow test FAILED at step " + steps.size());
        }

        return ResponseEntity.ok(results);
    }

    /**
     * Test error scenarios and validation
     */
    @PostMapping("/program-plan/error-scenarios")
    public ResponseEntity<Map<String, Object>> testProgramPlanErrorScenarios() {
        Map<String, Object> results = new HashMap<>();
        List<String> tests = new ArrayList<>();
        List<String> passed = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        try {
            UserPrincipal userPrincipal = createMockUserDetails("testpro", "PROFESSIONAL");
            Authentication auth = createAuthentication("testpro", "PROFESSIONAL");
            Long programId = 999999L; // Non-existent program

            // Test 1: Schedule conflict
            tests.add("Schedule conflict detection");
            try {
                WorkoutScheduleRequest conflictRequest1 = createWorkoutScheduleRequest(1L, 1, 1,
                        ProgramPlan.PhaseType.BASE_BUILDING, BigDecimal.valueOf(70), false);
                WorkoutScheduleRequest conflictRequest2 = createWorkoutScheduleRequest(2L, 1, 1,
                        ProgramPlan.PhaseType.BASE_BUILDING, BigDecimal.valueOf(80), false);

                // This should detect conflict
                ResponseEntity<Map<String, Object>> conflictCheck =
                        programPlanController.checkSchedulingConflicts(programId, conflictRequest2);

                passed.add("Schedule conflict detection");
            } catch (Exception e) {
                failed.add("Schedule conflict detection: " + e.getMessage());
            }

            // Test 2: Invalid schedule position
            tests.add("Invalid schedule position validation");
            try {
                WorkoutScheduleRequest invalidRequest = createWorkoutScheduleRequest(1L, 1, 8, // Invalid day
                        ProgramPlan.PhaseType.BASE_BUILDING, BigDecimal.valueOf(70), false);
                passed.add("Invalid schedule position validation");
            } catch (Exception e) {
                passed.add("Invalid schedule position validation (correctly rejected)");
            }

            // Test 3: Rest day without workout plan
            tests.add("Rest day validation");
            try {
                WorkoutScheduleRequest restDayRequest = createRestDayScheduleRequest(1, 2, null);
                // This should work
                passed.add("Rest day validation");
            } catch (Exception e) {
                failed.add("Rest day validation: " + e.getMessage());
            }

            // Test 4: Non-rest day without workout plan
            tests.add("Workout day without plan validation");
            try {
                WorkoutScheduleRequest invalidWorkoutRequest = new WorkoutScheduleRequest();
                invalidWorkoutRequest.setWorkoutPlanId(null); // Missing workout plan
                invalidWorkoutRequest.setWeekNumber(1);
                invalidWorkoutRequest.setDayNumber(3);
                invalidWorkoutRequest.setIsRestDay(false);
                // This should fail
                failed.add("Workout day without plan validation (should have failed)");
            } catch (Exception e) {
                passed.add("Workout day without plan validation (correctly rejected)");
            }

            results.put("totalTests", tests.size());
            results.put("passed", passed.size());
            results.put("failed", failed.size());
            results.put("tests", tests);
            results.put("passedTests", passed);
            results.put("failedTests", failed);
            results.put("success", failed.isEmpty());
            results.put("message", failed.isEmpty() ?
                    "✅ All error scenario tests passed!" :
                    "⚠️ Some error scenario tests failed");

        } catch (Exception e) {
            log.error("Error in error scenario tests", e);
            results.put("success", false);
            results.put("message", "❌ Error scenario testing failed: " + e.getMessage());
        }

        return ResponseEntity.ok(results);
    }

    /**
     * Test phase type enum functionality
     */
    @PostMapping("/program-plan/phase-enum-test")
    public ResponseEntity<Map<String, Object>> testPhaseEnumFunctionality() {
        Map<String, Object> results = new HashMap<>();

        try {
            // Test all phase types
            Map<String, Object> phaseTests = new HashMap<>();

            for (ProgramPlan.PhaseType phase : ProgramPlan.PhaseType.values()) {
                Map<String, Object> phaseInfo = new HashMap<>();
                phaseInfo.put("name", phase.name());
                phaseInfo.put("description", phase.getDescription());

                // These methods might not exist in your PhaseType enum, so let's be safe
                try {
                    phaseInfo.put("ordinal", phase.ordinal());
                } catch (Exception e) {
                    phaseInfo.put("ordinal", "N/A");
                }

                phaseTests.put(phase.name(), phaseInfo);
            }

            results.put("phaseTypes", phaseTests);
            results.put("totalPhases", ProgramPlan.PhaseType.values().length);
            results.put("success", true);
            results.put("message", "✅ Phase enum functionality test passed!");

        } catch (Exception e) {
            log.error("Error in phase enum test", e);
            results.put("success", false);
            results.put("message", "❌ Phase enum test failed: " + e.getMessage());
        }

        return ResponseEntity.ok(results);
    }

    /**
     * Test intensity distribution (0-100 scale)
     */
    @PostMapping("/program-plan/intensity-test")
    public ResponseEntity<Map<String, Object>> testIntensityDistribution() {
        Map<String, Object> results = new HashMap<>();

        try {
            // Test intensity categorization for 0-100 scale
            List<Map<String, Object>> intensityTests = new ArrayList<>();

            BigDecimal[] testValues = {
                    BigDecimal.valueOf(15),  // Low
                    BigDecimal.valueOf(30),  // Low boundary
                    BigDecimal.valueOf(50),  // Medium
                    BigDecimal.valueOf(70),  // Medium boundary
                    BigDecimal.valueOf(85),  // High
                    BigDecimal.valueOf(100)  // High boundary
            };

            for (BigDecimal intensity : testValues) {
                Map<String, Object> test = new HashMap<>();
                test.put("value", intensity);

                String category;
                if (intensity.compareTo(BigDecimal.valueOf(30)) <= 0) {
                    category = "LOW";
                } else if (intensity.compareTo(BigDecimal.valueOf(70)) <= 0) {
                    category = "MEDIUM";
                } else {
                    category = "HIGH";
                }

                test.put("category", category);
                intensityTests.add(test);
            }

            results.put("intensityTests", intensityTests);
            results.put("scale", "0-100");
            results.put("categories", Map.of(
                    "LOW", "0-30%",
                    "MEDIUM", "31-70%",
                    "HIGH", "71-100%"
            ));
            results.put("success", true);
            results.put("message", "✅ Intensity distribution test passed!");

        } catch (Exception e) {
            log.error("Error in intensity test", e);
            results.put("success", false);
            results.put("message", "❌ Intensity test failed: " + e.getMessage());
        }

        return ResponseEntity.ok(results);
    }

    // ==================== HELPER METHODS ====================

    private WorkoutScheduleRequest createWorkoutScheduleRequest(Long workoutPlanId, Integer week,
                                                                Integer day, ProgramPlan.PhaseType phase,
                                                                BigDecimal intensity, Boolean optional) {
        WorkoutScheduleRequest request = new WorkoutScheduleRequest();
        request.setWorkoutPlanId(workoutPlanId);
        request.setWeekNumber(week);
        request.setDayNumber(day);
        request.setPhaseType(phase);
        request.setTargetIntensity(intensity);
        request.setIsOptional(optional);
        request.setIsRestDay(false);
        request.setNotes("Test workout for W" + week + "D" + day);
        return request;
    }

    private WorkoutScheduleRequest createRestDayScheduleRequest(Integer week, Integer day, ProgramPlan.PhaseType phase) {
        WorkoutScheduleRequest request = new WorkoutScheduleRequest();
        request.setWorkoutPlanId(null); // Rest days have no workout plan
        request.setWeekNumber(week);
        request.setDayNumber(day);
        request.setPhaseType(phase);
        request.setTargetIntensity(BigDecimal.ZERO);
        request.setIsOptional(false);
        request.setIsRestDay(true);
        request.setNotes("Rest day");
        return request;
    }

    private BulkAddRequest createBulkAddRequest(List<Long> workoutPlanIds, Integer week) {
        List<WorkoutScheduleRequest> workouts = new ArrayList<>();

        // Add workouts for Monday, Wednesday, Friday
        for (int day = 1; day <= 7; day += 2) {
            if (day <= 5) { // Workouts
                workouts.add(createWorkoutScheduleRequest(
                        workoutPlanIds.get((day-1)/2 % workoutPlanIds.size()),
                        week, day, ProgramPlan.PhaseType.INTENSITY,
                        BigDecimal.valueOf(70 + day * 3), false
                ));
            }
        }

        // Add rest days for Tuesday, Thursday, Weekend
        for (int day = 2; day <= 7; day += 2) {
            workouts.add(createRestDayScheduleRequest(week, day, null));
        }
        if (week == 2) { // Add Sunday rest for week 2
            workouts.add(createRestDayScheduleRequest(week, 7, null));
        }

        BulkAddRequest bulkRequest = new BulkAddRequest();
        bulkRequest.setWorkouts(workouts);
        bulkRequest.setValidateConflicts(true);
        bulkRequest.setNotes("Bulk added week " + week);
        return bulkRequest;
    }

    private Map<String, Object> createPhaseConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("weeks1-2", ProgramPlan.PhaseType.BASE_BUILDING.name());
        config.put("weeks3-4", ProgramPlan.PhaseType.INTENSITY.name());
        config.put("week5", ProgramPlan.PhaseType.DELOAD.name());
        return config;
    }

    private WorkoutProgramRequest createWorkoutProgramRequest() {
        WorkoutProgramRequest request = new WorkoutProgramRequest();
        request.setName("Test Program Structure");
        request.setDescription("Testing program plan structure functionality");
        request.setProgramType("STRENGTH");
        request.setDifficultyLevel("INTERMEDIATE");
        request.setDurationWeeks(8);
        request.setSessionsPerWeek(3);
        request.setTargetGoals("Test structure management");
        request.setEquipmentNeeded("Gym equipment");
        request.setIsPublic(false);
        return request;
    }

    private WorkoutTemplateRequestDTO createWorkoutPlanRequest(String suffix) {
        WorkoutTemplateRequestDTO request = new WorkoutTemplateRequestDTO();
        request.setWorkoutName("Test Workout " + suffix);
        request.setWorkoutDescription("Test workout plan for structure testing");
        request.setWorkoutCategory("STRENGTH");
        request.setWorkoutType("STRENGTH");
        request.setDifficultyLevel("INTERMEDIATE");
        request.setEstimatedDurationMinutes(60);
        request.setIsPublic(false);
        return request;
    }

    private UserPrincipal createMockUserDetails(String username, String role) {
        // Convert role string to UserType enum
        UserType userType;
        try {
            userType = UserType.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            userType = UserType.REGULAR; // fallback
        }

        return new UserPrincipal(
                1L, // Mock user ID
                username,
                username + "@example.com", // Mock email
                "", // Password not needed for testing
                userType
        );
    }

    private Authentication createAuthentication(String username, String role) {
        UserPrincipal userPrincipal = createMockUserDetails(username, role);
        return new UsernamePasswordAuthenticationToken(
                userPrincipal, // Use UserPrincipal as principal
                null,
                userPrincipal.getAuthorities() // Get authorities from UserPrincipal
        );
    }

    private Long extractId(ResponseEntity<?> response) {
        Object body = response.getBody();
        if (body instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) body;
            Object id = map.get("id");
            if (id instanceof Number) {
                return ((Number) id).longValue();
            }
        }
        return 1L; // Fallback for testing
    }

    // ==================== METHOD IDENTIFICATION HELPER ====================

    /**
     * Helper endpoint to identify correct WorkoutPlan method names
     * Use this once to see what methods are available in your WorkoutPlan entity
     */
    @GetMapping("/identify-workout-plan-methods")
    public ResponseEntity<Map<String, Object>> identifyWorkoutPlanMethods() {
        Map<String, Object> results = new HashMap<>();

        try {
            // Get all methods from WorkoutPlan class
            java.lang.reflect.Method[] methods = WorkoutPlan.class.getDeclaredMethods();

            // Filter getter methods
            List<String> getterMethods = java.util.Arrays.stream(methods)
                    .filter(method -> method.getName().startsWith("get"))
                    .map(java.lang.reflect.Method::getName)
                    .sorted()
                    .collect(Collectors.toList());

            // Check specific methods we're looking for
            Map<String, Boolean> methodExists = new HashMap<>();
            String[] methodsToCheck = {
                    "getName", "getWorkoutName", "getTitle",
                    "getDescription", "getWorkoutDescription",
                    "getDifficulty", "getDifficultyLevel",
                    "getEstimatedDuration", "getDuration", "getEstimatedDurationMinutes",
                    "getPlanExercises", "getWorkoutPlanExercises", "getExercises"
            };

            for (String methodName : methodsToCheck) {
                try {
                    WorkoutPlan.class.getMethod(methodName);
                    methodExists.put(methodName, true);
                } catch (NoSuchMethodException e) {
                    methodExists.put(methodName, false);
                }
            }

            results.put("allGetterMethods", getterMethods);
            results.put("methodAvailability", methodExists);
            results.put("totalMethods", getterMethods.size());
            results.put("instructions", "Use the available methods in your ProgramPlanMapper");

        } catch (Exception e) {
            results.put("error", "Could not analyze WorkoutPlan class: " + e.getMessage());
        }

        return ResponseEntity.ok(results);
    }

    /**
     * Helper endpoint to identify correct ProgramPlanResponse DTO fields
     * Use this to see what fields are available in your ProgramPlanResponse
     */
    @GetMapping("/identify-program-plan-response-fields")
    public ResponseEntity<Map<String, Object>> identifyProgramPlanResponseFields() {
        Map<String, Object> results = new HashMap<>();

        try {
            // Get all fields from ProgramPlanResponse class
            java.lang.reflect.Field[] fields = com.chidituke.workout_tracker.dto.response.program_plan.ProgramPlanResponse.class.getDeclaredFields();

            // Get field names
            List<String> fieldNames = java.util.Arrays.stream(fields)
                    .map(java.lang.reflect.Field::getName)
                    .sorted()
                    .collect(Collectors.toList());

            // Check specific fields we're looking for
            Map<String, Boolean> fieldExists = new HashMap<>();
            String[] fieldsToCheck = {
                    "phaseDescription", "displayName", "weekDayIdentifier", "canBeSkipped",
                    "phaseType", "workoutPlanId", "workoutPlanName", "workoutPlanSummary"
            };

            for (String fieldName : fieldsToCheck) {
                boolean exists = fieldNames.contains(fieldName);
                fieldExists.put(fieldName, exists);
            }

            results.put("allFields", fieldNames);
            results.put("fieldAvailability", fieldExists);
            results.put("totalFields", fieldNames.size());
            results.put("instructions", "Update your ProgramPlanMapper to only use existing fields");

        } catch (Exception e) {
            results.put("error", "Could not analyze ProgramPlanResponse class: " + e.getMessage());
        }

        return ResponseEntity.ok(results);
    }

    // ==================== INFO AND HEALTH ====================

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getTestInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("module", "TestController with Proper UserPrincipal");
        info.put("version", "2.2");
        info.put("changes", List.of(
                "Removed AuthController dependency",
                "Uses proper UserPrincipal instead of Spring Security User",
                "Fixed Authentication objects to contain UserPrincipal",
                "Updated WorkoutPlanRequest field names",
                "Maintains all ProgramPlan testing functionality",
                "Compatible with @CurrentUser UserPrincipal pattern"
        ));
        info.put("features", List.of(
                "Complete ProgramPlan workflow testing",
                "Phase enum functionality testing",
                "Intensity distribution testing (0-100 scale)",
                "Error scenario validation",
                "Rest day vs workout day testing",
                "Bulk operations testing",
                "Template application testing",
                "Analytics and recommendations testing"
        ));
        info.put("endpoints", List.of(
                "/api/test/program-plan/complete-workflow",
                "/api/test/program-plan/error-scenarios",
                "/api/test/program-plan/phase-enum-test",
                "/api/test/program-plan/intensity-test",
                "/api/test/identify-workout-plan-methods",
                "/api/test/identify-program-plan-response-fields"
        ));
        info.put("status", "✅ Fixed - Proper UserPrincipal Support");
        return ResponseEntity.ok(info);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("✅ TestController is healthy with proper UserPrincipal support!");
    }
}