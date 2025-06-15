package com.chidituke.workout_tracker.dto.response.exercise;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExerciseFiltersDTO {
    private List<ExerciseTypeDTO> exerciseTypes;
    private List<DifficultyLevelDTO> difficultyLevels;
    private List<String> equipment;
    private List<String> muscleGroups;

    @Data
    @Builder
    public static class ExerciseTypeDTO {
        private String value;
        private String displayName;
        private long count;
    }

    @Data
    @Builder
    public static class DifficultyLevelDTO {
        private String value;
        private String description;
        private long count;
    }

    public static ExerciseFiltersDTO createDefault() {
        return ExerciseFiltersDTO.builder()
                .exerciseTypes(List.of(
                        ExerciseTypeDTO.builder().value("STRENGTH").displayName("Strength Training").count(0).build(),
                        ExerciseTypeDTO.builder().value("CARDIO").displayName("Cardiovascular").count(0).build(),
                        ExerciseTypeDTO.builder().value("FLEXIBILITY").displayName("Flexibility & Mobility").count(0).build(),
                        ExerciseTypeDTO.builder().value("BALANCE").displayName("Balance & Stability").count(0).build(),
                        ExerciseTypeDTO.builder().value("PLYOMETRIC").displayName("Plyometric & Power").count(0).build(),
                        ExerciseTypeDTO.builder().value("REHABILITATION").displayName("Rehabilitation").count(0).build(),
                        ExerciseTypeDTO.builder().value("SPORTS_SPECIFIC").displayName("Sports Specific").count(0).build()
                ))
                .difficultyLevels(List.of(
                        DifficultyLevelDTO.builder().value("BEGINNER").description("Beginner - No experience needed").count(0).build(),
                        DifficultyLevelDTO.builder().value("INTERMEDIATE").description("Intermediate - Some experience recommended").count(0).build(),
                        DifficultyLevelDTO.builder().value("ADVANCED").description("Advanced - For experienced athletes").count(0).build()
                ))
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
}