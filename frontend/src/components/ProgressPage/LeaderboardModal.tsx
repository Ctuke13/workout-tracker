import React, {useEffect, useState} from 'react';
import {X, Trophy, TrendingUp, Crown, Medal, Award, Search} from 'lucide-react';
import {progressApi} from '../../services/progressApi';
import {useSeason} from '../../contexts/SeasonContext';

interface LeaderboardModalProps {
    isOpen: boolean;
    onClose: () => void;
}

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
        default:
            return {color: 'text-gray-600', bg: 'bg-gray-100', border: 'border-gray-300'};
    }
};

const getPositionBadge = (position: number) => {
    switch (position) {
        case 1:
            return {icon: <Crown className="w-5 h-5"/>, color: 'text-yellow-500', bg: 'bg-yellow-100'};
        case 2:
            return {icon: <Medal className="w-5 h-5"/>, color: 'text-gray-400', bg: 'bg-gray-100'};
        case 3:
            return {icon: <Award className="w-5 h-5"/>, color: 'text-orange-500', bg: 'bg-orange-100'};
        default:
            return null;
    }
};

const formatRank = (rank: string, tier: number): string => {
    const tierRoman = ['I', 'II', 'III'][tier - 1] || 'III';
    return `${rank} ${tierRoman}`;
};

