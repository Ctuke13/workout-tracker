import React, {useEffect, useState} from 'react';
import {Calendar, TrendingUp, Trophy, Clock} from 'lucide-react';
import {progressApi} from '../../services/progressApi';
import {useSeason} from '../../contexts/SeasonContext';

// Types
interface SeasonStats {
    rank: string;
    tier: number;
    position: number;
    percentile: number;
    workoutsThisSeason: number;
    xp: number;
    longestStreak: number;
}

// Seasonal animated elements component
const SeasonalAnimation: React.FC<{ seasonName: string }> = ({seasonName}) => {
    const lowerName = seasonName.toLowerCase();

    // Generate multiple animated elements
    const elements = Array.from({length: 15}, (_, i) => ({
        id: i,
        left: `${Math.random() * 100}%`,
        animationDelay: `${Math.random() * 5}s`,
        animationDuration: `${8 + Math.random() * 8}s`,
        size: `${10 + Math.random() * 15}px`,
        opacity: 0.2 + Math.random() * 0.4
    }));

    if (lowerName.includes('winter')) {
        // Falling snowflakes
        return (
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                {elements.map((el) => (
                    <div
                        key={el.id}
                        className="absolute animate-fall"
                        style={{
                            left: el.left,
                            top: '-20px',
                            animationDelay: el.animationDelay,
                            animationDuration: el.animationDuration,
                            opacity: el.opacity
                        }}
                    >
                        <div
                            className="text-blue-300"
                            style={{fontSize: el.size}}
                        >
                            ❄️
                        </div>
                    </div>
                ))}
                <style>{`
                    @keyframes fall {
                        0% { transform: translateY(0) rotate(0deg); }
                        100% { transform: translateY(100vh) rotate(360deg); }
                    }
                    .animate-fall {
                        animation: fall linear infinite;
                    }
                `}</style>
            </div>
        );
    } else if (lowerName.includes('spring')) {
        // Floating petals
        return (
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                {elements.map((el) => (
                    <div
                        key={el.id}
                        className="absolute animate-float-petal"
                        style={{
                            left: el.left,
                            bottom: '-20px',
                            animationDelay: el.animationDelay,
                            animationDuration: el.animationDuration,
                            opacity: el.opacity
                        }}
                    >
                        <div
                            className="text-pink-300"
                            style={{fontSize: el.size}}
                        >
                            🌸
                        </div>
                    </div>
                ))}
                <style>{`
                    @keyframes floatPetal {
                        0% { transform: translateY(0) translateX(0) rotate(0deg); }
                        50% { transform: translateY(-50vh) translateX(30px) rotate(180deg); }
                        100% { transform: translateY(-100vh) translateX(0) rotate(360deg); }
                    }
                    .animate-float-petal {
                        animation: floatPetal ease-in-out infinite;
                    }
                `}</style>
            </div>
        );
    } else if (lowerName.includes('summer')) {
        // Floating sunshine elements
        return (
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                {elements.map((el) => (
                    <div
                        key={el.id}
                        className="absolute animate-float-up"
                        style={{
                            left: el.left,
                            bottom: '-20px',
                            animationDelay: el.animationDelay,
                            animationDuration: el.animationDuration,
                            opacity: el.opacity
                        }}
                    >
                        <div
                            className="text-yellow-300"
                            style={{fontSize: el.size}}
                        >
                            ☀️
                        </div>
                    </div>
                ))}
                <style>{`
                    @keyframes floatUp {
                        0% { transform: translateY(0) scale(0.8); }
                        50% { transform: translateY(-50vh) scale(1.2); }
                        100% { transform: translateY(-100vh) scale(0.8); opacity: 0; }
                    }
                    .animate-float-up {
                        animation: floatUp ease-in-out infinite;
                    }
                `}</style>
            </div>
        );
    } else {
        // Fall - Falling leaves
        return (
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                {elements.map((el) => (
                    <div
                        key={el.id}
                        className="absolute animate-fall-leaf"
                        style={{
                            left: el.left,
                            top: '-20px',
                            animationDelay: el.animationDelay,
                            animationDuration: el.animationDuration,
                            opacity: el.opacity
                        }}
                    >
                        <div
                            className="text-orange-400"
                            style={{fontSize: el.size}}
                        >
                            🍂
                        </div>
                    </div>
                ))}
                <style>{`
                    @keyframes fallLeaf {
                        0% { transform: translateY(0) translateX(0) rotate(0deg); }
                        25% { transform: translateY(25vh) translateX(20px) rotate(90deg); }
                        50% { transform: translateY(50vh) translateX(-20px) rotate(180deg); }
                        75% { transform: translateY(75vh) translateX(20px) rotate(270deg); }
                        100% { transform: translateY(100vh) translateX(0) rotate(360deg); }
                    }
                    .animate-fall-leaf {
                        animation: fallLeaf ease-in-out infinite;
                    }
                `}</style>
            </div>
        );
    }
};

