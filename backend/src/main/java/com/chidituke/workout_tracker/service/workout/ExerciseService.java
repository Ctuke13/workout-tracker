package com.chidituke.workout_tracker.service.workout;

import com.chidituke.workout_tracker.dto.response.exercise.ExerciseFiltersDTO;
import com.chidituke.workout_tracker.exceptions.exercise.ExerciseNotFoundException;
import com.chidituke.workout_tracker.exceptions.exercise.InvalidExerciseDataException;
import com.chidituke.workout_tracker.exceptions.user.ProfessionalVerificationException;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException;
import com.chidituke.workout_tracker.mapper.workout.ExerciseMapper;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.model.workout.UserExerciseRating;
import com.chidituke.workout_tracker.model.workout.UserExerciseHistory;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.repository.workout.ExerciseRepository;
import com.chidituke.workout_tracker.repository.workout.UserExerciseRatingRepository;
import com.chidituke.workout_tracker.repository.workout.UserExerciseHistoryRepository;
import com.chidituke.workout_tracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;
    private final ExerciseMapper exerciseMapper;

    // 🆕 NEW DEPENDENCIES
    private final UserExerciseRatingRepository ratingRepository;
    private final UserExerciseHistoryRepository historyRepository;

    // 🔍 EXERCISE DISCOVERY & SEARCH (NO SUBSCRIPTION FILTERING - ALL FREE!)

    public List<Exercise> findSuitableExercises(User user, List<String> availableEquipment,
                                                Exercise.DifficultyLevel maxDifficulty) {
        return exerciseRepository.findPublishedExercises().stream()
                .filter(exercise -> hasRequiredEquipment(exercise, availableEquipment))
                .filter(exercise -> exercise.getDifficultyLevel().ordinal() <= maxDifficulty.ordinal())
                .sorted(this::compareExercisesByRelevance)
                .collect(Collectors.toList());
    }

    public List<Exercise> findExercisesForWorkoutType(Exercise.ExerciseType type) {
        return exerciseRepository.findByExerciseTypeAndPublishedTrueOrderByExerciseNameAsc(type).stream()
                .sorted(Comparator.comparing(Exercise::getAverageRating).reversed()
                        .thenComparing(Exercise::getUsageCount).reversed())
                .collect(Collectors.toList());
    }

    public Page<Exercise> searchExercises(String searchTerm, List<String> muscleGroups,
                                          List<String> equipment, Exercise.DifficultyLevel difficulty,
                                          Pageable pageable) {
        String muscleGroup = (muscleGroups != null && !muscleGroups.isEmpty()) ? muscleGroups.get(0) : null;
        return exerciseRepository.searchExercisesWithFilters(searchTerm, muscleGroup, null, difficulty, pageable);
    }

    // 🆕 UPDATED - Now uses real user history for personalized recommendations
    public List<Exercise> findRecommendedExercises(User user, int limit) {

        if (user == null) {
            // Return popular exercises for anonymous users
            return exerciseRepository.findRecommendations(PageRequest.of(0, limit)).getContent();
        }

        List<Exercise> recentExercises = getRecentlyUsedExercises(user);
        List<String> preferredMuscleGroups = extractPreferredMuscleGroups(recentExercises);
        List<String> preferredTypes = extractPreferredExerciseTypes(user);

        if (preferredMuscleGroups.isEmpty() && preferredTypes.isEmpty()) {
            // New user - return popular exercises
            return exerciseRepository.findRecommendations(PageRequest.of(0, limit)).getContent();
        }

        // Get personalized recommendations
        List<Exercise> recommendations = new ArrayList<>();

        // Add exercises based on preferred muscle groups
        if (!preferredMuscleGroups.isEmpty()) {
            List<Exercise> muscleGroupRecs = exerciseRepository.findRecommendationsByMuscleGroups(
                    preferredMuscleGroups, PageRequest.of(0, limit / 2)).getContent();
            recommendations.addAll(muscleGroupRecs);
        }

        // Fill remaining slots with highly rated exercises user hasn't tried
        if (recommendations.size() < limit) {
            List<Exercise> untriedExercises = findUntriedHighlyRatedExercises(user, limit - recommendations.size());
            recommendations.addAll(untriedExercises);
        }

        // Remove duplicates and limit results
        return recommendations.stream()
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 🔧 FIXED - Now throws custom exception instead of returning null
    public Exercise findById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ExerciseNotFoundException(id));
    }

    @Cacheable(value = "popular-exercises", key = "#limit")
    public List<Exercise> findMostPopular(int limit) {
        return exerciseRepository.findMostPopular(PageRequest.of(0, limit)).getContent();
    }

    public Page<Exercise> findPublishedExercises(Pageable pageable) {
        return exerciseRepository.findByPublishedTrueOrderByExerciseNameAsc(pageable);
    }

    public ExerciseFiltersDTO getAvailableFilters() {
        return ExerciseFiltersDTO.createDefault();
    }

    public ExerciseFiltersDTO getAvailableFiltersWithCounts() {
        // Get actual counts from database
        List<Object[]> typeCounts = exerciseRepository.getExerciseTypeCounts();
        List<Object[]> difficultyCounts = exerciseRepository.getDifficultyLevelCounts();

        // Convert to DTOs with real counts
        List<ExerciseFiltersDTO.ExerciseTypeDTO> exerciseTypes = Arrays.stream(Exercise.ExerciseType.values())
                .map(type -> {
                    long count = typeCounts.stream()
                            .filter(row -> row[0].equals(type))
                            .mapToLong(row -> (Long) row[1])
                            .findFirst()
                            .orElse(0L);

                    return ExerciseFiltersDTO.ExerciseTypeDTO.builder()
                            .value(type.name())
                            .displayName(type.getDisplayName())
                            .count(count)
                            .build();
                })
                .toList();

        List<ExerciseFiltersDTO.DifficultyLevelDTO> difficultyLevels = Arrays.stream(Exercise.DifficultyLevel.values())
                .map(level -> {
                    long count = difficultyCounts.stream()
                            .filter(row -> row[0].equals(level))
                            .mapToLong(row -> (Long) row[1])
                            .findFirst()
                            .orElse(0L);

                    return ExerciseFiltersDTO.DifficultyLevelDTO.builder()
                            .value(level.name())
                            .description(level.getDescription())
                            .count(count)
                            .build();
                })
                .toList();

        return ExerciseFiltersDTO.builder()
                .exerciseTypes(exerciseTypes)
                .difficultyLevels(difficultyLevels)
                .equipment(List.of(
                        "dumbbells", "barbell", "yoga_mat", "resistance_bands",
                        "kettlebell", "jump_rope", "pull_up_bar", "medicine_ball",
                        "foam_roller", "exercise_bike", "treadmill", "elliptical"
                ))
                .muscleGroups(List.of(
                        "CHEST", "BACK", "SHOULDERS", "BICEPS", "TRICEPS", "FOREARMS",
                        "CORE", "ABS", "OBLIQUES", "QUADS", "HAMSTRINGS", "GLUTES",
                        "CALVES", "CARDIO", "FULL_BODY"
                ))
                .build();
    }

    @Transactional
    public void performBulkAction(List<Long> exerciseIds, String action, String reason, User admin) {
        validateAdminPermissions(admin);

        for (Long exerciseId : exerciseIds) {
            try {
                switch (action.toLowerCase()) {
                    case "approve" -> approveExercise(exerciseId, admin);
                    case "delete" -> deleteExercise(exerciseId, admin);
                    case "publish" -> publishExercise(exerciseId, admin);
                    case "unpublish" -> unpublishExercise(exerciseId, admin);
                    // 🔧 FIXED - Now uses custom exception
                    default -> throw new InvalidExerciseDataException("action", "Unknown action: " + action);
                }
            } catch (Exception e) {
                log.error("Failed to perform action {} on exercise {}: {}", action, exerciseId, e.getMessage());
                // Re-throw for proper error handling
                throw e;
            }
        }

        log.info("Bulk action {} performed on {} exercises by admin {}",
                action, exerciseIds.size(), admin.getId());
    }

    // 👥 PROFESSIONAL CONTENT MANAGEMENT

    @Transactional
    public Exercise createProfessionalExercise(User professional, ExerciseCreationRequest request) {
        // 🔧 FIXED - Added validation calls
        validateProfessionalCanCreateContent(professional);
        validateExerciseCreationRequest(request);

        Exercise exercise = new Exercise();

        // Use mapper to set fields from request
        exerciseMapper.mapRequestToEntity(request, exercise);

        // Professional content settings
        exercise.setCreatedByUserId(professional.getId());
        exercise.setCreatedByProfessional(true);
        exercise.setPublished(false); // Require admin approval for professional content

        Exercise savedExercise = exerciseRepository.save(exercise);

        log.info("Professional exercise created: {} by user {}",
                savedExercise.getExerciseName(), professional.getId());

        return savedExercise;
    }

    @Transactional
    public void approveExercise(Long exerciseId, User admin) {
        // 🔧 FIXED - Now uses ExerciseNotFoundException
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        validateAdminPermissions(admin);

        exercise.setPublished(true);
        exerciseRepository.save(exercise);

        log.info("Exercise approved: {} by admin {}", exercise.getExerciseName(), admin.getId());
    }

    @Transactional
    public void deleteExercise(Long exerciseId, User admin) {
        // 🔧 FIXED - Now uses ExerciseNotFoundException
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        validateAdminPermissions(admin);

        exerciseRepository.delete(exercise);
        log.info("Exercise deleted: {} by admin {}", exercise.getExerciseName(), admin.getId());
    }

    @Transactional
    public void publishExercise(Long exerciseId, User admin) {
        // 🔧 FIXED - Now uses ExerciseNotFoundException
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        validateAdminPermissions(admin);

        exercise.setPublished(true);
        exerciseRepository.save(exercise);
        log.info("Exercise published: {} by admin {}", exercise.getExerciseName(), admin.getId());
    }

    @Transactional
    public void unpublishExercise(Long exerciseId, User admin) {
        // 🔧 FIXED - Now uses ExerciseNotFoundException
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        validateAdminPermissions(admin);

        exercise.setPublished(false);
        exerciseRepository.save(exercise);
        log.info("Exercise unpublished: {} by admin {}", exercise.getExerciseName(), admin.getId());
    }

    // ===================================================================
    // 📊 RATING & ANALYTICS - 🆕 REAL IMPLEMENTATION
    // ===================================================================

    @Transactional
    public void rateExercise(Long exerciseId, User user, double rating, String comment, List<String> tags) {

        if (user == null) {
            throw new UserNotFoundException("User not found - authentication required");
        }

        if (rating < 0.0 || rating > 5.0) {
            throw new InvalidExerciseDataException("rating", "Rating must be between 0.0 and 5.0");
        }

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        // Check if user already rated this exercise
        if (hasUserRatedExercise(user, exercise)) {
            // 🔧 FIXED - Now uses InvalidExerciseDataException
            throw new InvalidExerciseDataException("rating", "User has already rated this exercise");
        }

        // Create and save user rating
        UserExerciseRating userRating = new UserExerciseRating(user, exercise, rating, comment, tags);
        ratingRepository.save(userRating);

        // Update exercise rating
        updateExerciseRating(exercise, rating);
        exerciseRepository.save(exercise);

        // Record user's rating in history
        recordExerciseInHistory(user, exercise, UserExerciseHistory.CONTEXT_RATE);

        log.info("Exercise rated: {} - {} stars by user {}",
                exercise.getExerciseName(), rating, user.getId());
    }

    // Overloaded method for backward compatibility (rating only)
    @Transactional
    public void rateExercise(Long exerciseId, User user, double rating) {
        rateExercise(exerciseId, user, rating, null, null);
    }

    public List<Object[]> getExerciseTypeCounts() {
        return exerciseRepository.countByExerciseType();
    }

    // 🆕 UPDATED - Real usage tracking with persistence
    @Transactional
    public void recordExerciseUsage(Long exerciseId, User user) {
        if (user == null) {
            log.debug("User is null - skipping usage recording for exercise {}", exerciseId);
            return; // Silently skip if user is null (for anonymous usage)
        }
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        exercise.incrementUsage();
        exerciseRepository.save(exercise);

        // Record user's exercise history
        recordExerciseInHistory(user, exercise, UserExerciseHistory.CONTEXT_VIEW);

        log.debug("Exercise usage recorded: {} by user {}", exercise.getExerciseName(), user.getId());
    }

    // 🆕 NEW - Workout usage tracking with duration and notes
    @Transactional
    public void recordWorkoutUsage(Long exerciseId, User user, Integer durationMinutes, String notes) {

        if (user == null) {
            throw new UserNotFoundException("User not found - authentication required");
        }

        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        exercise.incrementUsage();
        exerciseRepository.save(exercise);

        // Record detailed workout usage
        UserExerciseHistory history = new UserExerciseHistory(
                user, exercise, UserExerciseHistory.CONTEXT_WORKOUT, durationMinutes, notes);
        historyRepository.save(history);

        log.info("Workout usage recorded: {} - {} minutes by user {}",
                exercise.getExerciseName(), durationMinutes, user.getId());
    }

    public ExerciseService.ExerciseAnalytics getExerciseAnalytics(Long exerciseId) {
        // 🔧 FIXED - Now uses ExerciseNotFoundException
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        return ExerciseService.ExerciseAnalytics.builder()
                .exerciseId(exercise.getId())
                .exerciseName(exercise.getExerciseName())
                .totalUsage(exercise.getUsageCount())
                .averageRating(exercise.getAverageRating())
                .totalRatings(exercise.getTotalRatings())
                .popularityRank(calculatePopularityRank(exercise))
                .usageGrowthRate(calculateUsageGrowthRate(exercise))
                .isFromVerifiedSource(exercise.isFromVerifiedSource())
                .build();
    }

    // ===================================================================
    // 🆕 USER ANALYTICS AND INSIGHTS
    // ===================================================================

    public UserExerciseInsights getUserExerciseInsights(User user) {
        if (user == null) {
            // Return empty insights for null user
            return UserExerciseInsights.builder()
                    .totalExercisesTried(0)
                    .totalWorkouts(0)
                    .totalRatingsGiven(0)
                    .averageRatingGiven(0.0)
                    .preferredMuscleGroups(List.of())
                    .favoriteExercises(List.of())
                    .weeklyActivity(0)
                    .monthlyActivity(0)
                    .build();
        }

        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        // Get user statistics
        List<UserExerciseHistory> recentHistory = historyRepository.findUserRecentHistory(user.getId(), monthAgo);
        List<UserExerciseRating> userRatings = ratingRepository.findByUserId(user.getId());
        Long workoutCount = historyRepository.countUserWorkouts(user.getId(), monthAgo);

        // Calculate insights
        List<Object[]> preferredMuscleGroups = historyRepository.getUserPreferredMuscleGroups(user.getId(), monthAgo);
        List<Object[]> mostUsedExercises = historyRepository.getUserMostUsedExercises(user.getId());
        List<UserExerciseRating> favoriteExercises = ratingRepository.findUserHighRatedExercises(user.getId(), 4.0);

        return UserExerciseInsights.builder()
                .totalExercisesTried(getTriedExerciseIds(user).size())
                .totalWorkouts(workoutCount.intValue())
                .totalRatingsGiven(userRatings.size())
                .averageRatingGiven(userRatings.stream().mapToDouble(UserExerciseRating::getRating).average().orElse(0.0))
                .preferredMuscleGroups(preferredMuscleGroups.stream().limit(3).map(row -> (String) row[0]).collect(Collectors.toList()))
                .favoriteExercises(favoriteExercises.stream().limit(5).map(UserExerciseRating::getExercise).collect(Collectors.toList()))
                .weeklyActivity(historyRepository.countUsageSince(weekAgo).intValue())
                .monthlyActivity(recentHistory.size())
                .build();
    }

    // 🏋️ WORKOUT INTEGRATION

    public List<Exercise> buildWorkoutPlan(User user, WorkoutPlanRequest request) {
        // 🔧 FIXED - Added validation
        validateWorkoutPlanRequest(request);

        // Use optimized repository query instead of multiple calls
        List<Exercise> candidateExercises = exerciseRepository.findOptimizedForWorkoutPlan(
                request.getTargetMuscleGroups(),
                request.getMaxDifficulty(),
                PageRequest.of(0, 100) // Reasonable limit for candidate selection
        );

        // Filter by equipment availability (in memory - more efficient than SQL for this)
        List<Exercise> availableExercises = candidateExercises.stream()
                .filter(ex -> hasRequiredEquipment(ex, request.getAvailableEquipment()))
                .collect(Collectors.toList());

        // 🔧 FIXED - Added error check for empty results
        if (availableExercises.isEmpty()) {
            throw new InvalidExerciseDataException("availableEquipment",
                    "No exercises found matching your equipment and difficulty requirements");
        }

        // Distribute exercises across muscle groups
        Map<String, List<Exercise>> exercisesByMuscleGroup = availableExercises.stream()
                .flatMap(ex -> ex.getTargetMuscleGroups().stream()
                        .filter(request.getTargetMuscleGroups()::contains)
                        .map(mg -> Map.entry(mg, ex)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        List<Exercise> selectedExercises = new ArrayList<>();
        for (String muscleGroup : request.getTargetMuscleGroups()) {
            List<Exercise> muscleGroupExercises = exercisesByMuscleGroup.getOrDefault(muscleGroup, List.of())
                    .stream()
                    .sorted((e1, e2) -> Double.compare(
                            exerciseMapper.calculateRelevanceScore(e2),
                            exerciseMapper.calculateRelevanceScore(e1)
                    ))
                    .limit(request.getExercisesPerMuscleGroup())
                    .collect(Collectors.toList());

            selectedExercises.addAll(muscleGroupExercises);
        }

        adjustWorkoutForDuration(selectedExercises, request.getTargetDurationMinutes());
        return selectedExercises;
    }

    // ===================================================================
    // 🔧 HELPER METHODS - 🆕 REAL IMPLEMENTATIONS
    // ===================================================================

    // 🆕 REAL IMPLEMENTATION - Now uses database
    private boolean hasUserRatedExercise(User user, Exercise exercise) {
        if (user == null || exercise == null) {
            return false; // Safe default - user hasn't rated if user is null
        }
        return ratingRepository.existsByUserIdAndExerciseId(user.getId(), exercise.getId());
    }

    private void updateExerciseRating(Exercise exercise, double newRating) {
        Integer currentTotal = exercise.getTotalRatings();
        Double currentAverage = exercise.getAverageRating();

        if (currentTotal == null) currentTotal = 0;
        if (currentAverage == null) currentAverage = 0.0;

        double totalPoints = currentAverage * currentTotal;
        int newTotal = currentTotal + 1;
        double newAverage = (totalPoints + newRating) / newTotal;

        exercise.setTotalRatings(newTotal);
        exercise.setAverageRating(newAverage);
    }

    // 🆕 REAL IMPLEMENTATION - Now persists to database
    private void recordExerciseInHistory(User user, Exercise exercise, String context) {
        UserExerciseHistory history = new UserExerciseHistory(user, exercise, context);
        historyRepository.save(history);

        log.debug("Exercise history recorded: user={}, exercise={}, context={}",
                user.getId(), exercise.getId(), context);
    }

    // 🆕 REAL IMPLEMENTATION - Now queries database
    private List<Exercise> getRecentlyUsedExercises(User user) {
        if (user == null) {
            return List.of();
        }
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<UserExerciseHistory> recentHistory = historyRepository.findUserRecentHistory(user.getId(), since);

        return recentHistory.stream()
                .map(UserExerciseHistory::getExercise)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> extractPreferredMuscleGroups(List<Exercise> recentExercises) {
        if (recentExercises.isEmpty()) {
            return List.of();
        }

        return recentExercises.stream()
                .flatMap(ex -> ex.getTargetMuscleGroups().stream())
                .collect(Collectors.groupingBy(group -> group, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // 🆕 NEW - Extract exercise type preferences from user history
    private List<String> extractPreferredExerciseTypes(User user) {
        if (user == null) {
            return List.of();
        }
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<Object[]> typePreferences = historyRepository.getUserPreferredExerciseTypes(user.getId(), since);

        return typePreferences.stream()
                .limit(2) // Top 2 preferred types
                .map(row -> ((Exercise.ExerciseType) row[0]).name())
                .collect(Collectors.toList());
    }

    // 🆕 NEW - Find exercises user hasn't tried
    private List<Exercise> findUntriedHighlyRatedExercises(User user, int limit) {
        List<Exercise> highlyRated = exerciseRepository.findHighlyRated();
        Set<Long> triedExerciseIds = getTriedExerciseIds(user);

        return highlyRated.stream()
                .filter(exercise -> !triedExerciseIds.contains(exercise.getId()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 🆕 NEW - Get set of exercise IDs user has tried
    private Set<Long> getTriedExerciseIds(User user) {
        if (user == null) {
            return Set.of();
        }
        List<UserExerciseHistory> userHistory = historyRepository.findByUserId(user.getId());
        return userHistory.stream()
                .map(history -> history.getExercise().getId())
                .collect(Collectors.toSet());
    }

    // 🔧 FIXED - Added null check for equipment list
    private boolean hasRequiredEquipment(Exercise exercise, List<String> availableEquipment) {
        if (exercise.getEquipmentRequired() == null || exercise.getEquipmentRequired().isEmpty()) {
            return true;
        }

        if (availableEquipment == null || availableEquipment.isEmpty()) {
            return false; // Exercise requires equipment but none available
        }

        // Use Set for O(1) lookup instead of List.contains() which is O(n)
        Set<String> availableSet = new HashSet<>(availableEquipment);
        return exercise.getEquipmentRequired().stream()
                .allMatch(availableSet::contains);
    }

    private int compareExercisesByRelevance(Exercise e1, Exercise e2) {
        // Use mapper method for consistency
        double score1 = exerciseMapper.calculateRelevanceScore(e1);
        double score2 = exerciseMapper.calculateRelevanceScore(e2);
        return Double.compare(score2, score1); // Higher score first
    }

    public boolean isExerciseCreatedByUser(Long exerciseId, Long userId) {
        if (exerciseId == null || userId == null) {
            return false;
        }

        Exercise exercise = exerciseRepository.findById(exerciseId).orElse(null);
        if (exercise == null) {
            return false;
        }

        return userId.equals(exercise.getCreatedByUserId());
    }

    private void validateAdminPermissions(User admin) {
        if (admin == null) {
            throw new UserNotFoundException("Admin user not found");
        }

        if (!admin.hasRole("ADMIN")) {
            throw new UnauthorizedOperationException("Admin role required for this operation", true);
        }
    }

    // 🔧 NEW VALIDATION METHODS

    /**
     * Validates that a professional user can create content
     */
    private void validateProfessionalCanCreateContent(User professional) {
        if (professional == null) {
            throw new UserNotFoundException("User not found");
        }

        // Check if user has professional role
        if (!professional.hasRole("PROFESSIONAL") && !professional.hasRole("ADMIN")) {
            throw new ProfessionalVerificationException("Professional verification required for operation: create professional exercises");
        }

        // Additional professional verification checks could go here
        // e.g., check if professional profile is complete, verified, etc.
    }

    /**
     * Validates exercise creation request data
     */
    private void validateExerciseCreationRequest(ExerciseCreationRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidExerciseDataException("name", "Exercise name is required");
        }

        if (request.getExerciseType() == null) {
            throw new InvalidExerciseDataException("exerciseType", "Exercise type is required");
        }

        if (request.getDifficultyLevel() == null) {
            throw new InvalidExerciseDataException("difficultyLevel", "Difficulty level is required");
        }

        if (request.getTargetMuscleGroups() == null || request.getTargetMuscleGroups().isEmpty()) {
            throw new InvalidExerciseDataException("targetMuscleGroups", "At least one target muscle group is required");
        }
    }

    /**
     * Validates workout plan request data
     */
    private void validateWorkoutPlanRequest(WorkoutPlanRequest request) {
        if (request.getTargetMuscleGroups() == null || request.getTargetMuscleGroups().isEmpty()) {
            throw new InvalidExerciseDataException("targetMuscleGroups", "At least one target muscle group is required");
        }

        if (request.getMaxDifficulty() == null) {
            throw new InvalidExerciseDataException("maxDifficulty", "Maximum difficulty level is required");
        }

        if (request.getExercisesPerMuscleGroup() != null &&
                (request.getExercisesPerMuscleGroup() < 1 || request.getExercisesPerMuscleGroup() > 10)) {
            throw new InvalidExerciseDataException("exercisesPerMuscleGroup",
                    "Exercises per muscle group must be between 1 and 10");
        }
    }

    private int calculatePopularityRank(Exercise exercise) {
        // TODO: Implement complex calculation involving database queries
        return 1; // Placeholder
    }

    private double calculateUsageGrowthRate(Exercise exercise) {
        // TODO: Calculate usage growth over time with historical data
        return 0.0; // Placeholder
    }

    private void adjustWorkoutForDuration(List<Exercise> exercises, Integer targetDurationMinutes) {
        if (targetDurationMinutes == null) return;

        int currentDuration = exercises.stream()
                .mapToInt(ex -> ex.getEstimatedDurationMinutes() != null ? ex.getEstimatedDurationMinutes() : 0)
                .sum();

        // TODO: Logic to add or remove exercises to meet target duration
        log.debug("Adjusting workout duration from {} to {} minutes", currentDuration, targetDurationMinutes);
    }

    private double calculateRelevanceScore(Exercise exercise) {
        double score = 0.0;

        // Rating weight (40%)
        if (exercise.getAverageRating() != null && exercise.getTotalRatings() > 0) {
            score += exercise.getAverageRating() * 0.4;
        }

        // Popularity weight (30%)
        if (exercise.getUsageCount() != null) {
            score += Math.min(exercise.getUsageCount() / 1000.0, 1.0) * 0.3;
        }

        // Professional content weight (20%)
        if (exercise.isFromVerifiedSource()) {
            score += 0.2;
        }

        // Recency weight (10%)
        if (exercise.getCreatedAt() != null) {
            long daysSinceCreation = java.time.Duration.between(
                    exercise.getCreatedAt(), LocalDateTime.now()).toDays();
            if (daysSinceCreation < 30) {
                score += 0.1 * (30 - daysSinceCreation) / 30.0;
            }
        }

        return score;
    }

    // 📋 INNER CLASSES (Keep for now - can be moved to separate DTOs later)

    public static class ExerciseCreationRequest {
        private String name;
        private String description;
        private Exercise.ExerciseType exerciseType;
        private Exercise.DifficultyLevel difficultyLevel;
        private List<String> targetMuscleGroups;
        private List<String> equipmentRequired;
        private List<String> benefits;
        private List<String> tips;
        private String videoUrl;

        public ExerciseCreationRequest() {}

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Exercise.ExerciseType getExerciseType() { return exerciseType; }
        public void setExerciseType(Exercise.ExerciseType exerciseType) { this.exerciseType = exerciseType; }

        public Exercise.DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(Exercise.DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }

        public List<String> getTargetMuscleGroups() { return targetMuscleGroups; }
        public void setTargetMuscleGroups(List<String> targetMuscleGroups) { this.targetMuscleGroups = targetMuscleGroups; }

        public List<String> getEquipmentRequired() { return equipmentRequired; }
        public void setEquipmentRequired(List<String> equipmentRequired) { this.equipmentRequired = equipmentRequired; }

        public List<String> getBenefits() { return benefits; }
        public void setBenefits(List<String> benefits) { this.benefits = benefits; }

        public List<String> getTips() { return tips; }
        public void setTips(List<String> tips) { this.tips = tips; }

        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    }

    public static class WorkoutPlanRequest {
        private List<String> targetMuscleGroups;
        private List<String> availableEquipment;
        private Exercise.DifficultyLevel maxDifficulty;
        private Integer targetDurationMinutes;
        private Integer exercisesPerMuscleGroup = 2;

        public WorkoutPlanRequest() {}

        public List<String> getTargetMuscleGroups() { return targetMuscleGroups; }
        public void setTargetMuscleGroups(List<String> targetMuscleGroups) { this.targetMuscleGroups = targetMuscleGroups; }

        public List<String> getAvailableEquipment() { return availableEquipment; }
        public void setAvailableEquipment(List<String> availableEquipment) { this.availableEquipment = availableEquipment; }

        public Exercise.DifficultyLevel getMaxDifficulty() { return maxDifficulty; }
        public void setMaxDifficulty(Exercise.DifficultyLevel maxDifficulty) { this.maxDifficulty = maxDifficulty; }

        public Integer getTargetDurationMinutes() { return targetDurationMinutes; }
        public void setTargetDurationMinutes(Integer targetDurationMinutes) { this.targetDurationMinutes = targetDurationMinutes; }

        public Integer getExercisesPerMuscleGroup() { return exercisesPerMuscleGroup; }
        public void setExercisesPerMuscleGroup(Integer exercisesPerMuscleGroup) { this.exercisesPerMuscleGroup = exercisesPerMuscleGroup; }
    }

    public static class ExerciseAnalytics {
        private Long exerciseId;
        private String exerciseName;
        private Integer totalUsage;
        private Double averageRating;
        private Integer totalRatings;
        private Integer popularityRank;
        private Double usageGrowthRate;
        private Boolean isFromVerifiedSource;

        private ExerciseAnalytics(Builder builder) {
            this.exerciseId = builder.exerciseId;
            this.exerciseName = builder.exerciseName;
            this.totalUsage = builder.totalUsage;
            this.averageRating = builder.averageRating;
            this.totalRatings = builder.totalRatings;
            this.popularityRank = builder.popularityRank;
            this.usageGrowthRate = builder.usageGrowthRate;
            this.isFromVerifiedSource = builder.isFromVerifiedSource;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Long exerciseId;
            private String exerciseName;
            private Integer totalUsage;
            private Double averageRating;
            private Integer totalRatings;
            private Integer popularityRank;
            private Double usageGrowthRate;
            private Boolean isFromVerifiedSource;

            public Builder exerciseId(Long exerciseId) { this.exerciseId = exerciseId; return this; }
            public Builder exerciseName(String exerciseName) { this.exerciseName = exerciseName; return this; }
            public Builder totalUsage(Integer totalUsage) { this.totalUsage = totalUsage; return this; }
            public Builder averageRating(Double averageRating) { this.averageRating = averageRating; return this; }
            public Builder totalRatings(Integer totalRatings) { this.totalRatings = totalRatings; return this; }
            public Builder popularityRank(Integer popularityRank) { this.popularityRank = popularityRank; return this; }
            public Builder usageGrowthRate(Double usageGrowthRate) { this.usageGrowthRate = usageGrowthRate; return this; }
            public Builder isFromVerifiedSource(Boolean isFromVerifiedSource) { this.isFromVerifiedSource = isFromVerifiedSource; return this; }

            public ExerciseAnalytics build() {
                return new ExerciseAnalytics(this);
            }
        }

        // Getters
        public Long getExerciseId() { return exerciseId; }
        public String getExerciseName() { return exerciseName; }
        public Integer getTotalUsage() { return totalUsage; }
        public Double getAverageRating() { return averageRating; }
        public Integer getTotalRatings() { return totalRatings; }
        public Integer getPopularityRank() { return popularityRank; }
        public Double getUsageGrowthRate() { return usageGrowthRate; }
        public Boolean getIsFromVerifiedSource() { return isFromVerifiedSource; }
    }

    // 🆕 NEW - User Exercise Insights DTO
    @lombok.Data
    @lombok.Builder
    public static class UserExerciseInsights {
        private Integer totalExercisesTried;
        private Integer totalWorkouts;
        private Integer totalRatingsGiven;
        private Double averageRatingGiven;
        private List<String> preferredMuscleGroups;
        private List<Exercise> favoriteExercises;
        private Integer weeklyActivity;
        private Integer monthlyActivity;
    }
}