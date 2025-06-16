package com.chidituke.workout_tracker.exceptions.auth;

public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}