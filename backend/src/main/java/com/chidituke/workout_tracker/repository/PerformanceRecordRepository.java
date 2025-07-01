package com.chidituke.workout_tracker.repository;

import com.chidituke.workout_tracker.model.Exercise;
import com.chidituke.workout_tracker.model.PerformanceRecord;
import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.model.WorkoutSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.QueryHint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Enhanced Performance Record Repository with comprehensive queries and performance optimizations
 * Updated to use WorkoutSession entity for application congruency
 */
@Repository
public interface PerformanceRecordRepository extends JpaRepository<PerformanceRecord, Long>,
        JpaSpecificationExecutor<PerformanceRecord> {

    // ==============================================
    // BASIC QUERY METHODS - ENHANCED
    // ==============================================

    /**
     * Find all performance records for a workout session, ordered by set number
     */
    @Query("SELECT pr FROM PerformanceRecord pr " +
            "WHERE pr.workoutSession = :workoutSession " +
            "ORDER BY pr.setNumber, pr.createdAt")
    List<PerformanceRecord> findByWorkoutSessionOrderBySetNumber(@Param("workoutSession") WorkoutSession workoutSession);

    /**
     * Find performance records by user (REQUIRED BY SERVICE)
     */
    @Query("SELECT pr FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "ORDER BY ws.date DESC, pr.setNumber")
    Page<PerformanceRecord> findByWorkoutSessionUser(@Param("user") User user, Pageable pageable);

    /**
     * Count performance records by user (REQUIRED BY SERVICE)
     */
    @Query("SELECT COUNT(pr) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user")
    Long countByWorkoutSessionUser(@Param("user") User user);

    /**
     * Find performance records for a specific user and workout plan with pagination
     */
    @Query("SELECT pr FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "JOIN ws.workoutPlan w " +
            "WHERE ws.user = :user AND w.id = :workoutPlanId " +
            "ORDER BY ws.date DESC, pr.setNumber")
    Page<PerformanceRecord> findByUserAndWorkoutPlan(@Param("user") User user,
                                                     @Param("workoutPlanId") Long workoutPlanId,
                                                     Pageable pageable);

    /**
     * Find performance records for a specific user and exercise
     */
    @Query("SELECT pr FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user AND pr.exercise = :exercise " +
            "ORDER BY ws.date DESC, pr.setNumber")
    Page<PerformanceRecord> findByUserAndExercise(@Param("user") User user,
                                                  @Param("exercise") Exercise exercise,
                                                  Pageable pageable);

    /**
     * Find performance records for a user within date range
     */
    @Query("SELECT pr FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND ws.date BETWEEN :startDate AND :endDate " +
            "ORDER BY ws.date DESC, pr.setNumber")
    Page<PerformanceRecord> findByUserAndDateRange(@Param("user") User user,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   Pageable pageable);

    // ==============================================
    // PERSONAL RECORDS AND PROGRESS TRACKING
    // ==============================================

    /**
     * Get maximum weight for a specific user and exercise
     */
    @Query("SELECT MAX(pr.weight) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user AND pr.exercise = :exercise " +
            "AND pr.weight IS NOT NULL")
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    Optional<Double> findMaxWeightByUserAndExercise(@Param("user") User user, @Param("exercise") Exercise exercise);

    /**
     * Get maximum volume (weight × reps) for a specific user and exercise
     */
    @Query("SELECT MAX(pr.weight * pr.reps) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user AND pr.exercise = :exercise " +
            "AND pr.weight IS NOT NULL AND pr.reps IS NOT NULL")
    @QueryHints({@QueryHint(name = "org.hibernate.cacheable", value = "true")})
    Optional<Double> findMaxVolumeByUserAndExercise(@Param("user") User user, @Param("exercise") Exercise exercise);

    /**
     * Get personal record for maximum reps at a specific weight
     */
    @Query("SELECT MAX(pr.reps) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user AND pr.exercise = :exercise " +
            "AND pr.weight = :weight AND pr.reps IS NOT NULL")
    Optional<Integer> findMaxRepsAtWeight(@Param("user") User user,
                                          @Param("exercise") Exercise exercise,
                                          @Param("weight") Double weight);

    /**
     * Get best cardio performance (fastest time for a distance)
     */
    @Query("SELECT MIN(pr.durationMinutes + COALESCE(pr.durationSeconds/60, 0)) " +
            "FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user AND pr.exercise = :exercise " +
            "AND pr.distanceKm = :distance " +
            "AND (pr.durationMinutes IS NOT NULL OR pr.durationSeconds IS NOT NULL)")
    Optional<Double> findBestTimeForDistance(@Param("user") User user,
                                             @Param("exercise") Exercise exercise,
                                             @Param("distance") Double distance);

    /**
     * Get longest distance covered in a session
     */
    @Query("SELECT MAX(pr.distanceKm) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user AND pr.exercise = :exercise " +
            "AND pr.distanceKm IS NOT NULL")
    Optional<Double> findMaxDistanceByUserAndExercise(@Param("user") User user, @Param("exercise") Exercise exercise);

    // ==============================================
    // VOLUME AND ANALYTICS QUERIES
    // ==============================================

    /**
     * Get total volume (weight × reps) for a user in a date range
     */
    @Query("SELECT COALESCE(SUM(pr.reps * pr.weight), 0) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND ws.date BETWEEN :startDate AND :endDate " +
            "AND pr.reps IS NOT NULL AND pr.weight IS NOT NULL")
    Double getTotalVolumeByUserAndDateRange(@Param("user") User user,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * Get total volume for a specific exercise in a date range
     */
    @Query("SELECT COALESCE(SUM(pr.reps * pr.weight), 0) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user AND pr.exercise = :exercise " +
            "AND ws.date BETWEEN :startDate AND :endDate " +
            "AND pr.reps IS NOT NULL AND pr.weight IS NOT NULL")
    Double getTotalVolumeByUserExerciseAndDateRange(@Param("user") User user,
                                                    @Param("exercise") Exercise exercise,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    /**
     * Get total workout sessions count in date range
     */
    @Query("SELECT COUNT(DISTINCT ws.id) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND ws.date BETWEEN :startDate AND :endDate")
    Long getTotalWorkoutSessionsByUserAndDateRange(@Param("user") User user,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * Get total sets performed in date range
     */
    @Query("SELECT COUNT(pr) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND ws.date BETWEEN :startDate AND :endDate")
    Long getTotalSetsByUserAndDateRange(@Param("user") User user,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    /**
     * Get average perceived exertion for date range
     */
    @Query("SELECT AVG(pr.perceivedExertion) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND ws.date BETWEEN :startDate AND :endDate " +
            "AND pr.perceivedExertion IS NOT NULL")
    Optional<Double> getAveragePerceivedExertionByUserAndDateRange(@Param("user") User user,
                                                                   @Param("startDate") LocalDate startDate,
                                                                   @Param("endDate") LocalDate endDate);

    // ==============================================
    // PROGRESS TRACKING QUERIES
    // ==============================================

    /**
     * Get performance records for exercise progress chart (last N workouts)
     */
    @Query(value = "SELECT pr.* FROM performance_records pr " +
            "JOIN workout_sessions ws ON pr.workout_session_id = ws.id " +
            "WHERE ws.user_id = :userId AND pr.exercise_id = :exerciseId " +
            "AND pr.weight IS NOT NULL AND pr.reps IS NOT NULL " +
            "ORDER BY ws.date DESC, pr.set_number " +
            "LIMIT :limit",
            nativeQuery = true)
    List<PerformanceRecord> findRecentProgressByUserAndExercise(@Param("userId") Long userId,
                                                                @Param("exerciseId") Long exerciseId,
                                                                @Param("limit") int limit);

    /**
     * Get strength progression data (best set from each workout)
     */
    @Query("SELECT pr FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user AND pr.exercise = :exercise " +
            "AND pr.weight IS NOT NULL AND pr.reps IS NOT NULL " +
            "ORDER BY ws.date DESC")
    List<PerformanceRecord> findStrengthProgressionByUserAndExercise(@Param("user") User user,
                                                                     @Param("exercise") Exercise exercise);

    /**
     * Get volume progression over time (weekly aggregates)
     */
    @Query(value = "SELECT " +
            "DATE_TRUNC('week', ws.date) as week, " +
            "SUM(pr.reps * pr.weight) as total_volume, " +
            "COUNT(DISTINCT ws.id) as workout_count " +
            "FROM performance_records pr " +
            "JOIN workout_sessions ws ON pr.workout_session_id = ws.id " +
            "WHERE ws.user_id = :userId " +
            "AND pr.exercise_id = :exerciseId " +
            "AND ws.date >= :startDate " +
            "AND pr.reps IS NOT NULL AND pr.weight IS NOT NULL " +
            "GROUP BY DATE_TRUNC('week', ws.date) " +
            "ORDER BY week DESC",
            nativeQuery = true)
    List<Object[]> findVolumeProgressionByWeek(@Param("userId") Long userId,
                                               @Param("exerciseId") Long exerciseId,
                                               @Param("startDate") LocalDate startDate);

    // ==============================================
    // ACHIEVEMENT AND TARGET TRACKING (SIMPLIFIED)
    // ==============================================

    /**
     * Count achievements by status for a user (SIMPLIFIED - no targetWeight dependency)
     */
    @Query("SELECT 'EXCEEDED', COUNT(pr) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND pr.notes LIKE '%exceeded%'")
    List<Object[]> countAchievementsByStatusForUser(@Param("user") User user);

    /**
     * Achievement tracking methods (SIMPLIFIED VERSIONS)
     */
    @Query("SELECT COUNT(pr) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND pr.notes LIKE '%exceeded%'")
    Long countExceededTargetsByUser(@Param("user") User user);

    @Query("SELECT COUNT(pr) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND pr.notes LIKE '%met target%'")
    Long countMetTargetsByUser(@Param("user") User user);

    @Query("SELECT COUNT(pr) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND pr.notes LIKE '%below%'")
    Long countBelowTargetsByUser(@Param("user") User user);

    /**
     * Find performances that exceeded targets (SIMPLIFIED)
     */
    @Query("SELECT pr FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND pr.notes LIKE '%exceeded%' " +
            "ORDER BY ws.date DESC")
    Page<PerformanceRecord> findExceededTargetsByUser(@Param("user") User user, Pageable pageable);

    /**
     * Find performances with professional trainer assignments (SIMPLIFIED)
     */
    @Query("SELECT pr FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND pr.assignedByTrainerId = :trainerId " +
            "ORDER BY ws.date DESC")
    Page<PerformanceRecord> findByUserAndAssignedTrainer(@Param("user") User user,
                                                         @Param("trainerId") Long trainerId,
                                                         Pageable pageable);

    // ==============================================
    // ADVANCED ANALYTICS QUERIES
    // ==============================================

    /**
     * Get exercise frequency (how often user performs each exercise)
     */
    @Query("SELECT pr.exercise, COUNT(DISTINCT ws.date) as frequency " +
            "FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND ws.date >= :startDate " +
            "GROUP BY pr.exercise " +
            "ORDER BY frequency DESC")
    List<Object[]> getExerciseFrequencyByUser(@Param("user") User user, @Param("startDate") LocalDate startDate);

    /**
     * Get performance quality metrics (average form rating, RPE)
     */
    @Query("SELECT " +
            "AVG(pr.formRating) as avgFormRating, " +
            "AVG(pr.perceivedExertion) as avgRPE, " +
            "COUNT(pr) as totalSets " +
            "FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND ws.date BETWEEN :startDate AND :endDate " +
            "AND (pr.formRating IS NOT NULL OR pr.perceivedExertion IS NOT NULL)")
    Object[] getPerformanceQualityMetrics(@Param("user") User user,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    /**
     * Get workout intensity distribution
     */
    @Query("SELECT " +
            "CASE " +
            "  WHEN pr.perceivedExertion <= 3 THEN 'LOW' " +
            "  WHEN pr.perceivedExertion <= 6 THEN 'MODERATE' " +
            "  WHEN pr.perceivedExertion <= 8 THEN 'HIGH' " +
            "  ELSE 'MAXIMUM' " +
            "END as intensity, " +
            "COUNT(pr) as count " +
            "FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND ws.date >= :startDate " +
            "AND pr.perceivedExertion IS NOT NULL " +
            "GROUP BY " +
            "CASE " +
            "  WHEN pr.perceivedExertion <= 3 THEN 'LOW' " +
            "  WHEN pr.perceivedExertion <= 6 THEN 'MODERATE' " +
            "  WHEN pr.perceivedExertion <= 8 THEN 'HIGH' " +
            "  ELSE 'MAXIMUM' " +
            "END")
    List<Object[]> getIntensityDistribution(@Param("user") User user, @Param("startDate") LocalDate startDate);

    // ==============================================
    // BULK OPERATIONS AND MAINTENANCE
    // ==============================================

    /**
     * Bulk update rest times for a workout session
     */
    @Modifying
    @Query("UPDATE PerformanceRecord pr SET pr.restSeconds = :restSeconds " +
            "WHERE pr.workoutSession = :workoutSession")
    int bulkUpdateRestTimesByWorkoutSession(@Param("workoutSession") WorkoutSession workoutSession,
                                            @Param("restSeconds") Integer restSeconds);

    /**
     * Bulk update perceived exertion for a workout session
     */
    @Modifying
    @Query("UPDATE PerformanceRecord pr SET pr.perceivedExertion = :rpe " +
            "WHERE pr.workoutSession = :workoutSession")
    int bulkUpdateRPEByWorkoutSession(@Param("workoutSession") WorkoutSession workoutSession,
                                      @Param("rpe") Integer rpe);

    /**
     * Delete old performance records (data retention)
     */
    @Modifying
    @Query("DELETE FROM PerformanceRecord pr " +
            "WHERE pr.createdAt < :cutoffDate")
    int deleteOldPerformanceRecords(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count total performance records for a user
     */
    @Query("SELECT COUNT(pr) FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "WHERE ws.user = :user")
    Long countByUser(@Param("user") User user);

    /**
     * Find duplicate performance records (same workout session, exercise, set number)
     */
    @Query("SELECT pr FROM PerformanceRecord pr " +
            "WHERE EXISTS (" +
            "  SELECT 1 FROM PerformanceRecord pr2 " +
            "  WHERE pr2.workoutSession = pr.workoutSession " +
            "  AND pr2.exercise = pr.exercise " +
            "  AND pr2.setNumber = pr.setNumber " +
            "  AND pr2.id != pr.id" +
            ")")
    List<PerformanceRecord> findDuplicateRecords();

    // ==============================================
    // PERFORMANCE OPTIMIZATION QUERIES
    // ==============================================

    /**
     * Find performance records with minimal data for list views
     */
    @Query("SELECT pr.id, pr.setNumber, pr.reps, pr.weight, " +
            "e.exerciseName, ws.date " +
            "FROM PerformanceRecord pr " +
            "JOIN pr.workoutSession ws " +
            "JOIN pr.exercise e " +
            "WHERE ws.user = :user " +
            "ORDER BY ws.date DESC")
    Page<Object[]> findMinimalByUser(@Param("user") User user, Pageable pageable);

    /**
     * Find recent activity for dashboard
     */
    @Query("SELECT pr FROM PerformanceRecord pr " +
            "JOIN FETCH pr.exercise " +
            "JOIN FETCH pr.workoutSession ws " +
            "WHERE ws.user = :user " +
            "AND pr.createdAt >= :since " +
            "ORDER BY pr.createdAt DESC")
    List<PerformanceRecord> findRecentActivityByUser(@Param("user") User user,
                                                     @Param("since") LocalDateTime since);
}