package com.chidituke.workout_tracker.repository.workout;

import com.chidituke.workout_tracker.model.workout.ScheduledWorkout;
import com.chidituke.workout_tracker.model.workout.ScheduledWorkout.ScheduleStatus;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import com.chidituke.workout_tracker.model.workout.WorkoutProgram;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledWorkoutRepository extends JpaRepository<ScheduledWorkout, Long> {

    // =======================
    // CALENDAR VIEW QUERIES
    // =======================

    // Get user's calendar for date range (main calendar view)
    List<ScheduledWorkout> findByUserAndScheduledDateBetweenOrderByScheduledDateAsc(
            User user, LocalDate startDate, LocalDate endDate);

    // Get user's workouts for specific date
    List<ScheduledWorkout> findByUserAndScheduledDateOrderByCreatedAtAsc(User user, LocalDate date);

    // Get user's upcoming workouts (next 7 days)
    @Query("SELECT sw FROM ScheduledWorkout sw WHERE sw.user = :user " +
            "AND sw.scheduledDate BETWEEN CURRENT_DATE AND :endDate " +
            "AND sw.status IN ('SCHEDULED', 'IN_PROGRESS') " +
            "ORDER BY sw.scheduledDate ASC, sw.createdAt ASC")
    List<ScheduledWorkout> findUpcomingWorkouts(@Param("user") User user,
                                                @Param("endDate") LocalDate endDate);

    @Query("SELECT s FROM ScheduledWorkout s WHERE s.user = :user AND s.program = :program AND s.status = :status ORDER BY s.scheduledDate ASC")
    List<ScheduledWorkout> findByUserAndProgramAndStatusOrderByScheduledDateAsc(
            @Param("user") User user,
            @Param("program") WorkoutProgram program,
            @Param("status") ScheduledWorkout.ScheduleStatus status);


    // Get today's workouts for user
    @Query("SELECT sw FROM ScheduledWorkout sw WHERE sw.user = :user " +
            "AND sw.scheduledDate = CURRENT_DATE " +
            "ORDER BY sw.createdAt ASC")
    List<ScheduledWorkout> findTodaysWorkouts(@Param("user") User user);

    // =======================
    // STATUS-BASED QUERIES
    // =======================

    // Find workouts by status
    List<ScheduledWorkout> findByUserAndStatusOrderByScheduledDateAsc(User user, ScheduleStatus status);

    // Find overdue workouts (scheduled but past due)
    @Query("SELECT sw FROM ScheduledWorkout sw WHERE sw.user = :user " +
            "AND sw.status = 'SCHEDULED' " +
            "AND sw.scheduledDate < CURRENT_DATE " +
            "ORDER BY sw.scheduledDate ASC")
    List<ScheduledWorkout> findOverdueWorkouts(@Param("user") User user);

    // Find in-progress workouts
    List<ScheduledWorkout> findByUserAndStatusOrderByUpdatedAtDesc(User user, ScheduleStatus status);

    // Check if user has any in-progress workouts
    boolean existsByUserAndStatus(User user, ScheduleStatus status);

    // =======================
    // WORKOUT PLAN QUERIES
    // =======================

    // Find scheduled instances of specific workout plan
    List<ScheduledWorkout> findByWorkoutPlanOrderByScheduledDateDesc(WorkoutPlan workoutPlan);

    // Count how many times user has scheduled specific workout
    long countByUserAndWorkoutPlan(User user, WorkoutPlan workoutPlan);

    // Find user's last scheduled instance of workout plan
    Optional<ScheduledWorkout> findTopByUserAndWorkoutPlanOrderByScheduledDateDesc(
            User user, WorkoutPlan workoutPlan);

    // =======================
    // PROGRAM TRACKING QUERIES
    // =======================

    // Find scheduled workouts for specific program
    List<ScheduledWorkout> findByUserAndProgramOrderByWeekNumberAscDayOfWeekAsc(
            User user, WorkoutProgram program);

    // Find scheduled workouts for specific week of program
    List<ScheduledWorkout> findByUserAndProgramAndWeekNumberOrderByDayOfWeekAsc(
            User user, WorkoutProgram program, Integer weekNumber);

    // Check if user has scheduled specific week/day of program
    boolean existsByUserAndProgramAndWeekNumberAndDayOfWeek(
            User user, WorkoutProgram program, Integer weekNumber, Integer dayOfWeek);

    // Count scheduled weeks for program
    @Query("SELECT COUNT(DISTINCT sw.weekNumber) FROM ScheduledWorkout sw " +
            "WHERE sw.user = :user AND sw.program = :program")
    Long countScheduledWeeksByUserAndProgram(@Param("user") User user, @Param("program") WorkoutProgram program);

    // =======================
    // SUBSCRIPTION ENFORCEMENT
    // =======================

    // Count user's scheduled workouts (for free tier limits)
    long countByUserAndStatusAndScheduledDateAfter(User user, ScheduleStatus status, LocalDate afterDate);

    // Find user's farthest scheduled workout (for limit checking)
    @Query("SELECT MAX(sw.scheduledDate) FROM ScheduledWorkout sw " +
            "WHERE sw.user = :user AND sw.status = 'SCHEDULED'")
    Optional<LocalDate> findFarthestScheduledDate(@Param("user") User user);

    // =======================
    // SCHEDULING CONFLICT DETECTION
    // =======================

    // Check for scheduling conflicts (same user, date, and time slot)
    @Query("SELECT sw FROM ScheduledWorkout sw WHERE sw.user = :user " +
            "AND sw.scheduledDate = :date " +
            "AND sw.status IN ('SCHEDULED', 'IN_PROGRESS') " +
            "AND sw.id != :excludeId")
    List<ScheduledWorkout> findSchedulingConflicts(@Param("user") User user,
                                                   @Param("date") LocalDate date,
                                                   @Param("excludeId") Long excludeId);

    // Check if user already has workout scheduled for date
    boolean existsByUserAndScheduledDateAndStatusIn(User user, LocalDate date, List<ScheduleStatus> statuses);

    // =======================
    // COACH/TRAINER QUERIES
    // =======================

    // Find workouts created by specific trainer/coach
    List<ScheduledWorkout> findByCreatedByUserIdOrderByScheduledDateDesc(Long createdByUserId);

    // Find user's trainer-assigned workouts
    List<ScheduledWorkout> findByUserAndCreatedByUserIdIsNotNullOrderByScheduledDateAsc(User user);

    // =======================
    // ANALYTICS QUERIES
    // =======================

    // Count scheduled workouts in date range
    @Query("SELECT COUNT(sw) FROM ScheduledWorkout sw WHERE sw.user = :user " +
            "AND sw.scheduledDate BETWEEN :startDate AND :endDate")
    Long countScheduledWorkoutsInRange(@Param("user") User user,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    // Get completion rate for user
    @Query("SELECT " +
            "CAST(SUM(CASE WHEN sw.status = 'COMPLETED' THEN 1 ELSE 0 END) AS double) / COUNT(sw) * 100 " +
            "FROM ScheduledWorkout sw WHERE sw.user = :user " +
            "AND sw.scheduledDate BETWEEN :startDate AND :endDate")
    Optional<Double> calculateCompletionRate(@Param("user") User user,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    // Get workout frequency by day of week
    @Query("SELECT sw.dayOfWeek, COUNT(sw) FROM ScheduledWorkout sw " +
            "WHERE sw.user = :user AND sw.dayOfWeek IS NOT NULL " +
            "GROUP BY sw.dayOfWeek ORDER BY sw.dayOfWeek")
    List<Object[]> getWorkoutFrequencyByDayOfWeek(@Param("user") User user);

    // =======================
    // REMINDER SYSTEM QUERIES
    // =======================

    // Find workouts needing reminders
    @Query("SELECT sw FROM ScheduledWorkout sw WHERE sw.reminderTime IS NOT NULL " +
            "AND sw.reminderTime <= :currentTime " +
            "AND sw.status = 'SCHEDULED'")
    List<ScheduledWorkout> findWorkoutsNeedingReminders(@Param("currentTime") LocalDateTime currentTime);

    // Find tomorrow's workouts for daily reminders
    @Query("SELECT sw FROM ScheduledWorkout sw WHERE sw.scheduledDate = :tomorrow " +
            "AND sw.status = 'SCHEDULED'")
    List<ScheduledWorkout> findTomorrowsWorkouts(@Param("tomorrow") LocalDate tomorrow);

    // =======================
    // CLEANUP QUERIES
    // =======================

    // Find old completed/cancelled workouts for cleanup
    @Query("SELECT sw FROM ScheduledWorkout sw WHERE sw.user = :user " +
            "AND sw.status IN ('COMPLETED', 'CANCELLED') " +
            "AND sw.scheduledDate < :cutoffDate")
    List<ScheduledWorkout> findOldWorkoutsForCleanup(@Param("user") User user,
                                                     @Param("cutoffDate") LocalDate cutoffDate);

    // Delete old scheduled workouts (for free user data retention)
    void deleteByUserAndStatusInAndScheduledDateBefore(User user, List<ScheduleStatus> statuses, LocalDate beforeDate);

    // =======================
    // PAGINATION QUERIES
    // =======================

    // Paginated calendar view
    Page<ScheduledWorkout> findByUserAndScheduledDateBetween(User user,
                                                             LocalDate startDate,
                                                             LocalDate endDate,
                                                             Pageable pageable);

    // Paginated workout history
    Page<ScheduledWorkout> findByUserAndStatusOrderByScheduledDateDesc(User user,
                                                                       ScheduleStatus status,
                                                                       Pageable pageable);

    // =======================
    // BULK OPERATIONS
    // =======================

    // Bulk status update
    @Query("UPDATE ScheduledWorkout sw SET sw.status = :newStatus, sw.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE sw.user = :user AND sw.status = :currentStatus " +
            "AND sw.scheduledDate < :beforeDate")
    int bulkUpdateOverdueWorkouts(@Param("user") User user,
                                  @Param("currentStatus") ScheduleStatus currentStatus,
                                  @Param("newStatus") ScheduleStatus newStatus,
                                  @Param("beforeDate") LocalDate beforeDate);
}