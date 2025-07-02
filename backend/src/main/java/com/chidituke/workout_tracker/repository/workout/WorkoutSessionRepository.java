package com.chidituke.workout_tracker.repository.workout;

import com.chidituke.workout_tracker.model.workout.WorkoutSession;
import com.chidituke.workout_tracker.model.workout.WorkoutSession.WorkoutMood;
import com.chidituke.workout_tracker.model.workout.WorkoutSession.WorkoutLocation;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import com.chidituke.workout_tracker.model.workout.WorkoutProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    // =======================
    // BASIC USER WORKOUT TRACKING
    // =======================

    // Find all workout sessions for a user, ordered by most recent
    List<WorkoutSession> findByUserOrderByDateDesc(User user);

    // Find workout sessions for a specific user and date
    List<WorkoutSession> findByUserAndDate(User user, LocalDate date);

    // Find workout sessions within a date range
    List<WorkoutSession> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);

    // Count total workouts for a user
    long countByUser(User user);

    // Find user's most recent workout
    Optional<WorkoutSession> findTopByUserOrderByDateDescCreatedAtDesc(User user);

    // =======================
    // WORKOUT PLAN TRACKING
    // =======================

    // Find sessions for specific workout plan
    List<WorkoutSession> findByWorkoutPlanOrderByDateDesc(WorkoutPlan workoutPlan);

    // Find how many times user completed specific workout plan
    long countByUserAndWorkoutPlan(User user, WorkoutPlan workoutPlan);

    // Find user's sessions for specific workout plan
    List<WorkoutSession> findByUserAndWorkoutPlanOrderByDateDesc(User user, WorkoutPlan workoutPlan);

    // Find user's last completion of specific workout plan
    Optional<WorkoutSession> findTopByUserAndWorkoutPlanOrderByDateDesc(User user, WorkoutPlan workoutPlan);

    // =======================
    // WORKOUT PROGRAM TRACKING (Multi-week programs)
    // =======================

    // Count sessions by workout plan
    long countByWorkoutPlan(WorkoutPlan workoutPlan);

    // Count sessions by program
    long countByProgram(WorkoutProgram program);

    // Find all sessions within a workout program
    List<WorkoutSession> findByProgramOrderByWeekNumberAscDateAsc(WorkoutProgram program);

    // Find user's sessions within a specific program
    List<WorkoutSession> findByUserAndProgramOrderByWeekNumberAscDateAsc(User user, WorkoutProgram program);

    // Find sessions for specific week of a program
    List<WorkoutSession> findByUserAndProgramAndWeekNumberOrderByDate(User user, WorkoutProgram program, Integer weekNumber);

    // Count completed weeks in a program for user
    @Query("SELECT COUNT(DISTINCT ws.weekNumber) FROM WorkoutSession ws " +
            "WHERE ws.user = :user AND ws.program = :program")
    Long countCompletedWeeksByUserAndProgram(@Param("user") User user, @Param("program") WorkoutProgram program);

    // Check if user completed specific week of program
    boolean existsByUserAndProgramAndWeekNumber(User user, WorkoutProgram program, Integer weekNumber);

    // Check if user has sessions for program after a certain date
    boolean existsByUserAndProgramAndDateAfter(User user, WorkoutProgram program, LocalDate afterDate);

    // Find user's current week in program
    @Query("SELECT MAX(ws.weekNumber) FROM WorkoutSession ws " +
            "WHERE ws.user = :user AND ws.program = :program")
    Optional<Integer> findMaxWeekNumberByUserAndProgram(@Param("user") User user, @Param("program") WorkoutProgram program);



    // =======================
    // ANALYTICS & STATISTICS
    // =======================

    // Count workouts in a date range
    @Query("SELECT COUNT(ws) FROM WorkoutSession ws WHERE ws.user = :user " +
            "AND ws.date BETWEEN :startDate AND :endDate")
    Long countByUserAndDateRange(@Param("user") User user,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    // Calculate total workout time for user
    @Query("SELECT SUM(ws.totalDurationMinutes) FROM WorkoutSession ws " +
            "WHERE ws.user = :user AND ws.totalDurationMinutes IS NOT NULL")
    Optional<Long> sumTotalDurationByUser(@Param("user") User user);

    // Calculate total calories burned
    @Query("SELECT SUM(ws.estimatedCalories) FROM WorkoutSession ws " +
            "WHERE ws.user = :user AND ws.estimatedCalories IS NOT NULL")
    Optional<Long> sumCaloriesByUser(@Param("user") User user);

    // Average workout duration for user
    @Query("SELECT AVG(ws.totalDurationMinutes) FROM WorkoutSession ws " +
            "WHERE ws.user = :user AND ws.totalDurationMinutes IS NOT NULL")
    Optional<Double> averageDurationByUser(@Param("user") User user);

    // =======================
    // FREE USER DATA RETENTION (30-day limit)
    // =======================

    // Find sessions within retention period for free users
    @Query("SELECT ws FROM WorkoutSession ws WHERE ws.user = :user " +
            "AND ws.createdAt >= :cutoffDate ORDER BY ws.date DESC")
    List<WorkoutSession> findSessionsWithinRetentionPeriod(@Param("user") User user,
                                                           @Param("cutoffDate") LocalDateTime cutoffDate);

    // Find old sessions to cleanup for free users
    @Query("SELECT ws FROM WorkoutSession ws WHERE ws.user = :user " +
            "AND ws.createdAt < :cutoffDate")
    List<WorkoutSession> findSessionsToCleanup(@Param("user") User user,
                                               @Param("cutoffDate") LocalDateTime cutoffDate);

    // =======================
    // MOOD & LOCATION TRACKING
    // =======================

    // Find workouts by mood
    List<WorkoutSession> findByUserAndMoodOrderByDateDesc(User user, WorkoutMood mood);

    // Find workouts by location
    List<WorkoutSession> findByUserAndLocationOrderByDateDesc(User user, WorkoutLocation location);

    // Count workouts by mood for analytics
    @Query("SELECT ws.mood, COUNT(ws) FROM WorkoutSession ws " +
            "WHERE ws.user = :user AND ws.mood IS NOT NULL GROUP BY ws.mood")
    List<Object[]> countByMoodForUser(@Param("user") User user);

    // =======================
    // EFFORT & DIFFICULTY TRACKING
    // =======================

    // Find high-effort workouts
    List<WorkoutSession> findByUserAndOverallEffortGreaterThanEqualOrderByDateDesc(User user, Double minEffort);

    // Average effort rating for user
    @Query("SELECT AVG(ws.overallEffort) FROM WorkoutSession ws " +
            "WHERE ws.user = :user AND ws.overallEffort IS NOT NULL")
    Optional<Double> averageEffortByUser(@Param("user") User user);

    // =======================
    // SHARING & SOCIAL FEATURES
    // =======================

    // Find shared workout sessions
    List<WorkoutSession> findByIsSharedTrueOrderByDateDesc();

    // Find user's shared workouts
    List<WorkoutSession> findByUserAndIsSharedTrueOrderByDateDesc(User user);

    // =======================
    // PROGRAM PROGRESS TRACKING
    // =======================

    // Calculate program completion percentage
    @Query("SELECT CAST(COUNT(DISTINCT ws.weekNumber) AS double) / :totalWeeks * 100 " +
            "FROM WorkoutSession ws WHERE ws.user = :user AND ws.program = :program")
    Optional<Double> calculateProgramCompletionPercentage(@Param("user") User user,
                                                          @Param("program") WorkoutProgram program,
                                                          @Param("totalWeeks") Integer totalWeeks);

    // Find users who completed entire program
    @Query("SELECT DISTINCT ws.user FROM WorkoutSession ws " +
            "WHERE ws.program = :program " +
            "GROUP BY ws.user " +
            "HAVING COUNT(DISTINCT ws.weekNumber) = :totalWeeks")
    List<User> findUsersWhoCompletedProgram(@Param("program") WorkoutProgram program,
                                            @Param("totalWeeks") Integer totalWeeks);

    // =======================
    // RECENT ACTIVITY
    // =======================

    // Find recent workouts (last 7 days)
    @Query("SELECT ws FROM WorkoutSession ws WHERE ws.user = :user " +
            "AND ws.date >= :sevenDaysAgo ORDER BY ws.date DESC")
    List<WorkoutSession> findRecentWorkouts(@Param("user") User user,
                                            @Param("sevenDaysAgo") LocalDate sevenDaysAgo);

    // Check if user worked out today
    boolean existsByUserAndDate(User user, LocalDate today);

    // Find workout streak days
    @Query("SELECT ws.date FROM WorkoutSession ws WHERE ws.user = :user " +
            "AND ws.date >= :startDate ORDER BY ws.date DESC")
    List<LocalDate> findWorkoutDatesForStreak(@Param("user") User user,
                                              @Param("startDate") LocalDate startDate);
}