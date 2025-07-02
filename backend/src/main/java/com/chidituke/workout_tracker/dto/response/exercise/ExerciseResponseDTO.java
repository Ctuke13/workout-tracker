package com.chidituke.workout_tracker.dto.response.exercise;

import com.chidituke.workout_tracker.model.workout.Exercise;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExerciseResponseDTO {
    // ===== EXISTING FIELDS (Keep all) =====
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
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== ADD FRONTEND-SPECIFIC FIELDS =====

    // Frontend-formatted fields (only populated for public API)
    private String duration;           // "20 mins", "30-45 mins"
    private String calories;           // "400-600/hr", "250/session"
    private String equipment;          // "Dumbbells", "No Equipment"
    private String difficulty;         // "Beginner", "Intermediate", "Advanced"

    // Goal mapping fields (for frontend filtering)
    private String goal;               // Primary goal: "fat-burn", "muscle-building"
    private List<String> goals;        // All applicable goals

    // Frontend convenience fields
    private Boolean hasVideo;          // videoUrl != null && !videoUrl.isEmpty()
    private String rating;             // "4.5 stars (120 reviews)"

    // ===== FACTORY METHODS =====

    // For admin/user APIs (full data, no frontend formatting)
    public static ExerciseResponseDTO fromEntity(Exercise exercise) {
        return ExerciseResponseDTO.builder()
                .id(exercise.getId())
                .name(exercise.getExerciseName())  // Fixed: was exercise.getName()
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
                // Frontend fields will be null for admin API
                .build();
    }

    // For public frontend API (includes formatted frontend fields)
    public static ExerciseResponseDTO fromEntityForFrontend(Exercise exercise) {
        ExerciseResponseDTO dto = fromEntity(exercise);

        // Add frontend-specific formatting
        dto.duration = formatDuration(exercise.getEstimatedDurationMinutes());
        dto.calories = formatCalories(exercise.getEstimatedCalories());
        dto.equipment = formatEquipmentForFrontend(exercise.getEquipmentRequired());
        dto.difficulty = formatDifficulty(exercise.getDifficultyLevel());
        dto.goal = mapToPrimaryGoal(exercise.getExerciseType());
        dto.goals = mapToAllGoals(exercise.getExerciseType());
        dto.hasVideo = exercise.getVideoUrl() != null && !exercise.getVideoUrl().isEmpty();
        dto.rating = formatRating(exercise.getAverageRating(), exercise.getTotalRatings());

        return dto;
    }

    public static List<ExerciseResponseDTO> fromEntityList(List<Exercise> exercises) {
        return exercises.stream()
                .map(ExerciseResponseDTO::fromEntity)
                .toList();
    }

    public static List<ExerciseResponseDTO> fromEntityListForFrontend(List<Exercise> exercises) {
        return exercises.stream()
                .map(ExerciseResponseDTO::fromEntityForFrontend)
                .toList();
    }

    // ===== PRIVATE FORMATTING METHODS =====

    private static String formatDuration(Integer durationMinutes) {
        if (durationMinutes == null) return "20 mins";

        if (durationMinutes <= 15) return durationMinutes + " mins";
        if (durationMinutes <= 30) return durationMinutes + " mins";
        if (durationMinutes <= 60) return durationMinutes + " mins";

        int hours = durationMinutes / 60;
        int mins = durationMinutes % 60;
        return hours + "h" + (mins > 0 ? " " + mins + "m" : "");
    }

    private static String formatCalories(Integer calories) {
        if (calories == null) return "200-400/hr";

        int lower = (int) (calories * 0.8);
        int upper = (int) (calories * 1.2);
        return lower + "-" + upper + "/hr";
    }

    private static String formatEquipmentForFrontend(List<String> equipmentList) {
        if (equipmentList == null || equipmentList.isEmpty()) {
            return "No Equipment";
        }

        if (equipmentList.size() == 1) {
            return formatSingleEquipment(equipmentList.get(0));
        }

        return formatSingleEquipment(equipmentList.get(0)) + " (+more)";
    }

    private static String formatSingleEquipment(String equipment) {
        return switch (equipment.toLowerCase()) {
            case "dumbbells", "dumbbell" -> "Dumbbells";
            case "barbell" -> "Barbell";
            case "resistance_bands", "resistance_band" -> "Resistance Bands";
            case "kettlebell" -> "Kettlebell";
            case "yoga_mat" -> "Yoga Mat";
            case "bodyweight", "none" -> "No Equipment";
            default -> equipment;
        };
    }

    private static String formatDifficulty(Exercise.DifficultyLevel difficulty) {
        if (difficulty == null) return "Beginner";
        return switch (difficulty) {
            case BEGINNER -> "Beginner";
            case INTERMEDIATE -> "Intermediate";
            case ADVANCED -> "Advanced";
        };
    }

    private static String mapToPrimaryGoal(Exercise.ExerciseType exerciseType) {
        if (exerciseType == null) return "muscle-building";

        return switch (exerciseType) {
            case STRENGTH -> "muscle-building";
            case CARDIO -> "fat-burn";
            case FLEXIBILITY -> "flexibility";
            case REHABILITATION -> "recovery";
            case SPORTS_SPECIFIC -> "sport-specific";
            case PLYOMETRIC -> "endurance";
            case BALANCE -> "recovery";
        };
    }

    private static List<String> mapToAllGoals(Exercise.ExerciseType exerciseType) {
        if (exerciseType == null) return List.of("muscle-building");

        return switch (exerciseType) {
            case STRENGTH -> List.of("muscle-building", "endurance");
            case CARDIO -> List.of("fat-burn", "endurance");
            case FLEXIBILITY -> List.of("flexibility", "recovery");
            case REHABILITATION -> List.of("recovery");
            case SPORTS_SPECIFIC -> List.of("sport-specific", "endurance");
            case PLYOMETRIC -> List.of("endurance", "fat-burn");
            case BALANCE -> List.of("recovery", "flexibility");
        };
    }

    private static String formatRating(Double averageRating, Integer totalRatings) {
        if (averageRating == null || totalRatings == null || totalRatings == 0) {
            return "Not rated";
        }

        return String.format("%.1f stars (%d review%s)",
                averageRating, totalRatings, totalRatings == 1 ? "" : "s");
    }
}