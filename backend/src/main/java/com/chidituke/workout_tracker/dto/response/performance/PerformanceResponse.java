package com.chidituke.workout_tracker.dto.response.performance;

import com.chidituke.workout_tracker.model.workout.PerformanceRecord;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enhanced Performance Response DTO with comprehensive performance metrics
 * Aligned with enhanced PerformanceRecord entity and WorkoutSession integration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields from JSON
public class PerformanceResponse {

    // ==============================================
    // CORE IDENTIFICATION
    // ==============================================

    private Long id;
    private Long workoutSessionId;
    private Long exerciseId;
    private String exerciseName;
    private String exerciseCategory;
    private Boolean isCardioExercise;
    private Boolean isIsometricExercise;
    private String workoutTrackingMode;

    // ==============================================
    // WORKOUT CONTEXT
    // ==============================================

    private LocalDate workoutDate;
    private String workoutName;
    private String workoutCategory;
    private Integer setNumber;

    // Enhanced workout session context
    private String sessionStatus; // PLANNED, IN_PROGRESS, COMPLETED, etc.
    private Double sessionCompletionPercentage;
    private Integer totalSetsInSession;
    private Integer currentSetInSession;

    // ==============================================
    // BASIC PERFORMANCE METRICS
    // ==============================================

    private Integer reps;
    private Double weight;
    private Double volume;

    // ==============================================
    // CARDIO METRICS
    // ==============================================

    private Integer durationMinutes;
    private Double durationSeconds;
    private Double totalDurationSeconds; // total duration
    private Double distanceKm;
    private Integer caloriesBurned;
    private Double pace; // minutes per km
    private Double speed; // km per hour

    // ==============================================
    //  REST TIME AND SET TIMING TRACKING
    // ==============================================

    private Integer restTimeBeforeSetSeconds; // Rest time before this set
    private LocalDateTime setStartTime; // When the set started
    private LocalDateTime setEndTime; // When the set ended
    private Integer setDurationSeconds; // How long the set took
    private Double averageRestTimeSeconds; // Average rest for this exercise
    private String restTimeEfficiency; // OPTIMAL, TOO_SHORT, TOO_LONG

    // ==============================================
    // EXERCISE COMPLETION TRACKING
    // ==============================================

    private Boolean isExerciseCompleted; // Whether the entire exercise is done
    private String exerciseCompletionNotes; // Notes about exercise completion
    private Integer totalSetsCompleted; // Total sets completed for this exercise
    private Integer totalSetsPlanned; // Total sets planned for this exercise
    private Double exerciseCompletionPercentage; // % of exercise completed

    // ==============================================
    // TARGET COMPARISON FIELDS
    // ==============================================

    private Integer targetRepsPlanned; // Target reps for this set
    private Double targetWeightPlanned; // Target weight for this set
    private String performanceVsTarget; // NOT_SET, EXCEEDED, MET, BELOW, STRUGGLED
    private Double targetAchievementPercentage; // % of target achieved
    private Boolean metTargets; // Whether targets were met
    private String targetComparisonSummary; // Summary of target vs actual

    // ==============================================
    // ADVANCED PERFORMANCE METRICS
    // ==============================================

    private Integer perceivedExertion; // RPE 1-10
    private String intensityLevel; // LOW, MODERATE, HIGH, MAXIMUM
    private Integer formRating; // 1-10
    private Integer restSeconds; // Use restTimeBeforeSetSeconds instead
    private String tempo; // "3-1-2-1"

    // ==============================================
    // SPECIALIZED EXERCISE METRICS
    // ==============================================

    private Integer holdDurationSeconds;
    private Integer balanceScore;
    private Double jumpHeightCm;
    private Double powerOutputWatts;

    // ==============================================
    // PROFESSIONAL TRAINING CONTEXT
    // ==============================================

    private Long assignedByTrainerId;
    private String trainerName;
    private Integer targetReps; //  DEPRECATED: Use targetRepsPlanned instead
    private Double targetWeight; //  DEPRECATED: Use targetWeightPlanned instead
    private Double targetVolume; //  target weight × target reps
    private PerformanceRecord.AchievementStatus achievementStatus;
    private Double efficiencyPercentage; //  actual vs target performance

    // ==============================================
    // ✅ NEW: ENHANCED PERFORMANCE ANALYTICS
    // ==============================================

