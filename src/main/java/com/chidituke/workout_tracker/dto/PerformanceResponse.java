package com.chidituke.workout_tracker.dto;

import lombok.Data;

import java.time.LocalDate;

public class PerformanceResponse {

    @Data
    public class PerformanceResponse {
        private Long id;
        private Long workoutLogId;
        private LocalDate date;
        private String workoutName;
        private String category;
        private Boolean isCardio;
        private Integer setNumber;

        // Strength training
        private Integer reps;
        private Double weight;

        // Cardio
        private Integer durationMinutes;
        private Double durationSeconds;
        private Double distanceKm;

        // Additional Metrics
        private Integer caloriesBurned;
        private String notes;

        // Constructor
        public PerformanceResponse(Long id, Long workoutLogId, LocalDate date, String workoutName,
                                   String category, Boolean isCardio, Integer setNumber,
                                   Integer reps, Double weight, Integer durationMinutes,
                                   Double durationSeconds, Double distanceKm,
                                   Integer caloriesBurned, String notes){
            this.id = id;
            this.workoutLogId = workoutLogId;
            this.date = date;
            this.workoutName = workoutName;
            this.category = category;
            this.isCardio = isCardio;
            this.setNumber = setNumber;
            this.reps = reps;
            this.weight = weight;
            this.durationMinutes = durationMinutes;
            this.durationSeconds = durationSeconds;
            this.distanceKm = distanceKm;
            this.caloriesBurned = caloriesBurned;
            this.notes = notes;
        }
    }
}
