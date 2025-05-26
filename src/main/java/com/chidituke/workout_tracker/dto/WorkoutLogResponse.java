package com.chidituke.workout_tracker.dto;

import lombok.Data;

import java.time.LocalDate;

public class WorkoutLogResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long workoutId;
    private String workoutName;
    private String workoutCategory;
    private Boolean isCardio;
    private LocalDate date;
    private String notes;

    // Constructor
    public WorkoutLogResponse(Long id, Long userId, String username, Long workoutId, String workoutName, String workoutCategory, Boolean isCardio, LocalDate date, String notes) {
        this.id = id;
        this.userId = userId;
        this.userName = username;
        this.workoutId = workoutId;
        this.workoutName = workoutName;
        this.workoutCategory = workoutCategory;
        this.isCardio = isCardio;
        this.date = date;
        this.notes = notes;
    }
}
