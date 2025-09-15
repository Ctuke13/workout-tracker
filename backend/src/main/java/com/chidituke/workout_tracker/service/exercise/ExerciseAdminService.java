package com.chidituke.workout_tracker.service.exercise;

import com.chidituke.workout_tracker.dto.request.exercise.ExerciseCreateRequestDTO;
import com.chidituke.workout_tracker.exceptions.common.UnauthorizedOperationException;
import com.chidituke.workout_tracker.exceptions.exercise.InvalidExerciseDataException;
import com.chidituke.workout_tracker.exceptions.user.ProfessionalVerificationException;
import com.chidituke.workout_tracker.exceptions.user.UserNotFoundException;
import com.chidituke.workout_tracker.mapper.workout.ExerciseMapper;
import com.chidituke.workout_tracker.model.user.User;
import com.chidituke.workout_tracker.model.workout.Exercise;
import com.chidituke.workout_tracker.repository.workout.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseAdminService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseQueryService exerciseQueryService;
    private final ExerciseMapper exerciseMapper;

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

    @Transactional
    public Exercise createProfessionalExercise(User professional, ExerciseCreateRequestDTO createRequest) {
        validateProfessionalCanCreateContent(professional);
        validateExerciseCreateRequestDTO(createRequest); // 🔧 FIXED: Updated method name

        Exercise exercise = new Exercise();

        // Use mapper instead of manual mapping
        exerciseMapper.mapRequestToEntity(createRequest, exercise);

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
        Exercise exercise = exerciseQueryService.findById(exerciseId);

        validateAdminPermissions(admin);

        exercise.setPublished(true);
        exerciseRepository.save(exercise);

        log.info("Exercise approved: {} by admin {}", exercise.getExerciseName(), admin.getId());
    }

    @Transactional
    public void deleteExercise(Long exerciseId, User admin) {
        Exercise exercise = exerciseQueryService.findById(exerciseId);

        validateAdminPermissions(admin);

        exerciseRepository.delete(exercise);
        log.info("Exercise deleted: {} by admin {}", exercise.getExerciseName(), admin.getId());
    }

    @Transactional
    public void publishExercise(Long exerciseId, User admin) {
        // 🔧 FIXED - Now uses ExerciseNotFoundException
        Exercise exercise = exerciseQueryService.findById(exerciseId);

        validateAdminPermissions(admin);

        exercise.setPublished(true);
        exerciseRepository.save(exercise);
        log.info("Exercise published: {} by admin {}", exercise.getExerciseName(), admin.getId());
    }

    @Transactional
    public void unpublishExercise(Long exerciseId, User admin) {
        // 🔧 FIXED - Now uses ExerciseNotFoundException
        Exercise exercise = exerciseQueryService.findById(exerciseId);

        validateAdminPermissions(admin);

        exercise.setPublished(false);
        exerciseRepository.save(exercise);
        log.info("Exercise unpublished: {} by admin {}", exercise.getExerciseName(), admin.getId());
    }

    public ExerciseAnalytics getExerciseAnalytics(Long exerciseId) {
        Exercise exercise = exerciseQueryService.findById(exerciseId);

        return ExerciseAnalytics.builder()
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

    /**
     * Find all published exercises with professional content prioritized
     */
    public List<Exercise> findExercisesWithProfessionalFirst() {
        return exerciseRepository.findPublishedExercisesOrderByProfessionalFirst();
    }

    /**
     * Find exercises by professional status
     */
    public List<Exercise> findExercisesByProfessionalStatus(boolean isProfessional) {
        if (isProfessional) {
            return exerciseQueryService.findProfessionalExercises(); // Changed line
        } else {
            return exerciseRepository.findPublishedExercises().stream()
                    .filter(exercise -> !exercise.isCreatedByProfessional())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Validates exercise creation DTO data
     */
    private void validateExerciseCreateRequestDTO(ExerciseCreateRequestDTO request) {
        // Additional validation beyond the DTO annotations if needed
        if (request.getTargetMuscleGroups() == null || request.getTargetMuscleGroups().isEmpty()) {
            throw new InvalidExerciseDataException("targetMuscleGroups", "At least one target muscle group is required");
        }

        // Additional business logic validation can go here
        if (request.getEstimatedDurationMinutes() != null && request.getEstimatedDurationMinutes() > 480) {
            throw new InvalidExerciseDataException("estimatedDurationMinutes", "Duration cannot exceed 8 hours");
        }
    }

    private void validateAdminPermissions(User admin) {
        if (admin == null) {
            throw new UserNotFoundException("Admin user not found");
        }

        if (!admin.hasRole("ADMIN")) {
            throw new UnauthorizedOperationException("Admin role required for this operation", true);
        }
    }

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

    private int calculatePopularityRank(Exercise exercise) {
        // TODO: Implement complex calculation involving database queries
        return 1; // Placeholder
    }

    private double calculateUsageGrowthRate(Exercise exercise) {
        // TODO: Calculate usage growth over time with historical data
        return 0.0; // Placeholder
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

            public Builder exerciseId(Long exerciseId) {
                this.exerciseId = exerciseId;
                return this;
            }

            public Builder exerciseName(String exerciseName) {
                this.exerciseName = exerciseName;
                return this;
            }

            public Builder totalUsage(Integer totalUsage) {
                this.totalUsage = totalUsage;
                return this;
            }

            public Builder averageRating(Double averageRating) {
                this.averageRating = averageRating;
                return this;
            }

            public Builder totalRatings(Integer totalRatings) {
                this.totalRatings = totalRatings;
                return this;
            }

            public Builder popularityRank(Integer popularityRank) {
                this.popularityRank = popularityRank;
                return this;
            }

            public Builder usageGrowthRate(Double usageGrowthRate) {
                this.usageGrowthRate = usageGrowthRate;
                return this;
            }

            public Builder isFromVerifiedSource(Boolean isFromVerifiedSource) {
                this.isFromVerifiedSource = isFromVerifiedSource;
                return this;
            }

            public ExerciseAnalytics build() {
                return new ExerciseAnalytics(this);
            }
        }

        // Getters
        public Long getExerciseId() {
            return exerciseId;
        }

        public String getExerciseName() {
            return exerciseName;
        }

        public Integer getTotalUsage() {
            return totalUsage;
        }

        public Double getAverageRating() {
            return averageRating;
        }

        public Integer getTotalRatings() {
            return totalRatings;
        }

        public Integer getPopularityRank() {
            return popularityRank;
        }

        public Double getUsageGrowthRate() {
            return usageGrowthRate;
        }

        public Boolean getIsFromVerifiedSource() {
            return isFromVerifiedSource;
        }
    }
}