    private Double performanceScore; // ✅ CALCULATED: 0-100 overall score
    private Boolean isPersonalRecord; // ✅ CALCULATED: if this beats previous best
    private Boolean exceededTargets; // ✅ CALCULATED: if performance exceeded targets
    private String performanceGrade; // ✅ CALCULATED: A, B, C, D, F based on score
    private String performanceTrend; // ✅ CALCULATED: IMPROVING, STABLE, DECLINING
    private Integer performanceRank; // ✅ CALCULATED: Rank compared to previous performances

    // Advanced calculated metrics
    private Double workloadIndex; // ✅ CALCULATED: RPE × Volume for strength, RPE × Duration for cardio
    private Double fatigueIndex; // ✅ CALCULATED: Based on RPE progression through sets
    private String recoveryRecommendation; // ✅ CALCULATED: Based on performance and fatigue
    private Double trainingStressScore; // ✅ CALCULATED: Overall stress from this performance

    // ==============================================
    // CONTEXT AND NOTES
    // ==============================================

    private String notes;
    private String equipmentUsed;
    private String workoutEnvironment;

    // Enhanced context
    private String muscleGroupsWorked; // Comma-separated list
    private String exerciseDifficulty; // BEGINNER, INTERMEDIATE, ADVANCED
    private Boolean wasAssistedSet; // Whether assistance was provided
    private Boolean wasDropSet; // Whether this was a drop set
    private Boolean wasSuperSet; // Whether this was part of a superset

    // ==============================================
    // AUDIT INFORMATION
    // ==============================================

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Enhanced timing
    private LocalDateTime recordedAt; // When the performance was recorded
    private LocalDateTime performedAt; // When the actual performance happened
    private Long recordingDelaySeconds; // Delay between performance and recording

    // ==============================================
    // HELPER METHODS FOR ENHANCED DISPLAY
    // ==============================================

    /**
     * Get target vs actual comparison display with enhanced details
     */
    public String getEnhancedTargetComparisonDisplay() {
        if (performanceVsTarget == null || "NOT_SET".equals(performanceVsTarget)) {
            return "No target set";
        }

        String emoji = switch (performanceVsTarget) {
            case "EXCEEDED" -> "🚀";
            case "MET" -> "✅";
            case "BELOW" -> "⚠️";
            case "STRUGGLED" -> "❌";
            default -> "❓";
        };

        String percentage = targetAchievementPercentage != null ?
                String.format(" (%.1f%%)", targetAchievementPercentage) : "";

        return String.format("%s %s%s", emoji, performanceVsTarget.toLowerCase().replace("_", " "), percentage);
    }

    /**
     * Get rest time display with efficiency indicator
     */
    public String getRestTimeDisplay() {
        if (restTimeBeforeSetSeconds == null) {
            return "No rest data";
        }

        int minutes = restTimeBeforeSetSeconds / 60;
        int seconds = restTimeBeforeSetSeconds % 60;
        String timeStr = minutes > 0 ?
                String.format("%dm %ds", minutes, seconds) :
                String.format("%ds", seconds);

        String efficiency = restTimeEfficiency != null ?
                String.format(" (%s)", restTimeEfficiency.toLowerCase().replace("_", " ")) : "";

        return timeStr + efficiency;
    }

    /**
     * Get set timing display (start to end)
     */
    public String getSetTimingDisplay() {
        if (setStartTime == null || setEndTime == null) {
            return "No timing data";
        }

        long durationSecs = java.time.Duration.between(setStartTime, setEndTime).getSeconds();

        if (durationSecs < 60) {
            return String.format("%ds", durationSecs);
        } else {
            return String.format("%dm %ds", durationSecs / 60, durationSecs % 60);
        }
    }

    /**
     * Get exercise completion progress display
     */
    public String getExerciseProgressDisplay() {
        if (totalSetsCompleted == null || totalSetsPlanned == null) {
            return String.format("Set %d", setNumber != null ? setNumber : 1);
        }

        return String.format("Set %d/%d (%s)",
                totalSetsCompleted,
                totalSetsPlanned,
                isExerciseCompleted ? "Complete" : "In Progress");
    }

    /**
     * Get workload index display with interpretation
     */
    public String getWorkloadDisplay() {
        if (workloadIndex == null) {
            return "No workload data";
        }

        String interpretation;
        if (workloadIndex < 50) interpretation = "Light";
        else if (workloadIndex < 100) interpretation = "Moderate";
        else if (workloadIndex < 150) interpretation = "Heavy";
        else interpretation = "Very Heavy";

        return String.format("%.1f (%s)", workloadIndex, interpretation);
    }

