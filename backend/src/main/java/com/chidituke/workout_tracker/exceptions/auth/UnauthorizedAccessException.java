package com.chidituke.workout_tracker.exceptions.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when user is not authorized to perform an action
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedAccessException extends RuntimeException {

    private final String username;
    private final String resource;
    private final String action;

    public UnauthorizedAccessException(String message) {
        super(message);
        this.username = null;
        this.resource = null;
        this.action = null;
    }

    public UnauthorizedAccessException(String message, String username, String resource, String action) {
        super(message);
        this.username = username;
        this.resource = resource;
        this.action = action;
    }

    public UnauthorizedAccessException(String username, String resource, String action) {
        super(String.format("User '%s' is not authorized to %s on %s", username, action, resource));
        this.username = username;
        this.resource = resource;
        this.action = action;
    }

    public String getUsername() {
        return username;
    }

    public String getResource() {
        return resource;
    }

    public String getAction() {
        return action;
    }
}