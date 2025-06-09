
package com.chidituke.workout_tracker.repository;

import com.chidituke.workout_tracker.model.Exercise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    // Basic published exercises
    List<Exercise> findByPublishedTrueOrderByNameAsc();

    // Single filters
    List<Exercise> findByGoalAndPublishedTrueOrderByNameAsc(String goal);
    List<Exercise> findByDifficultyAndPublishedTrueOrderByNameAsc(String difficulty);
    List<Exercise> findByEquipmentAndPublishedTrueOrderByNameAsc(String equipment);  // ✅ FIXED: Removed IgnoreCase

    // Two-parameter combinations
    List<Exercise> findByGoalAndDifficultyAndPublishedTrueOrderByNameAsc(String goal, String difficulty);
    List<Exercise> findByGoalAndEquipmentAndPublishedTrueOrderByNameAsc(String goal, String equipment);  // ✅ FIXED: Removed IgnoreCase
    List<Exercise> findByDifficultyAndEquipmentAndPublishedTrueOrderByNameAsc(String difficulty, String equipment);  // ✅ ADDED

    // Three-parameter combination
    List<Exercise> findByGoalAndDifficultyAndEquipmentAndPublishedTrueOrderByNameAsc(String goal, String difficulty, String equipment);  // ✅ ADDED

    // Search by name or description (KEEP - this works!)
    @Query("SELECT e FROM Exercise e WHERE e.published = true AND " +
            "(LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY e.name ASC")
    List<Exercise> searchExercises(@Param("search") String search);

    // ❌ REMOVE THIS PROBLEMATIC METHOD (comment it out or delete):
    /*
    @Query("SELECT e FROM Exercise e WHERE e.published = true " +
            "AND (:goal IS NULL OR LOWER(e.goal) = LOWER(:goal)) " +
            "AND (:difficulty IS NULL OR LOWER(e.difficulty) = LOWER(:difficulty)) " +
            "AND (:equipment IS NULL OR LOWER(e.equipment) = LOWER(:equipment)) " +
            "AND (:search IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "     OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY e.name ASC")
    List<Exercise> findFilteredExercises(
            @Param("goal") String goal,
            @Param("difficulty") String difficulty,
            @Param("equipment") String equipment,
            @Param("search") String search
    );
    */

    // Keep all your working @Query methods:
    @Query("SELECT e.goal, COUNT(e) FROM Exercise e WHERE e.published = true GROUP BY e.goal")
    List<Object[]> findGoalsWithCounts();

    @Query("SELECT DISTINCT e.equipment FROM Exercise e WHERE e.published = true ORDER BY e.equipment")
    List<String> findDistinctEquipment();

    @Query("SELECT DISTINCT e.difficulty FROM Exercise e WHERE e.published = true ORDER BY e.difficulty")
    List<String> findDistinctDifficulties();

    // Keep pagination methods:
    Page<Exercise> findByPublishedTrueOrderByNameAsc(Pageable pageable);
    Page<Exercise> findByGoalAndPublishedTrueOrderByNameAsc(String goal, Pageable pageable);
}