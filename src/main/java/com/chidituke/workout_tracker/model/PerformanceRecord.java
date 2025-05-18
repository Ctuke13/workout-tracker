package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "performance_records")
public class PerformanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    private String notes; // e.g., "Felt challenging", "Used different form"

}
