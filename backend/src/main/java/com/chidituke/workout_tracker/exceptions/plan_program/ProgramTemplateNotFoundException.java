package com.chidituke.workout_tracker.exceptions.plan_program;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a program template is not found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProgramTemplateNotFoundException extends RuntimeException {

    private final String templateName;

    public ProgramTemplateNotFoundException(String templateName) {
        super("Program template not found: " + templateName);
        this.templateName = templateName;
    }

    public ProgramTemplateNotFoundException(String message, String templateName) {
        super(message);
        this.templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }
}