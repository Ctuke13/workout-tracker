package com.chidituke.workout_tracker.model.pet;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "pet_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_stats_id")
    private Long petStatsId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // ==========================================
    // CORE STATS (0-100)
    // ==========================================

    @Column(nullable = false)
    private Integer fuel = 100;

    @Column(nullable = false)
    private Integer motivation = 100;

    @Column(nullable = false)
    private Integer fatigue = 0;

    @Column(nullable = false)
    private Integer cleanliness = 100;

    // ==========================================
    // CRYSTAL ECONOMY
    // ==========================================

    @Column(nullable = false)
    private Integer crystals = 0;

    private static final int MAX_CRYSTALS = 15;

    @Column(name = "last_workout_time")
    private LocalDateTime lastWorkoutTime;

    @Column(name = "last_fed_time")
    private LocalDateTime lastFedTime;

    // ==========================================
    // FORCED SLEEP SYSTEM
    // ==========================================

    @Column(name = "is_sleeping", nullable = false)
    private Boolean isSleeping = false;

    @Column(name = "sleep_start_time")
    private LocalDateTime sleepStartTime;

    @Column(name = "sleep_end_time")
    private LocalDateTime sleepEndTime;

    // ==========================================
    // NEGLECT SYSTEM
    // ==========================================

    @Column(name = "is_neglected", nullable = false)
    private Boolean isNeglected = false;

    @Column(name = "neglect_recovery_time")
    private LocalDateTime neglectRecoveryTime;

    // ==========================================
    // HOME SELECTION
    // ==========================================

    @Column(name = "selected_home", nullable = false)
    private String selectedHome = "GYM";

    // ==========================================
    // INTERACTION COOLDOWNS
    // ==========================================

    @Column(name = "last_motivate_time")
    private LocalDateTime lastMotivateTime;

    @Column(name = "last_bath_time")
    private LocalDateTime lastBathTime;

    /**
     * Gets motivate cooldown in hours
     */
    public int getMotivateCooldownHours() {
        if (lastMotivateTime == null) return 0;

        long hoursSince = ChronoUnit.HOURS.between(lastMotivateTime, LocalDateTime.now());
        long remaining = 12 - hoursSince;

        return remaining > 0 ? (int) remaining : 0;
    }

    // ==========================================
    // METADATA
    // ==========================================

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ==========================================
    // FATIGUE & SLEEP MANAGEMENT
    // ==========================================

    /**
     * Applies fatigue from completed exercises
     *
     * @param exerciseCount Number of exercises completed
     */
    public void applyExerciseFatigue(int exerciseCount) {
        this.fatigue = Math.min(100, this.fatigue + (exerciseCount * 15));
        this.lastUpdated = LocalDateTime.now();

        // Check if fatigue triggered sleep
        if (this.fatigue >= 100) {
            triggerForcedSleep();
        }
    }

    /**
     * Triggers forced 24-hour sleep when fatigue reaches 100
     */
    private void triggerForcedSleep() {
        this.isSleeping = true;
        this.sleepStartTime = LocalDateTime.now();
        this.sleepEndTime = LocalDateTime.now().plusHours(24);
        this.motivation = 0; // Exhaustion effect
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Checks if sleep period is over and wakes pet
     */
    public void checkSleepStatus() {
        if (isSleeping && sleepEndTime != null && LocalDateTime.now().isAfter(sleepEndTime)) {
            wakeUp();
        }
    }

    /**
     * Ends sleep state and resets fatigue
     */
    private void wakeUp() {
        this.isSleeping = false;
        this.fatigue = 0;
        this.sleepStartTime = null;
        this.sleepEndTime = null;
        this.lastUpdated = LocalDateTime.now();
        // Motivation rebuilds slowly over time (handled by daily job)
    }

    /**
     * Applies daily fatigue decay
     */
    public void applyDailyFatigueDecay() {
        this.fatigue = Math.max(0, this.fatigue - 20);
        this.lastUpdated = LocalDateTime.now();
    }

    // ==========================================
    // CRYSTAL ECONOMY
    // ==========================================

    /**
     * Earns crystals from workout based on exercise count (tiered)
     *
     * @param exerciseCount Number of exercises completed
     * @return Number of crystals that were wasted due to cap
     */
    public int earnCrystalsFromWorkout(int exerciseCount) {
        int crystalsEarned;

        // Tiered earning system
        if (exerciseCount == 1) {
            crystalsEarned = 2;
        } else if (exerciseCount == 2) {
            crystalsEarned = 5;
        } else if (exerciseCount >= 3 && exerciseCount <= 4) {
            crystalsEarned = 7;
        } else if (exerciseCount >= 5 && exerciseCount <= 6) {
            crystalsEarned = 9;
        } else {
            crystalsEarned = 12; // 7+ exercises
        }

        int oldCrystals = this.crystals;
        this.crystals = Math.min(MAX_CRYSTALS, this.crystals + crystalsEarned);
        int wastedCrystals = crystalsEarned - (this.crystals - oldCrystals);

        this.lastUpdated = LocalDateTime.now();
        return wastedCrystals;
    }

    /**
     * Spends crystals for feeding
     *
     * @param amount Number of crystals to spend
     * @return true if successful, false if not enough crystals
     */
    public boolean spendCrystals(int amount) {
        if (this.crystals >= amount) {
            this.crystals -= amount;
            this.lastUpdated = LocalDateTime.now();
            return true;
        }
        return false;
    }

    // ==========================================
    // FEEDING EFFICIENCY
    // ==========================================

    /**
     * Calculates feeding efficiency based on time since last workout
     *
     * @return Efficiency multiplier (0.40 to 1.00)
     */
    public double getFeedingEfficiency() {
        if (lastWorkoutTime == null) {
            return 0.40; // 40% if never worked out
        }

        long daysSinceWorkout = ChronoUnit.DAYS.between(lastWorkoutTime, LocalDateTime.now());

        if (daysSinceWorkout <= 1) return 1.00; // 100%
        else if (daysSinceWorkout == 2) return 0.85; // 85%
        else if (daysSinceWorkout == 3) return 0.70; // 70%
        else if (daysSinceWorkout == 4) return 0.55; // 55%
        else return 0.40; // 40% for 5+ days
    }

    /**
     * Feeds pet with efficiency multiplier
     *
     * @param baseFuel    Base fuel amount (15, 40, or 60)
     * @param crystalCost Crystal cost (1, 3, or 5)
     * @return Actual fuel gained, or -1 if failed
     */
    public int feedWithEfficiency(int baseFuel, int crystalCost) {
        // Check if sleeping (can't feed while sleeping)
        if (isSleeping) {
            return -1;
        }

        // Check crystals
        if (!spendCrystals(crystalCost)) {
            return -1; // Not enough crystals
        }

        // Calculate actual fuel with efficiency
        double efficiency = getFeedingEfficiency();
        int actualFuel = (int) (baseFuel * efficiency);

        // Apply fuel
        this.fuel = Math.min(100, this.fuel + actualFuel);
        this.lastFedTime = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();

        // Check neglect status after feeding
        if (isNeglected) {
            startNeglectRecovery();
        }

        return actualFuel;
    }

    // ==========================================
    // NEGLECT SYSTEM
    // ==========================================

    /**
     * Checks if pet should be marked as neglected (4+ days without feeding)
     */
    public void checkNeglectStatus() {
        // If already recovering, check if recovery is complete
        if (isNeglected && neglectRecoveryTime != null) {
            if (LocalDateTime.now().isAfter(neglectRecoveryTime)) {
                recoverFromNeglect();
            }
            return;
        }

        // Check if should trigger neglect
        if (lastFedTime == null) {
            return; // New pet, not neglected yet
        }

        long daysSinceFed = ChronoUnit.DAYS.between(lastFedTime, LocalDateTime.now());

        if (daysSinceFed >= 4 && fuel <= 40) {
            triggerNeglect();
        }
    }

    /**
     * Triggers neglect state
     */
    private void triggerNeglect() {
        this.isNeglected = true;
        this.motivation = 0; // Pet is demoralized
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Starts 24-hour neglect recovery timer after feeding
     */
    private void startNeglectRecovery() {
        this.neglectRecoveryTime = LocalDateTime.now().plusHours(24);
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Ends neglect state after recovery period
     */
    private void recoverFromNeglect() {
        this.isNeglected = false;
        this.neglectRecoveryTime = null;
        this.lastUpdated = LocalDateTime.now();
        // Motivation will rebuild slowly from daily increases
    }

    // ==========================================
    // FEATURE GATING
    // ==========================================

    /**
     * Checks if Motivate interaction is allowed
     */
    public boolean canMotivate() {
        return fuel >= 40 && !isNeglected && !isSleeping;
    }

    /**
     * Checks if Bath interaction is allowed
     */
    public boolean canBathe() {
        return motivation >= 40 && fuel >= 20 && !isNeglected && !isSleeping;
    }

    /**
     * Checks if Feed interaction is allowed
     */
    public boolean canFeed() {
        return !isSleeping; // Can feed even when neglected
    }

    /**
     * Checks if any interaction is allowed
     */
    public boolean canInteract() {
        return !isSleeping;
    }

    /**
     * Gets reason why interaction is disabled
     */
    public String getDisabledReason() {
        if (isSleeping) {
            return "Pet is sleeping (recovery in progress)";
        }
        if (isNeglected) {
            return "Pet is neglected (feed and wait 24 hours)";
        }
        if (!canMotivate() && fuel < 40) {
            return "Not enough fuel (need 40+)";
        }
        if (!canBathe() && motivation < 40) {
            return "Not enough motivation (need 40+)";
        }
        if (!canBathe() && fuel < 20) {
            return "Not enough fuel (need 20+)";
        }
        return null;
    }

    // ==========================================
    // DAILY MAINTENANCE
    // ==========================================

    /**
     * Applies all daily stat changes
     */
    public void applyDailyMaintenance() {
        // Fuel decay
        this.fuel = Math.max(0, this.fuel - 15);

        // Fatigue recovery
        this.fatigue = Math.max(0, this.fatigue - 20);

        // Cleanliness decay
        this.cleanliness = Math.max(0, this.cleanliness - 5);

        // Motivation decay (if no workout yesterday)
        if (lastWorkoutTime == null ||
                ChronoUnit.DAYS.between(lastWorkoutTime, LocalDateTime.now()) > 0) {
            this.motivation = Math.max(0, this.motivation - 5);
        }

        // Check sleep status
        checkSleepStatus();

        // Check neglect status
        checkNeglectStatus();

        this.lastUpdated = LocalDateTime.now();
    }

    // ==========================================
    // WORKOUT INTEGRATION
    // ==========================================

    /**
     * Applies all stat changes from completing a workout
     *
     * @param exerciseCount Number of exercises completed
     */
    public void applyWorkoutCompletion(int exerciseCount) {
        // Apply fatigue
        applyExerciseFatigue(exerciseCount);

        // Increase motivation
        this.motivation = Math.min(100, this.motivation + 15);

        // Decrease cleanliness (sweating)
        this.cleanliness = Math.max(0, this.cleanliness - 10);

        // Update last workout time
        this.lastWorkoutTime = LocalDateTime.now();

        this.lastUpdated = LocalDateTime.now();
    }

    // ==========================================
    // STAT STATUS METHODS
    // ==========================================

    /**
     * Gets fuel status string
     */
    public String getFuelStatus() {
        if (fuel >= 70) return "ENERGIZED";
        else if (fuel >= 40) return "NORMAL";
        else if (fuel >= 15) return "LOW";
        else return "DEPLETED";
    }

    /**
     * Gets motivation status string
     */
    public String getMotivationStatus() {
        if (motivation >= 70) return "FIRED_UP";
        else if (motivation >= 40) return "DETERMINED";
        else if (motivation >= 20) return "WAVERING";
        else return "DISCOURAGED";
    }

    /**
     * Gets fatigue status string
     */
    public String getFatigueStatus() {
        if (fatigue <= 39) return "FRESH";
        else if (fatigue <= 69) return "TIRED";
        else if (fatigue <= 89) return "VERY_TIRED";
        else return "EXHAUSTED";
    }

    /**
     * Gets cleanliness status string
     */
    public String getCleanlinessStatus() {
        if (cleanliness >= 80) return "PRISTINE";
        else if (cleanliness >= 60) return "CLEAN";
        else if (cleanliness >= 40) return "DUSTY";
        else if (cleanliness >= 20) return "DIRTY";
        else return "FILTHY";
    }

    /**
     * Gets bath tier based on cleanliness level
     */
    public int getBathTier() {
        if (cleanliness >= 60) return 1; // Deodorant
        else if (cleanliness >= 40) return 2; // Sponge
        else return 3; // Full shower
    }

    /**
     * Gets if pet can bathe (motivation check)
     */
    public boolean getCanBathe() {
        return canBathe();
    }

    /**
     * Gets motivate cooldown in hours
     */
    public int getMotiveCooldownHours() {
        if (lastMotivateTime == null) return 0;

        long hoursSince = ChronoUnit.HOURS.between(lastMotivateTime, LocalDateTime.now());
        long remaining = 12 - hoursSince;

        return remaining > 0 ? (int) remaining : 0;
    }
}