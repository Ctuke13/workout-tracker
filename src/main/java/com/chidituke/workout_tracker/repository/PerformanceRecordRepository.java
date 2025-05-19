package com.chidituke.workout_tracker.repository;

import com.chidituke.workout_tracker.model.PerformanceRecord;
import com.chidituke.workout_tracker.model.WorkoutLog;
import com.chidituke.workout_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PerformanceRecordRepository extends JpaRepository<PerformanceRecord, Long> {
    // Find all performance records for a workout log
    List<PerformanceRecord> findByWorkoutLogOrderBySetNumber(WorkoutLog workoutLog);

    // Find performance records for a specific user and workout
    @Query("SELECT pr FROM PerformanceRecord pr " +
            "JOIN pr.workoutLog wl " +
            "JOIN wl.workout w " +
            "WHERE wl.user = :user AND w.id = :workoutId " +
            "ORDER BY wl.date DESC, pr.setNumber")
    List<PerformanceRecord> findByUserAndWorkoutOrderByDateDesc(@Param("user") User user, @Param("workoutId") Long workoutId);

    // Get max weight for progress tracking
    @Query("SELECT MAX(pr.weight) FROM PerformanceRecord pr " +
            "JOIN pr.workoutLog wl " +
            "JOIN wl.workout w " +
            "WHERE wl.user = :user AND w.id = :workoutId"
    )
    Double findMaxWeightByUserAndWorkout(@Param("user") User user, @Param("workoutId") Long workoutId);

    @Query("SELECT SUM(pr.reps * pr.weight) FROM PerformanceRecord  pr " +
            "JOIN pr.workoutLog wl " +
            "WHERE wl.user = :user AND wl.date BETWEEN :startDate AND :endDate")
    Double getTotalVolumeByUserAndDateRange(@Param("user") User user,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

}
