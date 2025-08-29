package com.chidituke.workout_tracker.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "Password123";
        String storedHash = "$2a$10$ub432U6KDccTEHiJKyKOguBPWSiBE4QuUdjMPnuFFWFljC/3JY0rO";

        System.out.println("Password: " + password);
        System.out.println("Stored hash: " + storedHash);
        System.out.println("Matches: " + encoder.matches(password, storedHash));

        // Generate fresh hash
        String freshHash = encoder.encode(password);
        System.out.println("Fresh hash: " + freshHash);
        System.out.println("Fresh matches: " + encoder.matches(password, freshHash));
    }
}