package com.chidituke.workout_tracker.dto.response.pet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetStatsResponse {

    // ==========================================
    // IDENTIFIERS
    // ==========================================

    private Long petStatsId;
    private Long userId;

    // ==========================================
    // PET IDENTITY & PROGRESSION
    // ==========================================

    private String petName;              // User-chosen name (e.g., "Luna")
    private String petType;              // "WOLF" (future: BEAR, FOX, DRAGON)
    private String petColor;             // "GREY", "BROWN", "BLACK" (varies by type)
    private Integer xp;                  // Current XP toward next level
    private Integer level;               // Current level (1-100+)
    private Integer xpToNextLevel;       // XP required for next level
    private Integer xpProgress;          // XP earned in current level (for progress bar)
    private String evolutionStage;       // "BABY", "KID", "TEEN", "ADULT", "CHAMPION", "LEGENDARY"
    private String evolutionStageDisplay; // "Baby Wolf", "Kid Wolf", etc.
    private Integer workoutsCompleted;   // Total workouts since pet creation
    private Boolean canEvolve;           // True if eligible for next evolution
    private String nextEvolutionStage;   // Next stage name, or null if at max
    private Integer levelForNextEvolution; // Level needed for next evolution

    // ==========================================
    // CORE STATS (0-100)
    // ==========================================

    private Integer fuel;
    private Integer motivation;
    private Integer fatigue;
    private Integer cleanliness;

    // ==========================================
    // STAT STATUS STRINGS
    // ==========================================

    private String fuelStatus;        // "ENERGIZED", "NORMAL", "LOW", "DEPLETED"
    private String motivationStatus;  // "FIRED_UP", "DETERMINED", "WAVERING", "DISCOURAGED"
    private String fatigueStatus;     // "FRESH", "TIRED", "VERY_TIRED", "EXHAUSTED"
    private String cleanlinessStatus; // "PRISTINE", "CLEAN", "DUSTY", "DIRTY", "FILTHY"

    // ==========================================
    // CRYSTAL ECONOMY
    // ==========================================

    private Integer crystals;
    private Integer maxCrystals;      // Always 15
    private Double feedingEfficiency; // 0.40 to 1.00
    private Integer daysSinceLastWorkout;

    // ==========================================
    // SLEEP SYSTEM
    // ==========================================

    private Boolean isSleeping;
    private Long sleepTimeRemainingMinutes; // Null if not sleeping

    // ==========================================
    // NEGLECT SYSTEM
    // ==========================================

    private Boolean isNeglected;
    private Long neglectRecoveryTimeRemainingMinutes; // Null if not neglected

    // ==========================================
    // FEATURE GATING
    // ==========================================

    private Boolean canMotivate;
    private Boolean canBathe;
    private Boolean canFeed;
    private Boolean canInteract;
    private String disabledReason; // Null if all interactions available

    // ==========================================
    // HOME SELECTION
    // ==========================================

    private String selectedHome;
    private List<String> unlockedHomes;

    // ==========================================
    // BATH SYSTEM
    // ==========================================

    private Integer bathTier;    // 1, 2, or 3

    // ==========================================
    // COOLDOWNS
    // ==========================================

    private Integer motivateCooldownHours;
    private LocalDateTime lastMotivateTime;
    private LocalDateTime lastBathTime;

    // ==========================================
    // METADATA
    // ==========================================

    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;

    // ==========================================
    // MOOD SYSTEM
    // ==========================================

    private String mood; // "happy", "neutral", or "sad"

    /**
     * Calculate and set mood based on current stats
     * HAPPY: fuel > 60 AND motivation > 60 AND fatigue < 40 AND cleanliness > 60
     * SAD: fuel < 30 OR motivation < 30 OR fatigue > 70 OR cleanliness < 30
     * NEUTRAL: Everything else
     */
    public void calculateAndSetMood() {
        if (fuel == null || motivation == null || fatigue == null || cleanliness == null) {
            this.mood = "neutral";
            return;
        }

        // Check SAD first - any critical stat triggers sadness
        if (fuel < 30 || motivation < 30 || fatigue > 70 || cleanliness < 30) {
            this.mood = "sad";
            return;
        }

        // Check HAPPY - all stats must be healthy
        if (fuel > 60 && motivation > 60 && fatigue < 40 && cleanliness > 60) {
            this.mood = "happy";
            return;
        }

        // Everything else is NEUTRAL (default)
        this.mood = "neutral";
    }
}