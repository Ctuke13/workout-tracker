package com.chidituke.workout_tracker.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ExerciseDTO {
    private Long id;
    private String name;
    private String goal;
    private String duration;
    private String equipment;
    private String difficulty;
    private String calories;
    private String emoji;
    private String description;
    private List<String> benefits;
    private List<String> tips;
    private String videoUrl;
}