// Helper functions
const calculatePercentile = (position: number, totalUsers: number): number => {
    if (totalUsers <= 0) return 0;
    const percentile = ((totalUsers - position + 1) / totalUsers) * 100;
    return Math.round(Math.max(1, Math.min(100, percentile)));
};

const calculateDaysRemaining = (endDate: string): number => {
    const end = new Date(endDate);
    const now = new Date();
    const diffTime = end.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return Math.max(0, diffDays);
};

const calculateSeasonProgress = (startDate: string, endDate: string): number => {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const now = new Date();
    const totalDuration = end.getTime() - start.getTime();
    const elapsed = now.getTime() - start.getTime();
    return Math.min(100, Math.max(0, (elapsed / totalDuration) * 100));
};

const formatRank = (rank: string, tier: number): string => {
    const tierRoman = ['I', 'II', 'III'][tier - 1] || 'III';
    return `${rank} ${tierRoman}`;
};

const getXpForNextRank = (currentRank: string, currentTier: number, currentXp: number): {
    nextXp: number;
    xpNeeded: number;
    progress: number
} => {
    const rankThresholds: { [key: string]: number[] } = {
        'NOVICE': [0, 500, 1000],
        'BRONZE': [1500, 2500, 4000],
        'SILVER': [6000, 9000, 13000],
        'GOLD': [18000, 25000, 35000],
        'PLATINUM': [50000, 70000, 100000],
        'DIAMOND': [140000, 200000, 280000],
        'MASTER': [400000, 600000, 1000000],
        'GRANDMASTER': [1500000, 2500000, 5000000],
        'LEGEND': [10000000, 10000000, 10000000]
    };

    const tiers = rankThresholds[currentRank];
    if (!tiers) return {nextXp: 0, xpNeeded: 0, progress: 100};

    const tierIndex = currentTier - 1;

    if (currentTier === 1) {
        const ranks = Object.keys(rankThresholds);
        const currentRankIndex = ranks.indexOf(currentRank);
        if (currentRankIndex < ranks.length - 1) {
            const nextRankName = ranks[currentRankIndex + 1];
            const nextRankTiers = rankThresholds[nextRankName];
            const nextXp = nextRankTiers[2];
            const xpNeeded = nextXp - currentXp;
            const progress = ((currentXp - tiers[0]) / (nextXp - tiers[0])) * 100;
            return {nextXp, xpNeeded, progress: Math.min(100, Math.max(0, progress))};
        }
        return {nextXp: currentXp, xpNeeded: 0, progress: 100};
    }

    const nextXp = tiers[tierIndex - 1];
    const xpNeeded = nextXp - currentXp;
    const prevXp = tierIndex === 2 ? tiers[2] : tiers[tierIndex];
    const progress = ((currentXp - prevXp) / (nextXp - prevXp)) * 100;

    return {nextXp, xpNeeded, progress: Math.min(100, Math.max(0, progress))};
};

