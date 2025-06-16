package com.chidituke.workout_tracker.exceptions.performance;

public class InvalidPerformanceDataException extends PerformanceException {
    public InvalidPerformanceDataException(String metric, Object value) {
        super(String.format("Invalid performance data: %s = %s", metric, value));
    }
}