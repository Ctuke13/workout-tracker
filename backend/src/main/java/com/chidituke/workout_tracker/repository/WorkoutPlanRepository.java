package com.chidituke.workout_tracker.repository;

import com.chidituke.workout_tracker.model.WorkoutPlan;
import com.chidituke.workout_tracker.model.WorkoutPlan.DifficultyLevel;
import com.chidituke.workout_tracker.model.WorkoutPlan.WorkoutType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {

    // =======================
    // PUBLIC DISCOVERY QUERIES
    // =======================

    // Find public workout plans (for visitors and users)
    List<WorkoutPlan> findByIsPublicTrueOrderByTimesUsedDesc();

    // Find by category (popular for visitors browsing)
    List<WorkoutPlan> findByWorkoutCategoryIgnoreCaseAndIsPublicTrue(String category);

    // Search by name (for visitors searching)
    Page<WorkoutPlan> findByWorkoutNameContainingIgnoreCaseAndIsPublicTrue(String name, Pageable pageable);

    // Filter by workout type
    List<WorkoutPlan> findByWorkoutTypeAndIsPublicTrue(WorkoutType workoutType);

    // Filter by difficulty level
    List<WorkoutPlan> findByDifficultyLevelAndIsPublicTrue(DifficultyLevel difficultyLevel);

    // Filter by equipment (contains check for multiple equipment)
    @Query("SELECT w FROM WorkoutPlan w WHERE w.isPublic = true AND " +
            "(:equipment IS NULL OR UPPER(w.equipmentNeeded) LIKE UPPER(CONCAT('%', :equipment, '%')))")
    List<WorkoutPlan> findByEquipmentNeeded(@Param("equipment") String equipment);

    // Filter by target muscle groups
    @Query("SELECT w FROM WorkoutPlan w WHERE w.isPublic = true AND " +
            "(:muscleGroup IS NULL OR UPPER(w.targetMuscleGroups) LIKE UPPER(CONCAT('%', :muscleGroup, '%')))")
    List<WorkoutPlan> findByTargetMuscleGroups(@Param("muscleGroup") String muscleGroup);

    // =======================
    // ADVANCED FILTERING
    // =======================

    // Multi-filter search for visitors
    @Query("SELECT w FROM WorkoutPlan w WHERE w.isPublic = true " +
            "AND (:category IS NULL OR UPPER(w.workoutCategory) = UPPER(:category)) " +
            "AND (:workoutType IS NULL OR w.workoutType = :workoutType) " +
            "AND (:difficultyLevel IS NULL OR w.difficultyLevel = :difficultyLevel) " +
            "AND (:equipment IS NULL OR UPPER(w.equipmentNeeded) LIKE UPPER(CONCAT('%', :equipment, '%'))) " +
            "AND (:muscleGroup IS NULL OR UPPER(w.targetMuscleGroups) LIKE UPPER(CONCAT('%', :muscleGroup, '%'))) " +
            "ORDER BY w.timesUsed DESC")
    Page<WorkoutPlan> findWithFilters(@Param("category") String category,
                                      @Param("workoutType") WorkoutType workoutType,
                                      @Param("difficultyLevel") DifficultyLevel difficultyLevel,
                                      @Param("equipment") String equipment,
                                      @Param("muscleGroup") String muscleGroup,
                                      Pageable pageable);

    // =======================
    // SUBSCRIPTION TIER FILTERING
    // =======================

    // Find workouts accessible to user's subscription tier
    @Query("SELECT w FROM WorkoutPlan w WHERE w.isPublic = true " +
            "AND (w.subscriptionTierRequired = 'FREE' " +
            "OR (:userTier = 'PLUS' AND w.subscriptionTierRequired IN ('FREE', 'PLUS')) " +
            "OR (:userTier = 'PRO' AND w.subscriptionTierRequired IN ('FREE', 'PLUS', 'PRO'))) " +
            "ORDER BY w.averageRating DESC")
    List<WorkoutPlan> findAccessibleWorkouts(@Param("userTier") String userSubscriptionTier);

    // =======================
    // USER-SPECIFIC QUERIES
    // =======================

    // Find workouts created by specific user
    List<WorkoutPlan> findByCreatedByUserIdOrderByCreatedAtDesc(Long userId);

    // Find user's private workouts
    List<WorkoutPlan> findByCreatedByUserIdAndIsPublicFalse(Long userId);

    // Find most popular workouts (for recommendations)
    List<WorkoutPlan> findTop10ByIsPublicTrueOrderByTimesUsedDesc();

    // Find highly rated workouts
    @Query("SELECT w FROM WorkoutPlan w WHERE w.isPublic = true " +
            "AND w.averageRating >= :minRating ORDER BY w.averageRating DESC")
    List<WorkoutPlan> findHighlyRatedWorkouts(@Param("minRating") Double minRating);

    // =======================
    // USAGE TRACKING
    // =======================

    // Increment usage count when workout is used
    @Query("UPDATE WorkoutPlan w SET w.timesUsed = w.timesUsed + 1 WHERE w.id = :workoutId")
    void incrementUsageCount(@Param("workoutId") Long workoutId);

    // =======================
    // ANALYTICS QUERIES
    // =======================

    // Count public workout plans
    long countByIsPublicTrue();

    // Count workouts by category
    @Query("SELECT w.workoutCategory, COUNT(w) FROM WorkoutPlan w " +
            "WHERE w.isPublic = true GROUP BY w.workoutCategory")
    List<Object[]> countByCategory();

    // Count workouts by difficulty
    @Query("SELECT w.difficultyLevel, COUNT(w) FROM WorkoutPlan w " +
            "WHERE w.isPublic = true GROUP BY w.difficultyLevel")
    List<Object[]> countByDifficultyLevel();

    // Find trending workouts (most used in recent period)
    @Query(
            value = "SELECT * FROM workout_plan " +
                    "WHERE is_public = true " +
                    "AND updated_at >= CURRENT_DATE - INTERVAL '30 days' " +
                    "ORDER BY times_used DESC",
            nativeQuery = true
    )
    List<WorkoutPlan> findTrendingWorkouts(Pageable pageable);
}