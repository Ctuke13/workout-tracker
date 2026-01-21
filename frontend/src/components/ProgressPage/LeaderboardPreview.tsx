import React, {useEffect, useState} from 'react';
import {Trophy, TrendingUp, Crown, Medal, Award} from 'lucide-react';
import {progressApi} from '../../services/progressApi';
import {useSeason} from '../../contexts/SeasonContext';

// Types
interface LeaderboardEntry {
    userId: number;
    username: string;
    seasonalXp: number;
    seasonalRank: string;
    seasonalTier: number;
    position: number;
    workoutsCompleted: number;
    currentStreak: number;
}

interface UserProgression {
    userId: number;
    seasonalXp: number;
    seasonalRank: string;
    seasonalTier: number;
}

// Elegant leaderboard animations - trophy sparkles
const LeaderboardAnimation: React.FC = () => {
    const sparkles = [
        {id: 1, Icon: Trophy, top: '8%', left: '12%', delay: '0s', size: 'w-4 h-4', opacity: 0.3},
        {id: 2, Icon: Crown, top: '22%', right: '15%', delay: '1s', size: 'w-5 h-5', opacity: 0.4},
        {id: 3, Icon: Award, top: '45%', left: '8%', delay: '2s', size: 'w-4 h-4', opacity: 0.35},
        {id: 4, Icon: Medal, top: '65%', right: '10%', delay: '0.5s', size: 'w-5 h-5', opacity: 0.3},
        {id: 5, Icon: Trophy, top: '82%', left: '20%', delay: '1.5s', size: 'w-4 h-4', opacity: 0.4},
        {id: 6, Icon: Crown, top: '38%', right: '25%', delay: '2.5s', size: 'w-6 h-6', opacity: 0.35},
    ];

    return (
        <>
            <style>{`
                @keyframes floatSparkle {
                    0%, 100% { 
                        transform: translateY(0px) translateX(0px) rotate(0deg) scale(1);
                        opacity: 0.3;
                    }
                    25% {
                        transform: translateY(-12px) translateX(6px) rotate(10deg) scale(1.15);
                        opacity: 0.5;
                    }
                    50% { 
                        transform: translateY(-18px) translateX(-6px) rotate(-5deg) scale(1.1);
                        opacity: 0.45;
                    }
                    75% {
                        transform: translateY(-8px) translateX(8px) rotate(8deg) scale(1.08);
                        opacity: 0.4;
                    }
                }
                .float-sparkle {
                    animation: floatSparkle 7s ease-in-out infinite;
                }
            `}</style>
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                {sparkles.map((sparkle) => {
                    const Icon = sparkle.Icon;
                    return (
                        <div
                            key={sparkle.id}
                            className="absolute float-sparkle"
                            style={{
                                top: sparkle.top,
                                left: sparkle.left,
                                right: sparkle.right,
                                animationDelay: sparkle.delay,
                                opacity: sparkle.opacity,
                            }}
                        >
                            <Icon
                                className={`${sparkle.size} text-yellow-400 drop-shadow-lg`}
                                style={{
                                    filter: 'drop-shadow(0 0 8px rgba(251, 191, 36, 0.4))'
                                }}
                            />
                        </div>
                    );
                })}
            </div>
        </>
    );
};

// Rank color themes
const getRankTheme = (rank: string) => {
    switch (rank.toUpperCase()) {
        case 'NOVICE':
            return {color: 'text-gray-600', bg: 'bg-gray-100', border: 'border-gray-300'};
        case 'APPRENTICE':
            return {color: 'text-orange-600', bg: 'bg-orange-100', border: 'border-orange-300'};
        case 'DEVOTEE':
            return {color: 'text-yellow-600', bg: 'bg-yellow-100', border: 'border-yellow-300'};
        case 'WARRIOR':
            return {color: 'text-green-600', bg: 'bg-green-100', border: 'border-green-300'};
        case 'CHAMPION':
            return {color: 'text-blue-600', bg: 'bg-blue-100', border: 'border-blue-300'};
        case 'ELITE':
            return {color: 'text-purple-600', bg: 'bg-purple-100', border: 'border-purple-300'};
        case 'MASTER':
            return {color: 'text-red-600', bg: 'bg-red-100', border: 'border-red-300'};
        case 'LEGEND':
            return {color: 'text-pink-600', bg: 'bg-pink-100', border: 'border-pink-300'};
        case 'ICON':
            return {color: 'text-amber-600', bg: 'bg-amber-100', border: 'border-amber-300'};
        case 'IMMORTAL':
            return {color: 'text-cyan-600', bg: 'bg-cyan-100', border: 'border-cyan-300'};
        default:
            return {color: 'text-gray-600', bg: 'bg-gray-100', border: 'border-gray-300'};
    }
};

