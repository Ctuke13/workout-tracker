package com.chidituke.workout_tracker.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
    /**
     * Whether the authentication principal is required.
     * Default is true (required).
     * Set to false for optional authentication.
     */
    boolean required() default true;
}