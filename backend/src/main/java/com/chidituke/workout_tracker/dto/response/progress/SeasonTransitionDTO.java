package com.chidituke.workout_tracker.dto.response.progress;


// ========================================================================
// RESPONSE DTO FOR SEASON TRANSITION
// ========================================================================

import java.time.LocalDate;

/**
 * Response DTO for manual season transition endpoint.
 * Contains transition details and status.
 */
@lombok.Data
@lombok.Builder
public class SeasonTransitionDTO {
    private boolean success;
    private String message;
    private String previousSeason;
    private String newSeason;
    private LocalDate transitionDate;
}