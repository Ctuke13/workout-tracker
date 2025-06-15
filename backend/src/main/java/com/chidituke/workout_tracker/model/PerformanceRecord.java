package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "performance_records")
public class PerformanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_log_id", nullable = false)
    private WorkoutLog workoutLog;

    @Column(name = "set_number")
    private Integer setNumber;

    private Integer reps;
    private Double weight;

    // For cardio
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "duration_seconds")
    private Double durationSeconds;

    @Column(name = "distance_km")
    private Double distanceKm;

    //Additonal performance metrics
    @Column(name = "calories_burned")
    private Integer caloriesBurned;

    @Column(name = "perceived_exertion")
    private Integer perceivedExertion; // 1-10 RPE scale

    @Column(name = "form_rating")
    private Integer formRating; // 1-10 self-assessment of form quality

    @Column(name = "rest_seconds")
    private Integer restSeconds; // Rest time between sets

    @Column(name = "tempo")
    private String tempo; // "3-1-2-1" (eccentric-pause-concentric-pause)

    // For flexibility/balance exercises:
    @Column(name = "hold_duration_seconds")
    private Integer holdDurationSeconds; // How long held a pose/position

    @Column(name = "balance_score")
    private Integer balanceScore; // 1-10 for balance exercises

    // For plyometric exercises:
    @Column(name = "jump_height_cm")
    private Double jumpHeightCm;

    @Column(name = "power_output_watts")
    private Double powerOutputWatts;

    // Professional program tracking:
    @Column(name = "assigned_by_trainer_id")
    private Long assignedByTrainerId; // If part of a trainer's program

    @Column(name = "target_reps")
    private Integer targetReps; // What trainer/program prescribed

    @Column(name = "target_weight")
    private Double targetWeight; // What was supposed to be lifted

    @Column(name = "achievement_status")
    private String achievementStatus; // "EXCEEDED", "MET", "BELOW_TARGET"

    private String notes; // e.g., "Felt challenging", "Used different form"

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Add JPA lifecycle methods:
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
