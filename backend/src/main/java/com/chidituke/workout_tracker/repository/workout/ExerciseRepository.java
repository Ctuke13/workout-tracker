package com.chidituke.workout_tracker.repository.workout;

import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.model.workout.FitnessGoal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    // 📚 BASIC QUERIES
    List<Exercise> findByExerciseTypeAndPublishedTrueOrderByExerciseNameAsc(Exercise.ExerciseType exerciseType);

    @Query("SELECT e FROM Exercise e WHERE (e.published = true OR e.published IS NULL)")
    List<Exercise> findPublishedExercises();

    Page<Exercise> findByPublishedTrueOrderByExerciseNameAsc(Pageable pageable);

    // 🎯 EXERCISE TYPE & DIFFICULTY QUERIES
    List<Exercise> findByDifficultyLevelAndPublishedTrueOrderByExerciseNameAsc(Exercise.DifficultyLevel difficultyLevel);

    List<Exercise> findByExerciseTypeAndDifficultyLevelAndPublishedTrueOrderByExerciseNameAsc(
            Exercise.ExerciseType exerciseType, Exercise.DifficultyLevel difficultyLevel);

    // Get all fitness goals for UI
    @Query("SELECT fg FROM FitnessGoal fg WHERE fg.isActive = true ORDER BY fg.displayOrder ASC")
    List<FitnessGoal> findActiveGoals();

    // Get goal statistics for frontend goals API
    @Query("SELECT fg.goalCode, fg.goalName, fg.goalEmoji, COUNT(DISTINCT e) as exerciseCount " +
            "FROM FitnessGoal fg " +
            "LEFT JOIN ExerciseGoalMapping egm ON fg.goalId = egm.goalId " +
            "LEFT JOIN Exercise e ON egm.exerciseId = e.id AND e.published = true AND egm.relevanceScore >= 3 " +
            "WHERE fg.isActive = true " +
            "GROUP BY fg.goalId, fg.goalCode, fg.goalName, fg.goalEmoji, fg.displayOrder " +
            "ORDER BY fg.displayOrder ASC")
    List<Object[]> getGoalStatistics();

    // 🎯 GOAL-BASED EXERCISE FILTERING WITH PAGINATION
    @Query("SELECT DISTINCT e FROM Exercise e " +
            "JOIN ExerciseGoalMapping egm ON e.id = egm.exerciseId " +
            "JOIN FitnessGoal fg ON egm.goalId = fg.goalId " +
            "WHERE fg.goalCode = :goalCode " +
            "AND egm.relevanceScore >= :minRelevance " +
            "AND e.published = true " +
            "ORDER BY egm.relevanceScore DESC, e.averageRating DESC, e.usageCount DESC, e.exerciseName ASC")
    Page<Exercise> findByGoalWithPagination(
            @Param("goalCode") String goalCode,
            @Param("minRelevance") int minRelevance,
            Pageable pageable);

    // 🔍 GOAL-BASED SEARCH WITH PAGINATION
    @Query("SELECT DISTINCT e FROM Exercise e " +
            "JOIN ExerciseGoalMapping egm ON e.id = egm.exerciseId " +
            "JOIN FitnessGoal fg ON egm.goalId = fg.goalId " +
            "WHERE fg.goalCode = :goalCode " +
            "AND egm.relevanceScore >= :minRelevance " +
            "AND e.published = true " +
            "AND (:search IS NULL OR :search = '' OR " +
            "     LOWER(e.exerciseName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "     LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY egm.relevanceScore DESC, e.averageRating DESC, e.usageCount DESC")
    Page<Exercise> searchByGoalWithPagination(
            @Param("goalCode") String goalCode,
            @Param("search") String search,
            @Param("minRelevance") int minRelevance,
            Pageable pageable);

    // 📊 COUNT EXERCISES BY GOAL (for pagination info)
    @Query("SELECT COUNT(DISTINCT e) FROM Exercise e " +
            "JOIN ExerciseGoalMapping egm ON e.id = egm.exerciseId " +
            "JOIN FitnessGoal fg ON egm.goalId = fg.goalId " +
            "WHERE fg.goalCode = :goalCode " +
            "AND egm.relevanceScore >= :minRelevance " +
            "AND e.published = true")
    Long countByGoal(@Param("goalCode") String goalCode, @Param("minRelevance") int minRelevance);

    // 🏆 GET EXERCISE GOALS (for individual exercise details)
    @Query("SELECT fg.goalCode, fg.goalName, fg.goalEmoji, egm.relevanceScore, egm.isPrimary " +
            "FROM FitnessGoal fg " +
            "JOIN ExerciseGoalMapping egm ON fg.goalId = egm.goalId " +
            "WHERE egm.exerciseId = :exerciseId " +
            "ORDER BY egm.isPrimary DESC, egm.relevanceScore DESC")
    List<Object[]> getExerciseGoals(@Param("exerciseId") Long exerciseId);

    // 🎯 GET PRIMARY GOAL FOR EXERCISE
    @Query("SELECT fg FROM FitnessGoal fg " +
            "JOIN ExerciseGoalMapping egm ON fg.goalId = egm.goalId " +
            "WHERE egm.exerciseId = :exerciseId AND egm.isPrimary = true")
    Optional<FitnessGoal> getPrimaryGoalForExercise(@Param("exerciseId") Long exerciseId);

    // 🔍 ENHANCED PUBLIC SEARCH (combines text search with optional goal filter)
    @Query("SELECT DISTINCT e FROM Exercise e " +
            "LEFT JOIN ExerciseGoalMapping egm ON e.id = egm.exerciseId " +
            "LEFT JOIN FitnessGoal fg ON egm.goalId = fg.goalId " +
            "WHERE e.published = true " +
            "AND (:goalCode IS NULL OR :goalCode = '' OR " +
            "     (fg.goalCode = :goalCode AND egm.relevanceScore >= :minRelevance)) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "     LOWER(e.exerciseName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "     LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY " +
            "CASE WHEN :goalCode IS NOT NULL AND :goalCode != '' " +
            "     THEN egm.relevanceScore ELSE 0 END DESC, " +
            "e.averageRating DESC, e.usageCount DESC, e.exerciseName ASC")
    Page<Exercise> searchPublicExercisesWithGoals(
            @Param("search") String search,
            @Param("goalCode") String goalCode,
            @Param("minRelevance") int minRelevance,
            Pageable pageable);

    // 🎯 RECOMMENDED EXERCISES BY GOALS (for authenticated users)
    @Query("SELECT DISTINCT e FROM Exercise e " +
            "JOIN ExerciseGoalMapping egm ON e.id = egm.exerciseId " +
            "JOIN FitnessGoal fg ON egm.goalId = fg.goalId " +
            "WHERE fg.goalCode IN :preferredGoals " +
            "AND egm.relevanceScore >= 4 " +  // Only great/perfect fits for recommendations
            "AND e.published = true " +
            "ORDER BY " +
            "CASE WHEN e.createdByProfessional = true THEN 1 ELSE 0 END DESC, " +
            "egm.relevanceScore DESC, " +
            "e.averageRating DESC, " +
            "e.usageCount DESC")
    Page<Exercise> findRecommendationsByGoals(
            @Param("preferredGoals") List<String> preferredGoals,
            Pageable pageable);

    // 🏋️ EQUIPMENT-BASED QUERIES
    @Query("SELECT e FROM Exercise e WHERE e.published = true AND " +
            "(:equipment MEMBER OF e.equipmentRequired OR SIZE(e.equipmentRequired) = 0)")
    List<Exercise> findByEquipmentAvailable(@Param("equipment") String equipment);

    @Query("SELECT e FROM Exercise e WHERE e.published = true AND " +
            "(e.equipmentRequired IS EMPTY OR SIZE(e.equipmentRequired) = 0)")
    List<Exercise> findBodyweightExercises();

    // 💪 MUSCLE GROUP QUERIES
    // 🔄 FIXED - Renamed for consistency and removed duplicate method
    @Query("SELECT e FROM Exercise e WHERE e.published = true AND " +
            ":muscleGroup MEMBER OF e.targetMuscleGroups")
    List<Exercise> findByTargetMuscleGroupContainingAndPublishedTrue(@Param("muscleGroup") String muscleGroup);

    // 🔍 SEARCH QUERIES
    @Query("SELECT e FROM Exercise e WHERE e.published = true AND " +
            "(:search IS NULL OR :search = '' OR " +
            "e.exerciseName LIKE CONCAT('%', :search, '%') OR " +
            "e.description LIKE CONCAT('%', :search, '%'))")
    List<Exercise> findByNameOrDescriptionContaining(@Param("search") String search);

    // 🎯 COMPREHENSIVE SEARCH WITH FILTERS
    @Query("SELECT e FROM Exercise e WHERE e.published = true " +
            "AND (:exerciseType IS NULL OR e.exerciseType = :exerciseType) " +
            "AND (:difficulty IS NULL OR e.difficultyLevel = :difficulty) " +
            "AND (:muscleGroup IS NULL OR :muscleGroup MEMBER OF e.targetMuscleGroups) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "     e.exerciseName LIKE CONCAT('%', :search, '%') OR " +
            "     e.description LIKE CONCAT('%', :search, '%')) " +
            "ORDER BY e.averageRating DESC, e.usageCount DESC, e.exerciseName ASC")
    Page<Exercise> searchExercisesWithFilters(
            @Param("search") String search,
            @Param("muscleGroup") String muscleGroup,
            @Param("exerciseType") Exercise.ExerciseType exerciseType,
            @Param("difficulty") Exercise.DifficultyLevel difficulty,
            Pageable pageable);

    @Query("SELECT e FROM Exercise e WHERE e.published = true " +
            "AND (COALESCE(:preferredMuscleGroups, null) IS NULL OR " +
            "     EXISTS (SELECT mg FROM e.targetMuscleGroups mg WHERE mg IN :preferredMuscleGroups)) " +
            "ORDER BY " +
            "CASE WHEN e.createdByProfessional = true THEN 1 ELSE 0 END DESC, " +
            "e.averageRating DESC, " +
            "e.usageCount DESC, " +
            "e.createdAt DESC")
    Page<Exercise> findRecommendationsByMuscleGroups(@Param("preferredMuscleGroups") List<String> preferredMuscleGroups,
                                                     Pageable pageable);

    // 🏆 POPULAR & RECOMMENDED QUERIES
    @Query("SELECT e FROM Exercise e WHERE e.published = true " +
            "ORDER BY e.usageCount DESC, e.averageRating DESC")
    Page<Exercise> findMostPopular(Pageable pageable);

    @Query("SELECT e FROM Exercise e WHERE e.published = true " +
            "AND e.averageRating >= 4.0 AND e.totalRatings >= 5 " +
            "ORDER BY e.averageRating DESC, e.totalRatings DESC")
    List<Exercise> findHighlyRated();

    @Query("SELECT e FROM Exercise e WHERE e.published = true " +
            "ORDER BY " +
            "CASE WHEN e.createdByProfessional = true THEN 1 ELSE 0 END DESC, " +
            "e.averageRating DESC, " +
            "e.usageCount DESC, " +
            "e.createdAt DESC")
    Page<Exercise> findRecommendations(Pageable pageable);

    @Query("SELECT e FROM Exercise e WHERE e.published = true " +
            "AND (:preferredMuscleGroup IS NULL OR :preferredMuscleGroup MEMBER OF e.targetMuscleGroups) " +
            "ORDER BY " +
            "CASE WHEN e.createdByProfessional = true THEN 1 ELSE 0 END DESC, " +
            "e.averageRating DESC, " +
            "e.usageCount DESC, " +
            "e.createdAt DESC")
    Page<Exercise> findRecommendationsByMuscleGroup(@Param("preferredMuscleGroup") String preferredMuscleGroup,
                                                    Pageable pageable);

    @Query("SELECT e FROM Exercise e " +
            "WHERE e.published = true " +
            "AND (:muscleGroups IS NULL OR EXISTS (SELECT mg FROM e.targetMuscleGroups mg WHERE mg IN :muscleGroups)) " +
            "AND (:maxDifficulty IS NULL OR e.difficultyLevel <= :maxDifficulty) " +
            "ORDER BY e.averageRating DESC, e.usageCount DESC")
    List<Exercise> findOptimizedForWorkoutPlan(
            @Param("muscleGroups") List<String> muscleGroups,
            @Param("maxDifficulty") Exercise.DifficultyLevel maxDifficulty,
            Pageable pageable
    );

    @Query("SELECT DISTINCT e FROM Exercise e " +
            "LEFT JOIN FETCH e.targetMuscleGroups " +
            "LEFT JOIN FETCH e.equipmentRequired " +
            "WHERE e.published = true " +
            "ORDER BY e.averageRating DESC, e.usageCount DESC")
    List<Exercise> findPublishedExercisesWithDetails(Pageable pageable);

    // 👨‍💼 PROFESSIONAL CONTENT QUERIES
    @Query("SELECT e FROM Exercise e WHERE e.createdByUserId = :userId ORDER BY e.createdAt DESC")
    List<Exercise> findByCreatedByUserId(@Param("userId") Long userId);

    @Query("SELECT e FROM Exercise e WHERE e.createdByProfessional = true AND e.published = false")
    List<Exercise> findPendingProfessionalApproval();

    // 📊 ANALYTICS QUERIES
    // ✅ UNCHANGED - These work correctly
    @Query("SELECT e.exerciseType, COUNT(e) FROM Exercise e WHERE e.published = true GROUP BY e.exerciseType")
    List<Object[]> countByExerciseType();

    @Query("SELECT e.difficultyLevel, COUNT(e) FROM Exercise e WHERE e.published = true GROUP BY e.difficultyLevel")
    List<Object[]> countByDifficultyLevel();

    @Query("SELECT " +
            "e.exerciseType as type, " +
            "COUNT(e) as count " +
            "FROM Exercise e " +
            "WHERE e.published = true " +
            "GROUP BY e.exerciseType")
    List<Object[]> getExerciseTypeCounts();

    @Query("SELECT " +
            "e.difficultyLevel as level, " +
            "COUNT(e) as count " +
            "FROM Exercise e " +
            "WHERE e.published = true " +
            "GROUP BY e.difficultyLevel")
    List<Object[]> getDifficultyLevelCounts();

    @Query("SELECT mg, COUNT(e) " +
            "FROM Exercise e " +
            "JOIN e.targetMuscleGroups mg " +
            "WHERE e.published = true " +
            "GROUP BY mg " +
            "ORDER BY COUNT(e) DESC")
    List<Object[]> getMuscleGroupCounts();

    @Query("SELECT eq, COUNT(e) " +
            "FROM Exercise e " +
            "JOIN e.equipmentRequired eq " +
            "WHERE e.published = true " +
            "GROUP BY eq " +
            "ORDER BY COUNT(e) DESC")
    List<Object[]> getEquipmentCounts();

    // 🔧 ADMIN QUERIES
    @Query("SELECT e FROM Exercise e WHERE e.published = false")
    List<Exercise> findUnpublishedExercises();

    @Query("SELECT COUNT(e) FROM Exercise e WHERE e.published = true")
    Long countPublishedExercises();

    @Query("SELECT COUNT(e) FROM Exercise e WHERE e.createdByProfessional = true")
    Long countProfessionalContent();

    @Query("SELECT COUNT(e) FROM Exercise e WHERE e.usageCount > :threshold")
    Long countPopularExercises(@Param("threshold") Integer threshold);

    @Query("SELECT AVG(e.averageRating) FROM Exercise e WHERE e.published = true AND e.totalRatings >= 5")
    Double getAverageRatingAcrossExercises();

    @Query("SELECT COUNT(e) FROM Exercise e WHERE e.createdAt >= :since")
    Long countExercisesCreatedSince(@Param("since") java.time.LocalDateTime since);
}