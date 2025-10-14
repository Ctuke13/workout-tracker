// components/gamification/PostWorkoutOrchestrator.tsx
import React, {useState, useEffect} from 'react';
import {AchievementModal} from './AchievementModal';
import {RankPromotionModal} from './RankPromotionModal';
import {ProgressSummaryModal} from './ProgressSummaryModal';

interface Achievement {
    achievementId: number;
    name: string;
    description: string;
    icon: string;
    rarity: 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY';
    bonusXp: number;
}

interface PostWorkoutData {
    // XP data
    xpGained: number;
    newSeasonalXp: number;
    newLifetimeXp: number;

    // Rank data
    seasonalRank: string;
    lifetimeRank: string;
    rankedUp: boolean;
    oldRank?: string;
    oldTier?: number;
    newSeasonalTier?: number;

    // Streak data
    currentStreak: number;
    streakMilestone: boolean;

    // Achievements
    achievementsUnlocked: Achievement[];

    // Additional data for Progress Summary (you'll need to add these to your backend)
    previousSeasonalXp?: number;
    rankProgress?: number;
    weeklyWorkouts?: number;
    weeklyGoal?: number;
    tieredUp?: boolean;
    previousTier?: number;
    currentTier?: number;
}

interface PostWorkoutOrchestratorProps {
    data: PostWorkoutData;
    onComplete: () => void;
}

export const PostWorkoutOrchestrator: React.FC<PostWorkoutOrchestratorProps> = ({
                                                                                    data,
                                                                                    onComplete
                                                                                }) => {
    const [currentStage, setCurrentStage] = useState(0);

    // Calculate total number of stages
    const achievements = data.achievementsUnlocked || [];
    const hasRankUp = data.rankedUp;
    const totalStages =
        achievements.length +         // Achievement modals
        (hasRankUp ? 1 : 0) +        // Rank promotion modal
        1;                            // Progress summary modal (always)

    console.log('🎬 ORCHESTRATOR INIT:', {
        currentStage,
        totalStages,
        achievementCount: achievements.length,
        hasRankUp,
        rankedUpValue: data.rankedUp,
        seasonalRank: data.seasonalRank,
        newSeasonalXp: data.newSeasonalXp
    });

    // Move to next stage
    const nextStage = () => {
        console.log(`⏭️ NEXT STAGE: ${currentStage} → ${currentStage + 1}`);
        if (currentStage < totalStages - 1) {
            setCurrentStage(prev => prev + 1);
        } else {
            console.log('✅ ORCHESTRATOR COMPLETE');
            onComplete();
        }
    };

    useEffect(() => {
        console.log(`📍 CURRENT STAGE: ${currentStage}/${totalStages - 1}`);
    }, [currentStage]);

    // Determine which modal to show based on current stage
    let stageCounter = 0;

    // Stages 0 to N-1: Achievement modals
    if (currentStage < achievements.length) {
        const achievement = achievements[currentStage];
        return (
            <AchievementModal
                isOpen={true}
                achievement={achievement}
                onDismiss={nextStage}
                autoAdvanceSeconds={10}
            />
        );
    }
    stageCounter += achievements.length;

    // Stage N: Rank promotion (if applicable)
    if (hasRankUp && currentStage === stageCounter) {
        console.log(`👑 SHOWING RANK PROMOTION MODAL at stage ${currentStage}`);
        // Extract tier from rank string if format is "RANK TIER" (e.g., "NOVICE III")
        const extractTier = (rankStr: string): number => {
            if (rankStr.includes('III')) return 3;
            if (rankStr.includes('II')) return 2;
            if (rankStr.includes('I')) return 1;
            return 3; // Default to III
        };

        const extractRankName = (rankStr: string): string => {
            return rankStr.replace(/\s+(I|II|III)$/, '');
        };

        // You'll need to pass oldRank/oldTier from backend
        // For now, we'll derive it (this is a limitation - ideally backend provides this)
        const newRankName = extractRankName(data.seasonalRank);
        const newTier = extractTier(data.seasonalRank);

        // Calculate old rank (this is approximate - backend should provide this)
        const oldRankName = newRankName; // Same rank if just tier change
        const oldTier = data.previousTier || (newTier + 1); // Assume tier drop

        return (
            <RankPromotionModal
                isOpen={true}
                oldRank={data.oldRank || data.seasonalRank}
                oldTier={data.oldTier || 3}
                newRank={newRankName}
                newTier={data.newSeasonalTier || 3}
                onDismiss={nextStage}
            />
        );
    }
    if (hasRankUp) stageCounter++;

    // Final stage: Progress summary (always shows)
    if (currentStage === stageCounter) {
        const extractTier = (rankStr: string): number => {
            if (rankStr.includes('III')) return 3;
            if (rankStr.includes('II')) return 2;
            if (rankStr.includes('I')) return 1;
            return 3;
        };

        const extractRankName = (rankStr: string): string => {
            return rankStr.replace(/\s+(I|II|III)$/, '');
        };

        const currentRankName = data.seasonalRank;  // Already just "APPRENTICE"
        const currentTier = data.newSeasonalTier || 3;

        // Calculate streak bonus XP
        const streakBonusXP = achievements
            .filter(a => a.name.toLowerCase().includes('streak'))
            .reduce((sum, a) => sum + a.bonusXp, 0);

        return (
            <ProgressSummaryModal
                isOpen={true}
                onClose={nextStage}
                xpGained={data.xpGained}
                previousXP={data.previousSeasonalXp || (data.newSeasonalXp - data.xpGained)}
                newTotalXP={data.newSeasonalXp}
                currentRank={currentRankName}
                currentTier={currentTier}
                rankProgress={data.rankProgress || 50} // Default if not provided
                streakDays={data.currentStreak}
                streakActive={data.currentStreak > 0}
                streakBonusXP={streakBonusXP}
                weeklyWorkouts={data.weeklyWorkouts || 1}
                weeklyGoal={data.weeklyGoal || 7}
                tieredUp={data.tieredUp || false}
                previousTier={data.oldTier}
            />
        );
    }

    // Fallback (should never reach here)
    return null;
};