export const CurrentSeasonCard: React.FC = () => {
    const {season, theme, loading: seasonLoading} = useSeason();
    const [seasonStats, setSeasonStats] = useState<SeasonStats | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [daysRemaining, setDaysRemaining] = useState(0);
    const [seasonProgress, setSeasonProgress] = useState(0);

    useEffect(() => {
        const fetchSeasonStats = async () => {
            if (!season) return;

            try {
                setLoading(true);
                setError(null);

                const [progressionResponse, rankResponse] = await Promise.all([
                    progressApi.getUserProgression(),
                    progressApi.getUserRankInfo()
                ]);

                setSeasonStats({
                    rank: progressionResponse.seasonalRank,
                    tier: progressionResponse.seasonalTier,
                    position: rankResponse.seasonalPosition || 1,
                    percentile: calculatePercentile(rankResponse.seasonalPosition || 1, 1000),
                    workoutsThisSeason: progressionResponse.totalWorkoutsCompleted || 0,
                    xp: progressionResponse.seasonalXp,
                    longestStreak: progressionResponse.longestStreakDays || 0
                });

                setLoading(false);
            } catch (err) {
                console.error('Failed to fetch season stats:', err);
                setError('Failed to load season stats');
                setLoading(false);
            }
        };

        fetchSeasonStats();
    }, [season]);

    useEffect(() => {
        if (season) {
            setDaysRemaining(calculateDaysRemaining(season.endDate));
            setSeasonProgress(calculateSeasonProgress(season.startDate, season.endDate));
        }
    }, [season]);

    useEffect(() => {
        if (!season) return;

        const interval = setInterval(() => {
            setDaysRemaining(calculateDaysRemaining(season.endDate));
            setSeasonProgress(calculateSeasonProgress(season.startDate, season.endDate));
        }, 3600000);

        return () => clearInterval(interval);
    }, [season]);

    if (seasonLoading || loading) {
        return (
            <div
                className="bg-gradient-to-br from-slate-100 to-slate-200 rounded-xl p-4 sm:p-6 border-2 border-slate-300 animate-pulse">
                <div className="h-8 bg-slate-300 rounded w-32 sm:w-48 mb-4"></div>
                <div className="h-4 bg-slate-300 rounded w-full mb-2"></div>
                <div className="h-4 bg-slate-300 rounded w-3/4"></div>
            </div>
        );
    }

    if (error || !season || !seasonStats) {
        return (
            <div className="bg-gradient-to-br from-red-50 to-red-100 rounded-xl p-4 sm:p-6 border-2 border-red-300">
                <div className="text-center text-red-700">
                    <p className="text-base sm:text-lg font-semibold mb-2">⚠️ {error || 'No Active Season'}</p>
                    <p className="text-xs sm:text-sm text-red-600">Unable to load season data</p>
                </div>
            </div>
        );
    }

    return (
        <div
            className={`relative overflow-hidden bg-gradient-to-br ${theme.gradient} rounded-xl border-2 ${theme.border} shadow-xl hover:shadow-2xl transition-shadow`}>
            {/* Seasonal animated background */}
            <SeasonalAnimation seasonName={season.seasonName}/>

            {/* Ambient orbs */}
            <div className="absolute inset-0 overflow-hidden pointer-events-none opacity-30">
                <div
                    className={`absolute top-0 right-0 w-48 h-48 sm:w-64 sm:h-64 ${theme.orb1} rounded-full blur-3xl animate-pulse`}></div>
                <div
                    className={`absolute bottom-0 left-0 w-48 h-48 sm:w-64 sm:h-64 ${theme.orb2} rounded-full blur-3xl animate-pulse`}
                    style={{animationDelay: '1s'}}
                ></div>
            </div>

            <div className="relative p-4 sm:p-6 space-y-4 sm:space-y-6">
                <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3 sm:gap-4">
                    <div className="flex items-center gap-3">
                        <div className="text-3xl sm:text-4xl flex-shrink-0 animate-bounce"
                             style={{animationDuration: '3s'}}>
                            {theme.emoji}
                        </div>
                        <div className="min-w-0">
                            <h2 className={`text-lg sm:text-2xl font-black ${theme.textPrimary} truncate`}>
                                {season.seasonName.toUpperCase()}
                            </h2>
                            <p className={`text-xs sm:text-sm ${theme.textSecondary} font-medium`}>
                                {new Date(season.startDate).toLocaleDateString('en-US', {
                                    month: 'short',
                                    day: 'numeric'
                                })} - {new Date(season.endDate).toLocaleDateString('en-US', {
                                month: 'short',
                                day: 'numeric',
                                year: 'numeric'
                            })}
                            </p>
                        </div>
                    </div>
                    <div
                        className={`flex items-center gap-2 px-4 py-2 ${theme.cardBg} rounded-lg border-2 ${theme.cardBorder} shadow-md flex-shrink-0 self-start sm:self-auto`}>
                        <Clock className={`w-4 h-4 sm:w-5 sm:h-5 ${theme.textSecondary}`}/>
                        <div className="text-center">
                            <span
                                className={`text-xl sm:text-2xl font-black ${theme.textPrimary} tabular-nums`}>{daysRemaining}</span>
                            <p className={`text-xs ${theme.textTertiary} font-semibold`}>days left</p>
                        </div>
                    </div>
                </div>

                <div className="space-y-2">
                    <div className="flex items-center justify-between text-xs sm:text-sm">
                        <span className={`font-semibold ${theme.textSecondary}`}>Season Progress</span>
                        <span
                            className={`font-bold ${theme.textPrimary} tabular-nums`}>{Math.round(seasonProgress)}%</span>
                    </div>
                    <div
                        className={`relative h-3 sm:h-4 ${theme.progressBg} rounded-full overflow-hidden border-2 ${theme.progressBorder} shadow-inner`}>
                        <div
                            className={`absolute inset-y-0 left-0 bg-gradient-to-r ${theme.progressBar} rounded-full transition-all duration-1000 ease-out shadow-lg`}
                            style={{width: `${seasonProgress}%`}}
                        >
                            <div className="absolute inset-0 bg-white/30 animate-pulse"></div>
                        </div>
                    </div>
                </div>

                <div className="grid grid-cols-2 gap-3 sm:gap-4">
                    <div
                        className={`${theme.cardBg} rounded-lg p-3 sm:p-4 border-2 ${theme.cardBorder} backdrop-blur-sm shadow-lg`}>
                        <div className="flex items-center gap-1.5 sm:gap-2 mb-2">
                            <Trophy className={`w-3.5 h-3.5 sm:w-4 sm:h-4 ${theme.textSecondary}`}/>
                            <span
                                className={`text-[10px] sm:text-xs ${theme.textTertiary} uppercase tracking-wide font-bold`}>Rank</span>
                        </div>
                        <p className={`text-base sm:text-xl font-black ${theme.textPrimary}`}>
                            {formatRank(seasonStats.rank, seasonStats.tier)}
                        </p>
                        <p className={`text-[10px] sm:text-xs ${theme.textSecondary} mt-1 font-semibold tabular-nums`}>
                            {seasonStats.xp.toLocaleString()} XP
                        </p>
                        {(() => {
                            const {
                                xpNeeded,
                                progress
                            } = getXpForNextRank(seasonStats.rank, seasonStats.tier, seasonStats.xp);
                            if (xpNeeded > 0) {
                                return (
                                    <div className="mt-2">
                                        <div className={`h-1.5 ${theme.progressBg} rounded-full overflow-hidden`}>
                                            <div
                                                className={`h-full bg-gradient-to-r ${theme.progressBar} rounded-full transition-all duration-500`}
                                                style={{width: `${progress}%`}}
                                            />
                                        </div>
                                        <p className={`text-[9px] sm:text-[10px] ${theme.textTertiary} mt-1 font-semibold`}>
                                            {xpNeeded.toLocaleString()} XP to next
                                        </p>
                                    </div>
                                );
                            }
                            return null;
                        })()}
                    </div>

                    <div
                        className={`${theme.cardBg} rounded-lg p-3 sm:p-4 border-2 ${theme.cardBorder} backdrop-blur-sm shadow-lg`}>
                        <div className="flex items-center gap-1.5 sm:gap-2 mb-2">
                            <TrendingUp className={`w-3.5 h-3.5 sm:w-4 sm:h-4 ${theme.textSecondary}`}/>
                            <span
                                className={`text-[10px] sm:text-xs ${theme.textTertiary} uppercase tracking-wide font-bold`}>Position</span>
                        </div>
                        <p className={`text-base sm:text-xl font-black ${theme.textPrimary} tabular-nums`}>
                            #{seasonStats.position}
                        </p>
                        <p className={`text-[10px] sm:text-xs ${theme.textSecondary} mt-1 font-semibold`}>
                            Top {seasonStats.percentile}%
                        </p>
                    </div>

                    <div
                        className={`${theme.cardBg} rounded-lg p-3 sm:p-4 border-2 ${theme.cardBorder} backdrop-blur-sm shadow-lg`}>
                        <div className="flex items-center gap-1.5 sm:gap-2 mb-2">
                            <Calendar className={`w-3.5 h-3.5 sm:w-4 sm:h-4 ${theme.textSecondary}`}/>
                            <span
                                className={`text-[10px] sm:text-xs ${theme.textTertiary} uppercase tracking-wide font-bold`}>Workouts</span>
                        </div>
                        <p className={`text-base sm:text-xl font-black ${theme.textPrimary} tabular-nums`}>
                            {seasonStats.workoutsThisSeason}
                        </p>
                        <p className={`text-[10px] sm:text-xs ${theme.textSecondary} mt-1 font-semibold`}>
                            this season
                        </p>
                    </div>

                    <div
                        className={`${theme.cardBg} rounded-lg p-3 sm:p-4 border-2 ${theme.cardBorder} backdrop-blur-sm shadow-lg`}>
                        <div className="flex items-center gap-1.5 sm:gap-2 mb-2">
                            <span className="text-sm sm:text-base">🏆</span>
                            <span
                                className={`text-[10px] sm:text-xs ${theme.textTertiary} uppercase tracking-wide font-bold`}>Best Streak</span>
                        </div>
                        <p className={`text-base sm:text-xl font-black ${theme.textPrimary} tabular-nums`}>
                            {seasonStats.longestStreak} days
                        </p>
                        <p className={`text-[10px] sm:text-xs ${theme.textSecondary} mt-1 font-semibold`}>
                            personal best
                        </p>
                    </div>
                </div>

                <button
                    className={`w-full py-3 px-4 bg-gradient-to-r ${theme.buttonGradient} rounded-lg font-bold text-white text-sm sm:text-base transition-all duration-200 transform hover:scale-[1.02] active:scale-[0.98] shadow-lg hover:shadow-xl`}>
                    📜 View Season History
                </button>

                <div
                    className={`${theme.messageBg} border-2 ${theme.messageBorder} rounded-lg p-3 sm:p-4 text-center shadow-md`}>
                    <p className={`text-xs sm:text-sm font-bold ${theme.messageText}`}>
                        {daysRemaining > 60 ? `🚀 Plenty of time to climb the ranks!` : daysRemaining > 30 ? `⏰ Final stretch - give it your all!` : daysRemaining > 7 ? `🔥 Last few weeks - finish strong!` : `⚡ Final days - make them count!`}
                    </p>
                </div>
            </div>
        </div>
    );
};