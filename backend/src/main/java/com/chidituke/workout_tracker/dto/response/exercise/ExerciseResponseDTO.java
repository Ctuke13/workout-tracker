package com.chidituke.workout_tracker.dto.response.exercise;

import com.chidituke.workout_tracker.model.Exercise;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExerciseResponseDTO {
    private Long id;
    private String name;
    private String emoji;
    private String description;
    private String exerciseType;
    private String exerciseTypeDisplay;
    private String difficultyLevel;
    private String difficultyDescription;
    private Integer estimatedDurationMinutes;
    private Integer estimatedCalories;
    private List<String> targetMuscleGroups;
    private List<String> equipmentRequired;
    private String equipmentSummary;
    private List<String> benefits;
    private List<String> tips;
    private String videoUrl;
    private Integer usageCount;
    private Double averageRating;
    private Integer totalRatings;
    private Boolean isPopular;
    private Boolean isHighlyRated;
    private Boolean isFromVerifiedSource;
    private Boolean canDoAtHome;
    private Boolean requiresEquipment;
    private String createdBy; // "Platform" or professional name
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ExerciseResponseDTO fromEntity(Exercise exercise) {
        return ExerciseResponseDTO.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .emoji(exercise.getEmoji())
                .description(exercise.getDescription())
                .exerciseType(exercise.getExerciseType().name())
                .exerciseTypeDisplay(exercise.getExerciseType().getDisplayName())
                .difficultyLevel(exercise.getDifficultyLevel().name())
                .difficultyDescription(exercise.getDifficultyDescription())
                .estimatedDurationMinutes(exercise.getEstimatedDurationMinutes())
                .estimatedCalories(exercise.getEstimatedCalories())
                .targetMuscleGroups(exercise.getTargetMuscleGroupsList())
                .equipmentRequired(exercise.getEquipmentRequired())
                .equipmentSummary(exercise.getEquipmentSummary())
                .benefits(exercise.getBenefits())
                .tips(exercise.getTips())
                .videoUrl(exercise.getVideoUrl())
                .usageCount(exercise.getUsageCount())
                .averageRating(exercise.getAverageRating())
                .totalRatings(exercise.getTotalRatings())
                .isPopular(exercise.isPopular())
                .isHighlyRated(exercise.isHighlyRated())
                .isFromVerifiedSource(exercise.isFromVerifiedSource())
                .canDoAtHome(exercise.canDoAtHome())
                .requiresEquipment(exercise.requiresEquipment())
                .createdBy(exercise.getCreatedByProfessional() ? "Professional" : "Platform")
                .createdAt(exercise.getCreatedAt())
                .updatedAt(exercise.getUpdatedAt())
                .build();
    }

    public static List<ExerciseResponseDTO> fromEntityList(List<Exercise> exercises) {
        return exercises.stream()
                .map(ExerciseResponseDTO::fromEntity)
                .toList();
    }
}