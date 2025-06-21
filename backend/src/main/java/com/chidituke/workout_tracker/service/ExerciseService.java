package com.chidituke.workout_tracker.service;

import com.chidituke.workout_tracker.dto.response.exercise.ExerciseFiltersDTO;
import com.chidituke.workout_tracker.exceptions.exercise.ExerciseNotFoundException;
import com.chidituke.workout_tracker.exceptions.exercise.InvalidExerciseDataException;
import com.chidituke.workout_tracker.exceptions.user.ProfessionalVerificationException;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException;
import com.chidituke.workout_tracker.mapper.ExerciseMapper;
import com.chidituke.workout_tracker.model.Exercise;
import com.chidituke.workout_tracker.model.User;
import com.chidituke.workout_tracker.model.ProfessionalProfile;
import com.chidituke.workout_tracker.repository.ExerciseRepository;
import com.chidituke.workout_tracker.repository.UserRepository;
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

    public List<Exercise> findRecommendedExercises(User user, int limit) {
        List<Exercise> recentExercises = getRecentlyUsedExercises(user);
        List<String> preferredMuscleGroups = extractPreferredMuscleGroups(recentExercises);

        if (preferredMuscleGroups.isEmpty()) {
            return exerciseRepository.findRecommendations(PageRequest.of(0, limit)).getContent();
        } else {
            return exerciseRepository.findRecommendationsByMuscleGroups(
                    preferredMuscleGroups, PageRequest.of(0, limit)).getContent();
        }
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

    // 📊 RATING & ANALYTICS

    @Transactional
    public void rateExercise(Long exerciseId, User user, double rating) {
        // 🔧 FIXED - Now uses InvalidExerciseDataException
        if (rating < 0.0 || rating > 5.0) {
            throw new InvalidExerciseDataException("rating", "Rating must be between 0.0 and 5.0");
        }

        // 🔧 FIXED - Now uses ExerciseNotFoundException
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        // Check if user already rated this exercise
        if (hasUserRatedExercise(user, exercise)) {
            // 🔧 FIXED - Now uses InvalidExerciseDataException
            throw new InvalidExerciseDataException("rating", "User has already rated this exercise");
        }

        // Update exercise rating
        updateExerciseRating(exercise, rating);

        // Record user's rating (implement UserExerciseRating entity later)
        recordUserRating(user, exercise, rating);

        exerciseRepository.save(exercise);

        log.info("Exercise rated: {} - {} stars by user {}",
                exercise.getExerciseName(), rating, user.getId());
    }

    public List<Object[]> getExerciseTypeCounts() {
        return exerciseRepository.countByExerciseType();
    }

    @Transactional
    public void recordExerciseUsage(Long exerciseId, User user) {
        // 🔧 FIXED - Now uses ExerciseNotFoundException
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId));

        exercise.incrementUsage();
        exerciseRepository.save(exercise);

        // Record user's exercise history (implement UserExerciseHistory entity later)
        recordExerciseInHistory(user, exercise);

        log.debug("Exercise usage recorded: {} by user {}", exercise.getExerciseName(), user.getId());
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

    // 🔧 HELPER METHODS

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

    // 🔧 FIXED - Now uses proper custom exceptions
    private void validateAdminPermissions(User admin) {
        if (admin == null) {
            throw new UserNotFoundException("Admin user not found");
        }

        if (!admin.hasRole("ADMIN")) {
            throw new UnauthorizedOperationException("Admin role required for this operation");
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
            throw new ProfessionalVerificationException("create professional exercises");
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

    private boolean hasUserRatedExercise(User user, Exercise exercise) {
        // TODO: Implement UserExerciseRating entity and query
        return false;
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

    private void recordUserRating(User user, Exercise exercise, double rating) {
        // TODO: Implement UserExerciseRating entity recording
        log.debug("Recording user rating: user={}, exercise={}, rating={}",
                user.getId(), exercise.getId(), rating);
    }

    private void recordExerciseInHistory(User user, Exercise exercise) {
        // TODO: Implement UserExerciseHistory entity recording
        log.debug("Recording exercise usage: user={}, exercise={}", user.getId(), exercise.getId());
    }

    private List<Exercise> getRecentlyUsedExercises(User user) {
        // TODO: Query user's exercise history when implemented
        return List.of();
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
}