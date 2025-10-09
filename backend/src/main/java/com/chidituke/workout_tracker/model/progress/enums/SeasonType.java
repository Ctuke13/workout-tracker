package com.chidituke.workout_tracker.model.progress.enums;

/**
 * Enum representing the four seasonal competition cycles.
 * Each season lasts 3 months and resets user seasonal progression.
 */
public enum SeasonType {
    WINTER,  // January 1 - March 31
    SPRING,  // April 1 - June 30
    SUMMER,  // July 1 - September 30
    FALL;    // October 1 - December 31

    public static SeasonType fromMonth(int month) {
        if (month >= 1 && month <= 3) return WINTER;
        else if (month >= 4 && month <= 6) return SPRING;
        else if (month >= 7 && month <= 9) return SUMMER;
        else if (month >= 10 && month <= 12) return FALL;
        throw new IllegalArgumentException("Invalid month: " + month);
    }
}