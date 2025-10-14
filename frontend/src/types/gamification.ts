// types/gamification.ts
export interface UserProgression {
    userId: number;

    // Seasonal progression
    seasonalXp: number;
    seasonalRank: string;
    seasonalTier: number;
    currentSeasonId: number;
    seasonStartDate: string;

    // Lifetime progression
    lifetimeXp: number;
    lifetimeRank: string;
    lifetimeTier: number;

    // Streak tracking
    currentStreakDays: number;
    longestStreakDays: number;
    lastWorkoutDate: string;
    streakActive: boolean;

    // Workout stats
    totalWorkoutsCompleted: number;
    totalSetsCompleted: number;
    totalVolumeLifted: number;
    totalWorkoutMinutes: number;
    totalDistanceKm: number;
    totalHoldSeconds: number;

    // Progress metrics
    xpToNextSeasonalRank: number;
    xpToNextLifetimeRank: number;
    seasonalRankProgress: number; // Percentage 0-100
    lifetimeRankProgress: number; // Percentage 0-100
}

export interface ProgressionUpdate {
    xpGained: number;
    newSeasonalXp: number;
    newLifetimeXp: number;
    seasonalRank: string;
    lifetimeRank: string;
    rankedUp: boolean;
    newRank?: string;
    currentStreak: number;
    streakMilestone: boolean;
    streakMessage?: string;
    achievementsUnlocked: Achievement[];
    totalAchievementBonusXp: number;
    summaryMessage?: string;
}

export interface Achievement {
    achievementId: number;
    achievementKey: string;
    name: string;
    description: string;
    category: string;
    rarity: 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';
    bonusXp: number;
    icon: string;
    isHidden: boolean;
    displayOrder: number;
    criteriaField: string;
    criteriaOperator: string;
    criteriaValue: number;
    createdAt: string;
}

export interface UnlockedAchievement extends Achievement {
    userAchievementId: number;
    userId: number;
    unlockedAt: string;
    bonusXpAwarded: number;
    progressValueAtUnlock: number;
}

export interface RankInfo {
    name: string;
    minXp: number;
    maxXp: number;
    icon: string;
    color: string;
}

// Rank thresholds (matches backend RankConstants)
export const RANK_THRESHOLDS: Record<string, RankInfo> = {
    NOVICE: {
        name: 'NOVICE',
        minXp: 0,
        maxXp: 1500,
        icon: '🌱',
        color: 'gray'
    },
    APPRENTICE: {
        name: 'APPRENTICE',
        minXp: 1500,
        maxXp: 4000,
        icon: '🟠',
        color: 'orange'
    },
    DEVOTEE: {
        name: 'DEVOTEE',
        minXp: 4000,
        maxXp: 8000,
        icon: '🟡',
        color: 'yellow'
    },
    WARRIOR: {
        name: 'WARRIOR',
        minXp: 8000,
        maxXp: 14000,
        icon: '🟢',
        color: 'green'
    },
    CHAMPION: {
        name: 'CHAMPION',
        minXp: 14000,
        maxXp: 22000,
        icon: '🔵',
        color: 'blue'
    },
    ELITE: {
        name: 'ELITE',
        minXp: 22000,
        maxXp: 32000,
        icon: '💜',
        color: 'purple'
    },
    MASTER: {
        name: 'MASTER',
        minXp: 32000,
        maxXp: 44000,
        icon: '🔴',
        color: 'red'
    },
    LEGEND: {
        name: 'LEGEND',
        minXp: 44000,
        maxXp: 60000,
        icon: '⚪',
        color: 'white'
    },
    ICON: {
        name: 'ICON',
        minXp: 60000,
        maxXp: 90000,
        icon: '🌟',
        color: 'gold'
    },
    IMMORTAL: {
        name: 'IMMORTAL',
        minXp: 90000,
        maxXp: 999999,
        icon: '💎',
        color: 'diamond'
    }
};

// Helper functions
export const getRankInfo = (rankName: string): RankInfo => {
    return RANK_THRESHOLDS[rankName] || RANK_THRESHOLDS.NOVICE;
};

export const getNextRank = (currentRank: string): RankInfo | null => {
    const ranks = Object.keys(RANK_THRESHOLDS);
    const currentIndex = ranks.indexOf(currentRank);
    if (currentIndex === -1 || currentIndex === ranks.length - 1) {
        return null; // Already at max rank
    }
    return RANK_THRESHOLDS[ranks[currentIndex + 1]];
};

/**
 * Convert tier number to Roman numeral display
 */
export const getTierDisplay = (tier: number): string => {
    switch (tier) {
        case 3:
            return 'III';
        case 2:
            return 'II';
        case 1:
            return 'I';
        default:
            return 'III';
    }
};