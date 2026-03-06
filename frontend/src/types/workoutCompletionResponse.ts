// types/workoutCompletionResponse.ts

export interface WorkoutCompletionResponse {
    // XP changes
    xpGained: number;
    newSeasonalXp: number;
    newLifetimeXp: number;

    // Rank info
    seasonalRank: string;
    lifetimeRank: string;
    rankedUp: boolean;
    tieredUp?: boolean;
    newRank?: string;
    oldRank?: string;
    oldTier?: number;
    newSeasonalTier?: number;

    // Streak info
    currentStreak: number;
    streakMilestone: boolean;
    streakMessage?: string;

    // Achievements
    achievementsUnlocked: Array<{
        achievementId: number;
        name: string;
        description: string;
        icon: string;
        rarity: string;
        bonusXp: number;
    }>;
    totalAchievementBonusXp?: number;

    // Summary message
    summaryMessage?: string;

    // Consistency bonus indicator — true when backend applied the 15% XP boost
    consistencyBonusApplied?: boolean;

    // 🆕 PET STATS UPDATE
    petUpdate?: {
        crystalsEarned: number;
        wastedCrystals: number;
        newCrystalBalance: number;
        fatigueIncrease: number;
        newFatigue: number;
        isSleeping: boolean;
        message: string;
    };
}