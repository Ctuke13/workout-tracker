package com.chidituke.workout_tracker.mapper;

import com.chidituke.workout_tracker.dto.request.workout_session.WorkoutSessionRequest;
import com.chidituke.workout_tracker.dto.response.workout_session.WorkoutSessionResponse;
import com.chidituke.workout_tracker.model.WorkoutSession;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WorkoutSessionMapper {

    public WorkoutSessionResponse toResponse(WorkoutSession entity) {
        if (entity == null) {
            return null;
        }

        return WorkoutSessionResponse.builder()
                .id(entity.getId())
                .date(entity.getDate())
                .totalDurationMinutes(entity.getTotalDurationMinutes())
                .estimatedCalories(entity.getEstimatedCalories())
                .difficultyRating(entity.getDifficultyRating())
                .overallEffort(entity.getOverallEffort())
                .mood(entity.getMood() != null ? entity.getMood().name() : null)
                .location(entity.getLocation() != null ? entity.getLocation().name() : null)
                .notes(entity.getNotes())
                .workoutPlanId(entity.getWorkoutPlan() != null ? entity.getWorkoutPlan().getId() : null)
                .workoutPlanName(entity.getWorkoutPlan() != null ? entity.getWorkoutPlan().getWorkoutName() : null)
                .workoutPlanCategory(entity.getWorkoutPlan() != null ? entity.getWorkoutPlan().getWorkoutCategory() : null)
                .programId(entity.getProgram() != null ? entity.getProgram().getId() : null)
                .programName(entity.getProgram() != null ? entity.getProgram().getName() : null)
                .weekNumber(entity.getWeekNumber())
                .scheduledWorkoutId(entity.getScheduledWorkout() != null ? entity.getScheduledWorkout().getId() : null)
                .isShared(entity.getIsShared())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<WorkoutSessionResponse> toResponseList(List<WorkoutSession> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void mapRequestToEntity(WorkoutSessionRequest request, WorkoutSession entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setDate(request.getDate());
        entity.setTotalDurationMinutes(request.getTotalDurationMinutes());
        entity.setEstimatedCalories(request.getEstimatedCalories());
        entity.setDifficultyRating(request.getDifficultyRating());
        entity.setOverallEffort(request.getOverallEffort());
        entity.setNotes(request.getNotes());
        entity.setWeekNumber(request.getWeekNumber());
        entity.setIsShared(request.getIsShared());

        // Convert mood string to enum
        if (request.getMood() != null) {
            try {
                entity.setMood(WorkoutSession.WorkoutMood.valueOf(request.getMood().toUpperCase()));
            } catch (IllegalArgumentException e) {
                entity.setMood(null);
            }
        }

        // Convert location string to enum
        if (request.getLocation() != null) {
            try {
                entity.setLocation(WorkoutSession.WorkoutLocation.valueOf(request.getLocation().toUpperCase()));
            } catch (IllegalArgumentException e) {
                entity.setLocation(null);
            }
        }
    }
}