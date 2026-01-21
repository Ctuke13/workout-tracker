import React from 'react';
import {X, Trophy, Calendar, TrendingUp, Award} from 'lucide-react';
import {useSeason} from '../../contexts/SeasonContext';

interface SeasonHistoryModalProps {
    isOpen: boolean;
    onClose: () => void;
}

// TODO: Replace with real data from backend when available
const mockSeasonHistory = [
    {
        seasonId: 4,
        seasonName: 'Fall 2024',
        seasonType: 'FALL',
        startDate: '2024-10-01',
        endDate: '2024-12-31',
        isActive: true,
        stats: {
            rank: 'NOVICE',
            tier: 3,
            xp: 170,
            workouts: 1,
            position: 1
        }
    }
];

const formatRank = (rank: string, tier: number): string => {
    const tierRoman = ['I', 'II', 'III'][tier - 1] || 'III';
    return `${rank} ${tierRoman}`;
};

const getSeasonEmoji = (seasonType: string): string => {
    switch (seasonType.toUpperCase()) {
        case 'WINTER':
            return '❄️';
        case 'SPRING':
            return '🌸';
        case 'SUMMER':
            return '☀️';
        case 'FALL':
        case 'AUTUMN':
            return '🍂';
        default:
            return '🌟';
    }
};