export const LeaderboardModal: React.FC<LeaderboardModalProps> = ({isOpen, onClose}) => {
    const {theme} = useSeason();
    const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
    const [currentUser, setCurrentUser] = useState<UserProgression | null>(null);
    const [searchQuery, setSearchQuery] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (isOpen) {
            loadLeaderboard();
        }
    }, [isOpen]);

    const loadLeaderboard = async () => {
        try {
            setLoading(true);
            setError(null);

            const [leaderboardData, userData] = await Promise.all([
                progressApi.getSeasonalLeaderboard(50), // Load top 50
                progressApi.getUserProgression()
            ]);

            setLeaderboard(leaderboardData);
            setCurrentUser(userData);
            setLoading(false);
        } catch (err) {
            console.error('Failed to load leaderboard:', err);
            setError('Failed to load leaderboard');
            setLoading(false);
        }
    };

    if (!isOpen) return null;

    const filteredLeaderboard = searchQuery
        ? leaderboard.filter(entry =>
            entry.username?.toLowerCase().includes(searchQuery.toLowerCase())
        )
        : leaderboard;

    const userInLeaderboard = currentUser && leaderboard.some(e => e.userId === currentUser.userId);

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
            <div className="relative w-full max-w-3xl max-h-[85vh] bg-white rounded-2xl shadow-2xl overflow-hidden">
                {/* Header */}
                <div className={`bg-gradient-to-br ${theme.gradient} border-b-2 ${theme.border} p-6`}>
                    <div className="flex items-center justify-between mb-4">
                        <div className="flex items-center gap-3">
                            <div className={`p-3 ${theme.accentGradient} rounded-xl shadow-lg`}>
                                <Trophy className="w-6 h-6 text-white"/>
                            </div>
                            <div>
                                <h2 className={`text-2xl font-black ${theme.textPrimary}`}>
                                    Seasonal Leaderboard
                                </h2>
                                <p className={`text-sm ${theme.textSecondary} font-semibold`}>
                                    Top performers this season
                                </p>
                            </div>
                        </div>
                        <button
                            onClick={onClose}
                            className="p-2 hover:bg-white/20 rounded-lg transition"
                        >
                            <X className="w-6 h-6 text-gray-700"/>
                        </button>
                    </div>

                    {/* Search */}
                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400"/>
                        <input
                            type="text"
                            placeholder="Search users..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="w-full pl-10 pr-4 py-3 bg-white/90 border-2 border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                </div>

                {/* Content */}
                <div className="p-6 overflow-y-auto max-h-[calc(85vh-200px)]">
                    {loading ? (
                        <div className="space-y-3">
                            {[...Array(5)].map((_, i) => (
                                <div key={i} className="h-20 bg-gray-200 rounded-lg animate-pulse"/>
                            ))}
                        </div>
                    ) : error ? (
                        <div className="text-center py-12">
                            <p className="text-red-600 font-bold mb-4">⚠️ {error}</p>
                            <button
                                onClick={loadLeaderboard}
                                className="px-6 py-2 bg-red-600 text-white rounded-lg font-bold hover:bg-red-700"
                            >
                                Try Again
                            </button>
                        </div>
                    ) : filteredLeaderboard.length === 0 ? (
                        <div className="text-center py-12">
                            <Trophy className="w-16 h-16 text-gray-300 mx-auto mb-4"/>
                            <h3 className="text-xl font-bold text-gray-900 mb-2">
                                {searchQuery ? 'No users found' : 'Be the First!'}
                            </h3>
                            <p className="text-gray-600">
                                {searchQuery ? 'Try a different search term' : 'Complete a workout to appear on the leaderboard'}
                            </p>
                        </div>
                    ) : (
                        <div className="space-y-2">
                            {filteredLeaderboard.map((entry, index) => {
                                const isCurrentUser = currentUser && entry.userId === currentUser.userId;
                                const badge = getPositionBadge(entry.position || index + 1);
                                const rankTheme = getRankTheme(entry.seasonalRank);
                                const position = entry.position || index + 1;

                                return (
                                    <div
                                        key={entry.userId}
                                        className={`flex items-center gap-3 p-4 rounded-lg border-2 transition-all ${
                                            isCurrentUser
                                                ? 'bg-gradient-to-r from-yellow-100 to-amber-100 border-yellow-400 ring-2 ring-yellow-400/50'
                                                : 'bg-white border-gray-200 hover:border-gray-300 hover:shadow-md'
                                        }`}
                                    >
                                        {/* Position */}
                                        <div
                                            className={`flex-shrink-0 w-12 h-12 rounded-lg flex items-center justify-center font-black text-base ${
                                                badge ? `${badge.bg} ${badge.color}` : 'bg-gray-100 text-gray-600'
                                            }`}
                                        >
                                            {badge ? badge.icon : `#${position}`}
                                        </div>

                                        {/* User Info */}
                                        <div className="flex-1 min-w-0">
                                            <div className="flex items-center gap-2 mb-1">
                                                <h3 className={`text-base font-bold truncate ${
                                                    isCurrentUser ? 'text-amber-900' : 'text-gray-900'
                                                }`}>
                                                    {entry.username || `User ${entry.userId}`}
                                                </h3>
                                                {isCurrentUser && (
                                                    <span
                                                        className="flex-shrink-0 px-2 py-0.5 bg-yellow-500 text-white text-xs font-bold rounded-full">
                                                        YOU
                                                    </span>
                                                )}
                                            </div>
                                            <div className="flex items-center gap-2">
                                                <span
                                                    className={`text-xs font-bold px-2 py-0.5 rounded ${rankTheme.bg} ${rankTheme.color} border ${rankTheme.border}`}>
                                                    {formatRank(entry.seasonalRank, entry.seasonalTier)}
                                                </span>
                                                <span className="text-xs text-gray-600 font-semibold">
                                                    {entry.workoutsCompleted || 0} workouts
                                                </span>
                                            </div>
                                        </div>

                                        {/* XP */}
                                        <div className="flex-shrink-0 text-right">
                                            <div className="flex items-center gap-1">
                                                <TrendingUp
                                                    className={`w-4 h-4 ${isCurrentUser ? 'text-amber-600' : 'text-gray-500'}`}/>
                                                <span className={`text-lg font-black tabular-nums ${
                                                    isCurrentUser ? 'text-amber-900' : 'text-gray-900'
                                                }`}>
                                                    {(entry.seasonalXp || 0).toLocaleString()}
                                                </span>
                                            </div>
                                            <p className="text-xs text-gray-500 font-semibold">XP</p>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}

                    {/* Your Position (if not in top results) */}
                    {currentUser && !userInLeaderboard && !loading && !error && (
                        <div className="mt-6 pt-6 border-t-2 border-gray-200">
                            <p className="text-sm font-bold text-gray-600 mb-3 flex items-center gap-2">
                                <Trophy className="w-4 h-4"/>
                                Your Position
                            </p>
                            <div
                                className={`bg-gradient-to-r ${theme.gradient} border-2 ${theme.border} rounded-lg p-4`}>
                                <div className="flex items-center justify-between">
                                    <div>
                                        <p className={`text-base font-bold ${theme.textPrimary}`}>
                                            Keep climbing! 💪
                                        </p>
                                        <p className={`text-sm ${theme.textSecondary} font-semibold`}>
                                            {formatRank(currentUser.seasonalRank, currentUser.seasonalTier)} • {currentUser.seasonalXp.toLocaleString()} XP
                                        </p>
                                    </div>
                                    <div className="text-right">
                                        <p className={`text-3xl font-black ${theme.textPrimary}`}>?</p>
                                        <p className={`text-xs ${theme.textSecondary} font-bold`}>Position</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>

                {/* Footer */}
                <div className="border-t-2 border-gray-200 p-4 bg-gray-50">
                    <button
                        onClick={onClose}
                        className={`w-full py-3 px-4 bg-gradient-to-r ${theme.buttonGradient} text-white font-bold rounded-lg hover:opacity-90 transition shadow-lg`}
                    >
                        Close
                    </button>
                </div>
            </div>
        </div>
    );
};