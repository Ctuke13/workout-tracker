package com.chidituke.workout_tracker.service.scheduled_workouts;

import com.chidituke.workout_tracker.dto.request.scheduled_workouts.IndividualExerciseRequest;
import com.chidituke.workout_tracker.dto.request.scheduled_workouts.ScheduledWorkoutRequest;
import com.chidituke.workout_tracker.dto.request.workout_plan.ScheduleMultipleExercisesRequestDTO;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import com.chidituke.workout_tracker.exceptions.scheduled_workout.*;
import com.chidituke.workout_tracker.exceptions.subscription.SubscriptionLimitExceededException;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.exceptions.workout_plan.WorkoutPlanNotFoundException;
import com.chidituke.workout_tracker.mapper.workout.ExerciseMapper;
import com.chidituke.workout_tracker.mapper.workout.ScheduledWorkoutMapper;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.SubscriptionTier;
import com.chidituke.workout_tracker.model.workout.*;
import com.chidituke.workout_tracker.repository.scheduled_workouts.ScheduledWorkoutRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.repository.workout.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for workout scheduling operations, subscription enforcement, and schedule management.
 * Handles all aspects of creating, updating, and managing workout schedules.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkoutSchedulingService {

    private final ScheduledWorkoutRepository scheduledWorkoutRepository;
    private final ScheduledWorkoutQueryService queryService;
    private final UserRepository userRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutProgramRepository workoutProgramRepository;
    private final PlanExerciseRepository planExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final ScheduledWorkoutMapper scheduledWorkoutMapper;
    private final ExerciseMapper exerciseMapper;

    // ==================== INDIVIDUAL EXERCISE SCHEDULING ====================

    /**
     * Schedule an individual exercise by creating a temporary single-exercise workout plan
     */
    public ScheduledWorkoutResponse scheduleIndividualExercise(String username, IndividualExerciseRequest request) {
        try {
            log.info("Scheduling individual exercise {} for user {} on {}",
                    request.getExerciseId(), username, request.getScheduledDate());

            // Validate user exists and exercise exists
            User user = findUserByUsername(username);
            Exercise exercise = findExerciseById(request.getExerciseId());

            exerciseMapper.autoCorrectExerciseModality(exercise);

            if (!exerciseMapper.isExerciseDataComplete(exercise)) {
                log.warn("Exercise {} has incomplete data: {}",
                        exercise.getId(), exerciseMapper.getExerciseSummary(exercise));
            }

            // Check subscription limits before proceeding
            validateIndividualExerciseScheduling(user, request);

            // Create a minimal temporary workout plan containing just this exercise
            WorkoutPlan temporaryPlan = createTemporaryWorkoutPlan(exercise, request);
            WorkoutPlan savedPlan = workoutPlanRepository.save(temporaryPlan);

            log.debug("Created temporary workout plan with ID: {}", savedPlan.getId());

            // Create the scheduled workout entry
            ScheduledWorkout scheduledWorkout = buildScheduledWorkout(user, savedPlan, request);
            scheduledWorkout.setExercise(exercise);

            // Apply exercise-specific configuration based on exercise type
            applyExerciseConfiguration(scheduledWorkout, exercise, request);

            ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

            log.info("Successfully scheduled individual exercise for user {}, ID: {}", username, saved.getId());

            return scheduledWorkoutMapper.toResponse(saved);

        } catch (Exception e) {
            log.error("Failed to schedule individual exercise: {}", e.getMessage(), e);
            if (e instanceof SubscriptionLimitExceededException) {
                throw e;
            } else {
                throw new RuntimeException("Failed to schedule exercise: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Update a scheduled individual exercise
     */
    public ScheduledWorkoutResponse updateScheduledExercise(String username, Long exerciseId,
                                                            IndividualExerciseRequest updates) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(exerciseId);
        validateOwnership(scheduledWorkout, username);

        log.info("Updating scheduled exercise {} for user {}", exerciseId, username);

        // Update the workout details
        if (updates.getScheduledDate() != null) {
            scheduledWorkout.setScheduledDate(updates.getScheduledDate());
        }
        if (updates.getNotes() != null) {
            scheduledWorkout.setCustomNotes(updates.getNotes());
        }

        // Update exercise configuration
        Exercise exercise = findExerciseById(updates.getExerciseId());
        setExerciseConfiguration(scheduledWorkout, exercise, updates);
        scheduledWorkout.setEstimatedDurationMinutes(calculateIndividualExerciseDuration(exercise, updates));

        ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Successfully updated scheduled exercise {} for user {}", exerciseId, username);
        return scheduledWorkoutMapper.toResponse(saved);
    }

    /**
     * Delete a scheduled individual exercise
     */
    @Transactional
    public void deleteScheduledExercise(String username, Long exerciseId) {
        ScheduledWorkout scheduledWorkout = queryService.getById(exerciseId);
        validateOwnership(scheduledWorkout, username);
        validateWorkoutCanBeDeleted(scheduledWorkout);

        log.info("Soft deleting scheduled exercise {} for user {}", exerciseId, username);

        try {
            // Handle cleanup of temporary workout plans BEFORE soft delete
            WorkoutPlan plan = scheduledWorkout.getWorkoutPlan();
            boolean isIndividualPlan = plan != null &&
                    plan.getWorkoutDescription() != null &&
                    plan.getWorkoutDescription().contains("Individual exercise");

            handleRelatedRecordsBeforeDeletion(scheduledWorkout);

            // Soft delete the scheduled workout
            scheduledWorkout.setDeleted(true);
            scheduledWorkout.setDeletedAt(LocalDateTime.now());
            scheduledWorkout.setDeletedBy(username);

            scheduledWorkoutRepository.save(scheduledWorkout);

            // Clean up temporary individual plans (these can be hard deleted)
            if (isIndividualPlan && plan != null) {
                workoutPlanRepository.delete(plan);
                log.debug("Deleted temporary individual workout plan {}", plan.getId());
            }

            log.info("Successfully soft deleted scheduled exercise {} for user {}", exerciseId, username);

        } catch (Exception e) {
            log.error("Failed to delete scheduled exercise {} for user {}: {}",
                    exerciseId, username, e.getMessage());
            throw new RuntimeException("Failed to delete exercise: " + e.getMessage());
        }
    }

    // ==================== WORKOUT PLAN SCHEDULING ====================

    /**
     * Schedule multiple exercises from a workout plan with subscription validation
     */
    public List<ScheduledWorkoutResponse> scheduleWorkoutPlan(String username, ScheduleMultipleExercisesRequestDTO request) {
        User user = findUserByUsername(username);
        WorkoutPlan workoutPlan = findWorkoutPlanById(request.getWorkoutPlanId());

        log.info("Scheduling workout plan: {} for user {} on {}",
                workoutPlan.getWorkoutName(), username, request.getScheduledDate());

        // Get exercises from the workout plan
        List<PlanExercise> planExercises = planExerciseRepository.findByWorkoutPlanOrderByOrderInWorkout(workoutPlan);

        if (planExercises.isEmpty()) {
            throw new IllegalArgumentException("Workout plan has no exercises configured");
        }

        // Validate subscription limits BEFORE scheduling
        validateWorkoutPlanScheduling(user, request, planExercises.size());

        List<ScheduledWorkout> scheduledWorkouts = new ArrayList<>();

        // Determine which exercises to schedule (respect FREE user limits)
        List<PlanExercise> exercisesToSchedule = getExercisesToSchedule(planExercises, request, user);

        log.info("Scheduling {} out of {} exercises for {} user",
                exercisesToSchedule.size(), planExercises.size(), user.getSubscriptionTier());

        // Convert each plan exercise to a scheduled workout
        for (int i = 0; i < exercisesToSchedule.size(); i++) {
            PlanExercise planExercise = exercisesToSchedule.get(i);

            ScheduledWorkout scheduledWorkout = new ScheduledWorkout();
            scheduledWorkout.setUser(user);
            scheduledWorkout.setWorkoutPlan(createIndividualExercisePlan(planExercise));
            scheduledWorkout.setScheduledDate(request.getScheduledDate());
            scheduledWorkout.setCustomNotes(buildExerciseNotes(request.getNotes(), planExercise, i + 1));

            // Set timing based on auto-spacing preference
            if (request.getAutoSpaceWorkouts() != null && request.getAutoSpaceWorkouts()) {
                LocalDateTime scheduledTime = calculateScheduledTime(request, i, exercisesToSchedule.size());
                scheduledWorkout.setReminderTime(scheduledTime);
            } else if (request.getPreferredStartTime() != null) {
                LocalDateTime baseTime = request.getScheduledDate().atTime(request.getPreferredStartTime());
                scheduledWorkout.setReminderTime(baseTime.plusMinutes(i * (request.getSpacingMinutes() != null ? request.getSpacingMinutes() : 15)));
            }

            // Set estimated duration from plan exercise
            scheduledWorkout.setEstimatedDurationMinutes(calculateExerciseDuration(planExercise));

            // Set exercise configuration from plan exercise
            setExerciseConfigurationFromPlan(scheduledWorkout, planExercise);

            scheduledWorkouts.add(scheduledWorkout);
        }

        // Save all scheduled workouts
        List<ScheduledWorkout> saved = scheduledWorkoutRepository.saveAll(scheduledWorkouts);

        log.info("Successfully scheduled {} exercises from workout plan for user {}", saved.size(), username);

        return saved.stream()
                .map(scheduledWorkoutMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Schedule a single workout (individual exercise or workout plan)
     */
    public ScheduledWorkoutResponse scheduleWorkout(String username, ScheduledWorkoutRequest request) {
        User user = findUserByUsername(username);
        WorkoutPlan workoutPlan = findWorkoutPlanById(request.getWorkoutPlanId());

        // Validate scheduling constraints
        validateSchedulingRequest(user, request);

        // Check for conflicts
        checkForSchedulingConflicts(user, request.getScheduledDate(), null);

        // Create scheduled workout
        ScheduledWorkout scheduledWorkout = new ScheduledWorkout();
        scheduledWorkout.setUser(user);
        scheduledWorkout.setWorkoutPlan(workoutPlan);
        scheduledWorkout.setScheduledDate(request.getScheduledDate());
        scheduledWorkout.setCustomNotes(request.getCustomNotes());
        scheduledWorkout.setReminderTime(request.getReminderTime());

        // Set program context if provided
        if (request.getProgramId() != null) {
            WorkoutProgram program = findWorkoutProgramById(request.getProgramId());
            scheduledWorkout.setProgram(program);
            scheduledWorkout.setWeekNumber(request.getWeekNumber());
            scheduledWorkout.setDayOfWeek(request.getScheduledDate().getDayOfWeek().getValue());
        }

        ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Workout scheduled: {} for user {} on {}",
                workoutPlan.getWorkoutName(), username, request.getScheduledDate());

        return scheduledWorkoutMapper.toResponse(saved);
    }

    /**
     * Reschedule an existing workout to a new date
     */
    public ScheduledWorkoutResponse rescheduleWorkout(String username, Long scheduledWorkoutId, LocalDate newDate) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(scheduledWorkoutId);
        validateOwnership(scheduledWorkout, username);

        // Validate new date
        if (!scheduledWorkout.canSchedule(scheduledWorkout.getUser(), newDate)) {
            throw new SchedulingConstraintException("Cannot schedule workout that far in advance");
        }

        // Check for conflicts on new date
        checkForSchedulingConflicts(scheduledWorkout.getUser(), newDate, scheduledWorkoutId);

        // Reschedule
        scheduledWorkout.reschedule(newDate);
        ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Workout rescheduled: {} moved to {} for user {}", scheduledWorkoutId, newDate, username);

        return scheduledWorkoutMapper.toResponse(saved);
    }

    // ==================== PROGRAM SCHEDULING ====================

    /**
     * Schedule an entire workout program
     */
    public List<ScheduledWorkoutResponse> scheduleProgram(String username, Long programId, LocalDate startDate) {
        User user = findUserByUsername(username);
        WorkoutProgram program = findWorkoutProgramById(programId);

        validateProgramScheduling(user, program, startDate);

        List<ScheduledWorkout> scheduledWorkouts = createProgramSchedule(user, program, startDate);
        List<ScheduledWorkout> savedWorkouts = scheduledWorkoutRepository.saveAll(scheduledWorkouts);

        log.info("Program scheduled: {} starting {} for user {}", program.getName(), startDate, username);

        return scheduledWorkoutMapper.toResponseList(savedWorkouts);
    }

    // ==================== SUBSCRIPTION ENFORCEMENT & VALIDATION ====================

    private void validateIndividualExerciseScheduling(User user, IndividualExerciseRequest request) {
        if (request.getScheduledDate().isBefore(LocalDate.now())) {
            throw new SchedulingConstraintException("Cannot schedule exercises in the past");
        }

        if (!canScheduleForDate(user, request.getScheduledDate())) {
            long daysOut = ChronoUnit.DAYS.between(LocalDate.now(), request.getScheduledDate());
            throw new SubscriptionLimitExceededException(
                    String.format("Free tier limited to 7 days. Upgrade to schedule %d days out", daysOut));
        }

        long currentExercisesOnDate = scheduledWorkoutRepository.countByUserAndScheduledDate(user, request.getScheduledDate());
        int dailyLimit = getDailySchedulingLimit(user);

        if (currentExercisesOnDate >= dailyLimit) {
            if (user.getSubscriptionTier() == SubscriptionTier.FREE) {
                throw new SubscriptionLimitExceededException(String.format(
                        "FREE users limited to %d exercises per day. Upgrade to PLUS for unlimited scheduling!",
                        dailyLimit));
            } else {
                throw new SchedulingConstraintException("Daily scheduling limit reached");
            }
        }
    }

    private void validateWorkoutPlanScheduling(User user, ScheduleMultipleExercisesRequestDTO request, int totalExercises) {
        if (request.getScheduledDate().isBefore(LocalDate.now())) {
            throw new SchedulingConstraintException("Cannot schedule workouts in the past");
        }

        if (!canScheduleForDate(user, request.getScheduledDate())) {
            long daysOut = ChronoUnit.DAYS.between(LocalDate.now(), request.getScheduledDate());
            throw new SubscriptionLimitExceededException(
                    String.format("Free tier limited to 7 days. Upgrade to schedule %d days out", daysOut));
        }

        long currentExercisesOnDate = scheduledWorkoutRepository.countByUserAndScheduledDate(user, request.getScheduledDate());
        int dailyLimit = getDailySchedulingLimit(user);

        if (currentExercisesOnDate + totalExercises > dailyLimit) {
            if (user.getSubscriptionTier() == SubscriptionTier.FREE) {
                throw new SubscriptionLimitExceededException(String.format(
                        "FREE users limited to %d exercises per day. This workout has %d exercises. " +
                                "Upgrade to PLUS to schedule complete workout plans!",
                        dailyLimit, totalExercises));
            } else {
                throw new SchedulingConstraintException("Daily scheduling limit reached");
            }
        }
    }

    private void validateSchedulingRequest(User user, ScheduledWorkoutRequest request) {
        if (request.getScheduledDate().isBefore(LocalDate.now())) {
            throw new SchedulingConstraintException("Cannot schedule workouts in the past");
        }

        if (!canScheduleForDate(user, request.getScheduledDate())) {
            long daysOut = ChronoUnit.DAYS.between(LocalDate.now(), request.getScheduledDate());
            throw new SubscriptionLimitExceededException(
                    String.format("Free tier limited to 7 days. Upgrade to schedule %d days out", daysOut));
        }

        List<ScheduledWorkout> existingOnDate = scheduledWorkoutRepository
                .findByUserAndScheduledDateOrderByCreatedAtAsc(user, request.getScheduledDate());

        if (existingOnDate.size() >= getDailySchedulingLimit(user)) {
            throw new SchedulingConstraintException("Daily scheduling limit reached");
        }
    }

    private boolean canScheduleForDate(User user, LocalDate date) {
        return new ScheduledWorkout().canSchedule(user, date);
    }

    private int getDailySchedulingLimit(User user) {
        return switch (user.getSubscriptionTier()) {
            case FREE -> 3;
            case PLUS -> 8;
            case PRO -> 15;
            default -> 3;
        };
    }

    // ==================== WORKOUT PLAN CREATION HELPERS ====================

    private WorkoutPlan createTemporaryWorkoutPlan(Exercise exercise, IndividualExerciseRequest request) {
        WorkoutPlan temporaryPlan = new WorkoutPlan();
        temporaryPlan.setWorkoutName(exercise.getExerciseName());
        temporaryPlan.setWorkoutDescription("Individual exercise from workout plan");
        temporaryPlan.setDifficultyLevel(WorkoutPlan.DifficultyLevel.INTERMEDIATE);
        temporaryPlan.setEstimatedDurationMinutes(calculateIndividualExerciseDuration(exercise, request));

        // Set category based on exercise
        if (exercise.getTargetMuscleGroups() != null && !exercise.getTargetMuscleGroups().isEmpty()) {
            temporaryPlan.setWorkoutCategory(exercise.getTargetMuscleGroups().get(0));
        } else if (exercise.getExerciseType() != null) {
            temporaryPlan.setWorkoutCategory(exercise.getExerciseType().getDisplayName());
        } else {
            temporaryPlan.setWorkoutCategory("General");
        }

        temporaryPlan.setSubscriptionTierRequired("FREE");
        temporaryPlan.setIsPublic(false);

        return temporaryPlan;
    }

    private WorkoutPlan createIndividualExercisePlan(PlanExercise planExercise) {
        WorkoutPlan individualPlan = new WorkoutPlan();
        individualPlan.setWorkoutName(planExercise.getExercise().getExerciseName());
        individualPlan.setWorkoutDescription("Individual exercise from workout plan");
        individualPlan.setDifficultyLevel(WorkoutPlan.DifficultyLevel.INTERMEDIATE);
        individualPlan.setEstimatedDurationMinutes(calculateExerciseDuration(planExercise));

        Exercise exercise = planExercise.getExercise();

        if (exercise.getTargetMuscleGroups() != null && !exercise.getTargetMuscleGroups().isEmpty()) {
            individualPlan.setWorkoutCategory(exercise.getTargetMuscleGroups().get(0));
        } else if (exercise.getExerciseType() != null) {
            individualPlan.setWorkoutCategory(exercise.getExerciseType().getDisplayName());
        } else {
            individualPlan.setWorkoutCategory("General");
        }

        individualPlan.setSubscriptionTierRequired("FREE");
        individualPlan.setIsPublic(false);

        return individualPlan;
    }

    // ==================== EXERCISE CONFIGURATION HELPERS ====================

    private ScheduledWorkout buildScheduledWorkout(User user, WorkoutPlan workoutPlan, IndividualExerciseRequest request) {
        ScheduledWorkout scheduledWorkout = new ScheduledWorkout();
        scheduledWorkout.setUser(user);
        scheduledWorkout.setWorkoutPlan(workoutPlan);
        scheduledWorkout.setScheduledDate(request.getScheduledDate());
        scheduledWorkout.setCustomNotes(request.getNotes());
        scheduledWorkout.setEstimatedDurationMinutes(workoutPlan.getEstimatedDurationMinutes());

        return scheduledWorkout;
    }

    private void applyExerciseConfiguration(ScheduledWorkout scheduledWorkout, Exercise exercise,
                                            IndividualExerciseRequest request) {
        try {
            log.debug("Applying configuration for exercise: {} (cardio: {}, isometric: {})",
                    exercise.getExerciseName(), exercise.getIsCardio(), exercise.getIsIsometric());

            if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
                configureCardioExercise(scheduledWorkout, request);
            } else if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
                configureIsometricExercise(scheduledWorkout, request);
            } else {
                configureStrengthExercise(scheduledWorkout, request);
            }

        } catch (Exception e) {
            log.warn("Error applying exercise configuration, using defaults: {}", e.getMessage());
            applySafeDefaults(scheduledWorkout);
        }
    }

    private void configureCardioExercise(ScheduledWorkout scheduledWorkout, IndividualExerciseRequest request) {
        scheduledWorkout.setTargetDurationMinutes(
                request.getTargetDurationMinutes() != null ? request.getTargetDurationMinutes() : 20);
        scheduledWorkout.setTargetDistanceKm(request.getTargetDistanceKm());
        scheduledWorkout.setTargetPace(request.getTargetPace());

        log.debug("Configured cardio: duration={}min, distance={}km, pace={}",
                scheduledWorkout.getTargetDurationMinutes(),
                scheduledWorkout.getTargetDistanceKm(),
                scheduledWorkout.getTargetPace());
    }

    private void configureIsometricExercise(ScheduledWorkout scheduledWorkout, IndividualExerciseRequest request) {
        scheduledWorkout.setTargetSets(request.getSets() != null ? request.getSets() : 3);
        scheduledWorkout.setHoldDurationSeconds(
                request.getHoldDurationSeconds() != null ? request.getHoldDurationSeconds() : 30);
        scheduledWorkout.setRestSeconds(request.getRestSeconds() != null ? request.getRestSeconds() : 60);

        log.debug("Configured isometric: sets={}, hold={}s, rest={}s",
                scheduledWorkout.getTargetSets(),
                scheduledWorkout.getHoldDurationSeconds(),
                scheduledWorkout.getRestSeconds());
    }

    private void configureStrengthExercise(ScheduledWorkout scheduledWorkout, IndividualExerciseRequest request) {
        scheduledWorkout.setTargetSets(request.getSets() != null ? request.getSets() : 3);
        scheduledWorkout.setTargetReps(request.getReps() != null ? request.getReps() : "10");
        scheduledWorkout.setTargetWeight(request.getWeight());
        scheduledWorkout.setTargetWeightUnit("lbs");
        scheduledWorkout.setRestSeconds(request.getRestSeconds() != null ? request.getRestSeconds() : 90);
        scheduledWorkout.setTempo(request.getTempo());
        scheduledWorkout.setTargetRpe(request.getTargetRpe());

        log.debug("Configured strength: sets={}, reps={}, weight={}lbs, rest={}s",
                scheduledWorkout.getTargetSets(),
                scheduledWorkout.getTargetReps(),
                scheduledWorkout.getTargetWeight(),
                scheduledWorkout.getRestSeconds());
    }

    private void setExerciseConfiguration(ScheduledWorkout scheduledWorkout, Exercise exercise,
                                          IndividualExerciseRequest request) {
        if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
            scheduledWorkout.setTargetDurationMinutes(request.getTargetDurationMinutes());
            scheduledWorkout.setTargetDistanceKm(request.getTargetDistanceKm());
            scheduledWorkout.setTargetPace(request.getTargetPace());
        } else if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
            scheduledWorkout.setTargetSets(request.getSets());
            scheduledWorkout.setHoldDurationSeconds(request.getHoldDurationSeconds());
            scheduledWorkout.setRestSeconds(60);
        } else {
            scheduledWorkout.setTargetSets(request.getSets());
            scheduledWorkout.setTargetReps(request.getReps());
            scheduledWorkout.setTargetWeight(request.getWeight());
            scheduledWorkout.setTargetWeightUnit("lbs");
            scheduledWorkout.setRestSeconds(request.getRestSeconds());
            scheduledWorkout.setTempo(request.getTempo());
            scheduledWorkout.setTargetRpe(request.getTargetRpe());
        }
    }

    private void setExerciseConfigurationFromPlan(ScheduledWorkout scheduledWorkout, PlanExercise planExercise) {
        Exercise exercise = planExercise.getExercise();

        if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
            scheduledWorkout.setTargetDurationMinutes(planExercise.getPrescribedSets());
            scheduledWorkout.setTargetDistanceKm(planExercise.getPrescribedWeightPercent());
            scheduledWorkout.setTargetPace(planExercise.getPrescribedRpe() != null ? planExercise.getPrescribedRpe().doubleValue() : null);
        } else if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
            scheduledWorkout.setTargetSets(planExercise.getPrescribedSets());
            scheduledWorkout.setHoldDurationSeconds(planExercise.getPrescribedRestSeconds());
            scheduledWorkout.setRestSeconds(60);
        } else {
            scheduledWorkout.setTargetSets(planExercise.getPrescribedSets());
            scheduledWorkout.setTargetReps(planExercise.getPrescribedReps() != null ? planExercise.getPrescribedReps().toString() : null);
            scheduledWorkout.setTargetWeight(planExercise.getPrescribedWeightPercent());
            scheduledWorkout.setTargetWeightUnit("lbs");
            scheduledWorkout.setRestSeconds(planExercise.getPrescribedRestSeconds());
            scheduledWorkout.setTempo(planExercise.getPrescribedTempo());
            scheduledWorkout.setTargetRpe(planExercise.getPrescribedRpe() != null ? planExercise.getPrescribedRpe().intValue() : null);
        }
    }

    private void applySafeDefaults(ScheduledWorkout scheduledWorkout) {
        scheduledWorkout.setTargetSets(3);
        scheduledWorkout.setTargetReps("10");
        scheduledWorkout.setRestSeconds(90);
        scheduledWorkout.setTargetWeightUnit("lbs");
    }

    // ==================== CALCULATION HELPERS ====================

    private Integer calculateIndividualExerciseDuration(Exercise exercise, IndividualExerciseRequest request) {
        if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
            return request.getTargetDurationMinutes() != null ? request.getTargetDurationMinutes() : 30;
        } else if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
            Integer sets = request.getSets() != null ? request.getSets() : 3;
            Integer holdDuration = request.getHoldDurationSeconds() != null ? request.getHoldDurationSeconds() : 30;
            return sets * (holdDuration + 60) / 60;
        } else {
            Integer sets = request.getSets() != null ? request.getSets() : 3;
            Integer restSeconds = request.getRestSeconds() != null ? request.getRestSeconds() : 90;
            int workTime = sets * 45;
            int restTime = (sets - 1) * restSeconds;
            return (workTime + restTime) / 60;
        }
    }

    private Integer calculateExerciseDuration(PlanExercise planExercise) {
        Exercise exercise = planExercise.getExercise();

        if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
            return planExercise.getPrescribedSets() != null ? planExercise.getPrescribedSets() : 30;
        } else if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
            Integer sets = planExercise.getPrescribedSets();
            Integer holdDuration = planExercise.getPrescribedRestSeconds();
            if (sets != null && holdDuration != null) {
                return sets * (holdDuration + 60) / 60;
            }
            return 10;
        } else {
            Integer sets = planExercise.getPrescribedSets();
            Integer restSeconds = planExercise.getPrescribedRestSeconds();
            if (sets != null) {
                int workTime = sets * 45;
                int restTime = restSeconds != null ? (sets - 1) * restSeconds : (sets - 1) * 90;
                return (workTime + restTime) / 60;
            }
            return 15;
        }
    }

    // ==================== WORKOUT PLAN SCHEDULING HELPERS ====================

    private List<PlanExercise> getExercisesToSchedule(List<PlanExercise> allExercises,
                                                      ScheduleMultipleExercisesRequestDTO request,
                                                      User user) {
        if (request.getExerciseIdsToSchedule() != null && !request.getExerciseIdsToSchedule().isEmpty()) {
            return allExercises.stream()
                    .filter(pe -> request.getExerciseIdsToSchedule().contains(pe.getExercise().getId()))
                    .collect(Collectors.toList());
        }

        if (user.getSubscriptionTier() == SubscriptionTier.FREE) {
            long currentScheduledToday = scheduledWorkoutRepository.countByUserAndScheduledDate(user, request.getScheduledDate());
            int remainingLimit = Math.max(0, (int) (getDailySchedulingLimit(user) - currentScheduledToday));

            if (remainingLimit == 0) {
                throw new SubscriptionLimitExceededException("Daily exercise limit reached. Upgrade to PLUS for unlimited scheduling!");
            }

            return allExercises.stream()
                    .limit(remainingLimit)
                    .collect(Collectors.toList());
        }

        return allExercises;
    }

    private String buildExerciseNotes(String baseNotes, PlanExercise planExercise, int exerciseNumber) {
        StringBuilder notes = new StringBuilder();

        if (baseNotes != null && !baseNotes.trim().isEmpty()) {
            notes.append(baseNotes);
        }

        Exercise exercise = planExercise.getExercise();
        if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
            notes.append(notes.length() > 0 ? " | " : "").append("Cardio: ");
            if (planExercise.getPrescribedSets() != null) {
                notes.append(planExercise.getPrescribedSets()).append(" min");
            }
        } else if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
            notes.append(notes.length() > 0 ? " | " : "").append("Isometric: ");
            if (planExercise.getPrescribedSets() != null) {
                notes.append(planExercise.getPrescribedSets()).append(" sets");
            }
        } else {
            if (planExercise.getPrescribedSets() != null && planExercise.getPrescribedReps() != null) {
                notes.append(notes.length() > 0 ? " | " : "")
                        .append(planExercise.getPrescribedSets()).append("x").append(planExercise.getPrescribedReps());
            }
        }

        return notes.toString();
    }

    private LocalDateTime calculateScheduledTime(ScheduleMultipleExercisesRequestDTO request, int exerciseIndex, int totalExercises) {
        LocalDateTime baseTime;

        if (request.getPreferredStartTime() != null) {
            baseTime = request.getScheduledDate().atTime(request.getPreferredStartTime());
        } else {
            baseTime = request.getScheduledDate().atTime(9, 0);
        }

        int spacingMinutes = request.getSpacingMinutes() != null ? request.getSpacingMinutes() :
                (totalExercises > 1 ? (8 * 60) / (totalExercises - 1) : 0);

        return baseTime.plusMinutes(exerciseIndex * spacingMinutes);
    }

    // ==================== PROGRAM SCHEDULING HELPERS ====================

    private void validateProgramScheduling(User user, WorkoutProgram program, LocalDate startDate) {
        LocalDate programEndDate = startDate.plusWeeks(program.getDurationWeeks());

        if (!canScheduleForDate(user, programEndDate)) {
            throw new SubscriptionLimitExceededException(
                    "Cannot schedule full program - upgrade for longer scheduling horizon");
        }
    }

    private List<ScheduledWorkout> createProgramSchedule(User user, WorkoutProgram program, LocalDate startDate) {
        List<ScheduledWorkout> scheduledWorkouts = new ArrayList<>();

        for (int week = 1; week <= program.getDurationWeeks(); week++) {
            LocalDate weekStart = startDate.plusWeeks(week - 1);

            for (int dayOffset : Arrays.asList(0, 2, 4)) {
                LocalDate workoutDate = weekStart.plusDays(dayOffset);

                ScheduledWorkout scheduled = new ScheduledWorkout();
                scheduled.setUser(user);
                scheduled.setProgram(program);
                scheduled.setWeekNumber(week);
                scheduled.setDayOfWeek(workoutDate.getDayOfWeek().getValue());
                scheduled.setScheduledDate(workoutDate);

                scheduledWorkouts.add(scheduled);
            }
        }

        return scheduledWorkouts;
    }

    // ==================== CONFLICT DETECTION & VALIDATION ====================

    private void checkForSchedulingConflicts(User user, LocalDate date, Long excludeId) {
        Long excludeIdSafe = excludeId != null ? excludeId : -1L;

        List<ScheduledWorkout> conflicts = scheduledWorkoutRepository
                .findSchedulingConflicts(user, date, excludeIdSafe);

        if (!conflicts.isEmpty()) {
            log.warn("Scheduling conflict detected: user {} already has {} workout(s) on {}",
                    user.getUsername(), conflicts.size(), date);
        }
    }

    private void validateWorkoutCanBeDeleted(ScheduledWorkout scheduledWorkout) {
        ScheduledWorkout.ScheduleStatus status = scheduledWorkout.getStatus();

        switch (status) {
            case SCHEDULED:
            case CANCELLED:
                log.debug("Allowing deletion of {} workout {}", status, scheduledWorkout.getId());
                break;
            case IN_PROGRESS:
                throw new WorkoutInProgressException(scheduledWorkout.getId());
            case COMPLETED:
                throw new InvalidWorkoutStateException("delete", "COMPLETED", "SCHEDULED or CANCELLED");
            default:
                throw new InvalidWorkoutStateException("delete", status.toString(), "SCHEDULED or CANCELLED");
        }
    }

    private void handleRelatedRecordsBeforeDeletion(ScheduledWorkout scheduledWorkout) {
        if (scheduledWorkout.getCompletedSession() != null) {
            log.debug("Scheduled workout {} has a completed session, handling cleanup",
                    scheduledWorkout.getId());
            scheduledWorkout.setCompletedSession(null);
            scheduledWorkoutRepository.save(scheduledWorkout);
        }
    }

    // ==================== ENTITY LOOKUP HELPERS ====================

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    private WorkoutPlan findWorkoutPlanById(Long id) {
        return workoutPlanRepository.findById(id)
                .orElseThrow(() -> new WorkoutPlanNotFoundException(id));
    }

    private Exercise findExerciseById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercise not found: " + id));
    }

    private WorkoutProgram findWorkoutProgramById(Long id) {
        return workoutProgramRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout program not found: " + id));
    }

    private ScheduledWorkout findScheduledWorkoutById(Long id) {
        return scheduledWorkoutRepository.findById(id)
                .orElseThrow(() -> new ScheduledWorkoutNotFoundException(id));
    }

    private void validateOwnership(ScheduledWorkout scheduledWorkout, String username) {
        if (!scheduledWorkout.getUser().getUsername().equals(username)) {
            throw new UnauthorizedScheduledWorkoutAccessException(
                    "User does not have access to this scheduled workout");
        }
    }
}