package com.chidituke.workout_tracker.dto.request.performance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * * Request DTO for completing a set with detailed performance data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompleteSetRequest {
    private Long exerciseId;
    private Integer setNumber;
    private Integer reps;
    private Double weight;
    private Integer durationMinutes;
    private Double durationSeconds;
    private Double distanceKm;
    private Integer caloriesBurned;
    private Integer holdDurationSeconds;
    private Integer targetReps;
    private Double targetWeight;
    private LocalDateTime setStartTime;
    private LocalDateTime setEndTime;
    private Integer restTimeSeconds;
    private Integer perceivedExertion;
    private Integer formRating;
    private String notes;

    // Add getters and setters or use @Data annotation
    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public Integer getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }

    public Integer getReps() {
        return reps;
    }

    public void setReps(Integer reps) {
        this.reps = reps;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Double getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Double durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(Integer caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public Integer getHoldDurationSeconds() {
        return holdDurationSeconds;
    }

    public void setHoldDurationSeconds(Integer holdDurationSeconds) {
        this.holdDurationSeconds = holdDurationSeconds;
    }

    public Integer getTargetReps() {
        return targetReps;
    }

    public void setTargetReps(Integer targetReps) {
        this.targetReps = targetReps;
    }

    public Double getTargetWeight() {
        return targetWeight;
    }

    public void setTargetWeight(Double targetWeight) {
        this.targetWeight = targetWeight;
    }

    public LocalDateTime getSetStartTime() {
        return setStartTime;
    }

    public void setSetStartTime(LocalDateTime setStartTime) {
        this.setStartTime = setStartTime;
    }

    public LocalDateTime getSetEndTime() {
        return setEndTime;
    }

    public void setSetEndTime(LocalDateTime setEndTime) {
        this.setEndTime = setEndTime;
    }

    public Integer getRestTimeSeconds() {
        return restTimeSeconds;
    }

    public void setRestTimeSeconds(Integer restTimeSeconds) {
        this.restTimeSeconds = restTimeSeconds;
    }

    public Integer getPerceivedExertion() {
        return perceivedExertion;
    }

    public void setPerceivedExertion(Integer perceivedExertion) {
        this.perceivedExertion = perceivedExertion;
    }

    public Integer getFormRating() {
        return formRating;
    }

    public void setFormRating(Integer formRating) {
        this.formRating = formRating;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
