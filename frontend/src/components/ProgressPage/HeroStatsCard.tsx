// components/ProgressPage/HeroStatsCard.tsx - COOL LIGHT MODE
import React, {useEffect, useState} from 'react';
import {TrendingUp, Zap, Trophy} from 'lucide-react';
import {Card, CardContent} from '../ui/card';
import {useUserProgression} from '../../hooks/useUserProgression';
import {getRankInfo, getNextRank, getTierDisplay} from '../../types/gamification';

export const HeroStatsCard: React.FC = () => {
    const {progression, loading} = useUserProgression();
    const [animatedSeasonalProgress, setAnimatedSeasonalProgress] = useState(0);
    const [animatedLifetimeProgress, setAnimatedLifetimeProgress] = useState(0);

    // Animate progress bars
    useEffect(() => {
        if (progression) {
            const seasonalRankInfo = getRankInfo(progression.seasonalRank);
            const seasonalNextRank = getNextRank(progression.seasonalRank);
            const lifetimeRankInfo = getRankInfo(progression.lifetimeRank);
            const lifetimeNextRank = getNextRank(progression.lifetimeRank);

            // Calculate seasonal progress
            let seasonalTarget = 0;
            if (seasonalNextRank) {
                const rankXpRange = seasonalRankInfo.maxXp - seasonalRankInfo.minXp;
                const xpIntoRank = progression.seasonalXp - seasonalRankInfo.minXp;
                seasonalTarget = Math.min(Math.max((xpIntoRank / rankXpRange) * 100, 0), 100);
            } else {
                seasonalTarget = 100;
            }

            // Calculate lifetime progress
            let lifetimeTarget = 0;
            if (lifetimeNextRank) {
                const rankXpRange = lifetimeRankInfo.maxXp - lifetimeRankInfo.minXp;
                const xpIntoRank = progression.lifetimeXp - lifetimeRankInfo.minXp;
                lifetimeTarget = Math.min(Math.max((xpIntoRank / rankXpRange) * 100, 0), 100);
            } else {
                lifetimeTarget = 100;
            }

            // Animate progress bars
            const duration = 1500;
            const startTime = Date.now();

            const animate = () => {
                const elapsed = Date.now() - startTime;
                const progress = Math.min(elapsed / duration, 1);
                const easeOut = 1 - Math.pow(1 - progress, 3);

                setAnimatedSeasonalProgress(seasonalTarget * easeOut);
                setAnimatedLifetimeProgress(lifetimeTarget * easeOut);

                if (progress < 1) {
                    requestAnimationFrame(animate);
                }
            };

            animate();
        }
    }, [progression]);

    if (loading) {
        return (
            <Card className="bg-gradient-to-br from-slate-50 via-blue-50/30 to-purple-50/30 border-slate-300 shadow-xl">
                <CardContent className="p-6 sm:p-8">
                    <div className="animate-pulse space-y-6">
                        <div className="flex justify-between items-center">
                            <div className="h-20 w-20 sm:h-24 sm:w-24 bg-slate-200 rounded-full"></div>
                            <div className="space-y-2">
                                <div className="h-8 w-32 sm:w-48 bg-slate-200 rounded"></div>
                                <div className="h-4 w-24 sm:w-32 bg-slate-200 rounded"></div>
                            </div>
                        </div>
                        <div className="space-y-4">
                            <div className="h-4 bg-slate-200 rounded"></div>
                            <div className="h-4 bg-slate-200 rounded"></div>
                        </div>
                    </div>
                </CardContent>
            </Card>
        );
    }

    if (!progression) {
        return null;
    }

    const currentRankInfo = getRankInfo(progression.seasonalRank);
    const nextSeasonalRank = getNextRank(progression.seasonalRank);
    const nextLifetimeRank = getNextRank(progression.lifetimeRank);

    const seasonalXpToNext = nextSeasonalRank
        ? nextSeasonalRank.minXp - progression.seasonalXp
        : 0;
    const lifetimeXpToNext = nextLifetimeRank
        ? nextLifetimeRank.minXp - progression.lifetimeXp
        : 0;

    return (
        <Card
            className="bg-gradient-to-br from-slate-50 via-blue-50/30 to-purple-50/30 border-slate-300 shadow-xl hover:shadow-2xl transition-shadow overflow-hidden relative">
            {/* Animated Dot Pattern Background */}
            <div className="absolute inset-0 opacity-20">
                <div
                    className="absolute inset-0"
                    style={{
                        backgroundImage: 'radial-gradient(circle, #64748b 1.5px, transparent 1.5px)',
                        backgroundSize: '24px 24px'
                    }}
                />
            </div>

            {/* Glowing Gradient Orbs */}
            <div
                className="absolute top-0 right-0 w-96 h-96 bg-gradient-to-br from-blue-400/20 to-purple-400/20 rounded-full blur-3xl pointer-events-none"/>
            <div
                className="absolute bottom-0 left-0 w-96 h-96 bg-gradient-to-tr from-cyan-400/20 to-blue-400/20 rounded-full blur-3xl pointer-events-none"/>

            <CardContent className="p-6 sm:p-8 relative z-10">
                {/* Top Row: Rank & Stats */}
                <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-6 mb-8">
                    {/* Rank Badge */}
                    <div className="flex items-center gap-4">
                        <div
                            className="w-20 h-20 sm:w-24 sm:h-24 bg-gradient-to-br from-purple-600 to-pink-600 rounded-full flex items-center justify-center shadow-2xl shadow-purple-500/50 relative flex-shrink-0 ring-4 ring-purple-200">
                            <span className="text-3xl sm:text-4xl">{currentRankInfo.icon}</span>
                            <div
                                className="absolute -bottom-2 -right-2 bg-white border-3 border-purple-600 rounded-full px-3 py-1 shadow-lg">
                                <span className="text-xs font-bold text-purple-600">
                                    {getTierDisplay(progression.seasonalTier)}
                                </span>
                            </div>
                        </div>
                        <div>
                            <div className="text-xs sm:text-sm text-slate-600 mb-1 font-medium">Current Rank</div>
                            <div
                                className="text-2xl sm:text-3xl font-black bg-gradient-to-r from-purple-600 via-pink-600 to-purple-600 bg-clip-text text-transparent">
                                {progression.seasonalRank}
                            </div>
                            <div className="text-xs sm:text-sm text-slate-700 mt-1 font-semibold">
                                Tier {getTierDisplay(progression.seasonalTier)}
                            </div>
                        </div>
                    </div>

                    {/* Quick Stats */}
                    <div className="flex sm:flex-col gap-4 sm:gap-3 sm:text-right">
                        <div
                            className="flex items-center sm:justify-end gap-2 bg-gradient-to-r from-yellow-100 to-amber-100 px-4 py-2 rounded-lg border-2 border-yellow-300 shadow-md">
                            <Zap className="w-5 h-5 text-yellow-600"/>
                            <div>
                                <span className="text-xl sm:text-2xl font-bold text-yellow-700 tabular-nums">
                                    {progression.seasonalXp.toLocaleString()}
                                </span>
                                <span className="text-sm text-yellow-600 ml-1 font-medium">XP</span>
                            </div>
                        </div>
                        {progression.currentStreakDays >= 2 && (
                            <div
                                className="flex items-center sm:justify-end gap-2 bg-gradient-to-r from-orange-100 to-red-100 px-4 py-2 rounded-lg border-2 border-orange-300 shadow-md">
                                <span className="text-lg sm:text-xl">🔥</span>
                                <div>
                                    <span className="text-lg sm:text-xl font-bold text-orange-700 tabular-nums">
                                        {progression.currentStreakDays}
                                    </span>
                                    <span className="text-sm text-orange-600 ml-1 font-medium">days</span>
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                {/* Seasonal Progress */}
                <div
                    className="space-y-2 mb-6 p-5 bg-gradient-to-br from-blue-100/80 to-cyan-100/80 rounded-xl border-2 border-blue-300 shadow-lg backdrop-blur-sm">
                    <div className="flex justify-between items-center">
                        <div className="flex items-center gap-2">
                            <Trophy className="w-5 h-5 text-blue-700"/>
                            <span className="text-sm font-bold text-blue-900">
                                Seasonal Progress
                            </span>
                        </div>
                        {nextSeasonalRank ? (
                            <span className="text-sm text-slate-700 tabular-nums font-medium">
                                {progression.seasonalXp.toLocaleString()} / {nextSeasonalRank.minXp.toLocaleString()} XP
                            </span>
                        ) : (
                            <span className="text-sm text-green-700 font-bold">MAX RANK! 🎉</span>
                        )}
                    </div>
                    <div
                        className="relative h-4 bg-white/60 rounded-full overflow-hidden border-2 border-blue-200 shadow-inner">
                        <div
                            className="absolute inset-y-0 left-0 bg-gradient-to-r from-blue-600 via-cyan-500 to-blue-600 rounded-full transition-all duration-1000 ease-out shadow-lg"
                            style={{width: `${animatedSeasonalProgress}%`}}
                        >
                            <div
                                className="absolute inset-0 bg-gradient-to-r from-transparent via-white/40 to-transparent animate-shimmer"/>
                        </div>
                    </div>
                    {nextSeasonalRank && (
                        <div className="flex justify-between items-center">
                            <span className="text-xs text-slate-600 font-medium">
                                {progression.seasonalRank} {getTierDisplay(progression.seasonalTier)}
                            </span>
                            <span className="text-xs text-blue-700 font-bold">
                                {seasonalXpToNext.toLocaleString()} XP to {nextSeasonalRank.name}
                            </span>
                        </div>
                    )}
                </div>

                {/* Lifetime Progress */}
                <div
                    className="space-y-2 mb-8 p-5 bg-gradient-to-br from-purple-100/80 to-pink-100/80 rounded-xl border-2 border-purple-300 shadow-lg backdrop-blur-sm">
                    <div className="flex justify-between items-center">
                        <div className="flex items-center gap-2">
                            <TrendingUp className="w-5 h-5 text-purple-700"/>
                            <span className="text-sm font-bold text-purple-900">
                                Lifetime Progress
                            </span>
                        </div>
                        {nextLifetimeRank ? (
                            <span className="text-sm text-slate-700 tabular-nums font-medium">
                                {progression.lifetimeXp.toLocaleString()} / {nextLifetimeRank.minXp.toLocaleString()} XP
                            </span>
                        ) : (
                            <span className="text-sm text-green-700 font-bold">MAX RANK! 🎉</span>
                        )}
                    </div>
                    <div
                        className="relative h-4 bg-white/60 rounded-full overflow-hidden border-2 border-purple-200 shadow-inner">
                        <div
                            className="absolute inset-y-0 left-0 bg-gradient-to-r from-purple-600 via-pink-500 to-purple-600 rounded-full transition-all duration-1000 ease-out shadow-lg"
                            style={{width: `${animatedLifetimeProgress}%`}}
                        >
                            <div
                                className="absolute inset-0 bg-gradient-to-r from-transparent via-white/40 to-transparent animate-shimmer"/>
                        </div>
                    </div>
                    {nextLifetimeRank && (
                        <div className="flex justify-between items-center">
                            <span className="text-xs text-slate-600 font-medium">
                                {progression.lifetimeRank} {getTierDisplay(progression.lifetimeTier)}
                            </span>
                            <span className="text-xs text-purple-700 font-bold">
                                {lifetimeXpToNext.toLocaleString()} XP to {nextLifetimeRank.name}
                            </span>
                        </div>
                    )}
                </div>

                {/* Quick Stats Grid */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4 pt-6 border-t-2 border-slate-300">
                    <div
                        className="text-center p-4 bg-gradient-to-br from-green-100 to-emerald-100 rounded-xl border-2 border-green-300 shadow-lg hover:shadow-xl transition-shadow">
                        <div className="text-xl sm:text-2xl font-black text-green-700 mb-1">
                            {progression.totalWorkoutsCompleted}
                        </div>
                        <div className="text-xs text-green-800 font-semibold">Workouts</div>
                    </div>
                    <div
                        className="text-center p-4 bg-gradient-to-br from-blue-100 to-cyan-100 rounded-xl border-2 border-blue-300 shadow-lg hover:shadow-xl transition-shadow">
                        <div className="text-xl sm:text-2xl font-black text-blue-700 mb-1">
                            {progression.totalSetsCompleted}
                        </div>
                        <div className="text-xs text-blue-800 font-semibold">Sets</div>
                    </div>
                    <div
                        className="text-center p-4 bg-gradient-to-br from-purple-100 to-pink-100 rounded-xl border-2 border-purple-300 shadow-lg hover:shadow-xl transition-shadow">
                        <div className="text-xl sm:text-2xl font-black text-purple-700 mb-1">
                            {Math.round(progression.totalVolumeLifted).toLocaleString()}
                        </div>
                        <div className="text-xs text-purple-800 font-semibold">lbs</div>
                    </div>
                    <div
                        className="text-center p-4 bg-gradient-to-br from-orange-100 to-amber-100 rounded-xl border-2 border-orange-300 shadow-lg hover:shadow-xl transition-shadow">
                        <div className="text-xl sm:text-2xl font-black text-orange-700 mb-1">
                            {progression.totalWorkoutMinutes}
                        </div>
                        <div className="text-xs text-orange-800 font-semibold">Minutes</div>
                    </div>
                </div>
            </CardContent>

            {/* Shimmer animation for progress bars */}
            <style>{`
                @keyframes shimmer {
                    0% { transform: translateX(-100%); }
                    100% { transform: translateX(100%); }
                }
                .animate-shimmer {
                    animation: shimmer 2s infinite;
                }
            `}</style>
        </Card>
    );
};