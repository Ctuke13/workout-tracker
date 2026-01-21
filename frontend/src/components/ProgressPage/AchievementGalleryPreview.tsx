import React, {useEffect, useState} from 'react';
import {Trophy, Lock, TrendingUp, Sparkles} from 'lucide-react';
import {progressApi} from '../../services/progressApi';
import {Achievement, UnlockedAchievement} from '../../types/gamification';
import {useSeason} from '../../contexts/SeasonContext';
import {useNavigate} from 'react-router-dom';

interface AchievementProgress {
    achievement: Achievement;
    progress: number;
    target: number;
    unlocked: boolean;
}

const getRarityTheme = (rarity: string) => {
    switch (rarity.toUpperCase()) {
        case 'COMMON':
            return {
                gradient: 'from-gray-400 to-gray-500',
                bg: 'bg-gray-100',
                border: 'border-gray-300',
                text: 'text-gray-700',
                glow: 'shadow-gray-400/50'
            };
        case 'UNCOMMON':
            return {
                gradient: 'from-green-400 to-green-600',
                bg: 'bg-green-50',
                border: 'border-green-300',
                text: 'text-green-700',
                glow: 'shadow-green-400/50'
            };
        case 'RARE':
            return {
                gradient: 'from-blue-400 to-blue-600',
                bg: 'bg-blue-50',
                border: 'border-blue-300',
                text: 'text-blue-700',
                glow: 'shadow-blue-400/50'
            };
        case 'EPIC':
            return {
                gradient: 'from-purple-400 to-purple-600',
                bg: 'bg-purple-50',
                border: 'border-purple-300',
                text: 'text-purple-700',
                glow: 'shadow-purple-400/50'
            };
        case 'LEGENDARY':
            return {
                gradient: 'from-amber-400 to-orange-600',
                bg: 'bg-amber-50',
                border: 'border-amber-300',
                text: 'text-amber-700',
                glow: 'shadow-amber-400/50'
            };
        default:
            return {
                gradient: 'from-gray-400 to-gray-500',
                bg: 'bg-gray-100',
                border: 'border-gray-300',
                text: 'text-gray-700',
                glow: 'shadow-gray-400/50'
            };
    }
};

const getAchievementEmoji = (category: string) => {
    const cat = category.toUpperCase();
    if (cat.includes('MILESTONE')) return '🎯';
    if (cat.includes('CONSISTENCY') || cat.includes('STREAK')) return '🔥';
    if (cat.includes('VOLUME') || cat.includes('WEIGHT')) return '💪';
    if (cat.includes('ENDURANCE') || cat.includes('CARDIO')) return '🏃';
    if (cat.includes('STRENGTH')) return '🏋️';
    if (cat.includes('SPEED') || cat.includes('PACE')) return '⚡';
    if (cat.includes('DEDICATION') || cat.includes('COMMITMENT')) return '❤️';
    if (cat.includes('WORKOUT') || cat.includes('TRAINING')) return '💯';
    if (cat.includes('DISTANCE')) return '🎽';
    if (cat.includes('TIME') || cat.includes('DURATION')) return '⏱️';
    return '🏆';
};

