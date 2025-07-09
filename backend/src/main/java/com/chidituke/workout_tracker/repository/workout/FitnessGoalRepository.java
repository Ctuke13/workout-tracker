package com.chidituke.workout_tracker.repository.workout;

import com.chidituke.workout_tracker.model.workout.FitnessGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FitnessGoalRepository extends JpaRepository<FitnessGoal, Integer> {

    // 🎯 BASIC QUERIES
    Optional<FitnessGoal> findByGoalCode(String goalCode);

    List<FitnessGoal> findByIsActiveTrueOrderByDisplayOrderAsc();

    // 📊 ANALYTICS QUERIES
    @Query("SELECT COUNT(fg) FROM FitnessGoal fg WHERE fg.isActive = true")
    Long countActiveGoals();

    @Query("SELECT fg FROM FitnessGoal fg WHERE fg.isActive = true AND fg.displayOrder <= :maxOrder ORDER BY fg.displayOrder ASC")
    List<FitnessGoal> findTopGoals(@Param("maxOrder") Integer maxOrder);

    // 🔍 SEARCH QUERIES
    @Query("SELECT fg FROM FitnessGoal fg WHERE fg.isActive = true AND " +
            "(LOWER(fg.goalName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(fg.goalCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<FitnessGoal> searchGoals(@Param("search") String search);
}