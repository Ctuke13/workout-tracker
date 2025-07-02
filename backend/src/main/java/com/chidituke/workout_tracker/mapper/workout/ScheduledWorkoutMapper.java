package com.chidituke.workout_tracker.mapper.workout;

import com.chidituke.workout_tracker.dto.response.scheduled_workouts.ScheduledWorkoutResponse;
import com.chidituke.workout_tracker.model.workout.ScheduledWorkout;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScheduledWorkoutMapper {

    public ScheduledWorkoutResponse toResponse(ScheduledWorkout entity) {
        if (entity == null) {
            return null;
        }

        return ScheduledWorkoutResponse.builder()
                .id(entity.getId())
                .scheduledDate(entity.getScheduledDate())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .weekNumber(entity.getWeekNumber())
                .dayOfWeek(entity.getDayOfWeek())
                .customNotes(entity.getCustomNotes())
                .reminderTime(entity.getReminderTime())
                .estimatedDurationMinutes(entity.getEstimatedDurationMinutes())
                .completedAt(entity.getCompletedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdByUserId(entity.getCreatedByUserId())
                // Map nested WorkoutPlanInfo
                .workoutPlan(mapWorkoutPlanInfo(entity))
                // Map nested UserInfo
                .user(mapUserInfo(entity))
                // Map nested WorkoutProgramInfo
                .program(mapWorkoutProgramInfo(entity))
                // Map nested WorkoutSessionInfo
                .completedSession(mapWorkoutSessionInfo(entity))
                .build();
    }

    public List<ScheduledWorkoutResponse> toResponseList(List<ScheduledWorkout> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ScheduledWorkoutResponse.WorkoutPlanInfo mapWorkoutPlanInfo(ScheduledWorkout entity) {
        if (entity.getWorkoutPlan() == null) {
            return null;
        }

        return ScheduledWorkoutResponse.WorkoutPlanInfo.builder()
                .id(entity.getWorkoutPlan().getId())
                .name(entity.getWorkoutPlan().getWorkoutName())
                .description(entity.getWorkoutPlan().getWorkoutDescription())
                .difficulty(entity.getWorkoutPlan().getDifficultyLevel() != null ?
                        entity.getWorkoutPlan().getDifficultyLevel().name() : null)
                .estimatedDurationMinutes(entity.getWorkoutPlan().getEstimatedDurationMinutes())
                .category(entity.getWorkoutPlan().getWorkoutCategory())
                .imageUrl(entity.getWorkoutPlan().getWorkoutImageUrl())
                .isPublic(entity.getWorkoutPlan().getIsPublic())
                .build();
    }

    private ScheduledWorkoutResponse.UserInfo mapUserInfo(ScheduledWorkout entity) {
        if (entity.getUser() == null) {
            return null;
        }

        return ScheduledWorkoutResponse.UserInfo.builder()
                .id(entity.getUser().getId())
                .username(entity.getUser().getUsername())
                .email(entity.getUser().getEmail())
                .firstName(entity.getUser().getFirstName())
                .lastName(entity.getUser().getLastName())
                .subscriptionTier(entity.getUser().getSubscriptionTier() != null ?
                        entity.getUser().getSubscriptionTier().name() : null)
                .build();
    }

    private ScheduledWorkoutResponse.WorkoutProgramInfo mapWorkoutProgramInfo(ScheduledWorkout entity) {
        if (entity.getProgram() == null) {
            return null;
        }

        return ScheduledWorkoutResponse.WorkoutProgramInfo.builder()
                .id(entity.getProgram().getId())
                .name(entity.getProgram().getName())
                .description(entity.getProgram().getDescription())
                .totalWeeks(entity.getProgram().getDurationWeeks())
                .difficulty(entity.getProgram().getDifficultyLevel() != null ?
                        entity.getProgram().getDifficultyLevel().name() : null)
                .category(entity.getProgram().getProgramType() != null ?
                        entity.getProgram().getProgramType().getDisplayName() : null)
                .imageUrl(null) // WorkoutProgram doesn't have imageUrl field
                .isActive(entity.getProgram().isActive()) // Use isActive() method, not getIsActive()
                .build();
    }

    private ScheduledWorkoutResponse.WorkoutSessionInfo mapWorkoutSessionInfo(ScheduledWorkout entity) {
        if (entity.getCompletedSession() == null) {
            return null;
        }

        return ScheduledWorkoutResponse.WorkoutSessionInfo.builder()
                .id(entity.getCompletedSession().getId())
                .startTime(entity.getCompletedSession().getCreatedAt()) // Assuming this is start time
                .endTime(entity.getCompletedSession().getUpdatedAt()) // Assuming this is end time
                .actualDurationMinutes(entity.getCompletedSession().getTotalDurationMinutes())
                .notes(entity.getCompletedSession().getNotes())
                .completed(true) // If it exists, it's completed
                .build();
    }

    private String dayOfWeekToName(Integer dayOfWeek) {
        if (dayOfWeek == null) {
            return null;
        }
        try {
            return DayOfWeek.of(dayOfWeek).name();
        } catch (Exception e) {
            return null;
        }
    }
}