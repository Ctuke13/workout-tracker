package com.chidituke.workout_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Entity
@Table(name = "scheduled_workouts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "scheduled_date", "workout_plan_id"}))
public class ScheduledWorkout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_id", nullable = false)
    @NotNull(message = "Workout plan is required")
    private WorkoutPlan workoutPlan;

    @Column(name = "scheduled_date", nullable = false)
    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScheduleStatus status = ScheduleStatus.SCHEDULED;

    // Optional program context (for multi-week programs)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private WorkoutProgram program;

    @Column(name = "week_number")
    private Integer weekNumber; // Which week of the program

    @Column(name = "day_of_week")
    private Integer dayOfWeek; // 1=Monday, 7=Sunday

    // User customizations
    @Column(name = "custom_notes", columnDefinition = "TEXT")
    private String customNotes;

    @Column(name = "reminder_time")
    private LocalDateTime reminderTime;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    // Completion tracking
    @OneToOne(mappedBy = "scheduledWorkout", cascade = CascadeType.ALL)
    private WorkoutSession completedSession;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Metadata
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by_user_id")
    private Long createdByUserId; // For coach-assigned workouts

    // ENUMS
    public enum ScheduleStatus {
        SCHEDULED,      // Future workout
        IN_PROGRESS,    // Currently doing workout
        COMPLETED,      // Finished workout
        CANCELLED,      // User cancelled
        SKIPPED,        // Missed/skipped workout
        RESCHEDULED     // Moved to different date
    }

    // BUSINESS LOGIC METHODS

    /**
     * Check if user can schedule workout this far in advance
     */
    public boolean canSchedule(User user, LocalDate date) {
        if (user.getSubscriptionTier() == null) {
            return false;
        }

        switch (user.getSubscriptionTier()) {
            case FREE:
                long daysOut = ChronoUnit.DAYS.between(LocalDate.now(), date);
                return daysOut <= 7; // Free users: 7 days max
            case PLUS:
                long weeksOut = ChronoUnit.WEEKS.between(LocalDate.now(), date);
                return weeksOut <= 8; // Plus users: 8 weeks max
            case PRO:
                return true; // Pro users: unlimited
            default:
                return false;
        }
    }

    public boolean isOverdue() {
        return status == ScheduleStatus.SCHEDULED &&
                scheduledDate.isBefore(LocalDate.now());
    }

    public boolean isToday() {
        return scheduledDate.equals(LocalDate.now());
    }

    public boolean isUpcoming() {
        return status == ScheduleStatus.SCHEDULED &&
                scheduledDate.isAfter(LocalDate.now());
    }

    public boolean canBeStarted() {
        return status == ScheduleStatus.SCHEDULED &&
                (isToday() || isOverdue());
    }

    public boolean canBeCancelled() {
        return status == ScheduleStatus.SCHEDULED ||
                status == ScheduleStatus.IN_PROGRESS;
    }

    public boolean canBeRescheduled() {
        return status == ScheduleStatus.SCHEDULED &&
                scheduledDate.isAfter(LocalDate.now());
    }

    /**
     * Start the scheduled workout
     */
    public void startWorkout() {
        if (!canBeStarted()) {
            throw new IllegalStateException("Cannot start workout in current state: " + status);
        }
        this.status = ScheduleStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Complete the scheduled workout
     */
    public void completeWorkout(WorkoutSession session) {
        if (status != ScheduleStatus.IN_PROGRESS) {
            throw new IllegalStateException("Workout must be in progress to complete");
        }
        this.status = ScheduleStatus.COMPLETED;
        this.completedSession = session;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // Set back-reference
        session.setScheduledWorkout(this);
    }

    /**
     * Cancel the scheduled workout
     */
    public void cancelWorkout() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Cannot cancel workout in current state: " + status);
        }
        this.status = ScheduleStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Reschedule the workout to a new date
     */
    public void reschedule(LocalDate newDate) {
        if (!canBeRescheduled()) {
            throw new IllegalStateException("Cannot reschedule workout in current state: " + status);
        }

        if (!canSchedule(user, newDate)) {
            throw new IllegalArgumentException("Cannot schedule workout that far in advance");
        }

        this.scheduledDate = newDate;
        this.status = ScheduleStatus.RESCHEDULED;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDisplayStatus() {
        if (isOverdue() && status == ScheduleStatus.SCHEDULED) {
            return "OVERDUE";
        }
        return status.name();
    }

    public long getDaysUntilScheduled() {
        return ChronoUnit.DAYS.between(LocalDate.now(), scheduledDate);
    }

    // JPA LIFECYCLE METHODS
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Set estimated duration from workout plan if not set
        if (estimatedDurationMinutes == null && workoutPlan != null) {
            estimatedDurationMinutes = workoutPlan.getEstimatedDurationMinutes();
        }

        // Validate scheduling constraints
        if (!canSchedule(user, scheduledDate)) {
            throw new IllegalArgumentException("Cannot schedule workout that far in advance for user's subscription tier");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}