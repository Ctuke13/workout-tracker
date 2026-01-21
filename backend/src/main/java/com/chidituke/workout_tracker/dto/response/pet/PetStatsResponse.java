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
}