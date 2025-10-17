package com.chidituke.workout_tracker.model.analytics;

/**
 * Available metrics for Performance Tracker
 */
public enum PerformanceMetric {
    WEIGHT,      // Max weight lifted
    VOLUME,      // Total volume (weight × reps)
    REPS,        // Total reps
    SETS,        // Total sets
    DISTANCE,    // Distance (cardio)
    PACE,        // Pace (cardio)
    SPEED,       // Speed (cardio)
    CALORIES;    // Calories burned

    public static PerformanceMetric fromString(String metric) {
        try {
            return PerformanceMetric.valueOf(metric.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid metric: " + metric);
        }
    }
}