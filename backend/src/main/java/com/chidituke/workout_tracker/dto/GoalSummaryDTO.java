package com.chidituke.workout_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoalSummaryDTO {
    private String goal;
    private int count;
}