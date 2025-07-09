package com.chidituke.workout_tracker.repository.workout;

import com.chidituke.workout_tracker.model.workout.ExerciseGoalMapping;
import com.chidituke.workout_tracker.model.workout.ExerciseGoalMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseGoalMappingRepository extends JpaRepository<ExerciseGoalMapping, ExerciseGoalMappingId> {

    // 🎯 BASIC QUERIES
    List<ExerciseGoalMapping> findByExerciseId(Long exerciseId);

    List<ExerciseGoalMapping> findByGoalId(Integer goalId);

    Optional<ExerciseGoalMapping> findByExerciseIdAndGoalId(Long exerciseId, Integer goalId);

    // 🏆 PRIMARY GOAL QUERIES
    Optional<ExerciseGoalMapping> findByExerciseIdAndIsPrimaryTrue(Long exerciseId);

    List<ExerciseGoalMapping> findByGoalIdAndIsPrimaryTrue(Integer goalId);

    // 📊 RELEVANCE QUERIES
    List<ExerciseGoalMapping> findByGoalIdAndRelevanceScoreGreaterThanEqual(Integer goalId, Integer minRelevance);

    List<ExerciseGoalMapping> findByExerciseIdOrderByRelevanceScoreDesc(Long exerciseId);

    // 🔧 ADMIN/MANAGEMENT QUERIES
    @Query("SELECT COUNT(egm) FROM ExerciseGoalMapping egm WHERE egm.goalId = :goalId")
    Long countExercisesForGoal(@Param("goalId") Integer goalId);

    @Query("SELECT COUNT(egm) FROM ExerciseGoalMapping egm WHERE egm.exerciseId = :exerciseId")
    Long countGoalsForExercise(@Param("exerciseId") Long exerciseId);

    @Query("SELECT AVG(egm.relevanceScore) FROM ExerciseGoalMapping egm WHERE egm.goalId = :goalId")
    Double getAverageRelevanceForGoal(@Param("goalId") Integer goalId);

    // 🔧 DATA MANAGEMENT
    @Modifying
    @Query("DELETE FROM ExerciseGoalMapping egm WHERE egm.exerciseId = :exerciseId")
    void deleteByExerciseId(@Param("exerciseId") Long exerciseId);

    @Modifying
    @Query("DELETE FROM ExerciseGoalMapping egm WHERE egm.goalId = :goalId")
    void deleteByGoalId(@Param("goalId") Integer goalId);

    @Modifying
    @Query("UPDATE ExerciseGoalMapping egm SET egm.isPrimary = false WHERE egm.exerciseId = :exerciseId")
    void clearPrimaryGoalForExercise(@Param("exerciseId") Long exerciseId);
}