package com.chidituke.workout_tracker.exceptions.auth;

public class TokenExpiredException extends AuthException {
    public TokenExpiredException() {
        super("Authentication token has expired");
    }
}