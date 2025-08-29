package com.chidituke.workout_tracker.dto.request.performance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for batch enhanced completion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchCompletionRequest {
    @NotEmpty
    @Valid
    private List<CompletionRequest> completions;
}
