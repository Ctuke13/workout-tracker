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
    tier: number;
    minXp: number;
    maxXp: number;
    icon: string;
    color: string;
}

// Rank thresholds (matches backend RankConstants)
export const RANK_THRESHOLDS: Record<string, RankInfo> = {
    NOVICE: {
        name: 'NOVICE',
        tier: 3,
        minXp: 0,
        maxXp: 99,
        icon: '🌱',
        color: 'gray'
    },
    APPRENTICE: {
        name: 'APPRENTICE',
        tier: 3,
        minXp: 100,
        maxXp: 499,
        icon: '👤',
        color: 'blue'
    },
    DEVOTEE: {
        name: 'DEVOTEE',
        tier: 2,
        minXp: 500,
        maxXp: 1499,
        icon: '⭐',
        color: 'yellow'
    },
    WARRIOR: {
        name: 'WARRIOR',
        tier: 2,
        minXp: 1500,
        maxXp: 3499,
        icon: '⚔️',
        color: 'green'
    },
    CHAMPION: {
        name: 'CHAMPION',
        tier: 2,
        minXp: 3500,
        maxXp: 6999,
        icon: '🏆',
        color: 'blue'
    },
    ELITE: {
        name: 'ELITE',
        tier: 1,
        minXp: 7000,
        maxXp: 11999,
        icon: '💎',
        color: 'purple'
    },
    MASTER: {
        name: 'MASTER',
        tier: 1,
        minXp: 12000,
        maxXp: 19999,
        icon: '👑',
        color: 'red'
    },
    LEGEND: {
        name: 'LEGEND',
        tier: 1,
        minXp: 20000,
        maxXp: 999999,
        icon: '⚡',
        color: 'gold'
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