package com.chidituke.workout_tracker.service;

import com.chidituke.workout_tracker.dto.request.program_plan.BulkAddRequest;
import com.chidituke.workout_tracker.dto.request.program_plan.WorkoutScheduleRequest;
import com.chidituke.workout_tracker.dto.request.program_plan.UpdateProgramPlanRequest;
import com.chidituke.workout_tracker.dto.response.program_plan.ProgramPlanResponse;
import com.chidituke.workout_tracker.dto.response.program_plan.ProgramStructureAnalyticsResponse;
import com.chidituke.workout_tracker.exceptions.plan_program.InvalidProgramStructureException;
import com.chidituke.workout_tracker.exceptions.plan_program.ScheduleConflictException;
import com.chidituke.workout_tracker.exceptions.plan_program.ProgramPlanNotFoundException;
import com.chidituke.workout_tracker.exceptions.workout_plan.WorkoutPlanNotFoundException;
import com.chidituke.workout_tracker.exceptions.workout_program.WorkoutProgramNotFoundException;
import com.chidituke.workout_tracker.exceptions.auth.UnauthorizedAccessException;
import com.chidituke.workout_tracker.mapper.ProgramPlanMapper;
import com.chidituke.workout_tracker.model.ProgramPlan;
import com.chidituke.workout_tracker.model.WorkoutPlan;
import com.chidituke.workout_tracker.model.WorkoutProgram;
import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.repository.ProgramPlanRepository;
import com.chidituke.workout_tracker.repository.WorkoutPlanRepository;
import com.chidituke.workout_tracker.repository.WorkoutProgramRepository;
import com.chidituke.workout_tracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ProgramPlanService {

    private final ProgramPlanRepository programPlanRepository;
    private final WorkoutProgramRepository workoutProgramRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final UserRepository userRepository;
    private final ProgramPlanMapper programPlanMapper;

    // ==================== PROGRAM STRUCTURE RETRIEVAL ====================

    @Transactional(readOnly = true)
    public List<ProgramPlanResponse> getProgramStructure(Long programId) {
        log.debug("Fetching program structure for program {}", programId);

        validateProgramExists(programId);
        List<ProgramPlan> programPlans = programPlanRepository
                .findByProgramIdOrderByWeekNumberAscDayNumberAsc(programId);

        return programPlans.stream()
                .map(programPlanMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProgramPlanResponse> getWeekStructure(Long programId, Integer weekNumber) {
        log.debug("Fetching week {} structure for program {}", weekNumber, programId);

        WorkoutProgram program = findProgramById(programId);
        List<ProgramPlan> weekPlans = programPlanRepository
                .findByProgramAndWeekNumberOrderByDayNumberAsc(program, weekNumber);

        return weekPlans.stream()
                .map(programPlanMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProgramStructureAnalyticsResponse getStructureAnalytics(Long programId) {
        log.debug("Generating structure analytics for program {}", programId);

        validateProgramExists(programId);
        List<Integer> weeks = programPlanRepository.findWeekNumbersByProgramId(programId);
        Long totalPlans = programPlanRepository.countByProgramId(programId);
        Long workoutDays = programPlanRepository.countWorkoutDaysByProgramId(programId);
        BigDecimal avgIntensity = programPlanRepository.findAverageIntensityByProgram(programId)
                .orElse(BigDecimal.ZERO);

        return ProgramStructureAnalyticsResponse.builder()
                .totalWeeks(weeks.size())
                .totalPlans(totalPlans.intValue())
                .workoutDays(workoutDays.intValue())
                .restDays(totalPlans.intValue() - workoutDays.intValue())
                .averageIntensity(avgIntensity)
                .weekNumbers(weeks)
                .build();
    }

    // ==================== ADDING WORKOUTS TO PROGRAMS ====================

    public ProgramPlanResponse addWorkoutToProgram(Long programId, WorkoutScheduleRequest request, String username) {
        log.debug("Adding workout to program {} at week {} day {} by user {}",
                programId, request.getWeekNumber(), request.getDayNumber(), username);

        User user = findUserByUsername(username);
        WorkoutProgram program = findProgramById(programId);
        validateProgramOwnership(program, user);

        WorkoutPlan workoutPlan = findWorkoutPlanById(request.getWorkoutPlanId());
        validateSchedulePosition(program, request.getWeekNumber(), request.getDayNumber(), null);

        ProgramPlan programPlan = ProgramPlan.builder()
                .program(program)
                .workoutPlan(workoutPlan)
                .weekNumber(request.getWeekNumber())
                .dayNumber(request.getDayNumber())
                .phaseType(request.getPhaseType())
                .targetIntensity(request.getTargetIntensity())
                .isOptional(request.getIsOptional())
                .notes(request.getNotes())
                .displayOrder(calculateNextDisplayOrder(program, request.getWeekNumber()))
                .isRestDay(false)
                .createdByUserId(user.getId())
                .build();

        ProgramPlan saved = programPlanRepository.save(programPlan);
        log.info("Successfully added workout to program: {} by user: {}", saved.getId(), username);

        return programPlanMapper.toResponse(saved);
    }

    public List<ProgramPlanResponse> bulkAddWorkouts(Long programId, BulkAddRequest request, String username) {
        log.debug("Bulk adding {} workouts to program {} by user {}",
                request.getWorkouts().size(), programId, username);

        User user = findUserByUsername(username);
        WorkoutProgram program = findProgramById(programId);
        validateProgramOwnership(program, user);

        List<ProgramPlan> programPlans = new ArrayList<>();

        for (WorkoutScheduleRequest workout : request.getWorkouts()) {
            validateSchedulePosition(program, workout.getWeekNumber(), workout.getDayNumber(), null);
            WorkoutPlan workoutPlan = findWorkoutPlanById(workout.getWorkoutPlanId());

            ProgramPlan programPlan = ProgramPlan.builder()
                    .program(program)
                    .workoutPlan(workoutPlan)
                    .weekNumber(workout.getWeekNumber())
                    .dayNumber(workout.getDayNumber())
                    .phaseType(workout.getPhaseType())
                    .targetIntensity(workout.getTargetIntensity())
                    .isOptional(workout.getIsOptional())
                    .notes(workout.getNotes())
                    .displayOrder(calculateNextDisplayOrder(program, workout.getWeekNumber()))
                    .isRestDay(false)
                    .createdByUserId(user.getId())
                    .build();

            programPlans.add(programPlan);
        }

        List<ProgramPlan> saved = programPlanRepository.saveAll(programPlans);
        log.info("Successfully bulk added {} workouts to program by user: {}", saved.size(), username);

        return saved.stream()
                .map(programPlanMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== UPDATING PROGRAM STRUCTURE ====================

    public ProgramPlanResponse updateProgramWorkout(Long programPlanId, UpdateProgramPlanRequest request, String username) {
        log.debug("Updating program workout {} by user {}", programPlanId, username);

        User user = findUserByUsername(username);
        ProgramPlan programPlan = programPlanRepository.findById(programPlanId)
                .orElseThrow(() -> new ProgramPlanNotFoundException("Program plan not found: " + programPlanId));

        validateProgramOwnership(programPlan.getProgram(), user);

        // Validate new schedule position if changed
        if (!Objects.equals(programPlan.getWeekNumber(), request.getWeekNumber()) ||
                !Objects.equals(programPlan.getDayNumber(), request.getDayNumber())) {
            validateSchedulePosition(programPlan.getProgram(),
                    request.getWeekNumber(),
                    request.getDayNumber(),
                    programPlanId);
        }

        // Update fields
        updateProgramPlanFields(programPlan, request);

        ProgramPlan saved = programPlanRepository.save(programPlan);
        log.info("Successfully updated program workout: {} by user: {}", saved.getId(), username);

        return programPlanMapper.toResponse(saved);
    }

    public void removeWorkoutFromProgram(Long programPlanId, String username) {
        log.debug("Removing workout from program: {} by user: {}", programPlanId, username);

        User user = findUserByUsername(username);
        ProgramPlan programPlan = programPlanRepository.findById(programPlanId)
                .orElseThrow(() -> new ProgramPlanNotFoundException("Program plan not found: " + programPlanId));

        validateProgramOwnership(programPlan.getProgram(), user);

        programPlanRepository.deleteById(programPlanId);
        log.info("Successfully removed workout from program: {} by user: {}", programPlanId, username);
    }

    // ==================== REORDERING AND RESTRUCTURING ====================

    public List<ProgramPlanResponse> reorderWeekWorkouts(Long programId, Integer weekNumber,
                                                         List<Long> programPlanIds, String username) {
        log.debug("Reordering week {} workouts for program {} by user {}", weekNumber, programId, username);

        User user = findUserByUsername(username);
        WorkoutProgram program = findProgramById(programId);
        validateProgramOwnership(program, user);

        for (int i = 0; i < programPlanIds.size(); i++) {
            programPlanRepository.updateDisplayOrder(programPlanIds.get(i), i + 1);
        }

        // Return updated week structure
        return getWeekStructure(programId, weekNumber);
    }

    public ProgramPlanResponse moveWorkout(Long programPlanId, Integer newWeekNumber,
                                           Integer newDayNumber, String username) {
        log.debug("Moving workout {} to week {} day {} by user {}",
                programPlanId, newWeekNumber, newDayNumber, username);

        User user = findUserByUsername(username);
        ProgramPlan programPlan = programPlanRepository.findById(programPlanId)
                .orElseThrow(() -> new ProgramPlanNotFoundException("Program plan not found: " + programPlanId));

        validateProgramOwnership(programPlan.getProgram(), user);
        validateSchedulePosition(programPlan.getProgram(), newWeekNumber, newDayNumber, programPlanId);

        programPlan.setWeekNumber(newWeekNumber);
        programPlan.setDayNumber(newDayNumber);
        programPlan.setDisplayOrder(calculateNextDisplayOrder(programPlan.getProgram(), newWeekNumber));

        ProgramPlan saved = programPlanRepository.save(programPlan);
        return programPlanMapper.toResponse(saved);
    }

    // ==================== TEMPLATE AND COPYING OPERATIONS ====================

    public List<ProgramPlanResponse> copyWeekStructure(Long programId, Integer sourceWeek,
                                                       Integer targetWeek, String username) {
        log.debug("Copying week {} to week {} for program {} by user {}",
                sourceWeek, targetWeek, programId, username);

        User user = findUserByUsername(username);
        WorkoutProgram program = findProgramById(programId);
        validateProgramOwnership(program, user);

        List<ProgramPlan> sourceWeekPlans = programPlanRepository
                .findByProgramAndWeekNumberOrderByDayNumberAsc(program, sourceWeek);

        List<ProgramPlan> copiedPlans = sourceWeekPlans.stream()
                .map(sourcePlan -> copyProgramPlan(sourcePlan, targetWeek, user.getId()))
                .collect(Collectors.toList());

        List<ProgramPlan> saved = programPlanRepository.saveAll(copiedPlans);
        return saved.stream()
                .map(programPlanMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<ProgramPlanResponse> applyProgramTemplate(Long programId, String templateName, String username) {
        log.debug("Applying template {} to program {} by user {}", templateName, programId, username);

        User user = findUserByUsername(username);
        WorkoutProgram program = findProgramById(programId);
        validateProgramOwnership(program, user);

        // This would integrate with your template system
        List<ProgramPlan> templatePlans = generateTemplateStructure(program, templateName, user.getId());
        List<ProgramPlan> saved = programPlanRepository.saveAll(templatePlans);

        return saved.stream()
                .map(programPlanMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== PHASE MANAGEMENT ====================

    public List<ProgramPlanResponse> setProgramPhases(Long programId, Map<String, Object> phaseConfiguration, String username) {
        log.debug("Setting program phases for program {} by user {}", programId, username);

        User user = findUserByUsername(username);
        WorkoutProgram program = findProgramById(programId);
        validateProgramOwnership(program, user);

        // Apply phase configuration to program plans
        List<ProgramPlan> programPlans = programPlanRepository.findByProgramIdOrderByWeekNumberAscDayNumberAsc(programId);
        applyPhaseConfiguration(programPlans, phaseConfiguration);

        List<ProgramPlan> saved = programPlanRepository.saveAll(programPlans);
        return saved.stream()
                .map(programPlanMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== VALIDATION AND CONFLICTS ====================

    public Map<String, Object> validateProgramStructure(Long programId) {
        log.debug("Validating program structure for program {}", programId);

        Map<String, Object> results = new HashMap<>();
        List<String> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Check for gaps in schedule
        List<ProgramPlan> programPlans = programPlanRepository.findByProgramIdOrderByWeekNumberAscDayNumberAsc(programId);
        validateScheduleGaps(programPlans, issues, warnings);

        // Check for intensity distribution
        validateIntensityDistribution(programPlans, issues, warnings);

        results.put("isValid", issues.isEmpty());
        results.put("issues", issues);
        results.put("warnings", warnings);
        results.put("planCount", programPlans.size());

        return results;
    }

    public Map<String, Object> checkSchedulingConflicts(Long programId, WorkoutScheduleRequest request) {
        WorkoutProgram program = findProgramById(programId);
        Map<String, Object> results = new HashMap<>();

        boolean hasConflict = programPlanRepository
                .findByProgramAndWeekNumberAndDayNumber(program, request.getWeekNumber(), request.getDayNumber())
                .isPresent();

        results.put("hasConflict", hasConflict);
        if (hasConflict) {
            results.put("conflictType", "SCHEDULE_OVERLAP");
            results.put("message", String.format("Week %d, Day %d is already scheduled",
                    request.getWeekNumber(), request.getDayNumber()));
        }

        return results;
    }

    // ==================== PROGRAM INSIGHTS ====================

    public Map<String, Object> getProgramRecommendations(Long programId) {
        log.debug("Generating program recommendations for program {}", programId);

        Map<String, Object> recommendations = new HashMap<>();
        List<ProgramPlan> programPlans = programPlanRepository.findByProgramIdOrderByWeekNumberAscDayNumberAsc(programId);

        // Analyze program structure and generate recommendations
        analyzeWorkloadDistribution(programPlans, recommendations);
        analyzeIntensityProgression(programPlans, recommendations);
        analyzeRestDayDistribution(programPlans, recommendations);

        return recommendations;
    }

    public Map<String, Object> getIntensityAnalysis(Long programId) {
        log.debug("Generating intensity analysis for program {}", programId);

        List<ProgramPlan> programPlans = programPlanRepository.findByProgramIdOrderByWeekNumberAscDayNumberAsc(programId);
        Map<String, Object> analysis = new HashMap<>();

        // Calculate intensity metrics
        Map<Integer, BigDecimal> weeklyIntensity = calculateWeeklyIntensity(programPlans);
        BigDecimal overallAverage = calculateOverallIntensity(programPlans);
        Map<String, Integer> intensityDistribution = calculateIntensityDistribution(programPlans);

        analysis.put("weeklyIntensity", weeklyIntensity);
        analysis.put("overallAverage", overallAverage);
        analysis.put("intensityDistribution", intensityDistribution);
        analysis.put("totalWorkouts", programPlans.stream().filter(p -> !p.getIsRestDay()).count());

        return analysis;
    }

    // ==================== BULK OPERATIONS ====================

    public void clearProgramStructure(Long programId, String username) {
        log.debug("Clearing program structure for program {} by user {}", programId, username);

        User user = findUserByUsername(username);
        WorkoutProgram program = findProgramById(programId);
        validateProgramOwnership(program, user);

        List<ProgramPlan> allPlans = programPlanRepository.findByProgramIdOrderByWeekNumberAscDayNumberAsc(programId);
        programPlanRepository.deleteAll(allPlans);

        log.info("Successfully cleared {} plans from program {} by user {}", allPlans.size(), programId, username);
    }

    public List<ProgramPlanResponse> duplicateProgramStructure(Long sourceId, Long targetId, String username) {
        log.debug("Duplicating program structure from {} to {} by user {}", sourceId, targetId, username);

        User user = findUserByUsername(username);
        WorkoutProgram sourceProgram = findProgramById(sourceId);
        WorkoutProgram targetProgram = findProgramById(targetId);

        validateProgramOwnership(sourceProgram, user);
        validateProgramOwnership(targetProgram, user);

        List<ProgramPlan> sourcePlans = programPlanRepository.findByProgramIdOrderByWeekNumberAscDayNumberAsc(sourceId);
        List<ProgramPlan> duplicatedPlans = sourcePlans.stream()
                .map(plan -> duplicateProgramPlan(plan, targetProgram, user.getId()))
                .collect(Collectors.toList());

        List<ProgramPlan> saved = programPlanRepository.saveAll(duplicatedPlans);
        return saved.stream()
                .map(programPlanMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==================== UTILITY METHODS ====================

    public List<Map<String, Object>> getAvailableTemplates() {
        // Return available program templates
        List<Map<String, Object>> templates = new ArrayList<>();

        templates.add(createTemplateInfo("3_DAY_SPLIT", "3-Day Split", "Classic 3-day workout split"));
        templates.add(createTemplateInfo("PUSH_PULL_LEGS", "Push/Pull/Legs", "6-day push/pull/legs routine"));
        templates.add(createTemplateInfo("FULL_BODY", "Full Body", "3-day full body workout"));
        templates.add(createTemplateInfo("STRENGTH_CYCLE", "Strength Cycle", "12-week strength building program"));

        return templates;
    }

    // ==================== HELPER METHODS ====================

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private WorkoutProgram findProgramById(Long id) {
        return workoutProgramRepository.findById(id)
                .orElseThrow(() -> new WorkoutProgramNotFoundException("Workout program not found: " + id));
    }

    private WorkoutPlan findWorkoutPlanById(Long id) {
        return workoutPlanRepository.findById(id)
                .orElseThrow(() -> new WorkoutPlanNotFoundException("Workout plan not found: " + id));
    }

    private void validateProgramExists(Long programId) {
        if (!workoutProgramRepository.existsById(programId)) {
            throw new WorkoutProgramNotFoundException("Workout program not found: " + programId);
        }
    }

    private void validateProgramOwnership(WorkoutProgram program, User user) {
        if (!isOwner(program, user) && !user.getUserType().name().equals("ADMIN")) {
            throw new UnauthorizedAccessException("Not authorized to modify this program");
        }
    }

    private boolean isOwner(WorkoutProgram program, User user) {
        return program.getCreatedByUserId() != null &&
                Objects.equals(program.getCreatedByUserId(), user.getId());
    }

    private void validateSchedulePosition(WorkoutProgram program, Integer weekNumber,
                                          Integer dayNumber, Long excludeId) {
        if (weekNumber < 1 || dayNumber < 1 || dayNumber > 7) {
            throw new InvalidProgramStructureException("Invalid schedule position: week " + weekNumber + ", day " + dayNumber);
        }

        boolean hasConflict = excludeId != null ?
                programPlanRepository.existsConflictingSchedule(program.getId(), weekNumber, dayNumber, excludeId) :
                programPlanRepository.findByProgramAndWeekNumberAndDayNumber(program, weekNumber, dayNumber).isPresent();

        if (hasConflict) {
            throw new ScheduleConflictException(
                    String.format("Schedule conflict at week %d, day %d", weekNumber, dayNumber));
        }
    }

    private Integer calculateNextDisplayOrder(WorkoutProgram program, Integer weekNumber) {
        List<ProgramPlan> weekPlans = programPlanRepository
                .findByProgramAndWeekNumberOrderByDayNumberAsc(program, weekNumber);
        return weekPlans.size() + 1;
    }

    private void updateProgramPlanFields(ProgramPlan programPlan, UpdateProgramPlanRequest request) {
        if (request.getWeekNumber() != null) {
            programPlan.setWeekNumber(request.getWeekNumber());
        }
        if (request.getDayNumber() != null) {
            programPlan.setDayNumber(request.getDayNumber());
        }
        if (request.getPhaseType() != null) {
            programPlan.setPhaseType(request.getPhaseType());
        }
        if (request.getTargetIntensity() != null) {
            programPlan.setTargetIntensity(request.getTargetIntensity());
        }
        if (request.getNotes() != null) {
            programPlan.setNotes(request.getNotes());
        }
        if (request.getIsOptional() != null) {
            programPlan.setIsOptional(request.getIsOptional());
        }
    }

    private ProgramPlan copyProgramPlan(ProgramPlan source, Integer targetWeek, Long userId) {
        return ProgramPlan.builder()
                .program(source.getProgram())
                .workoutPlan(source.getWorkoutPlan())
                .weekNumber(targetWeek)
                .dayNumber(source.getDayNumber())
                .phaseType(source.getPhaseType())
                .targetIntensity(source.getTargetIntensity())
                .isOptional(source.getIsOptional())
                .isRestDay(source.getIsRestDay())
                .notes(source.getNotes() + " (Copied)")
                .displayOrder(source.getDisplayOrder())
                .createdByUserId(userId)
                .build();
    }

    private ProgramPlan duplicateProgramPlan(ProgramPlan source, WorkoutProgram targetProgram, Long userId) {
        return ProgramPlan.builder()
                .program(targetProgram)
                .workoutPlan(source.getWorkoutPlan())
                .weekNumber(source.getWeekNumber())
                .dayNumber(source.getDayNumber())
                .phaseType(source.getPhaseType())
                .targetIntensity(source.getTargetIntensity())
                .isOptional(source.getIsOptional())
                .isRestDay(source.getIsRestDay())
                .notes(source.getNotes())
                .displayOrder(source.getDisplayOrder())
                .createdByUserId(userId)
                .build();
    }

    private List<ProgramPlan> generateTemplateStructure(WorkoutProgram program, String templateName, Long userId) {
        // Template generation logic would go here
        // For now, return empty list - implement based on your template requirements
        return new ArrayList<>();
    }

    private void applyPhaseConfiguration(List<ProgramPlan> programPlans, Map<String, Object> phaseConfiguration) {
        // Phase configuration logic would go here
        // Implement based on your phase management requirements
    }

    private void validateScheduleGaps(List<ProgramPlan> programPlans, List<String> issues, List<String> warnings) {
        // Validation logic for schedule gaps
    }

    private void validateIntensityDistribution(List<ProgramPlan> programPlans, List<String> issues, List<String> warnings) {
        // Validation logic for intensity distribution
    }

    private void analyzeWorkloadDistribution(List<ProgramPlan> programPlans, Map<String, Object> recommendations) {
        // Workload analysis logic
    }

    private void analyzeIntensityProgression(List<ProgramPlan> programPlans, Map<String, Object> recommendations) {
        // Intensity progression analysis logic
    }

    private void analyzeRestDayDistribution(List<ProgramPlan> programPlans, Map<String, Object> recommendations) {
        // Rest day distribution analysis logic
    }

    private Map<Integer, BigDecimal> calculateWeeklyIntensity(List<ProgramPlan> programPlans) {
        return programPlans.stream()
                .filter(p -> !p.getIsRestDay() && p.getTargetIntensity() != null)
                .collect(Collectors.groupingBy(
                        ProgramPlan::getWeekNumber,
                        Collectors.averagingDouble(p -> p.getTargetIntensity().doubleValue())))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> BigDecimal.valueOf(e.getValue())));
    }

    private BigDecimal calculateOverallIntensity(List<ProgramPlan> programPlans) {
        return programPlans.stream()
                .filter(p -> !p.getIsRestDay() && p.getTargetIntensity() != null)
                .map(ProgramPlan::getTargetIntensity)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(programPlans.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    private Map<String, Integer> calculateIntensityDistribution(List<ProgramPlan> programPlans) {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("low", 0);
        distribution.put("medium", 0);
        distribution.put("high", 0);

        programPlans.stream()
                .filter(p -> !p.getIsRestDay() && p.getTargetIntensity() != null)
                .forEach(p -> {
                    BigDecimal intensity = p.getTargetIntensity();
                    if (intensity.compareTo(BigDecimal.valueOf(3)) <= 0) {
                        distribution.merge("low", 1, Integer::sum);
                    } else if (intensity.compareTo(BigDecimal.valueOf(7)) <= 0) {
                        distribution.merge("medium", 1, Integer::sum);
                    } else {
                        distribution.merge("high", 1, Integer::sum);
                    }
                });

        return distribution;
    }

    private Map<String, Object> createTemplateInfo(String id, String name, String description) {
        Map<String, Object> template = new HashMap<>();
        template.put("id", id);
        template.put("name", name);
        template.put("description", description);
        return template;
    }
}