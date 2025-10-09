package com.chidituke.workout_tracker.model.progress;

import com.chidituke.workout_tracker.model.progress.enums.Rank;
import com.chidituke.workout_tracker.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a user's complete progression tracking.
 * <p>
 * Data Flow:
 * 1. User completes workout
 * 2. WorkoutCompletionService calls UserProgressionService
 * 3. Stats updated (workouts, sets, volume, minutes, etc.)
 * 4. XP awarded based on workout
 * 5. Rank recalculated if XP threshold reached
 * 6. Streak checked and updated
 * 7. Achievement tracking fields updated
 * <p>
 * Database Table: user_progression
 */
@Entity
@Table(name = "user_progression")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProgression {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_progression_id")
    private Long userProgressionId;

    // ========== USER RELATIONSHIP ==========

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    // ========== SEASONAL PROGRESSION (Resets every 3 months) ==========

    @Column(name = "seasonal_xp", nullable = false)
    private Integer seasonalXp = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "seasonal_rank", nullable = false, length = 20)
    private Rank seasonalRank = Rank.NOVICE;

    @Column(name = "seasonal_tier", nullable = false)
    private Integer seasonalTier = 3; // 3 = III, 2 = II, 1 = I

    @Column(name = "current_season_id", nullable = false)
    private Integer currentSeasonId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_season_id", insertable = false, updatable = false)
    private Season currentSeason;

    @Column(name = "season_start_date", nullable = false)
    private LocalDate seasonStartDate;

    // ========== LIFETIME PROGRESSION (Never resets) ==========

    @Column(name = "lifetime_xp", nullable = false)
    private Integer lifetimeXp = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "lifetime_rank", nullable = false, length = 20)
    private Rank lifetimeRank = Rank.NOVICE;

    @Column(name = "lifetime_tier", nullable = false)
    private Integer lifetimeTier = 3;

    // ========== STREAK TRACKING ==========

    @Column(name = "current_streak_days", nullable = false)
    private Integer currentStreakDays = 0;

    @Column(name = "longest_streak_days", nullable = false)
    private Integer longestStreakDays = 0;

    @Column(name = "last_workout_date")
    private LocalDate lastWorkoutDate;

    // ========== CORE STATISTICS ==========

    @Column(name = "total_workouts_completed", nullable = false)
    private Integer totalWorkoutsCompleted = 0;

    @Column(name = "total_sets_completed", nullable = false)
    private Integer totalSetsCompleted = 0;

    @Column(name = "total_volume_lifted", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalVolumLifted = BigDecimal.ZERO;

    @Column(name = "total_workout_minutes", nullable = false)
    private Integer totalWorkoutMinutes = 0;

    // ========== ACHIEVEMENT TRACKING FIELDS ==========

    /**
     * Total cardio distance for Cardio Distance achievements
     * (First Mile → Marathon Runner → Around the World)
     */
    @Column(name = "total_distance_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDistanceKm = BigDecimal.ZERO;

    /**
     * Total isometric hold time for Endurance achievements
     * (Steady Holder → Iron Will → The Pillar)
     */
    @Column(name = "total_hold_seconds", nullable = false)
    private Integer totalHoldSeconds = 0;

    /**
     * Count of unique exercises tried for Diversity achievements
     * (Explorer → Versatile Athlete → Jack of All Trades)
     */
    @Column(name = "unique_exercises_tried", nullable = false)
    private Integer uniqueExercisesTried = 0;

    /**
     * Count of cardio workouts for Cardio Specialist achievement
     */
    @Column(name = "cardio_workouts_completed", nullable = false)
    private Integer cardioWorkoutsCompleted = 0;

    /**
     * Count of strength workouts for Strength Specialist achievement
     */
    @Column(name = "strength_workouts_completed", nullable = false)
    private Integer strengthWorkoutsCompleted = 0;

    /**
     * Count of isometric workouts for Balance Master achievement
     */
    @Column(name = "isometric_workouts_completed", nullable = false)
    private Integer isometricWorkoutsCompleted = 0;

    /**
     * Count of first-of-month workouts for hidden achievement
     */
    @Column(name = "first_of_month_count", nullable = false)
    private Integer firstOfMonthCount = 0;

    /**
     * Count of weekend workouts for Weekend Warrior achievement
     */
    @Column(name = "weekend_workout_count", nullable = false)
    private Integer weekendWorkoutCount = 0;

    // ========== WEEKLY TRACKING (For streak bonuses) ==========

    @Column(name = "weekly_workout_count", nullable = false)
    private Integer weeklyWorkoutCount = 0;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate = LocalDate.now();

    // ========== METADATA ==========

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ========== HELPER METHODS ==========

    /**
     * Add XP to both seasonal and lifetime totals.
     * Does NOT recalculate ranks - call UserProgressionService.recalculateRanks() after.
     *
     * @param xpAmount XP to add
     */
    public void addXp(int xpAmount) {
        this.seasonalXp += xpAmount;
        this.lifetimeXp += xpAmount;
    }

    /**
     * Check if streak should be broken based on last workout date.
     *
     * @return true if streak is still active
     */
    @Transient
    public boolean isStreakActive() {
        if (lastWorkoutDate == null) {
            return false;
        }
        LocalDate yesterday = LocalDate.now().minusDays(1);
        return !lastWorkoutDate.isBefore(yesterday);
    }

    /**
     * Get XP needed to reach next seasonal rank.
     *
     * @return XP needed, or 0 if at max rank
     */
    @Transient
    public int getXpToNextSeasonalRank() {
        return seasonalRank.getXpToNextRank(seasonalXp);
    }

    /**
     * Get XP needed to reach next lifetime rank.
     *
     * @return XP needed, or 0 if at max rank
     */
    @Transient
    public int getXpToNextLifetimeRank() {
        return lifetimeRank.getXpToNextRank(lifetimeXp);
    }
}