// components/gamification/MiniProgressWidget.tsx
import React, {useEffect, useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {Badge} from '../ui/badge';
import {ProgressTooltip} from './ProgressTooltip';
import {useUserProgression} from '../../hooks/useUserProgression';
import {getRankInfo, getNextRank} from '../../types/gamification';
import {useAuth} from '../../contexts/AuthContext';

interface MiniProgressWidgetProps {
    className?: string;
}

export const MiniProgressWidget: React.FC<MiniProgressWidgetProps> = ({className = ''}) => {
    const navigate = useNavigate();
    const {isAuthenticated} = useAuth();
    const {progression, loading} = useUserProgression();
    const [animatedXP, setAnimatedXP] = useState<number>(0);

    // Animate XP changes
    useEffect(() => {
        if (progression?.seasonalXp !== undefined) {
            const start = animatedXP;
            const end = progression.seasonalXp;
            const duration = 1000;
            const startTime = Date.now();

            const animate = () => {
                const elapsed = Date.now() - startTime;
                const progress = Math.min(elapsed / duration, 1);

                const easeOut = 1 - Math.pow(1 - progress, 3);
                const current = Math.floor(start + (end - start) * easeOut);

                setAnimatedXP(current);

                if (progress < 1) {
                    requestAnimationFrame(animate);
                }
            };

            animate();
        }
    }, [progression?.seasonalXp]);

    // Don't show widget if not authenticated
    if (!isAuthenticated) {
        return null;
    }

    // Show loading state
    if (loading) {
        return (
            <div className={`flex items-center gap-2 ${className}`}>
                <div className="h-7 w-20 bg-gray-200 animate-pulse rounded"/>
                <div className="h-7 w-24 bg-gray-200 animate-pulse rounded"/>
                <div className="h-7 w-16 bg-gray-200 animate-pulse rounded"/>
            </div>
        );
    }

    // Don't show if no progression data
    if (!progression) {
        return null;
    }

    const currentRankInfo = getRankInfo(progression.seasonalRank);
    const nextRankInfo = getNextRank(progression.seasonalRank);
    const xpToNext = nextRankInfo ? (nextRankInfo.minXp - progression.seasonalXp) : 0;

    return (
        <div
            className={`flex items-center gap-2 cursor-pointer ${className}`}
            onClick={() => navigate('/progress')}
        >
            {/* XP Badge */}
            <ProgressTooltip content={nextRankInfo ? `${xpToNext} XP to ${nextRankInfo.name}` : 'Max Rank!'}>
                <Badge className="bg-blue-600 hover:bg-blue-700 transition-colors px-3 py-1 text-sm font-medium">
                    <span className="flex items-center gap-1.5">
                        <span className="text-yellow-300">⚡</span>
                        <span className="tabular-nums">{animatedXP}</span>
                        <span className="text-blue-200 text-xs">XP</span>
                    </span>
                </Badge>
            </ProgressTooltip>

            {/* Rank Badge */}
            <ProgressTooltip content={`Tier ${currentRankInfo.tier} • Seasonal Rank`}>
                <Badge
                    className="bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 transition-all px-3 py-1 text-sm font-medium">
                    <span className="flex items-center gap-1.5">
                        <span>{currentRankInfo.icon}</span>
                        <span className="uppercase tracking-wide">{progression.seasonalRank}</span>
                    </span>
                </Badge>
            </ProgressTooltip>

            {/* Streak Badge */}
            <ProgressTooltip content={
                progression.streakActive
                    ? `Keep it going! Best: ${progression.longestStreakDays} days`
                    : `Longest streak: ${progression.longestStreakDays} days`
            }>
                <Badge className={`
                    ${progression.streakActive ? 'bg-orange-600 hover:bg-orange-700' : 'bg-gray-600 hover:bg-gray-700'}
                    transition-colors px-3 py-1 text-sm font-medium
                `}>
                    <span className="flex items-center gap-1.5">
                        <span className={progression.streakActive ? 'animate-pulse' : ''}>🔥</span>
                        <span className="tabular-nums">{progression.currentStreakDays}</span>
                        <span className="text-xs">day{progression.currentStreakDays !== 1 ? 's' : ''}</span>
                    </span>
                </Badge>
            </ProgressTooltip>
        </div>
    );
};