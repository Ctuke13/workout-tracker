package com.chidituke.workout_tracker.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetNameUpdateRequest {

    @NotBlank(message = "Pet name cannot be empty")
    @Size(max = 50, message = "Pet name cannot exceed 50 characters")
    private String petName;
}