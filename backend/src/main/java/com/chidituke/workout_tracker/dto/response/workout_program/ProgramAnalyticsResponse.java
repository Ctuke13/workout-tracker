package com.chidituke.workout_tracker.dto.response.workout_program;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProgramAnalyticsResponse {

    private Long programId;
    private String programName;
    private Integer enrollmentCount;
    private Integer completionCount;
    private Double completionRate;
    private Double averageRating;
    private Integer totalRatings;
    private Boolean isPopular;
    private Boolean isHighlyRated;
}