package com.chidituke.workout_tracker.dto.response.performance;

import com.chidituke.workout_tracker.model.PerformanceRecord;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enhanced Performance Response DTO with comprehensive performance metrics
 * Includes calculated fields and advanced analytics
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
    private Long workoutLogId;
    private Long exerciseId; // ✅ ADDED: Missing from original
    private String exerciseName; // ✅ ADDED: For display purposes
    private String exerciseCategory; // ✅ ADDED: For categorization
    private Boolean isCardioExercise; // ✅ ADDED: Exercise type indicator

    // ==============================================
    // WORKOUT CONTEXT
    // ==============================================

    private LocalDate workoutDate;
    private String workoutName;
    private String workoutCategory;
    private Integer setNumber;

    // ==============================================
    // BASIC PERFORMANCE METRICS
    // ==============================================

    private Integer reps;
    private Double weight;
    private Double volume; // ✅ CALCULATED: weight × reps

    // ==============================================
    // CARDIO METRICS
    // ==============================================

    private Integer durationMinutes;
    private Double durationSeconds;
    private Double totalDurationSeconds; // ✅ CALCULATED: total duration
    private Double distanceKm;
    private Integer caloriesBurned;
    private Double pace; // ✅ CALCULATED: minutes per km
    private Double speed; // ✅ CALCULATED: km per hour

    // ==============================================
    // ADVANCED PERFORMANCE METRICS
    // ==============================================

    private Integer perceivedExertion; // RPE 1-10
    private String intensityLevel; // ✅ CALCULATED: LOW, MODERATE, HIGH, MAXIMUM
    private Integer formRating; // 1-10
    private Integer restSeconds;
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
    private String trainerName; // ✅ ADDED: For display
    private Integer targetReps;
    private Double targetWeight;
    private Double targetVolume; // ✅ CALCULATED: target weight × target reps
    private PerformanceRecord.AchievementStatus achievementStatus;
    private Double efficiencyPercentage; // ✅ CALCULATED: actual vs target performance

    // ==============================================
    // PERFORMANCE ANALYTICS
    // ==============================================

    private Double performanceScore; // ✅ CALCULATED: 0-100 overall score
    private Boolean isPersonalRecord; // ✅ CALCULATED: if this beats previous best
    private Boolean exceededTargets; // ✅ CALCULATED: if performance exceeded targets
    private String performanceGrade; // ✅ CALCULATED: A, B, C, D, F based on score

    // ==============================================
    // CONTEXT AND NOTES
    // ==============================================

    private String notes;
    private String equipmentUsed;
    private String workoutEnvironment;

    // ==============================================
    // AUDIT INFORMATION
    // ==============================================

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==============================================
    // HELPER METHODS FOR DISPLAY
    // ==============================================

    /**
     * Get formatted volume display (e.g., "225.0 kg")
     */
    public String getVolumeDisplay() {
        if (volume == null || volume == 0) {
            return "No volume data";
        }
        return String.format("%.1f kg", volume);
    }

    /**
     * Get formatted pace display (e.g., "5:30 /km")
     */
    public String getPaceDisplay() {
        if (pace == null) {
            return "No pace data";
        }
        int minutes = (int) Math.floor(pace);
        int seconds = (int) ((pace - minutes) * 60);
        return String.format("%d:%02d /km", minutes, seconds);
    }

    /**
     * Get formatted speed display (e.g., "12.5 km/h")
     */
    public String getSpeedDisplay() {
        if (speed == null) {
            return "No speed data";
        }
        return String.format("%.1f km/h", speed);
    }

    /**
     * Get formatted duration display (e.g., "25:30")
     */
    public String getDurationDisplay() {
        if (totalDurationSeconds == null || totalDurationSeconds == 0) {
            return "No duration data";
        }
        int minutes = (int) (totalDurationSeconds / 60);
        int seconds = (int) (totalDurationSeconds % 60);
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Get formatted weight display with units
     */
    public String getWeightDisplay() {
        if (weight == null) {
            return "Bodyweight";
        }
        return String.format("%.1f kg", weight);
    }

    /**
     * Get formatted reps display
     */
    public String getRepsDisplay() {
        if (reps == null) {
            return "N/A";
        }
        return reps + (reps == 1 ? " rep" : " reps");
    }

    /**
     * Get RPE display with description
     */
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

    /**
     * Get form rating display with description
     */
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

    /**
     * Get achievement status display
     */
    public String getAchievementDisplay() {
        if (achievementStatus == null) {
            return "Not set";
        }
        return achievementStatus.getDisplayName();
    }

    /**
     * Get performance score display with grade
     */
    public String getPerformanceScoreDisplay() {
        if (performanceScore == null) {
            return "Not calculated";
        }
        return String.format("%.1f/100 (%s)", performanceScore, performanceGrade != null ? performanceGrade : "N/A");
    }

    /**
     * Get efficiency display as percentage
     */
    public String getEfficiencyDisplay() {
        if (efficiencyPercentage == null) {
            return "Not calculated";
        }
        return String.format("%.1f%%", efficiencyPercentage);
    }

    /**
     * Get target vs actual comparison display
     */
    public String getTargetComparisonDisplay() {
        if (targetReps == null || targetWeight == null || reps == null || weight == null) {
            return "No target set";
        }

        double targetVol = targetReps * targetWeight;
        double actualVol = reps * weight;
        double percentage = (actualVol / targetVol) * 100;

        String status = percentage >= 100 ? "✅" : percentage >= 90 ? "⚠️" : "❌";
        return String.format("%s %.1f%% of target", status, percentage);
    }

    /**
     * Check if this is a strength training performance
     */
    public boolean isStrengthTraining() {
        return reps != null && weight != null;
    }

    /**
     * Check if this is a cardio performance
     */
    public boolean isCardio() {
        return durationMinutes != null || durationSeconds != null || distanceKm != null;
    }

    /**
     * Check if this performance has advanced metrics
     */
    public boolean hasAdvancedMetrics() {
        return perceivedExertion != null || formRating != null ||
                restSeconds != null || tempo != null;
    }

    /**
     * Check if this performance has professional context
     */
    public boolean hasProfessionalContext() {
        return assignedByTrainerId != null || targetReps != null ||
                targetWeight != null || achievementStatus != null;
    }

    /**
     * Get performance category for analytics
     */
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

    /**
     * Get workout quality indicator
     */
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

    /**
     * Create a summary string for the performance
     */
    public String getPerformanceSummary() {
        StringBuilder summary = new StringBuilder();

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

        if (perceivedExertion != null) {
            summary.append(String.format(" | RPE: %d/10", perceivedExertion));
        }

        return summary.length() > 0 ? summary.toString() : "Performance data";
    }
}