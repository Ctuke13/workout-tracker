package com.chidituke.workout_tracker.dto.request.pet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeSelectionRequest {

    @NotBlank(message = "Home type is required")
    @Pattern(
            regexp = "GYM|NATURE|COZY|BEACH|SPACE|CYBER",
            message = "Invalid home type. Must be one of: GYM, NATURE, COZY, BEACH, SPACE, CYBER"
    )
    private String homeType;
}