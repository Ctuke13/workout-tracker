package com.chidituke.workout_tracker.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NicknameUpdateRequest {

    @NotBlank(message = "Nickname cannot be empty")
    @Size(min = 3, max = 20, message = "Nickname must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Nickname can only contain letters, numbers, and underscores")
    private String nickname;
}