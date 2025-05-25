package com.chidituke.workout_tracker.dto;

import lombok.Data;

@Data
public class WorkoutResponse {
    private Long id;
    private String workoutName;
    private String workoutDescription;
    private String workoutType;
    private String workoutImageUrl;
    private Boolean isCardio;

    // Constructor for easy mapping
    public WorkoutResponse(Long id, String workoutName, String workoutDescription, String workoutType, String workoutImageUrl, Boolean isCardio) {
        this.id = id;
        this.workoutName = workoutName;
        this.workoutDescription = workoutDescription;
        this.workoutType = workoutType;
        this.workoutImageUrl = workoutImageUrl;
        this.isCardio = isCardio;
    }
}
