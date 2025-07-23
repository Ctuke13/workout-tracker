package com.chidituke.workout_tracker.service.workout;

import com.chidituke.workout_tracker.dto.request.workout_plan.WorkoutTemplateRequestDTO;
import com.chidituke.workout_tracker.dto.response.workout_plan.WorkoutPlanResponse;
import com.chidituke.workout_tracker.dto.response.workout_plan.WorkoutPlanAnalyticsResponse;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.exceptions.workout_plan.WorkoutPlanNotFoundException;
import com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException;
import com.chidituke.workout_tracker.mapper.workout.WorkoutPlanMapper;
import com.chidituke.workout_tracker.model.workout.PlanExercise;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan.DifficultyLevel;
import com.chidituke.workout_tracker.model.workout.WorkoutPlan.WorkoutType;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import com.chidituke.workout_tracker.repository.workout.PlanExerciseRepository;
import com.chidituke.workout_tracker.repository.workout.WorkoutPlanRepository;
import com.chidituke.workout_tracker.repository.workout.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkoutPlanService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final UserRepository userRepository;
    private final PlanExerciseRepository planExerciseRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutPlanMapper workoutPlanMapper;

    // =======================
    // PUBLIC WORKOUT PLAN DISCOVERY
    // =======================

    @Cacheable(value = "public-workout-plans", unless = "#result.isEmpty()")
    public List<WorkoutPlanResponse> getAllPublicWorkoutPlans() {
        List<WorkoutPlan> workoutPlans = workoutPlanRepository.findByIsPublicTrueOrderByTimesUsedDesc();
        return workoutPlanMapper.toResponseList(workoutPlans);
    }

    public Optional<WorkoutPlanResponse> getWorkoutPlanById(Long id) {
        return workoutPlanRepository.findById(id)
                .filter(plan -> plan.getIsPublic()) // Only return public plans for general access
                .map(workoutPlanMapper::toResponse);
    }

    public Optional<WorkoutPlanResponse> getWorkoutPlanById(Long id, String username) {
        Optional<WorkoutPlan> workoutPlan = workoutPlanRepository.findById(id);

        if (workoutPlan.isEmpty()) {
            return Optional.empty();
        }

        WorkoutPlan plan = workoutPlan.get();

        // Allow access if public OR if user owns it
        if (plan.getIsPublic() || isOwner(plan, username)) {
            return Optional.of(workoutPlanMapper.toResponse(plan));
        }

        return Optional.empty();
    }

    public List<WorkoutPlanResponse> getWorkoutPlansByCategory(String category) {
        List<WorkoutPlan> workoutPlans = workoutPlanRepository
                .findByWorkoutCategoryIgnoreCaseAndIsPublicTrue(category);
        return workoutPlanMapper.toResponseList(workoutPlans);
    }

    public List<WorkoutPlanResponse> getWorkoutPlansByType(WorkoutType workoutType) {
        List<WorkoutPlan> workoutPlans = workoutPlanRepository
                .findByWorkoutTypeAndIsPublicTrue(workoutType);
        return workoutPlanMapper.toResponseList(workoutPlans);
    }

    public List<WorkoutPlanResponse> getWorkoutPlansByDifficulty(DifficultyLevel difficultyLevel) {
        List<WorkoutPlan> workoutPlans = workoutPlanRepository
                .findByDifficultyLevelAndIsPublicTrue(difficultyLevel);
        return workoutPlanMapper.toResponseList(workoutPlans);
    }

    // =======================
    // SEARCH & FILTERING
    // =======================

    public Page<WorkoutPlanResponse> searchWorkoutPlans(String searchTerm, Pageable pageable) {
        Page<WorkoutPlan> workoutPlans = workoutPlanRepository
                .findByWorkoutNameContainingIgnoreCaseAndIsPublicTrue(searchTerm, pageable);
        return workoutPlans.map(workoutPlanMapper::toResponse);
    }

    public Page<WorkoutPlanResponse> searchWorkoutPlansWithFilters(
            String category, WorkoutType workoutType, DifficultyLevel difficultyLevel,
            String equipment, String muscleGroup, Pageable pageable) {

        Page<WorkoutPlan> workoutPlans = workoutPlanRepository.findWithFilters(
                category, workoutType, difficultyLevel, equipment, muscleGroup, pageable);
        return workoutPlans.map(workoutPlanMapper::toResponse);
    }

    public List<WorkoutPlanResponse> getWorkoutPlansByEquipment(String equipment) {
        List<WorkoutPlan> workoutPlans = workoutPlanRepository.findByEquipmentNeeded(equipment);
        return workoutPlanMapper.toResponseList(workoutPlans);
    }

    public List<WorkoutPlanResponse> getWorkoutPlansByMuscleGroup(String muscleGroup) {
        List<WorkoutPlan> workoutPlans = workoutPlanRepository.findByTargetMuscleGroups(muscleGroup);
        return workoutPlanMapper.toResponseList(workoutPlans);
    }

    // =======================
    // SUBSCRIPTION-AWARE QUERIES
    // =======================

    public List<WorkoutPlanResponse> getAccessibleWorkoutPlans(String username) {
        User user = findUserByUsername(username);
        String userTier = user.getSubscriptionTier().name();

        List<WorkoutPlan> workoutPlans = workoutPlanRepository.findAccessibleWorkouts(userTier);
        return workoutPlanMapper.toResponseList(workoutPlans);
    }

    // =======================
    // POPULAR & RECOMMENDED
    // =======================

    @Cacheable(value = "popular-workout-plans", key = "#limit")
    public List<WorkoutPlanResponse> getMostPopularWorkoutPlans(int limit) {
        List<WorkoutPlan> workoutPlans = workoutPlanRepository.findTop10ByIsPublicTrueOrderByTimesUsedDesc();
        return workoutPlans.stream()
                .limit(limit)
                .map(workoutPlanMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "highly-rated-workout-plans", key = "#minRating")
    public List<WorkoutPlanResponse> getHighlyRatedWorkoutPlans(Double minRating) {
        List<WorkoutPlan> workoutPlans = workoutPlanRepository.findHighlyRatedWorkouts(minRating);
        return workoutPlanMapper.toResponseList(workoutPlans);
    }

    public List<WorkoutPlanResponse> getTrendingWorkoutPlans(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<WorkoutPlan> workoutPlans = workoutPlanRepository.findTrendingWorkouts(pageable);
        return workoutPlanMapper.toResponseList(workoutPlans);
    }

    // =======================
    // USER WORKOUT PLAN MANAGEMENT
    // =======================

    public List<WorkoutPlanResponse> getUserCreatedWorkoutPlans(String username) {
        User user = findUserByUsername(username);
        List<WorkoutPlan> workoutPlans = workoutPlanRepository
                .findByCreatedByUserIdOrderByCreatedAtDesc(user.getId());
        return workoutPlanMapper.toResponseList(workoutPlans);
    }

    public List<WorkoutPlanResponse> getUserPrivateWorkoutPlans(String username) {
        User user = findUserByUsername(username);
        List<WorkoutPlan> workoutPlans = workoutPlanRepository
                .findByCreatedByUserIdAndIsPublicFalse(user.getId());
        return workoutPlanMapper.toResponseList(workoutPlans);
    }

    // =======================
    // WORKOUT PLAN CRUD
    // =======================

    @Transactional
    @CacheEvict(value = {"public-workout-plans", "popular-workout-plans"}, allEntries = true)
    public WorkoutPlanResponse createWorkoutPlan(String username, WorkoutTemplateRequestDTO request) {
        User user = findUserByUsername(username);

        WorkoutPlan workoutPlan = new WorkoutPlan();
        workoutPlan.setCreatedByUserId(user.getId());

        // Map request to entity
        workoutPlanMapper.mapRequestToEntity(request, workoutPlan);

        WorkoutPlan saved = workoutPlanRepository.save(workoutPlan);

        log.info("Workout plan created: '{}' by user {}", saved.getWorkoutName(), username);

        return workoutPlanMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"public-workout-plans", "popular-workout-plans"}, allEntries = true)
    public WorkoutPlanResponse updateWorkoutPlan(Long id, String username, WorkoutTemplateRequestDTO request) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(id);
        validateOwnership(workoutPlan, username);

        // Update fields from request
        workoutPlanMapper.mapRequestToEntity(request, workoutPlan);

        WorkoutPlan saved = workoutPlanRepository.save(workoutPlan);

        log.info("Workout plan updated: {} by user {}", id, username);

        return workoutPlanMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"public-workout-plans", "popular-workout-plans"}, allEntries = true)
    public void deleteWorkoutPlan(Long id, String username) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(id);
        validateOwnership(workoutPlan, username);

        // Check if workout plan is being used in any sessions
        long sessionCount = workoutSessionRepository.countByWorkoutPlan(workoutPlan);
        if (sessionCount > 0) {
            throw new IllegalStateException(
                    "Cannot delete workout plan that has been used in workout sessions");
        }

        // Delete associated plan exercises first
        planExerciseRepository.deleteByWorkoutPlan(workoutPlan);

        workoutPlanRepository.delete(workoutPlan);

        log.info("Workout plan deleted: {} by user {}", id, username);
    }

    @Transactional
    public WorkoutPlanResponse duplicateWorkoutPlan(Long id, String username, String newName) {
        WorkoutPlan originalPlan = workoutPlanRepository.findById(id)
                .orElseThrow(() -> new WorkoutPlanNotFoundException(id));

        // Verify access to original plan
        if (!originalPlan.getIsPublic() && !isOwner(originalPlan, username)) {
            throw new UnauthorizedOperationException("Cannot duplicate this workout plan");
        }

        User user = findUserByUsername(username);

        // Create duplicate
        WorkoutPlan duplicatedPlan = new WorkoutPlan();
        copyWorkoutPlanProperties(originalPlan, duplicatedPlan);
        duplicatedPlan.setWorkoutName(newName != null ? newName : originalPlan.getWorkoutName() + " (Copy)");
        duplicatedPlan.setCreatedByUserId(user.getId());
        duplicatedPlan.setIsPublic(false); // User copies are private by default

        WorkoutPlan saved = workoutPlanRepository.save(duplicatedPlan);

        // Copy plan exercises
        List<PlanExercise> originalExercises = planExerciseRepository
                .findByWorkoutPlanOrderByOrderInWorkout(originalPlan);

        for (PlanExercise originalExercise : originalExercises) {
            PlanExercise duplicatedExercise = new PlanExercise();
            copyPlanExerciseProperties(originalExercise, duplicatedExercise);
            duplicatedExercise.setWorkoutPlan(saved);
            duplicatedExercise.setCreatedByUserId(user.getId());
            duplicatedExercise.setIsUserCustomization(true);
            planExerciseRepository.save(duplicatedExercise);
        }

        log.info("Workout plan duplicated: {} -> {} by user {}", id, saved.getId(), username);

        return workoutPlanMapper.toResponse(saved);
    }

    // =======================
    // WORKOUT PLAN USAGE TRACKING
    // =======================

    @Transactional
    public void recordWorkoutPlanUsage(Long id) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(id);
        workoutPlan.setTimesUsed(workoutPlan.getTimesUsed() + 1);
        workoutPlanRepository.save(workoutPlan);

        log.debug("Usage recorded for workout plan: {}", id);
    }

    // =======================
    // ANALYTICS
    // =======================

    public WorkoutPlanAnalyticsResponse getWorkoutPlanAnalytics(Long id, String username) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(id);

        // Only allow analytics for owners or public plans
        if (!workoutPlan.getIsPublic() && !isOwner(workoutPlan, username)) {
            throw new UnauthorizedOperationException("Cannot access analytics for this workout plan");
        }

        // Get usage statistics
        long totalCompletions = workoutSessionRepository.countByWorkoutPlan(workoutPlan);
        int exerciseCount = (int) planExerciseRepository.countByWorkoutPlan(workoutPlan);

        return WorkoutPlanAnalyticsResponse.builder()
                .workoutPlanId(id)
                .workoutPlanName(workoutPlan.getWorkoutName())
                .timesUsed(workoutPlan.getTimesUsed())
                .totalCompletions(totalCompletions)
                .exerciseCount(exerciseCount)
                .averageRating(workoutPlan.getAverageRating())
                .difficultyLevel(workoutPlan.getDifficultyLevel().name())
                .category(workoutPlan.getWorkoutCategory())
                .build();
    }

    public Map<String, Object> getWorkoutPlanStatistics() {
        List<Object[]> categoryStats = workoutPlanRepository.countByCategory();
        List<Object[]> difficultyStats = workoutPlanRepository.countByDifficultyLevel();

        return Map.of(
                "categoryCounts", categoryStats,
                "difficultyCounts", difficultyStats,
                "totalPublicPlans", workoutPlanRepository.countByIsPublicTrue()
        );
    }

    // =======================
    // PLAN EXERCISES MANAGEMENT
    // =======================

    public List<PlanExercise> getWorkoutPlanExercises(Long workoutPlanId) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(workoutPlanId);
        return planExerciseRepository.findByWorkoutPlanOrderByOrderInWorkout(workoutPlan);
    }

    public List<PlanExercise> getAccessibleWorkoutPlanExercises(Long workoutPlanId, String username) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(workoutPlanId);
        User user = findUserByUsername(username);

        return planExerciseRepository.findAccessibleExercises(workoutPlan, user.getSubscriptionTier().name());
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

    private boolean isOwner(WorkoutPlan workoutPlan, String username) {
        if (workoutPlan.getCreatedByUserId() == null) {
            return false;
        }

        User user = findUserByUsername(username);
        return workoutPlan.getCreatedByUserId().equals(user.getId());
    }

    private void validateOwnership(WorkoutPlan workoutPlan, String username) {
        if (!isOwner(workoutPlan, username)) {
            throw new UnauthorizedOperationException("User does not own this workout plan");
        }
    }

    private void copyWorkoutPlanProperties(WorkoutPlan source, WorkoutPlan target) {
        target.setWorkoutName(source.getWorkoutName());
        target.setWorkoutDescription(source.getWorkoutDescription());
        target.setWorkoutCategory(source.getWorkoutCategory());
        target.setWorkoutImageUrl(source.getWorkoutImageUrl());
        target.setCardio(source.isCardio());
        target.setWorkoutType(source.getWorkoutType());
        target.setEstimatedDurationMinutes(source.getEstimatedDurationMinutes());
        target.setDifficultyLevel(source.getDifficultyLevel());
        target.setTargetMuscleGroups(source.getTargetMuscleGroups());
        target.setEquipmentNeeded(source.getEquipmentNeeded());
        target.setSubscriptionTierRequired(source.getSubscriptionTierRequired());
    }

    private void copyPlanExerciseProperties(PlanExercise source, PlanExercise target) {
        target.setExercise(source.getExercise());
        target.setOrderInWorkout(source.getOrderInWorkout());
        target.setPrescribedSets(source.getPrescribedSets());
        target.setPrescribedReps(source.getPrescribedReps());
        target.setPrescribedWeightPercent(source.getPrescribedWeightPercent());
        target.setPrescribedRestSeconds(source.getPrescribedRestSeconds());
        target.setPrescribedTempo(source.getPrescribedTempo());
        target.setPrescribedRpe(source.getPrescribedRpe());
        target.setInstructions(source.getInstructions());
        target.setCoachingCues(source.getCoachingCues());
        target.setModificationNotes(source.getModificationNotes());
        target.setAlternativeExercise(source.getAlternativeExercise());
        target.setIsProgressionExercise(source.getIsProgressionExercise());
        target.setProgressionGoal(source.getProgressionGoal());
        target.setSubscriptionTierRequired(source.getSubscriptionTierRequired());
        target.setIsOptional(source.getIsOptional());
        target.setIsSuperset(source.getIsSuperset());
        target.setSupersetGroup(source.getSupersetGroup());
        target.setEquipmentAlternatives(source.getEquipmentAlternatives());
    }

    public boolean workoutPlanExists(Long id) {
        return workoutPlanRepository.existsById(id);
    }

    @Transactional
    @CacheEvict(value = {"public-workout-plans", "popular-workout-plans"}, allEntries = true)
    public void makeWorkoutPlanPublic(Long id, String username) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(id);
        validateOwnership(workoutPlan, username);

        workoutPlan.setIsPublic(true);
        workoutPlanRepository.save(workoutPlan);

        log.info("Workout plan made public: {} by user {}", id, username);
    }

    @Transactional
    @CacheEvict(value = {"public-workout-plans", "popular-workout-plans"}, allEntries = true)
    public void makeWorkoutPlanPrivate(Long id, String username) {
        WorkoutPlan workoutPlan = findWorkoutPlanById(id);
        validateOwnership(workoutPlan, username);

        workoutPlan.setIsPublic(false);
        workoutPlanRepository.save(workoutPlan);

        log.info("Workout plan made private: {} by user {}", id, username);
    }
}