package com.chidituke.workout_tracker.dto.response.scheduled_workouts;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for ScheduledWorkout entity
 * Accurately maps to the actual ScheduledWorkout.java entity structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduledWorkoutResponse {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduledDate;

    private String status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, SKIPPED, RESCHEDULED

    // Program context (optional)
    private Integer weekNumber; // Which week of the program
    private Integer dayOfWeek; // 1=Monday, 7=Sunday

    // User customizations
    private String customNotes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reminderTime;

    private Integer estimatedDurationMinutes;

    // Completion tracking
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    // Metadata
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Long createdByUserId; // For coach-assigned workouts

    // Related entity information
    private WorkoutPlanInfo workoutPlan;
    private UserInfo user;
    private WorkoutProgramInfo program; // Optional - only if part of program
    private WorkoutSessionInfo completedSession; // Only if completed

    // Nested DTOs for related entities
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkoutPlanInfo {
        private Long id;
        private String name;
        private String description;
        private String difficulty; // BEGINNER, INTERMEDIATE, ADVANCED
        private Integer estimatedDurationMinutes;
        private Integer exerciseCount;
        private String category;
        private String imageUrl;
        private Boolean isPublic;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String subscriptionTier; // FREE, PLUS, PRO
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkoutProgramInfo {
        private Long id;
        private String name;
        private String description;
        private Integer totalWeeks;
        private String difficulty;
        private String category;
        private String imageUrl;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkoutSessionInfo {
        private Long id;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer actualDurationMinutes;
        private String notes;
        private Boolean completed;
    }

    // Utility methods matching the entity's business logic
    public boolean isOverdue() {
        return "SCHEDULED".equals(status) &&
                scheduledDate != null &&
                scheduledDate.isBefore(LocalDate.now());
    }

    public boolean isToday() {
        if (scheduledDate == null) return false;
        return scheduledDate.equals(LocalDate.now());
    }

    public boolean isUpcoming() {
        return "SCHEDULED".equals(status) &&
                scheduledDate != null &&
                scheduledDate.isAfter(LocalDate.now());
    }

    public boolean canBeStarted() {
        return "SCHEDULED".equals(status) &&
                (isToday() || isOverdue());
    }

    public boolean canBeCancelled() {
        return "SCHEDULED".equals(status) || "IN_PROGRESS".equals(status);
    }

    public boolean canBeRescheduled() {
        return "SCHEDULED".equals(status) &&
                scheduledDate != null &&
                scheduledDate.isAfter(LocalDate.now());
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isInProgress() {
        return "IN_PROGRESS".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isSkipped() {
        return "SKIPPED".equals(status);
    }

    public boolean isRescheduled() {
        return "RESCHEDULED".equals(status);
    }

    public boolean isPartOfProgram() {
        return program != null && program.getId() != null;
    }

    public boolean hasCompletedSession() {
        return completedSession != null && completedSession.getId() != null;
    }

    public boolean isCoachAssigned() {
        return createdByUserId != null;
    }

    public boolean hasReminder() {
        return reminderTime != null;
    }

    // Display methods for UI
    public String getDisplayTitle() {
        if (workoutPlan != null && workoutPlan.getName() != null) {
            return workoutPlan.getName();
        }
        return "Workout";
    }

    public String getDisplaySubtitle() {
        StringBuilder subtitle = new StringBuilder();

        if (workoutPlan != null) {
            if (workoutPlan.getDifficulty() != null) {
                subtitle.append(workoutPlan.getDifficulty());
            }
            if (workoutPlan.getEstimatedDurationMinutes() != null) {
                if (subtitle.length() > 0) subtitle.append(" • ");
                subtitle.append(workoutPlan.getEstimatedDurationMinutes()).append(" min");
            }
        }

        if (isPartOfProgram() && weekNumber != null) {
            if (subtitle.length() > 0) subtitle.append(" • ");
            subtitle.append("Week ").append(weekNumber);
            if (dayOfWeek != null) {
                subtitle.append(", Day ").append(dayOfWeek);
            }
        }

        return subtitle.toString();
    }

    public String getDisplayStatus() {
        if (isOverdue() && "SCHEDULED".equals(status)) {
            return "OVERDUE";
        }
        return status;
    }

    public String getDayOfWeekName() {
        if (dayOfWeek == null) return null;
        String[] days = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return dayOfWeek >= 1 && dayOfWeek <= 7 ? days[dayOfWeek] : null;
    }

    public long getDaysUntilScheduled() {
        if (scheduledDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), scheduledDate);
    }

    // Program progress calculation
    public String getProgramWeekDisplay() {
        if (!isPartOfProgram() || weekNumber == null) return null;
        return "Week " + weekNumber;
    }

    public String getProgramDayDisplay() {
        if (!isPartOfProgram() || dayOfWeek == null) return null;
        return "Day " + dayOfWeek + " (" + getDayOfWeekName() + ")";
    }
}