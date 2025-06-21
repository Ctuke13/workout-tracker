package com.chidituke.workout_tracker.exceptions.workout_session;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WorkoutSessionNotFoundException extends RuntimeException {

    public WorkoutSessionNotFoundException(Long id) {
        super("Workout session not found with id: " + id);
    }

    public WorkoutSessionNotFoundException(String message) {
        super(message);
    }
}