export const AchievementGalleryPreview: React.FC = () => {
    const {theme} = useSeason();
    const navigate = useNavigate();
    const [unlockedAchievements, setUnlockedAchievements] = useState<UnlockedAchievement[]>([]);
    const [totalAchievements, setTotalAchievements] = useState<number>(0);
    const [nextAchievement, setNextAchievement] = useState<AchievementProgress | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchAchievements = async () => {
            try {
                setLoading(true);
                setError(null);

                const [unlocked, all] = await Promise.all([
                    progressApi.getUnlockedAchievements(),
                    progressApi.getAllAchievements()
                ]);

                const sortedUnlocked = unlocked.sort((a, b) =>
                    new Date(b.unlockedAt).getTime() - new Date(a.unlockedAt).getTime()
                );

                setUnlockedAchievements(sortedUnlocked);
                setTotalAchievements(all.length);

                const unlockedIds = new Set(unlocked.map(u => u.achievementId));
                const lockedAchievements = all
                    .filter(a => !unlockedIds.has(a.achievementId))
                    .filter(a => !a.isHidden)
                    .filter(a => a.category !== 'SPECIAL_HIDDEN')
                    .slice(0, 15);

                if (lockedAchievements.length > 0) {
                    console.log(`🔍 Checking progress for ${lockedAchievements.length} locked achievements...`);

                    const progressData = await Promise.all(
                        lockedAchievements.map(async (achievement) => {
                            try {
                                const progress = await progressApi.getAchievementProgress(achievement.achievementId);
                                return {
                                    achievement,
                                    progress: progress.currentValue || 0,
                                    target: progress.targetValue || 1,
                                    unlocked: progress.unlocked || false,
                                    percentage: progress.targetValue > 0 ? (progress.currentValue / progress.targetValue) * 100 : 0
                                };
                            } catch (error) {
                                console.error(`Failed to get progress for achievement ${achievement.achievementId}:`, error);
                                return null;
                            }
                        })
                    );

                    const validProgress = progressData.filter(p => p !== null);
                    if (validProgress.length > 0) {
                        const closest = validProgress.sort((a, b) => b.percentage - a.percentage)[0];
                        console.log(`🎯 Next to unlock: ${closest.achievement.name} (${Math.round(closest.percentage)}% complete)`);
                        setNextAchievement(closest);
                    } else {
                        console.log('⚠️ No trackable achievements found');
                    }
                } else {
                    console.log('🎉 All trackable achievements unlocked!');
                }

                setLoading(false);
            } catch (err) {
                console.error('Failed to fetch achievements:', err);
                setError('Failed to load achievements');
                setLoading(false);
            }
        };

        fetchAchievements();
    }, []);

    if (loading) {
        return (
            <div
                className="bg-gradient-to-br from-slate-100 to-slate-200 rounded-xl p-4 sm:p-6 border-2 border-slate-300 animate-pulse">
                <div className="h-8 bg-slate-300 rounded w-48 mb-4"></div>
                <div className="grid grid-cols-3 gap-3 mb-4">
                    <div className="h-24 bg-slate-300 rounded"></div>
                    <div className="h-24 bg-slate-300 rounded"></div>
                    <div className="h-24 bg-slate-300 rounded"></div>
                </div>
                <div className="h-20 bg-slate-300 rounded"></div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-gradient-to-br from-red-50 to-red-100 rounded-xl p-4 sm:p-6 border-2 border-red-300">
                <div className="text-center text-red-700">
                    <p className="text-base sm:text-lg font-semibold mb-2">⚠️ {error}</p>
                    <p className="text-xs sm:text-sm text-red-600">Unable to load achievement data</p>
                </div>
            </div>
        );
    }

    const recentAchievements = unlockedAchievements.slice(0, 3);

    return (
        <div
            className={`relative overflow-hidden bg-gradient-to-br ${theme.gradient} rounded-xl border-2 ${theme.border} shadow-xl hover:shadow-2xl transition-shadow`}>
            <div className="absolute inset-0 overflow-hidden pointer-events-none opacity-20">
                <Sparkles className={`absolute top-4 right-4 w-6 h-6 ${theme.textSecondary} animate-pulse`}/>
                <Sparkles className={`absolute bottom-4 left-4 w-4 h-4 ${theme.textTertiary} animate-pulse`}
                          style={{animationDelay: '0.5s'}}/>
                <Sparkles className={`absolute top-1/2 left-1/3 w-5 h-5 ${theme.textSecondary} animate-pulse`}
                          style={{animationDelay: '1s'}}/>
            </div>

            <div className="relative p-4 sm:p-6 space-y-4 sm:space-y-6">
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <div
                            className={`w-10 h-10 sm:w-12 sm:h-12 bg-gradient-to-br ${theme.progressBar} rounded-xl flex items-center justify-center shadow-lg`}>
                            <Trophy className="w-5 h-5 sm:w-6 sm:h-6 text-white"/>
                        </div>
                        <div>
                            <h2 className={`text-lg sm:text-2xl font-black ${theme.textPrimary}`}>
                                Achievements
                            </h2>
                            <p className={`text-xs sm:text-sm ${theme.textSecondary} font-semibold`}>
                                {unlockedAchievements.length}/{totalAchievements} Unlocked
                            </p>
                        </div>
                    </div>
                    <button
                        onClick={() => navigate('/progress/achievements')}
                        className={`px-3 py-1.5 sm:px-4 sm:py-2 bg-gradient-to-r ${theme.buttonGradient} text-white text-xs sm:text-sm font-bold rounded-lg transition-all duration-200 transform hover:scale-105 shadow-lg`}>
                        View All →
                    </button>
                </div>

                {recentAchievements.length > 0 && (
                    <div className="space-y-3">
                        <h3 className={`text-sm sm:text-base font-bold ${theme.textSecondary} flex items-center gap-2`}>
                            <Sparkles className="w-4 h-4"/>
                            Recently Unlocked
                        </h3>
                        <div className="grid grid-cols-3 gap-2 sm:gap-3">
                            {recentAchievements.map((ua) => {
                                const rarityTheme = getRarityTheme(ua.rarity);
                                const emoji = getAchievementEmoji(ua.category);

                                return (
                                    <div key={ua.userAchievementId}
                                         className={`${rarityTheme.bg} rounded-lg p-2 sm:p-3 border-2 ${rarityTheme.border} shadow-lg hover:shadow-xl transition-all duration-200 hover:scale-105 cursor-pointer group`}>
                                        <div className="text-center space-y-1 sm:space-y-2">
                                            <div
                                                className={`text-2xl sm:text-3xl mx-auto w-10 h-10 sm:w-12 sm:h-12 bg-gradient-to-br ${rarityTheme.gradient} rounded-lg flex items-center justify-center shadow-md ${rarityTheme.glow} group-hover:shadow-xl transition-shadow`}>
                                                <span className="filter drop-shadow-sm">{emoji}</span>
                                            </div>
                                            <h4 className={`text-[10px] sm:text-xs font-black ${rarityTheme.text} line-clamp-2 min-h-[28px] sm:min-h-[32px]`}>
                                                {ua.name}
                                            </h4>
                                            <p className="text-[9px] sm:text-[10px] font-bold text-green-600">
                                                +{ua.bonusXp} XP
                                            </p>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                )}

                {nextAchievement && (
                    <div className="bg-slate-100 rounded-lg p-3 sm:p-4 border-2 border-slate-300 shadow-md">
                        <h3 className="text-sm sm:text-base font-bold text-slate-700 mb-3 flex items-center gap-2">
                            <Lock className="w-4 h-4"/>
                            Next to Unlock
                        </h3>
                        <div className="flex items-start gap-3">
                            <div
                                className="w-12 h-12 sm:w-14 sm:h-14 bg-gradient-to-br from-slate-300 to-slate-400 rounded-lg flex items-center justify-center shadow-md flex-shrink-0">
                                <span className="text-2xl sm:text-3xl opacity-50">
                                    {getAchievementEmoji(nextAchievement.achievement.category)}
                                </span>
                            </div>
                            <div className="flex-1 min-w-0">
                                <h4 className="text-sm sm:text-base font-bold text-slate-800 mb-1">
                                    {nextAchievement.achievement.name}
                                </h4>
                                <p className="text-[10px] sm:text-xs text-slate-600 mb-2 line-clamp-2">
                                    {nextAchievement.achievement.description}
                                </p>
                                <div className="space-y-1">
                                    <div className="flex items-center justify-between text-[10px] sm:text-xs">
                                        <span className="text-slate-600 font-semibold">Progress</span>
                                        <span className="text-slate-800 font-bold">
                                            {nextAchievement.progress}/{nextAchievement.target}
                                        </span>
                                    </div>
                                    <div
                                        className="relative h-2 bg-slate-200 rounded-full overflow-hidden border border-slate-300">
                                        <div
                                            className="absolute inset-y-0 left-0 bg-gradient-to-r from-blue-400 to-blue-600 rounded-full transition-all duration-500"
                                            style={{width: `${(nextAchievement.progress / nextAchievement.target) * 100}%`}}>
                                            <div className="absolute inset-0 bg-white/30 animate-pulse"></div>
                                        </div>
                                    </div>
                                    <p className="text-[9px] sm:text-[10px] text-slate-500 font-semibold flex items-center gap-1">
                                        <TrendingUp className="w-3 h-3"/>
                                        {nextAchievement.target - nextAchievement.progress} more to unlock!
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {recentAchievements.length === 0 && (
                    <div className="text-center py-8 space-y-3">
                        <div
                            className={`w-16 h-16 mx-auto bg-gradient-to-br ${theme.progressBar} rounded-full flex items-center justify-center`}>
                            <Trophy className={`w-8 h-8 ${theme.textPrimary}`}/>
                        </div>
                        <div>
                            <p className={`text-base sm:text-lg font-bold ${theme.textPrimary} mb-1`}>
                                No Achievements Yet!
                            </p>
                            <p className={`text-xs sm:text-sm ${theme.textSecondary}`}>
                                Complete your first workout to start earning achievements! 💪
                            </p>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};