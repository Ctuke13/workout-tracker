package com.chidituke.workout_tracker.service.scheduled_workouts;

import com.chidituke.workout_tracker.dto.request.performance.CompleteSetRequest;
import com.chidituke.workout_tracker.dto.request.performance.CompleteWorkoutRequest;
import com.chidituke.workout_tracker.dto.response.performance.ExerciseExecutionSummary;
import com.chidituke.workout_tracker.dto.response.performance.PerformanceResponse;
import com.chidituke.workout_tracker.dto.response.performance.WorkoutExecutionSummary;
import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import com.chidituke.workout_tracker.dto.response.workout_session.WorkoutSessionResponse;
import com.chidituke.workout_tracker.exceptions.scheduled_workout.*;
import com.chidituke.workout_tracker.mapper.workout.ScheduledWorkoutMapper;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.model.workout.PerformanceRecord;
import com.chidituke.workout_tracker.model.workout.ScheduledWorkout;
import com.chidituke.workout_tracker.model.workout.WorkoutSession;
import com.chidituke.workout_tracker.repository.scheduled_workouts.ScheduledWorkoutRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.repository.workout.ExerciseRepository;
import com.chidituke.workout_tracker.repository.workout.PerformanceRecordRepository;
import com.chidituke.workout_tracker.repository.workout.WorkoutSessionRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for workout execution, performance tracking, and completion operations.
 * Handles all aspects of recording workout performance and managing workout sessions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkoutExecutionService {

    private final ScheduledWorkoutRepository scheduledWorkoutRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final PerformanceRecordRepository performanceRecordRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final ScheduledWorkoutMapper scheduledWorkoutMapper;

    // ==================== WORKOUT SESSION MANAGEMENT ====================

    /**
     * Start a workout session from scheduled workout
     */
    public WorkoutSessionResponse startWorkoutExecution(String username, Long scheduledWorkoutId) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(scheduledWorkoutId);
        validateOwnership(scheduledWorkout, username);

        log.info("Starting workout execution for scheduled workout {} by user {}", scheduledWorkoutId, username);

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
            // Count exercises from workout plan
            workoutSession.setTotalExercisesPlanned(1); // Simplified for individual workouts
        }

        workoutSession.setTotalExercisesCompleted(0);
        workoutSession.setCompletionPercentage(0.0);

        WorkoutSession savedSession = workoutSessionRepository.save(workoutSession);

        // Update scheduled workout status
        scheduledWorkout.setStatus(ScheduledWorkout.ScheduleStatus.IN_PROGRESS);
        scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Created workout session {} for scheduled workout {}", savedSession.getId(), scheduledWorkoutId);

        return mapToWorkoutSessionResponse(savedSession);
    }

    /**
     * Complete a set with detailed performance data
     */
    public PerformanceResponse completeSet(String username, Long workoutSessionId, CompleteSetRequest request) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found: " + workoutSessionId));

        validateWorkoutSessionOwnership(workoutSession, username);

        log.info("Recording performance for exercise {} set {} in session {}",
                request.getExerciseId(), request.getSetNumber(), workoutSessionId);

        // Create performance record
        PerformanceRecord performanceRecord = new PerformanceRecord();
        performanceRecord.setWorkoutSession(workoutSession);
        performanceRecord.setExercise(findExerciseById(request.getExerciseId()));
        performanceRecord.setSetNumber(request.getSetNumber());

        // Set performance data based on exercise type
        Exercise exercise = findExerciseById(request.getExerciseId());

        if (exercise.getIsCardio() != null && exercise.getIsCardio()) {
            performanceRecord.setDurationMinutes(request.getDurationMinutes());
            performanceRecord.setDurationSeconds(request.getDurationSeconds());
            performanceRecord.setDistanceKm(request.getDistanceKm());
            performanceRecord.setCaloriesBurned(request.getCaloriesBurned());
        } else if (exercise.getIsIsometric() != null && exercise.getIsIsometric()) {
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

        log.info("Recorded performance for exercise {} set {}", request.getExerciseId(), request.getSetNumber());

        return mapToPerformanceRecordResponse(savedRecord);
    }

    /**
     * Complete an exercise (all sets done)
     */
    public WorkoutSessionResponse completeExercise(String username, Long workoutSessionId,
                                                   Long exerciseId, String completionNotes) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found: " + workoutSessionId));

        validateWorkoutSessionOwnership(workoutSession, username);

        log.info("Completing exercise {} in workout session {}", exerciseId, workoutSessionId);

        // Mark all performance records for this exercise as completed
        List<PerformanceRecord> exerciseRecords = performanceRecordRepository
                .findByWorkoutSessionAndExerciseOrderBySetNumber(workoutSession, findExerciseById(exerciseId));

        for (PerformanceRecord record : exerciseRecords) {
            record.setIsExerciseCompleted(true);
            record.setExerciseCompletionNotes(completionNotes);
        }

        performanceRecordRepository.saveAll(exerciseRecords);

        // Update workout session completion
        WorkoutSession updatedSession = workoutSessionRepository.findById(workoutSessionId).orElse(workoutSession);

        log.info("Completed exercise {} in workout session {}", exerciseId, workoutSessionId);

        return mapToWorkoutSessionResponse(updatedSession);
    }

    /**
     * Complete entire workout session
     */
    public ScheduledWorkoutResponse completeWorkoutSession(String username, Long workoutSessionId,
                                                           CompleteWorkoutRequest request) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found: " + workoutSessionId));

        validateWorkoutSessionOwnership(workoutSession, username);

        log.info("Completing workout session {} for user {}", workoutSessionId, username);

        // Update workout session
        workoutSession.setSessionStatus(WorkoutSession.SessionStatus.COMPLETED);
        workoutSession.setTotalDurationMinutes(request.getTotalDurationMinutes());
        workoutSession.setDifficultyRating(request.getDifficultyRating());
        workoutSession.setOverallEffort(request.getOverallEffort());

        // Convert String to enum for mood
        if (request.getMood() != null) {
            try {
                WorkoutSession.WorkoutMood mood = WorkoutSession.WorkoutMood.valueOf(request.getMood().toUpperCase());
                workoutSession.setMood(mood);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid mood value: {}, using default", request.getMood());
                workoutSession.setMood(WorkoutSession.WorkoutMood.FOCUSED);
            }
        }

        // Convert String to enum for location
        if (request.getLocation() != null) {
            try {
                WorkoutSession.WorkoutLocation location = WorkoutSession.WorkoutLocation.valueOf(request.getLocation().toUpperCase());
                workoutSession.setLocation(location);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid location value: {}, using default", request.getLocation());
                workoutSession.setLocation(WorkoutSession.WorkoutLocation.HOME);
            }
        }

        // Store feedback and performance summary in notes
        if (request.getWorkoutFeedback() != null) {
            String existingNotes = workoutSession.getNotes();
            String feedback = "Feedback: " + request.getWorkoutFeedback();
            workoutSession.setNotes(existingNotes != null ? existingNotes + " | " + feedback : feedback);
        }

        if (request.getPerformanceSummary() != null) {
            String existingNotes = workoutSession.getNotes();
            String summary = "Performance: " + request.getPerformanceSummary();
            workoutSession.setNotes(existingNotes != null ? existingNotes + " | " + summary : summary);
        }

        WorkoutSession savedSession = workoutSessionRepository.save(workoutSession);

        // Update scheduled workout
        ScheduledWorkout scheduledWorkout = workoutSession.getScheduledWorkout();
        if (scheduledWorkout != null) {
            scheduledWorkout.setStatus(ScheduledWorkout.ScheduleStatus.COMPLETED);
            scheduledWorkout.setCompletedAt(LocalDateTime.now());
            scheduledWorkout.setActualDurationMinutes(request.getTotalDurationMinutes());
            scheduledWorkout = scheduledWorkoutRepository.save(scheduledWorkout);

            log.info("Completed workout session {} and scheduled workout {}",
                    workoutSessionId, scheduledWorkout.getId());

            return scheduledWorkoutMapper.toResponse(scheduledWorkout);
        }

        throw new RuntimeException("No scheduled workout associated with session");
    }

    // ==================== SIMPLE EXERCISE COMPLETION ====================

    /**
     * Mark exercise as completed (simple version)
     */
    public ScheduledWorkoutResponse markExerciseCompleted(String username, Long exerciseId) {
        return markExerciseCompleted(exerciseId, username, null, null, null, "MET");
    }

    /**
     * Mark exercise as completed with detailed completion data
     */
    public ScheduledWorkoutResponse markExerciseCompleted(Long exerciseId, String username,
                                                          LocalDateTime completedAt,
                                                          Integer totalDurationMinutes,
                                                          String notes,
                                                          String performanceRating) {
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(exerciseId);
        validateOwnership(scheduledWorkout, username);

        log.info("Marking exercise {} as completed for user {} with detailed data", exerciseId, username);

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

        ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Successfully marked exercise {} as completed for user {} with {} min duration",
                exerciseId, username, totalDurationMinutes);
        return scheduledWorkoutMapper.toResponse(saved);
    }

    /**
     * Mark multiple exercises as completed (batch operation)
     */
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

    // ==================== WORKOUT EXECUTION ANALYTICS ====================

    /**
     * Get workout execution summary with performance details
     */
    @Transactional(readOnly = true)
    public WorkoutExecutionSummary getWorkoutExecutionSummary(String username, Long workoutSessionId) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new RuntimeException("Workout session not found: " + workoutSessionId));

        validateWorkoutSessionOwnership(workoutSession, username);

        log.info("Getting execution summary for workout session {}", workoutSessionId);

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

            ExerciseExecutionSummary exerciseSummary = ExerciseExecutionSummary.builder()
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

        log.info("Getting batch workout results for {} scheduled workouts for user {}", exerciseIds.size(), username);

        Map<String, Object> results = new HashMap<>();

        for (String scheduledWorkoutIdStr : exerciseIds) {
            try {
                Long scheduledWorkoutId = Long.parseLong(scheduledWorkoutIdStr);
                Optional<ScheduledWorkout> scheduledWorkoutOpt = scheduledWorkoutRepository.findById(scheduledWorkoutId);

                if (scheduledWorkoutOpt.isPresent()) {
                    ScheduledWorkout scheduledWorkout = scheduledWorkoutOpt.get();

                    // Verify ownership and completion status
                    if (scheduledWorkout.getUser().equals(user) &&
                            scheduledWorkout.getStatus() == ScheduledWorkout.ScheduleStatus.COMPLETED) {

                        Map<String, Object> workoutResults = buildWorkoutResultsFromScheduledWorkout(scheduledWorkout);
                        results.put(scheduledWorkoutIdStr, workoutResults);

                        log.debug("Found workout results for scheduled workout {}", scheduledWorkoutIdStr);
                    } else {
                        log.debug("Scheduled workout {} not completed or not owned by user {}", scheduledWorkoutIdStr, username);
                    }
                } else {
                    log.debug("Scheduled workout {} not found", scheduledWorkoutIdStr);
                }
            } catch (Exception e) {
                log.error("Error processing scheduled workout {}: {}", scheduledWorkoutIdStr, e.getMessage());
            }
        }

        results.put("summary", Map.of(
                "totalExercisesAnalyzed", results.size() - 1,
                "generatedAt", LocalDateTime.now().toString()
        ));

        log.info("Returning workout results for {}/{} scheduled workouts", results.size() - 1, exerciseIds.size());
        return results;
    }

    // ==================== HELPER METHODS ====================

    private PerformanceRecord.PerformanceVsTarget calculatePerformanceVsTarget(PerformanceRecord record) {
        if (record.getTargetRepsPlanned() != null && record.getReps() != null) {
            if (record.getReps() > record.getTargetRepsPlanned()) {
                return PerformanceRecord.PerformanceVsTarget.EXCEEDED;
            } else if (record.getReps().equals(record.getTargetRepsPlanned())) {
                return PerformanceRecord.PerformanceVsTarget.MET;
            } else {
                return PerformanceRecord.PerformanceVsTarget.BELOW;
            }
        }

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

    private Map<String, Object> buildWorkoutResultsFromScheduledWorkout(ScheduledWorkout scheduledWorkout) {
        Map<String, Object> workoutResults = new HashMap<>();

        // Build basic workout results
        workoutResults.put("exerciseId", scheduledWorkout.getId().toString());
        workoutResults.put("completedAt", scheduledWorkout.getCompletedAt() != null ?
                scheduledWorkout.getCompletedAt().toString() : LocalDateTime.now().toString());
        workoutResults.put("totalDurationMinutes", scheduledWorkout.getActualDurationMinutes());
        workoutResults.put("performanceRating", "MET");
        workoutResults.put("notes", scheduledWorkout.getCustomNotes());
        workoutResults.put("caloriesBurned", scheduledWorkout.getCompletedSession() != null ?
                scheduledWorkout.getCompletedSession().getActualCaloriesBurned() : null);
        workoutResults.put("totalCaloriesCalculated", scheduledWorkout.getCompletedSession() != null ?
                scheduledWorkout.getCompletedSession().getTotalCaloriesCalculated() : null);

        //  Retrieve actual performance records (sets data)
        List<Map<String, Object>> sets = new ArrayList<>();

        // Try to find workout session for this scheduled workout
        Optional<WorkoutSession> sessionOpt = workoutSessionRepository
                .findByScheduledWorkout(scheduledWorkout);

        if (sessionOpt.isPresent()) {
            WorkoutSession session = sessionOpt.get();
            log.debug("Found workout session {} for scheduled workout {}", session.getId(), scheduledWorkout.getId());

            // Get performance records for this session
            List<PerformanceRecord> performanceRecords = performanceRecordRepository
                    .findByWorkoutSessionOrderByExerciseIdAscSetNumberAsc(session);

            log.debug("Found {} performance records for session {}", performanceRecords.size(), session.getId());

            // Convert performance records to sets data
            for (PerformanceRecord record : performanceRecords) {
                Map<String, Object> setData = new HashMap<>();
                setData.put("setNumber", record.getSetNumber());
                setData.put("actualReps", record.getReps());
                setData.put("actualWeight", record.getWeight());
                setData.put("actualDurationMinutes", record.getDurationMinutes());
                setData.put("actualHoldSeconds", record.getHoldDurationSeconds());
                setData.put("targetReps", record.getTargetRepsPlanned());
                setData.put("targetWeight", record.getTargetWeightPlanned());
                setData.put("completed", record.getIsExerciseCompleted() != null ? record.getIsExerciseCompleted() : true);
                setData.put("rpe", record.getPerceivedExertion());
                setData.put("restSeconds", record.getRestTimeBeforeSetSeconds());
                setData.put("actualRestSeconds", record.getActualRestSeconds());
                setData.put("notes", record.getNotes());

                sets.add(setData);
            }
        } else {
            // Fallback: Check if we can use the completedSession relationship
            if (scheduledWorkout.getCompletedSession() != null) {
                WorkoutSession session = scheduledWorkout.getCompletedSession();
                log.debug("Using completedSession {} for scheduled workout {}", session.getId(), scheduledWorkout.getId());

                List<PerformanceRecord> performanceRecords = performanceRecordRepository
                        .findByWorkoutSessionOrderByExerciseIdAscSetNumberAsc(session);

                for (PerformanceRecord record : performanceRecords) {
                    Map<String, Object> setData = new HashMap<>();
                    setData.put("setNumber", record.getSetNumber());
                    setData.put("actualReps", record.getReps());
                    setData.put("actualWeight", record.getWeight());
                    setData.put("actualDurationMinutes", record.getDurationMinutes());
                    setData.put("actualHoldSeconds", record.getHoldDurationSeconds());
                    setData.put("targetReps", record.getTargetRepsPlanned());
                    setData.put("targetWeight", record.getTargetWeightPlanned());
                    setData.put("completed", record.getIsExerciseCompleted() != null ? record.getIsExerciseCompleted() : true);
                    setData.put("rpe", record.getPerceivedExertion());
                    setData.put("restSeconds", record.getRestTimeBeforeSetSeconds());
                    setData.put("notes", record.getNotes());

                    sets.add(setData);
                }
            } else {
                log.debug("No workout session found for scheduled workout {}", scheduledWorkout.getId());
            }
        }

        workoutResults.put("sets", sets);
        workoutResults.put("personalRecords", List.of());
        workoutResults.put("improvements", List.of());

        log.debug("Built workout results with {} sets for scheduled workout {}", sets.size(), scheduledWorkout.getId());

        return workoutResults;
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

    // Entity lookup helpers
    private ScheduledWorkout findScheduledWorkoutById(Long id) {
        return scheduledWorkoutRepository.findById(id)
                .orElseThrow(() -> new ScheduledWorkoutNotFoundException(id));
    }

    private Exercise findExerciseById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercise not found: " + id));
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private void validateOwnership(ScheduledWorkout scheduledWorkout, String username) {
        if (!scheduledWorkout.getUser().getUsername().equals(username)) {
            throw new UnauthorizedScheduledWorkoutAccessException(
                    "User does not have access to this scheduled workout");
        }
    }

    private void validateWorkoutSessionOwnership(WorkoutSession workoutSession, String username) {
        if (!workoutSession.getUser().getUsername().equals(username)) {
            throw new UnauthorizedScheduledWorkoutAccessException(
                    "User does not have access to this workout session");
        }
    }

    public ScheduledWorkoutResponse markExerciseCompletedWithPerformance(String username, String exerciseId,
                                                                         WorkoutCompletionData completionData) {
        log.debug("Enhanced completion called for exercise {} by user {} with data: {}",
                exerciseId, username, completionData);

        Long exerciseIdLong = Long.parseLong(exerciseId);
        ScheduledWorkout scheduledWorkout = findScheduledWorkoutById(exerciseIdLong);
        validateOwnership(scheduledWorkout, username);

        // Create WorkoutSession
        WorkoutSession workoutSession = new WorkoutSession();
        workoutSession.setUser(scheduledWorkout.getUser());
        workoutSession.setScheduledWorkout(scheduledWorkout);
        workoutSession.setWorkoutPlan(scheduledWorkout.getWorkoutPlan());
        workoutSession.setDate(completionData.getCompletedAt().toLocalDate());
        workoutSession.setSessionStatus(WorkoutSession.SessionStatus.COMPLETED);
        workoutSession.setTotalDurationMinutes(completionData.getTotalDurationMinutes());
        workoutSession.setDifficultyRating(completionData.getDifficultyRating());
        workoutSession.setOverallEffort(completionData.getOverallEffort());

        if (completionData.getMood() != null) {
            try {
                workoutSession.setMood(WorkoutSession.WorkoutMood.valueOf(completionData.getMood()));
            } catch (IllegalArgumentException e) {
                workoutSession.setMood(WorkoutSession.WorkoutMood.FOCUSED);
            }
        }

        if (completionData.getLocation() != null) {
            try {
                workoutSession.setLocation(WorkoutSession.WorkoutLocation.valueOf(completionData.getLocation()));
            } catch (IllegalArgumentException e) {
                workoutSession.setLocation(WorkoutSession.WorkoutLocation.HOME);
            }
        }

        WorkoutSession savedSession = workoutSessionRepository.save(workoutSession);

        // Create PerformanceRecords for each set
        if (completionData.getSets() != null) {
            for (CompletedSetData setData : completionData.getSets()) {
                PerformanceRecord record = new PerformanceRecord();
                record.setWorkoutSession(savedSession);
                record.setExercise(scheduledWorkout.getResolvedExercise());
                record.setSetNumber(setData.getSetNumber());
                record.setReps(setData.getActualReps());
                record.setWeight(setData.getActualWeight());
                record.setHoldDurationSeconds(setData.getActualHoldSeconds());
                record.setDurationMinutes(setData.getActualDurationMinutes());
                record.setTargetRepsPlanned(setData.getTargetReps());
                record.setTargetWeightPlanned(setData.getTargetWeight());
                record.setPerceivedExertion(setData.getRpe());
                record.setRestTimeBeforeSetSeconds(setData.getRestSeconds());
                record.setActualRestSeconds(setData.getActualRestSeconds());
                record.setIsExerciseCompleted(setData.getCompleted());
                record.setNotes(setData.getNotes());

                performanceRecordRepository.save(record);
            }
        }

        // Update ScheduledWorkout
        scheduledWorkout.setStatus(ScheduledWorkout.ScheduleStatus.COMPLETED);
        scheduledWorkout.setCompletedAt(completionData.getCompletedAt());
        scheduledWorkout.setActualDurationMinutes(completionData.getTotalDurationMinutes());
        scheduledWorkout.setCustomNotes(completionData.getNotes());
        scheduledWorkout.setCompletedSession(savedSession);

        ScheduledWorkout saved = scheduledWorkoutRepository.save(scheduledWorkout);

        log.info("Successfully created WorkoutSession {} with {} sets for exercise {}",
                savedSession.getId(),
                completionData.getSets() != null ? completionData.getSets().size() : 0,
                exerciseId);

        return scheduledWorkoutMapper.toResponse(saved);
    }

    /**
     * WorkoutCompletionData class to hold completion information
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
     * CompletedSetData class for individual set information
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
        private Integer actualRestSeconds;
        private Boolean completed;
        private Integer actualDurationMinutes;
        private Integer actualHoldSeconds;
        private String notes;
    }
}

