package com.chidituke.workout_tracker.repository;

import com.chidituke.workout_tracker.model.Workout;
import com.chidituke.workout_tracker.model.WorkoutLog;
import com.chidituke.workout_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;

@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
    // Find all workout logs for a user, ordered by most recent
    List<WorkoutLog> findByUserOrderByDateDesc(User user);

    // Find workout logs for a specific user and date
    List<WorkoutLog> findByUserAndDate(User user, LocalDate date);

    // Find workout logs within a date range
    List<WorkoutLog> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);

    // Count total workouts for a user
    long countByUser(User user);

    // Count workouts in a date range
    @Query("SELECT COUNT(wl) FROM WorkoutLog wl WHERE wl.user = :user AND wl.date BETWEEN :startDate AND :endDate")
    Long countByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