export const SeasonHistoryModal: React.FC<SeasonHistoryModalProps> = ({isOpen, onClose}) => {
    const {theme} = useSeason();

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm">
            <div className="relative w-full max-w-2xl max-h-[85vh] bg-white rounded-2xl shadow-2xl overflow-hidden">
                {/* Header */}
                <div className={`bg-gradient-to-br ${theme.gradient} border-b-2 ${theme.border} p-6`}>
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                            <div className={`p-3 ${theme.accentGradient} rounded-xl shadow-lg`}>
                                <Calendar className="w-6 h-6 text-white"/>
                            </div>
                            <div>
                                <h2 className={`text-2xl font-black ${theme.textPrimary}`}>
                                    Season History
                                </h2>
                                <p className={`text-sm ${theme.textSecondary} font-semibold`}>
                                    Your journey through the seasons
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
                </div>

                {/* Content */}
                <div className="p-6 overflow-y-auto max-h-[calc(85vh-120px)]">
                    {mockSeasonHistory.length === 0 ? (
                        <div className="text-center py-12">
                            <div
                                className={`w-20 h-20 mx-auto ${theme.accentGradient} rounded-full flex items-center justify-center mb-4`}>
                                <Trophy className="w-10 h-10 text-white"/>
                            </div>
                            <h3 className="text-xl font-bold text-gray-900 mb-2">
                                This is Your First Season!
                            </h3>
                            <p className="text-gray-600">
                                Complete more workouts to see your seasonal progress and history.
                            </p>
                        </div>
                    ) : (
                        <div className="space-y-4">
                            {mockSeasonHistory.map((season, index) => {
                                const emoji = getSeasonEmoji(season.seasonType);
                                const isActive = season.isActive;

                                return (
                                    <div
                                        key={season.seasonId}
                                        className={`relative border-2 rounded-xl p-5 transition-all ${
                                            isActive
                                                ? `${theme.border} bg-gradient-to-br ${theme.gradient} shadow-lg`
                                                : 'border-gray-200 bg-gray-50 hover:shadow-md'
                                        }`}
                                    >
                                        {isActive && (
                                            <div className="absolute top-3 right-3">
                                                <span
                                                    className="px-3 py-1 bg-green-500 text-white text-xs font-bold rounded-full">
                                                    ACTIVE
                                                </span>
                                            </div>
                                        )}

                                        <div className="flex items-start gap-4 mb-4">
                                            <div className="text-4xl">{emoji}</div>
                                            <div className="flex-1">
                                                <h3 className={`text-xl font-black ${isActive ? theme.textPrimary : 'text-gray-900'} mb-1`}>
                                                    {season.seasonName}
                                                </h3>
                                                <p className={`text-sm ${isActive ? theme.textSecondary : 'text-gray-600'} font-semibold`}>
                                                    {new Date(season.startDate).toLocaleDateString('en-US', {
                                                        month: 'long',
                                                        day: 'numeric'
                                                    })}
                                                    {' - '}
                                                    {new Date(season.endDate).toLocaleDateString('en-US', {
                                                        month: 'long',
                                                        day: 'numeric',
                                                        year: 'numeric'
                                                    })}
                                                </p>
                                            </div>
                                        </div>

                                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                                            <div
                                                className={`p-3 rounded-lg border-2 ${isActive ? `${theme.cardBorder} ${theme.cardBg}` : 'border-gray-200 bg-white'}`}>
                                                <div className="flex items-center gap-2 mb-1">
                                                    <Trophy
                                                        className={`w-4 h-4 ${isActive ? theme.textSecondary : 'text-gray-500'}`}/>
                                                    <span
                                                        className={`text-xs font-bold ${isActive ? theme.textTertiary : 'text-gray-500'}`}>Rank</span>
                                                </div>
                                                <p className={`text-sm font-black ${isActive ? theme.textPrimary : 'text-gray-900'}`}>
                                                    {formatRank(season.stats.rank, season.stats.tier)}
                                                </p>
                                                <p className={`text-xs font-semibold ${isActive ? theme.textSecondary : 'text-gray-600'}`}>
                                                    {season.stats.xp.toLocaleString()} XP
                                                </p>
                                            </div>

                                            <div
                                                className={`p-3 rounded-lg border-2 ${isActive ? `${theme.cardBorder} ${theme.cardBg}` : 'border-gray-200 bg-white'}`}>
                                                <div className="flex items-center gap-2 mb-1">
                                                    <TrendingUp
                                                        className={`w-4 h-4 ${isActive ? theme.textSecondary : 'text-gray-500'}`}/>
                                                    <span
                                                        className={`text-xs font-bold ${isActive ? theme.textTertiary : 'text-gray-500'}`}>Position</span>
                                                </div>
                                                <p className={`text-sm font-black ${isActive ? theme.textPrimary : 'text-gray-900'}`}>
                                                    #{season.stats.position}
                                                </p>
                                            </div>

                                            <div
                                                className={`p-3 rounded-lg border-2 ${isActive ? `${theme.cardBorder} ${theme.cardBg}` : 'border-gray-200 bg-white'}`}>
                                                <div className="flex items-center gap-2 mb-1">
                                                    <Calendar
                                                        className={`w-4 h-4 ${isActive ? theme.textSecondary : 'text-gray-500'}`}/>
                                                    <span
                                                        className={`text-xs font-bold ${isActive ? theme.textTertiary : 'text-gray-500'}`}>Workouts</span>
                                                </div>
                                                <p className={`text-sm font-black ${isActive ? theme.textPrimary : 'text-gray-900'}`}>
                                                    {season.stats.workouts}
                                                </p>
                                            </div>

                                            <div
                                                className={`p-3 rounded-lg border-2 ${isActive ? `${theme.cardBorder} ${theme.cardBg}` : 'border-gray-200 bg-white'}`}>
                                                <div className="flex items-center gap-2 mb-1">
                                                    <Award
                                                        className={`w-4 h-4 ${isActive ? theme.textSecondary : 'text-gray-500'}`}/>
                                                    <span
                                                        className={`text-xs font-bold ${isActive ? theme.textTertiary : 'text-gray-500'}`}>Status</span>
                                                </div>
                                                <p className={`text-xs font-bold ${isActive ? 'text-green-600' : 'text-gray-600'}`}>
                                                    {isActive ? 'In Progress' : 'Completed'}
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}

                    {/* Empty State for Future Seasons */}
                    {mockSeasonHistory.length > 0 && (
                        <div className="mt-6 p-6 border-2 border-dashed border-gray-300 rounded-xl text-center">
                            <p className="text-gray-500 font-semibold">
                                🎯 More seasons coming soon! Keep training to build your legacy.
                            </p>
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