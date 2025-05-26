package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "workouts")
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String workoutName;

    @Column(columnDefinition = "TEXT")
    private String workoutDescription;

    @Column(nullable = false)
    private String workoutCategory;

    @Column(name = "image_url")
    private String workoutImageUrl;

    @Column(name = "is_cardio")
    private boolean isCardio = false;

}
