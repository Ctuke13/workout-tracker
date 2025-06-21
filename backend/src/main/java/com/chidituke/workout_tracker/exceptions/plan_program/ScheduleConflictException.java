package com.chidituke.workout_tracker.exceptions.plan_program;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there's a scheduling conflict
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ScheduleConflictException extends RuntimeException {

    private final Integer weekNumber;
    private final Integer dayNumber;
    private final Long programId;

    public ScheduleConflictException(String message) {
        super(message);
        this.weekNumber = null;
        this.dayNumber = null;
        this.programId = null;
    }

    public ScheduleConflictException(String message, Integer weekNumber, Integer dayNumber, Long programId) {
        super(message);
        this.weekNumber = weekNumber;
        this.dayNumber = dayNumber;
        this.programId = programId;
    }

    public ScheduleConflictException(Integer weekNumber, Integer dayNumber, Long programId) {
        super(String.format("Schedule conflict at week %d, day %d for program %d", weekNumber, dayNumber, programId));
        this.weekNumber = weekNumber;
        this.dayNumber = dayNumber;
        this.programId = programId;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public Integer getDayNumber() {
        return dayNumber;
    }

    public Long getProgramId() {
        return programId;
    }
}