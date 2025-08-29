package com.chidituke.workout_tracker.dto.request.performance;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletionRequest {
    @NotNull
    private String exerciseId;

    private LocalDateTime completedAt;
    private Integer totalDurationMinutes;
    private String notes;
    private String performanceRating; // EXCEEDED, MET, BELOW, STRUGGLED
}
