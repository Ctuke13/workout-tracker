package com.chidituke.workout_tracker.service.calendar;

import com.chidituke.workout_tracker.controller.calendar.CalendarController;
import com.chidituke.workout_tracker.dto.request.scheduled_workouts.ScheduledWorkoutRequest;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.*;
import com.chidituke.workout_tracker.model.workout.ScheduledWorkout.ScheduleStatus;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.repository.workout.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final ScheduledWorkoutRepository scheduledWorkoutRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final PlanExerciseRepository planExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final UserRepository userRepository;

    // =============================================================================
    // MAIN CALENDAR OPERATIONS
    // =============================================================================

    @Transactional(readOnly = true)
    public List<ScheduledWorkoutResponse> getScheduledExercisesForDateRange(String username, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting scheduled exercises for user {} from {} to {}", username, startDate, endDate);

        User user = getUserByUsername(username);
        List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startDate, endDate);

        return workouts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduledWorkoutResponse scheduleIndividualExercise(String username, CalendarController.IndividualExerciseRequest request) {
        log.debug("Scheduling exercise {} for user {} on {}", request.getExerciseId(), username, request.getScheduledDate());

        User user = getUserByUsername(username);
        Exercise exercise = getExerciseById(request.getExerciseId());

        // Create a single-exercise workout plan
        WorkoutPlan workoutPlan = createIndividualExerciseWorkoutPlan(exercise, user);
        workoutPlan = workoutPlanRepository.save(workoutPlan);

        // Create the plan exercise with configuration
        PlanExercise planExercise = createPlanExercise(workoutPlan, exercise, request);
        planExercise = planExerciseRepository.save(planExercise);

        // Create the scheduled workout
        ScheduledWorkout scheduledWorkout = createScheduledWorkout(user, workoutPlan, request.getScheduledDate());
        scheduledWorkout = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Successfully scheduled exercise {} for user {} on {}", exercise.getExerciseName(), username, request.getScheduledDate());
        return convertToResponse(scheduledWorkout);
    }

    @Transactional
    public ScheduledWorkoutResponse updateScheduledExercise(String username, String exerciseId, CalendarController.IndividualExerciseRequest updates) {
        log.debug("Updating scheduled exercise {} for user {}", exerciseId, username);

        User user = getUserByUsername(username);
        ScheduledWorkout scheduledWorkout = getScheduledWorkoutById(Long.valueOf(exerciseId), user);

        // Update scheduled date if provided
        if (updates.getScheduledDate() != null) {
            scheduledWorkout.setScheduledDate(updates.getScheduledDate());
        }

        // ✅ FIXED: Update custom notes directly on ScheduledWorkout
        if (updates.getNotes() != null) {
            scheduledWorkout.setCustomNotes(updates.getNotes());
        }

        // Update exercise configuration in PlanExercise
        List<PlanExercise> planExercises = planExerciseRepository.findByWorkoutPlanOrderByOrderInWorkout(scheduledWorkout.getWorkoutPlan());
        if (!planExercises.isEmpty()) {
            PlanExercise planExercise = planExercises.get(0); // Single exercise
            updatePlanExerciseConfiguration(planExercise, updates);
            planExerciseRepository.save(planExercise);
        }

        scheduledWorkout = scheduledWorkoutRepository.save(scheduledWorkout);
        log.info("Successfully updated scheduled exercise {}", exerciseId);
        return convertToResponse(scheduledWorkout);
    }

    @Transactional
    public void deleteScheduledExercise(String username, String exerciseId) {
        log.debug("Deleting scheduled exercise {} for user {}", exerciseId, username);

        User user = getUserByUsername(username);
        ScheduledWorkout scheduledWorkout = getScheduledWorkoutById(Long.valueOf(exerciseId), user);

        // Clean up the temporary workout plan if it was created for individual exercise
        WorkoutPlan workoutPlan = scheduledWorkout.getWorkoutPlan();
        boolean shouldDeletePlan = isTemporaryIndividualExercisePlan(workoutPlan);

        // Delete the scheduled workout
        scheduledWorkoutRepository.delete(scheduledWorkout);

        // Delete temporary workout plan and its exercises if applicable
        if (shouldDeletePlan) {
            planExerciseRepository.deleteByWorkoutPlan(workoutPlan);
            workoutPlanRepository.delete(workoutPlan);
        }

        log.info("Successfully deleted scheduled exercise {}", exerciseId);
    }

    @Transactional(readOnly = true)
    public List<ScheduledWorkoutResponse> getExercisesForDate(String username, LocalDate date) {
        log.debug("Getting exercises for user {} on {}", username, date);

        User user = getUserByUsername(username);
        List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateOrderByCreatedAtAsc(user, date);

        return workouts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduledWorkoutResponse markExerciseCompleted(String username, String exerciseId) {
        log.debug("Marking exercise {} as completed for user {}", exerciseId, username);

        User user = getUserByUsername(username);
        ScheduledWorkout scheduledWorkout = getScheduledWorkoutById(Long.valueOf(exerciseId), user);

        // Update status and completion time
        scheduledWorkout.setStatus(ScheduleStatus.COMPLETED);
        scheduledWorkout.setCompletedAt(LocalDateTime.now());
        scheduledWorkout = scheduledWorkoutRepository.save(scheduledWorkout);

        // Create workout session
        createWorkoutSessionForCompleted(user, scheduledWorkout);

        // Update user statistics
        user.incrementWorkoutCount();
        userRepository.save(user);

        log.info("Successfully marked exercise {} as completed", exerciseId);
        return convertToResponse(scheduledWorkout);
    }

    @Transactional
    public List<ScheduledWorkoutResponse> markMultipleExercisesCompleted(String username, List<String> exerciseIds) {
        log.debug("Marking {} exercises as completed for user {}", exerciseIds.size(), username);

        User user = getUserByUsername(username);
        return exerciseIds.stream()
                .map(id -> markExerciseCompleted(username, id))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CalendarController.WorkoutStatsResponse getWorkoutStats(String username, LocalDate specificDate) {
        log.debug("Getting workout stats for user {} (date: {})", username, specificDate);

        User user = getUserByUsername(username);
        LocalDate referenceDate = specificDate != null ? specificDate : LocalDate.now();

        return CalendarController.WorkoutStatsResponse.builder()
                .exercisesScheduledToday(getExercisesScheduledForDate(user, referenceDate))
                .exercisesCompletedToday(getExercisesCompletedForDate(user, referenceDate))
                .minutesWorkedOutToday(getMinutesWorkedOutForDate(user, referenceDate))
                .exercisesScheduledThisWeek(getExercisesScheduledForWeek(user, referenceDate))
                .exercisesCompletedThisWeek(getExercisesCompletedForWeek(user, referenceDate))
                .minutesWorkedOutThisWeek(getMinutesWorkedOutForWeek(user, referenceDate))
                .exercisesScheduledThisMonth(getExercisesScheduledForMonth(user, referenceDate))
                .exercisesCompletedThisMonth(getExercisesCompletedForMonth(user, referenceDate))
                .minutesWorkedOutThisMonth(getMinutesWorkedOutForMonth(user, referenceDate))
                .completionRateThisWeek(calculateCompletionRateForWeek(user, referenceDate))
                .completionRateThisMonth(calculateCompletionRateForMonth(user, referenceDate))
                .lastWorkoutDate(getLastWorkoutDate(user))
                .lastWorkoutType(getLastWorkoutType(user))
                .totalWorkoutsCompleted(getTotalWorkoutsCompleted(user))
                .totalMinutesWorkedOut(getTotalMinutesWorkedOut(user))
                .favoriteExerciseType(getFavoriteExerciseType(user))
                .build();
    }

    @Transactional
    public ScheduledWorkoutResponse scheduleWorkoutPlan(String username, ScheduledWorkoutRequest request) {
        log.debug("Scheduling workout plan {} for user {} on {}", request.getWorkoutPlanId(), username, request.getScheduledDate());

        User user = getUserByUsername(username);
        WorkoutPlan workoutPlan = getWorkoutPlanById(request.getWorkoutPlanId());

        ScheduledWorkout scheduledWorkout = new ScheduledWorkout();
        scheduledWorkout.setUser(user);
        scheduledWorkout.setWorkoutPlan(workoutPlan);
        scheduledWorkout.setScheduledDate(request.getScheduledDate());
        scheduledWorkout.setStatus(ScheduleStatus.SCHEDULED);
        scheduledWorkout.setCreatedAt(LocalDateTime.now());
        scheduledWorkout.setUpdatedAt(LocalDateTime.now());

        scheduledWorkout = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Successfully scheduled workout plan {} for user {} on {}", workoutPlan.getWorkoutName(), username, request.getScheduledDate());
        return convertToResponse(scheduledWorkout);
    }

    // =============================================================================
    // ENTITY RETRIEVAL HELPER METHODS
    // =============================================================================

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private Exercise getExerciseById(Long exerciseId) {
        return exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found: " + exerciseId));
    }

    private WorkoutPlan getWorkoutPlanById(Long workoutPlanId) {
        return workoutPlanRepository.findById(workoutPlanId)
                .orElseThrow(() -> new RuntimeException("Workout plan not found: " + workoutPlanId));
    }

    private ScheduledWorkout getScheduledWorkoutById(Long id, User user) {
        return scheduledWorkoutRepository.findById(id)
                .filter(workout -> workout.getUser().equals(user))
                .orElseThrow(() -> new RuntimeException("Scheduled workout not found: " + id));
    }

    // =============================================================================
    // WORKOUT PLAN AND EXERCISE CREATION METHODS
    // =============================================================================

    private WorkoutPlan createIndividualExerciseWorkoutPlan(Exercise exercise, User user) {
        WorkoutPlan workoutPlan = new WorkoutPlan();
        workoutPlan.setWorkoutName("Individual: " + exercise.getExerciseName());
        workoutPlan.setWorkoutDescription("Individual exercise session");
        workoutPlan.setWorkoutCategory("Individual");
        workoutPlan.setWorkoutImageUrl(null);
        workoutPlan.setCardio(exercise.getIsCardio());
        workoutPlan.setWorkoutType(WorkoutPlan.WorkoutType.STRENGTH);
        workoutPlan.setEstimatedDurationMinutes(exercise.getEstimatedDurationMinutes());

        // Convert Exercise.DifficultyLevel to WorkoutPlan.DifficultyLevel
        if (exercise.getDifficultyLevel() != null) {
            switch (exercise.getDifficultyLevel()) {
                case BEGINNER -> workoutPlan.setDifficultyLevel(WorkoutPlan.DifficultyLevel.BEGINNER);
                case INTERMEDIATE -> workoutPlan.setDifficultyLevel(WorkoutPlan.DifficultyLevel.INTERMEDIATE);
                case ADVANCED -> workoutPlan.setDifficultyLevel(WorkoutPlan.DifficultyLevel.ADVANCED);
            }
        }

        workoutPlan.setTargetMuscleGroups(String.join(",", exercise.getTargetMuscleGroupsList()));
        workoutPlan.setEquipmentNeeded(String.join(",", exercise.getEquipmentRequired()));
        workoutPlan.setSubscriptionTierRequired("FREE");
        workoutPlan.setIsPublic(false);
        workoutPlan.setCreatedByUserId(user.getId());
        workoutPlan.setCreatedAt(LocalDateTime.now());
        workoutPlan.setUpdatedAt(LocalDateTime.now());

        return workoutPlan;
    }

    private PlanExercise createPlanExercise(WorkoutPlan workoutPlan, Exercise exercise, CalendarController.IndividualExerciseRequest request) {
        PlanExercise planExercise = new PlanExercise();
        planExercise.setWorkoutPlan(workoutPlan);
        planExercise.setExercise(exercise);
        planExercise.setOrderInWorkout(1);

        // Configure based on exercise type
        if (exercise.getIsCardio()) {
            configureCardioExercise(planExercise, request);
        } else if (exercise.getIsIsometric()) {
            configureIsometricExercise(planExercise, request);
        } else {
            configureStrengthExercise(planExercise, request);
        }

        planExercise.setInstructions(request.getNotes());
        return planExercise;
    }

    private ScheduledWorkout createScheduledWorkout(User user, WorkoutPlan workoutPlan, LocalDate scheduledDate) {
        ScheduledWorkout scheduledWorkout = new ScheduledWorkout();
        scheduledWorkout.setUser(user);
        scheduledWorkout.setWorkoutPlan(workoutPlan);
        scheduledWorkout.setScheduledDate(scheduledDate);
        scheduledWorkout.setStatus(ScheduleStatus.SCHEDULED);
        scheduledWorkout.setCreatedAt(LocalDateTime.now());
        scheduledWorkout.setUpdatedAt(LocalDateTime.now());

        // Set estimated duration from the workout plan
        if (workoutPlan.getEstimatedDurationMinutes() != null) {
            scheduledWorkout.setEstimatedDurationMinutes(workoutPlan.getEstimatedDurationMinutes());
        }

        return scheduledWorkout;
    }

    // =============================================================================
    // EXERCISE CONFIGURATION METHODS
    // =============================================================================

    private void configureStrengthExercise(PlanExercise planExercise, CalendarController.IndividualExerciseRequest request) {
        planExercise.setPrescribedSets(request.getSets() != null ? request.getSets() : 3);
        planExercise.setPrescribedReps(request.getReps() != null ? request.getReps() : "8-12");
        if (request.getWeight() != null) {
            planExercise.setPrescribedWeightPercent(request.getWeight()); // Store weight as percentage for now
        }
        planExercise.setPrescribedRestSeconds(request.getRestSeconds() != null ? request.getRestSeconds() : 60);
        planExercise.setPrescribedTempo(request.getTempo());
        planExercise.setPrescribedRpe(request.getTargetRpe());
    }

    private void configureCardioExercise(PlanExercise planExercise, CalendarController.IndividualExerciseRequest request) {
        // Store duration in prescribedSets field creatively (in minutes)
        if (request.getTargetDurationMinutes() != null) {
            planExercise.setPrescribedSets(request.getTargetDurationMinutes());
        } else {
            planExercise.setPrescribedSets(20); // Default 20 minutes
        }

        // Store distance in prescribedWeightPercent field creatively
        if (request.getTargetDistanceKm() != null) {
            planExercise.setPrescribedWeightPercent(request.getTargetDistanceKm());
        }

        // Store pace in prescribedRpe field creatively
        if (request.getTargetPace() != null) {
            planExercise.setPrescribedRpe(request.getTargetPace().intValue());
        }
    }

    private void configureIsometricExercise(PlanExercise planExercise, CalendarController.IndividualExerciseRequest request) {
        // Store hold duration in prescribedRestSeconds field
        planExercise.setPrescribedRestSeconds(request.getHoldDurationSeconds() != null ? request.getHoldDurationSeconds() : 30);
        planExercise.setPrescribedSets(3); // Number of holds
    }

    private void updatePlanExerciseConfiguration(PlanExercise planExercise, CalendarController.IndividualExerciseRequest updates) {
        // Determine exercise type from the associated exercise
        Exercise exercise = planExercise.getExercise();

        if (exercise.getIsCardio()) {
            // Update cardio configuration
            if (updates.getTargetDurationMinutes() != null) {
                planExercise.setPrescribedSets(updates.getTargetDurationMinutes());
            }
            if (updates.getTargetDistanceKm() != null) {
                planExercise.setPrescribedWeightPercent(updates.getTargetDistanceKm());
            }
            if (updates.getTargetPace() != null) {
                planExercise.setPrescribedRpe(updates.getTargetPace().intValue());
            }
        } else if (exercise.getIsIsometric()) {
            // Update isometric configuration
            if (updates.getHoldDurationSeconds() != null) {
                planExercise.setPrescribedRestSeconds(updates.getHoldDurationSeconds());
            }
        } else {
            // Update strength configuration
            if (updates.getSets() != null) planExercise.setPrescribedSets(updates.getSets());
            if (updates.getReps() != null) planExercise.setPrescribedReps(updates.getReps());
            if (updates.getWeight() != null) planExercise.setPrescribedWeightPercent(updates.getWeight());
            if (updates.getRestSeconds() != null) planExercise.setPrescribedRestSeconds(updates.getRestSeconds());
            if (updates.getTempo() != null) planExercise.setPrescribedTempo(updates.getTempo());
            if (updates.getTargetRpe() != null) planExercise.setPrescribedRpe(updates.getTargetRpe());
        }

        if (updates.getNotes() != null) {
            planExercise.setInstructions(updates.getNotes());
        }
    }

    // =============================================================================
    // RESPONSE CONVERSION AND SESSION MANAGEMENT
    // =============================================================================

    private void createWorkoutSessionForCompleted(User user, ScheduledWorkout scheduledWorkout) {
        WorkoutSession session = new WorkoutSession();
        session.setUser(user);
        session.setWorkoutPlan(scheduledWorkout.getWorkoutPlan());
        session.setDate(scheduledWorkout.getScheduledDate());
        session.setTotalDurationMinutes(scheduledWorkout.getEstimatedDurationMinutes());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        workoutSessionRepository.save(session);

        // Link the session to the scheduled workout
        scheduledWorkout.setCompletedSession(session);
        scheduledWorkoutRepository.save(scheduledWorkout);
    }

    private boolean isTemporaryIndividualExercisePlan(WorkoutPlan workoutPlan) {
        return workoutPlan.getWorkoutName().startsWith("Individual:") &&
                workoutPlan.getWorkoutCategory().equals("Individual");
    }

    // ✅ FIXED: Extract configuration data from PlanExercise and include in response
    private ScheduledWorkoutResponse convertToResponse(ScheduledWorkout scheduledWorkout) {
        WorkoutPlan plan = scheduledWorkout.getWorkoutPlan();

        // Create WorkoutPlanInfo nested object
        ScheduledWorkoutResponse.WorkoutPlanInfo workoutPlanInfo = ScheduledWorkoutResponse.WorkoutPlanInfo.builder()
                .id(plan.getId())
                .name(plan.getWorkoutName())
                .description(plan.getWorkoutDescription())
                .difficulty(plan.getDifficultyLevel().name())
                .estimatedDurationMinutes(plan.getEstimatedDurationMinutes())
                .exerciseCount(getExerciseCount(plan))
                .category(plan.getWorkoutCategory())
                .imageUrl(plan.getWorkoutImageUrl())
                .isPublic(plan.getIsPublic())
                .build();

        // Create WorkoutSessionInfo if completed session exists
        ScheduledWorkoutResponse.WorkoutSessionInfo sessionInfo = null;
        if (scheduledWorkout.getCompletedSession() != null) {
            WorkoutSession session = scheduledWorkout.getCompletedSession();
            sessionInfo = ScheduledWorkoutResponse.WorkoutSessionInfo.builder()
                    .id(session.getId())
                    .startTime(session.getCreatedAt())
                    .endTime(session.getUpdatedAt())
                    .actualDurationMinutes(session.getTotalDurationMinutes())
                    .notes(null)
                    .completed(true)
                    .build();
        }

        // ✅ FIXED: Use correct builder class name
        ScheduledWorkoutResponse.ScheduledWorkoutResponseBuilder responseBuilder = ScheduledWorkoutResponse.builder()
                .id(scheduledWorkout.getId())
                .scheduledDate(scheduledWorkout.getScheduledDate())
                .status(scheduledWorkout.getStatus().name())
                .completedAt(scheduledWorkout.getCompletedAt())
                .estimatedDurationMinutes(scheduledWorkout.getEstimatedDurationMinutes())
                .customNotes(scheduledWorkout.getCustomNotes())
                .createdAt(scheduledWorkout.getCreatedAt())
                .updatedAt(scheduledWorkout.getUpdatedAt())
                .workoutPlan(workoutPlanInfo)
                .completedSession(sessionInfo);

        // ✅ NEW: Extract configuration from the first PlanExercise (for individual exercises)
        List<PlanExercise> planExercises = planExerciseRepository.findByWorkoutPlanOrderByOrderInWorkout(plan);
        if (!planExercises.isEmpty()) {
            PlanExercise planExercise = planExercises.get(0); // Single exercise for individual scheduling
            Exercise exercise = planExercise.getExercise();

            // Extract configuration based on exercise type
            if (exercise.getIsCardio()) {
                // Extract cardio configuration
                responseBuilder
                        .targetDurationMinutes(planExercise.getPrescribedSets()) // Duration stored in sets field
                        .targetDistanceKm(planExercise.getPrescribedWeightPercent()) // Distance stored in weight field
                        .targetPace(planExercise.getPrescribedRpe() != null ? planExercise.getPrescribedRpe().doubleValue() : null); // Pace stored in RPE field
            } else if (exercise.getIsIsometric()) {
                // Extract isometric configuration
                responseBuilder
                        .sets(planExercise.getPrescribedSets()) // Number of holds
                        .holdDurationSeconds(planExercise.getPrescribedRestSeconds()) // Hold duration stored in rest seconds
                        .restSeconds(60); // Default rest between holds
            } else {
                // Extract strength configuration
                responseBuilder
                        .sets(planExercise.getPrescribedSets())
                        .reps(planExercise.getPrescribedReps())
                        .weight(planExercise.getPrescribedWeightPercent()) // Weight stored as percentage
                        .restSeconds(planExercise.getPrescribedRestSeconds())
                        .tempo(planExercise.getPrescribedTempo())
                        .targetRpe(planExercise.getPrescribedRpe());
            }
        }

        return responseBuilder.build();
    }

    private Integer getExerciseCount(WorkoutPlan plan) {
        return (int) planExerciseRepository.countByWorkoutPlan(plan);
    }

    // =============================================================================
    // DISPLAY HELPER METHODS
    // =============================================================================

    private String getDifficultyDescription(WorkoutPlan.DifficultyLevel difficultyLevel) {
        if (difficultyLevel == null) return "Unknown difficulty";

        return switch (difficultyLevel) {
            case BEGINNER -> "Beginner - No experience needed";
            case INTERMEDIATE -> "Intermediate - Some experience recommended";
            case ADVANCED -> "Advanced - For experienced athletes";
        };
    }

    private String getWorkoutTypeDisplay(WorkoutPlan.WorkoutType workoutType) {
        if (workoutType == null) return "Unknown type";

        return switch (workoutType) {
            case STRENGTH -> "Strength Training";
            case CARDIO -> "Cardiovascular";
            case FLEXIBILITY -> "Flexibility & Mobility";
            case MIXED -> "Mixed Training";
            case HIIT -> "High Intensity Interval Training";
            case POWERLIFTING -> "Powerlifting";
        };
    }

    // =============================================================================
    // STATISTICS HELPER METHODS - DAILY CALCULATIONS
    // =============================================================================

    private Integer getExercisesScheduledForDate(User user, LocalDate date) {
        List<ScheduledWorkout> workouts = scheduledWorkoutRepository.findByUserAndScheduledDateOrderByCreatedAtAsc(user, date);
        return workouts.size();
    }

    private Integer getExercisesCompletedForDate(User user, LocalDate date) {
        List<ScheduledWorkout> workouts = scheduledWorkoutRepository.findByUserAndScheduledDateOrderByCreatedAtAsc(user, date);
        return (int) workouts.stream()
                .filter(workout -> workout.getStatus() == ScheduleStatus.COMPLETED)
                .count();
    }

    private Integer getMinutesWorkedOutForDate(User user, LocalDate date) {
        List<ScheduledWorkout> workouts = scheduledWorkoutRepository.findByUserAndScheduledDateOrderByCreatedAtAsc(user, date);
        return workouts.stream()
                .filter(workout -> workout.getStatus() == ScheduleStatus.COMPLETED)
                .mapToInt(workout -> workout.getEstimatedDurationMinutes() != null ? workout.getEstimatedDurationMinutes() : 0)
                .sum();
    }

    // =============================================================================
    // STATISTICS HELPER METHODS - WEEKLY CALCULATIONS
    // =============================================================================

    private Integer getExercisesScheduledForWeek(User user, LocalDate referenceDate) {
        LocalDate startOfWeek = referenceDate.minusDays(referenceDate.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startOfWeek, endOfWeek);
        return workouts.size();
    }

    private Integer getExercisesCompletedForWeek(User user, LocalDate referenceDate) {
        LocalDate startOfWeek = referenceDate.minusDays(referenceDate.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startOfWeek, endOfWeek);
        return (int) workouts.stream()
                .filter(workout -> workout.getStatus() == ScheduleStatus.COMPLETED)
                .count();
    }

    private Integer getMinutesWorkedOutForWeek(User user, LocalDate referenceDate) {
        LocalDate startOfWeek = referenceDate.minusDays(referenceDate.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startOfWeek, endOfWeek);
        return workouts.stream()
                .filter(workout -> workout.getStatus() == ScheduleStatus.COMPLETED)
                .mapToInt(workout -> workout.getEstimatedDurationMinutes() != null ? workout.getEstimatedDurationMinutes() : 0)
                .sum();
    }

    private Double calculateCompletionRateForWeek(User user, LocalDate referenceDate) {
        Integer scheduled = getExercisesScheduledForWeek(user, referenceDate);
        Integer completed = getExercisesCompletedForWeek(user, referenceDate);

        if (scheduled == 0) return 0.0;
        return (double) completed / scheduled * 100.0;
    }

    // =============================================================================
    // STATISTICS HELPER METHODS - MONTHLY CALCULATIONS
    // =============================================================================

    private Integer getExercisesScheduledForMonth(User user, LocalDate referenceDate) {
        LocalDate startOfMonth = referenceDate.withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startOfMonth, endOfMonth);
        return workouts.size();
    }

    private Integer getExercisesCompletedForMonth(User user, LocalDate referenceDate) {
        LocalDate startOfMonth = referenceDate.withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startOfMonth, endOfMonth);
        return (int) workouts.stream()
                .filter(workout -> workout.getStatus() == ScheduleStatus.COMPLETED)
                .count();
    }

    private Integer getMinutesWorkedOutForMonth(User user, LocalDate referenceDate) {
        LocalDate startOfMonth = referenceDate.withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startOfMonth, endOfMonth);
        return workouts.stream()
                .filter(workout -> workout.getStatus() == ScheduleStatus.COMPLETED)
                .mapToInt(workout -> workout.getEstimatedDurationMinutes() != null ? workout.getEstimatedDurationMinutes() : 0)
                .sum();
    }

    private Double calculateCompletionRateForMonth(User user, LocalDate referenceDate) {
        Integer scheduled = getExercisesScheduledForMonth(user, referenceDate);
        Integer completed = getExercisesCompletedForMonth(user, referenceDate);

        if (scheduled == 0) return 0.0;
        return (double) completed / scheduled * 100.0;
    }

    // =============================================================================
    // STATISTICS HELPER METHODS - HISTORICAL DATA
    // =============================================================================

    private LocalDate getLastWorkoutDate(User user) {
        List<ScheduledWorkout> completedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduleStatus.COMPLETED);

        if (completedWorkouts.isEmpty()) return null;

        // Get the most recent workout (last item in ASC ordered list)
        return completedWorkouts.get(completedWorkouts.size() - 1).getScheduledDate();
    }

    private String getLastWorkoutType(User user) {
        List<ScheduledWorkout> completedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduleStatus.COMPLETED);

        if (completedWorkouts.isEmpty()) return null;

        // Take the last workout from ASC ordered list
        ScheduledWorkout lastWorkout = completedWorkouts.get(completedWorkouts.size() - 1);
        return getWorkoutTypeDisplay(lastWorkout.getWorkoutPlan().getWorkoutType());
    }

    private Integer getTotalWorkoutsCompleted(User user) {
        List<ScheduledWorkout> completedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduleStatus.COMPLETED);
        return completedWorkouts.size();
    }

    private Integer getTotalMinutesWorkedOut(User user) {
        List<ScheduledWorkout> completedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduleStatus.COMPLETED);

        return completedWorkouts.stream()
                .mapToInt(workout -> workout.getEstimatedDurationMinutes() != null ? workout.getEstimatedDurationMinutes() : 0)
                .sum();
    }

    private String getFavoriteExerciseType(User user) {
        List<ScheduledWorkout> completedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduleStatus.COMPLETED);

        if (completedWorkouts.isEmpty()) return "None";

        // Count exercise types from completed workouts
        int strengthCount = 0;
        int cardioCount = 0;
        int isometricCount = 0;

        for (ScheduledWorkout workout : completedWorkouts) {
            List<PlanExercise> exercises = planExerciseRepository.findByWorkoutPlanOrderByOrderInWorkout(workout.getWorkoutPlan());
            for (PlanExercise planExercise : exercises) {
                Exercise exercise = planExercise.getExercise();
                if (exercise.getIsCardio()) {
                    cardioCount++;
                } else if (exercise.getIsIsometric()) {
                    isometricCount++;
                } else {
                    strengthCount++;
                }
            }
        }

        // Return most common type
        if (strengthCount >= cardioCount && strengthCount >= isometricCount) {
            return "Strength";
        } else if (cardioCount >= isometricCount) {
            return "Cardio";
        } else {
            return "Isometric";
        }
    }
}