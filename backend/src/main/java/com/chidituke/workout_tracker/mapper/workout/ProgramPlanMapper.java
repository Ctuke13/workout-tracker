package com.chidituke.workout_tracker.mapper.workout;

import com.chidituke.workout_tracker.dto.response.program_plan.ProgramPlanResponse;
import com.chidituke.workout_tracker.dto.response.program_plan.ProgramStructureAnalyticsResponse;
import com.chidituke.workout_tracker.dto.response.program_plan.WeekScheduleResponse;
import com.chidituke.workout_tracker.model.workout.ProgramPlan;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Mapper for converting ProgramPlan entities to DTOs
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProgramPlanMapper {

    private final UserRepository userRepository;

    /**
     * Convert ProgramPlan entity to ProgramPlanResponse DTO
     */
    public ProgramPlanResponse toResponse(ProgramPlan programPlan) {
        if (programPlan == null) {
            return null;
        }

        ProgramPlanResponse.ProgramPlanResponseBuilder builder = ProgramPlanResponse.builder()
                .id(programPlan.getId())
                .programId(programPlan.getProgram().getId())
                .programName(programPlan.getProgram().getName())
                .weekNumber(programPlan.getWeekNumber())
                .dayNumber(programPlan.getDayNumber())
                .phaseType(programPlan.getPhaseType() != null ? programPlan.getPhaseType().name() : null)
                .targetIntensity(programPlan.getTargetIntensity())
                .isOptional(programPlan.getIsOptional())
                .isRestDay(programPlan.getIsRestDay())
                .notes(programPlan.getNotes())
                .displayOrder(programPlan.getDisplayOrder())
                .createdByUserId(programPlan.getCreatedByUserId())
                .createdAt(programPlan.getCreatedAt())
                .updatedAt(programPlan.getUpdatedAt());


        if (!programPlan.getIsRestDay() && programPlan.getWorkoutPlan() != null) {
            WorkoutPlan workoutPlan = programPlan.getWorkoutPlan();
            builder.workoutPlanId(workoutPlan.getId())
                    .workoutPlanName(getWorkoutPlanName(workoutPlan))
                    .workoutPlanSummary(createWorkoutPlanSummary(workoutPlan));
        }

        // Add creator username if available
        if (programPlan.getCreatedByUserId() != null) {
            userRepository.findById(programPlan.getCreatedByUserId())
                    .ifPresent(user -> builder.createdByUsername(user.getUsername()));
        }

        return builder.build();
    }

    /**
     * Convert list of ProgramPlan entities to list of ProgramPlanResponse DTOs
     */
    public List<ProgramPlanResponse> toResponseList(List<ProgramPlan> programPlans) {
        if (programPlans == null) {
            return null;
        }

        return programPlans.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create workout plan summary for response
     */
    private ProgramPlanResponse.WorkoutPlanSummary createWorkoutPlanSummary(WorkoutPlan workoutPlan) {
        if (workoutPlan == null) {
            return null;
        }

        return ProgramPlanResponse.WorkoutPlanSummary.builder()
                .name(getWorkoutPlanName(workoutPlan))
                .description(getWorkoutPlanDescription(workoutPlan))
                .estimatedDuration(getWorkoutPlanEstimatedDuration(workoutPlan))
                .difficulty(getWorkoutPlanDifficulty(workoutPlan))
                .exerciseCount(getExerciseCount(workoutPlan))
                .primaryMuscleGroups(getPrimaryMuscleGroups(workoutPlan))
                .build();
    }

    /**
     * Create week schedule response from program plans
     */
    public WeekScheduleResponse toWeekScheduleResponse(Integer weekNumber, Long programId,
                                                       String programName, List<ProgramPlan> weekPlans) {

        // Create day schedules for all 7 days
        List<WeekScheduleResponse.DaySchedule> daySchedules = IntStream.rangeClosed(1, 7)
                .mapToObj(dayNumber -> createDaySchedule(dayNumber, weekPlans))
                .collect(Collectors.toList());

        // Calculate week statistics
        WeekScheduleResponse.WeekStatistics statistics = calculateWeekStatistics(weekPlans);

        return WeekScheduleResponse.builder()
                .weekNumber(weekNumber)
                .programId(programId)
                .programName(programName)
                .days(daySchedules)
                .statistics(statistics)
                .build();
    }

    /**
     * Create analytics response from program plans
     */
    public ProgramStructureAnalyticsResponse toAnalyticsResponse(List<ProgramPlan> programPlans) {
        if (programPlans == null || programPlans.isEmpty()) {
            return ProgramStructureAnalyticsResponse.builder()
                    .totalWeeks(0)
                    .totalPlans(0)
                    .workoutDays(0)
                    .restDays(0)
                    .averageIntensity(BigDecimal.ZERO)
                    .build();
        }

        // Group by week
        Map<Integer, List<ProgramPlan>> weekGroups = programPlans.stream()
                .collect(Collectors.groupingBy(ProgramPlan::getWeekNumber));

        // Create week summaries
        Map<Integer, ProgramStructureAnalyticsResponse.WeekSummary> weekSummaries =
                weekGroups.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> createWeekSummary(entry.getKey(), entry.getValue())
                        ));

        // Group by phase
        Map<String, ProgramStructureAnalyticsResponse.PhaseSummary> phaseSummaries =
                createPhaseSummaries(programPlans);

        // Create intensity distribution
        ProgramStructureAnalyticsResponse.IntensityDistribution intensityDistribution =
                createIntensityDistribution(programPlans);

        // Calculate overall metrics
        int workoutDays = (int) programPlans.stream().filter(p -> !p.getIsRestDay()).count();
        int restDays = programPlans.size() - workoutDays;
        BigDecimal averageIntensity = calculateAverageIntensity(programPlans);

        return ProgramStructureAnalyticsResponse.builder()
                .totalWeeks(weekGroups.size())
                .totalPlans(programPlans.size())
                .workoutDays(workoutDays)
                .restDays(restDays)
                .averageIntensity(averageIntensity)
                .weekNumbers(weekGroups.keySet().stream().sorted().collect(Collectors.toList()))
                .weekSummaries(weekSummaries)
                .phaseSummaries(phaseSummaries)
                .intensityDistribution(intensityDistribution)
                .build();
    }

    // ==================== HELPER METHODS ====================

    private WeekScheduleResponse.DaySchedule createDaySchedule(Integer dayNumber, List<ProgramPlan> weekPlans) {
        ProgramPlan dayPlan = weekPlans.stream()
                .filter(plan -> plan.getDayNumber().equals(dayNumber))
                .findFirst()
                .orElse(null);

        return WeekScheduleResponse.DaySchedule.builder()
                .dayNumber(dayNumber)
                .dayName(getDayName(dayNumber))
                .programPlan(dayPlan != null ? toResponse(dayPlan) : null)
                .hasWorkout(dayPlan != null && !dayPlan.getIsRestDay())
                .isRestDay(dayPlan != null && dayPlan.getIsRestDay())
                .build();
    }

    private WeekScheduleResponse.WeekStatistics calculateWeekStatistics(List<ProgramPlan> weekPlans) {
        if (weekPlans.isEmpty()) {
            return WeekScheduleResponse.WeekStatistics.builder()
                    .totalWorkouts(0)
                    .restDays(0)
                    .averageIntensity(BigDecimal.ZERO)
                    .totalExercises(0)
                    .dominantPhase("")
                    .build();
        }

        int totalWorkouts = (int) weekPlans.stream().filter(p -> !p.getIsRestDay()).count();
        int restDays = weekPlans.size() - totalWorkouts;
        BigDecimal averageIntensity = calculateAverageIntensity(weekPlans);
        int totalExercises = weekPlans.stream()
                .filter(p -> !p.getIsRestDay())
                .mapToInt(this::getExerciseCount)
                .sum();
        String dominantPhase = findDominantPhase(weekPlans);

        return WeekScheduleResponse.WeekStatistics.builder()
                .totalWorkouts(totalWorkouts)
                .restDays(restDays)
                .averageIntensity(averageIntensity)
                .totalExercises(totalExercises)
                .dominantPhase(dominantPhase)
                .build();
    }

    private ProgramStructureAnalyticsResponse.WeekSummary createWeekSummary(Integer weekNumber, List<ProgramPlan> weekPlans) {
        int workoutCount = (int) weekPlans.stream().filter(p -> !p.getIsRestDay()).count();
        int restDayCount = weekPlans.size() - workoutCount;
        BigDecimal averageIntensity = calculateAverageIntensity(weekPlans);

        List<String> phaseTypes = weekPlans.stream()
                .map(ProgramPlan::getPhaseType)
                .filter(phase -> phase != null)
                .map(ProgramPlan.PhaseType::name) // Convert enum to string
                .distinct()
                .collect(Collectors.toList());

        int totalExercises = weekPlans.stream()
                .filter(p -> !p.getIsRestDay())
                .mapToInt(this::getExerciseCount)
                .sum();

        return ProgramStructureAnalyticsResponse.WeekSummary.builder()
                .weekNumber(weekNumber)
                .workoutCount(workoutCount)
                .restDayCount(restDayCount)
                .averageIntensity(averageIntensity)
                .phaseTypes(phaseTypes)
                .totalExercises(totalExercises)
                .build();
    }

    private Map<String, ProgramStructureAnalyticsResponse.PhaseSummary> createPhaseSummaries(List<ProgramPlan> programPlans) {
        return programPlans.stream()
                .filter(p -> p.getPhaseType() != null)
                .collect(Collectors.groupingBy(p -> p.getPhaseType().name())) // ✅ FIXED: Convert enum to string
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> createPhaseSummary(entry.getKey(), entry.getValue())
                ));
    }

    private ProgramStructureAnalyticsResponse.PhaseSummary createPhaseSummary(String phaseTypeName, List<ProgramPlan> phasePlans) {
        List<Integer> weekNumbers = phasePlans.stream()
                .map(ProgramPlan::getWeekNumber)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        int workoutCount = (int) phasePlans.stream().filter(p -> !p.getIsRestDay()).count();
        BigDecimal averageIntensity = calculateAverageIntensity(phasePlans);

        return ProgramStructureAnalyticsResponse.PhaseSummary.builder()
                .phaseType(phaseTypeName)
                .weekCount(weekNumbers.size())
                .workoutCount(workoutCount)
                .averageIntensity(averageIntensity)
                .weekNumbers(weekNumbers)
                .build();
    }

    private ProgramStructureAnalyticsResponse.IntensityDistribution createIntensityDistribution(List<ProgramPlan> programPlans) {
        List<ProgramPlan> workoutPlans = programPlans.stream()
                .filter(p -> !p.getIsRestDay() && p.getTargetIntensity() != null)
                .collect(Collectors.toList());

        int lowIntensity = (int) workoutPlans.stream()
                .filter(p -> p.getTargetIntensity().compareTo(BigDecimal.valueOf(30)) <= 0)
                .count();

        int mediumIntensity = (int) workoutPlans.stream()
                .filter(p -> p.getTargetIntensity().compareTo(BigDecimal.valueOf(30)) > 0 &&
                        p.getTargetIntensity().compareTo(BigDecimal.valueOf(70)) <= 0)
                .count();

        int highIntensity = (int) workoutPlans.stream()
                .filter(p -> p.getTargetIntensity().compareTo(BigDecimal.valueOf(70)) > 0)
                .count();

        BigDecimal averageIntensity = calculateAverageIntensity(workoutPlans);

        return ProgramStructureAnalyticsResponse.IntensityDistribution.builder()
                .lowIntensity(lowIntensity)
                .mediumIntensity(mediumIntensity)
                .highIntensity(highIntensity)
                .averageIntensity(averageIntensity)
                .build();
    }

    private BigDecimal calculateAverageIntensity(List<ProgramPlan> programPlans) {
        List<BigDecimal> intensities = programPlans.stream()
                .filter(p -> !p.getIsRestDay() && p.getTargetIntensity() != null)
                .map(ProgramPlan::getTargetIntensity)
                .collect(Collectors.toList());

        if (intensities.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = intensities.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(intensities.size()), 2, RoundingMode.HALF_UP);
    }

    private String getDayName(Integer dayNumber) {
        return switch (dayNumber) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            case 4 -> "Thursday";
            case 5 -> "Friday";
            case 6 -> "Saturday";
            case 7 -> "Sunday";
            default -> "Unknown";
        };
    }

    private String findDominantPhase(List<ProgramPlan> weekPlans) {
        return weekPlans.stream()
                .map(ProgramPlan::getPhaseType)
                .filter(phase -> phase != null)
                .map(ProgramPlan.PhaseType::name)
                .collect(Collectors.groupingBy(phase -> phase, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
    }

    private int getExerciseCount(ProgramPlan programPlan) {
        if (programPlan.getIsRestDay() || programPlan.getWorkoutPlan() == null) {
            return 0;
        }
        return getExerciseCount(programPlan.getWorkoutPlan());
    }

    // ==================== REFLECTION-SAFE WORKOUT PLAN METHODS ====================

    /**
     * Get workout plan name using reflection
     */
    private String getWorkoutPlanName(WorkoutPlan workoutPlan) {
        if (workoutPlan == null) return "";

        String[] possibleMethods = {"getName", "getWorkoutName", "getTitle"};

        for (String methodName : possibleMethods) {
            Object result = invokeMethod(workoutPlan, methodName);
            if (result != null) {
                return result.toString();
            }
        }

        return "Workout Plan"; // Fallback
    }

    /**
     * Get workout plan description using reflection
     */
    private String getWorkoutPlanDescription(WorkoutPlan workoutPlan) {
        if (workoutPlan == null) return "";

        Object result = invokeMethod(workoutPlan, "getDescription");
        return result != null ? result.toString() : "";
    }

    /**
     * Get workout plan estimated duration using reflection
     */
    private Integer getWorkoutPlanEstimatedDuration(WorkoutPlan workoutPlan) {
        if (workoutPlan == null) return 0;

        String[] possibleMethods = {"getEstimatedDuration", "getDuration"};

        for (String methodName : possibleMethods) {
            Object result = invokeMethod(workoutPlan, methodName);
            if (result instanceof Integer) {
                return (Integer) result;
            }
        }

        return 60; // Default 60 minutes
    }

    /**
     * Get workout plan difficulty using reflection
     */
    private String getWorkoutPlanDifficulty(WorkoutPlan workoutPlan) {
        if (workoutPlan == null) return "";

        String[] possibleMethods = {"getDifficulty", "getDifficultyLevel"};

        for (String methodName : possibleMethods) {
            Object result = invokeMethod(workoutPlan, methodName);
            if (result != null) {
                return result.toString();
            }
        }

        return "INTERMEDIATE"; // Fallback
    }

    /**
     * Get exercise count from workout plan using reflection
     */
    private int getExerciseCount(WorkoutPlan workoutPlan) {
        if (workoutPlan == null) {
            return 0;
        }

        // Try to get exercise count from various possible relationship names
        String[] possibleMethods = {"getPlanExercises", "getWorkoutPlanExercises", "getExercises"};

        for (String methodName : possibleMethods) {
            Object result = invokeMethod(workoutPlan, methodName);
            if (result != null) {
                // Check if it's a Collection
                if (result instanceof Collection) {
                    return ((Collection<?>) result).size();
                }
                // Check if it's an array
                if (result.getClass().isArray()) {
                    return ((Object[]) result).length;
                }
            }
        }

        // Try to get exercise count directly
        Object exerciseCount = invokeMethod(workoutPlan, "getExerciseCount");
        if (exerciseCount instanceof Integer) {
            return (Integer) exerciseCount;
        }

        return 0; // Fallback
    }

    /**
     * Get primary muscle groups from workout plan exercises using reflection
     */
    private String getPrimaryMuscleGroups(WorkoutPlan workoutPlan) {
        if (workoutPlan == null) {
            return "";
        }

        // Try to get exercises through various possible relationship names
        String[] possibleMethods = {"getPlanExercises", "getWorkoutPlanExercises", "getExercises"};

        for (String methodName : possibleMethods) {
            Object result = invokeMethod(workoutPlan, methodName);
            if (result instanceof Collection) {
                Collection<?> exercises = (Collection<?>) result;
                if (!exercises.isEmpty()) {
                    List<String> muscleGroups = new ArrayList<>();

                    for (Object exercise : exercises) {
                        String muscleGroup = getExercisePrimaryMuscleGroup(exercise);
                        if (muscleGroup != null && !muscleGroup.trim().isEmpty()) {
                            muscleGroups.add(muscleGroup.trim());
                        }
                    }

                    return muscleGroups.stream()
                            .distinct()
                            .limit(3)
                            .collect(Collectors.joining(", "));
                }
            }
        }

        return "Various"; // Fallback
    }

    /**
     * Get primary muscle group from exercise using reflection
     */
    private String getExercisePrimaryMuscleGroup(Object exercise) {
        if (exercise == null) {
            return "";
        }

        // For PlanExercise objects, try to get the Exercise first
        Object actualExercise = invokeMethod(exercise, "getExercise");
        if (actualExercise != null) {
            exercise = actualExercise;
        }

        // Try different possible method names for primary muscle group
        String[] possibleMethods = {"getPrimaryMuscleGroup", "getMuscleGroup", "getTargetMuscle"};

        for (String methodName : possibleMethods) {
            Object result = invokeMethod(exercise, methodName);
            if (result != null) {
                return result.toString().trim();
            }
        }

        return "General"; // Fallback
    }

    /**
     * Safely invoke a method using reflection
     */
    private Object invokeMethod(Object object, String methodName) {
        if (object == null || methodName == null) {
            return null;
        }

        try {
            Method method = object.getClass().getMethod(methodName);
            return method.invoke(object);
        } catch (Exception e) {
            // Silently fail - this is expected for non-existent methods
            return null;
        }
    }
}