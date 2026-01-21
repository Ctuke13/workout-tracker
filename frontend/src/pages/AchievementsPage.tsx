import React, {useEffect, useState} from 'react';
import {Trophy, Lock, Search, ArrowLeft, Sparkles, Filter, X, Crown, Zap} from 'lucide-react';
import {useNavigate} from 'react-router-dom';
import {progressApi} from '../services/progressApi';
import {Achievement, UnlockedAchievement} from '../types/gamification';
import {useSeason} from '../contexts/SeasonContext';

interface AchievementWithProgress extends Achievement {
    unlocked: boolean;
    unlockedAt?: string;
    progress?: number;
    target?: number;
    percentage?: number;
}

const getRarityStyles = (rarity: string, theme: any, unlocked: boolean) => {
    const base = {
        COMMON: {
            emblemGradient: 'from-gray-300 via-gray-400 to-gray-500',
            glowColor: 'shadow-gray-500/50',
            badgeGradient: 'from-gray-400 to-gray-600',
            accentColor: 'text-gray-700',
            ringColor: 'ring-gray-400/30',
            shimmer: 'bg-gradient-to-tr from-transparent via-gray-300/20 to-transparent'
        },
        UNCOMMON: {
            emblemGradient: 'from-green-300 via-green-500 to-green-700',
            glowColor: 'shadow-green-500/60',
            badgeGradient: 'from-green-400 to-green-700',
            accentColor: 'text-green-700',
            ringColor: 'ring-green-400/40',
            shimmer: 'bg-gradient-to-tr from-transparent via-green-300/30 to-transparent'
        },
        RARE: {
            emblemGradient: 'from-blue-300 via-blue-500 to-blue-700',
            glowColor: 'shadow-blue-500/60',
            badgeGradient: 'from-blue-400 to-blue-700',
            accentColor: 'text-blue-700',
            ringColor: 'ring-blue-400/40',
            shimmer: 'bg-gradient-to-tr from-transparent via-blue-300/30 to-transparent'
        },
        EPIC: {
            emblemGradient: 'from-purple-300 via-purple-500 to-purple-700',
            glowColor: 'shadow-purple-500/70',
            badgeGradient: 'from-purple-400 to-purple-700',
            accentColor: 'text-purple-700',
            ringColor: 'ring-purple-400/50',
            shimmer: 'bg-gradient-to-tr from-transparent via-purple-300/40 to-transparent'
        },
        LEGENDARY: {
            emblemGradient: theme.buttonGradient,
            glowColor: `${theme.textPrimary.replace('text-', 'shadow-')}/80`,
            badgeGradient: theme.buttonGradient,
            accentColor: theme.textPrimary,
            ringColor: `${theme.border.replace('border-', 'ring-')}/60`,
            shimmer: `bg-gradient-to-tr from-transparent via-white/40 to-transparent`
        }
    };

    const styles = base[rarity as keyof typeof base] || base.COMMON;

    return {
        ...styles,
        opacity: unlocked ? 'opacity-100' : 'opacity-60',
        filter: unlocked ? 'grayscale-0' : 'grayscale',
        animation: unlocked && rarity === 'LEGENDARY' ? 'animate-pulse' : ''
    };
};

