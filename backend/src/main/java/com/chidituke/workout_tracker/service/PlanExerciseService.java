package com.chidituke.workout_tracker.service;

import com.chidituke.workout_tracker.dto.request.plan_exercise.PlanExerciseRequest;
import com.chidituke.workout_tracker.dto.response.plan_exercise.PlanExerciseResponse;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.exceptions.workout_plan.WorkoutPlanNotFoundException;
import com.chidituke.workout_tracker.exceptions.exercise.ExerciseNotFoundException;
import com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException;
import com.chidituke.workout_tracker.mapper.PlanExerciseMapper;
import com.chidituke.workout_tracker.model.*;
import com.chidituke.workout_tracker.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing exercises within workout plans (junction table operations)
 * This is a specialized service for the PlanExercise junction table
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanExerciseService {

    private final PlanExerciseRepository planExerciseRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final PlanExerciseMapper planExerciseMapper;

    // =======================
    // PLAN EXERCISE RETRIEVAL
    // =======================

    public List<PlanExerciseResponse> getExercisesInWorkoutPlan(Long workoutPlanId) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(workoutPlanId);

        // Only return if workout plan is public
        if (!workoutPlan.getIsPublic()) {
            throw new UnauthorizedOperationException("Workout plan is not public");
        }

        List<PlanExercise> planExercises = planExerciseRepository
                .findByWorkoutPlanOrderByOrderInWorkout(workoutPlan);

        return planExerciseMapper.toResponseList(planExercises);
    }

    public List<PlanExerciseResponse> getExercisesInWorkoutPlan(Long workoutPlanId, String username) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(workoutPlanId);
        User user = findUserByUsername(username);

        // Check access: public OR user owns it
        if (!workoutPlan.getIsPublic() && !isOwner(workoutPlan, user)) {
            throw new UnauthorizedOperationException("User does not have access to this workout plan");
        }

        // Get exercises accessible to user's subscription tier
        List<PlanExercise> planExercises = planExerciseRepository
                .findAccessibleExercises(workoutPlan, user.getSubscriptionTier().name());

        return planExerciseMapper.toResponseList(planExercises);
    }

    public Optional<PlanExerciseResponse> getPlanExerciseById(Long planExerciseId, String username) {
        Optional<PlanExercise> planExercise = planExerciseRepository.findById(planExerciseId);

        if (planExercise.isEmpty()) {
            return Optional.empty();
        }

        PlanExercise pe = planExercise.get();
        User user = findUserByUsername(username);

        // Check access to the workout plan
        if (!pe.getWorkoutPlan().getIsPublic() && !isOwner(pe.getWorkoutPlan(), user)) {
            throw new UnauthorizedOperationException("User does not have access to this workout plan");
        }

        return Optional.of(planExerciseMapper.toResponse(pe));
    }

    // =======================
    // PLAN EXERCISE MANAGEMENT (Owner only)
    // =======================

    @Transactional
    public PlanExerciseResponse addExerciseToWorkoutPlan(Long workoutPlanId, String username,
                                                         PlanExerciseRequest request) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(workoutPlanId);
        User user = findUserByUsername(username);
        Exercise exercise = findExerciseById(request.getExerciseId());

        // Validate ownership
        validateOwnership(workoutPlan, user);

        // Check if exercise already exists in workout plan
        if (planExerciseRepository.existsByWorkoutPlanAndExercise(workoutPlan, exercise)) {
            throw new IllegalArgumentException("Exercise already exists in this workout plan");
        }

        // Get next order number
        Integer nextOrder = planExerciseRepository.findMaxOrderInWorkout(workoutPlan)
                .map(maxOrder -> maxOrder + 1)
                .orElse(1);

        // Create plan exercise
        PlanExercise planExercise = new PlanExercise();
        planExercise.setWorkoutPlan(workoutPlan);
        planExercise.setExercise(exercise);
        planExercise.setOrderInWorkout(request.getOrderInWorkout() != null ?
                request.getOrderInWorkout() : nextOrder);
        planExercise.setCreatedByUserId(user.getId());
        planExercise.setIsUserCustomization(true);

        // Map other fields from request
        planExerciseMapper.mapRequestToEntity(request, planExercise);

        PlanExercise saved = planExerciseRepository.save(planExercise);

        log.info("Exercise {} added to workout plan {} by user {}",
                exercise.getExerciseName(), workoutPlan.getWorkoutName(), username);

        return planExerciseMapper.toResponse(saved);
    }

    @Transactional
    public PlanExerciseResponse updatePlanExercise(Long planExerciseId, String username,
                                                   PlanExerciseRequest request) {
        PlanExercise planExercise = findPlanExerciseById(planExerciseId);
        User user = findUserByUsername(username);

        // Validate ownership of the workout plan
        validateOwnership(planExercise.getWorkoutPlan(), user);

        // Update fields from request
        planExerciseMapper.mapRequestToEntity(request, planExercise);

        PlanExercise saved = planExerciseRepository.save(planExercise);

        log.info("Plan exercise {} updated by user {}", planExerciseId, username);

        return planExerciseMapper.toResponse(saved);
    }

    @Transactional
    public void removePlanExercise(Long planExerciseId, String username) {
        PlanExercise planExercise = findPlanExerciseById(planExerciseId);
        User user = findUserByUsername(username);

        // Validate ownership of the workout plan
        validateOwnership(planExercise.getWorkoutPlan(), user);

        // Store info before deletion
        String exerciseName = planExercise.getExercise().getExerciseName();
        String workoutName = planExercise.getWorkoutPlan().getWorkoutName();

        planExerciseRepository.delete(planExercise);

        // Reorder remaining exercises
        reorderExercisesAfterDeletion(planExercise.getWorkoutPlan(), planExercise.getOrderInWorkout());

        log.info("Exercise '{}' removed from workout plan '{}' by user {}",
                exerciseName, workoutName, username);
    }

    // =======================
    // EXERCISE ORDERING
    // =======================

    @Transactional
    public List<PlanExerciseResponse> reorderExercises(Long workoutPlanId, String username,
                                                       List<Long> planExerciseIds) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(workoutPlanId);
        User user = findUserByUsername(username);

        // Validate ownership
        validateOwnership(workoutPlan, user);

        // Update order for each exercise
        for (int i = 0; i < planExerciseIds.size(); i++) {
            Long planExerciseId = planExerciseIds.get(i);
            PlanExercise planExercise = findPlanExerciseById(planExerciseId);

            // Verify it belongs to this workout plan
            if (!planExercise.getWorkoutPlan().getId().equals(workoutPlanId)) {
                throw new IllegalArgumentException("Plan exercise does not belong to this workout plan");
            }

            planExercise.setOrderInWorkout(i + 1);
            planExerciseRepository.save(planExercise);
        }

        log.info("Exercises reordered in workout plan {} by user {}", workoutPlan.getWorkoutName(), username);

        // Return updated exercises
        List<PlanExercise> reorderedExercises = planExerciseRepository
                .findByWorkoutPlanOrderByOrderInWorkout(workoutPlan);
        return planExerciseMapper.toResponseList(reorderedExercises);
    }

    // =======================
    // SUPERSET MANAGEMENT
    // =======================

    @Transactional
    public List<PlanExerciseResponse> createSuperset(Long workoutPlanId, String username,
                                                     List<Long> planExerciseIds, String supersetGroup) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(workoutPlanId);
        User user = findUserByUsername(username);

        // Validate ownership
        validateOwnership(workoutPlan, user);

        // Update exercises to be part of superset
        for (Long planExerciseId : planExerciseIds) {
            PlanExercise planExercise = findPlanExerciseById(planExerciseId);

            // Verify it belongs to this workout plan
            if (!planExercise.getWorkoutPlan().getId().equals(workoutPlanId)) {
                throw new IllegalArgumentException("Plan exercise does not belong to this workout plan");
            }

            planExercise.setIsSuperset(true);
            planExercise.setSupersetGroup(supersetGroup);
            planExerciseRepository.save(planExercise);
        }

        log.info("Superset '{}' created in workout plan {} by user {}",
                supersetGroup, workoutPlan.getWorkoutName(), username);

        // Return superset exercises
        List<PlanExercise> supersetExercises = planExerciseRepository
                .findByWorkoutPlanAndSupersetGroupOrderByOrderInWorkout(workoutPlan, supersetGroup);
        return planExerciseMapper.toResponseList(supersetExercises);
    }

    @Transactional
    public void removeSuperset(Long workoutPlanId, String username, String supersetGroup) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(workoutPlanId);
        User user = findUserByUsername(username);

        // Validate ownership
        validateOwnership(workoutPlan, user);

        // Remove superset grouping
        List<PlanExercise> supersetExercises = planExerciseRepository
                .findByWorkoutPlanAndSupersetGroupOrderByOrderInWorkout(workoutPlan, supersetGroup);

        for (PlanExercise planExercise : supersetExercises) {
            planExercise.setIsSuperset(false);
            planExercise.setSupersetGroup(null);
            planExerciseRepository.save(planExercise);
        }

        log.info("Superset '{}' removed from workout plan {} by user {}",
                supersetGroup, workoutPlan.getWorkoutName(), username);
    }

    // =======================
    // PRESCRIPTION MANAGEMENT
    // =======================

    @Transactional
    public PlanExerciseResponse updateExercisePrescription(Long planExerciseId, String username,
                                                           ExercisePrescriptionRequest prescription) {
        PlanExercise planExercise = findPlanExerciseById(planExerciseId);
        User user = findUserByUsername(username);

        // Validate ownership
        validateOwnership(planExercise.getWorkoutPlan(), user);

        // Update prescription fields
        planExercise.setPrescribedSets(prescription.getSets());
        planExercise.setPrescribedReps(prescription.getReps());
        planExercise.setPrescribedWeightPercent(prescription.getWeightPercent());
        planExercise.setPrescribedRestSeconds(prescription.getRestSeconds());
        planExercise.setPrescribedTempo(prescription.getTempo());
        planExercise.setPrescribedRpe(prescription.getRpe());

        PlanExercise saved = planExerciseRepository.save(planExercise);

        log.info("Exercise prescription updated for plan exercise {} by user {}", planExerciseId, username);

        return planExerciseMapper.toResponse(saved);
    }

    // =======================
    // ANALYTICS & QUERIES
    // =======================

    public List<PlanExerciseResponse> getExercisesWithProgression(Long workoutPlanId, String username) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(workoutPlanId);
        User user = findUserByUsername(username);

        // Check access
        if (!workoutPlan.getIsPublic() && !isOwner(workoutPlan, user)) {
            throw new UnauthorizedOperationException("User does not have access to this workout plan");
        }

        List<PlanExercise> progressionExercises = planExerciseRepository
                .findByWorkoutPlanAndIsProgressionExercise(workoutPlan, true);

        return planExerciseMapper.toResponseList(progressionExercises);
    }

    public List<PlanExerciseResponse> getSupersetExercises(Long workoutPlanId, String supersetGroup) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(workoutPlanId);

        if (!workoutPlan.getIsPublic()) {
            throw new UnauthorizedOperationException("Workout plan is not public");
        }

        List<PlanExercise> supersetExercises = planExerciseRepository
                .findByWorkoutPlanAndSupersetGroupOrderByOrderInWorkout(workoutPlan, supersetGroup);

        return planExerciseMapper.toResponseList(supersetExercises);
    }

    public int getExerciseCount(Long workoutPlanId) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(workoutPlanId);
        Long count = planExerciseRepository.countByWorkoutPlan(workoutPlan);
        return count != null ? count.intValue() : 0;
    }

    // =======================
    // HELPER METHODS
    // =======================

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

    private WorkoutPlan findWorkoutPlanById(Long id) {
        return workoutPlanRepository.findById(id)
                .orElseThrow(() -> new WorkoutPlanNotFoundException(id));
    }

    private Exercise findExerciseById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ExerciseNotFoundException(id));
    }

    private PlanExercise findPlanExerciseById(Long id) {
        return planExerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan exercise not found: " + id));
    }

    private boolean isOwner(WorkoutPlan workoutPlan, User user) {
        return workoutPlan.getCreatedByUserId() != null &&
                workoutPlan.getCreatedByUserId().equals(user.getId());
    }

    private void validateOwnership(WorkoutPlan workoutPlan, User user) {
        if (!isOwner(workoutPlan, user)) {
            throw new UnauthorizedOperationException("User does not own this workout plan");
        }
    }

    private void reorderExercisesAfterDeletion(WorkoutPlan workoutPlan, int deletedOrder) {
        List<PlanExercise> exercisesToReorder = planExerciseRepository
                .findByWorkoutPlanAndOrderInWorkoutGreaterThanOrderByOrderInWorkout(workoutPlan, deletedOrder);

        for (PlanExercise exercise : exercisesToReorder) {
            exercise.setOrderInWorkout(exercise.getOrderInWorkout() - 1);
            planExerciseRepository.save(exercise);
        }
    }

    // Inner class for prescription requests
    public static class ExercisePrescriptionRequest {
        private Integer sets;
        private String reps;
        private Double weightPercent;
        private Integer restSeconds;
        private String tempo;
        private Integer rpe;

        // Getters and setters
        public Integer getSets() { return sets; }
        public void setSets(Integer sets) { this.sets = sets; }

        public String getReps() { return reps; }
        public void setReps(String reps) { this.reps = reps; }

        public Double getWeightPercent() { return weightPercent; }
        public void setWeightPercent(Double weightPercent) { this.weightPercent = weightPercent; }

        public Integer getRestSeconds() { return restSeconds; }
        public void setRestSeconds(Integer restSeconds) { this.restSeconds = restSeconds; }

        public String getTempo() { return tempo; }
        public void setTempo(String tempo) { this.tempo = tempo; }

        public Integer getRpe() { return rpe; }
        public void setRpe(Integer rpe) { this.rpe = rpe; }
    }
}