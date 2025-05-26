package com.chidituke.workout_tracker.mapper;

import com.chidituke.workout_tracker.dto.WorkoutRequest;
import com.chidituke.workout_tracker.dto.WorkoutResponse;
import com.chidituke.workout_tracker.model.Workout;
import org.springframework.stereotype.Component;

@Component
public class WorkoutMapper {

    public void mapRequestToEntity(WorkoutRequest request, Workout workout) {
        workout.setWorkoutName(request.getWorkoutName());
        workout.setWorkoutDescription(request.getWorkoutDescription());
        workout.setWorkoutType(request.getWorkoutCategory());
        workout.setWorkoutImageUrl(request.getImageUrl());
        workout.setCardio(request.getIsCardio());
    }

    public WorkoutResponse mapEntityToResponse(Workout workout) {
        return new WorkoutResponse(
                workout.getId(),
                workout.getWorkoutName(),
                workout.getWorkoutDescription(),
                workout.getWorkoutCategory(),
                workout.getWorkoutImageUrl(),
                workout.isCardio()
        );
    }

    public Workout createEntityFromRequest(WorkoutRequest request) {
        Workout workout = new Workout();
        mapRequestToEntity(request, workout);
        return workout;
    }
}