package com.chidituke.workout_tracker.service.workout;

import com.chidituke.workout_tracker.dto.request.performance.CompleteSetRequest;
import com.chidituke.workout_tracker.dto.request.performance.CompleteWorkoutRequest;
import com.chidituke.workout_tracker.dto.request.scheduled_workouts.ScheduledWorkoutRequest;
import com.chidituke.workout_tracker.dto.request.workout_plan.ScheduleMultipleExercisesRequestDTO;
import com.chidituke.workout_tracker.dto.request.scheduled_workouts.IndividualExerciseRequest;
import com.chidituke.workout_tracker.dto.response.performance.ExerciseExecutionSummary;
import com.chidituke.workout_tracker.dto.response.performance.PerformanceResponse;
import com.chidituke.workout_tracker.dto.response.performance.WorkoutExecutionSummary;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.CalendarViewResponse;
import com.chidituke.workout_tracker.dto.response.workout_session.WorkoutSessionResponse;
import com.chidituke.workout_tracker.exceptions.scheduled_workout.*;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.exceptions.workout_plan.WorkoutPlanNotFoundException;
import com.chidituke.workout_tracker.exceptions.subscription.SubscriptionLimitExceededException;
import com.chidituke.workout_tracker.mapper.workout.ScheduledWorkoutMapper;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.user.enums.SubscriptionTier;
import com.chidituke.workout_tracker.model.workout.ScheduledWorkout;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import com.chidituke.workout_tracker.model.workout.WorkoutProgram;
import com.chidituke.workout_tracker.model.workout.WorkoutSession;
import com.chidituke.workout_tracker.model.workout.PlanExercise;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.mapper.workout.ExerciseMapper;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan.DifficultyLevel;
import com.chidituke.workout_tracker.repository.workout.PlanExerciseRepository;
import com.chidituke.workout_tracker.repository.workout.ExerciseRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.repository.workout.ScheduledWorkoutRepository;
import com.chidituke.workout_tracker.repository.workout.WorkoutPlanRepository;
import com.chidituke.workout_tracker.repository.workout.WorkoutProgramRepository;
import com.chidituke.workout_tracker.repository.workout.WorkoutSessionRepository;
import com.chidituke.workout_tracker.model.workout.PerformanceRecord;
import com.chidituke.workout_tracker.repository.workout.PerformanceRecordRepository;
import com.chidituke.workout_tracker.controller.workout.ScheduledWorkoutController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ✅ FIXED: ScheduledWorkoutService aligned with existing codebase
 * <p>
 * Comprehensive service for managing scheduled workouts with:
 * - Individual workout scheduling ✅
 * - Complete workout plan scheduling with subscription limits ✅
 * - Individual exercise scheduling ✅
 * - Exercise completion tracking ✅
 * - Calendar views with exercise configuration ✅
 * - Workout statistics and analytics ✅
 * - Subscription enforcement (3-exercise daily limit for FREE users) ✅
 * - Program scheduling and analytics ✅
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduledWorkoutService {

    // =======================
    // DEPENDENCIES
    // =======================
    private final ScheduledWorkoutRepository scheduledWorkoutRepository;
    private final UserRepository userRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutProgramRepository workoutProgramRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final PlanExerciseRepository planExerciseRepository;
    private final PerformanceRecordRepository performanceRecordRepository;
    private final ExerciseRepository exerciseRepository;
    private final ScheduledWorkoutMapper scheduledWorkoutMapper;
    private final ExerciseMapper exerciseMapper;

    // =======================
    // INDIVIDUAL EXERCISE SCHEDULING
    // =======================

    /**
     * Schedule an individual exercise by creating a temporary single-exercise workout plan.
     * This method bridges the gap between exercise-based user thinking and
     * workout-plan-based data storage.
     */
    @Transactional
    public ScheduledWorkoutResponse scheduleIndividualExercise(String username,
                                                               IndividualExerciseRequest request) {
        try {
            log.info("🎯 Scheduling individual exercise {} for user {} on {}",
                    request.getExerciseId(), username, request.getScheduledDate());

            // Validate user exists and exercise exists
            User user = findUserByUsername(username);
            Exercise exercise = findExerciseById(request.getExerciseId());

            exerciseMapper.autoCorrectExerciseModality(exercise);

            if (!exerciseMapper.isExerciseDataComplete(exercise)) {
                log.warn("⚠️ Exercise {} has incomplete data: {}",
                        exercise.getId(), exerciseMapper.getExerciseSummary(exercise));
            }

            // Check subscription limits before proceeding
            validateIndividualExerciseScheduling(user, request);

            // Create a minimal temporary workout plan containing just this exercise
            WorkoutPlan temporaryPlan = createTemporaryWorkoutPlan(exercise, request);
            WorkoutPlan savedPlan = workoutPlanRepository.save(temporaryPlan);

            log.debug("✅ Created temporary workout plan with ID: {}", savedPlan.getId());

            // Create the scheduled workout entry
            ScheduledWorkout scheduledWorkout = buildScheduledWorkout(user, savedPlan, request);

            scheduledWorkout.setExercise(exercise);

            // Apply exercise-specific configuration based on exercise type
            applyExerciseConfiguration(scheduledWorkout, exercise, request);

            ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

            validateAndPopulateExerciseData(saved);

            log.info("✅ Successfully scheduled individual exercise for user {}, ID: {}",
                    username, saved.getId());

            return scheduledWorkoutMapper.toResponse(saved);

        } catch (Exception e) {
            log.error("❌ Failed to schedule individual exercise: {}", e.getMessage(), e);
            // Preserve specific exception types while providing user-friendly messages
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
    @Transactional
    public ScheduledWorkoutResponse updateScheduledExercise(String username, Long exerciseId,
                                                            IndividualExerciseRequest updates) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(exerciseId);
        validateOwnership(scheduledWorkout, username);

        log.info("🔄 Updating scheduled exercise {} for user {}", exerciseId, username);

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

        log.info("✅ Successfully updated scheduled exercise {} for user {}", exerciseId, username);
        return scheduledWorkoutMapper.toResponse(saved);
    }

    /**
     * Delete a scheduled individual exercise
     */
    @Transactional
    public void deleteScheduledExercise(String username, Long exerciseId) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(exerciseId);
        validateOwnership(scheduledWorkout, username);
        validateWorkoutCanBeDeleted(scheduledWorkout);

        log.info("🗑️ Deleting scheduled exercise {} for user {}", exerciseId, username);

        try {
            // Clean up the temporary workout plan if it was created for individual exercise
            WorkoutPlan plan = scheduledWorkout.getWorkoutPlan();
            boolean isIndividualPlan = plan != null &&
                    plan.getWorkoutDescription() != null &&
                    plan.getWorkoutDescription().contains("Individual exercise");

            handleRelatedRecordsBeforeDeletion(scheduledWorkout);
            scheduledWorkoutRepository.delete(scheduledWorkout);

            // Delete the temporary individual plan if it was created for this exercise
            if (isIndividualPlan && plan != null) {
                workoutPlanRepository.delete(plan);
                log.debug("Deleted temporary individual workout plan {}", plan.getId());
            }

            log.info("✅ Successfully deleted scheduled exercise {} for user {}", exerciseId, username);

        } catch (Exception e) {
            log.error("Failed to delete scheduled exercise {} for user {}: {}",
                    exerciseId, username, e.getMessage());
            throw new RuntimeException("Failed to delete exercise: " + e.getMessage());
        }
    }

    /**
     * ✅ NEW: Get exercise information for scheduled workout response
     * This method bridges the gap between ScheduledWorkout entities and frontend needs
     */
    private ScheduledWorkoutResponse.ExerciseInfo getExerciseInfoForResponse(ScheduledWorkout scheduledWorkout) {
        try {
            // Use ExerciseMapper to extract and map exercise data
            Exercise exercise = exerciseMapper.extractExerciseFromScheduledWorkout(scheduledWorkout);

            if (exercise != null) {
                // Validate exercise data consistency
                if (!exerciseMapper.validateExerciseTypeConsistency(exercise)) {
                    log.warn("⚠️ Exercise {} has inconsistent type flags, auto-correcting", exercise.getId());
                    exerciseMapper.autoCorrectExerciseModality(exercise);
                }

                // Map to response format
                ScheduledWorkoutResponse.ExerciseInfo exerciseInfo = exerciseMapper.mapExerciseToResponseInfo(exercise);

                log.debug("✅ Successfully mapped exercise info for scheduled workout {}: isCardio={}, isIsometric={}",
                        scheduledWorkout.getId(),
                        exerciseInfo.getIsCardio(),
                        exerciseInfo.getIsIsometric());

                return exerciseInfo;
            } else {
                log.warn("⚠️ No exercise found for scheduled workout {}", scheduledWorkout.getId());
                return createFallbackExerciseInfo(scheduledWorkout);
            }

        } catch (Exception e) {
            log.error("❌ Failed to get exercise info for scheduled workout {}: {}",
                    scheduledWorkout.getId(), e.getMessage(), e);
            return createFallbackExerciseInfo(scheduledWorkout);
        }
    }

    /**
     * Create fallback exercise info when no exercise is found
     */
    private ScheduledWorkoutResponse.ExerciseInfo createFallbackExerciseInfo(ScheduledWorkout scheduledWorkout) {
        String exerciseName = "Unknown Exercise";
        String exerciseType = "STRENGTH";
        boolean isCardio = false;
        boolean isIsometric = false;

        // Try to get info from workout plan
        if (scheduledWorkout.getWorkoutPlan() != null) {
            exerciseName = scheduledWorkout.getWorkoutPlan().getWorkoutName();

            // Determine type from workout plan
            if (scheduledWorkout.isCardioWorkout()) {
                exerciseType = "CARDIO";
                isCardio = true;
            } else if (scheduledWorkout.isIsometricWorkout()) {
                exerciseType = "BALANCE";
                isIsometric = true;
            }
        }

        return ScheduledWorkoutResponse.ExerciseInfo.builder()
                .id(null)
                .name(exerciseName)
                .emoji("💪")
                .description("Exercise from workout plan")
                .exerciseType(exerciseType)
                .difficultyLevel("INTERMEDIATE")
                .isCardio(isCardio)
                .isIsometric(isIsometric)
                .workoutTrackingMode(isCardio ? "TIME_BASED" : isIsometric ? "HOLD_BASED" : "REP_BASED")
                .isFromVerifiedSource(false)
                .build();
    }

    /**
     * Enhanced method to validate and populate exercise data
     */
    private void validateAndPopulateExerciseData(ScheduledWorkout scheduledWorkout) {
        try {
            Exercise exercise = exerciseMapper.extractExerciseFromScheduledWorkout(scheduledWorkout);

            if (exercise != null) {
                // Validate exercise data consistency
                if (!exerciseMapper.isExerciseDataComplete(exercise)) {
                    log.warn("⚠️ Incomplete exercise data for scheduled workout {}: {}",
                            scheduledWorkout.getId(), exerciseMapper.getExerciseSummary(exercise));
                }

                // Auto-correct any inconsistencies
                exerciseMapper.autoCorrectExerciseModality(exercise);

                log.debug("✅ Validated exercise data for scheduled workout {}: {}",
                        scheduledWorkout.getId(), exerciseMapper.getExerciseSummary(exercise));
            }

        } catch (Exception e) {
            log.error("❌ Failed to validate exercise data for scheduled workout {}: {}",
                    scheduledWorkout.getId(), e.getMessage(), e);
        }
    }

    // =======================
    //  EXERCISE COMPLETION TRACKING
    // =======================


    /**
     * Mark exercise as completed with detailed completion data
     */
    @Transactional
    public ScheduledWorkoutResponse markExerciseCompleted(Long exerciseId, String username,
                                                          LocalDateTime completedAt,
                                                          Integer totalDurationMinutes,
                                                          String notes,
                                                          String performanceRating) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(exerciseId);
        validateOwnership(scheduledWorkout, username);

        log.info("✅ Marking exercise {} as completed for user {} with detailed data", exerciseId, username);

        // Validate that the exercise can be completed
        if (scheduledWorkout.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED) {
            throw new IllegalStateException("Exercise is already completed");
        }

        if (scheduledWorkout.getStatus() == ScheduledWorkout.ScheduleStatus.CANCELLED) {
            throw new IllegalStateException("Cannot complete a cancelled exercise");
        }

        // Mark as completed with detailed data
        scheduledWorkout.setStatus(ScheduledWorkout.ScheduleStatus.COMPLETED);
        scheduledWorkout.setCompletedAt(completedAt != null ? completedAt : LocalDateTime.now());

        // Store completion details
        if (totalDurationMinutes != null) {
            scheduledWorkout.setActualDurationMinutes(totalDurationMinutes);
        }

        if (notes != null && !notes.trim().isEmpty()) {
            String existingNotes = scheduledWorkout.getCustomNotes();
            String combinedNotes = existingNotes != null ?
                    existingNotes + " | Completion: " + notes :
                    "Completion: " + notes;
            scheduledWorkout.setCustomNotes(combinedNotes);
        }

        // Store performance rating (you might need to add this field to ScheduledWorkout entity)
        // scheduledWorkout.setPerformanceRating(performanceRating);

        ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("✅ Successfully marked exercise {} as completed for user {} with {} min duration",
                exerciseId, username, totalDurationMinutes);
        return scheduledWorkoutMapper.toResponse(saved);
    }

    /**
     * ✅ NEW: Enhanced mark exercise completed with full performance tracking
     */
    @Transactional
    public ScheduledWorkoutResponse markExerciseCompletedWithPerformance(
            String username,
            String exerciseId,
            WorkoutCompletionData completionData) {

        try {
            log.error("🔥 DEBUG: Enhanced completion called for exercise {} by user {} with data: {}",
                    exerciseId, username, completionData);

            // ... your existing method code here ...

        } catch (Exception e) {
            log.error("❌ ENHANCED COMPLETION FAILED: {}", e.getMessage(), e);
            throw e;
        }

        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(Long.parseLong(exerciseId));
        validateOwnership(scheduledWorkout, username);

        log.info("🏃‍♂️ Creating complete workout flow for exercise {} by user {}", exerciseId, username);

        try {
            // Step 1: Create WorkoutSession (if not exists)
            WorkoutSession workoutSession = getOrCreateWorkoutSession(scheduledWorkout, completionData);

            // Step 2: Create PerformanceRecord entries for each completed set
            List<PerformanceRecord> performanceRecords = createPerformanceRecords(
                    workoutSession, scheduledWorkout, completionData);

            // Step 3: Mark scheduled workout as completed
            scheduledWorkout.setStatus(ScheduledWorkout.ScheduleStatus.COMPLETED);
            scheduledWorkout.setCompletedAt(completionData.getCompletedAt());
            scheduledWorkout.setActualDurationMinutes(completionData.getTotalDurationMinutes());
            scheduledWorkout.setCompletedSession(workoutSession);

            if (completionData.getNotes() != null) {
                String existingNotes = scheduledWorkout.getCustomNotes();
                String combinedNotes = existingNotes != null ?
                        existingNotes + " | " + completionData.getNotes() :
                        completionData.getNotes();
                scheduledWorkout.setCustomNotes(combinedNotes);
            }

            // Step 4: Update workout session completion
            updateWorkoutSessionCompletion(workoutSession, completionData);

            // Save everything
            workoutSessionRepository.save(workoutSession);
            performanceRecordRepository.saveAll(performanceRecords);
            ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

            log.info("✅ Successfully completed exercise {} with {} performance records",
                    exerciseId, performanceRecords.size());

            return scheduledWorkoutMapper.toResponse(saved);

        } catch (Exception e) {
            log.error("❌ Failed to complete exercise with performance data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to complete workout: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ NEW: Get or create workout session for scheduled workout
     */
    private WorkoutSession getOrCreateWorkoutSession(ScheduledWorkout scheduledWorkout,
                                                     WorkoutCompletionData completionData) {

        // Check if workout session already exists
        if (scheduledWorkout.getCompletedSession() != null) {
            return scheduledWorkout.getCompletedSession();
        }

        // Create new workout session
        WorkoutSession workoutSession = new WorkoutSession();
        workoutSession.setUser(scheduledWorkout.getUser());
        workoutSession.setWorkoutPlan(scheduledWorkout.getWorkoutPlan());
        workoutSession.setScheduledWorkout(scheduledWorkout);
        workoutSession.setDate(LocalDate.now());
        workoutSession.setSessionStatus(WorkoutSession.SessionStatus.COMPLETED);

        // Set basic session data
        workoutSession.setTotalDurationMinutes(completionData.getTotalDurationMinutes());
        workoutSession.setTotalExercisesPlanned(1); // Single exercise
        workoutSession.setTotalExercisesCompleted(1);
        workoutSession.setCompletionPercentage(100.0);

        // Set optional fields from completion data
        if (completionData.getDifficultyRating() != null) {
            workoutSession.setDifficultyRating(completionData.getDifficultyRating());
        }
        if (completionData.getOverallEffort() != null) {
            workoutSession.setOverallEffort(completionData.getOverallEffort());
        }

        // Set mood and location if provided
        if (completionData.getMood() != null) {
            try {
                WorkoutSession.WorkoutMood mood = WorkoutSession.WorkoutMood.valueOf(
                        completionData.getMood().toUpperCase());
                workoutSession.setMood(mood);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid mood value: {}, using default", completionData.getMood());
                workoutSession.setMood(WorkoutSession.WorkoutMood.FOCUSED);
            }
        }

        if (completionData.getLocation() != null) {
            try {
                WorkoutSession.WorkoutLocation location = WorkoutSession.WorkoutLocation.valueOf(
                        completionData.getLocation().toUpperCase());
                workoutSession.setLocation(location);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid location value: {}, using default", completionData.getLocation());
                workoutSession.setLocation(WorkoutSession.WorkoutLocation.HOME);
            }
        }

        // Set notes
        if (completionData.getNotes() != null || completionData.getWorkoutFeedback() != null) {
            StringBuilder notes = new StringBuilder();
            if (completionData.getNotes() != null) {
                notes.append(completionData.getNotes());
            }
            if (completionData.getWorkoutFeedback() != null) {
                if (notes.length() > 0) notes.append(" | ");
                notes.append("Feedback: ").append(completionData.getWorkoutFeedback());
            }
            workoutSession.setNotes(notes.toString());
        }

        return workoutSession;
    }

    /**
     * ✅ NEW: Create performance records from completion data
     */
    private List<PerformanceRecord> createPerformanceRecords(
            WorkoutSession workoutSession,
            ScheduledWorkout scheduledWorkout,
            WorkoutCompletionData completionData) {

        List<PerformanceRecord> performanceRecords = new ArrayList<>();
        Exercise exercise = scheduledWorkout.getResolvedExercise();

        if (exercise == null) {
            log.warn("⚠️ No exercise resolved for scheduled workout {}, creating basic performance record",
                    scheduledWorkout.getId());

            // Create a basic performance record even without exercise details
            PerformanceRecord basicRecord = createBasicPerformanceRecord(
                    workoutSession, scheduledWorkout, completionData);
            performanceRecords.add(basicRecord);
            return performanceRecords;
        }

        // Create performance records for each completed set
        List<CompletedSetData> sets = completionData.getSets();
        if (sets == null || sets.isEmpty()) {
            // No set data provided - create default performance records
            performanceRecords.addAll(createDefaultPerformanceRecords(
                    workoutSession, exercise, scheduledWorkout, completionData));
        } else {
            // Create performance records from actual set data
            for (CompletedSetData setData : sets) {
                PerformanceRecord record = createPerformanceRecordFromSetData(
                        workoutSession, exercise, setData, completionData);
                performanceRecords.add(record);
            }
        }

        return performanceRecords;
    }

    /**
     * ✅ NEW: Create performance record from set data
     */
    private PerformanceRecord createPerformanceRecordFromSetData(
            WorkoutSession workoutSession,
            Exercise exercise,
            CompletedSetData setData,
            WorkoutCompletionData completionData) {

        PerformanceRecord record = PerformanceRecord.builder()
                .workoutSession(workoutSession)
                .exercise(exercise)
                .setNumber(setData.getSetNumber())
                .createdAt(LocalDateTime.now())
                .build();

        // Set exercise type specific data
        if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
            // Cardio exercise
            record.setDurationMinutes(setData.getActualDurationMinutes());
            record.setDistanceKm(completionData.getDistanceKm());
            record.setCaloriesBurned(completionData.getCaloriesBurned());

            // For cardio, "reps" represents duration target
            record.setTargetRepsPlanned(setData.getTargetReps());
            record.setReps(setData.getActualDurationMinutes());

        } else if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
            // Isometric exercise
            record.setHoldDurationSeconds(setData.getActualHoldSeconds());

            // For isometric, "reps" represents hold duration target
            record.setTargetRepsPlanned(setData.getTargetReps());
            record.setReps(setData.getActualHoldSeconds());

        } else {
            // Strength exercise
            record.setReps(setData.getActualReps());
            record.setWeight(setData.getActualWeight());
            record.setTargetRepsPlanned(setData.getTargetReps());
            record.setTargetWeightPlanned(setData.getTargetWeight());
        }

        // Set common fields
        record.setPerceivedExertion(setData.getRpe());
        record.setRestTimeBeforeSetSeconds(setData.getRestSeconds());
        record.setNotes(setData.getNotes());
        record.setIsExerciseCompleted(setData.getCompleted());

        // Calculate performance vs target
        record.evaluatePerformanceVsTarget();

        log.debug("✅ Created performance record for set {} of exercise {}",
                setData.getSetNumber(), exercise.getExerciseName());

        return record;
    }

    /**
     * ✅ NEW: Create default performance records when no set data provided
     */
    private List<PerformanceRecord> createDefaultPerformanceRecords(
            WorkoutSession workoutSession,
            Exercise exercise,
            ScheduledWorkout scheduledWorkout,
            WorkoutCompletionData completionData) {

        List<PerformanceRecord> records = new ArrayList<>();

        // Determine number of sets from scheduled workout
        int sets = scheduledWorkout.getTargetSets() != null ? scheduledWorkout.getTargetSets() :
                (exercise.getIsCardio() != null && exercise.getIsCardio() ? 1 : 3);

        for (int setNumber = 1; setNumber <= sets; setNumber++) {
            PerformanceRecord record = PerformanceRecord.builder()
                    .workoutSession(workoutSession)
                    .exercise(exercise)
                    .setNumber(setNumber)
                    .isExerciseCompleted(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Set default values based on exercise type and scheduled workout
            if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
                record.setDurationMinutes(scheduledWorkout.getTargetDurationMinutes() != null ?
                        scheduledWorkout.getTargetDurationMinutes() : completionData.getTotalDurationMinutes());
                record.setReps(record.getDurationMinutes());

            } else if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
                record.setHoldDurationSeconds(scheduledWorkout.getHoldDurationSeconds() != null ?
                        scheduledWorkout.getHoldDurationSeconds() : 30);
                record.setReps(record.getHoldDurationSeconds());

            } else {
                // Strength exercise defaults
                int defaultReps = parseTargetReps(scheduledWorkout.getTargetReps());
                record.setReps(defaultReps);
                record.setWeight(scheduledWorkout.getTargetWeight());
                record.setTargetRepsPlanned(defaultReps);
                record.setTargetWeightPlanned(scheduledWorkout.getTargetWeight());
            }

            // Set common defaults
            record.setPerceivedExertion(scheduledWorkout.getTargetRpe() != null ?
                    scheduledWorkout.getTargetRpe() : 7);
            record.setRestTimeBeforeSetSeconds(scheduledWorkout.getRestSeconds() != null ?
                    scheduledWorkout.getRestSeconds() : 90);
            record.setPerformanceVsTarget(PerformanceRecord.PerformanceVsTarget.MET);

            records.add(record);
        }

        log.info("✅ Created {} default performance records for exercise {}",
                records.size(), exercise.getExerciseName());

        return records;
    }

    /**
     * ✅ NEW: Create basic performance record when exercise is not resolved
     */
    private PerformanceRecord createBasicPerformanceRecord(
            WorkoutSession workoutSession,
            ScheduledWorkout scheduledWorkout,
            WorkoutCompletionData completionData) {

        // Try to find exercise by ID from scheduled workout
        Exercise exercise = null;
        try {
            if (scheduledWorkout.getExercise() != null) {
                exercise = scheduledWorkout.getExercise();
            } else {
                // Try to find any exercise from workout plan
                List<PlanExercise> planExercises = planExerciseRepository
                        .findByWorkoutPlanOrderByOrderInWorkout(scheduledWorkout.getWorkoutPlan());
                if (!planExercises.isEmpty()) {
                    exercise = planExercises.get(0).getExercise();
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not resolve exercise for basic performance record");
        }

        if (exercise == null) {
            throw new RuntimeException("Cannot create performance record without exercise reference");
        }

        PerformanceRecord record = PerformanceRecord.builder()
                .workoutSession(workoutSession)
                .exercise(exercise)
                .setNumber(1)
                .isExerciseCompleted(true)
                .notes("Completed via calendar")
                .createdAt(LocalDateTime.now())
                .build();

        // Set basic performance data
        record.setPerceivedExertion(7); // Default moderate effort
        record.setPerformanceVsTarget(PerformanceRecord.PerformanceVsTarget.MET);

        return record;
    }

    /**
     * ✅ NEW: Update workout session completion
     */
    private void updateWorkoutSessionCompletion(WorkoutSession workoutSession,
                                                WorkoutCompletionData completionData) {
        workoutSession.setSessionStatus(WorkoutSession.SessionStatus.COMPLETED);

        // Store additional completion data if available
        if (completionData.getPerformanceSummary() != null) {
            workoutSession.setPerformanceSummary(completionData.getPerformanceSummary());
        }

        // Calculate estimated calories if not provided
        if (workoutSession.getEstimatedCalories() == null && completionData.getTotalDurationMinutes() != null) {
            // Simple calorie estimation: 5 calories per minute (very rough estimate)
            workoutSession.setEstimatedCalories(completionData.getTotalDurationMinutes() * 5);
        }
    }

    /**
     * ✅ BACKWARD COMPATIBILITY: Keep the simple version for existing code
     */
    @Transactional
    public ScheduledWorkoutResponse markExerciseCompleted(String username, Long exerciseId) {
        return markExerciseCompleted(exerciseId, username, null, null, null, "MET");
    }

    /**
     * Enhanced markExerciseCompleted that creates full performance tracking
     */
    @Transactional
    public ScheduledWorkoutResponse markExerciseCompleted(String username, String exerciseId,
                                                          LocalDateTime completedAt,
                                                          Integer totalDurationMinutes,
                                                          String notes,
                                                          String performanceRating) {

        // Create completion data from parameters
        WorkoutCompletionData completionData = WorkoutCompletionData.builder()
                .exerciseId(exerciseId)
                .scheduledExerciseId(exerciseId)
                .completedAt(completedAt != null ? completedAt : LocalDateTime.now())
                .totalDurationMinutes(totalDurationMinutes)
                .notes(notes)
                .performanceRating(performanceRating != null ? performanceRating : "MET")
                .sets(new ArrayList<>()) // Empty sets - will create defaults
                .personalRecords(new ArrayList<>())
                .improvements(new ArrayList<>())
                .build();

        return markExerciseCompletedWithPerformance(username, exerciseId, completionData);
    }

    @Transactional
    public List<ScheduledWorkoutResponse> markMultipleExercisesCompleted(String username, List<Long> exerciseIds) {
        List<ScheduledWorkoutResponse> responses = new ArrayList<>();

        for (Long exerciseId : exerciseIds) {
            try {
                ScheduledWorkoutResponse response = markExerciseCompleted(username, exerciseId);
                responses.add(response);
            } catch (Exception e) {
                log.error("Failed to mark exercise {} as completed: {}", exerciseId, e.getMessage());
            }
        }

        return responses;
    }

    /**
     * Mark multiple exercises as completed (batch operation)
     */
    @Transactional
    public List<ScheduledWorkoutResponse> markMultipleExercisesCompletedByStringIds(String username, List<String> exerciseIds) {
        List<Long> longIds = exerciseIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
        return markMultipleExercisesCompleted(username, longIds);
    }

    /**
     * Start a workout session from scheduled workout - creates WorkoutSession and tracks performance
     */
    @Transactional
    public WorkoutSessionResponse startWorkoutExecution(String username, Long scheduledWorkoutId) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(scheduledWorkoutId);
        validateOwnership(scheduledWorkout, username);

        log.info("🚀 Starting workout execution for scheduled workout {} by user {}", scheduledWorkoutId, username);

        // Create new workout session
        WorkoutSession workoutSession = new WorkoutSession();
        workoutSession.setUser(scheduledWorkout.getUser());
        workoutSession.setWorkoutPlan(scheduledWorkout.getWorkoutPlan());
        workoutSession.setScheduledWorkout(scheduledWorkout);
        workoutSession.setDate(LocalDate.now());
        workoutSession.setSessionStatus(WorkoutSession.SessionStatus.IN_PROGRESS);

        // Initialize performance tracking
        if (scheduledWorkout.getExercise() != null) {
            workoutSession.setTotalExercisesPlanned(1);
        } else if (scheduledWorkout.getWorkoutPlan() != null) {
            List<PlanExercise> planExercises = planExerciseRepository.findByWorkoutPlanOrderByOrderInWorkout(scheduledWorkout.getWorkoutPlan());
            workoutSession.setTotalExercisesPlanned(planExercises.size());
        }

        workoutSession.setTotalExercisesCompleted(0);
        workoutSession.setCompletionPercentage(0.0);

        WorkoutSession savedSession = workoutSessionRepository.save(workoutSession);

        // Update scheduled workout status
        scheduledWorkout.setStatus(ScheduledWorkout.ScheduleStatus.IN_PROGRESS);
        scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("✅ Created workout session {} for scheduled workout {}", savedSession.getId(), scheduledWorkoutId);

        return mapToWorkoutSessionResponse(savedSession);
    }

    /**
     * Complete a set with detailed performance data
     */
    @Transactional
    public PerformanceResponse completeSet(String username, Long workoutSessionId,
                                           CompleteSetRequest request) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found: " + workoutSessionId));

        validateWorkoutSessionOwnership(workoutSession, username);

        log.info("💪 Recording performance for exercise {} set {} in session {}",
                request.getExerciseId(), request.getSetNumber(), workoutSessionId);

        // Create performance record
        PerformanceRecord performanceRecord = new PerformanceRecord();
        performanceRecord.setWorkoutSession(workoutSession);
        performanceRecord.setExercise(findExerciseById(request.getExerciseId()));
        performanceRecord.setSetNumber(request.getSetNumber());

        // Set performance data based on exercise type
        Exercise exercise = findExerciseById(request.getExerciseId());

        if (exercise.getIsCardio()) {
            performanceRecord.setDurationMinutes(request.getDurationMinutes());
            performanceRecord.setDurationSeconds(request.getDurationSeconds());
            performanceRecord.setDistanceKm(request.getDistanceKm());
            performanceRecord.setCaloriesBurned(request.getCaloriesBurned());
        } else if (exercise.getIsIsometric()) {
            performanceRecord.setHoldDurationSeconds(request.getHoldDurationSeconds());
        } else {
            performanceRecord.setReps(request.getReps());
            performanceRecord.setWeight(request.getWeight());
        }

        // Set target comparison data
        performanceRecord.setTargetRepsPlanned(request.getTargetReps());
        performanceRecord.setTargetWeightPlanned(request.getTargetWeight());

        // Set timing data
        performanceRecord.setSetStartTime(request.getSetStartTime());
        performanceRecord.setSetEndTime(request.getSetEndTime());
        performanceRecord.setRestTimeBeforeSetSeconds(request.getRestTimeSeconds());

        // Set subjective data
        performanceRecord.setPerceivedExertion(request.getPerceivedExertion());
        performanceRecord.setFormRating(request.getFormRating());
        performanceRecord.setNotes(request.getNotes());

        // Calculate performance vs target
        performanceRecord.setPerformanceVsTarget(calculatePerformanceVsTarget(performanceRecord));

        PerformanceRecord savedRecord = performanceRecordRepository.save(performanceRecord);

        log.info("✅ Recorded performance for exercise {} set {}", request.getExerciseId(), request.getSetNumber());

        return mapToPerformanceRecordResponse(savedRecord);
    }

    /**
     * Complete an exercise (all sets done)
     */
    @Transactional
    public WorkoutSessionResponse completeExercise(String username, Long workoutSessionId,
                                                   Long exerciseId, String completionNotes) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found: " + workoutSessionId));

        validateWorkoutSessionOwnership(workoutSession, username);

        log.info("🎯 Completing exercise {} in workout session {}", exerciseId, workoutSessionId);

        // Mark all performance records for this exercise as completed
        List<PerformanceRecord> exerciseRecords = performanceRecordRepository
                .findByWorkoutSessionAndExerciseOrderBySetNumber(workoutSession, findExerciseById(exerciseId));

        for (PerformanceRecord record : exerciseRecords) {
            record.setIsExerciseCompleted(true);
            record.setExerciseCompletionNotes(completionNotes);
        }

        performanceRecordRepository.saveAll(exerciseRecords);

        // Update workout session completion - trigger will handle this automatically
        WorkoutSession updatedSession = workoutSessionRepository.findById(workoutSessionId).orElse(workoutSession);

        log.info("✅ Completed exercise {} in workout session {}", exerciseId, workoutSessionId);

        return mapToWorkoutSessionResponse(updatedSession);
    }

    /**
     * Complete entire workout session
     */
    @Transactional
    public ScheduledWorkoutResponse completeWorkoutSession(String username, Long workoutSessionId,
                                                           CompleteWorkoutRequest request) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found: " + workoutSessionId));

        validateWorkoutSessionOwnership(workoutSession, username);

        log.info("🏁 Completing workout session {} for user {}", workoutSessionId, username);

        // Update workout session - FIXED: Use correct enum types and available methods
        workoutSession.setSessionStatus(WorkoutSession.SessionStatus.COMPLETED);
        workoutSession.setTotalDurationMinutes(request.getTotalDurationMinutes());
        workoutSession.setDifficultyRating(request.getDifficultyRating());
        workoutSession.setOverallEffort(request.getOverallEffort());

        // ✅ FIXED: Convert String to enum for mood
        if (request.getMood() != null) {
            try {
                WorkoutSession.WorkoutMood mood = WorkoutSession.WorkoutMood.valueOf(request.getMood().toUpperCase());
                workoutSession.setMood(mood);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid mood value: {}, using default", request.getMood());
                workoutSession.setMood(WorkoutSession.WorkoutMood.FOCUSED); // Use FOCUSED as default
            }
        }

        // ✅ FIXED: Convert String to enum for location
        if (request.getLocation() != null) {
            try {
                WorkoutSession.WorkoutLocation location = WorkoutSession.WorkoutLocation.valueOf(request.getLocation().toUpperCase());
                workoutSession.setLocation(location);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid location value: {}, using default", request.getLocation());
                workoutSession.setLocation(WorkoutSession.WorkoutLocation.HOME);
            }
        }

        // ✅ FIXED: Check if these methods exist in WorkoutSession entity
        // If these methods don't exist, we'll store the data in notes or create the methods
        if (request.getWorkoutFeedback() != null) {
            // Option 1: If setWorkoutFeedback doesn't exist, store in notes
            String existingNotes = workoutSession.getNotes();
            String feedback = "Feedback: " + request.getWorkoutFeedback();
            workoutSession.setNotes(existingNotes != null ? existingNotes + " | " + feedback : feedback);

            // Option 2: If you have this method in WorkoutSession, uncomment this:
            // workoutSession.setWorkoutFeedback(request.getWorkoutFeedback());
        }

        if (request.getPerformanceSummary() != null) {
            // Option 1: If setPerformanceSummary doesn't exist, store in notes
            String existingNotes = workoutSession.getNotes();
            String summary = "Performance: " + request.getPerformanceSummary();
            workoutSession.setNotes(existingNotes != null ? existingNotes + " | " + summary : summary);

            // Option 2: If you have this method in WorkoutSession, uncomment this:
            // workoutSession.setPerformanceSummary(request.getPerformanceSummary());
        }

        WorkoutSession savedSession = workoutSessionRepository.save(workoutSession);

        // Update scheduled workout
        ScheduledWorkout scheduledWorkout = workoutSession.getScheduledWorkout();
        if (scheduledWorkout != null) {
            scheduledWorkout.setStatus(ScheduledWorkout.ScheduleStatus.COMPLETED);
            scheduledWorkout.setCompletedAt(LocalDateTime.now());
            scheduledWorkout.setActualDurationMinutes(request.getTotalDurationMinutes());
            scheduledWorkout = scheduledWorkoutRepository.save(scheduledWorkout);

            log.info("✅ Completed workout session {} and scheduled workout {}",
                    workoutSessionId, scheduledWorkout.getId());

            return scheduledWorkoutMapper.toResponse(scheduledWorkout);
        }

        throw new RuntimeException("No scheduled workout associated with session");
    }

    /**
     * Get workout execution summary with performance details
     */
    @Transactional(readOnly = true)
    public WorkoutExecutionSummary getWorkoutExecutionSummary(String username, Long workoutSessionId) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found: " + workoutSessionId));

        validateWorkoutSessionOwnership(workoutSession, username);

        log.info("📊 Getting execution summary for workout session {}", workoutSessionId);

        // Get all performance records
        List<PerformanceRecord> performanceRecords = performanceRecordRepository
                .findByWorkoutSessionOrderByExerciseIdAscSetNumberAsc(workoutSession);

        // Group by exercise
        Map<Long, List<PerformanceRecord>> recordsByExercise = performanceRecords.stream()
                .collect(Collectors.groupingBy(pr -> pr.getExercise().getId()));

        // Build summary
        List<ExerciseExecutionSummary> exerciseSummaries = new ArrayList<>();

        for (Map.Entry<Long, List<PerformanceRecord>> entry : recordsByExercise.entrySet()) {
            Exercise exercise = findExerciseById(entry.getKey());
            List<PerformanceRecord> exerciseRecords = entry.getValue();

            ExerciseExecutionSummary exerciseSummary =
                    ExerciseExecutionSummary.builder()
                            .exerciseId(exercise.getId())
                            .exerciseName(exercise.getExerciseName())
                            .isCompleted(exerciseRecords.stream().anyMatch(PerformanceRecord::getIsExerciseCompleted))
                            .totalSets(exerciseRecords.size())
                            .averageRpe(calculateAverageRpe(exerciseRecords))
                            .averageFormRating(calculateAverageFormRating(exerciseRecords))
                            .totalVolume(calculateTotalVolume(exerciseRecords))
                            .performanceRecords(exerciseRecords.stream()
                                    .map(this::mapToPerformanceRecordResponse)
                                    .collect(Collectors.toList()))
                            .build();

            exerciseSummaries.add(exerciseSummary);
        }

        return WorkoutExecutionSummary.builder()
                .workoutSessionId(workoutSessionId)
                .sessionStatus(workoutSession.getSessionStatus().toString())
                .totalExercisesPlanned(workoutSession.getTotalExercisesPlanned())
                .totalExercisesCompleted(workoutSession.getTotalExercisesCompleted())
                .completionPercentage(workoutSession.getCompletionPercentage())
                .totalDurationMinutes(workoutSession.getTotalDurationMinutes())
                .exerciseSummaries(exerciseSummaries)
                .overallPerformanceRating(calculateOverallPerformanceRating(performanceRecords))
                .build();
    }

    /**
     * Get batch workout results for completed exercises
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getBatchWorkoutResults(String username, List<String> exerciseIds) {
        User user = findUserByUsername(username);

        log.info("📈 Getting batch workout results for {} scheduled workouts for user {}", exerciseIds.size(), username);

        Map<String, Object> results = new HashMap<>();

        for (String scheduledWorkoutIdStr : exerciseIds) {
            try {
                // ✅ FIXED: Parse as scheduled workout ID, not exercise ID
                Long scheduledWorkoutId = Long.parseLong(scheduledWorkoutIdStr);

                // ✅ FIXED: Find the scheduled workout, not the exercise template
                Optional<ScheduledWorkout> scheduledWorkoutOpt = scheduledWorkoutRepository.findById(scheduledWorkoutId);

                if (scheduledWorkoutOpt.isPresent()) {
                    ScheduledWorkout scheduledWorkout = scheduledWorkoutOpt.get();

                    // Verify ownership and completion status
                    if (scheduledWorkout.getUser().equals(user) &&
                            scheduledWorkout.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED) {

                        // ✅ NEW: Build workout results from the completed scheduled workout
                        Map<String, Object> workoutResults = buildWorkoutResultsFromScheduledWorkout(scheduledWorkout);
                        results.put(scheduledWorkoutIdStr, workoutResults);

                        log.debug("✅ Found workout results for scheduled workout {}", scheduledWorkoutIdStr);
                    } else {
                        log.debug("⚠️ Scheduled workout {} not completed or not owned by user {}", scheduledWorkoutIdStr, username);
                    }
                } else {
                    log.debug("⚠️ Scheduled workout {} not found", scheduledWorkoutIdStr);
                }
            } catch (NumberFormatException e) {
                log.error("❌ Invalid scheduled workout ID: {}", scheduledWorkoutIdStr);
            } catch (Exception e) {
                log.error("❌ Error processing scheduled workout {}: {}", scheduledWorkoutIdStr, e.getMessage());
            }
        }

        results.put("summary", Map.of(
                "totalExercisesAnalyzed", results.size() - 1, // Exclude summary itself
                "generatedAt", LocalDateTime.now().toString()
        ));

        log.info("📊 Returning workout results for {}/{} scheduled workouts", results.size() - 1, exerciseIds.size());
        return results;
    }

    /**
     * ✅ NEW: Build workout results from a completed scheduled workout
     */
    private Map<String, Object> buildWorkoutResultsFromScheduledWorkout(ScheduledWorkout scheduledWorkout) {
        Map<String, Object> workoutResults = new HashMap<>();

        // Get the exercise (either direct or from workout plan)
        Exercise exercise = scheduledWorkout.getResolvedExercise();
        if (exercise == null) {
            log.warn("⚠️ No exercise found for scheduled workout {}", scheduledWorkout.getId());
            return workoutResults;
        }

        // Basic workout information
        workoutResults.put("exerciseId", scheduledWorkout.getId().toString());
        workoutResults.put("exerciseName", exercise.getExerciseName());
        workoutResults.put("completedAt", scheduledWorkout.getCompletedAt() != null ?
                scheduledWorkout.getCompletedAt().toString() : LocalDateTime.now().toString());
        workoutResults.put("totalDurationMinutes", scheduledWorkout.getActualDurationMinutes() != null ?
                scheduledWorkout.getActualDurationMinutes() : scheduledWorkout.getEstimatedDurationMinutes());
        workoutResults.put("performanceRating", "MET"); // Default - you can enhance this later
        workoutResults.put("notes", scheduledWorkout.getCustomNotes());

        // Get performance records if they exist
        if (scheduledWorkout.getCompletedSession() != null) {
            List<PerformanceRecord> performanceRecords = performanceRecordRepository
                    .findByWorkoutSessionAndExerciseOrderBySetNumber(scheduledWorkout.getCompletedSession(), exercise);

            if (!performanceRecords.isEmpty()) {
                // Build sets data
                List<Map<String, Object>> sets = performanceRecords.stream()
                        .map(this::buildSetResultFromPerformanceRecord)
                        .collect(Collectors.toList());
                workoutResults.put("sets", sets);

                // Build metrics based on exercise type
                if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
                    workoutResults.put("cardioMetrics", buildCardioMetrics(performanceRecords, scheduledWorkout));
                } else if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
                    workoutResults.put("isometricMetrics", buildIsometricMetrics(performanceRecords));
                } else {
                    workoutResults.put("strengthMetrics", buildStrengthMetrics(performanceRecords));
                }
            } else {
                // No performance records - build basic set data from scheduled workout configuration
                workoutResults.put("sets", buildBasicSetsFromScheduledWorkout(scheduledWorkout));

                // Build basic metrics
                if (scheduledWorkout.isCardioWorkout()) {
                    workoutResults.put("cardioMetrics", buildBasicCardioMetrics(scheduledWorkout));
                } else if (scheduledWorkout.isIsometricWorkout()) {
                    workoutResults.put("isometricMetrics", buildBasicIsometricMetrics(scheduledWorkout));
                } else {
                    workoutResults.put("strengthMetrics", buildBasicStrengthMetrics(scheduledWorkout));
                }
            }
        } else {
            // No workout session - build basic results from scheduled workout
            workoutResults.put("sets", buildBasicSetsFromScheduledWorkout(scheduledWorkout));
        }

        // Add empty arrays for now (you can implement these later)
        workoutResults.put("personalRecords", List.of());
        workoutResults.put("improvements", List.of());

        return workoutResults;
    }

    /**
     * ✅ NEW: Build set result from performance record
     */
    private Map<String, Object> buildSetResultFromPerformanceRecord(PerformanceRecord record) {
        Map<String, Object> setResult = new HashMap<>();

        setResult.put("setNumber", record.getSetNumber());
        setResult.put("targetReps", record.getTargetRepsPlanned());
        setResult.put("actualReps", record.getReps());
        setResult.put("targetWeight", record.getTargetWeightPlanned());
        setResult.put("actualWeight", record.getWeight());
        setResult.put("targetWeightUnit", "lbs"); // You can enhance this
        setResult.put("rpe", record.getPerceivedExertion());
        setResult.put("restSeconds", record.getRestTimeBeforeSetSeconds());
        setResult.put("completed", record.getIsExerciseCompleted());
        setResult.put("performanceVsTarget", record.getPerformanceVsTarget() != null ?
                record.getPerformanceVsTarget().toString() : "NOT_SET");
        setResult.put("notes", record.getNotes());
        setResult.put("setDurationSeconds", record.calculateActualSetDuration());

        return setResult;
    }

    /**
     * ✅ NEW: Build basic sets when no performance records exist
     */
    private List<Map<String, Object>> buildBasicSetsFromScheduledWorkout(ScheduledWorkout scheduledWorkout) {
        List<Map<String, Object>> sets = new ArrayList<>();

        Integer targetSets = scheduledWorkout.getTargetSets() != null ? scheduledWorkout.getTargetSets() :
                (scheduledWorkout.isCardioWorkout() ? 1 : 3);

        for (int i = 1; i <= targetSets; i++) {
            Map<String, Object> set = new HashMap<>();
            set.put("setNumber", i);
            set.put("targetReps", parseTargetReps(scheduledWorkout.getTargetReps()));
            set.put("actualReps", parseTargetReps(scheduledWorkout.getTargetReps())); // Assume completed as planned
            set.put("targetWeight", scheduledWorkout.getTargetWeight());
            set.put("actualWeight", scheduledWorkout.getTargetWeight());
            set.put("targetWeightUnit", scheduledWorkout.getTargetWeightUnit());
            set.put("rpe", scheduledWorkout.getTargetRpe());
            set.put("restSeconds", scheduledWorkout.getRestSeconds());
            set.put("completed", true);
            set.put("performanceVsTarget", "MET");
            set.put("notes", "");
            sets.add(set);
        }

        return sets;
    }

    /**
     * ✅ NEW: Parse target reps (handles String to Integer conversion)
     */
    private Integer parseTargetReps(String targetReps) {
        if (targetReps == null) return 10; // Default
        try {
            return Integer.parseInt(targetReps);
        } catch (NumberFormatException e) {
            return 10; // Default fallback
        }
    }

    /**
     * ✅ NEW: Build strength metrics from performance records
     */
    private Map<String, Object> buildStrengthMetrics(List<PerformanceRecord> records) {
        Map<String, Object> metrics = new HashMap<>();

        double totalVolume = records.stream()
                .filter(r -> r.getReps() != null && r.getWeight() != null)
                .mapToDouble(r -> r.getReps() * r.getWeight())
                .sum();

        double averageRpe = records.stream()
                .filter(r -> r.getPerceivedExertion() != null)
                .mapToInt(PerformanceRecord::getPerceivedExertion)
                .average()
                .orElse(0.0);

        int totalReps = records.stream()
                .filter(r -> r.getReps() != null)
                .mapToInt(PerformanceRecord::getReps)
                .sum();

        metrics.put("totalVolume", totalVolume);
        metrics.put("averageRpe", averageRpe);
        metrics.put("totalReps", totalReps);

        return metrics;
    }

    /**
     * ✅ NEW: Build basic strength metrics from scheduled workout
     */
    private Map<String, Object> buildBasicStrengthMetrics(ScheduledWorkout scheduledWorkout) {
        Map<String, Object> metrics = new HashMap<>();

        Integer sets = scheduledWorkout.getTargetSets() != null ? scheduledWorkout.getTargetSets() : 3;
        Integer reps = parseTargetReps(scheduledWorkout.getTargetReps());
        Double weight = scheduledWorkout.getTargetWeight() != null ? scheduledWorkout.getTargetWeight() : 0.0;

        metrics.put("totalVolume", sets * reps * weight);
        metrics.put("averageRpe", scheduledWorkout.getTargetRpe() != null ? scheduledWorkout.getTargetRpe() : 7.0);
        metrics.put("totalReps", sets * reps);

        return metrics;
    }

    /**
     * ✅ NEW: Build cardio metrics from performance records
     */
    private Map<String, Object> buildCardioMetrics(List<PerformanceRecord> records, ScheduledWorkout scheduledWorkout) {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("totalDurationMinutes", scheduledWorkout.getActualDurationMinutes() != null ?
                scheduledWorkout.getActualDurationMinutes() : scheduledWorkout.getTargetDurationMinutes());
        metrics.put("totalDistanceKm", records.stream()
                .filter(r -> r.getDistanceKm() != null)
                .mapToDouble(PerformanceRecord::getDistanceKm)
                .sum());
        metrics.put("totalCaloriesBurned", records.stream()
                .filter(r -> r.getCaloriesBurned() != null)
                .mapToInt(PerformanceRecord::getCaloriesBurned)
                .sum());

        return metrics;
    }

    /**
     * ✅ NEW: Build basic cardio metrics from scheduled workout
     */
    private Map<String, Object> buildBasicCardioMetrics(ScheduledWorkout scheduledWorkout) {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("totalDurationMinutes", scheduledWorkout.getActualDurationMinutes() != null ?
                scheduledWorkout.getActualDurationMinutes() : scheduledWorkout.getTargetDurationMinutes());
        metrics.put("totalDistanceKm", scheduledWorkout.getTargetDistanceKm());
        metrics.put("averagePace", scheduledWorkout.getTargetPace());
        metrics.put("totalCaloriesBurned", 0); // Could estimate based on duration

        return metrics;
    }

    /**
     * ✅ NEW: Build isometric metrics from performance records
     */
    private Map<String, Object> buildIsometricMetrics(List<PerformanceRecord> records) {
        Map<String, Object> metrics = new HashMap<>();

        int totalHoldTime = records.stream()
                .filter(r -> r.getHoldDurationSeconds() != null)
                .mapToInt(PerformanceRecord::getHoldDurationSeconds)
                .sum();

        double averageHoldTime = records.stream()
                .filter(r -> r.getHoldDurationSeconds() != null)
                .mapToInt(PerformanceRecord::getHoldDurationSeconds)
                .average()
                .orElse(0.0);

        int longestHold = records.stream()
                .filter(r -> r.getHoldDurationSeconds() != null)
                .mapToInt(PerformanceRecord::getHoldDurationSeconds)
                .max()
                .orElse(0);

        metrics.put("totalHoldTimeSeconds", totalHoldTime);
        metrics.put("averageHoldTimeSeconds", averageHoldTime);
        metrics.put("longestHoldSeconds", longestHold);

        return metrics;
    }

    /**
     * ✅ NEW: Build basic isometric metrics from scheduled workout
     */
    private Map<String, Object> buildBasicIsometricMetrics(ScheduledWorkout scheduledWorkout) {
        Map<String, Object> metrics = new HashMap<>();

        Integer sets = scheduledWorkout.getTargetSets() != null ? scheduledWorkout.getTargetSets() : 3;
        Integer holdDuration = scheduledWorkout.getHoldDurationSeconds() != null ?
                scheduledWorkout.getHoldDurationSeconds() : 30;

        metrics.put("totalHoldTimeSeconds", sets * holdDuration);
        metrics.put("averageHoldTimeSeconds", (double) holdDuration);
        metrics.put("longestHoldSeconds", holdDuration);

        return metrics;
    }

