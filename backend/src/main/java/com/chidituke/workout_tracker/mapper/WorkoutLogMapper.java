package com.chidituke.workout_tracker.mapper;

import com.chidituke.workout_tracker.dto.WorkoutLogRequest;
import com.chidituke.workout_tracker.dto.WorkoutLogResponse;
import com.chidituke.workout_tracker.model.WorkoutLog;
import org.springframework.stereotype.Component;

@Component
public class WorkoutLogMapper {

    /**
     * Maps WorkoutLogRequest DTO to WorkoutLog entity for updates
     * Note: User and Workout are set separately in the service
     */
    public void mapRequestToEntity(WorkoutLogRequest workoutLogRequest, WorkoutLog workoutLog) {
        workoutLog.setDate(workoutLogRequest.getDate());
        workoutLog.setNotes(workoutLogRequest.getNotes());
    }

    /**
            * Converts WorkoutLog entity to WorkoutLogResponse DTO
     */
    public WorkoutLogResponse mapEntityToResponse(WorkoutLog workoutLog) {
        return new WorkoutLogResponse(
          workoutLog.getId(),
          workoutLog.getUser().getId(),
          workoutLog.getUser().getUsername(),
          workoutLog.getWorkout().getId(),
          workoutLog.getWorkout().getWorkoutName(),
          workoutLog.getWorkout().getWorkoutCategory(),
          workoutLog.getWorkout().isCardio(),
          workoutLog.getDate(),
                workoutLog.getNotes()
        );
    }
}