// Position badge for top 3
const getPositionBadge = (position: number) => {
    switch (position) {
        case 1:
            return {
                icon: <Crown className="w-4 h-4 sm:w-5 sm:h-5"/>,
                color: 'text-yellow-500',
                bg: 'bg-yellow-100',
                label: '1st'
            };
        case 2:
            return {
                icon: <Medal className="w-4 h-4 sm:w-5 sm:h-5"/>,
                color: 'text-gray-400',
                bg: 'bg-gray-100',
                label: '2nd'
            };
        case 3:
            return {
                icon: <Award className="w-4 h-4 sm:w-5 sm:h-5"/>,
                color: 'text-orange-500',
                bg: 'bg-orange-100',
                label: '3rd'
            };
        default:
            return null;
    }
};

// Format rank display
const formatRank = (rank: string, tier: number): string => {
    const tierRoman = ['I', 'II', 'III'][tier - 1] || 'III';
    return `${rank} ${tierRoman}`;
};

interface LeaderboardPreviewProps {
    onViewFull?: () => void;
}

export const LeaderboardPreview: React.FC<LeaderboardPreviewProps> = ({onViewFull}) => {
    const {theme, loading: seasonLoading} = useSeason();
    const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
    const [currentUser, setCurrentUser] = useState<UserProgression | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchLeaderboard = async () => {
            try {
                setLoading(true);
                setError(null);

                const [leaderboardData, userData] = await Promise.all([
                    progressApi.getSeasonalLeaderboard(10),
                    progressApi.getUserProgression()
                ]);

                setLeaderboard(leaderboardData);
                setCurrentUser(userData);
                setLoading(false);
            } catch (err) {
                console.error('Failed to fetch leaderboard:', err);
                setError('Failed to load leaderboard');
                setLoading(false);
            }
        };

        fetchLeaderboard();
    }, []);

    if (loading || seasonLoading) {
        return (
            <div
                className={`bg-gradient-to-br ${theme.gradient} rounded-xl p-4 sm:p-6 border-2 ${theme.border} animate-pulse`}>
                <div className={`h-8 ${theme.accentLight} rounded w-48 mb-4`}></div>
                <div className="space-y-2">
                    {[...Array(5)].map((_, i) => (
                        <div key={i} className={`h-16 ${theme.accentLight} rounded`}></div>
                    ))}
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-gradient-to-br from-red-50 to-red-100 rounded-xl p-4 sm:p-6 border-2 border-red-300">
                <div className="text-center text-red-700">
                    <p className="text-base sm:text-lg font-semibold mb-2">⚠️ {error}</p>
                    <p className="text-xs sm:text-sm text-red-600">Unable to load leaderboard data</p>
                </div>
            </div>
        );
    }

    const userInTop10 = currentUser && leaderboard.some(e => e.userId === currentUser.userId);
    const showEmptyState = leaderboard.length === 0;

    return (
        <div
            className={`relative overflow-hidden bg-gradient-to-br ${theme.gradient} rounded-xl border-2 ${theme.border} shadow-xl hover:shadow-2xl transition-shadow`}>
            {/* Subtle leaderboard animation */}
            <LeaderboardAnimation/>

            {/* Ambient orbs */}
            <div className="absolute inset-0 overflow-hidden pointer-events-none opacity-20">
                <div
                    className={`absolute top-0 right-0 w-48 h-48 sm:w-64 sm:h-64 ${theme.accentLight} rounded-full blur-3xl animate-pulse`}></div>
                <div
                    className={`absolute bottom-0 left-0 w-48 h-48 sm:w-64 sm:h-64 ${theme.accentLight} rounded-full blur-3xl animate-pulse`}
                    style={{animationDelay: '1s'}}
                ></div>
            </div>

            <div className="relative p-4 sm:p-6 space-y-4 sm:space-y-6">
                {/* Header */}
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div
                            className={`w-10 h-10 sm:w-12 sm:h-12 ${theme.accentGradient} rounded-xl flex items-center justify-center shadow-lg`}>
                            <Trophy className="w-5 h-5 sm:w-6 sm:h-6 text-white"/>
                        </div>
                        <div>
                            <h2 className={`text-lg sm:text-2xl font-black ${theme.textPrimary}`}>
                                Seasonal Leaderboard
                            </h2>
                            <p className={`text-xs sm:text-sm ${theme.textSecondary} font-semibold`}>
                                Top Performers This Season
                            </p>
                        </div>
                    </div>
                    <button
                        onClick={onViewFull}
                        className={`px-3 py-1.5 sm:px-4 sm:py-2 bg-gradient-to-r ${theme.buttonGradient} text-white text-xs sm:text-sm font-bold rounded-lg transition-all duration-200 transform hover:scale-105 shadow-lg`}>
                        View All →
                    </button>
                </div>

                {/* Empty State */}
                {showEmptyState ? (
                    <div className="text-center py-8 space-y-3">
                        <div
                            className={`w-16 h-16 mx-auto ${theme.accentGradient} rounded-full flex items-center justify-center`}>
                            <Trophy className={`w-8 h-8 text-white`}/>
                        </div>
                        <div>
                            <p className={`text-base sm:text-lg font-bold ${theme.textPrimary} mb-1`}>
                                No Rankings Yet!
                            </p>
                            <p className={`text-xs sm:text-sm ${theme.textSecondary}`}>
                                Be the first to complete a workout this season! 🚀
                            </p>
                        </div>
                    </div>
                ) : (
                    <>
                        {/* Leaderboard List */}
                        <div className="space-y-2">
                            {leaderboard.map((entry, index) => {
                                const isCurrentUser = currentUser && entry.userId === currentUser.userId;
                                const badge = getPositionBadge(entry.position || index + 1);
                                const rankTheme = getRankTheme(entry.seasonalRank);
                                const position = entry.position || index + 1;

                                return (
                                    <div
                                        key={entry.userId}
                                        className={`
                                            relative flex items-center gap-2 sm:gap-3 p-2 sm:p-3 rounded-lg border-2 
                                            transition-all duration-200 hover:scale-[1.02]
                                            ${isCurrentUser
                                            ? 'bg-gradient-to-r from-yellow-100 to-amber-100 border-yellow-400 shadow-lg ring-2 ring-yellow-400/50'
                                            : 'bg-white/80 border-slate-200 hover:border-slate-300 hover:shadow-md'
                                        }
                                        `}
                                    >
                                        {/* Position Badge */}
                                        <div
                                            className={`flex-shrink-0 w-8 h-8 sm:w-10 sm:h-10 rounded-lg flex items-center justify-center font-black text-sm sm:text-base ${badge ? `${badge.bg} ${badge.color}` : 'bg-slate-100 text-slate-600'}`}>
                                            {badge ? badge.icon : `#${position}`}
                                        </div>

                                        {/* User Info */}
                                        <div className="flex-1 min-w-0">
                                            <div className="flex items-center gap-2">
                                                <h3 className={`text-sm sm:text-base font-bold truncate ${isCurrentUser ? 'text-amber-900' : 'text-slate-800'}`}>
                                                    {entry.username || `User ${entry.userId}`}
                                                </h3>
                                                {isCurrentUser && (
                                                    <span
                                                        className="flex-shrink-0 px-2 py-0.5 bg-yellow-500 text-white text-[10px] sm:text-xs font-bold rounded-full">
                                                        YOU
                                                    </span>
                                                )}
                                            </div>
                                            <div className="flex items-center gap-2 mt-0.5">
                                                <span
                                                    className={`text-[10px] sm:text-xs font-bold px-2 py-0.5 rounded ${rankTheme.bg} ${rankTheme.color} border ${rankTheme.border}`}>
                                                    {formatRank(entry.seasonalRank, entry.seasonalTier)}
                                                </span>
                                                <span className="text-[10px] sm:text-xs text-slate-600 font-semibold">
                                                    {entry.workoutsCompleted || 0} workouts
                                                </span>
                                            </div>
                                        </div>

                                        {/* XP Display */}
                                        <div className="flex-shrink-0 text-right">
                                            <div className="flex items-center gap-1">
                                                <TrendingUp
                                                    className={`w-3 h-3 sm:w-4 sm:h-4 ${isCurrentUser ? 'text-amber-600' : theme.textSecondary}`}/>
                                                <span
                                                    className={`text-sm sm:text-lg font-black tabular-nums ${isCurrentUser ? 'text-amber-900' : theme.textPrimary}`}>
                                                    {(entry.seasonalXp || 0).toLocaleString()}
                                                </span>
                                            </div>
                                            <p className="text-[9px] sm:text-[10px] text-slate-500 font-semibold">
                                                XP
                                            </p>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>

                        {/* Current User Position - Only if NOT in top 10 */}
                        {currentUser && !userInTop10 && (
                            <div className={`pt-4 border-t-2 ${theme.border}`}>
                                <p className={`text-xs sm:text-sm ${theme.textSecondary} font-bold mb-2 flex items-center gap-2`}>
                                    <Trophy className="w-4 h-4"/>
                                    Your Position
                                </p>
                                <div
                                    className={`bg-gradient-to-r ${theme.gradient} border-2 ${theme.border} rounded-lg p-3 sm:p-4`}>
                                    <div className="flex items-center justify-between">
                                        <div>
                                            <p className={`text-sm sm:text-base font-bold ${theme.textPrimary}`}>
                                                Keep climbing! 💪
                                            </p>
                                            <p className={`text-xs sm:text-sm ${theme.textSecondary} font-semibold`}>
                                                {formatRank(currentUser.seasonalRank, currentUser.seasonalTier)} • {currentUser.seasonalXp.toLocaleString()} XP
                                            </p>
                                        </div>
                                        <div className="text-right">
                                            <p className={`text-2xl sm:text-3xl font-black ${theme.textPrimary}`}>
                                                ?
                                            </p>
                                            <p className={`text-[10px] sm:text-xs ${theme.textSecondary} font-bold`}>
                                                Position
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}
                    </>
                )}
            </div>
        </div>
    );
};