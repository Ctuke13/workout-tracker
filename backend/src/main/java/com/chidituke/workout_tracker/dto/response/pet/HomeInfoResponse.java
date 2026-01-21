package com.chidituke.workout_tracker.dto.response.pet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeInfoResponse {

    private String selectedHome;
    private List<String> unlockedHomes;
    private List<String> lockedHomes;
}