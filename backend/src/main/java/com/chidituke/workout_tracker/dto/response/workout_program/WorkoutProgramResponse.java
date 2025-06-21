package com.chidituke.workout_tracker.dto.response.workout_program;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class WorkoutProgramResponse {

    private Long id;
    private String name;
    private String description;
    private String programType;
    private String difficultyLevel;
    private Integer durationWeeks;
    private Integer sessionsPerWeek;
    private String targetGoals;
    private String equipmentNeeded;

    // Creator info
    private Long createdByUserId;
    private Boolean createdByProfessional;

    // Status
    private Boolean isPublished;
    private Boolean isPublic;

    // Usage metrics
    private Integer enrollmentCount;
    private Integer completionCount;
    private Double averageRating;
    private Integer totalRatings;

    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed fields
    private Double completionRate;
    private Boolean isPopular;
    private Boolean isHighlyRated;
}