    /**
     * Get performance trend display with emoji
     */
    public String getPerformanceTrendDisplay() {
        if (performanceTrend == null) {
            return "No trend data";
        }

        String emoji = switch (performanceTrend) {
            case "IMPROVING" -> "📈";
            case "STABLE" -> "➡️";
            case "DECLINING" -> "📉";
            default -> "❓";
        };

        return String.format("%s %s", emoji, performanceTrend.toLowerCase());
    }

    /**
     * Get fatigue indicator display
     */
    public String getFatigueDisplay() {
        if (fatigueIndex == null) {
            return "No fatigue data";
        }

        String level;
        String emoji;
        if (fatigueIndex < 0.3) {
            level = "Low";
            emoji = "🟢";
        } else if (fatigueIndex < 0.6) {
            level = "Moderate";
            emoji = "🟡";
        } else if (fatigueIndex < 0.8) {
            level = "High";
            emoji = "🟠";
        } else {
            level = "Very High";
            emoji = "🔴";
        }

        return String.format("%s %s (%.1f)", emoji, level, fatigueIndex);
    }

    /**
     * Get training stress score display
     */
    public String getTrainingStressDisplay() {
        if (trainingStressScore == null) {
            return "No stress data";
        }

        return String.format("%.1f TSS", trainingStressScore);
    }

    /**
     * Check if this performance represents a new personal record
     */
    public boolean isNewPersonalRecord() {
        return isPersonalRecord != null && isPersonalRecord;
    }

    /**
     * Get comprehensive performance summary for the set
     */
    public String getComprehensivePerformanceSummary() {
        StringBuilder summary = new StringBuilder();

        // Basic performance
        if (isStrengthTraining()) {
            summary.append(String.format("%d reps × %.1f kg", reps, weight));
            if (volume != null) {
                summary.append(String.format(" (%.1f kg total)", volume));
            }
        }

        if (isCardio()) {
            if (summary.length() > 0) summary.append(" | ");
            if (distanceKm != null) {
                summary.append(String.format("%.2f km", distanceKm));
            }
            if (totalDurationSeconds != null) {
                summary.append(String.format(" in %s", getDurationDisplay()));
            }
        }

        // Target achievement
        if (performanceVsTarget != null && !"NOT_SET".equals(performanceVsTarget)) {
            summary.append(" | ").append(getEnhancedTargetComparisonDisplay());
        }

        // RPE and form
        if (perceivedExertion != null) {
            summary.append(String.format(" | RPE: %d", perceivedExertion));
        }
        if (formRating != null) {
            summary.append(String.format(" | Form: %d/10", formRating));
        }

        // Personal record indicator
        if (isNewPersonalRecord()) {
            summary.append(" | 🏆 PR!");
        }

        return summary.length() > 0 ? summary.toString() : "Performance data";
    }

    /**
     * Get exercise type display
     */
    public String getExerciseTypeDisplay() {
        if (isCardioExercise != null && isCardioExercise) {
            return "Cardio";
        }
        if (isIsometricExercise != null && isIsometricExercise) {
            return "Isometric";
        }
        return "Strength";
    }

    /**
     * Check if this is an isometric exercise
     */
    public boolean isIsometric() {
        return isIsometricExercise != null && isIsometricExercise;
    }

    /**
     * Get workout tracking mode display
     */
    public String getTrackingModeDisplay() {
        if (workoutTrackingMode == null) {
            return "Standard";
        }
        return switch (workoutTrackingMode) {
            case "TIME_BASED" -> "Time-based";
            case "HOLD_BASED" -> "Hold-based";
            case "REP_BASED" -> "Rep-based";
            default -> workoutTrackingMode;
        };
    }

    // ==============================================
    // EXISTING HELPER METHODS (KEPT FOR COMPATIBILITY)
    // ==============================================

    public String getVolumeDisplay() {
        if (volume == null || volume == 0) {
            return "No volume data";
        }
        return String.format("%.1f kg", volume);
    }

    public String getPaceDisplay() {
        if (pace == null) {
            return "No pace data";
        }
        int minutes = (int) Math.floor(pace);
        int seconds = (int) ((pace - minutes) * 60);
        return String.format("%d:%02d /km", minutes, seconds);
    }

