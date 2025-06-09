package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;  // Primary identifier - no username!

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Utility methods
    public String getDisplayName() {
        return firstName + " " + lastName.charAt(0) + ".";
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}