const getCategoryEmoji = (category: string): string => {
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

const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - date.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays}d ago`;
    if (diffDays < 30) return `${Math.floor(diffDays / 7)}w ago`;
    return date.toLocaleDateString('en-US', {month: 'short', day: 'numeric'});
};

const AchievementsPage: React.FC = () => {
    const navigate = useNavigate();
    const {theme} = useSeason();
    const [achievements, setAchievements] = useState<AchievementWithProgress[]>([]);
    const [searchQuery, setSearchQuery] = useState('');
    const [filterRarity, setFilterRarity] = useState<string>('ALL');
    const [filterCategory, setFilterCategory] = useState<string>('ALL');
    const [showUnlocked, setShowUnlocked] = useState(true);
    const [showLocked, setShowLocked] = useState(true);
    const [showFilters, setShowFilters] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        loadAchievements();
    }, []);

    const loadAchievements = async () => {
        try {
            setLoading(true);
            setError(null);

            const [allAchievements, unlockedData] = await Promise.all([
                progressApi.getAllAchievements(),
                progressApi.getUnlockedAchievements()
            ]);

            const unlockedMap = new Map(
                unlockedData.map(u => [u.achievementId, u])
            );

            const achievementsWithProgress: AchievementWithProgress[] = await Promise.all(
                allAchievements
                    .filter(a => !a.isHidden)
                    .map(async (achievement) => {
                        const unlocked = unlockedMap.get(achievement.achievementId);

                        if (unlocked) {
                            return {
                                ...achievement,
                                unlocked: true,
                                unlockedAt: unlocked.unlockedAt
                            };
                        }

                        try {
                            const progress = await progressApi.getAchievementProgress(achievement.achievementId);
                            return {
                                ...achievement,
                                unlocked: false,
                                progress: progress.currentValue || 0,
                                target: progress.targetValue || 1,
                                percentage: progress.targetValue > 0
                                    ? (progress.currentValue / progress.targetValue) * 100
                                    : 0
                            };
                        } catch {
                            return {
                                ...achievement,
                                unlocked: false,
                                progress: 0,
                                target: 1,
                                percentage: 0
                            };
                        }
                    })
            );

            setAchievements(achievementsWithProgress);
            setLoading(false);
        } catch (err) {
            console.error('Failed to load achievements:', err);
            setError('Failed to load achievements');
            setLoading(false);
        }
    };

    const filteredAchievements = achievements.filter(a => {
        if (!showUnlocked && a.unlocked) return false;
        if (!showLocked && !a.unlocked) return false;
        if (filterRarity !== 'ALL' && a.rarity !== filterRarity) return false;
        if (filterCategory !== 'ALL' && a.category !== filterCategory) return false;
        if (searchQuery && !a.name.toLowerCase().includes(searchQuery.toLowerCase())) return false;
        return true;
    });

    const unlockedCount = achievements.filter(a => a.unlocked).length;
    const totalCount = achievements.length;
    const rarities = ['ALL', 'COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY'];
    const categories = ['ALL', ...Array.from(new Set(achievements.map(a => a.category)))];

    if (loading) {
        return (
            <div className={`min-h-screen w-full bg-gradient-to-br ${theme.gradient} pb-20 overflow-x-hidden`}>
                <div className="w-full max-w-7xl mx-auto px-4 md:px-6 py-4 md:py-6">
                    <div className="animate-pulse space-y-4">
                        <div className="h-10 bg-white/20 rounded-lg w-40"></div>
                        <div className="h-32 bg-white/20 rounded-xl"></div>
                        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
                            {[1, 2, 3, 4, 5, 6].map(i => (
                                <div key={i} className="h-56 bg-white/20 rounded-xl"></div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className={`min-h-screen w-full bg-gradient-to-br ${theme.gradient} pb-20 overflow-x-hidden`}>
                <div className="w-full max-w-7xl mx-auto px-4 md:px-6 py-4 md:py-6">
                    <div className="bg-red-50 border-2 border-red-300 rounded-xl p-4 md:p-6 text-center">
                        <p className="text-red-700 font-bold text-base md:text-lg">⚠️ {error}</p>
                        <button
                            onClick={loadAchievements}
                            className="mt-3 px-5 py-2 bg-red-600 text-white rounded-lg font-bold hover:bg-red-700 active:scale-95 transition"
                        >
                            Try Again
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className={`min-h-screen w-full bg-gradient-to-br ${theme.gradient} pb-20 overflow-x-hidden`}>
            {/* Inner container with proper width constraints */}
            <div className="w-full overflow-x-hidden">
                <div className="max-w-7xl mx-auto px-4 md:px-6 py-4 md:py-6 space-y-6">
                    {/* Header with Season Theme */}
                    <div className="flex items-center gap-3">
                        <button
                            onClick={() => navigate('/progress')}
                            className={`flex-shrink-0 p-2 bg-white/10 backdrop-blur-sm hover:bg-white/20 rounded-lg transition active:scale-95 border ${theme.border}`}
                        >
                            <ArrowLeft className={`w-5 h-5 md:w-6 md:h-6 ${theme.textPrimary}`}/>
                        </button>
                        <div className="flex-1 min-w-0">
                            <h1 className={`text-xl md:text-3xl font-black ${theme.textPrimary} flex items-center gap-2 drop-shadow-lg`}>
                                <Trophy className="w-6 h-6 md:w-8 md:h-8 flex-shrink-0"/>
                                <span className="truncate">Achievement Hall</span>
                            </h1>
                            <p className={`text-xs md:text-sm ${theme.textSecondary} font-bold drop-shadow`}>
                                {unlockedCount}/{totalCount} Unlocked • {Math.round((unlockedCount / totalCount) * 100)}%
                                Complete
                            </p>
                        </div>
                    </div>

                    {/* Progress Bar */}
                    <div
                        className={`bg-white/10 backdrop-blur-md rounded-xl p-3 md:p-4 border-2 ${theme.border} shadow-xl`}>
                        <div
                            className={`h-4 bg-black/20 rounded-full overflow-hidden border-2 ${theme.border} relative`}>
                            <div
                                className={`h-full bg-gradient-to-r ${theme.buttonGradient} transition-all duration-700 relative overflow-hidden`}
                                style={{width: `${(unlockedCount / totalCount) * 100}%`}}
                            >
                                <div
                                    className="absolute inset-0 bg-gradient-to-r from-transparent via-white/40 to-transparent animate-shimmer"></div>
                            </div>
                            {unlockedCount > 0 && (
                                <div className="absolute inset-0 flex items-center justify-center">
                                    <span
                                        className={`text-xs font-black ${theme.textPrimary} drop-shadow-[0_2px_4px_rgba(0,0,0,0.8)]`}>
                                        {unlockedCount} / {totalCount}
                                    </span>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Search & Filter */}
                    <div className="flex gap-2 items-center">
                        <div className="relative flex-1 min-w-0 max-w-xs ">
                            <Search
                                className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 md:w-5 md:h-5 text-gray-400 pointer-events-none"/>
                            <input
                                type="text"
                                placeholder="Search..."
                                value={searchQuery}
                                onChange={(e) => setSearchQuery(e.target.value)}
                                className={`w-60 pl-9 md:pl-10 pr-3 py-2.5 text-sm md:text-base bg-white/90 backdrop-blur-sm border-2 ${theme.border} rounded-lg focus:outline-none focus:ring-2 focus:ring-white/50 transition shadow-lg`}
                            />
                        </div>
                        <button
                            onClick={() => setShowFilters(!showFilters)}
                            className={`relative z-10 flex-shrink-0 p-2.5 rounded-lg font-bold text-sm transition active:scale-95 shadow-lg h-11 ${
                                showFilters
                                    ? `bg-gradient-to-r ${theme.buttonGradient} text-white border-2 ${theme.border}`
                                    : `bg-white/90 backdrop-blur-sm border-2 ${theme.border} ${theme.textPrimary}`
                            }`}
                        >
                            <Filter className="w-5 h-5"/>
                        </button>
                    </div>

                    {/* Filters */}
                    {showFilters && (
                        <div
                            className="bg-white/90 backdrop-blur-md rounded-xl p-3 md:p-4 border-2 border-gray-200 space-y-3 shadow-xl">
                            <div className="flex items-center justify-between">
                                <span className="text-sm font-bold text-gray-700">Filters</span>
                                <button onClick={() => setShowFilters(false)}
                                        className="p-1 hover:bg-gray-100 rounded-lg transition">
                                    <X className="w-4 h-4 text-gray-600"/>
                                </button>
                            </div>

                            <div className="grid grid-cols-2 gap-2">
                                <select value={filterRarity} onChange={(e) => setFilterRarity(e.target.value)}
                                        className="px-3 py-2 text-xs md:text-sm bg-white border-2 border-gray-300 rounded-lg font-bold focus:outline-none focus:ring-2 focus:ring-blue-500">
                                    {rarities.map(r => <option key={r} value={r}>{r}</option>)}
                                </select>
                                <select value={filterCategory} onChange={(e) => setFilterCategory(e.target.value)}
                                        className="px-3 py-2 text-xs md:text-sm bg-white border-2 border-gray-300 rounded-lg font-bold focus:outline-none focus:ring-2 focus:ring-blue-500">
                                    {categories.map(c => <option key={c} value={c}>{c.replace('_', ' ')}</option>)}
                                </select>
                            </div>

                            <div className="flex gap-2">
                                <button onClick={() => setShowUnlocked(!showUnlocked)}
                                        className={`flex-1 px-3 py-2 rounded-lg text-xs md:text-sm font-bold transition active:scale-95 ${
                                            showUnlocked ? 'bg-green-500 text-white shadow-md' : 'bg-gray-200 text-gray-600'
                                        }`}>
                                    ✓ Unlocked
                                </button>
                                <button onClick={() => setShowLocked(!showLocked)}
                                        className={`flex-1 px-3 py-2 rounded-lg text-xs md:text-sm font-bold transition active:scale-95 ${
                                            showLocked ? 'bg-blue-500 text-white shadow-md' : 'bg-gray-200 text-gray-600'
                                        }`}>
                                    🔒 Locked
                                </button>
                            </div>
                        </div>
                    )}

                    {/* Emblems Grid */}
                    {filteredAchievements.length === 0 ? (
                        <div className="text-center py-12 md:py-16">
                            <Trophy
                                className={`w-12 h-12 md:w-16 md:h-16 ${theme.textSecondary} mx-auto mb-3 opacity-50`}/>
                            <p className={`text-base md:text-lg font-bold ${theme.textPrimary}`}>No achievements
                                found</p>
                            <p className={`text-xs md:text-sm ${theme.textSecondary} mt-1`}>Try adjusting your
                                filters</p>
                        </div>
                    ) : (
                        <div className="">
                            <div
                                className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4 md:gap-6">
                                {filteredAchievements.map((achievement) => {
                                    const styles = getRarityStyles(achievement.rarity, theme, achievement.unlocked);
                                    const emoji = getCategoryEmoji(achievement.category);

                                    return (
                                        <div key={achievement.achievementId}
                                             className="flex flex-col items-center group py-4">
                                            {/* EMBLEM */}
                                            <div className="relative mb-2.5 flex items-center justify-center">
                                                {/* Contained Glow Effect */}
                                                {achievement.unlocked && (
                                                    <div
                                                        className={`absolute -inset-1 md:-inset-2 bg-gradient-to-br ${styles.emblemGradient} rounded-full blur-md md:blur-lg ${styles.glowColor} opacity-60 ${styles.animation}`}></div>
                                                )}

                                                {/* Main Emblem Shield - SMALLER on mobile */}
                                                <div
                                                    className={`relative w-20 h-24 md:w-28 md:h-32 ${styles.filter} ${styles.opacity} transition-all duration-500 group-hover:scale-105`}>
                                                    {/* Shield Background */}
                                                    <div
                                                        className={`absolute inset-0 bg-gradient-to-br ${styles.emblemGradient} shadow-xl ${styles.glowColor}`}
                                                        style={{
                                                            clipPath: 'polygon(50% 0%, 100% 15%, 100% 75%, 50% 100%, 0% 75%, 0% 15%)'
                                                        }}
                                                    >
                                                        {/* Inner Glow */}
                                                        <div
                                                            className="absolute inset-0 bg-gradient-to-br from-white/30 via-transparent to-black/30"></div>

                                                        {/* Shimmer Effect */}
                                                        {achievement.unlocked && (
                                                            <div
                                                                className={`absolute inset-0 ${styles.shimmer} animate-shimmer opacity-60`}></div>
                                                        )}
                                                    </div>

                                                    {/* Inner Border */}
                                                    <div
                                                        className="absolute inset-1.5 bg-gradient-to-br from-white/40 to-transparent"
                                                        style={{
                                                            clipPath: 'polygon(50% 0%, 100% 15%, 100% 75%, 50% 100%, 0% 75%, 0% 15%)'
                                                        }}
                                                    ></div>

                                                    {/* Emoji Icon */}
                                                    <div
                                                        className="absolute inset-0 flex items-center justify-center pt-1.5 md:pt-2">
                                                        <span
                                                            className="text-3xl md:text-5xl drop-shadow-2xl transform group-hover:scale-110 transition-transform duration-300">
                                                            {emoji}
                                                        </span>
                                                    </div>

                                                    {/* Lock Overlay */}
                                                    {!achievement.unlocked && (
                                                        <div
                                                            className="absolute inset-0 flex items-center justify-center bg-black/40 backdrop-blur-[2px]"
                                                            style={{clipPath: 'polygon(50% 0%, 100% 15%, 100% 75%, 50% 100%, 0% 75%, 0% 15%)'}}>
                                                            <Lock
                                                                className="w-5 h-5 md:w-8 md:h-8 text-white drop-shadow-lg"/>
                                                        </div>
                                                    )}

                                                    {/* Rarity Badge */}
                                                    <div
                                                        className="absolute -top-1.5 md:-top-2 left-1/2 -translate-x-1/2">
                                                        <div
                                                            className={`px-1.5 md:px-2 py-0.5 bg-gradient-to-r ${styles.badgeGradient} rounded-full text-[8px] md:text-[10px] font-black text-white shadow-lg border border-white/50`}>
                                                            {achievement.rarity === 'LEGENDARY' && <Crown
                                                                className="inline w-2 h-2 md:w-3 md:h-3 mr-0.5"/>}
                                                            {achievement.rarity}
                                                        </div>
                                                    </div>

                                                    {/* XP Bonus */}
                                                    {achievement.unlocked && (
                                                        <div
                                                            className="absolute -bottom-1.5 md:-bottom-2 left-1/2 -translate-x-1/2">
                                                            <div
                                                                className={`px-1.5 md:px-2 py-0.5 bg-gradient-to-r ${theme.buttonGradient} rounded-full text-[8px] md:text-[10px] font-black text-white shadow-lg border border-white/50 flex items-center gap-0.5`}>
                                                                <Sparkles className="w-2 h-2 md:w-3 md:h-3"/>
                                                                +{achievement.bonusXp}
                                                            </div>
                                                        </div>
                                                    )}
                                                </div>
                                            </div>

                                            {/* STATUS BAR - SMALLER on mobile */}
                                            <div
                                                className="w-full max-w-[125px] md:max-w-none mx-auto bg-white/90 backdrop-blur-sm rounded-lg p-1.5 md:p-2.5 border border-gray-300 md:border-2 shadow-lg">
                                                {/* Name */}
                                                <h3 className="text-[10px] md:text-sm font-black text-gray-900 text-center leading-tight mb-1 md:mb-1.5 line-clamp-2 min-h-[1.75rem] md:min-h-[2.5rem] flex items-center justify-center px-0.5">
                                                    {achievement.name}
                                                </h3>

                                                {/* Description */}
                                                <p className="text-[8px] md:text-xs text-gray-600 text-center mb-1 md:mb-1.5 line-clamp-2 min-h-[1.5rem] md:min-h-[2rem] px-0.5">
                                                    {achievement.description}
                                                </p>

                                                {/* Progress or Status */}
                                                {achievement.unlocked ? (
                                                    <div className="space-y-0.5 md:space-y-1">
                                                        <div
                                                            className="flex items-center justify-center gap-1 p-1 md:p-1.5 bg-green-100 border border-green-300 rounded">
                                                            <Sparkles
                                                                className="w-2.5 h-2.5 md:w-3 md:h-3 text-green-600"/>
                                                            <span
                                                                className="text-[9px] md:text-[10px] font-bold text-green-700">Unlocked!</span>
                                                        </div>
                                                        {achievement.unlockedAt && (
                                                            <p className="text-[8px] md:text-[9px] text-gray-500 font-semibold text-center">
                                                                {formatDate(achievement.unlockedAt)}
                                                            </p>
                                                        )}
                                                    </div>
                                                ) : achievement.percentage !== undefined ? (
                                                    <div className="space-y-0.5 md:space-y-1">
                                                        <div
                                                            className="flex items-center justify-between text-[9px] md:text-[10px] mb-0.5">
                                                            <span className="font-bold text-gray-600">Progress</span>
                                                            <span className="font-bold text-gray-900">
                                                                {achievement.progress}/{achievement.target}
                                                            </span>
                                                        </div>
                                                        <div
                                                            className="h-1.5 md:h-2 bg-gray-200 rounded-full overflow-hidden border border-gray-300">
                                                            <div
                                                                className={`h-full bg-gradient-to-r ${styles.badgeGradient} transition-all duration-500 relative overflow-hidden`}
                                                                style={{width: `${Math.min(100, achievement.percentage)}%`}}
                                                            >
                                                                <div
                                                                    className="absolute inset-0 bg-gradient-to-r from-transparent via-white/30 to-transparent animate-shimmer"></div>
                                                            </div>
                                                        </div>
                                                        <p className="text-[8px] md:text-[9px] text-gray-600 font-bold text-center">
                                                            {Math.round(achievement.percentage)}% Complete
                                                        </p>
                                                    </div>
                                                ) : (
                                                    <div
                                                        className="p-1 md:p-1.5 bg-gray-100 border border-gray-300 rounded">
                                                        <p className="text-[9px] md:text-[10px] text-gray-600 font-bold text-center">
                                                            Keep Training! 💪
                                                        </p>
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Add shimmer animation */}
            <style>{`
                @keyframes shimmer {
                    0% { transform: translateX(-100%); }
                    100% { transform: translateX(100%); }
                }
                .animate-shimmer {
                    animation: shimmer 2s infinite;
                }
            `}</style>
        </div>
    );
};

export default AchievementsPage;