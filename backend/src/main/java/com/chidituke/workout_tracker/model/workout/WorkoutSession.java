package com.chidituke.workout_tracker.model.workout;

import com.chidituke.workout_tracker.model.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "workout_sessions")
public class WorkoutSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_id", nullable = false)
    private WorkoutPlan workoutPlan;

    @OneToMany(mappedBy = "workoutSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PerformanceRecord> performanceRecords = new ArrayList<>();

    @Column(name = "total_exercises_planned")
    private Integer totalExercisesPlanned;

    @Column(name = "total_exercises_completed")
    private Integer totalExercisesCompleted;

    @Column(name = "completion_percentage")
    private Double completionPercentage; // Calculated field

    @Column(name = "session_status")
    @Enumerated(EnumType.STRING)
    private SessionStatus sessionStatus = SessionStatus.IN_PROGRESS;


    @Column(name = "total_duration_minutes")
    private Integer totalDurationMinutes;

    @Column(name = "estimated_calories")
    private Integer estimatedCalories;

    @Column(name = "difficulty_rating")
    @Min(value = 1, message = "Difficulty rating must be between 1 and 10")
    @Max(value = 10, message = "Difficulty rating must be between 1 and 10")
    private Integer difficultyRating;

    @Column(name = "overall_effort")
    @DecimalMin(value = "1.0", message = "Effort must be between 1 and 10")
    @DecimalMax(value = "10.0", message = "Effort must be between 1 and 10")
    private Double overallEffort;

    @Enumerated(EnumType.STRING)
    @Column(name = "mood")
    private WorkoutMood mood;

    @Enumerated(EnumType.STRING)
    @Column(name = "location")
    private WorkoutLocation location;

    // Program integration - NOTE: WorkoutProgram class needs to be created
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "workout_program_id")
    private WorkoutProgram program;

    @Column(name = "week_number")
    private Integer weekNumber;

    // Social features
    @Column(name = "is_shared")
    private Boolean isShared = false;

    // Timestamps
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "workout_feedback", columnDefinition = "TEXT")
    private String workoutFeedback;

    @Column(name = "performance_summary", columnDefinition = "TEXT")
    private String performanceSummary;

    // Link to scheduled workout (if this session was from a scheduled workout)
    @OneToOne
    @JoinColumn(name = "scheduled_workout_id")
    private ScheduledWorkout scheduledWorkout;

    // ==================== ENHANCED CALORIE TRACKING ====================

    @Column(name = "total_calories_calculated")
    private Integer totalCaloriesCalculated;

    @Column(name = "actual_calories_burned")
    private Integer actualCaloriesBurned;

    @Column(name = "calorie_calculation_status", length = 30)
    private String calorieCalculationStatus; // "NOT_CALCULATED", "CALCULATED", etc.

    @Column(name = "calorie_accuracy_rating")
    @Min(value = 1, message = "Rating must be between 1-5")
    @Max(value = 5, message = "Rating must be between 1-5")
    private Integer calorieAccuracyRating;

    @Column(name = "user_reported_calories")
    private Integer userReportedCalories;

    public boolean isFromScheduledWorkout() {
        return scheduledWorkout != null;
    }

    public boolean wasScheduledToday() {
        return isFromScheduledWorkout() && scheduledWorkout.isToday();
    }

    public boolean wasOverdue() {
        return isFromScheduledWorkout() && scheduledWorkout.isOverdue();
    }


    // ENUMS
    public enum WorkoutMood {
        ENERGETIC, TIRED, MOTIVATED, FOCUSED,
        STRESSED, RELAXED, PUMPED, SLUGGISH
    }

    public enum WorkoutLocation {
        HOME, GYM, PARK, OFFICE, HOTEL,
        BEACH, TRAIL, STUDIO, OTHER
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    //===================== HELPER METHODS ============================

    /**
     * Add a performance record to this session
     */
    public void addPerformanceRecord(PerformanceRecord record) {
        if (performanceRecords == null) {
            performanceRecords = new ArrayList<>();
        }
        performanceRecords.add(record);
        record.setWorkoutSession(this);
    }

    /**
     * Calculate and update completion percentage
     */
    public void updateCompletionPercentage() {
        if (totalExercisesPlanned == null || totalExercisesPlanned == 0) {
            this.completionPercentage = 0.0;
            return;
        }

        if (totalExercisesCompleted == null) {
            this.totalExercisesCompleted = 0;
        }

        this.completionPercentage = (totalExercisesCompleted.doubleValue() / totalExercisesPlanned) * 100.0;

        // Update session status based on completion
        updateSessionStatus();
    }

    /**
     * Update session status based on completion percentage
     */
    private void updateSessionStatus() {
        if (completionPercentage == null) {
            this.sessionStatus = SessionStatus.PLANNED;
        } else if (completionPercentage >= 100.0) {
            this.sessionStatus = SessionStatus.COMPLETED;
        } else if (completionPercentage > 0.0) {
            this.sessionStatus = SessionStatus.PARTIALLY_COMPLETED;
        } else {
            this.sessionStatus = SessionStatus.IN_PROGRESS;
        }
    }

    /**
     * Mark an exercise as completed
     */
    public void completeExercise() {
        if (totalExercisesCompleted == null) {
            totalExercisesCompleted = 0;
        }
        totalExercisesCompleted++;
        updateCompletionPercentage();
    }

    /**
     * Get completion status description
     */
    public String getCompletionStatusDescription() {
        if (completionPercentage == null || completionPercentage == 0) {
            return "Not started";
        } else if (completionPercentage >= 100) {
            return "Completed";
        } else {
            return String.format("%.0f%% complete (%d/%d exercises)",
                    completionPercentage,
                    totalExercisesCompleted,
                    totalExercisesPlanned);
        }
    }

    /**
     * Check if workout is partially completed
     */
    public boolean isPartiallyCompleted() {
        return sessionStatus == SessionStatus.PARTIALLY_COMPLETED;
    }

    /**
     * Check if workout is fully completed
     */
    public boolean isFullyCompleted() {
        return sessionStatus == SessionStatus.COMPLETED;
    }

    /**
     * Get exercise completion summary
     */
    public Map<String, Integer> getCompletionSummary() {
        Map<String, Integer> summary = new HashMap<>();
        summary.put("planned", totalExercisesPlanned != null ? totalExercisesPlanned : 0);
        summary.put("completed", totalExercisesCompleted != null ? totalExercisesCompleted : 0);
        summary.put("remaining", (totalExercisesPlanned != null ? totalExercisesPlanned : 0) -
                (totalExercisesCompleted != null ? totalExercisesCompleted : 0));
        return summary;
    }

    // ==================== SOCIAL SHARING METHODS ====================

    /**
     * Check if this workout can be shared to social feed
     * Workouts older than 30 days cannot be shared
     */
    public boolean canBeSharedToFeed() {
        if (date == null) return false;
        return date.isAfter(LocalDate.now().minusDays(30));
    }

    /**
     * Check if this workout has been shared to social feed
     * This would typically be checked via the SocialPost repository
     */
    public boolean hasBeenSharedToFeed() {
        // Note: This would be implemented by checking SocialPost table
        // Example: return socialPostRepository.existsByWorkoutSession(this);
        // For now, return false as placeholder
        return false;
    }

    /**
     * Get sharing eligibility status
     */
    public SharingEligibility getSharingEligibility() {
        if (hasBeenSharedToFeed()) {
            return SharingEligibility.ALREADY_SHARED;
        }

        if (!canBeSharedToFeed()) {
            return SharingEligibility.TOO_OLD;
        }

        if (date.isAfter(LocalDate.now())) {
            return SharingEligibility.FUTURE_DATE;
        }

        return SharingEligibility.ELIGIBLE;
    }

    /**
     * Get workout summary for social sharing
     */
    public String getWorkoutSummaryForSharing() {
        StringBuilder summary = new StringBuilder();

        summary.append(workoutPlan.getWorkoutName());

        if (totalDurationMinutes != null) {
            summary.append(" • ").append(totalDurationMinutes).append(" min");
        }

        if (estimatedCalories != null) {
            summary.append(" • ").append(estimatedCalories).append(" cal");
        }

        if (overallEffort != null) {
            summary.append(" • ").append(overallEffort).append("/10 effort");
        }

        return summary.toString();
    }

    /**
     * Get workout stats for detailed sharing
     */
    public WorkoutStats getWorkoutStatsForSharing() {
        return WorkoutStats.builder()
                .workoutName(workoutPlan != null ? workoutPlan.getWorkoutName() : "Unknown Workout")
                .duration(totalDurationMinutes)
                .calories(estimatedCalories)
                .effort(overallEffort)
                .difficulty(difficultyRating)
                .mood(mood)
                .location(location)
                .date(date)
                .notes(notes)
                .build();
    }

    /**
     * Check if workout was completed today
     */
    public boolean wasCompletedToday() {
        return date != null && date.equals(LocalDate.now());
    }

    /**
     * Check if workout was completed this week
     */
    public boolean wasCompletedThisWeek() {
        if (date == null) return false;
        LocalDate startOfWeek = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        return date.isAfter(startOfWeek.minusDays(1));
    }

    /**
     * Get days since workout completion
     */
    public long getDaysSinceCompletion() {
        if (date == null) return 0;
        return LocalDate.now().toEpochDay() - date.toEpochDay();
    }

    /**
     * Check if this is a personal record or achievement
     */
    public boolean isPersonalRecord() {
        // This would be determined by comparing with user's workout history
        // For now, return false as placeholder
        // Example logic: check if duration/calories are personal bests
        return false;
    }

    /**
     * Get achievement tags for this workout
     */
    public List<String> getAchievementTags() {
        List<String> achievements = new ArrayList<>();

        if (isPersonalRecord()) {
            achievements.add("Personal Record");
        }

        if (getDaysSinceCompletion() == 0) {
            achievements.add("Fresh Workout");
        }

        if (overallEffort != null && overallEffort >= 9.0) {
            achievements.add("High Intensity");
        }

        if (totalDurationMinutes != null && totalDurationMinutes >= 90) {
            achievements.add("Long Session");
        }

        if (estimatedCalories != null && estimatedCalories >= 500) {
            achievements.add("Calorie Crusher");
        }

        return achievements;
    }

    /**
     * Generate default hashtags for social sharing
     */
    public List<String> generateSharingHashtags() {
        List<String> hashtags = new ArrayList<>();

        // Basic tags
        hashtags.add("workout");
        hashtags.add("fitness");

        // Workout type specific
        if (workoutPlan != null) {
            String workoutName = workoutPlan.getWorkoutName().toLowerCase();
            if (workoutName.contains("strength") || workoutName.contains("weight")) {
                hashtags.add("strength");
            }
            if (workoutName.contains("cardio") || workoutName.contains("running")) {
                hashtags.add("cardio");
            }
            if (workoutName.contains("yoga")) {
                hashtags.add("yoga");
            }
            if (workoutName.contains("hiit")) {
                hashtags.add("hiit");
            }
        }

        // Location based
        if (location != null) {
            switch (location) {
                case GYM -> hashtags.add("gymlife");
                case HOME -> hashtags.add("homeworkout");
                case PARK -> hashtags.add("outdoorworkout");
                case BEACH -> hashtags.add("beachworkout");
            }
        }

        // Mood based
        if (mood != null) {
            switch (mood) {
                case ENERGETIC -> hashtags.add("energetic");
                case MOTIVATED -> hashtags.add("motivated");
                case PUMPED -> hashtags.add("pumped");
                case FOCUSED -> hashtags.add("focused");
            }
        }

        // Achievement based
        if (isPersonalRecord()) {
            hashtags.add("personalrecord");
        }

        return hashtags.stream().distinct().limit(8).collect(Collectors.toList());
    }

    // ==================== INNER CLASSES ====================

    @lombok.Data
    @lombok.Builder
    public static class WorkoutStats {
        private String workoutName;
        private Integer duration;
        private Integer calories;
        private Double effort;
        private Integer difficulty;
        private WorkoutMood mood;
        private WorkoutLocation location;
        private LocalDate date;
        private String notes;

        public String getFormattedDuration() {
            if (duration == null) return "Unknown duration";
            if (duration < 60) return duration + " minutes";
            int hours = duration / 60;
            int minutes = duration % 60;
            return hours + "h " + minutes + "m";
        }

        public String getEffortDescription() {
            if (effort == null) return "Effort not rated";
            if (effort >= 9.0) return "Maximum effort (" + effort + "/10)";
            if (effort >= 7.0) return "High effort (" + effort + "/10)";
            if (effort >= 5.0) return "Moderate effort (" + effort + "/10)";
            return "Light effort (" + effort + "/10)";
        }

        public String getDifficultyDescription() {
            if (difficulty == null) return "Difficulty not rated";
            if (difficulty >= 9) return "Very challenging (" + difficulty + "/10)";
            if (difficulty >= 7) return "Challenging (" + difficulty + "/10)";
            if (difficulty >= 5) return "Moderate (" + difficulty + "/10)";
            return "Easy (" + difficulty + "/10)";
        }
    }

    public enum SessionStatus {
        PLANNED("Planned but not started"),
        IN_PROGRESS("Currently working out"),
        COMPLETED("All exercises completed"),
        PARTIALLY_COMPLETED("Some exercises completed"),
        ABANDONED("Workout stopped early");

        private final String description;

        SessionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum SharingEligibility {
        ELIGIBLE("Can be shared"),
        ALREADY_SHARED("Already shared to feed"),
        TOO_OLD("Too old to share (30+ days)"),
        FUTURE_DATE("Cannot share future workouts");

        private final String description;

        SharingEligibility(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public boolean canShare() {
            return this == ELIGIBLE;
        }
    }

}