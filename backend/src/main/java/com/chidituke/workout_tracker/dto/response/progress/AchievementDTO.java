package com.chidituke.workout_tracker.dto.response.progress;

import com.chidituke.workout_tracker.model.progress.Achievement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Achievement information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementDTO {

    private Integer achievementId;
    private String achievementKey;
    private String name;
    private String description;
    private String category;
    private String rarity;
    private Integer bonusXp;
    private String icon;
    private Boolean isHidden;
    private Integer displayOrder;

    // Criteria info
    private String criteriaField;
    private String criteriaOperator;
    private Integer criteriaValue;
    private String criteriaDescription;

    private LocalDateTime createdAt;

    /**
     * Convert entity to DTO
     */
    public static AchievementDTO fromEntity(Achievement achievement) {
        if (achievement == null) return null;

        return AchievementDTO.builder()
                .achievementId(achievement.getAchievementId())
                .achievementKey(achievement.getAchievementKey())
                .name(achievement.getName())
                .description(achievement.getDescription())
                .category(achievement.getCategory().name())
                .rarity(achievement.getRarity().name())
                .bonusXp(achievement.getBonusXp())
                .icon(achievement.getIcon())
                .isHidden(achievement.getIsHidden())
                .displayOrder(achievement.getDisplayOrder())
                .criteriaField(achievement.getCriteriaField())
                .criteriaOperator(achievement.getCriteriaOperator())
                .criteriaValue(achievement.getCriteriaValue())
                .criteriaDescription(buildCriteriaDescription(achievement))
                .createdAt(achievement.getCreatedAt())
                .build();
    }

    /**
     * Build human-readable criteria description
     */
    private static String buildCriteriaDescription(Achievement achievement) {
        if (achievement.getCriteriaField() == null) return null;

        String field = formatFieldName(achievement.getCriteriaField());
        String operator = achievement.getCriteriaOperator();
        Integer value = achievement.getCriteriaValue();

        return switch (operator) {
            case ">=" -> field + " at least " + value;
            case ">" -> field + " more than " + value;
            case "=" -> field + " exactly " + value;
            case "<=" -> field + " at most " + value;
            case "<" -> field + " less than " + value;
            default -> field + " " + operator + " " + value;
        };
    }

    /**
     * Format database field name to human-readable
     */
    private static String formatFieldName(String field) {
        return switch (field) {
            case "total_workouts_completed" -> "Complete";
            case "current_streak_days" -> "Achieve streak of";
            case "longest_streak_days" -> "Achieve longest streak of";
            case "total_volume_lifted" -> "Lift total of";
            case "total_workout_minutes" -> "Train for";
            case "total_distance_km" -> "Cover distance of";
            case "total_hold_seconds" -> "Hold for";
            case "unique_exercises_tried" -> "Try";
            case "cardio_workouts_completed" -> "Complete";
            case "strength_workouts_completed" -> "Complete";
            case "isometric_workouts_completed" -> "Complete";
            case "weekly_workout_count" -> "Complete";
            case "first_of_month_count" -> "Workout on first of month";
            case "weekend_workout_count" -> "Complete weekend workouts";
            default -> field.replace("_", " ");
        };
    }
}