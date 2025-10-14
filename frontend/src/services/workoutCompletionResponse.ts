export interface WorkoutCompletionResponse {
    xpGained: number;
    newSeasonalXp: number;
    newLifetimeXp: number;
    seasonalRank: string;
    lifetimeRank: string;
    currentStreak: number;
    rankedUp: boolean;
    tieredUp?: boolean;
    oldRank?: string;
    oldTier?: number;
    newSeasonalTier?: number;
    streakMilestone: boolean;
    achievementsUnlocked: Array<{
        achievementId: number;
        name: string;
        description: string;
        icon: string;
        rarity: string;
        bonusXp: number;
    }>;
}