    public String getSpeedDisplay() {
        if (speed == null) {
            return "No speed data";
        }
        return String.format("%.1f km/h", speed);
    }

    public String getDurationDisplay() {
        if (totalDurationSeconds == null || totalDurationSeconds == 0) {
            return "No duration data";
        }
        int minutes = (int) (totalDurationSeconds / 60);
        int seconds = (int) (totalDurationSeconds % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    public String getWeightDisplay() {
        if (weight == null) {
            return "Bodyweight";
        }
        return String.format("%.1f kg", weight);
    }

    public String getRepsDisplay() {
        if (reps == null) {
            return "N/A";
        }
        return reps + (reps == 1 ? " rep" : " reps");
    }

    public String getRpeDisplay() {
        if (perceivedExertion == null) {
            return "Not rated";
        }
        String description = switch (perceivedExertion) {
            case 1, 2 -> "Very Easy";
            case 3, 4 -> "Easy";
            case 5, 6 -> "Moderate";
            case 7, 8 -> "Hard";
            case 9 -> "Very Hard";
            case 10 -> "Maximum Effort";
            default -> "Unknown";
        };
        return String.format("%d/10 (%s)", perceivedExertion, description);
    }

    public String getFormRatingDisplay() {
        if (formRating == null) {
            return "Not rated";
        }
        String description = switch (formRating) {
            case 1, 2, 3 -> "Poor";
            case 4, 5 -> "Fair";
            case 6, 7 -> "Good";
            case 8, 9 -> "Excellent";
            case 10 -> "Perfect";
            default -> "Unknown";
        };
        return String.format("%d/10 (%s)", formRating, description);
    }

    public String getAchievementDisplay() {
        if (achievementStatus == null) {
            return "Not set";
        }
        return achievementStatus.getDisplayName();
    }

    public String getPerformanceScoreDisplay() {
        if (performanceScore == null) {
            return "Not calculated";
        }
        return String.format("%.1f/100 (%s)", performanceScore, performanceGrade != null ? performanceGrade : "N/A");
    }

    public String getEfficiencyDisplay() {
        if (efficiencyPercentage == null) {
            return "Not calculated";
        }
        return String.format("%.1f%%", efficiencyPercentage);
    }

    public String getTargetComparisonDisplay() {
        if (targetRepsPlanned == null || targetWeightPlanned == null || reps == null || weight == null) {
            return "No target set";
        }

        double targetVol = targetRepsPlanned * targetWeightPlanned;
        double actualVol = reps * weight;
        double percentage = (actualVol / targetVol) * 100;

        String status = percentage >= 100 ? "✅" : percentage >= 90 ? "⚠️" : "❌";
        return String.format("%s %.1f%% of target", status, percentage);
    }

    public boolean isStrengthTraining() {
        return reps != null && weight != null;
    }

    public boolean isCardio() {
        return durationMinutes != null || durationSeconds != null || distanceKm != null;
    }

    public boolean hasAdvancedMetrics() {
        return perceivedExertion != null || formRating != null ||
                restTimeBeforeSetSeconds != null || tempo != null;
    }

    public boolean hasProfessionalContext() {
        return assignedByTrainerId != null || targetRepsPlanned != null ||
                targetWeightPlanned != null || achievementStatus != null;
    }

    public String getPerformanceCategory() {
        if (isStrengthTraining() && isCardio()) {
            return "MIXED";
        } else if (isStrengthTraining()) {
            return "STRENGTH";
        } else if (isCardio()) {
            return "CARDIO";
        } else if (holdDurationSeconds != null || balanceScore != null) {
            return "FLEXIBILITY_BALANCE";
        } else if (jumpHeightCm != null || powerOutputWatts != null) {
            return "PLYOMETRIC_POWER";
        } else {
            return "OTHER";
        }
    }

    public String getWorkoutQuality() {
        if (performanceScore == null) {
            return "UNKNOWN";
        }

        if (performanceScore >= 90) return "EXCELLENT";
        if (performanceScore >= 80) return "GOOD";
        if (performanceScore >= 70) return "FAIR";
        if (performanceScore >= 60) return "POOR";
        return "NEEDS_IMPROVEMENT";
    }

    public String getPerformanceSummary() {
        return getComprehensivePerformanceSummary();
    }
}