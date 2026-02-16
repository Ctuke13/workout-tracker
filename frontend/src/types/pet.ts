// ==================== PET STATS ====================

export interface PetStats {
    // IDs
    petStatsId?: number;
    userId?: number;

    // Pet Identity
    petName?: string | null;
    petType?: string;
    petColor?: string;

    // XP & Leveling (🆕 NEW FIELDS)
    xp?: number;                        // Current XP in level
    level?: number;                     // Current level
    xpToNextLevel?: number;             // XP needed for next level
    xpProgress?: number;                // Current XP (same as xp, for progress bar)

    // Evolution System (🆕 NEW FIELDS)
    evolutionStage?: string;            // BABY, KID, TEEN, ADULT, CHAMPION, LEGENDARY
    evolutionStageDisplay?: string;     // "Baby Wolf", "Kid Wolf", etc.
    canEvolve?: boolean;                // Can evolve to next stage
    nextEvolutionStage?: string | null; // Next stage or null if max
    levelForNextEvolution?: number;     // Level required for next evolution

    // Progression (🆕 NEW FIELD)
    workoutsCompleted?: number;         // Total workouts completed

    // Core Stats (EXISTING)
    fuel: number;           // 0-100, hunger/energy
    motivation: number;     // 0-100, willingness to exercise
    fatigue: number;        // 0-100, tiredness (higher = more tired)
    cleanliness: number;    // 0-100, hygiene level

    // Currency (EXISTING)
    crystals: number;       // Currency earned from workouts
    maxCrystals: number;    // Max crystals that can be held (🆕 ALREADY HAD THIS)

    // Sleep System (EXISTING)
    isSleeping: boolean;
    sleepStartTime?: string | null;

    // Timestamps (EXISTING)
    lastFedAt?: string | null;
    lastMotivatedAt?: string | null;
    lastBathedAt?: string | null;

    // Other (EXISTING)
    feedingEfficiency?: number;
    mood: 'happy' | 'neutral' | 'sad';
}

// ==================== ACTION RESPONSES ====================

export interface FeedResponse {
    success: boolean;
    message: string;
    fuelGained: number;
    newFuel: number;
    crystalsSpent: number;
    newCrystals: number;
    feedingEfficiency: number;
}

export interface MotivateResponse {
    success: boolean;
    message: string;
    motivationGained: number;
    newMotivation: number;
    cooldownMinutes: number;
}

export interface BatheResponse {
    success: boolean;
    message: string;
    cleanlinessGained: number;
    newCleanliness: number;
}

export interface WakeResponse {
    success: boolean;
    message: string;
    fatigueReduction: number;
    newFatigue: number;
}

// ==================== MEAL TYPES ====================

export type MealType = 'SNACK' | 'MEAL' | 'FEAST';

export const MEAL_COSTS: Record<MealType, { crystals: number; fuel: number }> = {
    SNACK: {crystals: 1, fuel: 15},
    MEAL: {crystals: 3, fuel: 40},
    FEAST: {crystals: 6, fuel: 80},
};

// ==================== SEASONS ====================

export type Season = 'winter' | 'spring' | 'summer' | 'fall';

export type ParticleType = 'snow' | 'leaves' | 'butterflies' | 'clouds';

export interface SeasonalAssets {
    outsideImage: string;   // The outside scene visible through window
    roomImage: string;      // Room with transparent window (same for all seasons)
    particleType: ParticleType;
    particleEmoji: string;
}

// Updated asset paths for layered approach
export const SEASONAL_ASSETS: Record<Season, SeasonalAssets> = {
    winter: {
        outsideImage: '/assets/pet/rooms/winter_window.png',
        roomImage: '/assets/pet/rooms/room.png',
        particleType: 'snow',
        particleEmoji: '❄️',
    },
    spring: {
        outsideImage: '/assets/pet/rooms/spring_window.png',
        roomImage: '/assets/pet/rooms/room.png',
        particleType: 'butterflies',
        particleEmoji: '🌸',
    },
    summer: {
        outsideImage: '/assets/pet/rooms/summer_window.png',
        roomImage: '/assets/pet/rooms/room.png',
        particleType: 'clouds',
        particleEmoji: '☁️',
    },
    fall: {
        outsideImage: '/assets/pet/rooms/fall_window.png',
        roomImage: '/assets/pet/rooms/room.png',
        particleType: 'leaves',
        particleEmoji: '🍂',
    },
};

// ==================== HELPER FUNCTIONS ====================

/**
 * Get current season based on month
 */
export function getCurrentSeason(): Season {
    const month = new Date().getMonth(); // 0-11

    if (month >= 2 && month <= 4) return 'spring';   // Mar, Apr, May
    if (month >= 5 && month <= 7) return 'summer';   // Jun, Jul, Aug
    if (month >= 8 && month <= 10) return 'fall';    // Sep, Oct, Nov
    return 'winter';                                   // Dec, Jan, Feb
}

/**
 * Get color class for stat bar based on value
 */
export function getStatColor(value: number, inverse = false): string {
    const effectiveValue = inverse ? 100 - value : value;

    if (effectiveValue >= 70) return 'bg-green-500';
    if (effectiveValue >= 40) return 'bg-yellow-500';
    if (effectiveValue >= 20) return 'bg-orange-500';
    return 'bg-red-500';
}

/**
 * Format cooldown time remaining
 */
export function formatCooldownRemaining(lastActionTime: string | null, cooldownMinutes: number): string {
    if (!lastActionTime) return 'Ready!';

    const lastAction = new Date(lastActionTime);
    const now = new Date();
    const elapsed = (now.getTime() - lastAction.getTime()) / 1000 / 60; // minutes
    const remaining = cooldownMinutes - elapsed;

    if (remaining <= 0) return 'Ready!';

    if (remaining < 1) {
        return `${Math.ceil(remaining * 60)}s`;
    }
    return `${Math.ceil(remaining)}m`;
}