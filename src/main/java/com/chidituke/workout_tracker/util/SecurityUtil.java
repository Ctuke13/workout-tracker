package com.chidituke.workout_tracker.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SecurityUtil {

    private SecurityUtil() {
        // Private constructor to prevent instantiaion
    }

    /**
     * Get the username of the currently authenticated user
     */
    public static String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null || !auth.isAuthenticated()) {
            throw new RuntimeException("No authenticated user found");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }

        throw new RuntimeException("Unable to determine current user");
    };

    /**
     * Check if there is an authenticated user
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}