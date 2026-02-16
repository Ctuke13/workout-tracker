package com.chidituke.workout_tracker.dto.request.pet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating pet name
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetNameRequest {

    @NotBlank(message = "Pet name cannot be empty")
    @Size(min = 1, max = 20, message = "Pet name must be between 1 and 20 characters")
    @Pattern(
            regexp = "^[a-zA-Z0-9 ,.'-]+$",
            message = "Pet name can only contain letters, numbers, spaces, and basic punctuation (,.'-)"
    )
    private String petName;
}