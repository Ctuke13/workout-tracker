package com.chidituke.workout_tracker.dto.response.scheduled_workouts;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for ScheduledWorkout entity
 * ✅ UPDATED: Now supports American-optimized workout tracking with:
 * - Target-prefixed field naming for consistency
 * - Weight units (kg/lbs) with US defaults
 * - Enhanced configuration display methods
 * - Comprehensive workout tracking support
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduledWorkoutResponse {
    // =============================================================================
    // CORE IDENTIFICATION & SCHEDULING
    // =============================================================================
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate scheduledDate;

    private String status; // SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, SKIPPED, RESCHEDULED

    // =============================================================================
    // ✅ UPDATED: EXERCISE CONFIGURATION FIELDS (American-Optimized)
    // =============================================================================

    // Strength exercise fields (with consistent "target" naming)
    private Integer targetSets;              // ✅ Consistent naming
    private String targetReps;               // ✅ Keeping as String for backend compatibility
    private Double targetWeight;             // ✅ Renamed for clarity
    private String targetWeightUnit;         // ✅ NEW: 'kg' or 'lbs' support
    private Integer restSeconds;             // Rest doesn't need "target" prefix
    private String tempo;                    // Tempo doesn't need "target" prefix
    private Integer targetRpe;               // Target RPE (1-10)

    // Cardio exercise fields
    private Integer targetDurationMinutes;   // Duration-based tracking
    private Double targetDistanceKm;         // Distance-based tracking
    private Double targetPace;               // Pace tracking (min/km)

    // Isometric exercise fields
    private Integer holdDurationSeconds;     // Hold-based tracking

    // =============================================================================
    // PROGRAM CONTEXT & SCHEDULING
    // =============================================================================
    private Integer weekNumber;              // Which week of the program
    private Integer dayOfWeek;               // 1=Monday, 7=Sunday

    // =============================================================================
    // USER CUSTOMIZATIONS & NOTES
    // =============================================================================
    private String customNotes;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reminderTime;

    private Integer estimatedDurationMinutes;

    // =============================================================================
    // COMPLETION & TRACKING
    // =============================================================================
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime completedAt;

    // =============================================================================
    // METADATA & AUDIT FIELDS
    // =============================================================================
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private Long createdByUserId;            // For coach-assigned workouts

    // =============================================================================
    // RELATED ENTITY INFORMATION
    // =============================================================================
    private WorkoutPlanInfo workoutPlan;
    private UserInfo user;
    private WorkoutProgramInfo program;      // Optional - only if part of program
    private WorkoutSessionInfo completedSession; // Only if completed
    private ExerciseInfo exercise;

    // =============================================================================
    // NESTED DTOS FOR RELATED ENTITIES
    // =============================================================================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkoutPlanInfo {
        private Long id;
        private String name;
        private String description;
        private String difficulty;           // BEGINNER, INTERMEDIATE, ADVANCED
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
        private String subscriptionTier;     // FREE, PLUS, PRO
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExerciseInfo {
        private Long id;
        private String name;
        private String emoji;
        private String description;
        private String exerciseType;           // STRENGTH, CARDIO, FLEXIBILITY, etc.
        private String difficultyLevel;       // BEGINNER, INTERMEDIATE, ADVANCED
        private Integer estimatedDurationMinutes;
        private Integer estimatedCalories;
        private List<String> targetMuscleGroups;
        private List<String> equipmentRequired;
        private String videoUrl;
        private List<String> benefits;
        private List<String> tips;

        //  These are the flags that fix the frontend exercise type detection!
        private Boolean isCardio;
        private Boolean isIsometric;
        private String workoutTrackingMode;   // TIME_BASED, HOLD_BASED, REP_BASED

        // Additional metadata
        private Double averageRating;
        private Integer totalRatings;
        private Integer usageCount;
        private Boolean isFromVerifiedSource;

        // Convenience methods for frontend
        @JsonIgnore
        public boolean isCardioExercise() {
            return isCardio != null && isCardio;
        }

        @JsonIgnore
        public boolean isIsometricExercise() {
            return isIsometric != null && isIsometric;
        }

        @JsonIgnore
        public boolean isStrengthExercise() {
            return !isCardioExercise() && !isIsometricExercise();
        }

        @JsonIgnore
        public String getTrackingModeForFrontend() {
            if (isCardioExercise()) return "cardio";
            if (isIsometricExercise()) return "isometric";
            return "strength";
        }
    }


    // =============================================================================
    // BUSINESS LOGIC METHODS (Status & State Checking)
    // =============================================================================

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
        return "SCHEDULED".equals(status) && (isToday() || isOverdue());
    }

    public boolean canBeCancelled() {
        return "SCHEDULED".equals(status) || "IN_PROGRESS".equals(status);
    }

    public boolean canBeRescheduled() {
        return "SCHEDULED".equals(status) &&
                scheduledDate != null &&
                scheduledDate.isAfter(LocalDate.now());
    }

    // Status checking methods
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

    // Context checking methods
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

    // =============================================================================
    // ✅ ENHANCED: WEIGHT CONVERSION METHODS (American-Optimized)
    // =============================================================================

    /**
     * Get target weight converted to kg (for internal calculations)
     */
    @JsonIgnore
    public Double getTargetWeightInKg() {
        if (targetWeight == null) return null;
        if ("lbs".equals(targetWeightUnit)) {
            return targetWeight * 0.453592;
        }
        return targetWeight; // Already in kg
    }

    /**
     * Get target weight converted to lbs (for American users)
     */
    @JsonIgnore
    public Double getTargetWeightInLbs() {
        if (targetWeight == null) return null;
        if ("kg".equals(targetWeightUnit)) {
            return targetWeight * 2.20462;
        }
        return targetWeight; // Already in lbs
    }

    /**
     * Get target weight in user's preferred unit with formatted string
     */
    @JsonIgnore
    public String getFormattedWeight() {
        if (targetWeight == null) return null;
        String unit = targetWeightUnit != null ? targetWeightUnit : "lbs";

        // Format with appropriate precision based on unit
        if ("lbs".equals(unit)) {
            // Lbs typically use .5 increments (e.g., 135.5 lbs)
            return String.format("%.1f%s", targetWeight, unit);
        } else {
            // Kg typically use .25 increments (e.g., 61.25 kg)
            return String.format("%.2f%s", targetWeight, unit);
        }
    }

    /**
     * Get weight conversion hint for UI display
     */
    @JsonIgnore
    public String getWeightConversionHint() {
        if (targetWeight == null) return null;

        if ("lbs".equals(targetWeightUnit)) {
            double kg = getTargetWeightInKg();
            return String.format("≈ %.1f kg", kg);
        } else {
            double lbs = getTargetWeightInLbs();
            return String.format("≈ %.1f lbs", lbs);
        }
    }

    // =============================================================================
    // ✅ ENHANCED: CONFIGURATION DISPLAY METHODS (Prioritizes Weight)
    // =============================================================================

    /**
     * Get formatted configuration string prioritizing weight for calendar display
     */
    @JsonIgnore
    public String getFormattedConfiguration() {
        List<String> parts = new ArrayList<>();

        // Always show sets and reps first if available
        if (targetSets != null) parts.add(targetSets + " sets");
        if (targetReps != null) parts.add(targetReps + " reps");

        // ✅ PRIORITIZE: Show weight if available (American gym culture)
        if (targetWeight != null) {
            parts.add(getFormattedWeight());
        }

        // Only show rest if no weight is set (to keep display concise)
        if (targetWeight == null && restSeconds != null) {
            parts.add(restSeconds + "s rest");
        }

        return String.join(" • ", parts);
    }

    /**
     * Get comprehensive configuration string for detailed view
     */
    @JsonIgnore
    public String getDetailedConfiguration() {
        List<String> parts = new ArrayList<>();

        if (targetSets != null) parts.add(targetSets + " sets");
        if (targetReps != null) parts.add(targetReps + " reps");
        if (targetWeight != null) parts.add(getFormattedWeight());
        if (restSeconds != null) parts.add(restSeconds + "s rest");
        if (targetRpe != null) parts.add("RPE " + targetRpe);
        if (tempo != null) parts.add("Tempo: " + tempo);

        return String.join(" • ", parts);
    }

    /**
     * Get cardio-specific configuration display
     */
    @JsonIgnore
    public String getCardioConfiguration() {
        List<String> parts = new ArrayList<>();

        if (targetDurationMinutes != null) parts.add(targetDurationMinutes + " min");
        if (targetDistanceKm != null) parts.add(String.format("%.1f km", targetDistanceKm));
        if (targetPace != null) parts.add(String.format("%.2f min/km", targetPace));

        return parts.isEmpty() ? "Cardio workout" : String.join(" • ", parts);
    }

    /**
     * Get isometric-specific configuration display
     */
    @JsonIgnore
    public String getIsometricConfiguration() {
        List<String> parts = new ArrayList<>();

        if (targetSets != null) parts.add(targetSets + " sets");
        if (holdDurationSeconds != null) parts.add(holdDurationSeconds + "s holds");
        if (restSeconds != null) parts.add(restSeconds + "s rest");

        return parts.isEmpty() ? "Isometric holds" : String.join(" • ", parts);
    }

    // =============================================================================
    // DISPLAY METHODS FOR UI
    // =============================================================================

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

    // =============================================================================
    // DATE & TIME UTILITY METHODS
    // =============================================================================

    public String getDayOfWeekName() {
        if (dayOfWeek == null) return null;
        String[] days = {"", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        return dayOfWeek >= 1 && dayOfWeek <= 7 ? days[dayOfWeek] : null;
    }

    public long getDaysUntilScheduled() {
        if (scheduledDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), scheduledDate);
    }

    // =============================================================================
    // PROGRAM PROGRESS UTILITY METHODS
    // =============================================================================

    public String getProgramWeekDisplay() {
        if (!isPartOfProgram() || weekNumber == null) return null;
        return "Week " + weekNumber;
    }

    public String getProgramDayDisplay() {
        if (!isPartOfProgram() || dayOfWeek == null) return null;
        return "Day " + dayOfWeek + " (" + getDayOfWeekName() + ")";
    }

    // =============================================================================
    // ✅ NEW: AMERICAN GYM CULTURE HELPERS
    // =============================================================================

    /**
     * Check if this is a "big 3" powerlifting exercise (common in American gyms)
     */
    @JsonIgnore
    public boolean isPowerliftingExercise() {
        if (workoutPlan == null || workoutPlan.getName() == null) return false;
        String name = workoutPlan.getName().toLowerCase();
        return name.contains("bench press") || name.contains("squat") || name.contains("deadlift");
    }

    /**
     * Get suggested progression for American users (lbs-based)
     */
    @JsonIgnore
    public String getProgressionSuggestion() {
        if (targetWeight == null) return null;

        double currentWeight = "lbs".equals(targetWeightUnit) ? targetWeight : getTargetWeightInLbs();
        double progression = isPowerliftingExercise() ? 5.0 : 2.5; // 5lbs for big lifts, 2.5lbs for accessories

        return String.format("Next: %.1f lbs (+%.1f)", currentWeight + progression, progression);
    }

    /**
     * Check if weight follows standard American gym increments
     */
    @JsonIgnore
    public boolean isStandardAmericanWeight() {
        if (targetWeight == null) return false;

        double weightInLbs = "lbs".equals(targetWeightUnit) ? targetWeight : getTargetWeightInLbs();

        // Check if it's a multiple of 2.5 lbs (standard American increment)
        return Math.abs(weightInLbs % 2.5) < 0.1;
    }
}