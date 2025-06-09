package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "exercises")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Exercise name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Goal is required")
    @Column(nullable = false)
    private String goal;

    @NotBlank(message = "Duration is required")
    private String duration;

    @NotBlank(message = "Equipment is required")
    private String equipment;

    @NotBlank(message = "Difficulty is required")
    private String difficulty;

    private String calories;

    @Column(length = 10)
    private String emoji;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "exercise_benefits", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "benefit")
    private List<String> benefits;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "exercise_tips", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "tip")
    private List<String> tips;

    private String videoUrl;

    @Column(nullable = false)
    private boolean published = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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