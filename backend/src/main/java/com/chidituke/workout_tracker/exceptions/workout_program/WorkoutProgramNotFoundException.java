package com.chidituke.workout_tracker.exceptions.workout_program;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WorkoutProgramNotFoundException extends RuntimeException {

    public WorkoutProgramNotFoundException(Long id) {
        super("Workout program not found with id: " + id);
    }

    public WorkoutProgramNotFoundException(String message) {
        super(message);
    }
}