package com.chidituke.workout_tracker.repository.workout;

import com.chidituke.workout_tracker.model.workout.PlanExercise;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import com.chidituke.workout_tracker.model.workout.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanExerciseRepository extends JpaRepository<PlanExercise, Long> {

    // =======================
    // CORE WORKOUT PLAN QUERIES
    // =======================

    // Get all exercises for a workout plan in order
    List<PlanExercise> findByWorkoutPlanOrderByOrderInWorkout(WorkoutPlan workoutPlan);

    // Get exercises by workout plan ID (most common usage)
    List<PlanExercise> findByWorkoutPlan_IdOrderByOrderInWorkout(Long workoutPlanId);

    // Count exercises in a workout plan
    long countByWorkoutPlan(WorkoutPlan workoutPlan);

    // Find specific exercise within a workout plan
    Optional<PlanExercise> findByWorkoutPlanAndExercise(WorkoutPlan workoutPlan, Exercise exercise);

    // Check if exercise exists in workout plan
    boolean existsByWorkoutPlanAndExercise(WorkoutPlan workoutPlan, Exercise exercise);

    // =======================
    // EXERCISE DISCOVERY
    // =======================

    // Find which workout plans use a specific exercise
    List<PlanExercise> findByExercise(Exercise exercise);

    // Find which workout plans use a specific exercise by ID
    List<PlanExercise> findByExercise_Id(Long exerciseId);

    // Find exercises at specific position in workout plans
    List<PlanExercise> findByOrderInWorkout(Integer order);

    // =======================
    // SUBSCRIPTION TIER FILTERING
    // =======================

    // Get exercises accessible to user's subscription tier
    @Query("SELECT pe FROM PlanExercise pe WHERE pe.workoutPlan = :workoutPlan " +
            "AND (pe.subscriptionTierRequired = 'FREE' " +
            "OR (:userTier = 'PLUS' AND pe.subscriptionTierRequired IN ('FREE', 'PLUS')) " +
            "OR (:userTier = 'PRO' AND pe.subscriptionTierRequired IN ('FREE', 'PLUS', 'PRO'))) " +
            "ORDER BY pe.orderInWorkout")
    List<PlanExercise> findAccessibleExercises(@Param("workoutPlan") WorkoutPlan workoutPlan,
                                               @Param("userTier") String userSubscriptionTier);

    // =======================
    // SUPERSET & CIRCUIT QUERIES
    // =======================

    // Find exercises in same superset group
    List<PlanExercise> findByWorkoutPlanAndSupersetGroupOrderByOrderInWorkout(WorkoutPlan workoutPlan, String supersetGroup);

    // Find all superset exercises in a workout
    List<PlanExercise> findByWorkoutPlanAndIsSuperset(WorkoutPlan workoutPlan, Boolean isSuperset);

    // Get unique superset groups in a workout
    @Query("SELECT DISTINCT pe.supersetGroup FROM PlanExercise pe " +
            "WHERE pe.workoutPlan = :workoutPlan AND pe.supersetGroup IS NOT NULL")
    List<String> findSupersetGroups(@Param("workoutPlan") WorkoutPlan workoutPlan);

    // =======================
    // PROGRESSION TRACKING
    // =======================

    // Find progression exercises in a workout plan
    List<PlanExercise> findByWorkoutPlanAndIsProgressionExercise(WorkoutPlan workoutPlan, Boolean isProgressionExercise);

    // Find exercises with specific progression goals
    @Query("SELECT pe FROM PlanExercise pe WHERE pe.workoutPlan = :workoutPlan " +
            "AND pe.progressionGoal IS NOT NULL AND pe.progressionGoal != ''")
    List<PlanExercise> findExercisesWithProgressionGoals(@Param("workoutPlan") WorkoutPlan workoutPlan);

    // =======================
    // USER CUSTOMIZATION QUERIES
    // =======================

    // Find user customizations for a workout plan
    List<PlanExercise> findByWorkoutPlanAndIsUserCustomization(WorkoutPlan workoutPlan, Boolean isUserCustomization);

    // Find exercises created by specific user
    List<PlanExercise> findByCreatedByUserId(Long userId);

    // Find user's customizations across all workout plans
    List<PlanExercise> findByCreatedByUserIdAndIsUserCustomization(Long userId, Boolean isUserCustomization);

    // =======================
    // EQUIPMENT & ALTERNATIVES
    // =======================

    // Find exercises with equipment alternatives
    @Query("SELECT pe FROM PlanExercise pe WHERE pe.workoutPlan = :workoutPlan " +
            "AND (pe.equipmentAlternatives IS NOT NULL OR pe.alternativeExercise IS NOT NULL)")
    List<PlanExercise> findExercisesWithAlternatives(@Param("workoutPlan") WorkoutPlan workoutPlan);

    // Find exercises that have alternative exercises set
    List<PlanExercise> findByAlternativeExerciseIsNotNull();

    // =======================
    // OPTIONAL & CORE EXERCISES
    // =======================

    // Find required (non-optional) exercises in workout plan
    List<PlanExercise> findByWorkoutPlanAndIsOptionalOrderByOrderInWorkout(WorkoutPlan workoutPlan, Boolean isOptional);

    // Count required vs optional exercises
    @Query("SELECT pe.isOptional, COUNT(pe) FROM PlanExercise pe " +
            "WHERE pe.workoutPlan = :workoutPlan GROUP BY pe.isOptional")
    List<Object[]> countByOptionalStatus(@Param("workoutPlan") WorkoutPlan workoutPlan);

    // =======================
    // PRESCRIPTION QUERIES
    // =======================

    // Find exercises with specific RPE targets
    List<PlanExercise> findByWorkoutPlanAndPrescribedRpe(WorkoutPlan workoutPlan, Integer prescribedRpe);

    // Find exercises with weight percentages
    List<PlanExercise> findByWorkoutPlanAndPrescribedWeightPercentIsNotNull(WorkoutPlan workoutPlan);

    // Find exercises with coaching cues
    @Query("SELECT pe FROM PlanExercise pe WHERE pe.workoutPlan = :workoutPlan " +
            "AND pe.coachingCues IS NOT NULL AND pe.coachingCues != ''")
    List<PlanExercise> findExercisesWithCoachingCues(@Param("workoutPlan") WorkoutPlan workoutPlan);

    // =======================
    // WORKOUT STRUCTURE ANALYSIS
    // =======================

    // Get workout plan summary with exercise count
    @Query("SELECT pe.workoutPlan, COUNT(pe), AVG(pe.prescribedSets), AVG(pe.prescribedRestSeconds) " +
            "FROM PlanExercise pe WHERE pe.workoutPlan = :workoutPlan GROUP BY pe.workoutPlan")
    List<Object[]> getWorkoutPlanSummary(@Param("workoutPlan") WorkoutPlan workoutPlan);

    // Find exercises in order range (e.g., first 3 exercises)
    List<PlanExercise> findByWorkoutPlanAndOrderInWorkoutBetweenOrderByOrderInWorkout(
            WorkoutPlan workoutPlan, Integer startOrder, Integer endOrder);

    // Get maximum order number in workout plan (for adding new exercises)
    @Query("SELECT MAX(pe.orderInWorkout) FROM PlanExercise pe WHERE pe.workoutPlan = :workoutPlan")
    Optional<Integer> findMaxOrderInWorkout(@Param("workoutPlan") WorkoutPlan workoutPlan);

    // Find exercises with order greater than specified (for reordering after deletion)
    List<PlanExercise> findByWorkoutPlanAndOrderInWorkoutGreaterThanOrderByOrderInWorkout(
            WorkoutPlan workoutPlan, Integer order);

    // =======================
    // BULK OPERATIONS
    // =======================

    // Delete all exercises from a workout plan
    void deleteByWorkoutPlan(WorkoutPlan workoutPlan);

    // Delete user customizations for a workout plan
    void deleteByWorkoutPlanAndCreatedByUserId(WorkoutPlan workoutPlan, Long userId);
}