// =======================
// HELPER METHODS FOR PERFORMANCE TRACKING
// =======================

    private void validateWorkoutSessionOwnership(WorkoutSession workoutSession, String username) {
        if (!workoutSession.getUser().getUsername().equals(username)) {
            throw new UnauthorizedScheduledWorkoutAccessException(
                    "User does not have access to this workout session");
        }
    }

    private PerformanceRecord.PerformanceVsTarget calculatePerformanceVsTarget(PerformanceRecord record) {
        // Compare actual vs target performance
        if (record.getTargetRepsPlanned() != null && record.getReps() != null) {
            if (record.getReps() > record.getTargetRepsPlanned()) {
                return PerformanceRecord.PerformanceVsTarget.EXCEEDED;
            } else if (record.getReps().equals(record.getTargetRepsPlanned())) {
                return PerformanceRecord.PerformanceVsTarget.MET;
            } else {
                return PerformanceRecord.PerformanceVsTarget.BELOW;
            }
        }

        // For cardio, compare duration
        if (record.getDurationMinutes() != null && record.getTargetRepsPlanned() != null) {
            if (record.getDurationMinutes() >= record.getTargetRepsPlanned()) {
                return PerformanceRecord.PerformanceVsTarget.MET;
            } else {
                return PerformanceRecord.PerformanceVsTarget.BELOW;
            }
        }

        return PerformanceRecord.PerformanceVsTarget.NOT_SET;
    }

    private Double calculateAverageRpe(List<PerformanceRecord> records) {
        return records.stream()
                .filter(r -> r.getPerceivedExertion() != null)
                .mapToInt(PerformanceRecord::getPerceivedExertion)
                .average()
                .orElse(0.0);
    }

    private Double calculateAverageFormRating(List<PerformanceRecord> records) {
        return records.stream()
                .filter(r -> r.getFormRating() != null)
                .mapToInt(PerformanceRecord::getFormRating)
                .average()
                .orElse(0.0);
    }

    private Double calculateTotalVolume(List<PerformanceRecord> records) {
        return records.stream()
                .filter(r -> r.getReps() != null && r.getWeight() != null)
                .mapToDouble(r -> r.getReps() * r.getWeight())
                .sum();
    }

    private String calculateOverallPerformanceRating(List<PerformanceRecord> records) {
        long metOrExceeded = records.stream()
                .filter(r -> r.getPerformanceVsTarget() == PerformanceRecord.PerformanceVsTarget.MET ||
                        r.getPerformanceVsTarget() == PerformanceRecord.PerformanceVsTarget.EXCEEDED)
                .count();

        double percentage = records.isEmpty() ? 0 : (double) metOrExceeded / records.size();

        if (percentage >= 0.9) return "EXCELLENT";
        if (percentage >= 0.7) return "GOOD";
        if (percentage >= 0.5) return "AVERAGE";
        return "NEEDS_IMPROVEMENT";
    }

    private Map<String, Object> getBestPerformance(List<PerformanceRecord> records) {
        // Find the best performance based on the exercise type
        PerformanceRecord bestRecord = records.stream()
                .max((r1, r2) -> {
                    if (r1.getWeight() != null && r2.getWeight() != null) {
                        return Double.compare(r1.getWeight(), r2.getWeight());
                    }
                    if (r1.getDurationMinutes() != null && r2.getDurationMinutes() != null) {
                        return Integer.compare(r1.getDurationMinutes(), r2.getDurationMinutes());
                    }
                    return 0;
                })
                .orElse(null);

        if (bestRecord != null) {
            Map<String, Object> best = new HashMap<>();
            best.put("date", bestRecord.getCreatedAt().toLocalDate());
            if (bestRecord.getWeight() != null) {
                best.put("weight", bestRecord.getWeight());
                best.put("reps", bestRecord.getReps());
            }
            if (bestRecord.getDurationMinutes() != null) {
                best.put("duration", bestRecord.getDurationMinutes());
            }
            return best;
        }

        return Map.of();
    }

    private String calculateProgressTrend(List<PerformanceRecord> records) {
        if (records.size() < 2) return "INSUFFICIENT_DATA";

        // Sort by date
        records.sort((r1, r2) -> r1.getCreatedAt().compareTo(r2.getCreatedAt()));

        // Compare first half vs second half
        int midPoint = records.size() / 2;
        List<PerformanceRecord> firstHalf = records.subList(0, midPoint);
        List<PerformanceRecord> secondHalf = records.subList(midPoint, records.size());

        double firstAvg = calculateAverageRpe(firstHalf);
        double secondAvg = calculateAverageRpe(secondHalf);

        if (secondAvg > firstAvg + 0.5) return "IMPROVING";
        if (firstAvg > secondAvg + 0.5) return "DECLINING";
        return "STABLE";
    }

    private WorkoutSessionResponse mapToWorkoutSessionResponse(WorkoutSession session) {
        return WorkoutSessionResponse.builder()
                .id(session.getId())
                .sessionStatus(session.getSessionStatus().toString())
                .totalExercisesPlanned(session.getTotalExercisesPlanned())
                .totalExercisesCompleted(session.getTotalExercisesCompleted())
                .completionPercentage(session.getCompletionPercentage())
                .totalDurationMinutes(session.getTotalDurationMinutes())
                .startedAt(session.getCreatedAt())
                .completedAt(session.getUpdatedAt())
                .date(session.getDate())
                .build();
    }

    /**
     * PerformanceResponse mapping
     */
    private PerformanceResponse mapToPerformanceRecordResponse(PerformanceRecord record) {
        return PerformanceResponse.builder()
                .id(record.getId())
                .exerciseId(record.getExercise().getId())
                .exerciseName(record.getExercise().getExerciseName())
                .setNumber(record.getSetNumber())
                .reps(record.getReps())
                .weight(record.getWeight())
                .durationMinutes(record.getDurationMinutes())
                .perceivedExertion(record.getPerceivedExertion())
                .formRating(record.getFormRating())
                .performanceVsTarget(record.getPerformanceVsTarget().toString())
                .isExerciseCompleted(record.getIsExerciseCompleted())
                .recordedAt(record.getCreatedAt())
                .workoutSessionId(record.getWorkoutSession().getId())
                .workoutDate(record.getWorkoutSession().getDate())
                .restTimeBeforeSetSeconds(record.getRestTimeBeforeSetSeconds())
                .setStartTime(record.getSetStartTime())
                .setEndTime(record.getSetEndTime())
                .targetRepsPlanned(record.getTargetRepsPlanned())
                .targetWeightPlanned(record.getTargetWeightPlanned())
                .exerciseCompletionNotes(record.getExerciseCompletionNotes())
                .build();
    }

    // =======================
    // ENHANCED CALENDAR VIEWS
    // =======================

    /**
     * Get exercises for a specific date
     */
    public List<ScheduledWorkoutResponse> getExercisesForDate(String username, LocalDate date) {
        User user = findUserByUsername(username);

        log.debug("📅 Getting exercises for user {} on {}", username, date);

        List<ScheduledWorkout> exercises = scheduledWorkoutRepository
                .findByUserAndScheduledDateOrderByCreatedAtAsc(user, date);

        return exercises.stream()
                .map(scheduledWorkoutMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get scheduled exercises for a date range
     */
    public List<ScheduledWorkoutResponse> getScheduledExercisesForDateRange(String username,
                                                                            LocalDate startDate, LocalDate endDate) {
        User user = findUserByUsername(username);

        log.debug("📅 Getting exercises for user {} from {} to {}", username, startDate, endDate);

        List<ScheduledWorkout> exercises = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startDate, endDate);

        return exercises.stream()
                .map(scheduledWorkoutMapper::toResponse)
                .collect(Collectors.toList());
    }

    // =======================
    // ✅ FIXED: WORKOUT STATISTICS
    // =======================

    /**
     * Get comprehensive workout statistics
     */
    public ScheduledWorkoutController.WorkoutStatsResponse getWorkoutStats(String username, LocalDate date) {
        User user = findUserByUsername(username);
        LocalDate today = LocalDate.now();

        log.debug("📊 Getting workout statistics for user {} (date: {})",
                username, date != null ? date : "all time");

        // Calculate date ranges
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        // Today's stats
        List<ScheduledWorkout> todaysWorkouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateOrderByCreatedAtAsc(user, today);
        int exercisesScheduledToday = todaysWorkouts.size();
        int exercisesCompletedToday = (int) todaysWorkouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .count();
        int minutesWorkedOutToday = todaysWorkouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .mapToInt(sw -> sw.getEstimatedDurationMinutes() != null ? sw.getEstimatedDurationMinutes() : 0)
                .sum();

        // This week's stats
        List<ScheduledWorkout> weekWorkouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, weekStart, weekEnd);
        int exercisesScheduledThisWeek = weekWorkouts.size();
        int exercisesCompletedThisWeek = (int) weekWorkouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .count();
        int minutesWorkedOutThisWeek = weekWorkouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .mapToInt(sw -> sw.getEstimatedDurationMinutes() != null ? sw.getEstimatedDurationMinutes() : 0)
                .sum();

        // This month's stats
        List<ScheduledWorkout> monthWorkouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, monthStart, monthEnd);
        int exercisesScheduledThisMonth = monthWorkouts.size();
        int exercisesCompletedThisMonth = (int) monthWorkouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .count();
        int minutesWorkedOutThisMonth = monthWorkouts.stream()
                .filter(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED)
                .mapToInt(sw -> sw.getEstimatedDurationMinutes() != null ? sw.getEstimatedDurationMinutes() : 0)
                .sum();

        // Completion rates
        double completionRateThisWeek = exercisesScheduledThisWeek > 0 ?
                (double) exercisesCompletedThisWeek / exercisesScheduledThisWeek * 100 : 0.0;
        double completionRateThisMonth = exercisesScheduledThisMonth > 0 ?
                (double) exercisesCompletedThisMonth / exercisesScheduledThisMonth * 100 : 0.0;

        // Calculate streaks and other metrics
        int currentStreak = calculateCurrentStreak(user);
        int longestStreak = calculateLongestStreak(user);
        LocalDate lastWorkoutDate = findLastWorkoutDate(user);
        String lastWorkoutType = findLastWorkoutType(user);
        String favoriteExerciseType = findFavoriteExerciseType(user);

        // Get total completed workouts and minutes
        List<ScheduledWorkout> allCompletedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduledWorkout.ScheduleStatus.COMPLETED);
        int totalWorkoutsCompleted = allCompletedWorkouts.size();
        int totalMinutesWorkedOut = allCompletedWorkouts.stream()
                .mapToInt(sw -> sw.getEstimatedDurationMinutes() != null ? sw.getEstimatedDurationMinutes() : 0)
                .sum();

        return ScheduledWorkoutController.WorkoutStatsResponse.builder()
                .exercisesScheduledToday(exercisesScheduledToday)
                .exercisesCompletedToday(exercisesCompletedToday)
                .minutesWorkedOutToday(minutesWorkedOutToday)
                .exercisesScheduledThisWeek(exercisesScheduledThisWeek)
                .exercisesCompletedThisWeek(exercisesCompletedThisWeek)
                .minutesWorkedOutThisWeek(minutesWorkedOutThisWeek)
                .exercisesScheduledThisMonth(exercisesScheduledThisMonth)
                .exercisesCompletedThisMonth(exercisesCompletedThisMonth)
                .minutesWorkedOutThisMonth(minutesWorkedOutThisMonth)
                .currentStreak(currentStreak)
                .longestStreak(longestStreak)
                .completionRateThisWeek(completionRateThisWeek)
                .completionRateThisMonth(completionRateThisMonth)
                .lastWorkoutDate(lastWorkoutDate)
                .lastWorkoutType(lastWorkoutType)
                .totalWorkoutsCompleted(totalWorkoutsCompleted)
                .totalMinutesWorkedOut(totalMinutesWorkedOut)
                .favoriteExerciseType(favoriteExerciseType)
                .build();
    }

    // =======================
    // ✅ EXISTING: WORKOUT PLAN SCHEDULING (SUBSCRIPTION-AWARE)
    // =======================

    /**
     * Schedule multiple exercises from a workout plan with subscription validation
     */
    @Transactional
    public List<ScheduledWorkoutResponse> scheduleWorkoutPlan(String username, ScheduleMultipleExercisesRequestDTO request) {
        User user = findUserByUsername(username);
        WorkoutPlan workoutPlan = findWorkoutPlanById(request.getWorkoutPlanId());

        log.info("🎯 Scheduling workout plan: {} for user {} on {}",
                workoutPlan.getWorkoutName(), username, request.getScheduledDate());

        // Get exercises from the workout plan
        List<PlanExercise> planExercises = planExerciseRepository.findByWorkoutPlanOrderByOrderInWorkout(workoutPlan);

        if (planExercises.isEmpty()) {
            throw new IllegalArgumentException("Workout plan has no exercises configured");
        }

        // ✅ CRITICAL: Validate subscription limits BEFORE scheduling
        validateWorkoutPlanScheduling(user, request, planExercises.size());

        List<ScheduledWorkout> scheduledWorkouts = new ArrayList<>();

        // Determine which exercises to schedule (respect FREE user limits)
        List<PlanExercise> exercisesToSchedule = getExercisesToSchedule(planExercises, request, user);

        log.info("📋 Scheduling {} out of {} exercises for {} user",
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

        log.info("✅ Successfully scheduled {} exercises from workout plan for user {}",
                saved.size(), username);

        return saved.stream()
                .map(scheduledWorkoutMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Schedule a single workout (individual exercise or workout plan)
     */
    @Transactional
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
    @Transactional
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

        log.info("Workout rescheduled: {} moved to {} for user {}",
                scheduledWorkoutId, newDate, username);

        return scheduledWorkoutMapper.toResponse(saved);
    }

    /**
     * Permanently delete a scheduled workout from the database
     */
    @Transactional
    public void permanentlyDeleteScheduledWorkout(String username, Long scheduledWorkoutId) {
        log.debug("Attempting to permanently delete scheduled workout {} for user {}", scheduledWorkoutId, username);

        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(scheduledWorkoutId);
        validateOwnership(scheduledWorkout, username);
        validateWorkoutCanBeDeleted(scheduledWorkout);

        try {
            handleRelatedRecordsBeforeDeletion(scheduledWorkout);
            scheduledWorkoutRepository.delete(scheduledWorkout);

            log.info("Successfully permanently deleted scheduled workout {} for user {}",
                    scheduledWorkoutId, username);

        } catch (Exception e) {
            log.error("Failed to delete scheduled workout {} for user {}: {}",
                    scheduledWorkoutId, username, e.getMessage());
            throw new RuntimeException("Failed to delete workout: " + e.getMessage());
        }
    }


    // =======================
    // CALENDAR VIEW & RETRIEVAL
    // =======================

    /**
     * Get calendar view with exercise configuration details
     */
    public CalendarViewResponse getCalendarView(String username, LocalDate startDate, LocalDate endDate) {
        User user = findUserByUsername(username);

        List<ScheduledWorkout> scheduledWorkouts = scheduledWorkoutRepository
                .findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(user, startDate, endDate);

        Map<LocalDate, List<ScheduledWorkoutResponse>> calendarData = scheduledWorkouts.stream()
                .collect(Collectors.groupingBy(
                        ScheduledWorkout::getScheduledDate,
                        LinkedHashMap::new,
                        Collectors.mapping(scheduledWorkoutMapper::toResponse, Collectors.toList())
                ));

        return CalendarViewResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .workoutsByDate(calendarData)
                .totalScheduled(scheduledWorkouts.size())
                .build();
    }

    /**
     * Get today's scheduled workouts
     */
    public List<ScheduledWorkoutResponse> getTodaysWorkouts(String username) {
        User user = findUserByUsername(username);
        List<ScheduledWorkout> todaysWorkouts = scheduledWorkoutRepository.findTodaysWorkouts(user);
        return scheduledWorkoutMapper.toResponseList(todaysWorkouts);
    }

    /**
     * Get upcoming workouts for the next N days
     */
    public List<ScheduledWorkoutResponse> getUpcomingWorkouts(String username, int days) {
        User user = findUserByUsername(username);
        LocalDate endDate = LocalDate.now().plusDays(days);
        List<ScheduledWorkout> upcomingWorkouts = scheduledWorkoutRepository
                .findUpcomingWorkouts(user, endDate);
        return scheduledWorkoutMapper.toResponseList(upcomingWorkouts);
    }

    /**
     * Get overdue workouts (scheduled in the past but not completed)
     */
    public List<ScheduledWorkoutResponse> getOverdueWorkouts(String username) {
        User user = findUserByUsername(username);
        List<ScheduledWorkout> overdueWorkouts = scheduledWorkoutRepository.findOverdueWorkouts(user);
        return scheduledWorkoutMapper.toResponseList(overdueWorkouts);
    }

    // =======================
    // WORKOUT EXECUTION
    // =======================

    /**
     * Start a scheduled workout (sets status to IN_PROGRESS)
     */
    @Transactional
    public ScheduledWorkoutResponse startScheduledWorkout(String username, Long scheduledWorkoutId) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(scheduledWorkoutId);
        validateOwnership(scheduledWorkout, username);

        // Check if user already has a workout in progress
        boolean hasInProgress = scheduledWorkoutRepository.existsByUserAndStatus(
                scheduledWorkout.getUser(), ScheduledWorkout.ScheduleStatus.IN_PROGRESS);

        if (hasInProgress) {
            throw new WorkoutInProgressException("Cannot start new workout while another is in progress");
        }

        scheduledWorkout.startWorkout();
        ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Workout started: {} for user {}", scheduledWorkoutId, username);

        return scheduledWorkoutMapper.toResponse(saved);
    }

    /**
     * Complete a scheduled workout with session data
     */
    @Transactional
    public ScheduledWorkoutResponse completeScheduledWorkout(String username, Long scheduledWorkoutId,
                                                             WorkoutSession workoutSession) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(scheduledWorkoutId);
        validateOwnership(scheduledWorkout, username);

        // Validate workout session belongs to this scheduled workout
        if (!workoutSession.getWorkoutPlan().getId().equals(scheduledWorkout.getWorkoutPlan().getId())) {
            throw new IllegalArgumentException("Workout session does not match scheduled workout plan");
        }

        scheduledWorkout.completeWorkout(workoutSession);
        ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Workout completed: {} for user {}", scheduledWorkoutId, username);

        return scheduledWorkoutMapper.toResponse(saved);
    }

    // =======================
    // PROGRAM SCHEDULING
    // =======================

    /**
     * Schedule an entire workout program
     */
    @Transactional
    public List<ScheduledWorkoutResponse> scheduleProgram(String username, Long programId, LocalDate startDate) {
        User user = findUserByUsername(username);
        WorkoutProgram program = findWorkoutProgramById(programId);

        validateProgramScheduling(user, program, startDate);

        List<ScheduledWorkout> scheduledWorkouts = createProgramSchedule(user, program, startDate);
        List<ScheduledWorkout> savedWorkouts = scheduledWorkoutRepository.saveAll(scheduledWorkouts);

        log.info("Program scheduled: {} starting {} for user {}",
                program.getName(), startDate, username);

        return scheduledWorkoutMapper.toResponseList(savedWorkouts);
    }

    /**
     * Get scheduled workouts for a specific program
     */
    public List<ScheduledWorkoutResponse> getProgramSchedule(String username, Long programId) {
        User user = findUserByUsername(username);
        WorkoutProgram program = findWorkoutProgramById(programId);

        List<ScheduledWorkout> programWorkouts = scheduledWorkoutRepository
                .findByUserAndProgramOrderByWeekNumberAscDayOfWeekAsc(user, program);

        return scheduledWorkoutMapper.toResponseList(programWorkouts);
    }

    // =======================
    // ANALYTICS & STATISTICS
    // =======================

    /**
     * Get scheduling analytics for a date range
     */
    public Map<String, Object> getSchedulingAnalytics(String username, LocalDate startDate, LocalDate endDate) {
        User user = findUserByUsername(username);

        Long totalScheduled = scheduledWorkoutRepository
                .countScheduledWorkoutsInRange(user, startDate, endDate);

        Double completionRate = scheduledWorkoutRepository
                .calculateCompletionRate(user, startDate, endDate)
                .orElse(0.0);

        List<Object[]> frequencyByDay = scheduledWorkoutRepository
                .getWorkoutFrequencyByDayOfWeek(user);

        return Map.of(
                "totalScheduled", totalScheduled,
                "completionRate", completionRate,
                "frequencyByDayOfWeek", frequencyByDay,
                "period", Map.of("start", startDate, "end", endDate)
        );
    }

    /**
     * Create comprehensive exercise analysis report (for debugging)
     */
    public void logExerciseAnalysis(String username, LocalDate date) {
        try {
            User user = findUserByUsername(username);
            List<ScheduledWorkout> workouts = scheduledWorkoutRepository
                    .findByUserAndScheduledDateOrderByCreatedAtAsc(user, date);

            log.info("🔍 Exercise Analysis Report for {} on {}:", username, date);
            log.info("📊 Found {} scheduled workouts", workouts.size());

            for (ScheduledWorkout workout : workouts) {
                Exercise exercise = exerciseMapper.extractExerciseFromScheduledWorkout(workout);

                if (exercise != null) {
                    String trackingMode = exerciseMapper.getExerciseTypeForFrontend(exercise);
                    log.info("🏋️ Workout {}: {} → Frontend Type: {} (Cardio: {}, Isometric: {})",
                            workout.getId(),
                            exercise.getExerciseName(),
                            trackingMode,
                            exercise.getIsCardio(),
                            exercise.getIsIsometric());
                } else {
                    log.info("❓ Workout {}: No exercise resolved (Plan: {})",
                            workout.getId(),
                            workout.getWorkoutPlan() != null ? workout.getWorkoutPlan().getWorkoutName() : "None");
                }
            }

        } catch (Exception e) {
            log.error("❌ Failed to generate exercise analysis: {}", e.getMessage(), e);
        }
    }

    // =======================
    // DATA RETENTION (FREE USER LIMITS)
    // =======================

    /**
     * Clean up old scheduled workouts for FREE users (30-day retention)
     */
    @Transactional
    public void cleanupOldScheduledWorkouts(String username) {
        User user = findUserByUsername(username);

        // Only cleanup for free users
        if (user.getSubscriptionTier() != SubscriptionTier.FREE) {
            return;
        }

        LocalDate cutoffDate = LocalDate.now().minusDays(30);
        List<ScheduledWorkout> oldWorkouts = scheduledWorkoutRepository
                .findOldWorkoutsForCleanup(user, cutoffDate);

        scheduledWorkoutRepository.deleteAll(oldWorkouts);

        log.info("Cleaned up {} old scheduled workouts for free user {}",
                oldWorkouts.size(), username);
    }

    // =======================
    // ✅ HELPER METHODS FOR EXERCISE CONFIGURATION
    // =======================

    /**
     * Set exercise configuration on scheduled workout from individual request
     */
    private void setExerciseConfiguration(ScheduledWorkout scheduledWorkout, Exercise exercise,
                                          IndividualExerciseRequest request) {
        if (exercise.getIsCardio()) {
            scheduledWorkout.setTargetDurationMinutes(request.getTargetDurationMinutes());
            scheduledWorkout.setTargetDistanceKm(request.getTargetDistanceKm());
            scheduledWorkout.setTargetPace(request.getTargetPace());
        } else if (exercise.getIsIsometric()) {
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

    /**
     * Set exercise configuration from plan exercise
     */
    private void setExerciseConfigurationFromPlan(ScheduledWorkout scheduledWorkout, PlanExercise planExercise) {
        Exercise exercise = planExercise.getExercise();

        if (exercise.getIsCardio()) {
            scheduledWorkout.setTargetDurationMinutes(planExercise.getPrescribedSets());
            scheduledWorkout.setTargetDistanceKm(planExercise.getPrescribedWeightPercent());
            scheduledWorkout.setTargetPace(planExercise.getPrescribedRpe() != null ? planExercise.getPrescribedRpe().doubleValue() : null);
        } else if (exercise.getIsIsometric()) {
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

    // =======================
    //  INDIVIDUAL EXERCISE HELPERS
    // =======================

    /**
     * Create individual exercise plan from request
     */
    private WorkoutPlan createIndividualExercisePlan(Exercise exercise,
                                                     IndividualExerciseRequest request) {
        WorkoutPlan individualPlan = new WorkoutPlan();
        individualPlan.setWorkoutName(exercise.getExerciseName());
        individualPlan.setWorkoutDescription("Individual exercise from workout plan");
        individualPlan.setDifficultyLevel(DifficultyLevel.INTERMEDIATE);
        individualPlan.setEstimatedDurationMinutes(calculateIndividualExerciseDuration(exercise, request));

        // Set category based on exercise
        if (exercise.getTargetMuscleGroups() != null && !exercise.getTargetMuscleGroups().isEmpty()) {
            individualPlan.setWorkoutCategory(exercise.getTargetMuscleGroups().get(0));
        } else if (exercise.getExerciseType() != null) {
            individualPlan.setWorkoutCategory(exercise.getExerciseType().getDisplayName());
        } else {
            individualPlan.setWorkoutCategory("General");
        }

        individualPlan.setSubscriptionTierRequired("FREE");
        individualPlan.setIsPublic(false);

        // Save the plan first to get an ID
        WorkoutPlan savedPlan = workoutPlanRepository.save(individualPlan);

        // Create and save the PlanExercise to store configuration
        PlanExercise planExercise = new PlanExercise();
        planExercise.setWorkoutPlan(savedPlan);
        planExercise.setExercise(exercise);
        planExercise.setOrderInWorkout(1);

        // Set configuration based on exercise type and request
        if (exercise.getIsCardio()) {
            planExercise.setPrescribedSets(request.getTargetDurationMinutes());
            planExercise.setPrescribedWeightPercent(request.getTargetDistanceKm());
            planExercise.setPrescribedRpe(request.getTargetRpe() != null ? request.getTargetRpe().intValue() : null);
        } else if (exercise.getIsIsometric()) {
            planExercise.setPrescribedSets(request.getSets() != null ? request.getSets() : 3);
            planExercise.setPrescribedRestSeconds(request.getHoldDurationSeconds() != null ? request.getHoldDurationSeconds() : 30);
        } else {
            planExercise.setPrescribedSets(request.getSets() != null ? request.getSets() : 3);
            planExercise.setPrescribedReps(request.getReps() != null ? request.getReps() : "10");
            planExercise.setPrescribedWeightPercent(request.getWeight());
            planExercise.setPrescribedRestSeconds(request.getRestSeconds() != null ? request.getRestSeconds() : 90);
            planExercise.setPrescribedTempo(request.getTempo());
            planExercise.setPrescribedRpe(request.getTargetRpe() != null ? request.getTargetRpe().intValue() : null);
        }

        planExerciseRepository.save(planExercise);

        return savedPlan;
    }

    /**
     * Calculate duration for individual exercise based on type and configuration
     */
    private Integer calculateIndividualExerciseDuration(Exercise exercise,
                                                        IndividualExerciseRequest request) {
        if (exercise.getIsCardio()) {
            return request.getTargetDurationMinutes() != null ? request.getTargetDurationMinutes() : 30;
        } else if (exercise.getIsIsometric()) {
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

    /**
     * Validate individual exercise scheduling with subscription limits
     */
    private void validateIndividualExerciseScheduling(User user,
                                                      IndividualExerciseRequest request) {
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

    // =======================
    //  STATISTICS CALCULATION HELPERS
    // =======================

    private int calculateCurrentStreak(User user) {
        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate checkDate = today;

        while (true) {
            List<ScheduledWorkout> dayWorkouts = scheduledWorkoutRepository
                    .findByUserAndScheduledDateOrderByCreatedAtAsc(user, checkDate);

            boolean hasCompletedWorkout = dayWorkouts.stream()
                    .anyMatch(sw -> sw.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED);

            if (hasCompletedWorkout) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }

            if (streak >= 365) break;
        }

        return streak;
    }

    private int calculateLongestStreak(User user) {
        List<ScheduledWorkout> allCompletedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduledWorkout.ScheduleStatus.COMPLETED);

        if (allCompletedWorkouts.isEmpty()) return 0;

        Set<LocalDate> completedDates = allCompletedWorkouts.stream()
                .map(ScheduledWorkout::getScheduledDate)
                .collect(Collectors.toSet());

        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate earliestDate = allCompletedWorkouts.get(0).getScheduledDate();
        LocalDate latestDate = allCompletedWorkouts.get(allCompletedWorkouts.size() - 1).getScheduledDate();

        for (LocalDate date = earliestDate; !date.isAfter(latestDate); date = date.plusDays(1)) {
            if (completedDates.contains(date)) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        return longestStreak;
    }

    private LocalDate findLastWorkoutDate(User user) {
        List<ScheduledWorkout> completedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduledWorkout.ScheduleStatus.COMPLETED);

        return completedWorkouts.isEmpty() ? null :
                completedWorkouts.get(completedWorkouts.size() - 1).getScheduledDate();
    }

    private String findLastWorkoutType(User user) {
        List<ScheduledWorkout> completedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduledWorkout.ScheduleStatus.COMPLETED);

        if (completedWorkouts.isEmpty()) return null;

        ScheduledWorkout lastWorkout = completedWorkouts.get(completedWorkouts.size() - 1);
        WorkoutPlan plan = lastWorkout.getWorkoutPlan();
        return plan != null ? plan.getWorkoutCategory() : "Unknown";
    }

    private String findFavoriteExerciseType(User user) {
        List<ScheduledWorkout> completedWorkouts = scheduledWorkoutRepository
                .findByUserAndStatusOrderByScheduledDateAsc(user, ScheduledWorkout.ScheduleStatus.COMPLETED);

        Map<String, Long> typeFrequency = completedWorkouts.stream()
                .filter(sw -> sw.getWorkoutPlan() != null && sw.getWorkoutPlan().getWorkoutCategory() != null)
                .collect(Collectors.groupingBy(
                        sw -> sw.getWorkoutPlan().getWorkoutCategory(),
                        Collectors.counting()
                ));

        return typeFrequency.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Mixed");
    }

    // =======================
    //  WORKOUT PLAN SCHEDULING HELPERS
    // =======================

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

    private WorkoutPlan createIndividualExercisePlan(PlanExercise planExercise) {
        WorkoutPlan individualPlan = new WorkoutPlan();
        individualPlan.setWorkoutName(planExercise.getExercise().getExerciseName());
        individualPlan.setWorkoutDescription("Individual exercise from workout plan");
        individualPlan.setDifficultyLevel(DifficultyLevel.INTERMEDIATE);
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

    private String buildExerciseNotes(String baseNotes, PlanExercise planExercise, int exerciseNumber) {
        StringBuilder notes = new StringBuilder();

        if (baseNotes != null && !baseNotes.trim().isEmpty()) {
            notes.append(baseNotes);
        }

        Exercise exercise = planExercise.getExercise();
        if (exercise.getIsCardio()) {
            notes.append(notes.length() > 0 ? " | " : "").append("Cardio: ");
            if (planExercise.getPrescribedSets() != null) {
                notes.append(planExercise.getPrescribedSets()).append(" min");
            }
        } else if (exercise.getIsIsometric()) {
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

    private Integer calculateExerciseDuration(PlanExercise planExercise) {
        Exercise exercise = planExercise.getExercise();

        if (exercise.getIsCardio()) {
            return planExercise.getPrescribedSets() != null ? planExercise.getPrescribedSets() : 30;
        } else if (exercise.getIsIsometric()) {
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

    // =======================
    // SUBSCRIPTION ENFORCEMENT & VALIDATION
    // =======================

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

    // =======================
    // DELETION & CLEANUP VALIDATION
    // =======================

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

    // =======================
    // CONFLICT DETECTION
    // =======================

    private void checkForSchedulingConflicts(User user, LocalDate date, Long excludeId) {
        Long excludeIdSafe = excludeId != null ? excludeId : -1L;

        List<ScheduledWorkout> conflicts = scheduledWorkoutRepository
                .findSchedulingConflicts(user, date, excludeIdSafe);

        if (!conflicts.isEmpty()) {
            log.warn("Scheduling conflict detected: user {} already has {} workout(s) on {}",
                    user.getUsername(), conflicts.size(), date);
        }
    }

    // =======================
    // PROGRAM SCHEDULING HELPERS
    // =======================

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

    // =======================
    // ✅ MISSING METHODS FROM CALENDAR CONTROLLER
    // =======================

    /**
     * Schedule workout plan (alternative endpoint for calendar)
     */
    @Transactional
    public ScheduledWorkoutResponse scheduleWorkoutPlan(String username, ScheduledWorkoutRequest request) {
        // This method already exists above as scheduleWorkout() - just delegate to it
        return scheduleWorkout(username, request);
    }

    /**
     * Update scheduled exercise with String ID (for calendar controller compatibility)
     */
    @Transactional
    public ScheduledWorkoutResponse updateScheduledExercise(String username, String exerciseId,
                                                            IndividualExerciseRequest updates) {
        // Convert String ID to Long and delegate to existing method
        Long exerciseIdLong = Long.parseLong(exerciseId);
        return updateScheduledExercise(username, exerciseIdLong, updates);
    }

    /**
     * Delete scheduled exercise with String ID (for calendar controller compatibility)
     */
    @Transactional
    public void deleteScheduledExercise(String username, String exerciseId) {
        // Convert String ID to Long and delegate to existing method
        Long exerciseIdLong = Long.parseLong(exerciseId);
        deleteScheduledExercise(username, exerciseIdLong);
    }

    /**
     * Mark exercise completed with String ID (for calendar controller compatibility)
     */
    @Transactional
    public ScheduledWorkoutResponse markExerciseCompleted(String username, String exerciseId) {
        // Convert String ID to Long and delegate to existing method
        Long exerciseIdLong = Long.parseLong(exerciseId);
        return markExerciseCompleted(username, exerciseIdLong);
    }

    /**
     * Mark multiple exercises completed with String IDs (for calendar controller compatibility)
     */
    @Transactional
    public List<ScheduledWorkoutResponse> markMultipleExercisesCompletedStringIds(String username, List<String> exerciseIds) {
        // Convert String IDs to Long and delegate to existing method
        List<Long> exerciseIdsLong = exerciseIds.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
        return markMultipleExercisesCompleted(username, exerciseIdsLong);
    }

    ;

    // =======================
    // HELPER METHODS - ENTITY LOOKUPS
    // =======================

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

    /**
     * Creates a minimal temporary workout plan for a single exercise.
     * These plans are lightweight containers that hold just enough information
     * to satisfy the database structure while remaining invisible to users.
     */
    private WorkoutPlan createTemporaryWorkoutPlan(Exercise exercise, IndividualExerciseRequest request) {
        WorkoutPlan temporaryPlan = new WorkoutPlan();

        // Set the minimum required fields for database constraints
        temporaryPlan.setWorkoutName(exercise.getExerciseName() + " (Individual)");
        temporaryPlan.setWorkoutDescription("Individual exercise scheduled from calendar");

        // Map exercise difficulty to workout plan difficulty safely
        temporaryPlan.setDifficultyLevel(mapExerciseDifficultyToWorkoutDifficulty(exercise.getDifficultyLevel()));

        temporaryPlan.setEstimatedDurationMinutes(calculateEstimatedDuration(exercise, request));

        // Set category based on exercise characteristics
        temporaryPlan.setWorkoutCategory(determineWorkoutCategory(exercise));

        // Mark as accessible to all users since it's a single exercise
        temporaryPlan.setSubscriptionTierRequired("FREE");
        temporaryPlan.setIsPublic(false); // Not searchable in workout plan library

        // Set metadata to identify this as a system-generated temporary plan
        temporaryPlan.setCreatedByUserId(null); // System-generated
        temporaryPlan.setCreatedByProfessional(false);

        return temporaryPlan;
    }

    /**
     * Builds the core ScheduledWorkout entity with common fields.
     * This separates the basic scheduling logic from exercise-specific configuration.
     */
    private ScheduledWorkout buildScheduledWorkout(User user, WorkoutPlan workoutPlan,
                                                   IndividualExerciseRequest request) {
        ScheduledWorkout scheduledWorkout = new ScheduledWorkout();
        scheduledWorkout.setUser(user);
        scheduledWorkout.setWorkoutPlan(workoutPlan);
        scheduledWorkout.setScheduledDate(request.getScheduledDate());
        scheduledWorkout.setCustomNotes(request.getNotes());
        scheduledWorkout.setEstimatedDurationMinutes(workoutPlan.getEstimatedDurationMinutes());

        return scheduledWorkout;
    }

    /**
     * Applies exercise-specific configuration based on the exercise's tracking mode.
     * This method handles the complexity of different exercise types while providing
     * sensible defaults for missing values.
     */
    private void applyExerciseConfiguration(ScheduledWorkout scheduledWorkout, Exercise exercise,
                                            IndividualExerciseRequest request) {
        try {
            log.debug("🔧 Applying configuration for exercise: {} (cardio: {}, isometric: {})",
                    exercise.getExerciseName(), exercise.getIsCardio(), exercise.getIsIsometric());

            if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
                configureCardioExercise(scheduledWorkout, request);
            } else if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
                configureIsometricExercise(scheduledWorkout, request);
            } else {
                configureStrengthExercise(scheduledWorkout, request);
            }

        } catch (Exception e) {
            log.warn("⚠️ Error applying exercise configuration, using defaults: {}", e.getMessage());
            // Set safe defaults to prevent scheduling failure
            applySafeDefaults(scheduledWorkout);
        }
    }

    private void configureCardioExercise(ScheduledWorkout scheduledWorkout, IndividualExerciseRequest request) {
        scheduledWorkout.setTargetDurationMinutes(
                request.getTargetDurationMinutes() != null ? request.getTargetDurationMinutes() : 20);
        scheduledWorkout.setTargetDistanceKm(request.getTargetDistanceKm());
        scheduledWorkout.setTargetPace(request.getTargetPace());

        log.debug("✅ Configured cardio: duration={}min, distance={}km, pace={}",
                scheduledWorkout.getTargetDurationMinutes(),
                scheduledWorkout.getTargetDistanceKm(),
                scheduledWorkout.getTargetPace());
    }

    private void configureIsometricExercise(ScheduledWorkout scheduledWorkout, IndividualExerciseRequest request) {
        scheduledWorkout.setTargetSets(request.getSets() != null ? request.getSets() : 3);
        scheduledWorkout.setHoldDurationSeconds(
                request.getHoldDurationSeconds() != null ? request.getHoldDurationSeconds() : 30);
        scheduledWorkout.setRestSeconds(request.getRestSeconds() != null ? request.getRestSeconds() : 60);

        log.debug("✅ Configured isometric: sets={}, hold={}s, rest={}s",
                scheduledWorkout.getTargetSets(),
                scheduledWorkout.getHoldDurationSeconds(),
                scheduledWorkout.getRestSeconds());
    }

    private void configureStrengthExercise(ScheduledWorkout scheduledWorkout, IndividualExerciseRequest request) {
        scheduledWorkout.setTargetSets(request.getSets() != null ? request.getSets() : 3);
        scheduledWorkout.setTargetReps(request.getReps() != null ? request.getReps() : "10");
        scheduledWorkout.setTargetWeight(request.getWeight());
        scheduledWorkout.setTargetWeightUnit("lbs"); // Default for American users
        scheduledWorkout.setRestSeconds(request.getRestSeconds() != null ? request.getRestSeconds() : 90);
        scheduledWorkout.setTempo(request.getTempo());
        scheduledWorkout.setTargetRpe(request.getTargetRpe());

        log.debug("✅ Configured strength: sets={}, reps={}, weight={}lbs, rest={}s",
                scheduledWorkout.getTargetSets(),
                scheduledWorkout.getTargetReps(),
                scheduledWorkout.getTargetWeight(),
                scheduledWorkout.getRestSeconds());
    }

    /**
     * Maps exercise difficulty levels to workout plan difficulty levels.
     * This handles cases where the enums might not match exactly.
     */
    private WorkoutPlan.DifficultyLevel mapExerciseDifficultyToWorkoutDifficulty(Exercise.DifficultyLevel exerciseDifficulty) {
        if (exerciseDifficulty == null) {
            return WorkoutPlan.DifficultyLevel.INTERMEDIATE;
        }

        switch (exerciseDifficulty) {
            case BEGINNER:
                return WorkoutPlan.DifficultyLevel.BEGINNER;
            case INTERMEDIATE:
                return WorkoutPlan.DifficultyLevel.INTERMEDIATE;
            case ADVANCED:
                return WorkoutPlan.DifficultyLevel.ADVANCED;
            default:
                return WorkoutPlan.DifficultyLevel.INTERMEDIATE;
        }
    }

    /**
     * Calculates estimated duration based on exercise type and configuration.
     */
    private Integer calculateEstimatedDuration(Exercise exercise, IndividualExerciseRequest request) {
        if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
            return request.getTargetDurationMinutes() != null ?
                    request.getTargetDurationMinutes() :
                    (exercise.getEstimatedDurationMinutes() != null ? exercise.getEstimatedDurationMinutes() : 20);
        } else if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
            Integer sets = request.getSets() != null ? request.getSets() : 3;
            Integer holdDuration = request.getHoldDurationSeconds() != null ? request.getHoldDurationSeconds() : 30;
            Integer rest = request.getRestSeconds() != null ? request.getRestSeconds() : 60;

            // Calculate total time: (hold time * sets) + (rest time * (sets - 1))
            int totalSeconds = (holdDuration * sets) + (rest * (sets - 1));
            return Math.max(1, totalSeconds / 60);
        } else {
            // Strength exercise: estimate based on sets and rest
            Integer sets = request.getSets() != null ? request.getSets() : 3;
            Integer restSeconds = request.getRestSeconds() != null ? request.getRestSeconds() : 90;

            int workTime = sets * 45; // Assume 45 seconds per set
            int restTime = (sets - 1) * restSeconds;
            return Math.max(1, (workTime + restTime) / 60);
        }
    }

    /**
     * Determines appropriate workout category based on exercise characteristics.
     */
    private String determineWorkoutCategory(Exercise exercise) {
        if (exercise.getTargetMuscleGroups() != null && !exercise.getTargetMuscleGroups().isEmpty()) {
            return exercise.getTargetMuscleGroups().get(0);
        } else if (exercise.getExerciseType() != null) {
            return exercise.getExerciseType().getDisplayName();
        } else {
            return "General";
        }
    }

    /**
     * Applies safe default values if configuration fails.
     */
    private void applySafeDefaults(ScheduledWorkout scheduledWorkout) {
        scheduledWorkout.setTargetSets(3);
        scheduledWorkout.setTargetReps("10");
        scheduledWorkout.setRestSeconds(90);
        scheduledWorkout.setTargetWeightUnit("lbs");
    }

    /**
     * ✅ WorkoutCompletionData class to hold completion information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkoutCompletionData {
        private String exerciseId;
        private String scheduledExerciseId;
        private LocalDateTime completedAt;
        private Integer totalDurationMinutes;
        private List<CompletedSetData> sets;
        private String notes;
        private String performanceRating;
        private List<Object> personalRecords;
        private List<Object> improvements;

        // Optional workout session data
        private Integer difficultyRating;
        private Double overallEffort;
        private String mood;
        private String location;
        private String workoutFeedback;
        private String performanceSummary;

        // Optional cardio data
        private Double distanceKm;
        private Integer caloriesBurned;
    }

    /**
     * ✅ CompletedSetData class for individual set information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletedSetData {
        private Integer setNumber;
        private Integer targetReps;
        private Integer actualReps;
        private Double targetWeight;
        private Double actualWeight;
        private String targetWeightUnit;
        private Integer rpe;
        private Integer restSeconds;
        private Boolean completed;
        private Integer actualDurationMinutes;
        private Integer actualHoldSeconds;
        private String notes;
    }
}